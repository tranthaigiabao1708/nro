package nro.models.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import nro.models.data.LocalManager;
import nro.models.data.LocalResultSet;
import nro.models.item.Item;
import nro.models.managers.GiftCodeManager;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.player_system.GiftCode;
import nro.models.player_system.Template;
import nro.models.server.Client;
import nro.models.server.Maintenance;
import nro.models.server.Manager;
import nro.models.server.ServerManager;
import nro.models.services.ClanService;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.shop_ky_gui.ConsignShopManager;
import nro.models.utils.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Web Admin Panel của Ngọc Rồng NewGen.
 *
 * Chạy ngay bên trong Game Server bằng HttpServer có sẵn của JDK (không cần cài thêm gì).
 * Cấu hình trong Config.properties:
 *   admin.panel.enabled=true
 *   admin.panel.host=127.0.0.1   (chỉ máy chủ truy cập được; đổi 0.0.0.0 nếu muốn vào từ máy khác)
 *   admin.panel.port=8686
 *   admin.panel.password=...     (BẮT BUỘC đổi trước khi mở cho người khác)
 *
 * Giao diện: file admin_panel/index.html (sửa trực tiếp, không cần build lại Java).
 * Mọi thao tác chỉ áp dụng cho người chơi ĐANG ONLINE (trừ Ban/Mở ban tài khoản) để
 * tránh việc ghi đè dữ liệu với lần lưu tự động của người chơi.
 */
public class AdminPanelServer {

    private static final long SESSION_TTL_MS = 12L * 60 * 60 * 1000;
    private static final String COOKIE_NAME = "nro_admin";
    private static final Path LOG_FILE = Paths.get("log", "admin_panel.log");

    private static AdminPanelServer instance;

    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final Map<String, long[]> loginFails = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final long startTime = System.currentTimeMillis();

    private HttpServer server;
    private String host = "127.0.0.1";
    private int port = 8686;
    private String password = "";

    public static AdminPanelServer gI() {
        if (instance == null) {
            instance = new AdminPanelServer();
        }
        return instance;
    }

    public void start() {
        Properties prop = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream("Config.properties"), StandardCharsets.UTF_8)) {
            prop.load(reader);
        } catch (Exception e) {
            Logger.error("[AdminPanel] Không đọc được Config.properties: " + e.getMessage() + "\n");
        }
        if (!Boolean.parseBoolean(prop.getProperty("admin.panel.enabled", "true").trim())) {
            Logger.warning("[AdminPanel] Đang tắt (admin.panel.enabled=false)\n");
            return;
        }
        host = prop.getProperty("admin.panel.host", "127.0.0.1").trim();
        password = prop.getProperty("admin.panel.password", "").trim();
        try {
            port = Integer.parseInt(prop.getProperty("admin.panel.port", "8686").trim());
        } catch (NumberFormatException e) {
            port = 8686;
        }
        if (password.isEmpty()) {
            Logger.error("[AdminPanel] Chưa đặt admin.panel.password trong Config.properties -> KHÔNG bật Admin Panel.\n");
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/", this::handle);
            server.setExecutor(Executors.newFixedThreadPool(4, r -> {
                Thread t = new Thread(r, "AdminPanel-HTTP");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            Logger.success("[AdminPanel] Đang chạy tại http://" + ("0.0.0.0".equals(host) ? "localhost" : host) + ":" + port + "\n");
        } catch (Exception e) {
            Logger.error("[AdminPanel] Không thể mở cổng " + port + ": " + e.getMessage() + "\n");
        }
    }

    // ============================================================ ROUTER

    private void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();
            if (path.equals("/") || path.equals("/index.html")) {
                serveIndex(ex);
                return;
            }
            if (!path.startsWith("/api/")) {
                sendText(ex, 404, "Not found");
                return;
            }
            if (path.equals("/api/login") && method.equals("POST")) {
                login(ex);
                return;
            }
            if (!isAuthed(ex)) {
                sendJson(ex, 401, err("Chưa đăng nhập hoặc phiên đã hết hạn"));
                return;
            }
            if (method.equals("POST") && !"1".equals(ex.getRequestHeaders().getFirst("X-NRO-Admin"))) {
                sendJson(ex, 403, err("Thiếu header bảo vệ"));
                return;
            }
            Map<String, String> q = parseQuery(ex.getRequestURI().getRawQuery());
            Map<String, String> p = method.equals("POST") ? parseBody(ex) : q;
            JSONObject res;
            switch (path) {
                case "/api/logout" -> res = logout(ex);
                case "/api/status" -> res = status();
                case "/api/players" -> res = players();
                case "/api/items" -> res = searchItems(q.getOrDefault("q", ""));
                case "/api/player/money" -> res = addMoney(p);
                case "/api/player/item" -> res = addItem(p);
                case "/api/player/thoivang" -> res = addThoiVang(p);
                case "/api/player/kick" -> res = kick(p);
                case "/api/account/ban" -> res = ban(p);
                case "/api/broadcast" -> res = broadcast(p);
                case "/api/giftcodes" -> res = listGiftcodes();
                case "/api/giftcode/create" -> res = createGiftcode(p);
                case "/api/giftcode/delete" -> res = deleteGiftcode(p);
                case "/api/server/save" -> res = saveAll();
                case "/api/server/maintenance" -> res = maintenance(p);
                case "/api/server/stop" -> res = stopServer();
                default -> {
                    sendJson(ex, 404, err("API không tồn tại"));
                    return;
                }
            }
            sendJson(ex, 200, res);
        } catch (IllegalArgumentException e) {
            sendJson(ex, 200, err(e.getMessage()));
        } catch (Exception e) {
            Logger.logException(AdminPanelServer.class, e);
            sendJson(ex, 500, err("Lỗi máy chủ: " + e.getMessage()));
        } finally {
            ex.close();
        }
    }

    // ============================================================ AUTH

    private void login(HttpExchange ex) throws IOException {
        String ip = ex.getRemoteAddress().getAddress().getHostAddress();
        long now = System.currentTimeMillis();
        long[] fail = loginFails.computeIfAbsent(ip, k -> new long[]{0, now});
        if (now - fail[1] > 60_000) {
            fail[0] = 0;
            fail[1] = now;
        }
        if (fail[0] >= 5) {
            sendJson(ex, 429, err("Sai mật khẩu quá nhiều lần, thử lại sau 1 phút"));
            return;
        }
        Map<String, String> p = parseBody(ex);
        byte[] given = p.getOrDefault("password", "").getBytes(StandardCharsets.UTF_8);
        byte[] expect = password.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(given, expect)) {
            fail[0]++;
            audit(ip, "LOGIN FAIL");
            sendJson(ex, 401, err("Sai mật khẩu"));
            return;
        }
        loginFails.remove(ip);
        byte[] b = new byte[32];
        random.nextBytes(b);
        StringBuilder token = new StringBuilder();
        for (byte x : b) {
            token.append(String.format("%02x", x));
        }
        sessions.put(token.toString(), now + SESSION_TTL_MS);
        ex.getResponseHeaders().add("Set-Cookie", COOKIE_NAME + "=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + (SESSION_TTL_MS / 1000));
        audit(ip, "LOGIN OK");
        sendJson(ex, 200, ok("Đăng nhập thành công"));
    }

    private JSONObject logout(HttpExchange ex) {
        String t = getToken(ex);
        if (t != null) {
            sessions.remove(t);
        }
        ex.getResponseHeaders().add("Set-Cookie", COOKIE_NAME + "=; Path=/; Max-Age=0");
        return ok("Đã đăng xuất");
    }

    private boolean isAuthed(HttpExchange ex) {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> e.getValue() < now);
        String t = getToken(ex);
        return t != null && sessions.containsKey(t);
    }

    private String getToken(HttpExchange ex) {
        List<String> cookies = ex.getRequestHeaders().get("Cookie");
        if (cookies == null) {
            return null;
        }
        for (String line : cookies) {
            for (String c : line.split(";")) {
                String[] kv = c.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals(COOKIE_NAME)) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    // ============================================================ API

    @SuppressWarnings("unchecked")
    private JSONObject status() {
        Runtime rt = Runtime.getRuntime();
        JSONObject o = ok(null);
        o.put("serverName", ServerManager.NAME_SERVER);
        o.put("online", Client.gI().getPlayers().size());
        o.put("maxPlayer", Manager.MAX_PLAYER);
        o.put("uptimeSec", (System.currentTimeMillis() - startTime) / 1000);
        o.put("memUsedMB", (rt.totalMemory() - rt.freeMemory()) / 1048576);
        o.put("memMaxMB", rt.maxMemory() / 1048576);
        o.put("threads", ManagementFactory.getThreadMXBean().getThreadCount());
        o.put("maintenance", Maintenance.isRunning);
        o.put("gamePort", ServerManager.PORT);
        boolean dbOk;
        try (java.sql.Connection c = LocalManager.getConnection()) {
            dbOk = c.isValid(2);
        } catch (Exception e) {
            dbOk = false;
        }
        o.put("dbOk", dbOk);
        return o;
    }

    @SuppressWarnings("unchecked")
    private JSONObject players() {
        JSONArray arr = new JSONArray();
        for (Player pl : new ArrayList<>(Client.gI().getPlayers())) {
            if (pl == null || pl.isDisposed || pl.inventory == null || pl.nPoint == null) {
                continue;
            }
            JSONObject o = new JSONObject();
            o.put("id", pl.id);
            o.put("name", pl.name);
            o.put("gender", (int) pl.gender);
            o.put("power", pl.nPoint.power);
            o.put("gold", pl.inventory.gold);
            o.put("gem", pl.inventory.gem);
            o.put("ruby", pl.inventory.ruby);
            o.put("thoiVang", countThoiVang(pl));
            o.put("map", pl.zone != null && pl.zone.map != null ? pl.zone.map.mapName + " (" + pl.zone.map.mapId + ") khu " + pl.zone.zoneId : "-");
            o.put("ip", pl.getSession() != null ? pl.getSession().ipAddress : "-");
            o.put("userId", pl.getSession() != null ? pl.getSession().userId : -1);
            o.put("isAdmin", pl.getSession() != null && pl.getSession().isAdmin);
            arr.add(o);
        }
        JSONObject o = ok(null);
        o.put("players", arr);
        return o;
    }

    private int countThoiVang(Player pl) {
        int n = 0;
        try {
            for (int i = 0; i < pl.inventory.itemsBag.size(); i++) {
                Item it = pl.inventory.itemsBag.get(i);
                if (it != null && it.isNotNullItem() && it.template.id == 457) {
                    n += it.quantity;
                }
            }
        } catch (Exception ignored) {
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    private JSONObject searchItems(String keyword) {
        String k = keyword.trim().toLowerCase();
        JSONArray arr = new JSONArray();
        for (Template.ItemTemplate t : Manager.ITEM_TEMPLATES) {
            if (t == null || t.name == null) {
                continue;
            }
            if (k.isEmpty() || t.name.toLowerCase().contains(k) || String.valueOf(t.id).equals(k)) {
                JSONObject o = new JSONObject();
                o.put("id", (int) t.id);
                o.put("name", t.name);
                o.put("type", (int) t.type);
                arr.add(o);
                if (arr.size() >= 60) {
                    break;
                }
            }
        }
        JSONObject o = ok(null);
        o.put("items", arr);
        return o;
    }

    private JSONObject addMoney(Map<String, String> p) {
        Player pl = online(p.get("name"));
        String type = p.getOrDefault("type", "");
        long amount = parseLong(p.get("amount"));
        if (amount == 0 || Math.abs(amount) > 2_000_000_000L) {
            return err("Số lượng phải khác 0 và không vượt quá 2.000.000.000");
        }
        switch (type) {
            case "gold" -> {
                long v = Math.max(0, Math.min(Inventory.LIMIT_GOLD, pl.inventory.gold + amount));
                pl.inventory.gold = v;
            }
            case "gem" -> pl.inventory.gem = (int) clampInt((long) pl.inventory.gem + amount);
            case "ruby" -> pl.inventory.ruby = (int) clampInt((long) pl.inventory.ruby + amount);
            default -> {
                return err("Loại tiền không hợp lệ");
            }
        }
        PlayerService.gI().sendInfoHpMpMoney(pl);
        String label = type.equals("gold") ? "vàng" : type.equals("gem") ? "ngọc xanh" : "hồng ngọc";
        Service.gI().sendThongBao(pl, (amount > 0 ? "Admin đã tặng bạn " : "Admin đã trừ ") + Math.abs(amount) + " " + label);
        audit(null, "MONEY " + type + " " + amount + " -> " + pl.name);
        return ok("Đã " + (amount > 0 ? "cộng " : "trừ ") + Math.abs(amount) + " " + label + " cho " + pl.name);
    }

    private JSONObject addItem(Map<String, String> p) {
        Player pl = online(p.get("name"));
        int itemId = (int) parseLong(p.get("itemId"));
        int quantity = (int) parseLong(p.get("quantity"));
        if (quantity <= 0 || quantity > 100_000) {
            return err("Số lượng phải từ 1 đến 100.000");
        }
        Template.ItemTemplate temp = tpl(itemId);
        if (temp == null) {
            return err("Không tìm thấy vật phẩm ID " + itemId);
        }
        List<Item.ItemOption> options = parseOptions(p.getOrDefault("options", ""));
        int given = 0;
        if (temp.isUpToUp) {
            Item item = ItemService.gI().createNewItem((short) itemId, quantity);
            applyOptions(item, options, itemId);
            if (InventoryService.gI().addItemBag(pl, item)) {
                given = quantity;
            }
        } else {
            if (quantity > 100) {
                return err("Vật phẩm không cộng dồn: tối đa 100 món mỗi lần");
            }
            for (int i = 0; i < quantity; i++) {
                Item item = ItemService.gI().createNewItem((short) itemId);
                applyOptions(item, options, itemId);
                if (!InventoryService.gI().addItemBag(pl, item)) {
                    break;
                }
                given++;
            }
        }
        InventoryService.gI().sendItemBags(pl);
        if (given == 0) {
            return err("Hành trang của " + pl.name + " đã đầy");
        }
        Service.gI().sendThongBao(pl, "Admin đã tặng bạn " + given + " " + temp.name);
        audit(null, "ITEM " + itemId + " x" + given + " opt=" + p.getOrDefault("options", "") + " -> " + pl.name);
        return ok("Đã gửi " + given + "/" + quantity + " x " + temp.name + " cho " + pl.name);
    }

    private JSONObject addThoiVang(Map<String, String> p) {
        Player pl = online(p.get("name"));
        int quantity = (int) parseLong(p.get("quantity"));
        boolean locked = "1".equals(p.get("locked"));
        if (quantity <= 0 || quantity > 1_000_000) {
            return err("Số lượng phải từ 1 đến 1.000.000");
        }
        Item item = ItemService.gI().createNewItem((short) 457, quantity);
        item.itemOptions.add(new Item.ItemOption(locked ? 30 : 73, 0));
        if (!InventoryService.gI().addItemBag(pl, item)) {
            return err("Hành trang của " + pl.name + " đã đầy");
        }
        InventoryService.gI().sendItemBags(pl);
        String label = locked ? "Thỏi vàng khóa" : "Thỏi vàng (giao dịch được)";
        Service.gI().sendThongBao(pl, "Admin đã tặng bạn " + quantity + " " + label);
        audit(null, "THOIVANG " + (locked ? "KHOA " : "THUONG ") + quantity + " -> " + pl.name);
        return ok("Đã gửi " + quantity + " " + label + " cho " + pl.name);
    }

    private JSONObject kick(Map<String, String> p) {
        Player pl = online(p.get("name"));
        String name = pl.name;
        Client.gI().kickSession(pl.getSession());
        audit(null, "KICK " + name);
        return ok("Đã kick " + name + " (dữ liệu được lưu trước khi thoát)");
    }

    private JSONObject ban(Map<String, String> p) throws Exception {
        String name = p.getOrDefault("name", "").trim().toLowerCase();
        boolean doBan = !"0".equals(p.get("ban"));
        if (name.isEmpty()) {
            return err("Nhập tên nhân vật");
        }
        int accountId = -1;
        LocalResultSet rs = null;
        try {
            rs = LocalManager.executeQuery("select account_id from player where name = ? limit 1", name);
            if (rs.first()) {
                accountId = rs.getInt("account_id");
            }
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        if (accountId < 0) {
            return err("Không tìm thấy nhân vật " + name);
        }
        LocalManager.executeUpdate("update account set ban = ? where id = ?", doBan ? 1 : 0, accountId);
        if (doBan) {
            Player pl = Client.gI().getPlayer(name);
            if (pl != null && pl.getSession() != null) {
                Client.gI().kickSession(pl.getSession());
            }
        }
        audit(null, (doBan ? "BAN " : "UNBAN ") + name + " (account " + accountId + ")");
        return ok((doBan ? "Đã khóa" : "Đã mở khóa") + " tài khoản của " + name);
    }

    private JSONObject broadcast(Map<String, String> p) {
        String msg = p.getOrDefault("message", "").trim();
        if (msg.isEmpty() || msg.length() > 500) {
            return err("Nội dung từ 1 đến 500 ký tự");
        }
        Service.gI().sendThongBaoAllPlayer("[Admin] " + msg);
        audit(null, "BROADCAST " + msg);
        return ok("Đã gửi thông báo tới " + Client.gI().getPlayers().size() + " người chơi");
    }

    @SuppressWarnings("unchecked")
    private JSONObject listGiftcodes() {
        JSONArray arr = new JSONArray();
        synchronized (GiftCodeManager.gI().listGiftCode) {
            for (GiftCode g : GiftCodeManager.gI().listGiftCode) {
                JSONObject o = new JSONObject();
                o.put("id", g.id);
                o.put("code", g.code);
                o.put("countLeft", g.countLeft >= 99_999_999 ? -1 : g.countLeft);
                o.put("expired", g.dateexpired != null ? g.dateexpired.toString() : "");
                JSONArray items = new JSONArray();
                for (Map.Entry<Integer, Integer> e : g.detail.entrySet()) {
                    Template.ItemTemplate t = tpl(e.getKey());
                    items.add((t != null ? t.name : "?") + " [" + e.getKey() + "] x" + e.getValue());
                }
                o.put("items", items);
                arr.add(o);
            }
        }
        JSONObject o = ok(null);
        o.put("giftcodes", arr);
        return o;
    }

    /**
     * items: mỗi dòng (hoặc ngăn cách bằng ;) dạng itemId:soLuong[:optId-param,optId-param]
     * ví dụ: 457:50:30-0  |  381:10  |  1635:5:30-0,73-0
     */
    @SuppressWarnings("unchecked")
    private JSONObject createGiftcode(Map<String, String> p) throws Exception {
        String code = p.getOrDefault("code", "").trim();
        if (!code.matches("[A-Za-z0-9_]{3,30}")) {
            return err("Mã chỉ gồm chữ, số, dấu _ (3-30 ký tự)");
        }
        synchronized (GiftCodeManager.gI().listGiftCode) {
            for (GiftCode g : GiftCodeManager.gI().listGiftCode) {
                if (g.code.equals(code)) {
                    return err("Mã " + code + " đã tồn tại");
                }
            }
        }
        int count = (int) parseLong(p.getOrDefault("count", "-1"));
        String expired = p.getOrDefault("expired", "").trim();
        Timestamp expTs;
        try {
            expTs = expired.isEmpty() ? Timestamp.valueOf("2037-12-31 23:59:59")
                    : new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse(expired).getTime() + 86_399_000L);
        } catch (Exception e) {
            return err("Ngày hết hạn phải dạng yyyy-MM-dd");
        }
        GiftCode gc = new GiftCode();
        gc.code = code;
        gc.countLeft = count <= 0 ? 999_999_999 : count;
        gc.datecreate = new Timestamp(System.currentTimeMillis());
        gc.dateexpired = expTs;
        JSONArray detail = new JSONArray();
        for (String line : p.getOrDefault("items", "").split("[;\\n\\r]+")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(":");
            if (parts.length < 2) {
                return err("Dòng vật phẩm sai cú pháp: " + line);
            }
            int id = Integer.parseInt(parts[0].trim());
            int qty = Integer.parseInt(parts[1].trim());
            if (tpl(id) == null || qty <= 0) {
                return err("Vật phẩm không hợp lệ: " + line);
            }
            JSONObject it = new JSONObject();
            it.put("id", id);
            it.put("quantity", qty);
            JSONArray opts = new JSONArray();
            ArrayList<Item.ItemOption> optList = new ArrayList<>();
            if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                for (String op : parts[2].split(",")) {
                    String[] kv = op.trim().split("-");
                    if (kv.length != 2) {
                        return err("Option sai cú pháp (optId-param): " + op);
                    }
                    JSONObject jo = new JSONObject();
                    jo.put("id", Integer.parseInt(kv[0].trim()));
                    jo.put("param", Integer.parseInt(kv[1].trim()));
                    opts.add(jo);
                    optList.add(new Item.ItemOption(Integer.parseInt(kv[0].trim()), Integer.parseInt(kv[1].trim())));
                }
            }
            it.put("options", opts);
            detail.add(it);
            gc.detail.put(id, qty);
            gc.option.put(id, optList);
        }
        if (detail.isEmpty()) {
            return err("Giftcode phải có ít nhất 1 vật phẩm");
        }
        LocalManager.executeUpdate("insert into giftcode (code, count_left, detail, expired) values (?, ?, ?, ?)",
                code, count <= 0 ? -1 : count, detail.toJSONString(), expTs);
        LocalResultSet rs = null;
        try {
            rs = LocalManager.executeQuery("select id from giftcode where code = ? order by id desc limit 1", code);
            if (rs.first()) {
                gc.id = rs.getInt("id");
            }
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        synchronized (GiftCodeManager.gI().listGiftCode) {
            GiftCodeManager.gI().listGiftCode.add(gc);
        }
        audit(null, "GIFTCODE CREATE " + code + " count=" + count + " items=" + detail.toJSONString());
        return ok("Đã tạo giftcode " + code);
    }

    private JSONObject deleteGiftcode(Map<String, String> p) throws Exception {
        int id = (int) parseLong(p.get("id"));
        LocalManager.executeUpdate("delete from giftcode where id = ?", id);
        String code = null;
        synchronized (GiftCodeManager.gI().listGiftCode) {
            Iterator<GiftCode> itr = GiftCodeManager.gI().listGiftCode.iterator();
            while (itr.hasNext()) {
                GiftCode g = itr.next();
                if (g.id == id) {
                    code = g.code;
                    itr.remove();
                }
            }
        }
        audit(null, "GIFTCODE DELETE " + id + " " + code);
        return code == null ? err("Không tìm thấy giftcode") : ok("Đã xóa giftcode " + code);
    }

    private JSONObject saveAll() {
        long st = System.currentTimeMillis();
        Client.gI().saveAllPlayers();
        try {
            ClanService.gI().close();
        } catch (Exception ignored) {
        }
        try {
            ConsignShopManager.gI().save();
        } catch (Exception ignored) {
        }
        audit(null, "SAVE ALL");
        return ok("Đã lưu toàn bộ người chơi, bang hội & ký gửi (" + (System.currentTimeMillis() - st) + " ms)");
    }

    private JSONObject maintenance(Map<String, String> p) {
        if (Maintenance.isRunning) {
            return err("Server đang trong quá trình bảo trì");
        }
        int seconds = (int) Math.max(10, Math.min(3600, parseLong(p.getOrDefault("seconds", "60"))));
        Maintenance.gI().startSeconds(seconds);
        audit(null, "MAINTENANCE " + seconds + "s");
        return ok("Đã bắt đầu đếm ngược bảo trì " + seconds + " giây (server sẽ lưu dữ liệu rồi tự khởi động lại)");
    }

    private JSONObject stopServer() {
        audit(null, "STOP SERVER");
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(500);
                Service.gI().sendThongBaoAllPlayer("Máy chủ đang tắt để bảo trì. Dữ liệu của bạn đã được lưu!");
                ServerManager.isRunning = false;
                try {
                    ClanService.gI().close();
                } catch (Exception ignored) {
                }
                try {
                    ConsignShopManager.gI().save();
                } catch (Exception ignored) {
                }
                Client.gI().close();
                Logger.success("[AdminPanel] SERVER SAVED & STOPPED CLEANLY!\n");
            } catch (Exception e) {
                Logger.logException(AdminPanelServer.class, e);
            }
            System.exit(0);
        }, "AdminPanel-Stop");
        t.start();
        return ok("Đang lưu dữ liệu và tắt server...");
    }

    // ============================================================ HELPERS

    private Player online(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu tên nhân vật");
        }
        Player pl = Client.gI().getPlayer(name.trim().toLowerCase());
        if (pl == null) {
            pl = Client.gI().getPlayer(name.trim());
        }
        if (pl == null || pl.isDisposed || pl.beforeDispose || pl.inventory == null) {
            throw new IllegalArgumentException("Nhân vật " + name + " không online");
        }
        return pl;
    }

    private List<Item.ItemOption> parseOptions(String raw) {
        List<Item.ItemOption> list = new ArrayList<>();
        for (String s : raw.trim().split("[\\s,;]+")) {
            if (s.contains(":")) {
                String[] kv = s.split(":");
                list.add(new Item.ItemOption(Integer.parseInt(kv[0].trim()), Integer.parseInt(kv[1].trim())));
            }
        }
        return list;
    }

    private void applyOptions(Item item, List<Item.ItemOption> options, int itemId) {
        if (!options.isEmpty()) {
            item.itemOptions = new ArrayList<>();
            for (Item.ItemOption o : options) {
                item.itemOptions.add(new Item.ItemOption(o.optionTemplate.id, o.param));
            }
        } else {
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) itemId);
            if (ops != null && !ops.isEmpty()) {
                item.itemOptions = ops;
            }
        }
    }

    private static Template.ItemTemplate tpl(int id) {
        if (id < 0 || id >= Manager.ITEM_TEMPLATES.size()) {
            return null;
        }
        return Manager.ITEM_TEMPLATES.get(id);
    }

    private static long clampInt(long v) {
        return Math.max(0, Math.min(Integer.MAX_VALUE, v));
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s == null ? "" : s.trim().replace(".", "").replace(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số không hợp lệ: " + s);
        }
    }

    private void audit(String ip, String action) {
        String line = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + (ip != null ? "[" + ip + "] " : "") + action;
        Logger.warning("[AdminPanel] " + action + "\n");
        try {
            Files.createDirectories(LOG_FILE.getParent());
            Files.write(LOG_FILE, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    private Map<String, String> parseBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] data = in.readNBytes(64 * 1024);
            return parseQuery(new String(data, StandardCharsets.UTF_8));
        }
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return map;
        }
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }

    private void serveIndex(HttpExchange ex) throws IOException {
        Path f = Paths.get("admin_panel", "index.html");
        if (!Files.exists(f)) {
            sendText(ex, 500, "Thiếu file admin_panel/index.html");
            return;
        }
        byte[] data = Files.readAllBytes(f);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.getResponseHeaders().set("X-Frame-Options", "DENY");
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject ok(String msg) {
        JSONObject o = new JSONObject();
        o.put("ok", true);
        if (msg != null) {
            o.put("message", msg);
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject err(String msg) {
        JSONObject o = new JSONObject();
        o.put("ok", false);
        o.put("message", msg);
        return o;
    }

    private void sendJson(HttpExchange ex, int code, JSONObject o) throws IOException {
        byte[] data = o.toJSONString().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    private void sendText(HttpExchange ex, int code, String s) throws IOException {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }
}
