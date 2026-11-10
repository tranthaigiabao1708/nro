import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * KIỂM THỬ TẢI TOÀN DIỆN NGỌC RỒNG NEWGEN (100 người chơi đồng thời):
 *  - Nhóm 1 (ltbot000..059 - 60 bot): Farm quái Đồi hoa cúc / Đồi nấm tím / Đồi hoang (map 1/8/15),
 *    làm nhiệm vụ nhặt Đùi gà (TASK_2_0), tranh nhau nhặt vật phẩm rơi từ quái.
 *  - Nhóm 2 (ltbot060..079 - 20 bot): Làm nhiệm vụ đánh Mộc Nhân (TASK_1_0), nói chuyện NPC,
 *    nâng tiềm năng, dùng đậu thần tại Làng Aru / Mori / Kakarot (map 0/7/14).
 *  - Nhóm 3 (ltbot080..089 - 10 bot): Săn Boss tập thể (dịch chuyển tới Boss đang sống,
 *    tấn công Boss bằng cmd -60, tiêu diệt Boss và nhặt Thỏi vàng rơi từ Boss).
 *  - Nhóm 4 (ltbot090..099 - 10 bot): Giao dịch 2 chiều hợp lệ + Test chặn Exploit giao dịch 1 chiều + Chợ ký gửi.
 *  - Pha C: Đăng nhập 2 phiên cùng lúc trên 10 tài khoản ltdup00..09.
 *  - Pha D: Ngắt kết nối đồng loạt 100 bot, kiểm tra kẹt nhân vật ma và đối chiếu RAM vs DB.
 */
@SuppressWarnings("unchecked")
public class LoadTest {

    static String HOST = "127.0.0.1";
    static int PORT = 14445;
    static String PANEL = "http://127.0.0.1:8686";
    static String PANEL_PW = "NewGen@2026";
    static String DB_URL = "jdbc:mysql://127.0.0.1:3306/nro_newgen?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    static int N = 100;
    static int DURATION = 180;
    static final String PASS = "lt123456";
    static final int[] VILLAGE = {0, 7, 14};
    static final int[] VILLAGE_Y = {432, 432, 408};
    static final int[][] VILLAGE_MOB_X = {{780, 900, 1020, 660}, {708, 804, 900, 996}, {708, 804, 900, 996}};

    static final int[] FARM_MAP = {1, 8, 15};
    static final int[][] FARM_MOB_X = {{348, 804, 972, 540}, {276, 444, 636, 852}, {228, 444, 612, 900}};
    static final int[][] FARM_MOB_Y = {{384, 408, 360, 408}, {384, 336, 288, 360}, {408, 360, 360, 312}};

    static final int[] SKILL = {0, 2, 4};
    static final int[] HAIR = {64, 9, 6};
    static final int[] BOSS_IDS_TO_HUNT = {-20, -21, -22, -23, -28, -29, -30, -31, -32, -33, -34, -35, -36, -37, -100, -101, -203};
    static final Set<Integer> SPECIAL = Set.of(-32, -66, -74, 11, -67, -87, 66);

    // ======= thống kê toàn cục
    static final AtomicLong msgIn = new AtomicLong(), msgOut = new AtomicLong(), bytesIn = new AtomicLong();
    static final AtomicInteger loginOk = new AtomicInteger(), loginFail = new AtomicInteger(), connFail = new AtomicInteger();
    static final AtomicInteger disconnectsUnexpected = new AtomicInteger(), relogs = new AtomicInteger();
    static final AtomicInteger mobKills = new AtomicInteger(), pickOk = new AtomicInteger(), pickReq = new AtomicInteger();
    static final AtomicInteger bossAttacks = new AtomicInteger(), bossKills = new AtomicInteger();
    static final AtomicInteger taskUpdates = new AtomicInteger(), npcTalks = new AtomicInteger();
    static final AtomicInteger tradeInvites = new AtomicInteger(), tradeLegitOk = new AtomicInteger(), tradeExploitBlocked = new AtomicInteger(), tradeExploitLeaked = new AtomicInteger();
    static final AtomicInteger protoErrors = new AtomicInteger();
    static final ConcurrentHashMap<String, AtomicInteger> notices = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Set<String>> pickWinners = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, AtomicInteger> doubleDeath = new ConcurrentHashMap<>();
    static final List<String> report = Collections.synchronizedList(new ArrayList<>());
    static volatile boolean running = true, actionsEnabled = true;
    static final Map<String, Long> playerIds = new ConcurrentHashMap<>();
    static final Map<Integer, Bot> botsByIdx = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length > 0) N = Integer.parseInt(args[0]);
        if (args.length > 1) DURATION = Integer.parseInt(args[1]);
        if (args.length > 2) PANEL_PW = args[2];
        log("===== KIỂM THỬ TẢI TOÀN DIỆN NGỌC RỒNG NEWGEN: " + N + " bot, " + DURATION + " giây =====");
        Panel panel = new Panel();
        panel.login();
        JSONObject st0 = panel.get("/api/status");
        log("Trạng thái ban đầu: " + st0.toJSONString());

        // ---------- A. tạo nhân vật & thiết lập phân vai
        phaseCreate();
        setupBotRolesInDb();

        // ---------- B. tải chính
        List<Bot> bots = new ArrayList<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        long startLoad = System.currentTimeMillis();
        for (int i = 0; i < N; i++) {
            Bot b = new Bot(i, botName(i), true);
            bots.add(b);
            botsByIdx.put(i, b);
            pool.submit(b::runLoop);
            Thread.sleep(120);
        }
        log("Đã khởi chạy " + N + " bot sau " + (System.currentTimeMillis() - startLoad) / 1000 + "s");

        long end = startLoad + DURATION * 1000L;
        int dupTestAt = DURATION / 2;
        boolean dupDone = false;
        while (System.currentTimeMillis() < end) {
            Thread.sleep(10_000);
            long t0 = System.nanoTime();
            JSONObject st = panel.get("/api/status");
            long panelMs = (System.nanoTime() - t0) / 1_000_000;
            String s = String.format("t=%3ds on=%s ram=%sMB thr=%s api=%dms | kill=%d pick=%d/%d bossHit=%d bossKill=%d nvUp=%d gd2Chieu=%d chanGD1Phia=%d relog=%d matKN=%d",
                    (System.currentTimeMillis() - startLoad) / 1000, st.get("online"), st.get("memUsedMB"), st.get("threads"), panelMs,
                    mobKills.get(), pickOk.get(), pickReq.get(), bossAttacks.get(), bossKills.get(),
                    taskUpdates.get(), tradeLegitOk.get(), tradeExploitBlocked.get(), relogs.get(), disconnectsUnexpected.get());
            log(s);
            if (!dupDone && System.currentTimeMillis() - startLoad > dupTestAt * 1000L) {
                dupDone = true;
                pool.submit(() -> phaseDualLogin(panel));
            }
        }

        // ---------- D. dừng hành động, chụp dữ liệu RAM, ngắt kết nối đồng loạt, so sánh DB
        actionsEnabled = false;
        log("Dừng mọi hành động, chờ 4s cho dữ liệu ổn định...");
        Thread.sleep(4000);
        JSONObject snap = panel.get("/api/players");
        Map<String, JSONObject> mem = new HashMap<>();
        Map<String, Integer> nameCount = new HashMap<>();
        for (Object o : (JSONArray) snap.getOrDefault("players", new JSONArray())) {
            JSONObject p = (JSONObject) o;
            String name = (String) p.get("name");
            nameCount.merge(name, 1, Integer::sum);
            mem.put(name, p);
        }
        nameCount.forEach((k, v) -> { if (v > 1) report.add("NHÂN BẢN: nhân vật " + k + " đang online " + v + " bản cùng lúc"); });
        log("Chụp dữ liệu RAM của " + mem.size() + " nhân vật, ngắt kết nối đồng loạt " + bots.size() + " bot...");
        running = false;
        for (Bot b : bots) b.close();
        long waitStart = System.currentTimeMillis();
        while (System.currentTimeMillis() - waitStart < 30_000) {
            Thread.sleep(1000);
            Object on = panel.get("/api/status").get("online");
            if (((Number) on).intValue() == 0) break;
        }
        int remainingOnline = ((Number) panel.get("/api/status").get("online")).intValue();
        log("Tất cả đã thoát sau " + (System.currentTimeMillis() - waitStart) / 1000 + "s (online=" + remainingOnline + ")");
        if (remainingOnline > 0) {
            report.add("KẸT NHÂN VẬT MA: vẫn còn online=" + remainingOnline + " sau khi tất cả socket đã đóng");
        }
        Thread.sleep(2000);
        verifyDb(mem);

        // ---------- tổng kết
        log("");
        log("================= KẾT QUẢ KIỂM THỬ TOÀN DIỆN =================");
        log("Đăng nhập thành công: " + loginOk.get() + " | thất bại: " + loginFail.get() + " | lỗi kết nối: " + connFail.get());
        log("Mất kết nối ngoài ý muốn: " + disconnectsUnexpected.get() + " | thoát-vào lại chủ động: " + relogs.get());
        log("Tin nhắn nhận: " + msgIn.get() + " (" + bytesIn.get() / 1024 / 1024 + " MB) | gửi: " + msgOut.get() + " | lỗi giải mã: " + protoErrors.get());
        log("Farm & Nhặt đồ: Quái bị hạ = " + mobKills.get() + " | Nhặt đồ thành công = " + pickOk.get() + "/" + pickReq.get());
        log("Săn Boss: Đòn đánh Boss = " + bossAttacks.get() + " | Boss bị tiêu diệt = " + bossKills.get());
        log("Nhiệm vụ & NPC: Tiến độ nhiệm vụ tăng = " + taskUpdates.get() + " | Tương tác NPC = " + npcTalks.get());
        log("Giao dịch: Lời mời = " + tradeInvites.get() + " | GD 2 chiều thành công = " + tradeLegitOk.get()
                + " | Chặn exploit GD 1 chiều = " + tradeExploitBlocked.get() + " | Lọt exploit GD 1 chiều = " + tradeExploitLeaked.get());
        if (tradeExploitLeaked.get() > 0) {
            report.add("LỖI GIAO DỊCH 1 CHIỀU: lọt " + tradeExploitLeaked.get() + " lần giao dịch không cần đối phương đồng ý");
        }
        int dupPick = 0;
        for (Map.Entry<String, Set<String>> e : pickWinners.entrySet()) {
            if (e.getValue().size() > 1) {
                dupPick++;
                if (dupPick <= 10) report.add("X2 NHẶT ĐỒ: vật phẩm " + e.getKey() + " được nhặt bởi " + e.getValue());
            }
        }
        if (dupPick > 10) report.add("... tổng cộng " + dupPick + " vật phẩm bị nhặt nhiều lần");
        log("Thông báo server thường gặp:");
        notices.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).limit(15)
                .forEach(e -> log("   " + e.getValue().get() + " x  " + e.getKey()));
        log("");
        log("PHÁT HIỆN LỖI (" + report.size() + "):");
        if (report.isEmpty()) log("   0 LỖI — Toàn bộ kiểm thử đều vượt qua an toàn!");
        synchronized (report) { for (String r : report) log("   - " + r); }
        Files.write(Paths.get("../tools/LoadTest/ket_qua_" + LocalDateTime.now().toString().replace(':', '-').substring(0, 19) + ".log"),
                LOG.toString().getBytes(StandardCharsets.UTF_8));
        pool.shutdownNow();
        System.exit(0);
    }

    static String botName(int i) { return i < N ? String.format("ltbot%03d", i) : String.format("ltdup%02d", i - N); }

    static void setupBotRolesInDb() throws Exception {
        try (Connection c = DriverManager.getConnection(DB_URL, "root", "")) {
            for (int i = 0; i < N + 10; i++) {
                String name = botName(i);
                int g = i % 3;
                int mapId;
                int x;
                int y;
                String dataTask;
                String dataPoint;
                boolean isAdmin = false;

                if (i < 60) {
                    // Nhóm 1 (0..59): Farm quái & nhặt đùi gà (TASK_2_0) tại map 1, 8, 15
                    mapId = FARM_MAP[g];
                    int m = i % 4;
                    x = FARM_MOB_X[g][m];
                    y = FARM_MOB_Y[g][m];
                    dataTask = "[2,0,0]";
                    dataPoint = "[0,2000000,2000000,1000,1000,50000,50000,300,100,10,0,0,50000,50000]";
                } else if (i < 80) {
                    // Nhóm 2 (60..79): Nhiệm vụ Mộc Nhân (TASK_1_0) & NPC tại làng 0, 7, 14
                    mapId = VILLAGE[g];
                    int m = i % 4;
                    x = VILLAGE_MOB_X[g][m];
                    y = VILLAGE_Y[g];
                    dataTask = "[1,0,0]";
                    dataPoint = "[0,500000,500000,1000,1000,20000,20000,50,50,5,0,0,20000,20000]";
                } else if (i < 90) {
                    // Nhóm 3 (80..89): Săn Boss (is_admin=1 để dùng cmd -118 dịch chuyển tới Boss, chỉ số cao)
                    mapId = 68;
                    x = 500 + (i - 80) * 40;
                    y = 312;
                    dataTask = "[19,0,0]";
                    dataPoint = "[9,40000000000,40000000000,1000,1000,2500000,2500000,600000,5000,30,0,0,2500000,2500000]";
                    isAdmin = true;
                } else {
                    // Nhóm 4 (90..99) & ltdup: Giao dịch tại Đảo Kamê (map 5, khu 0 riêng biệt cho 10 bot GD)
                    mapId = 5;
                    x = 400 + (i % 10) * 20;
                    y = 408;
                    dataTask = "[10,0,0]";
                    dataPoint = "[5,20000000000,20000000000,1000,1000,100000,100000,5000,200,10,0,0,100000,100000]";
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE player SET data_location = ?, data_task = ?, data_point = ? WHERE name = ?")) {
                    ps.setString(1, "[" + mapId + "," + x + "," + y + "]");
                    ps.setString(2, dataTask);
                    ps.setString(3, dataPoint);
                    ps.setString(4, name);
                    ps.executeUpdate();
                }
                if (isAdmin) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE account SET is_admin = 1 WHERE username = ?")) {
                        ps.setString(1, name);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM player WHERE name = ?")) {
                    ps.setString(1, name);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) playerIds.put(name, rs.getLong(1));
                }
            }
        }
        log("Đã cấu hình 4 nhóm kiểm thử cho " + playerIds.size() + " nhân vật (60 Farm/Nhặt đồ, 20 Nhiệm vụ/NPC, 10 Săn Boss, 10 Giao dịch/Ký gửi).");
    }

    // ===================================================== PHASE A
    static void phaseCreate() throws Exception {
        Set<String> existing = new HashSet<>();
        try (Connection c = DriverManager.getConnection(DB_URL, "root", "");
             ResultSet rs = c.createStatement().executeQuery("select name from player where name like 'ltbot%' or name like 'ltdup%'")) {
            while (rs.next()) existing.add(rs.getString(1));
        }
        List<Integer> todo = new ArrayList<>();
        for (int i = 0; i < N + 10; i++) if (!existing.contains(botName(i))) todo.add(i);
        log("A. Tạo nhân vật: đã có " + existing.size() + ", cần tạo " + todo.size());
        ExecutorService ex = Executors.newFixedThreadPool(20);
        AtomicInteger created = new AtomicInteger();
        List<Future<?>> fs = new ArrayList<>();
        for (int i : todo) {
            fs.add(ex.submit(() -> {
                Bot b = new Bot(i, botName(i), false);
                try {
                    if (b.connectAndLogin(20_000)) created.incrementAndGet();
                    Thread.sleep(1200);
                } catch (Exception e) {
                    report.add("Tạo nhân vật " + botName(i) + " lỗi: " + e);
                } finally {
                    b.close();
                }
            }));
        }
        for (Future<?> f : fs) f.get();
        ex.shutdown();
        log("A. Đã tạo/đăng nhập " + created.get() + "/" + todo.size() + " nhân vật. Chờ lưu khi thoát...");
        Thread.sleep(4000);
    }

    // ===================================================== PHASE C
    static void phaseDualLogin(Panel panel) {
        try {
            log("C. Kiểm thử đăng nhập 2 phiên cùng lúc cho 10 tài khoản ltdup00..09");
            List<Bot> pair = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                pair.add(new Bot(N + i, botName(N + i), false));
                pair.add(new Bot(N + i, botName(N + i), false));
            }
            for (Bot b : pair) {
                b.connectHandshake();
                b.closedByMe = true; // Bị server kick khi chống đăng nhập 2 tab là hợp lệ
            }
            CountDownLatch go = new CountDownLatch(1);
            List<Thread> ts = new ArrayList<>();
            for (Bot b : pair) {
                Thread t = new Thread(() -> { try { go.await(); b.sendLogin(); } catch (Exception ignored) { } });
                t.start();
                ts.add(t);
            }
            go.countDown();
            for (Thread t : ts) t.join();
            Thread.sleep(5000);
            JSONObject snap = panel.get("/api/players");
            Map<String, Integer> cnt = new HashMap<>();
            for (Object o : (JSONArray) snap.getOrDefault("players", new JSONArray())) {
                String name = (String) ((JSONObject) o).get("name");
                if (name.startsWith("ltdup")) cnt.merge(name, 1, Integer::sum);
            }
            int bothIn = 0;
            for (int i = 0; i < 10; i++) {
                Bot a = pair.get(i * 2), b = pair.get(i * 2 + 1);
                if (a.inGame && b.inGame && a.connected() && b.connected()) bothIn++;
            }
            int dupOnline = 0;
            for (int v : cnt.values()) if (v > 1) dupOnline++;
            log("C. Kết quả: " + cnt.size() + " tài khoản online, " + dupOnline + " tài khoản có 2 bản trong server, "
                    + bothIn + " cặp cả 2 phiên đều vào game & còn kết nối");
            if (dupOnline > 0 || bothIn > 0) {
                report.add("ĐĂNG NHẬP 2 PHIÊN: " + dupOnline + "/10 tài khoản có 2 nhân vật cùng online, " + bothIn + "/10 cặp cả 2 phiên đều trong game");
            }
            for (Bot b : pair) b.close();
        } catch (Exception e) {
            report.add("Pha đăng nhập 2 phiên lỗi: " + e);
        }
    }

    // ===================================================== PHASE D
    static void verifyDb(Map<String, JSONObject> mem) throws Exception {
        JSONParser jp = new JSONParser();
        int checked = 0, mismatch = 0, missing = 0;
        try (Connection c = DriverManager.getConnection(DB_URL, "root", "")) {
            try (ResultSet rs = c.createStatement().executeQuery("select name, count(*) n from player group by name having n > 1")) {
                while (rs.next()) report.add("DB: tên nhân vật trùng: " + rs.getString(1) + " x" + rs.getInt(2));
            }
            for (Map.Entry<String, JSONObject> e : mem.entrySet()) {
                String name = e.getKey();
                if (!name.startsWith("ltbot")) continue;
                JSONObject m = e.getValue();
                try (PreparedStatement ps = c.prepareStatement("select data_inventory, data_point, items_bag from player where name = ?")) {
                    ps.setString(1, name);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) { missing++; continue; }
                    JSONArray inv = (JSONArray) jp.parse(rs.getString(1));
                    JSONArray pt = (JSONArray) jp.parse(rs.getString(2));
                    JSONArray bag = (JSONArray) jp.parse(rs.getString(3));
                    long gold = ((Number) inv.get(0)).longValue();
                    long gem = ((Number) inv.get(1)).longValue();
                    long ruby = ((Number) inv.get(2)).longValue();
                    long power = ((Number) pt.get(1)).longValue();
                    long tv = 0;
                    for (Object it : bag) {
                        JSONArray item = (JSONArray) jp.parse(it.toString());
                        if (((Number) item.get(0)).intValue() == 457) tv += ((Number) item.get(1)).longValue();
                    }
                    checked++;
                    List<String> diff = new ArrayList<>();
                    cmp(diff, "vàng", ((Number) m.get("gold")).longValue(), gold);
                    cmp(diff, "ngọc", ((Number) m.get("gem")).longValue(), gem);
                    cmp(diff, "hồng ngọc", ((Number) m.get("ruby")).longValue(), ruby);
                    cmp(diff, "sức mạnh", ((Number) m.get("power")).longValue(), power);
                    cmp(diff, "thỏi vàng", ((Number) m.get("thoiVang")).longValue(), tv);
                    if (!diff.isEmpty()) {
                        mismatch++;
                        if (mismatch <= 10) report.add("LỆCH DỮ LIỆU SAU KHI LƯU: " + name + " " + diff);
                    }
                    if (gold < 0 || gem < 0 || ruby < 0) report.add("DB: giá trị âm ở " + name + " vàng=" + gold + " ngọc=" + gem + " ruby=" + ruby);
                }
            }
        }
        log("D. Đối chiếu RAM vs DB: kiểm tra " + checked + " nhân vật, lệch " + mismatch + ", không tìm thấy " + missing);
        if (mismatch > 10) report.add("... tổng cộng " + mismatch + " nhân vật bị lệch dữ liệu sau khi lưu");
    }

    static void cmp(List<String> diff, String label, long mem, long db) {
        if (mem != db) diff.add(label + " RAM=" + mem + " DB=" + db);
    }

    static class DropInfo {
        final int itemMapId, tempId, x, y, ownerId;
        DropInfo(int itemMapId, int tempId, int x, int y, int ownerId) {
            this.itemMapId = itemMapId;
            this.tempId = tempId;
            this.x = x;
            this.y = y;
            this.ownerId = ownerId;
        }
    }

    static class BossPos {
        final int id;
        volatile int x, y;
        BossPos(int id, int x, int y) { this.id = id; this.x = x; this.y = y; }
    }

    // ===================================================== BOT
    static class Bot {
        final int idx, gender;
        final String name;
        final boolean loadMode;
        final Random rnd;
        Socket sock;
        DataInputStream in;
        DataOutputStream out;
        byte[] key;
        int curR, curW;
        volatile boolean keyReady, inGame, needCreate, closedByMe;
        volatile boolean tradeOpen, partnerLocked, myLocked;
        volatile long lastPickSentMs;
        volatile int currentMapId = -1;
        final Map<Integer, DropInfo> seenDrops = new ConcurrentHashMap<>();
        final Map<Integer, BossPos> bossesInZone = new ConcurrentHashMap<>();
        final Set<Integer> pendingInvites = ConcurrentHashMap.newKeySet();
        Thread reader;

        Bot(int idx, String name, boolean loadMode) {
            this.idx = idx;
            this.name = name;
            this.gender = idx % 3;
            this.loadMode = loadMode;
            this.rnd = new Random(idx * 7919L + System.nanoTime());
        }

        boolean connected() { return sock != null && !sock.isClosed() && reader != null && reader.isAlive(); }

        void connectHandshake() throws Exception {
            curR = curW = 0;
            keyReady = inGame = needCreate = closedByMe = false;
            tradeOpen = partnerLocked = false;
            sock = new Socket();
            sock.connect(new InetSocketAddress(HOST, PORT), 5000);
            sock.setTcpNoDelay(true);
            in = new DataInputStream(new BufferedInputStream(sock.getInputStream(), 65536));
            out = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
            synchronized (this) {
                out.writeByte(-27);
                out.writeShort(0);
                out.flush();
            }
            reader = new Thread(this::readLoop, "reader-" + name);
            reader.setDaemon(true);
            reader.start();
            long t = System.currentTimeMillis();
            while (!keyReady && System.currentTimeMillis() - t < 5000) Thread.sleep(10);
            if (!keyReady) throw new IOException("không nhận được khóa");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(2); d.writeByte(4); d.writeByte(1); d.writeBoolean(false);
            d.writeInt(1024); d.writeInt(600); d.writeBoolean(true); d.writeBoolean(true);
            d.writeUTF("PC|2.4.0");
            send(-29, bo.toByteArray());
        }

        void sendLogin() throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(0); d.writeUTF(name); d.writeUTF(PASS);
            send(-29, bo.toByteArray());
        }

        boolean connectAndLogin(long timeoutMs) throws Exception {
            try {
                connectHandshake();
            } catch (Exception e) {
                connFail.incrementAndGet();
                throw e;
            }
            sendLogin();
            long t = System.currentTimeMillis();
            boolean createSent = false;
            while (System.currentTimeMillis() - t < timeoutMs) {
                if (inGame) {
                    loginOk.incrementAndGet();
                    afterEnter();
                    return true;
                }
                if (needCreate && !createSent) {
                    createSent = true;
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    DataOutputStream d = new DataOutputStream(bo);
                    d.writeByte(2); d.writeUTF(name); d.writeByte(gender); d.writeByte(HAIR[gender]);
                    send(-28, bo.toByteArray());
                }
                if (!connected()) break;
                Thread.sleep(20);
            }
            loginFail.incrementAndGet();
            return false;
        }

        void afterEnter() throws IOException {
            send(-28, new byte[]{13});
            send(-39, new byte[0]);
            send(34, shortBytes(SKILL[gender]));
            if (idx >= 90 && idx < 100) {
                send(21, new byte[]{0}); // tập trung toàn bộ 10 bot giao dịch về khu 0
            }
        }

        void runLoop() {
            int attempts = 0;
            while (running) {
                try {
                    if (!connected() || !inGame) {
                        close();
                        if (!running) break;
                        if (attempts++ > 0) Thread.sleep(2500 + rnd.nextInt(2000));
                        if (!connectAndLogin(25_000)) { close(); continue; }
                    }
                    if (!actionsEnabled) { Thread.sleep(200); continue; }
                    if (idx < 60) {
                        actFarm();
                    } else if (idx < 80) {
                        actTaskAndNpc();
                    } else if (idx < 90) {
                        actBossHunt();
                    } else {
                        actTradeAndConsign();
                    }
                    Thread.sleep(220 + rnd.nextInt(380));
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    if (running && !closedByMe) disconnectsUnexpected.incrementAndGet();
                    close();
                }
            }
        }

        // Nhóm 1: Farm quái (map 1, 8, 15) + nhặt Đùi gà nhiệm vụ & vật phẩm rơi
        void actFarm() throws Exception {
            long now = System.currentTimeMillis();
            if (!seenDrops.isEmpty() && now - lastPickSentMs >= 1100) {
                Long myIdObj = playerIds.get(name);
                int myId = myIdObj != null ? myIdObj.intValue() : -999;
                DropInfo target = null;
                for (DropInfo di : seenDrops.values()) {
                    if (di.ownerId == myId || di.ownerId == -1) {
                        target = di;
                        break;
                    }
                }
                if (target == null && rnd.nextInt(10) < 3) {
                    Iterator<DropInfo> it = seenDrops.values().iterator();
                    if (it.hasNext()) target = it.next();
                }
                if (target != null) {
                    seenDrops.remove(target.itemMapId);
                    moveTo(target.x, target.y);
                    lastPickSentMs = now;
                    pickReq.incrementAndGet();
                    send(-20, shortBytes(target.itemMapId));
                    return;
                }
            }
            int r = rnd.nextInt(1000);
            if (r < 720) {
                int m = rnd.nextInt(4);
                moveTo(FARM_MOB_X[gender][m] - 25 + rnd.nextInt(50), FARM_MOB_Y[gender][m]);
                send(54, new byte[]{(byte) m});
            } else if (r < 850) {
                int m = rnd.nextInt(4);
                moveTo(FARM_MOB_X[gender][m] - 40 + rnd.nextInt(80), FARM_MOB_Y[gender][m]);
            } else if (r < 920) {
                send(21, new byte[]{(byte) rnd.nextInt(6)});
            } else if (r < 960) {
                send(-16, new byte[0]);
            } else if (r < 968 && idx % 6 == 0) {
                relogs.incrementAndGet();
                closedByMe = true;
                close();
            }
        }

        // Nhóm 2: Làm nhiệm vụ Mộc Nhân + nói chuyện NPC + nâng tiềm năng + ăn đậu thần
        void actTaskAndNpc() throws Exception {
            int r = rnd.nextInt(1000);
            if (r < 550) {
                int m = rnd.nextInt(4);
                moveTo(VILLAGE_MOB_X[gender][m] - 25 + rnd.nextInt(50), VILLAGE_Y[gender]);
                send(54, new byte[]{(byte) m});
            } else if (r < 750) {
                // Nói chuyện với NPC Ông Gohan/Moori/Paragus (0, 1, 2) hoặc Bunma/Dende/Appule (7, 8, 9)
                int npcId = (r < 680) ? gender : (7 + gender);
                npcTalks.incrementAndGet();
                send(33, shortBytes(npcId));
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                DataOutputStream d = new DataOutputStream(bo);
                d.writeShort(npcId);
                d.writeByte(0);
                send(32, bo.toByteArray());
            } else if (r < 880) {
                // Nâng chỉ số tiềm năng (cmd -30, subCmd 16)
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                DataOutputStream d = new DataOutputStream(bo);
                d.writeByte(16);
                d.writeByte(rnd.nextInt(3)); // HP, MP, hoặc Sức đánh
                d.writeShort(1);
                send(-30, bo.toByteArray());
            } else if (r < 950) {
                // Sử dụng đậu thần ở ô 0 của hành trang (cmd -43)
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                DataOutputStream d = new DataOutputStream(bo);
                d.writeByte(0); // type = 0 (use item)
                d.writeByte(1); // where = 1 (bag)
                d.writeByte(0); // index = 0 (đậu thần)
                d.writeShort(-1);
                send(-43, bo.toByteArray());
            } else {
                send(-16, new byte[0]);
            }
        }

        // Nhóm 3: Săn Boss tập thể
        void actBossHunt() throws Exception {
            long now = System.currentTimeMillis();
            // Nhặt Thỏi vàng Boss rơi nếu có (bỏ qua Ngọc Rồng Namếc 353..359 để không bị khóa dịch chuyển 30s)
            if (!seenDrops.isEmpty() && now - lastPickSentMs >= 1100) {
                DropInfo target = null;
                for (DropInfo di : seenDrops.values()) {
                    if (di.tempId < 353 || di.tempId > 359) {
                        target = di;
                        break;
                    }
                }
                if (target != null) {
                    seenDrops.remove(target.itemMapId);
                    moveTo(target.x, target.y);
                    lastPickSentMs = now;
                    pickReq.incrementAndGet();
                    send(-20, shortBytes(target.itemMapId));
                    return;
                }
            }
            if (!bossesInZone.isEmpty()) {
                send(-16, new byte[0]);
                for (BossPos bp : bossesInZone.values()) {
                    moveTo(bp.x, bp.y);
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    new DataOutputStream(bo).writeInt(bp.id);
                    send(-60, bo.toByteArray());
                    bossAttacks.incrementAndGet();
                }
                if (rnd.nextInt(100) < 15) {
                    bossesInZone.clear();
                }
            } else {
                // Dịch chuyển trực tiếp tới một Boss chiến đấu đang sống trong BossManager
                int bossId = BOSS_IDS_TO_HUNT[rnd.nextInt(BOSS_IDS_TO_HUNT.length)];
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                new DataOutputStream(bo).writeInt(bossId);
                send(-118, bo.toByteArray());
                send(-16, new byte[0]);
            }
        }

        // Nhóm 4: Giao dịch 2 chiều hợp lệ + Test chặn Exploit giao dịch 1 chiều + Mở chợ ký gửi
        void actTradeAndConsign() throws Exception {
            if (idx == 90 || idx == 92 || idx == 94) {
                // Cặp giao dịch 2 chiều hợp lệ: (90,91), (92,93), (94,95)
                Bot partner = botsByIdx.get(idx + 1);
                Long partnerPid = playerIds.get(botName(idx + 1));
                if (partner != null && partnerPid != null && partner.inGame) {
                    if (!tradeOpen) {
                        trade(0, partnerPid.intValue());
                    } else if (!myLocked) {
                        trade(5, -1); // người mời khóa
                        myLocked = true;
                    } else if (partnerLocked) {
                        trade(7, -1); // người mời đồng ý sau khi cả hai bên đã khóa
                        tradeOpen = false;
                        partnerLocked = false;
                        myLocked = false;
                    }
                }
            } else if (idx == 91 || idx == 93 || idx == 95) {
                // Người nhận trong cặp giao dịch 2 chiều hợp lệ
                if (!pendingInvites.isEmpty()) {
                    int from = pendingInvites.iterator().next();
                    pendingInvites.clear();
                    trade(1, from); // mở tab
                    Thread.sleep(80);
                    trade(5, -1);   // người nhận khóa
                    myLocked = true;
                } else if (partnerLocked && myLocked) {
                    trade(7, -1);   // người nhận đồng ý
                    tradeOpen = false;
                    partnerLocked = false;
                    myLocked = false;
                }
            } else if (idx == 96 || idx == 98) {
                // Cặp kiểm thử Exploit 1 chiều: 96 mời 97, 98 mời 99 (người mời KHÔNG BAO GIỜ khóa hay đồng ý)
                Long partnerPid = playerIds.get(botName(idx + 1));
                if (partnerPid != null && !tradeOpen) {
                    trade(0, partnerPid.intValue());
                } else if (tradeOpen && rnd.nextInt(10) < 4) {
                    trade(3, -1); // hủy để thử lại lượt mới
                    tradeOpen = false;
                }
            } else if (idx == 97 || idx == 99) {
                // Người nhận cố tình gửi khóa + đồng ý 2 lần liên tiếp khi người mời chưa khóa
                if (!pendingInvites.isEmpty()) {
                    int from = pendingInvites.iterator().next();
                    pendingInvites.clear();
                    trade(1, from);
                    trade(5, -1);
                    trade(7, -1);
                    trade(7, -1);
                }
            }
            // Ngẫu nhiên xem tab Chợ Ký Gửi (cmd -100 action 4)
            if (rnd.nextInt(100) < 15) {
                send(-100, new byte[]{4, (byte) rnd.nextInt(4), 0});
            }
        }

        void moveTo(int x, int y) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(0);
            d.writeShort(Math.max(24, x));
            d.writeShort(Math.max(24, y));
            send(-7, bo.toByteArray());
        }

        void trade(int action, int pid) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(action);
            if (pid >= 0) d.writeInt(pid);
            send(-86, bo.toByteArray());
        }

        static byte[] shortBytes(int v) { return new byte[]{(byte) (v >> 8), (byte) v}; }

        synchronized void send(int cmd, byte[] data) throws IOException {
            if (out == null) throw new IOException("closed");
            out.writeByte(wk((byte) cmd));
            out.writeByte(wk((byte) (data.length >> 8)));
            out.writeByte(wk((byte) data.length));
            byte[] enc = new byte[data.length];
            for (int i = 0; i < data.length; i++) enc[i] = wk(data[i]);
            out.write(enc);
            out.flush();
            msgOut.incrementAndGet();
        }

        byte wk(byte b) {
            if (!keyReady) return b;
            byte r = (byte) ((key[curW++] & 0xFF) ^ (b & 0xFF));
            if (curW >= key.length) curW %= key.length;
            return r;
        }

        byte rk(byte b) {
            byte r = (byte) ((key[curR++] & 0xFF) ^ (b & 0xFF));
            if (curR >= key.length) curR %= key.length;
            return r;
        }

        void readLoop() {
            try {
                while (!sock.isClosed()) {
                    byte cmd = in.readByte();
                    int size;
                    if (keyReady) {
                        cmd = rk(cmd);
                        if (SPECIAL.contains((int) cmd)) {
                            int b1 = rk((byte) (in.readByte() ^ 0x80)) & 0xFF;
                            int b2 = rk((byte) (in.readByte() ^ 0x80)) & 0xFF;
                            int b3 = rk((byte) (in.readByte() ^ 0x80)) & 0xFF;
                            size = b1 | (b2 << 8) | (b3 << 16);
                        } else {
                            int b1 = rk(in.readByte()) & 0xFF;
                            int b2 = rk(in.readByte()) & 0xFF;
                            size = (b1 << 8) | b2;
                        }
                    } else {
                        size = in.readUnsignedShort();
                    }
                    if (size < 0 || size > 20_000_000) { protoErrors.incrementAndGet(); throw new IOException("kích thước lạ " + size + " cmd " + cmd); }
                    byte[] data = new byte[size];
                    in.readFully(data);
                    if (keyReady) for (int i = 0; i < size; i++) data[i] = rk(data[i]);
                    msgIn.incrementAndGet();
                    bytesIn.addAndGet(size + 3);
                    handle(cmd, data);
                }
            } catch (EOFException | SocketException e) {
                if (running && !closedByMe && inGame) disconnectsUnexpected.incrementAndGet();
            } catch (Exception e) {
                if (running && !closedByMe) { protoErrors.incrementAndGet(); }
            } finally {
                inGame = false;
            }
        }

        void handle(byte cmd, byte[] data) {
            try {
                DataInputStream d = new DataInputStream(new ByteArrayInputStream(data));
                switch (cmd) {
                    case -27 -> {
                        int n = d.readByte();
                        byte[] k = new byte[n];
                        for (int i = 0; i < n; i++) k[i] = d.readByte();
                        for (int j = 0; j < n - 1; j++) k[j + 1] ^= k[j];
                        key = k;
                        keyReady = true;
                    }
                    case 2 -> needCreate = true;
                    case 40 -> {
                        if (inGame) taskUpdates.incrementAndGet();
                        inGame = true;
                    }
                    case 41, 43 -> taskUpdates.incrementAndGet();
                    case -24 -> {
                        // Đổi map mới -> xóa danh sách vật phẩm/boss cũ và gửi finishLoadMap (-39)
                        currentMapId = d.readUnsignedByte();
                        seenDrops.clear();
                        bossesInZone.clear();
                        if (inGame) {
                            try { send(-39, new byte[0]); } catch (Exception ignored) { }
                        }
                    }
                    case -5 -> {
                        // Thông tin nhân vật / Boss trong khu
                        int pid = d.readInt();
                        d.readInt(); // clanId
                        d.readByte(); // level
                        d.readBoolean();
                        byte typePk = d.readByte();
                        d.readByte();
                        d.readByte();
                        d.readShort();
                        String plName = d.readUTF();
                        int hp = d.readInt();
                        d.readInt(); // hpMax
                        d.readShort();
                        d.readShort();
                        d.readByte();
                        d.readByte();
                        int x = d.readShort();
                        int y = d.readShort();
                        if (pid < 0 && pid != -77 && hp > 0 && !plName.startsWith("$") && !plName.startsWith("#")) {
                            bossesInZone.put(pid, new BossPos(pid, x, y));
                        }
                    }
                    case -7 -> {
                        int pid = d.readInt();
                        int x = d.readShort();
                        int y = d.readShort();
                        BossPos bp = bossesInZone.get(pid);
                        if (bp != null) { bp.x = x; bp.y = y; }
                    }
                    case -8 -> {
                        int pid = d.readShort();
                        if (pid < 0 && bossesInZone.remove(pid) != null) {
                            bossKills.incrementAndGet();
                        }
                    }
                    case -6 -> {
                        int pid = d.readInt();
                        bossesInZone.remove(pid);
                    }
                    case -26, -25 -> {
                        String s = d.readUTF();
                        notices.computeIfAbsent(s.length() > 70 ? s.substring(0, 70) : s, x -> new AtomicInteger()).incrementAndGet();
                        if (s.contains("Cả hai bên cần khóa giao dịch")) {
                            tradeExploitBlocked.incrementAndGet();
                        } else if (s.contains("Giao dịch thành công")) {
                            if (idx >= 96 && idx <= 99) {
                                tradeExploitLeaked.incrementAndGet();
                            } else if (idx == 90 || idx == 92 || idx == 94) {
                                tradeLegitOk.incrementAndGet();
                            }
                        }
                    }
                    case 122 -> notices.computeIfAbsent("[Đợi đăng nhập lại]", x -> new AtomicInteger()).incrementAndGet();
                    case -12 -> { // quái chết + đồ rơi
                        int mobId = d.readByte();
                        d.readInt(); d.readBoolean();
                        mobKills.incrementAndGet();
                        int n = d.readByte();
                        for (int i = 0; i < n; i++) {
                            int itemMapId = d.readShort(), temp = d.readShort(), x = d.readShort(), y = d.readShort(), ownerId = d.readInt();
                            seenDrops.put(itemMapId, new DropInfo(itemMapId, temp, x, y, ownerId));
                        }
                    }
                    case 68 -> {
                        int itemMapId = d.readShort(), temp = d.readShort(), x = d.readShort(), y = d.readShort(), ownerId = d.readInt();
                        seenDrops.put(itemMapId, new DropInfo(itemMapId, temp, x, y, ownerId));
                    }
                    case -21 -> {
                        int itemMapId = d.readShort();
                        seenDrops.remove(itemMapId);
                    }
                    case -20 -> { // nhặt thành công (chỉ gửi cho người nhặt)
                        int itemMapId = d.readShort();
                        pickOk.incrementAndGet();
                        DropInfo di = seenDrops.remove(itemMapId);
                        if (di != null) {
                            String keyDrop = currentMapId + ":" + itemMapId + "/" + di.tempId + "@" + di.x + "," + di.y;
                            pickWinners.computeIfAbsent(keyDrop, x -> ConcurrentHashMap.newKeySet()).add(name);
                        }
                    }
                    case -86 -> {
                        int act = d.readByte();
                        if (act == 0) {
                            tradeInvites.incrementAndGet();
                            pendingInvites.add(d.readInt());
                        } else if (act == 1) {
                            tradeOpen = true;
                            partnerLocked = false;
                            myLocked = false;
                        } else if (act == 6) {
                            partnerLocked = true;
                        } else if (act == 3 || act == 7) {
                            tradeOpen = false;
                            partnerLocked = false;
                            myLocked = false;
                        }
                    }
                    default -> { }
                }
                if (seenDrops.size() > 200) seenDrops.clear();
            } catch (Exception e) {
                // bỏ qua
            }
        }

        void close() {
            closedByMe = true;
            inGame = false;
            try { if (sock != null) sock.close(); } catch (Exception ignored) { }
            out = null;
            seenDrops.clear();
            bossesInZone.clear();
            pendingInvites.clear();
        }
    }

    // ===================================================== ADMIN PANEL CLIENT
    static class Panel {
        final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        final JSONParser jp = new JSONParser();
        volatile String cookie = "";

        void login() throws Exception {
            HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(PANEL + "/api/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("password=" + URLEncoder.encode(PANEL_PW, StandardCharsets.UTF_8))).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (r.statusCode() != 200) throw new IllegalStateException("Không đăng nhập được Admin Panel: " + r.body());
            cookie = r.headers().firstValue("Set-Cookie").orElse("").split(";")[0];
        }

        @SuppressWarnings("unchecked")
        synchronized JSONObject get(String path) {
            try {
                HttpResponse<String> r = http.send(HttpRequest.newBuilder(URI.create(PANEL + path)).header("X-NRO-Admin", "1")
                        .header("Cookie", cookie).timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.ofString());
                return (JSONObject) jp.parse(r.body());
            } catch (Exception e) {
                JSONObject o = new JSONObject();
                o.put("error", e.toString());
                o.put("online", -1);
                o.put("players", new JSONArray());
                return o;
            }
        }
    }

    static final StringBuilder LOG = new StringBuilder();

    static synchronized void log(String s) {
        String line = "[" + LocalTime.now().withNano(0) + "] " + s;
        System.out.println(line);
        LOG.append(line).append(System.lineSeparator());
    }
}
