package nro.models.matches.giai_dau;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.matches.dai_hoi_vo_thuat.DaiHoiVt13Match;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.server.Maintenance;
import nro.models.server.ServerNotify;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.VuTru13Service;
import nro.models.utils.Functions;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 * Đại Hội Võ Thuật Vũ Trụ 13 (Map -1 / ID 186):
 * - Khung giờ cố định mỗi ngày 1 lần: Mở báo danh 20:00 -> 20:15, Khai mạc & ghép cặp 1v1 lúc 20:15.
 * - Quy tắc đặc biệt: Kể cả chỉ có 1 người đăng ký tham gia thì người đó lập tức được xử Thắng & nhận trọn giải Top 1 (KHÔNG HỦY GIẢI).
 * - Khi có >= 2 người: ghép đôi 1v1 từng vòng, người thắng đi tiếp vòng sau đến khi tìm ra Quán Quân Top 1, Á Quân Top 2 & Hạng 3.
 * - Phần thưởng:
 *   + Top 1: 200 Thỏi vàng + 200 Mảnh Thần Vực Vô Cực
 *   + Top 2: 100 Thỏi vàng + 100 Mảnh Thần Vực Vô Cực
 *   + Top 3:  50 Thỏi vàng +  50 Mảnh Thần Vực Vô Cực
 */
public final class DaiHoiVt13Manager implements Runnable {

    public static final int DAILY_REG_HOUR = 20;
    public static final int DAILY_REG_START_MIN = 0;
    public static final int DAILY_FIGHT_START_MIN = 15;

    public static final int STATE_IDLE = 0;
    public static final int STATE_REGISTERING = 1;
    public static final int STATE_FIGHTING = 2;

    public static final class PendingPrize {
        public final int rank;
        public final int goldBars;
        public final int voCucShards;

        public PendingPrize(int rank, int goldBars, int voCucShards) {
            this.rank = rank;
            this.goldBars = goldBars;
            this.voCucShards = voCucShards;
        }
    }

    private static DaiHoiVt13Manager instance;

    public static DaiHoiVt13Manager gI() {
        if (instance == null) {
            instance = new DaiHoiVt13Manager();
        }
        return instance;
    }

    private volatile int state = STATE_IDLE;
    private LocalDate lastDailyRunDate = null;
    private long registrationDeadlineMs = 0L;
    private long lastBroadcastRegMs = 0L;

    private final Set<Long> registeredPlayerIds = ConcurrentHashMap.newKeySet();
    private final Map<Long, String> registeredNames = new ConcurrentHashMap<>();
    private final Map<Long, PendingPrize> pendingPrizes = new ConcurrentHashMap<>();

    // Trạng thái vòng đấu loại trực tiếp 1v1
    private int currentRoundNumber = 0;
    private final List<Player> roundAdvancingPlayers = Collections.synchronizedList(new ArrayList<>());
    private final List<Player> currentRoundLosers = Collections.synchronizedList(new ArrayList<>());
    private final List<DaiHoiVt13Match> activeMatches = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean waitingNextRound = false;
    private volatile long nextRoundStartMs = 0L;

    // Kết quả chung cuộc của giải gần nhất
    private volatile Player semiFinalSingleLoser = null;
    private volatile Player determinedThirdPlace = null;
    private volatile String lastChampionSummary = "Chưa có dữ liệu giải đấu hôm nay.";

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            long st = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                Logger.logException(DaiHoiVt13Manager.class, e);
            }
            long elapsed = System.currentTimeMillis() - st;
            Functions.sleep(Math.max(50L, 1000L - elapsed));
        }
    }

    private synchronized void update() {
        long nowMs = System.currentTimeMillis();
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        // 1. Tự động mở báo danh hằng ngày lúc 20:00 -> 20:15 (mỗi ngày 1 lần)
        if (state == STATE_IDLE) {
            if ((lastDailyRunDate == null || !lastDailyRunDate.equals(today))
                    && nowTime.getHour() == DAILY_REG_HOUR
                    && nowTime.getMinute() >= DAILY_REG_START_MIN
                    && nowTime.getMinute() < DAILY_FIGHT_START_MIN) {
                lastDailyRunDate = today;
                int remainSec = Math.max(60, (DAILY_FIGHT_START_MIN - nowTime.getMinute()) * 60 - nowTime.getSecond());
                openRegistrationWindow(remainSec, false);
            }
            return;
        }

        // 2. Đang mở đăng ký -> Đếm ngược đến giờ khai mạc
        if (state == STATE_REGISTERING) {
            long secLeft = (registrationDeadlineMs - nowMs) / 1000L;
            if (secLeft > 15 && nowMs - lastBroadcastRegMs >= 180_000L) {
                lastBroadcastRegMs = nowMs;
                ServerNotify.gI().notify("ĐẠI HỘI VÕ THUẬT VT13 (Map -1 bên trái Thánh Điện): Đang mở đăng ký ("
                        + registeredPlayerIds.size() + " chiến binh đã đăng ký) — Khai mạc sau " + Math.max(1, secLeft / 60) + " phút!");
            }
            if (nowMs >= registrationDeadlineMs) {
                startTournamentBracket();
            }
            return;
        }

        // 3. Đang thi đấu giải loại trực tiếp 1v1
        if (state == STATE_FIGHTING && waitingNextRound && nowMs >= nextRoundStartMs) {
            waitingNextRound = false;
            launchNextBracketRound();
        }
    }

    public synchronized void openRegistrationWindow(int durationSeconds, boolean triggeredByAdmin) {
        this.state = STATE_REGISTERING;
        this.registeredPlayerIds.clear();
        this.registeredNames.clear();
        this.activeMatches.clear();
        this.roundAdvancingPlayers.clear();
        this.currentRoundLosers.clear();
        this.semiFinalSingleLoser = null;
        this.determinedThirdPlace = null;
        this.currentRoundNumber = 0;
        this.waitingNextRound = false;
        this.registrationDeadlineMs = System.currentTimeMillis() + Math.max(15, durationSeconds) * 1000L;
        this.lastBroadcastRegMs = System.currentTimeMillis();

        String tag = triggeredByAdmin ? "[KHỞI ĐỘNG NHANH] " : "";
        ServerNotify.gI().notify(tag + "ĐẠI HỘI VÕ THUẬT VŨ TRỤ 13 đã mở đăng ký tại Map -1 (bên trái Thánh Điện Toàn Vương)! "
                + "Top 1 nhận 200 Thỏi vàng + 200 Mảnh Đại Thiên Sứ! (Còn " + durationSeconds + " giây đăng ký)");
    }

    public synchronized boolean forceStartBracketByAdmin(Player admin) {
        if (state != STATE_REGISTERING) {
            if (admin != null) {
                Service.gI().sendThongBao(admin, "Giải đấu hiện không trong giai đoạn mở đăng ký! Hãy bấm [Mở Đăng Ký Ngay] trước.");
            }
            return false;
        }
        startTournamentBracket();
        return true;
    }

    public synchronized void registerPlayer(Player player) {
        if (player == null || !player.isPl()) {
            return;
        }
        if (state != STATE_REGISTERING) {
            Service.gI().sendThongBao(player, "Hiện chưa tới giờ mở đăng ký! Giải đấu diễn ra mỗi ngày 1 lần lúc 20:00 - 20:15.");
            return;
        }
        if (registeredPlayerIds.contains(player.id)) {
            Service.gI().sendThongBao(player, "Bạn đã đăng ký tham gia Đại Hội Võ Thuật VT13 hôm nay rồi! Hãy đứng chờ tại Map Đại Hội nhé.");
            return;
        }
        registeredPlayerIds.add(player.id);
        registeredNames.put(player.id, player.name);
        long secLeft = Math.max(1L, (registrationDeadlineMs - System.currentTimeMillis()) / 1000L);
        Service.gI().sendThongBao(player, "Đăng ký thành công! Hiện có " + registeredPlayerIds.size()
                + " chiến binh (Khai mạc sau " + secLeft + " giây — kể cả 1 người vẫn nhận Top 1!).");
    }

    public synchronized void unregisterPlayer(Player player) {
        if (player == null || state != STATE_REGISTERING) {
            return;
        }
        if (registeredPlayerIds.remove(player.id)) {
            registeredNames.remove(player.id);
            Service.gI().sendThongBao(player, "Đã hủy đăng ký Đại Hội Võ Thuật VT13.");
        }
    }

    public boolean isRegistered(Player player) {
        return player != null && registeredPlayerIds.contains(player.id);
    }

    public boolean hasPendingPrize(Player player) {
        return player != null && pendingPrizes.containsKey(player.id);
    }

    public int getState() {
        return state;
    }

    public String getStatusText(Player viewer) {
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        if (state == STATE_REGISTERING) {
            long secLeft = Math.max(1L, (registrationDeadlineMs - now) / 1000L);
            sb.append("|2|★ TRẠNG THÁI: ĐANG MỞ ĐĂNG KÝ (Còn ").append(secLeft).append(" giây)\n");
            sb.append("|8|• Số chiến binh đã đăng ký: ").append(registeredPlayerIds.size()).append(" người");
            if (viewer != null && registeredPlayerIds.contains(viewer.id)) {
                sb.append(" (Bạn ĐÃ đăng ký!)");
            }
            sb.append("\n");
        } else if (state == STATE_FIGHTING) {
            sb.append("|7|★ TRẠNG THÁI: ĐANG THI ĐẤU VÒNG ").append(currentRoundNumber)
                    .append(" (Còn ").append(activeMatches.size()).append(" cặp đang giao chiến)\n");
        } else {
            sb.append("|1|★ LỊCH GIẢI ĐẤU HẰNG NGÀY: Mở đăng ký 20:00 - 20:15 | Khai mạc 20:15 (Mỗi ngày 1 lần)\n");
            sb.append("|8|• Kết quả gần nhất: ").append(lastChampionSummary).append("\n");
        }
        sb.append("|2|• Phần thưởng: Top 1 (200 TV + 200 Mảnh Đại Thiên Sứ) | Top 2 (100 TV + 100 Mảnh) | Top 3 (50 TV + 50 Mảnh)\n");
        sb.append("|1|• Quy tắc vàng: Ghép cặp 1v1 loại trực tiếp; kể cả chỉ có 1 người đăng ký cũng được xử Thắng Top 1 ngay!");
        return sb.toString();
    }

    private synchronized void startTournamentBracket() {
        List<Player> participants = new ArrayList<>();
        nro.models.map.Map tournamentMap = MapService.gI().getMapById(VuTru13Service.MAP_DAI_HOI_VT13);
        Zone lobbyZone = (tournamentMap != null && !tournamentMap.zones.isEmpty()) ? tournamentMap.zones.get(0) : null;

        for (Long pid : registeredPlayerIds) {
            Player pl = Client.gI().getPlayer(pid);
            if (pl != null && pl.isPl() && !pl.isDisposed && pl.zone != null) {
                // Tự động triệu tập người chơi đã đăng ký còn online về Sảnh Khu 0 Map 186 để không lỡ trận!
                if (lobbyZone != null && (pl.zone.map == null || pl.zone.map.mapId != VuTru13Service.MAP_DAI_HOI_VT13)) {
                    ChangeMapService.gI().changeMap(pl, lobbyZone, Util.nextInt(740, 940), 456);
                }
                participants.add(pl);
            }
        }

        if (participants.isEmpty()) {
            this.state = STATE_IDLE;
            this.lastChampionSummary = "Không có người tham dự ở khung giờ vừa qua.";
            ServerNotify.gI().notify("Đại Hội Võ Thuật VT13 kết thúc thời gian đăng ký (0 người tham gia). Hẹn gặp các chiến binh vào 20:00 ngày mai!");
            return;
        }

        // QUY TẮC ĐẶC BIỆT: Chỉ có đúng 1 người đăng ký -> KHÔNG HỦY GIẢI, trao ngay chức Vô Địch Top 1!
        if (participants.size() == 1) {
            Player soleChampion = participants.get(0);
            this.state = STATE_IDLE;
            awardPrize(soleChampion, 1, 200, 200);
            this.lastChampionSummary = "Top 1: " + soleChampion.name + " (thắng đặc cách)";
            ServerNotify.gI().notify("NHÀ VÔ ĐỊCH ĐẠI HỘI VÕ THUẬT VT13: Chỉ có duy nhất [" + soleChampion.name
                    + "] đăng ký tham gia — Đặc cách giành TOP 1 nhận 200 Thỏi vàng & 200 Mảnh Đại Thiên Sứ!");
            return;
        }

        // Có từ 2 người trở lên -> Xáo trộn ngẫu nhiên và chia cặp đấu 1v1 từng vòng!
        Collections.shuffle(participants);
        this.state = STATE_FIGHTING;
        this.currentRoundNumber = 0;
        this.roundAdvancingPlayers.clear();
        this.roundAdvancingPlayers.addAll(participants);
        this.currentRoundLosers.clear();
        this.semiFinalSingleLoser = null;
        this.determinedThirdPlace = null;

        ServerNotify.gI().notify("KHAI MẠC ĐẠI HỘI VÕ THUẬT VŨ TRỤ 13 với " + participants.size()
                + " chiến binh! Các cặp đấu 1v1 bắt đầu lên võ đài!");
        launchNextBracketRound();
    }

    private synchronized void launchNextBracketRound() {
        // Lọc các đấu thủ vẫn đang online
        List<Player> fighters = new ArrayList<>();
        for (Player pl : roundAdvancingPlayers) {
            if (pl != null && !pl.isDisposed && Client.gI().getPlayer(pl.id) != null) {
                fighters.add(pl);
            }
        }
        roundAdvancingPlayers.clear();
        activeMatches.clear();

        if (fighters.isEmpty()) {
            this.state = STATE_IDLE;
            return;
        }
        if (fighters.size() == 1) {
            // Đã tìm ra Nhà Vô Địch khi đối thủ cuối cùng rời mạng
            Player top1 = fighters.get(0);
            Player top2 = !currentRoundLosers.isEmpty() ? currentRoundLosers.get(0) : null;
            Player top3 = determinedThirdPlace != null ? determinedThirdPlace : semiFinalSingleLoser;
            concludeTournament(top1, top2, top3);
            return;
        }

        this.currentRoundNumber++;
        List<Player> prevLosers = new ArrayList<>(this.currentRoundLosers);
        this.currentRoundLosers.clear();

        nro.models.map.Map tournamentMap = MapService.gI().getMapById(VuTru13Service.MAP_DAI_HOI_VT13);
        if (tournamentMap == null || tournamentMap.zones.size() < 2) {
            concludeTournament(fighters.get(0), fighters.size() > 1 ? fighters.get(1) : null, fighters.size() > 2 ? fighters.get(2) : null);
            return;
        }

        boolean isFinalRound = (fighters.size() == 2);
        // Nếu vòng vừa rồi là Bán kết (có 1 hoặc 2 người thua) và bây giờ vào Chung kết (còn 2 người):
        if (isFinalRound) {
            if (prevLosers.size() == 1) {
                this.semiFinalSingleLoser = prevLosers.get(0);
            }
        }

        int nextArenaZoneIdx = 1;
        int i = 0;
        while (i + 1 < fighters.size()) {
            Player p1 = fighters.get(i);
            Player p2 = fighters.get(i + 1);
            Zone arenaZone = tournamentMap.zones.get(Math.min(nextArenaZoneIdx, tournamentMap.zones.size() - 1));
            nextArenaZoneIdx++;
            DaiHoiVt13Match match = new DaiHoiVt13Match(p1, p2, arenaZone, isFinalRound, false);
            activeMatches.add(match);
            match.start();
            i += 2;
        }

        // Nếu lẻ 1 người -> Đặc cách đi thẳng vào vòng tiếp theo!
        if (i < fighters.size()) {
            Player byePlayer = fighters.get(i);
            roundAdvancingPlayers.add(byePlayer);
            Service.gI().sendThongBao(byePlayer, "Vòng " + currentRoundNumber + ": Bạn được bốc thăm ĐẶC CÁCH tiến thẳng vào vòng tiếp theo!");
        }

        // Nếu đây là Vòng Chung Kết và có 2 người thua ở Bán Kết -> Tổ chức đồng thời Trận Tranh Hạng 3!
        if (isFinalRound && prevLosers.size() >= 2) {
            Player thirdP1 = prevLosers.get(0);
            Player thirdP2 = prevLosers.get(1);
            if (thirdP1 != null && !thirdP1.isDisposed && Client.gI().getPlayer(thirdP1.id) != null
                    && thirdP2 != null && !thirdP2.isDisposed && Client.gI().getPlayer(thirdP2.id) != null) {
                Zone thirdZone = tournamentMap.zones.get(Math.min(nextArenaZoneIdx, tournamentMap.zones.size() - 1));
                DaiHoiVt13Match thirdMatch = new DaiHoiVt13Match(thirdP1, thirdP2, thirdZone, false, true);
                activeMatches.add(thirdMatch);
                thirdMatch.start();
            } else if (thirdP1 != null && Client.gI().getPlayer(thirdP1.id) != null) {
                this.determinedThirdPlace = thirdP1;
            } else {
                this.determinedThirdPlace = thirdP2;
            }
        }
    }

    public synchronized void onMatchFinished(DaiHoiVt13Match match, Player winner, Player loser) {
        activeMatches.remove(match);
        if (match.isThirdPlaceMatch) {
            this.determinedThirdPlace = winner;
        } else {
            if (winner != null) {
                roundAdvancingPlayers.add(winner);
            }
            if (loser != null) {
                currentRoundLosers.add(loser);
            }
        }

        // Khi TẤT CẢ các cặp đấu trong vòng hiện tại đã kết thúc:
        if (activeMatches.isEmpty() && state == STATE_FIGHTING) {
            if (roundAdvancingPlayers.size() <= 1) {
                Player top1 = !roundAdvancingPlayers.isEmpty() ? roundAdvancingPlayers.get(0) : winner;
                Player top2 = !currentRoundLosers.isEmpty() ? currentRoundLosers.get(0) : loser;
                Player top3 = this.determinedThirdPlace != null ? this.determinedThirdPlace : this.semiFinalSingleLoser;
                concludeTournament(top1, top2, top3);
            } else {
                // Còn >= 2 người thắng -> Nghỉ 6 giây rồi ghép cặp vòng tiếp theo!
                this.waitingNextRound = true;
                this.nextRoundStartMs = System.currentTimeMillis() + 6000L;
                for (Player adv : roundAdvancingPlayers) {
                    if (adv != null && !adv.isDisposed) {
                        Service.gI().sendThongBao(adv, "Vòng tiếp theo sẽ bắt đầu ghép cặp sau 6 giây! Hãy chuẩn bị!");
                    }
                }
            }
        }
    }

    private void concludeTournament(Player top1, Player top2, Player top3) {
        this.state = STATE_IDLE;
        this.waitingNextRound = false;
        this.activeMatches.clear();

        StringBuilder summary = new StringBuilder();
        if (top1 != null) {
            awardPrize(top1, 1, 200, 200);
            summary.append("Top 1: ").append(top1.name);
        }
        if (top2 != null && (top1 == null || top2.id != top1.id)) {
            awardPrize(top2, 2, 100, 100);
            summary.append(" | Top 2: ").append(top2.name);
        }
        if (top3 != null && (top1 == null || top3.id != top1.id) && (top2 == null || top3.id != top2.id)) {
            awardPrize(top3, 3, 50, 50);
            summary.append(" | Top 3: ").append(top3.name);
        }
        this.lastChampionSummary = summary.length() > 0 ? summary.toString() : "Đã hoàn tất trao giải.";
        ServerNotify.gI().notify("KẾT QUẢ ĐẠI HỘI VÕ THUẬT VŨ TRỤ 13: " + this.lastChampionSummary + "! Chúc mừng các nhà vô địch!");
    }

    private void awardPrize(Player target, int rank, int goldBars, int voCucShards) {
        if (target == null) {
            return;
        }
        Player live = Client.gI().getPlayer(target.id);
        if (live != null && !live.isDisposed && InventoryService.gI().getCountEmptyBag(live) >= 2) {
            deliverPrizeToBag(live, rank, goldBars, voCucShards);
        } else {
            pendingPrizes.put(target.id, new PendingPrize(rank, goldBars, voCucShards));
            if (live != null && !live.isDisposed) {
                Service.gI().sendThongBao(live, "Chúc mừng đạt TOP " + rank + "! Hành trang đang đầy — hãy dọn 2 ô trống rồi gặp Trọng Tài Liên Vũ Trụ để nhận "
                        + goldBars + " Thỏi vàng & " + voCucShards + " Mảnh Đại Thiên Sứ!");
            }
        }
    }

    public synchronized void claimPendingPrize(Player player) {
        if (player == null || !player.isPl()) {
            return;
        }
        PendingPrize prize = pendingPrizes.get(player.id);
        if (prize == null) {
            Service.gI().sendThongBao(player, "Bạn không có phần thưởng giải đấu nào đang chờ nhận!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 2) {
            Service.gI().sendThongBao(player, "Hãy dọn trống ít nhất 2 ô hành trang để nhận thưởng Top " + prize.rank + "!");
            return;
        }
        pendingPrizes.remove(player.id);
        deliverPrizeToBag(player, prize.rank, prize.goldBars, prize.voCucShards);
    }

    private void deliverPrizeToBag(Player player, int rank, int goldBars, int voCucShards) {
        Item tv = ItemService.gI().createNewItem((short) 457, goldBars);
        tv.itemOptions.add(new Item.ItemOption(73, 0));
        Item shards = ItemService.gI().createNewItem(VuTru13Service.ITEM_MANH_THAN_VUC_VO_CUC, voCucShards);
        shards.itemOptions.add(new Item.ItemOption(30, 0));

        InventoryService.gI().addItemBag(player, tv);
        InventoryService.gI().addItemBag(player, shards);
        InventoryService.gI().sendItemBags(player);
        PlayerDAO.savePlayerAsync(player);

        Service.gI().sendThongBao(player, "PHẦN THƯỞNG TOP " + rank + " ĐẠI HỘI VT13: Bạn nhận được +"
                + goldBars + " Thỏi vàng & +" + voCucShards + " Mảnh Đại Thiên Sứ!");
    }
}
