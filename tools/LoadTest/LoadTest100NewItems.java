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
 * KIỂM THỬ TOÀN DIỆN 100 NGƯỜI CHƠI MẶC TRANG BỊ MỚI & SAO PHA LÊ MỚI:
 *  - 100/100 bot đều mặc Cải trang Hành Tinh mới (1781, 1817, 2015) ép 9 sao + Set SKH (Songoku, Picolo, Cađic M)
 *    ép 9 sao gộp dòng (NR 1s +81% SĐ, SPL Khí LL 2014 +99% KI, SPL Huyết LL 2012 +99% HP) + 5 Phụ kiện mở rộng ép SPL Đa dụng.
 *  - Nhóm 1 (0..39 - 40 bot): Farm quái (map 1/8/15), làm nhiệm vụ Đùi gà (TASK_2_0), đo rơi SPL C1 (441..447) ~1%, dùng Bổ Huyết/Bổ Khí/Cuồng Nộ.
 *  - Nhóm 2 (40..54 - 15 bot): Nhiệm vụ Mộc Nhân (TASK_1_0), nói chuyện NPC, nâng tiềm năng, ăn Đậu thần, mở Rương SPL (1440, 1453).
 *  - Nhóm 3 (55..69 - 15 bot): Săn Boss tập thể bằng Combo Nuke 3 hành tinh (DCTT+Kame, Socola+Makankosappo, Biến Khỉ+Huýt Sáo+Tự Phát Nổ+Kiêu Hãnh Xayda) & nhặt nguyên liệu Boss rơi (1423, 1438, 1439, 2011, 2013).
 *  - Nhóm 4 (70..79 - 10 bot): Mua Đá mài (1439) & Cải trang Hành Tinh tại Shop Santa; Chế tạo Hematite, Dùi đục, Đá mài, Nâng cấp SPL Huyết/Khí C2 từ NR 6s/7s, Đánh bóng SPL LL (2012/2014/1433/1434), test Khóa khe ép sao Phụ kiện & Gộp dòng ô 8-9 tại Bà Hạt Mít.
 *  - Nhóm 5 (80..89 - 10 bot): Tương tác người chơi (Chat, Kết bạn, Bật cờ Đen PK hỗn chiến bằng Kame / Makankosappo / Tự Phát Nổ / Hút HP-KI / Phản ST).
 *  - Nhóm 6 (90..99 - 10 bot): Giao dịch 2 chiều vật phẩm mới (Hematite, SPL Huyết/Khí LL) + Chặn Exploit GD 1 chiều + Chợ Ký Gửi.
 */
@SuppressWarnings("unchecked")
public class LoadTest100NewItems {

    static String HOST = "127.0.0.1";
    static int PORT = 14445;
    static String PANEL = "http://127.0.0.1:8686";
    static String PANEL_PW = "NewGen@2026";
    static String DB_URL = "jdbc:mysql://127.0.0.1:3306/nro_newgen?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    static int N = 100;
    static int DURATION = 90;
    static final String PASS = "lt123456";

    static final int[] VILLAGE = {0, 7, 14};
    static final int[] VILLAGE_Y = {432, 432, 408};
    static final int[][] VILLAGE_MOB_X = {{780, 900, 1020, 660}, {708, 804, 900, 996}, {708, 804, 900, 996}};

    static final int[] FARM_MAP = {1, 8, 15};
    static final int[][] FARM_MOB_X = {{348, 804, 972, 540}, {276, 444, 636, 852}, {228, 444, 612, 900}};
    static final int[][] FARM_MOB_Y = {{384, 408, 360, 408}, {384, 336, 288, 360}, {408, 360, 360, 312}};

    static final int[] BASE_MELEE_SKILL = {0, 2, 4};
    static final int[] BASE_BLAST_SKILL = {1, 3, 5};
    static final int[] HAIR = {64, 9, 6};
    static final int[] PLANET_COSTUME = {1781, 1817, 2015};
    static final int[] BOSS_IDS_TO_HUNT = {-20, -21, -22, -23, -28, -29, -30, -31, -32, -33, -34, -35, -36, -37, -100, -101, -203};
    static final Set<Integer> SPECIAL = Set.of(-32, -66, -74, 11, -67, -87, 66);

    // ======= Thống kê toàn cục
    static final AtomicLong msgIn = new AtomicLong(), msgOut = new AtomicLong(), bytesIn = new AtomicLong(), bytesOut = new AtomicLong();
    static final ConcurrentLinkedQueue<Long> combatRttUs = new ConcurrentLinkedQueue<>(),
            npcRttUs = new ConcurrentLinkedQueue<>(),
            mapRttUs = new ConcurrentLinkedQueue<>(),
            panelRttUs = new ConcurrentLinkedQueue<>();
    static final AtomicInteger loginOk = new AtomicInteger(), loginFail = new AtomicInteger(), connFail = new AtomicInteger();
    static final AtomicInteger disconnectsUnexpected = new AtomicInteger(), relogs = new AtomicInteger();
    static final AtomicInteger mobKills = new AtomicInteger(), pickOk = new AtomicInteger(), pickReq = new AtomicInteger();
    static final AtomicInteger splC1DropsSeen = new AtomicInteger(), bossSpecialDropsSeen = new AtomicInteger();
    static final AtomicInteger bossAttacks = new AtomicInteger(), bossKills = new AtomicInteger();
    static final AtomicInteger nukeComboTD = new AtomicInteger(), nukeComboNM = new AtomicInteger(), nukeComboXD = new AtomicInteger();
    static final AtomicInteger kieuHanhTriggered = new AtomicInteger();
    static final AtomicInteger taskUpdates = new AtomicInteger(), npcTalks = new AtomicInteger(), chestOpens = new AtomicInteger();
    static final AtomicInteger pvpAttacks = new AtomicInteger(), chatSent = new AtomicInteger(), friendReqs = new AtomicInteger();
    static final AtomicInteger shopSantaBuys = new AtomicInteger(), combineSuccessCount = new AtomicInteger();
    static final AtomicInteger slotLockBlockedOk = new AtomicInteger(), slotLockLeaked = new AtomicInteger();
    static final AtomicInteger tradeInvites = new AtomicInteger(), tradeLegitOk = new AtomicInteger(), tradeExploitBlocked = new AtomicInteger(), tradeExploitLeaked = new AtomicInteger();
    static final AtomicInteger protoErrors = new AtomicInteger(), statOverflowErrors = new AtomicInteger(), pointPacketsChecked = new AtomicInteger();
    static final AtomicLong maxHpObservedTD = new AtomicLong(), maxMpObservedNM = new AtomicLong(), maxHpObservedXD = new AtomicLong(), maxDameObservedTD = new AtomicLong();

    static final ConcurrentHashMap<String, AtomicInteger> notices = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Set<String>> pickWinners = new ConcurrentHashMap<>();
    static final List<String> report = Collections.synchronizedList(new ArrayList<>());
    static volatile boolean running = true, actionsEnabled = true;
    static final Map<String, Long> playerIds = new ConcurrentHashMap<>();
    static final Map<Integer, Bot> botsByIdx = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length > 0) N = Integer.parseInt(args[0]);
        if (args.length > 1) DURATION = Integer.parseInt(args[1]);
        if (args.length > 2) PANEL_PW = args[2];

        log("===== KIỂM THỬ 100 NGƯỜI CHƠI MẶC TRANG BỊ MỚI & SAO PHA LÊ MỚI (" + DURATION + "s) =====");
        Panel panel = new Panel();
        panel.login();
        JSONObject st0 = panel.get("/api/status");
        log("Trạng thái server ban đầu: " + st0.toJSONString());

        phaseCreate();
        setupAll100PlayersWithNewGearInDb();

        List<Bot> bots = new ArrayList<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        long startLoad = System.currentTimeMillis();
        for (int i = 0; i < N; i++) {
            Bot b = new Bot(i, botName(i), true);
            bots.add(b);
            botsByIdx.put(i, b);
            pool.submit(b::runLoop);
            Thread.sleep(90);
        }
        log("Đã đưa toàn bộ " + N + " người chơi mặc trang bị mới vào game sau " + (System.currentTimeMillis() - startLoad) / 1000 + "s");

        long end = startLoad + DURATION * 1000L;
        int dupTestAt = DURATION / 2;
        boolean dupDone = false;
        while (System.currentTimeMillis() < end) {
            Thread.sleep(10_000);
            long t0 = System.nanoTime();
            JSONObject st = panel.get("/api/status");
            long panelMs = (System.nanoTime() - t0) / 1_000_000;
            panelRttUs.add((System.nanoTime() - t0) / 1_000L);
            String s = String.format(
                    "t=%2ds on=%s ram=%sMB thr=%s api=%dms | kill=%d splDrop=%d bossHit=%d bossKill=%d combo(TD/NM/XD)=%d/%d/%d kieuHanh=%d | combineOK=%d khoaKheOK=%d shopBuy=%d pvp=%d gd=%d overflow=%d",
                    (System.currentTimeMillis() - startLoad) / 1000, st.get("online"), st.get("memUsedMB"), st.get("threads"), panelMs,
                    mobKills.get(), splC1DropsSeen.get(), bossAttacks.get(), bossKills.get(),
                    nukeComboTD.get(), nukeComboNM.get(), nukeComboXD.get(), kieuHanhTriggered.get(),
                    combineSuccessCount.get(), slotLockBlockedOk.get(), shopSantaBuys.get(),
                    pvpAttacks.get(), tradeLegitOk.get(), statOverflowErrors.get());
            log(s);
            if (!dupDone && System.currentTimeMillis() - startLoad > dupTestAt * 1000L) {
                dupDone = true;
                pool.submit(() -> phaseDualLogin(panel));
            }
        }

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
        verifyDbAndNewItems(mem);

        log("");
        log("================= TỔNG KẾT KIỂM THỬ 100 NGƯỜI CHƠI & TRANG BỊ MỚI =================");
        log("Đăng nhập thành công: " + loginOk.get() + " | thất bại: " + loginFail.get() + " | lỗi kết nối: " + connFail.get());
        log("Mất kết nối ngoài ý muốn: " + disconnectsUnexpected.get() + " | thoát-vào lại chủ động: " + relogs.get());
        double inKBpsPerBot = bytesIn.get() / 1024.0 / Math.max(1, DURATION) / Math.max(1, N);
        double outKBpsPerBot = bytesOut.get() / 1024.0 / Math.max(1, DURATION) / Math.max(1, N);
        log("Gói tin nhận: " + msgIn.get() + " (" + String.format("%.2f", bytesIn.get() / 1024.0 / 1024.0) + " MB) | gửi: "
                + msgOut.get() + " (" + String.format("%.2f", bytesOut.get() / 1024.0 / 1024.0) + " MB) | lỗi giải mã: " + protoErrors.get());
        log("Băng thông TB / người chơi: Nhận = " + String.format("%.2f", inKBpsPerBot) + " KB/s | Gửi = " + String.format("%.2f", outKBpsPerBot) + " KB/s");
        log("Độ trễ Combat (hit/kill):      " + fmtRtt(combatRttUs));
        log("Độ trễ Mở NPC (menu/say):      " + fmtRtt(npcRttUs));
        log("Độ trễ Đổi Khu/Dịch chuyển:    " + fmtRtt(mapRttUs));
        log("Độ trễ Admin API (/status):    " + fmtRtt(panelRttUs));
        log("Kiểm tra tràn số chỉ số (cmd -42): Đã kiểm tra " + pointPacketsChecked.get() + " bản tin chỉ số | Lỗi âm/tràn 32-bit = " + statOverflowErrors.get());
        log("Đỉnh chỉ số ghi nhận thực tế (64-bit long >2.147B & <2B): SĐ Trái Đất = " + maxDameObservedTD.get()
                + " | HP Trái Đất = " + maxHpObservedTD.get()
                + " | KI Namếc = " + maxMpObservedNM.get()
                + " | HP Xayda = " + maxHpObservedXD.get());
        log("Farm & Nhặt đồ: Quái bị hạ = " + mobKills.get() + " | SPL Cấp 1 (441..447) rơi = " + splC1DropsSeen.get()
                + " | Nhặt đồ thành công = " + pickOk.get() + "/" + pickReq.get());
        log("Nhiệm vụ, NPC & Mở rương: Tiến độ NV tăng = " + taskUpdates.get() + " | Tương tác NPC = " + npcTalks.get()
                + " | Dùng vật phẩm/Mở Rương SPL (1440/1453) = " + chestOpens.get());
        log("Săn Boss & Nuke 3 Hành Tinh: Đòn đánh Boss = " + bossAttacks.get() + " | Boss bị hạ = " + bossKills.get()
                + " | Nguyên liệu Boss rơi (1423/1438/1439/2011/2013) = " + bossSpecialDropsSeen.get()
                + " | Combo TD(DCTT+Kame)=" + nukeComboTD.get()
                + ", NM(Socola+Laze)=" + nukeComboNM.get()
                + ", XD(Khỉ+HuýtSáo+Nổ)=" + nukeComboXD.get()
                + " | Nội tại Kiêu Hãnh Xayda kích hoạt = " + kieuHanhTriggered.get());
        log("Shop Santa & Bà Hạt Mít: Lượt mua Shop Santa = " + shopSantaBuys.get()
                + " | Chế tạo/Đánh bóng/Ép sao thành công = " + combineSuccessCount.get()
                + " | Chặn đúng ép NR/HP/KI vào Phụ kiện = " + slotLockBlockedOk.get()
                + " | Lọt lỗi khóa khe Phụ kiện = " + slotLockLeaked.get());
        log("Tương tác người chơi (PvP/Chat/Kết bạn/Giao dịch): Đòn PK = " + pvpAttacks.get()
                + " | Chat = " + chatSent.get() + " | Kết bạn = " + friendReqs.get()
                + " | GD 2 chiều thành công = " + tradeLegitOk.get()
                + " | Chặn exploit GD 1 chiều = " + tradeExploitBlocked.get()
                + " | Lọt exploit GD 1 chiều = " + tradeExploitLeaked.get());

        if (tradeExploitLeaked.get() > 0) {
            report.add("LỖI GIAO DỊCH 1 CHIỀU: lọt " + tradeExploitLeaked.get());
        }
        if (slotLockLeaked.get() > 0) {
            report.add("LỖI KHÓA KHE PHỤ KIỆN: lọt " + slotLockLeaked.get() + " lần ép sao SĐ/HP/KI vào Phụ kiện!");
        }
        if (statOverflowErrors.get() > 0) {
            report.add("LỖI TRÀN SỐ 32-BIT INT: phát hiện " + statOverflowErrors.get() + " gói tin chỉ số âm/bằng 0!");
        }
        int dupPick = 0;
        for (Map.Entry<String, Set<String>> e : pickWinners.entrySet()) {
            if (e.getValue().size() > 1) {
                dupPick++;
                if (dupPick <= 10) report.add("X2 NHẶT ĐỒ: vật phẩm " + e.getKey() + " được nhặt bởi " + e.getValue());
            }
        }
        if (dupPick > 10) report.add("... tổng cộng " + dupPick + " vật phẩm bị nhặt nhiều lần");

        log("Thông báo server ghi nhận nhiều nhất:");
        notices.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).limit(18)
                .forEach(e -> log("   " + e.getValue().get() + " x  " + e.getKey()));
        log("");
        log("PHÁT HIỆN LỖI (" + report.size() + "):");
        if (report.isEmpty()) log("   0 LỖI — Toàn bộ 100 người chơi & hệ thống SPL/Trang bị mới vượt qua kiểm thử an toàn tuyệt đối!");
        synchronized (report) { for (String r : report) log("   - " + r); }

        Files.write(Paths.get("ket_qua_100_new_items_" + LocalDateTime.now().toString().replace(':', '-').substring(0, 19) + ".log"),
                LOG.toString().getBytes(StandardCharsets.UTF_8));
        pool.shutdownNow();
        System.exit(report.isEmpty() ? 0 : 2);
    }

    static String botName(int i) { return i < N ? String.format("ltbot%03d", i) : String.format("ltdup%02d", i - N); }

    static String makeItemJson(int tempId, int qty, int[][] opts, long now) {
        JSONArray item = new JSONArray();
        JSONArray options = new JSONArray();
        if (tempId != -1 && opts != null) {
            for (int[] op : opts) {
                JSONArray o = new JSONArray();
                o.add(op[0]);
                o.add(op[1]);
                options.add(o.toJSONString());
            }
        }
        item.add(tempId);
        item.add(tempId == -1 ? 0 : qty);
        item.add(options.toJSONString());
        item.add(now);
        return item.toJSONString();
    }

    static String buildNewEquipmentBody(int gender, long now) {
        JSONArray body = new JSONArray();
        // SKH + 9 sao gộp dòng theo từng hành tinh:
        // TD (0): Set Songoku (129, 141) + NR 1s 9 sao (+81% SĐ) + CT Gohan Beast 1781
        // NM (1): Set Picolo (130, 142) + SPL Khí LL 2014 9 sao (+99% KI) + CT Piccolo Hoàng Kim 1817
        // XD (2): Set Cađic M (241..244) + SPL Huyết LL 2012 9 sao (+99% HP) + CT Hoàng Tử Cađíc Majin 2015
        int skh1 = gender == 0 ? 129 : (gender == 1 ? 130 : 241);
        int skh2 = gender == 0 ? 141 : (gender == 1 ? 142 : 242);
        int skh2Param = gender == 2 ? 0 : 100;
        int starOpt = gender == 0 ? 50 : (gender == 1 ? 103 : 77);
        int starParam = gender == 0 ? 81 : 99;

        int[] coreIds = {
            gender,           // 0: Áo (0, 1, 2)
            6 + gender,       // 1: Quần (6, 7, 8)
            21 + gender,      // 2: Găng (21, 22, 23)
            27 + gender,      // 3: Giày (27, 28, 29)
            12                // 4: Rada
        };
        int[][] coreBaseOpt = {
            {47, 800},
            {6, 50000},
            {0, 5000},
            {7, 50000},
            {14, 15}
        };
        for (int s = 0; s < 5; s++) {
            int[][] opts;
            if (gender == 2) {
                opts = new int[][]{
                    coreBaseOpt[s],
                    {241, 0}, {242, 0}, {243, 0}, {244, 0},
                    {107, 9}, {starOpt, starParam}, {102, 9}, {218, 0}, {228, 9}
                };
            } else {
                opts = new int[][]{
                    coreBaseOpt[s],
                    {skh1, 0}, {skh2, skh2Param},
                    {107, 9}, {starOpt, starParam}, {102, 9}, {218, 0}, {228, 9}
                };
            }
            body.add(makeItemJson(coreIds[s], 1, opts, now));
        }

        // Slot 5: Cải trang Hành Tinh mới (1781 / 1817 / 2015) ép full 9 sao!
        if (gender == 0) {
            body.add(makeItemJson(1781, 1, new int[][]{
                {0, 12000}, {50, 32}, {77, 22}, {103, 22}, {5, 18}, {106, 0},
                {107, 9}, {50, 81}, {102, 9}, {218, 0}, {228, 9}
            }, now));
        } else if (gender == 1) {
            body.add(makeItemJson(1817, 1, new int[][]{
                {23, 100}, {103, 42}, {50, 24}, {77, 25}, {98, 10}, {106, 0},
                {107, 9}, {103, 99}, {102, 9}, {218, 0}, {228, 9}
            }, now));
        } else {
            body.add(makeItemJson(2015, 1, new int[][]{
                {22, 100}, {77, 42}, {50, 26}, {103, 20}, {94, 10}, {106, 0},
                {107, 9}, {77, 99}, {102, 9}, {218, 0}, {228, 9}
            }, now));
        }

        // Slots 6..10: 5 Phụ kiện mở rộng chỉ ép SPL Đa dụng (%TNSM, %Hút HP/KI, %Phản ST, %Xuyên giáp, %Vàng, %Phát nổ, %TNSM Đệ)
        body.add(makeItemJson(529, 1, new int[][]{{9, 64}, {107, 7}, {101, 35}, {100, 35}, {102, 7}}, now));
        body.add(makeItemJson(2003, 1, new int[][]{{50, 15}, {77, 15}, {103, 15}, {107, 7}, {95, 35}, {153, 10}, {102, 7}}, now));
        body.add(makeItemJson(2002, 1, new int[][]{{50, 18}, {77, 18}, {103, 18}, {107, 7}, {96, 35}, {160, 10}, {102, 7}}, now));
        body.add(makeItemJson(2005, 1, new int[][]{{0, 3000}, {107, 7}, {97, 35}, {102, 7}}, now));
        body.add(makeItemJson(2008, 1, new int[][]{{232, 0}, {107, 7}, {98, 28}, {99, 28}, {102, 7}}, now));
        body.add(makeItemJson(1278, 1, new int[][]{{73, 0}}, now));
        return body.toJSONString();
    }

    static String buildFullSkillsJson(int gender) {
        int[] arr = gender == 0 ? new int[]{0, 1, 6, 9, 10, 20, 22, 19}
                : (gender == 1 ? new int[]{2, 3, 7, 11, 12, 17, 18, 19}
                : new int[]{4, 5, 8, 13, 14, 21, 23, 19});
        JSONArray out = new JSONArray();
        for (int skId : arr) {
            JSONArray sk = new JSONArray();
            sk.add(skId);
            sk.add(7); // Max cấp 7 để test chuẩn 95% Makankosappo & 100% Tự Phát Nổ & Khỉ cấp 7!
            sk.add(0);
            out.add(sk.toJSONString());
        }
        return out.toJSONString();
    }

    static String buildBagForBot(int idx, int gender, long now) {
        List<String> items = new ArrayList<>();
        // Ô 0: Đậu thần cấp 8 (ID 65) hồi đầy HP/KI nhanh
        items.add(makeItemJson(65, 500, new int[][]{{2, 200}}, now));
        // Ô 1: Thỏi vàng không khóa (ID 457) để mua Shop Santa & giao dịch
        items.add(makeItemJson(457, 10000, new int[][]{{73, 0}}, now));
        // Ô 2..4: Item Buff Cuồng Nộ (381), Bổ Huyết (382), Bổ Khí (383)
        items.add(makeItemJson(381, 50, new int[][]{{73, 0}}, now));
        items.add(makeItemJson(382, 50, new int[][]{{73, 0}}, now));
        items.add(makeItemJson(383, 50, new int[][]{{73, 0}}, now));
        // Ô 5..6: Rương Sao Pha Lê (1440) & Rương Sao Pha Lê VIP (1453)
        items.add(makeItemJson(1440, 30, new int[][]{{73, 0}}, now));
        items.add(makeItemJson(1453, 30, new int[][]{{73, 0}}, now));

        if (idx >= 70 && idx < 80) {
            // Nhóm 4: Nguyên liệu test toàn diện Bà Hạt Mít & khóa khe ép sao
            items.add(makeItemJson(441, 200, new int[][]{{95, 5}}, now));                      // SPL Đỏ C1 (tạo Hematite)
            items.add(makeItemJson(1423, 100, new int[][]{{167, 0}, {168, 0}}, now));         // Đá Hematite
            items.add(makeItemJson(1438, 100, new int[][]{{73, 0}}, now));                    // Dùi đục
            items.add(makeItemJson(1439, 100, new int[][]{{73, 0}}, now));                    // Đá mài
            items.add(makeItemJson(20, 70, new int[][]{{77, 5}}, now));                       // Ngọc Rồng 7 sao -> 2011
            items.add(makeItemJson(19, 70, new int[][]{{103, 5}}, now));                      // Ngọc Rồng 6 sao -> 2013
            items.add(makeItemJson(2011, 40, new int[][]{{77, 8}}, now));                     // SPL Huyết C2 -> 2012
            items.add(makeItemJson(2013, 40, new int[][]{{103, 8}}, now));                    // SPL Khí C2 -> 2014
            items.add(makeItemJson(964, 20, new int[][]{{153, 5}}, now));                     // SPL Đen C2 -> 1433
            items.add(makeItemJson(965, 20, new int[][]{{160, 5}}, now));                     // SPL Trắng C2 -> 1434
            items.add(makeItemJson(14, 20, new int[][]{{50, 9}}, now));                       // NR 1 sao (+9% SĐ)
            items.add(makeItemJson(2012, 20, new int[][]{{77, 11}}, now));                    // SPL Huyết LL (+11% HP)
            items.add(makeItemJson(2014, 20, new int[][]{{103, 11}}, now));                   // SPL Khí LL (+11% KI)
            items.add(makeItemJson(1426, 20, new int[][]{{95, 5}}, now));                     // SPL Đỏ LL (Đa dụng)
            // Trang bị cốt lõi (Áo) có sẵn 7 sao + đã cường hóa ô 8 & ô 9 để test ép ô 8-9 gộp dòng!
            int coreStatOpt = gender == 0 ? 50 : (gender == 1 ? 103 : 77);
            int coreStatVal = gender == 0 ? 63 : 77;
            items.add(makeItemJson(gender, 1, new int[][]{
                {47, 500}, {107, 9}, {coreStatOpt, coreStatVal}, {102, 7}, {218, 0}, {228, 9}
            }, now));
            // Phụ kiện mở rộng (Thú cưỡi 2003) có 7 ô trống để test CHẶN ép NR/HP/KI và CHO PHÉP ép SPL Đa dụng!
            items.add(makeItemJson(2003, 1, new int[][]{
                {50, 10}, {107, 7}
            }, now));
        } else if (idx >= 90) {
            // Nhóm 6: Giao dịch vật phẩm mới (Hematite, Đá mài, SPL Huyết/Khí LL)
            items.add(makeItemJson(1423, 50, new int[][]{{167, 0}, {168, 0}}, now));
            items.add(makeItemJson(1439, 50, new int[][]{{73, 0}}, now));
            items.add(makeItemJson(2012, 30, new int[][]{{77, 11}}, now));
            items.add(makeItemJson(2014, 30, new int[][]{{103, 11}}, now));
        }

        while (items.size() < 40) {
            items.add(makeItemJson(-1, 0, null, now));
        }
        JSONArray bag = new JSONArray();
        bag.addAll(items);
        return bag.toJSONString();
    }

    static void setupAll100PlayersWithNewGearInDb() throws Exception {
        long now = System.currentTimeMillis();
        try (Connection c = DriverManager.getConnection(DB_URL, "root", "")) {
            for (int i = 0; i < N + 10; i++) {
                String name = botName(i);
                int g = i % 3;
                int mapId, x, y;
                String dataTask, dataPoint;
                boolean isAdmin = false;
                String dataInventory = "[50000000000,500000,50000,0,0]"; // 50 Tỷ vàng, 500k ngọc, 50k hồng ngọc

                if (i < 40) {
                    // Nhóm 1 (0..39): Farm quái & nhặt Đùi gà + đo rơi SPL Cấp 1 (map 1, 8, 15)
                    mapId = FARM_MAP[g];
                    int m = i % 4;
                    x = FARM_MOB_X[g][m];
                    y = FARM_MOB_Y[g][m];
                    dataTask = "[2,0,0]";
                    dataPoint = "[5,25000000000,25000000000,1000,1000,120000,120000,8000,200,10,0,0,120000,120000]";
                } else if (i < 55) {
                    // Nhóm 2 (40..54): Nhiệm vụ Mộc Nhân (TASK_1_0), NPC, nâng tiềm năng, mở Rương SPL (map 0, 7, 14)
                    mapId = VILLAGE[g];
                    int m = i % 4;
                    x = VILLAGE_MOB_X[g][m];
                    y = VILLAGE_Y[g];
                    dataTask = "[1,0,0]";
                    dataPoint = "[5,10000000000,10000000000,1000,1000,80000,80000,2000,150,10,0,0,80000,80000]";
                } else if (i < 70) {
                    // Nhóm 3 (55..69): Săn Boss Max Endgame (kiểm chứng đỉnh chỉ số dưới 2.147B & Combo Nuke 3 hành tinh!)
                    mapId = 68;
                    x = 450 + (i - 55) * 25;
                    y = 312;
                    dataTask = "[20,0,0]";
                    // Chỉ số gốc chuẩn Late Game: TD max SĐ gốc 380k, NM max KI gốc 500k, XD max HP gốc 500k
                    if (g == 0) {
                        dataPoint = "[9,80000000000,80000000000,1000,1000,300000,300000,380000,3000,25,0,0,300000,300000]";
                    } else if (g == 1) {
                        dataPoint = "[9,80000000000,80000000000,1000,1000,300000,500000,220000,3000,20,0,0,300000,500000]";
                    } else {
                        dataPoint = "[9,80000000000,80000000000,1000,1000,500000,300000,250000,3000,20,0,0,500000,300000]";
                    }
                    isAdmin = true;
                } else if (i < 80) {
                    // Nhóm 4 (70..79): Đảo Kamê khu 1 - Shop Santa & Bà Hạt Mít
                    mapId = 5;
                    x = 1080 + (i - 70) * 12;
                    y = 408;
                    dataTask = "[15,0,0]";
                    dataPoint = "[7,30000000000,30000000000,1000,1000,150000,150000,20000,500,15,0,0,150000,150000]";
                } else if (i < 90) {
                    // Nhóm 5 (80..89): Đảo Kamê khu 2 - PvP / PK hỗn chiến, Chat, Kết bạn
                    mapId = 5;
                    x = 650 + (i - 80) * 18;
                    y = 408;
                    dataTask = "[15,0,0]";
                    dataPoint = "[8,50000000000,50000000000,1000,1000,250000,250000,90000,1500,20,0,0,250000,250000]";
                } else {
                    // Nhóm 6 (90..99) & ltdup: Đảo Kamê khu 0 - Giao dịch vật phẩm mới & Ký gửi
                    mapId = 5;
                    x = 400 + (i % 10) * 20;
                    y = 408;
                    dataTask = "[15,0,0]";
                    dataPoint = "[6,20000000000,20000000000,1000,1000,150000,150000,15000,500,15,0,0,150000,150000]";
                }

                if (i < N && i % 2 == 0) {
                    dataPoint = "[9,80000000000,80000000000,1000,1000,15000000000,12000000000,3500000000,3000,25,0,0,15000000000,12000000000]";
                }

                String itemsBody = buildNewEquipmentBody(g, now);
                String itemsBag = buildBagForBot(i, g, now);
                String skills = buildFullSkillsJson(g);

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE player SET data_inventory = ?, data_location = ?, data_task = ?, data_point = ?, "
                        + "items_body = ?, items_bag = ?, skills = ?, baovetaikhoan = '[]' WHERE name = ?")) {
                    ps.setString(1, dataInventory);
                    ps.setString(2, "[" + mapId + "," + x + "," + y + "]");
                    ps.setString(3, dataTask);
                    ps.setString(4, dataPoint);
                    ps.setString(5, itemsBody);
                    ps.setString(6, itemsBag);
                    ps.setString(7, skills);
                    ps.setString(8, name);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE account SET is_admin = ?, active = 1 WHERE username = ?")) {
                    ps.setInt(1, isAdmin ? 1 : 0);
                    ps.setString(2, name);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM player WHERE name = ?")) {
                    ps.setString(1, name);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) playerIds.put(name, rs.getLong(1));
                }
            }
        }
        log("Đã trang bị Cải trang Hành Tinh mới (1781/1817/2015) + SKH 9 sao + Phụ kiện SPL Đa dụng cho toàn bộ " + playerIds.size() + " nhân vật.");
    }

    static void phaseCreate() throws Exception {
        Set<String> existing = new HashSet<>();
        try (Connection c = DriverManager.getConnection(DB_URL, "root", "");
             ResultSet rs = c.createStatement().executeQuery("select name from player where name like 'ltbot%' or name like 'ltdup%'")) {
            while (rs.next()) existing.add(rs.getString(1));
        }
        List<Integer> todo = new ArrayList<>();
        for (int i = 0; i < N + 10; i++) if (!existing.contains(botName(i))) todo.add(i);
        log("A. Tạo nhân vật: đã có " + existing.size() + ", cần tạo thêm " + todo.size());
        if (todo.isEmpty()) return;
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
        log("A. Đã tạo mới " + created.get() + "/" + todo.size() + " nhân vật. Chờ lưu...");
        Thread.sleep(3500);
    }

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
                b.closedByMe = true;
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
            log("C. Kết quả chống đăng nhập kép: " + cnt.size() + " tài khoản online, " + dupOnline + " nhân bản, " + bothIn + " cặp cả 2 phiên còn kết nối");
            if (dupOnline > 0 || bothIn > 0) {
                report.add("ĐĂNG NHẬP 2 PHIÊN: " + dupOnline + "/10 nhân bản, " + bothIn + "/10 cặp lọt cả 2 phiên");
            }
            for (Bot b : pair) b.close();
        } catch (Exception e) {
            report.add("Pha đăng nhập 2 phiên lỗi: " + e);
        }
    }

    static void verifyDbAndNewItems(Map<String, JSONObject> mem) throws Exception {
        JSONParser jp = new JSONParser();
        int checked = 0, mismatch = 0, missing = 0;
        int coreMerged9StarVerified = 0, hematiteOptionVerified = 0;
        try (Connection c = DriverManager.getConnection(DB_URL, "root", "")) {
            try (ResultSet rs = c.createStatement().executeQuery("select name, count(*) n from player group by name having n > 1")) {
                while (rs.next()) report.add("DB: tên nhân vật trùng: " + rs.getString(1) + " x" + rs.getInt(2));
            }
            for (Map.Entry<String, JSONObject> e : mem.entrySet()) {
                String name = e.getKey();
                if (!name.startsWith("ltbot")) continue;
                JSONObject m = e.getValue();
                try (PreparedStatement ps = c.prepareStatement("select data_inventory, data_point, items_bag, items_body from player where name = ?")) {
                    ps.setString(1, name);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) { missing++; continue; }
                    JSONArray inv = (JSONArray) jp.parse(rs.getString(1));
                    JSONArray pt = (JSONArray) jp.parse(rs.getString(2));
                    JSONArray bag = (JSONArray) jp.parse(rs.getString(3));
                    JSONArray body = (JSONArray) jp.parse(rs.getString(4));
                    long gold = ((Number) inv.get(0)).longValue();
                    long gem = ((Number) inv.get(1)).longValue();
                    long ruby = ((Number) inv.get(2)).longValue();
                    long power = ((Number) pt.get(1)).longValue();
                    long tv = 0;
                    for (Object it : bag) {
                        JSONArray item = (JSONArray) jp.parse(it.toString());
                        int tid = ((Number) item.get(0)).intValue();
                        if (tid == 457) tv += ((Number) item.get(1)).longValue();
                        if (tid == 1423) {
                            String optStr = String.valueOf(item.get(2));
                            if (optStr.contains("167") && optStr.contains("168")) {
                                hematiteOptionVerified++;
                            } else {
                                report.add("THIẾU DÒNG CHỮ XANH ĐÁ HEMATITE (167/168) ở " + name + ": " + optStr);
                            }
                        }
                        // Nếu có Thú cưỡi 2003 trong hành trang, tuyệt đối không được lọt option 50>10 hoặc 77 hoặc 103 từ ép sao!
                        if (tid == 2003) {
                            JSONArray opts = (JSONArray) jp.parse(String.valueOf(item.get(2)).replace("\"", ""));
                            for (Object oo : opts) {
                                JSONArray pair = (JSONArray) jp.parse(String.valueOf(oo));
                                int oid = ((Number) pair.get(0)).intValue();
                                int oparam = ((Number) pair.get(1)).intValue();
                                if ((oid == 50 && oparam > 10) || oid == 77 || oid == 103) {
                                    report.add("LỌT ÉP SAO SĐ/HP/KI VÀO PHỤ KIỆN THÚ CƯỠI 2003 ở " + name + ": opt=" + oid + ", param=" + oparam);
                                }
                            }
                        }
                        // Kiểm tra Áo trong hành trang nhóm 4 nếu đã ép lên 8-9 sao thì chỉ có tối đa 1 dòng option 50/77/103 (gộp dòng Điểm 2)!
                        if (tid >= 0 && tid <= 2) {
                            JSONArray opts = (JSONArray) jp.parse(String.valueOf(item.get(2)).replace("\"", ""));
                            int statLines = 0, starCount = 0;
                            for (Object oo : opts) {
                                JSONArray pair = (JSONArray) jp.parse(String.valueOf(oo));
                                int oid = ((Number) pair.get(0)).intValue();
                                int oparam = ((Number) pair.get(1)).intValue();
                                if (oid == 50 || oid == 77 || oid == 103) statLines++;
                                if (oid == 102) starCount = oparam;
                            }
                            if (starCount >= 8) {
                                if (statLines == 1) coreMerged9StarVerified++;
                                else if (statLines > 1) report.add("LỖI GỘP DÒNG Ô 8-9: Áo của " + name + " có " + statLines + " dòng chỉ số %!");
                            }
                        }
                    }
                    // Kiểm tra Cải trang Hành Tinh mới vẫn mặc chuẩn ở slot 5 và không bị mất/lỗi hạn sử dụng
                    JSONArray ctSlot = (JSONArray) jp.parse(body.get(5).toString());
                    int ctId = ((Number) ctSlot.get(0)).intValue();
                    if (ctId != 1781 && ctId != 1817 && ctId != 2015) {
                        report.add("MẤT CẢI TRANG HÀNH TINH MỚI ở slot 5 của " + name + " (ctId=" + ctId + ")");
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
                    if (gold < 0 || gem < 0 || ruby < 0) report.add("DB: tiền tệ âm ở " + name + " vàng=" + gold + " ngọc=" + gem + " ruby=" + ruby);
                }
            }
        }
        log("D. Đối chiếu RAM vs DB: kiểm tra " + checked + " nhân vật, lệch " + mismatch + ", thiếu " + missing
                + " | Xác thực gộp dòng Áo 8-9 sao DB = " + coreMerged9StarVerified
                + " | Xác thực option 167/168 Đá Hematite DB = " + hematiteOptionVerified);
        if (mismatch > 10) report.add("... tổng cộng " + mismatch + " nhân vật bị lệch dữ liệu sau khi lưu");
    }

    static void cmp(List<String> diff, String label, long mem, long db) {
        if (mem != db) diff.add(label + " RAM=" + mem + " DB=" + db);
    }

    static String fmtRtt(Collection<Long> samplesUs) {
        if (samplesUs.isEmpty()) return "n=0";
        List<Long> arr = new ArrayList<>(samplesUs);
        Collections.sort(arr);
        int n = arr.size();
        double p50 = arr.get(Math.min(n - 1, (int) (n * 0.50))) / 1000.0;
        double p95 = arr.get(Math.min(n - 1, (int) (n * 0.95))) / 1000.0;
        double p99 = arr.get(Math.min(n - 1, (int) (n * 0.99))) / 1000.0;
        double max = arr.get(n - 1) / 1000.0;
        return String.format("n=%d | P50=%.2fms | P95=%.2fms | P99=%.2fms | Max=%.2fms", n, p50, p95, p99, max);
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

    static class BagItemSlot {
        final int index, tempId, quantity;
        BagItemSlot(int index, int tempId, int quantity) {
            this.index = index;
            this.tempId = tempId;
            this.quantity = quantity;
        }
    }

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
        volatile boolean tradeOpen, partnerLocked, myLocked, buffsUsed;
        volatile long lastPickSentMs, lastSpecialComboMs;
        volatile long pendingCombatNs, pendingNpcNs, pendingMapNs;
        volatile int currentMapId = -1;
        volatile int combineStep = 0;
        final Map<Integer, DropInfo> seenDrops = new ConcurrentHashMap<>();
        final Map<Integer, BossPos> bossesInZone = new ConcurrentHashMap<>();
        final Map<Integer, BossPos> playersInZone = new ConcurrentHashMap<>();
        final List<BagItemSlot> liveBag = new CopyOnWriteArrayList<>();
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
            tradeOpen = partnerLocked = myLocked = buffsUsed = false;
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
            send(34, shortBytes(BASE_MELEE_SKILL[gender]));
            if (idx >= 70 && idx < 80) {
                send(21, new byte[]{1}); // Nhóm 4: khu 1 Đảo Kamê (Santa & Bà Hạt Mít)
            } else if (idx >= 80 && idx < 90) {
                send(21, new byte[]{2}); // Nhóm 5: khu 2 Đảo Kamê (PvP & Tương tác)
                send(-103, new byte[]{1, 8}); // Bật Cờ Đen để PK tự do
            } else if (idx >= 90 && idx < 100) {
                send(21, new byte[]{0}); // Nhóm 6: khu 0 Đảo Kamê (Giao dịch)
            }
        }

        void useBagItemByTempId(int tempId) throws IOException {
            for (BagItemSlot s : liveBag) {
                if (s.tempId == tempId && s.quantity > 0) {
                    useBagIndex(s.index);
                    return;
                }
            }
        }

        void useBagIndex(int index) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(0);
            d.writeByte(1);
            d.writeShort(index);
            send(-43, bo.toByteArray());
        }

        int findBagIndex(int tempId, int minQty) {
            for (BagItemSlot s : liveBag) {
                if (s.tempId == tempId && s.quantity >= minQty) return s.index;
            }
            return -1;
        }

        void ensureBuffsActive() throws IOException {
            if (!buffsUsed && !liveBag.isEmpty()) {
                buffsUsed = true;
                // Dùng Cuồng Nộ (381), Bổ Huyết (382), Bổ Khí (383) để kiểm chứng Điểm 3 & đỉnh chỉ số dưới 2.147B!
                useBagItemByTempId(381);
                useBagItemByTempId(382);
                useBagItemByTempId(383);
            }
        }

        void runLoop() {
            int attempts = 0;
            while (running) {
                try {
                    if (!connected() || !inGame) {
                        close();
                        if (!running) break;
                        if (attempts++ > 0) Thread.sleep(2500 + rnd.nextInt(1500));
                        if (!connectAndLogin(25_000)) { close(); continue; }
                    }
                    if (!actionsEnabled) { Thread.sleep(200); continue; }
                    ensureBuffsActive();

                    if (idx < 40) {
                        actFarm();
                    } else if (idx < 55) {
                        actTaskNpcAndChest();
                    } else if (idx < 70) {
                        actBossHuntAndPlanetNukes();
                    } else if (idx < 80) {
                        actSantaAndBaHatMit();
                    } else if (idx < 90) {
                        actPvpChatAndInteract();
                    } else {
                        actTradeAndConsign();
                    }
                    Thread.sleep(240 + rnd.nextInt(320));
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    if (running && !closedByMe) disconnectsUnexpected.incrementAndGet();
                    close();
                }
            }
        }

        // Nhóm 1 (40 bot): Farm quái + nhặt Đùi gà & SPL C1 rơi
        void actFarm() throws Exception {
            long now = System.currentTimeMillis();
            if (!seenDrops.isEmpty() && now - lastPickSentMs >= 1000) {
                Long myIdObj = playerIds.get(name);
                int myId = myIdObj != null ? myIdObj.intValue() : -999;
                DropInfo target = null;
                for (DropInfo di : seenDrops.values()) {
                    if (di.ownerId == myId || di.ownerId == -1) {
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
            int r = rnd.nextInt(1000);
            if (r < 760) {
                int m = rnd.nextInt(4);
                send(34, shortBytes(BASE_MELEE_SKILL[gender]));
                moveTo(FARM_MOB_X[gender][m] - 20 + rnd.nextInt(40), FARM_MOB_Y[gender][m]);
                send(54, new byte[]{(byte) m});
            } else if (r < 880) {
                useBagItemByTempId(65); // Ăn đậu thần
            } else if (r < 950) {
                send(21, new byte[]{(byte) rnd.nextInt(6)});
            } else if (r < 992) {
                send(-16, new byte[0]);
            } else if (idx % 8 == 0) {
                relogs.incrementAndGet();
                closedByMe = true;
                close();
            }
        }

        // Nhóm 2 (15 bot): Nhiệm vụ Mộc Nhân + NPC + Nâng tiềm năng + Mở Rương SPL (1440 / 1453)
        void actTaskNpcAndChest() throws Exception {
            int r = rnd.nextInt(1000);
            if (r < 500) {
                int m = rnd.nextInt(4);
                moveTo(VILLAGE_MOB_X[gender][m] - 20 + rnd.nextInt(40), VILLAGE_Y[gender]);
                send(54, new byte[]{(byte) m});
            } else if (r < 680) {
                int npcId = (r < 600) ? gender : (7 + gender);
                npcTalks.incrementAndGet();
                send(33, shortBytes(npcId));
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                DataOutputStream d = new DataOutputStream(bo);
                d.writeShort(npcId);
                d.writeByte(0);
                send(32, bo.toByteArray());
            } else if (r < 800) {
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                DataOutputStream d = new DataOutputStream(bo);
                d.writeByte(16);
                d.writeByte(rnd.nextInt(3));
                d.writeShort(1);
                send(-30, bo.toByteArray());
            } else if (r < 930) {
                // Mở Rương SPL (1440) hoặc Rương SPL VIP (1453)
                int chestId = rnd.nextBoolean() ? 1440 : 1453;
                int cIdx = findBagIndex(chestId, 1);
                if (cIdx >= 0) {
                    chestOpens.incrementAndGet();
                    useBagIndex(cIdx);
                } else {
                    useBagItemByTempId(65);
                }
            } else {
                useBagItemByTempId(65);
                send(-16, new byte[0]);
            }
        }

        // Nhóm 3 (15 bot): Săn Boss bằng Combo Nuke 3 Hành Tinh!
        void actBossHuntAndPlanetNukes() throws Exception {
            long now = System.currentTimeMillis();
            if (!seenDrops.isEmpty() && now - lastPickSentMs >= 1000) {
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
                useBagItemByTempId(65); // Hồi đầy HP/KI trước khi tung combo!
                BossPos bp = bossesInZone.values().iterator().next();
                moveTo(bp.x, bp.y);

                if (now - lastSpecialComboMs >= 4500) {
                    lastSpecialComboMs = now;
                    if (gender == 0) {
                        // Trái Đất: DCTT (20) -> Kamejoko (1)
                        send(34, shortBytes(20));
                        sendAttackPlayer(bp.id);
                        Thread.sleep(120);
                        send(34, shortBytes(1));
                        sendAttackPlayer(bp.id);
                        nukeComboTD.incrementAndGet();
                    } else if (gender == 1) {
                        // Namếc: Biến Sôcôla (18) -> Nạp Makankosappo (11) -> Bắn Makankosappo (11)
                        send(34, shortBytes(18));
                        sendAttackPlayer(bp.id);
                        Thread.sleep(120);
                        send(34, shortBytes(11));
                        sendAttackPlayer(bp.id); // bắt đầu nạp laze
                        Thread.sleep(350);
                        sendAttackPlayer(bp.id); // bắn laze xuyên giáp + x2 Picolo + Socola!
                        nukeComboNM.incrementAndGet();
                    } else {
                        // Xayda: Biến Khỉ (13) + Huýt Sáo (21) + Tự Phát Nổ (14) (Kiêu Hãnh Xayda!)
                        send(34, shortBytes(13));
                        send(-45, new byte[]{0});
                        Thread.sleep(120);
                        send(34, shortBytes(21));
                        send(-45, new byte[]{0});
                        Thread.sleep(120);
                        useBagItemByTempId(65); // Hồi đầy cây máu sau khi biến Khỉ & Huýt Sáo
                        Thread.sleep(80);
                        send(34, shortBytes(14));
                        send(-45, new byte[]{0}); // gồng tự sát
                        Thread.sleep(1650);       // chờ đủ 1.5s chuẩn của SkillService!
                        send(-45, new byte[]{0}); // PHÁT NỔ!
                        nukeComboXD.incrementAndGet();
                        Thread.sleep(100);
                        useBagItemByTempId(65); // Hồi lại từ 1 HP / 1 KI của Kiêu Hãnh Xayda!
                    }
                    bossAttacks.incrementAndGet();
                } else {
                    send(34, shortBytes(BASE_BLAST_SKILL[gender]));
                    sendAttackPlayer(bp.id);
                    bossAttacks.incrementAndGet();
                }
                if (rnd.nextInt(100) < 12) bossesInZone.clear();
            } else {
                int bossId = BOSS_IDS_TO_HUNT[rnd.nextInt(BOSS_IDS_TO_HUNT.length)];
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                new DataOutputStream(bo).writeInt(bossId);
                send(-118, bo.toByteArray());
                send(-16, new byte[0]);
            }
        }

        // Nhóm 4 (10 bot): Mua Shop Santa & Chế tạo / Ép sao tại Bà Hạt Mít (map 5 khu 1)
        void actSantaAndBaHatMit() throws Exception {
            if (currentMapId != 5) return;
            int step = combineStep++ % 8;
            switch (step) {
                case 0 -> {
                    // Mua Đá mài (1439 - giá 100 TV) tại Cửa hàng Santa (npc 39, select 0)
                    moveTo(990, 408);
                    send(33, shortBytes(39));
                    sendConfirmMenu(39, 0);
                    Thread.sleep(80);
                    sendBuyShopItem(1439);
                    shopSantaBuys.incrementAndGet();
                }
                case 1 -> {
                    // Mua Cải trang Hành Tinh (1781/1817/2015 - giá 1500 TV) tại Shop Thỏi Vàng (npc 39, select 3)
                    int tvIdx = findBagIndex(457, 1600);
                    if (tvIdx >= 0 && liveBag.size() < 36) {
                        moveTo(990, 408);
                        send(33, shortBytes(39));
                        sendConfirmMenu(39, 3);
                        Thread.sleep(80);
                        sendBuyShopItem(PLANET_COSTUME[gender]);
                        shopSantaBuys.incrementAndGet();
                    }
                }
                case 2 -> {
                    // Tạo đá Hematite (x20 SPL Đỏ 441 -> 1 Hematite 1423): Bà Hạt Mít (npc 21) -> 0 -> 5
                    int splIdx = findBagIndex(441, 20);
                    if (splIdx >= 0) {
                        doBaHatMitCombine(5, new int[]{splIdx});
                    }
                }
                case 3 -> {
                    // Chế tạo Dùi đục (x5 Hematite 1423 -> 1 Dùi đục 1438): Bà Hạt Mít -> 0 -> 6
                    int hemaIdx = findBagIndex(1423, 5);
                    if (hemaIdx >= 0) {
                        doBaHatMitCombine(6, new int[]{hemaIdx});
                    }
                }
                case 4 -> {
                    // Chế tạo Đá mài (x5 Dùi đục 1438 -> 1 Đá mài 1439): Bà Hạt Mít -> 0 -> 7
                    int duiIdx = findBagIndex(1438, 5);
                    if (duiIdx >= 0) {
                        doBaHatMitCombine(7, new int[]{duiIdx});
                    }
                }
                case 5 -> {
                    // Nâng cấp Ngọc Rồng 7s (20) / 6s (19) + Hematite (1423) -> SPL Huyết/Khí C2 (2011/2013): -> 0 -> 2
                    int nrId = (idx % 2 == 0) ? 20 : 19;
                    int nrIdx = findBagIndex(nrId, 7);
                    int hemaIdx = findBagIndex(1423, 1);
                    if (nrIdx >= 0 && hemaIdx >= 0) {
                        doBaHatMitCombine(2, new int[]{nrIdx, hemaIdx});
                    }
                }
                case 6 -> {
                    // Đánh bóng SPL C2 (2011, 2013, 964, 965) + Đá mài (1439) -> SPL Lấp Lánh: -> 0 -> 3
                    int[] c2Pool = {2011, 2013, 964, 965};
                    int c2Id = c2Pool[combineStep % c2Pool.length];
                    int c2Idx = findBagIndex(c2Id, 2);
                    int maiIdx = findBagIndex(1439, 1);
                    if (c2Idx >= 0 && maiIdx >= 0) {
                        doBaHatMitCombine(3, new int[]{c2Idx, maiIdx});
                    }
                }
                case 7 -> {
                    // Test Khóa khe ép sao Phụ kiện (2003) & Ép gộp dòng ô 8-9 Áo cốt lõi (0/1/2): -> 0 -> 0
                    int mountIdx = findBagIndex(2003, 1);
                    int forbiddenGemId = gender == 0 ? 14 : (gender == 1 ? 2014 : 2012);
                    int forbiddenIdx = findBagIndex(forbiddenGemId, 1);
                    if (mountIdx >= 0 && forbiddenIdx >= 0) {
                        // Cố tình đặt Thú cưỡi 2003 + NR 1s / SPL Huyết LL / SPL Khí LL -> Server PHẢI chặn!
                        moveTo(1195, 408);
                        send(33, shortBytes(21));
                        sendConfirmMenu(21, 0);
                        Thread.sleep(60);
                        sendConfirmMenu(21, 0);
                        Thread.sleep(60);
                        sendCombineItems(new int[]{mountIdx, forbiddenIdx});
                        Thread.sleep(80);
                        // Thử bấm xác nhận (nếu là IGNORE_MENU thì confirmMenu sẽ bỏ qua an toàn)
                        sendConfirmMenu(21, 0);
                        Thread.sleep(80);
                    }
                    // Tiếp theo ép SPL Đa dụng (1426) vào Thú cưỡi 2003 & ép gem hành tinh vào ô 8-9 của Áo cốt lõi!
                    int utilIdx = findBagIndex(1426, 1);
                    if (mountIdx >= 0 && utilIdx >= 0) {
                        doBaHatMitCombine(0, new int[]{mountIdx, utilIdx});
                    }
                    int aoIdx = findBagIndex(gender, 1);
                    forbiddenIdx = findBagIndex(forbiddenGemId, 1);
                    if (aoIdx >= 0 && forbiddenIdx >= 0) {
                        doBaHatMitCombine(0, new int[]{aoIdx, forbiddenIdx});
                    }
                }
            }
        }

        void doBaHatMitCombine(int subSelect, int[] bagIndices) throws Exception {
            moveTo(1195, 408);
            npcTalks.incrementAndGet();
            send(33, shortBytes(21));
            sendConfirmMenu(21, 0); // "Chức năng pha lê"
            Thread.sleep(60);
            sendConfirmMenu(21, subSelect);
            Thread.sleep(60);
            sendCombineItems(bagIndices);
            Thread.sleep(80);
            sendConfirmMenu(21, 0); // "Nâng cấp" / "Tạo đá" / "Đánh bóng"
        }

        // Nhóm 5 (10 bot): Tương tác người chơi - PvP hỗn chiến, Chat, Kết bạn tại Đảo Kamê khu 2
        void actPvpChatAndInteract() throws Exception {
            send(-16, new byte[0]);
            int r = rnd.nextInt(1000);
            if (r < 120) {
                sendChat("Test trang bi moi & SPL 9 sao #" + idx);
                chatSent.incrementAndGet();
                return;
            }
            if (r < 200) {
                int targetBot = 80 + rnd.nextInt(10);
                Long pid = playerIds.get(botName(targetBot));
                if (pid != null && targetBot != idx) {
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    DataOutputStream d = new DataOutputStream(bo);
                    d.writeByte(1); // add friend
                    d.writeInt(pid.intValue());
                    send(-80, bo.toByteArray());
                    friendReqs.incrementAndGet();
                }
                return;
            }
            useBagItemByTempId(65);
            // Chọn ngẫu nhiên 1 đối thủ trong nhóm 80..89 đang ở cùng khu 2
            int victimIdx = 80 + rnd.nextInt(10);
            if (victimIdx == idx) victimIdx = 80 + ((idx + 1) % 10);
            Long victimPid = playerIds.get(botName(victimIdx));
            if (victimPid == null) return;
            BossPos vp = playersInZone.get(victimPid.intValue());
            moveTo(vp != null ? vp.x : (650 + (victimIdx - 80) * 18), 408);

            if (r < 680) {
                send(34, shortBytes(BASE_BLAST_SKILL[gender]));
                sendAttackPlayer(victimPid.intValue());
                pvpAttacks.incrementAndGet();
            } else {
                // Tung kỹ năng đặc trưng trong PvP!
                if (gender == 0) {
                    send(34, shortBytes(20));
                    sendAttackPlayer(victimPid.intValue());
                    Thread.sleep(80);
                    send(34, shortBytes(1));
                    sendAttackPlayer(victimPid.intValue());
                } else if (gender == 1) {
                    send(34, shortBytes(18));
                    sendAttackPlayer(victimPid.intValue());
                    Thread.sleep(80);
                    send(34, shortBytes(11));
                    sendAttackPlayer(victimPid.intValue());
                    Thread.sleep(300);
                    sendAttackPlayer(victimPid.intValue());
                } else {
                    send(34, shortBytes(14));
                    send(-45, new byte[]{0});
                    Thread.sleep(1600);
                    send(-45, new byte[]{0});
                    Thread.sleep(80);
                    useBagItemByTempId(65);
                }
                pvpAttacks.incrementAndGet();
            }
        }

        // Nhóm 6 (10 bot): Giao dịch 2 chiều vật phẩm mới + Chặn Exploit GD 1 chiều + Chợ Ký Gửi
        void actTradeAndConsign() throws Exception {
            if (idx == 90 || idx == 92 || idx == 94) {
                Bot partner = botsByIdx.get(idx + 1);
                Long partnerPid = playerIds.get(botName(idx + 1));
                if (partner != null && partnerPid != null && partner.inGame) {
                    if (!tradeOpen) {
                        trade(0, partnerPid.intValue());
                    } else if (!myLocked) {
                        // Đặt thử 1 viên Đá Hematite (1423) hoặc SPL Huyết LL (2012) vào ô giao dịch!
                        int hemaIdx = findBagIndex(1423, 2);
                        if (hemaIdx >= 0) sendTradeAddItem(hemaIdx, 1);
                        Thread.sleep(50);
                        trade(5, -1);
                        myLocked = true;
                    } else if (partnerLocked) {
                        trade(7, -1);
                        tradeOpen = false;
                        partnerLocked = false;
                        myLocked = false;
                    }
                }
            } else if (idx == 91 || idx == 93 || idx == 95) {
                if (!pendingInvites.isEmpty()) {
                    int from = pendingInvites.iterator().next();
                    pendingInvites.clear();
                    trade(1, from);
                    Thread.sleep(80);
                    int splIdx = findBagIndex(2012, 2);
                    if (splIdx >= 0) sendTradeAddItem(splIdx, 1);
                    Thread.sleep(50);
                    trade(5, -1);
                    myLocked = true;
                } else if (partnerLocked && myLocked) {
                    trade(7, -1);
                    tradeOpen = false;
                    partnerLocked = false;
                    myLocked = false;
                }
            } else if (idx == 96 || idx == 98) {
                Long partnerPid = playerIds.get(botName(idx + 1));
                if (partnerPid != null && !tradeOpen) {
                    trade(0, partnerPid.intValue());
                } else if (tradeOpen && rnd.nextInt(10) < 4) {
                    trade(3, -1);
                    tradeOpen = false;
                }
            } else if (idx == 97 || idx == 99) {
                if (!pendingInvites.isEmpty()) {
                    int from = pendingInvites.iterator().next();
                    pendingInvites.clear();
                    trade(1, from);
                    trade(5, -1);
                    trade(7, -1);
                    trade(7, -1);
                }
            }
            if (rnd.nextInt(100) < 15) {
                send(-100, new byte[]{4, (byte) rnd.nextInt(4), 0});
            }
        }

        void sendAttackPlayer(int targetPid) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            new DataOutputStream(bo).writeInt(targetPid);
            send(-60, bo.toByteArray());
        }

        void sendConfirmMenu(int npcId, int select) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeShort(npcId);
            d.writeByte(select);
            send(32, bo.toByteArray());
        }

        void sendBuyShopItem(int tempId) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(0);
            d.writeShort(tempId);
            send(6, bo.toByteArray());
        }

        void sendCombineItems(int[] indices) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(1);
            d.writeShort(indices.length);
            for (int ix : indices) d.writeShort(ix);
            send(-81, bo.toByteArray());
        }

        void sendChat(String text) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            new DataOutputStream(bo).writeUTF(text);
            send(44, bo.toByteArray());
        }

        void sendTradeAddItem(int bagIdx, int qty) throws IOException {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            DataOutputStream d = new DataOutputStream(bo);
            d.writeByte(2);
            d.writeShort(bagIdx);
            d.writeInt(qty);
            send(-86, bo.toByteArray());
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
            if (inGame) {
                if (cmd == 54 || cmd == -60) pendingCombatNs = System.nanoTime();
                else if (cmd == 33) pendingNpcNs = System.nanoTime();
                else if (cmd == 21 || cmd == -118) pendingMapNs = System.nanoTime();
            }
            out.writeByte(wk((byte) cmd));
            out.writeByte(wk((byte) (data.length >> 8)));
            out.writeByte(wk((byte) data.length));
            byte[] enc = new byte[data.length];
            for (int i = 0; i < data.length; i++) enc[i] = wk(data[i]);
            out.write(enc);
            out.flush();
            msgOut.incrementAndGet();
            bytesOut.addAndGet(data.length + 3L);
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
                if (running && !closedByMe) protoErrors.incrementAndGet();
            } finally {
                inGame = false;
            }
        }

        void recordRtt(int type) {
            long nowNs = System.nanoTime();
            long t0 = 0;
            ConcurrentLinkedQueue<Long> q = null;
            if (type == 0 && pendingCombatNs != 0) { t0 = pendingCombatNs; pendingCombatNs = 0; q = combatRttUs; }
            else if (type == 1 && pendingNpcNs != 0) { t0 = pendingNpcNs; pendingNpcNs = 0; q = npcRttUs; }
            else if (type == 2 && pendingMapNs != 0) { t0 = pendingMapNs; pendingMapNs = 0; q = mapRttUs; }
            if (t0 > 0 && q != null) {
                long us = (nowNs - t0) / 1000L;
                if (us >= 0 && us <= 5_000_000L) q.add(us);
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
                    case -42 -> {
                        // Kiểm tra trực tiếp từng bản tin chỉ số (Point packet) 64-bit long!
                        long hpg = d.readLong();
                        long mpg = d.readLong();
                        long dameg = d.readLong();
                        long hpMax = d.readLong();
                        long mpMax = d.readLong();
                        long hp = d.readLong();
                        long mp = d.readLong();
                        d.readByte(); d.readByte(); d.readByte(); d.readByte();
                        long dame = d.readLong();
                        pointPacketsChecked.incrementAndGet();
                        if (hpg <= 0 || mpg <= 0 || dameg <= 0 || hpMax <= 0 || mpMax <= 0 || dame <= 0 || hp < 0 || mp < 0) {
                            statOverflowErrors.incrementAndGet();
                            if (statOverflowErrors.get() <= 10) {
                                report.add("TRÀN SỐ CHỈ SỐ ở " + name + ": hpMax=" + hpMax + ", mpMax=" + mpMax + ", dame=" + dame + ", hp=" + hp + ", mp=" + mp);
                            }
                        }
                        if (gender == 0) {
                            maxDameObservedTD.accumulateAndGet(dame, Math::max);
                            maxHpObservedTD.accumulateAndGet(hpMax, Math::max);
                        } else if (gender == 1) {
                            maxMpObservedNM.accumulateAndGet(mpMax, Math::max);
                        } else {
                            maxHpObservedXD.accumulateAndGet(hpMax, Math::max);
                        }
                    }
                    case -36 -> {
                        int act = d.readByte();
                        if (act == 0) {
                            int cap = d.readUnsignedShort();
                            List<BagItemSlot> parsed = new ArrayList<>();
                            for (int slotIdx = 0; slotIdx < cap && d.available() > 0; slotIdx++) {
                                int tempId = d.readShort();
                                if (tempId == -1) continue;
                                int qty = d.readInt();
                                d.readUTF();
                                d.readUTF();
                                int optCount = d.readUnsignedByte();
                                for (int o = 0; o < optCount; o++) {
                                    d.readUnsignedByte();
                                    d.readUnsignedShort();
                                }
                                parsed.add(new BagItemSlot(slotIdx, tempId, qty));
                            }
                            liveBag.clear();
                            liveBag.addAll(parsed);
                        }
                    }
                    case -81 -> {
                        int type = d.readByte();
                        if (type == 2) { // COMBINE_SUCCESS
                            combineSuccessCount.incrementAndGet();
                        }
                    }
                    case 32, 38 -> {
                        recordRtt(1);
                        // createOtherMenu từ NPC (kiểm tra thông báo khóa khe ép sao Phụ kiện!)
                        d.readShort(); // npcId
                        String npcSay = d.readUTF();
                        if (npcSay.contains("Phụ kiện") || npcSay.contains("SPL Đa dụng") || npcSay.contains("Chỉ cho phép ép")) {
                            slotLockBlockedOk.incrementAndGet();
                        }
                    }
                    case 44 -> {
                        int senderId = d.readInt();
                        String chat = d.readUTF();
                        if (chat.contains("Kiêu hãnh Xayda")) {
                            kieuHanhTriggered.incrementAndGet();
                        }
                    }
                    case -24 -> {
                        recordRtt(2);
                        currentMapId = d.readUnsignedByte();
                        seenDrops.clear();
                        bossesInZone.clear();
                        playersInZone.clear();
                        if (inGame) {
                            try { send(-39, new byte[0]); } catch (Exception ignored) { }
                        }
                    }
                    case -5 -> {
                        int pid = d.readInt();
                        d.readInt();
                        d.readByte();
                        d.readBoolean();
                        d.readByte();
                        d.readByte();
                        d.readByte();
                        d.readShort();
                        String plName = d.readUTF();
                        long hp = d.readLong();
                        long hpMax = d.readLong();
                        d.readShort();
                        d.readShort();
                        d.readByte();
                        d.readByte();
                        int x = d.readShort();
                        int y = d.readShort();
                        if (hpMax < 0 || hp < 0) {
                            statOverflowErrors.incrementAndGet();
                            report.add("TRÀN MÁU NHÂN VẬT/BOSS TRÊN KHU (-5) ở " + plName + ": hp=" + hp + ", hpMax=" + hpMax);
                        }
                        if (pid < 0 && pid != -77 && hp > 0 && !plName.startsWith("$") && !plName.startsWith("#")) {
                            bossesInZone.put(pid, new BossPos(pid, x, y));
                        } else if (pid > 0 && hp > 0) {
                            playersInZone.put(pid, new BossPos(pid, x, y));
                        }
                    }
                    case -7 -> {
                        int pid = d.readInt();
                        int x = d.readShort();
                        int y = d.available() >= 2 ? d.readShort() : 0;
                        BossPos bp = bossesInZone.get(pid);
                        if (bp != null) { bp.x = x; bp.y = y; }
                        BossPos pp = playersInZone.get(pid);
                        if (pp != null) { pp.x = x; pp.y = y; }
                    }
                    case -8 -> {
                        int pid = d.readInt();
                        if (pid < 0 && bossesInZone.remove(pid) != null) {
                            bossKills.incrementAndGet();
                        }
                    }
                    case -6 -> {
                        int pid = d.readInt();
                        bossesInZone.remove(pid);
                        playersInZone.remove(pid);
                    }
                    case -26, -25 -> {
                        String s = d.readUTF();
                        notices.computeIfAbsent(s.length() > 75 ? s.substring(0, 75) : s, x -> new AtomicInteger()).incrementAndGet();
                        if (s.contains("Cả hai bên cần khóa giao dịch")) {
                            tradeExploitBlocked.incrementAndGet();
                        } else if (s.contains("Giao dịch thành công")) {
                            if (idx >= 96 && idx <= 99) {
                                tradeExploitLeaked.incrementAndGet();
                            } else if (idx == 90 || idx == 92 || idx == 94) {
                                tradeLegitOk.incrementAndGet();
                            }
                        } else if (s.contains("Phụ kiện mở rộng") && s.contains("SPL Đa dụng")) {
                            slotLockBlockedOk.incrementAndGet();
                        }
                    }
                    case 122 -> notices.computeIfAbsent("[Đợi đăng nhập lại]", x -> new AtomicInteger()).incrementAndGet();
                    case -9, -60 -> recordRtt(0);
                    case -12 -> {
                        recordRtt(0);
                        d.readByte();
                        d.readLong();
                        d.readBoolean();
                        mobKills.incrementAndGet();
                        int n = d.readByte();
                        for (int i = 0; i < n; i++) {
                            int itemMapId = d.readShort(), temp = d.readShort(), x = d.readShort(), y = d.readShort(), ownerId = d.readInt();
                            if (temp >= 441 && temp <= 447) splC1DropsSeen.incrementAndGet();
                            if (temp == 1423 || temp == 1438 || temp == 1439 || temp == 2011 || temp == 2013) bossSpecialDropsSeen.incrementAndGet();
                            seenDrops.put(itemMapId, new DropInfo(itemMapId, temp, x, y, ownerId));
                        }
                    }
                    case 68 -> {
                        int itemMapId = d.readShort(), temp = d.readShort(), x = d.readShort(), y = d.readShort(), ownerId = d.readInt();
                        if (temp >= 441 && temp <= 447) splC1DropsSeen.incrementAndGet();
                        if (temp == 1423 || temp == 1438 || temp == 1439 || temp == 2011 || temp == 2013) bossSpecialDropsSeen.incrementAndGet();
                        seenDrops.put(itemMapId, new DropInfo(itemMapId, temp, x, y, ownerId));
                    }
                    case -21 -> seenDrops.remove((int) d.readShort());
                    case -20 -> {
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
            } catch (Exception ignored) {
            }
        }

        void close() {
            closedByMe = true;
            inGame = false;
            try { if (sock != null) sock.close(); } catch (Exception ignored) { }
            out = null;
            seenDrops.clear();
            bossesInZone.clear();
            playersInZone.clear();
            pendingInvites.clear();
        }
    }

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
