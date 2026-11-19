package nro.models.boss.vu_tru_13;

import java.time.LocalTime;
import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.boss.BossesData;
import nro.models.boss.vodai_vt6.Universe6Boss;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstTaskBadges;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.services.EffectSkillService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;
import nro.models.task.BadgesTaskService;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public class ThanHuyDietVt13 extends Universe6Boss {

    // 100 Tỷ HP & Giáp cực cao cho Boss Theo Giờ Thần Hủy Diệt Vũ Trụ 13
    public static final long WORLD_BOSS_REAL_HP = 100_000_000_000L; // 100 tỷ HP
    public static final long WORLD_BOSS_FLAT_ARMOR = 16_000_000L;   // Giáp phẳng cao
    public static final int WORLD_BOSS_ARMOR_PCT = 45;              // Giảm 45% sát thương sau giáp phẳng

    private static volatile ThanHuyDietVt13 worldInstance;

    private final boolean trialSummon;
    private Zone pinnedTrialZone;
    private final int spawnX;
    private final int spawnY;

    // Lịch xuất hiện theo khung giờ cố định (mỗi đầu giờ đúng 00h00, 01h00, ..., 23h00)
    private int lastSpawnEpochHour = -1;
    private int lastKilledEpochHour = -1;
    private volatile boolean forceAdminSpawn = false;

    // Kỹ năng 1: Đại Hakai Toàn Bản Đồ (cảnh báo 2.5s -> nổ toàn bộ người chơi trong khu)
    private long lastMapWideHakaiTime;
    private boolean preparingMapWideHakai;
    private long mapWideHakaiStartTime;

    // Kỹ năng 2: Tái Tạo Năng Lượng Hồi HP (bị ngắt ngay lập tức bởi mọi hiệu ứng khống chế)
    private long lastRegenSkillTime;
    private boolean isChannelingRegen;
    private long regenStartTime;
    private long lastRegenTickTime;

    // Đòn đánh kỹ năng kèm theo (Pharaoh-Mummy God of Destruction Kit)
    private long lastHakaiSphereTime;
    private long lastJudgmentSigilTime;
    private long lastMummyBandageTime;
    private long lastScarabCurseTime;

    public ThanHuyDietVt13() throws Exception {
        super(BossID.THAN_HUY_DIET_VT13, BossesData.THAN_HUY_DIET_VT13);
        this.trialSummon = false;
        this.spawnX = 920;
        this.spawnY = 456;
        worldInstance = this;
    }

    public ThanHuyDietVt13(Zone trialZone, int x, int y) throws Exception {
        super(BossID.THAN_HUY_DIET_VT13_TRIAL, BossesData.THAN_HUY_DIET_VT13_TRIAL);
        this.trialSummon = true;
        this.pinnedTrialZone = trialZone;
        this.spawnX = x;
        this.spawnY = y;
    }

    public static ThanHuyDietVt13 getWorldInstance() {
        return worldInstance;
    }

    private static int currentEpochHour() {
        return (int) (System.currentTimeMillis() / 3_600_000L);
    }

    public static String getScheduleStatusText() {
        ThanHuyDietVt13 inst = worldInstance;
        if (inst != null && inst.zone != null && !inst.isDie()
                && (inst.bossStatus == BossStatus.ACTIVE || inst.bossStatus == BossStatus.CHAT_S || inst.bossStatus == BossStatus.AFK)) {
            long hpPct = inst.nPoint != null && inst.nPoint.hpMax > 0
                    ? Math.max(1L, inst.nPoint.hp * 100L / inst.nPoint.hpMax) : 100L;
            return "ĐANG XUẤT HIỆN tại Điện Thờ VT13 - Khu " + inst.zone.zoneId + " (Còn ~" + hpPct + "% HP / 100 Tỷ)";
        }
        LocalTime now = LocalTime.now();
        int nextHour = (now.getHour() + 1) % 24;
        int minsLeft = Math.max(1, 60 - now.getMinute());
        return String.format("Khung giờ kế tiếp: %02d:00 (còn ~%d phút - Xuất hiện mỗi đầu giờ cố định)", nextHour, minsLeft);
    }

    public static boolean summonNowByAdmin(Player admin) {
        ThanHuyDietVt13 inst = worldInstance;
        if (inst == null) {
            return false;
        }
        if (inst.zone != null && !inst.isDie()
                && (inst.bossStatus == BossStatus.ACTIVE || inst.bossStatus == BossStatus.CHAT_S || inst.bossStatus == BossStatus.AFK)) {
            if (admin != null) {
                Service.gI().sendThongBao(admin, "Thần Hủy Diệt Bunny đang có mặt tại Điện Thờ VT13 - Khu " + inst.zone.zoneId + "!");
            }
            return true;
        }
        inst.forceAdminSpawn = true;
        inst.changeStatus(BossStatus.RESPAWN);
        return true;
    }

    @Override
    protected long getRealHpForCurrentStage() {
        return this.trialSummon ? 12_000_000_000L : WORLD_BOSS_REAL_HP;
    }

    @Override
    protected long getFlatArmorForCurrentStage() {
        return this.trialSummon ? 3_500_000L : WORLD_BOSS_FLAT_ARMOR;
    }

    @Override
    protected int getArmorPercentForCurrentStage() {
        return this.trialSummon ? 18 : WORLD_BOSS_ARMOR_PCT;
    }

    @Override
    protected int getMaHonDropCount() {
        return this.trialSummon ? 2 : 8;
    }

    @Override
    protected void configureStageStats() {
        // Bắt buộc không kháng khống chế để người chơi dùng Trói, Socola, Choáng (TDHS/DCTT), Thôi Miên, Hóa Đá, Mafuba ngắt chiêu Tái Tạo Năng Lượng!
        this.nPoint.tlKhangKhongChe = 0;
        this.nPoint.khangTDHS = false;
    }

    private boolean isUnderCrowdControl() {
        if (this.effectSkill == null) {
            return false;
        }
        return this.effectSkill.anTroi
                || this.effectSkill.useTroi
                || this.effectSkill.isSocola
                || this.effectSkill.isStun
                || this.effectSkill.isBlindDCTT
                || this.effectSkill.isThoiMien
                || this.effectSkill.isStone
                || this.effectSkill.isBinh
                || this.effectSkill.isMabuHold;
    }

    private void cancelRegenChanneling(boolean interruptedByCc) {
        if (!this.isChannelingRegen) {
            return;
        }
        this.isChannelingRegen = false;
        if (this.effectSkill != null && this.effectSkill.isCharging) {
            EffectSkillService.gI().stopCharge(this);
        }
        if (interruptedByCc && this.zone != null) {
            this.chat("Hự!! Quá trình Tái Tạo Năng Lượng của ta bị ngắt mất rồi!!");
            notifyZonePlayers(this.zone, "Tuyệt vời! Hiệu ứng khống chế đã NGẮT thành công chiêu Tái Tạo Năng Lượng của Thần Hủy Diệt Bunny!");
        }
    }

    @Override
    public void rest() {
        if (this.trialSummon) {
            return;
        }
        int curHour = currentEpochHour();
        LocalTime now = LocalTime.now();
        // Boss theo giờ: xuất hiện vào mỗi đầu khung giờ (phút 00..49) nếu chưa xuất hiện và chưa bị tiêu diệt trong khung giờ này
        if (this.forceAdminSpawn || (now.getMinute() < 50 && curHour != this.lastSpawnEpochHour && curHour != this.lastKilledEpochHour)) {
            this.forceAdminSpawn = false;
            this.lastSpawnEpochHour = curHour;
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void joinMap() {
        this.preparingMapWideHakai = false;
        this.isChannelingRegen = false;
        long now = System.currentTimeMillis();
        this.lastMapWideHakaiTime = now;
        this.lastRegenSkillTime = now;
        this.lastMummyBandageTime = now;
        this.lastScarabCurseTime = now;

        if (this.trialSummon) {
            if (this.pinnedTrialZone == null) {
                this.changeStatus(BossStatus.LEAVE_MAP);
                return;
            }
            ChangeMapService.gI().changeMap(this, this.pinnedTrialZone, this.spawnX, this.spawnY);
            this.changeStatus(BossStatus.CHAT_S);
            this.lastTimeJoinZone = now;
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                    this.location.x, this.location.y, this.location.x, this.location.y);
            return;
        }

        super.joinMap();
        if (this.zone != null) {
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_EGYPT_SIGIL_PILLAR,
                    this.location.x, this.location.y, this.location.x, this.location.y);
            ServerNotify.gI().notify("BOSS THEO GIỜ [VŨ TRỤ 13]: Thần Hủy Diệt Bunny (100 Tỷ HP) đã xuất hiện tại Điện Thờ Thần Hủy Diệt VT13 (Khu "
                    + this.zone.zoneId + ")! Mau hợp sức tiêu diệt!");
        }
    }

    @Override
    public void update() {
        // Kiểm tra ngắt Tái Tạo Năng Lượng & nhịp hồi HP TRƯỚC super.update() (vì Boss.update() return sớm khi dính CC hoặc đang isCharging!)
        if (!this.isDie() && this.zone != null && this.bossStatus == BossStatus.ACTIVE) {
            long now = System.currentTimeMillis();
            if (isUnderCrowdControl()) {
                if (this.isChannelingRegen) {
                    cancelRegenChanneling(true);
                }
                if (this.preparingMapWideHakai) {
                    this.preparingMapWideHakai = false;
                    this.chat("Gừ... Đòn Hakai Toàn Bản Đồ bị khống chế làm chệch hướng!");
                }
            } else if (this.isChannelingRegen) {
                if (now - this.regenStartTime >= 6000L || this.nPoint.hp >= this.nPoint.hpMax) {
                    cancelRegenChanneling(false);
                    this.chat("Hahaha!! Nguồn năng lượng Hủy Diệt của ta đã hồi phục mạnh mẽ!");
                } else if (now - this.lastRegenTickTime >= 1000L) {
                    this.lastRegenTickTime = now;
                    // Hồi 3% HP tối đa mỗi giây (3 Tỷ HP/s với Boss 100 Tỷ HP)
                    long healAmount = Math.max(1L, this.nPoint.hpMax * 3L / 100L);
                    this.nPoint.addHp(healAmount);
                    if (this.effectSkill != null && !this.effectSkill.isCharging) {
                        EffectSkillService.gI().startCharge(this);
                    }
                    PlayerService.gI().sendInfoHpMp(this);
                    Service.gI().Send_Info_NV(this);
                }
            }
        }
        super.update();
    }

    @Override
    protected void onSpecialTick() {
        if (this.zone == null || this.isDie() || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        long now = System.currentTimeMillis();

        // Tự hủy phân thân thử nghiệm sau 10 phút nếu không có người đánh
        if (this.trialSummon && Util.canDoWithTime(this.lastTimeJoinZone, 600_000)) {
            notifyZonePlayers(this.zone, "Phân thân Thần Hủy Diệt Bunny đã thu hồi năng lượng và biến mất!");
            this.changeStatus(BossStatus.LEAVE_MAP);
            return;
        }

        if (this.isChannelingRegen || isUnderCrowdControl()) {
            return;
        }

        // 1. KỸ NĂNG HỒI HP KIỂU TÁI TẠO NĂNG LƯỢNG (mỗi 24 giây khi HP < 92%)
        if (this.nPoint.hp < (this.nPoint.hpMax * 92L / 100L) && now - this.lastRegenSkillTime >= 24_000L) {
            List<Player> players = this.zone.getNotBosses();
            boolean anyAlivePlayer = false;
            for (Player pl : players) {
                if (pl != null && pl.isPl() && !pl.isDie()) {
                    anyAlivePlayer = true;
                    break;
                }
            }
            if (anyAlivePlayer) {
                this.lastRegenSkillTime = now;
                this.isChannelingRegen = true;
                this.regenStartTime = now;
                this.lastRegenTickTime = now;
                EffectSkillService.gI().startCharge(this);
                this.chat("TÁI TẠO NĂNG LƯỢNG HỦY DIỆT!! Phục hồi sức mạnh ngay lập tức!!");
                notifyZonePlayers(this.zone, "CẢNH BÁO: Thần Hủy Diệt Bunny đang Tái Tạo Năng Lượng hồi 3 Tỷ HP/giây! Mau dùng Trói, Socola, Choáng hoặc Thôi Miên để NGẮT chiêu!");
                return;
            }
        }

        // 2. KỸ NĂNG HAKAI TOÀN BỘ NGƯỜI CHƠI TRONG MAP (chu kỳ 20s: gồng cảnh báo 2.5s -> nổ toàn map!)
        if (this.preparingMapWideHakai) {
            if (now - this.mapWideHakaiStartTime >= 2500L) {
                this.preparingMapWideHakai = false;
                this.lastMapWideHakaiTime = now;
                this.chat("HAKAI TOÀN KHU VỰC — HỦY DIỆT TẤT CẢ!!");
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                        this.location.x, this.location.y, this.location.x, this.location.y);

                List<Player> allPlayersInMap = this.zone.getNotBosses();
                int pctDamage = this.trialSummon ? 22 : 35;
                for (Player pl : allPlayersInMap) {
                    if (pl != null && pl.isPl() && !pl.isDie()) {
                        VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_HAKAI_SPHERE,
                                this.location.x, this.location.y, pl.location.x, pl.location.y);
                        // Phá Khiên Năng Lượng và gây sát thương hủy diệt toàn bản đồ!
                        applyPercentMaxHpStrike(pl, pctDamage, false, true);
                    }
                }
            }
            return;
        }

        if (now - this.lastMapWideHakaiTime >= 20_000L) {
            List<Player> players = this.zone.getNotBosses();
            boolean hasAlivePlayer = false;
            for (Player pl : players) {
                if (pl != null && pl.isPl() && !pl.isDie()) {
                    hasAlivePlayer = true;
                    break;
                }
            }
            if (hasAlivePlayer) {
                this.preparingMapWideHakai = true;
                this.mapWideHakaiStartTime = now;
                this.chat("Tập trung năng lượng Hủy Diệt... Chuẩn bị HAKAI TOÀN BẢN ĐỒ!!");
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_EGYPT_SIGIL_PILLAR,
                        this.location.x, this.location.y, this.location.x, this.location.y);
                notifyZonePlayers(this.zone, "NGUY HIỂM: Thần Hủy Diệt Bunny đang tập trung năng lượng HAKAI TOÀN MAP (nổ sau 2.5 giây)!");
                return;
            }
        }

        // 3. KỸ NĂNG DẢI BĂNG XÁC ƯỞP TRÓI BUỘC (chu kỳ 14s: phóng dải băng xác ướp khóa chặt tối đa 2 mục tiêu trong 2.5s!)
        if (now - this.lastMummyBandageTime >= 14_000L) {
            List<Player> players = this.zone.getNotBosses();
            int boundCount = 0;
            for (Player pl : players) {
                if (pl != null && pl.isPl() && !pl.isDie() && Util.getDistance(this, pl) <= 460) {
                    if (boundCount == 0) {
                        this.lastMummyBandageTime = now;
                        this.chat("DẢI BĂNG XÁC ƯỞP CỔ ĐẠI — TRÓI CHẶT MỤC TIÊU!!");
                    }
                    VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_MUMMY_BANDAGE,
                            this.location.x, this.location.y, pl.location.x, pl.location.y);
                    applyPercentMaxHpStrike(pl, this.trialSummon ? 9 : 12, true, false);
                    if (pl.effectSkill != null && !pl.effectSkill.isStun) {
                        EffectSkillService.gI().startStun(pl, now, 2500);
                        Service.gI().sendThongBao(pl, "Bạn bị Dải Băng Xác Ướp của Thần Hủy Diệt Bunny quấn chặt trong 2.5 giây!");
                    }
                    boundCount++;
                    if (boundCount >= 2) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void attack() {
        if (this.isChannelingRegen || this.preparingMapWideHakai) {
            return;
        }
        super.attack();
    }

    @Override
    protected void onBossAttackHitPlayer(Player target) {
        if (target == null || target.isDie() || target.nPoint == null || this.zone == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastHakaiSphereTime >= 4500 && Util.isTrue(38, 100)) {
            this.lastHakaiSphereTime = now;
            this.chat("Quả Cầu Hakai Tử Cực!! Tan biến đi!");
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_HAKAI_SPHERE,
                    this.location.x, this.location.y, target.location.x, target.location.y);
            applyPercentMaxHpStrike(target, 14, true, true);
            return;
        }
        if (now - this.lastScarabCurseTime >= 8000 && Util.isTrue(38, 100)) {
            this.lastScarabCurseTime = now;
            this.chat("ẤN BỌ HUNG NGUYỀN RỦA PHARAOH — HÚT SINH LỰC!!");
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SCARAB_CURSE,
                    this.location.x, this.location.y, target.location.x, target.location.y);
            applyPercentMaxHpStrike(target, 13, true, false);
            // Hút sinh lực hồi 1% HP tối đa (1 Tỷ HP cho Boss thế giới)
            long drainHeal = Math.max(1L, this.nPoint.hpMax / 100L);
            this.nPoint.addHp(drainHeal);
            PlayerService.gI().sendInfoHpMp(this);
            Service.gI().Send_Info_NV(this);
            return;
        }
        if (now - this.lastJudgmentSigilTime >= 7000 && Util.isTrue(35, 100)) {
            this.lastJudgmentSigilTime = now;
            this.chat("CỘT LỬA HỦY DIỆT KIM TỰ THÁP — NỔ TUNG!!");
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_EGYPT_SIGIL_PILLAR,
                    this.location.x, this.location.y, target.location.x, target.location.y);
            applyPercentMaxHpStrike(target, 11, true, false);
            if (target.effectSkill != null && !target.effectSkill.isStun) {
                EffectSkillService.gI().startStun(target, now, 1500);
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        cancelRegenChanneling(false);
        this.preparingMapWideHakai = false;

        Player owner = resolveRewardOwner(plKill);
        if (this.trialSummon) {
            if (this.zone != null && this.zone.map != null && Util.isTrue(30, 100)) {
                try {
                    long trialOwnerId = owner != null ? owner.id : -1L;
                    short randomRuneId = (short) Util.nextInt(VuTru13Service.ITEM_AN_MO_KHOA_START, VuTru13Service.ITEM_AN_MO_KHOA_END);
                    int rx = clampZoneX(this.zone, this.location.x);
                    int ry = this.zone.map.yPhysicInTop(rx, this.location.y - 24);
                    ItemMap runeDrop = new ItemMap(this.zone, randomRuneId, 1, rx, ry, trialOwnerId);
                    runeDrop.options.add(new Item.ItemOption(73, 0));
                    Service.gI().dropItemMap(this.zone, runeDrop);
                } catch (Exception e) {
                    Logger.logException(ThanHuyDietVt13.class, e);
                }
            }
            if (owner != null) {
                Service.gI().sendThongBao(owner, "Bạn đã đánh bại Phân Thân Bunny! Hãy tới Điện Thờ (Map 180) vào đầu mỗi giờ để săn Thần Hủy Diệt Bunny 100 Tỷ HP!");
            }
            if (this.zone != null) {
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                        this.location.x, this.location.y, this.location.x, this.location.y);
            }
            return;
        }

        this.lastKilledEpochHour = currentEpochHour();
        Player winner = owner != null ? owner : plKill;
        int centerX = this.zone != null ? clampZoneX(this.zone, this.location.x) : this.location.x;
        int floorY = 456;

        // 1. CUTSCENE ("CASSIN") KẾT LIỄU ĐỒNG BỘ CHO TOÀN BỘ NGƯỜI CHƠI TRONG KHU:
        if (this.zone != null) {
            VuTru13Service.gI().broadcastGodBossFinisherCutscene(this.zone, winner, centerX, floorY);
        }

        // 2. RƠI THƯỞNG TRỰC TIẾP RA NGOÀI MẶT ĐẤT ĐỂ MỌI NGƯỜI NHÌN THẤY & NHẶT:
        int droppedSealNumber = 1;
        if (this.zone != null && this.zone.map != null) {
            long ownerId = winner != null ? winner.id : -1L;

            // (a) 100% rơi 100 Thỏi vàng [457] văng thành 10 cọc trên mặt đất (+ kiểm tra nội tại Cánh 20% rơi thêm 100 Thỏi vàng ra mặt đất!)
            Boss.dropGoldBarsStatic(this.zone, centerX, floorY, winner, 100, true);

            // (b) 100% rơi 1 Trứng Đệ Tử Thiên Sứ [2033] ra mặt đất
            try {
                int eggX = clampZoneX(this.zone, centerX);
                int eggY = this.zone.map.yPhysicInTop(eggX, floorY - 24);
                ItemMap angelEgg = new ItemMap(this.zone, VuTru13Service.ITEM_TRUNG_DE_TU_THIEN_SU, 1, eggX, eggY, ownerId);
                angelEgg.options.add(new Item.ItemOption(30, 0)); // Không thể giao dịch
                Service.gI().dropItemMap(this.zone, angelEgg);
            } catch (Exception e) {
                Logger.logException(ThanHuyDietVt13.class, e);
            }

            // (c) 100% rơi Ấn mở khóa ngẫu nhiên từ 1 đến 7 [2050..2056] dùng để Giải Phong Ấn cho Son Goku!
            try {
                droppedSealNumber = Util.nextInt(1, 7);
                short sealItemId = (short) (VuTru13Service.ITEM_AN_MO_KHOA_START + droppedSealNumber - 1);
                int sealX = clampZoneX(this.zone, centerX - 28);
                int sealY = this.zone.map.yPhysicInTop(sealX, floorY - 24);
                ItemMap sealItem = new ItemMap(this.zone, sealItemId, 1, sealX, sealY, ownerId);
                sealItem.options.add(new Item.ItemOption(73, 0)); // Cho phép giao dịch để người chơi trao đổi sưu tầm đủ 7 Ấn!
                Service.gI().dropItemMap(this.zone, sealItem);

                // 40% cơ hội may mắn văng thêm 1 Ấn mở khóa ngẫu nhiên thứ 2!
                if (Util.isTrue(40, 100)) {
                    int extraNum = Util.nextInt(1, 7);
                    short extraSealId = (short) (VuTru13Service.ITEM_AN_MO_KHOA_START + extraNum - 1);
                    int sealX2 = clampZoneX(this.zone, centerX + 28);
                    int sealY2 = this.zone.map.yPhysicInTop(sealX2, floorY - 24);
                    ItemMap sealItem2 = new ItemMap(this.zone, extraSealId, 1, sealX2, sealY2, ownerId);
                    sealItem2.options.add(new Item.ItemOption(73, 0));
                    Service.gI().dropItemMap(this.zone, sealItem2);
                }
            } catch (Exception e) {
                Logger.logException(ThanHuyDietVt13.class, e);
            }

            // (d) 100% rơi 5 - 10 Mảnh Thần Vực Vô Cực [2034] ra mặt đất
            try {
                int shardQty = Util.nextInt(5, 10);
                int shardX = clampZoneX(this.zone, centerX - 52);
                int shardY = this.zone.map.yPhysicInTop(shardX, floorY - 24);
                ItemMap shardMap = new ItemMap(this.zone, VuTru13Service.ITEM_MANH_THAN_VUC_VO_CUC, shardQty, shardX, shardY, ownerId);
                shardMap.options.add(new Item.ItemOption(30, 0));
                Service.gI().dropItemMap(this.zone, shardMap);
            } catch (Exception e) {
                Logger.logException(ThanHuyDietVt13.class, e);
            }

            // (e) 25% cơ hội rơi Cải trang Thần Hủy Diệt Bunny [2057] (ngoại hình Pharaoh-Mummy Bunny)
            if (Util.isTrue(25, 100)) {
                try {
                    int ctX = clampZoneX(this.zone, centerX + 52);
                    int ctY = this.zone.map.yPhysicInTop(ctX, floorY - 24);
                    ItemMap ctBunny = new ItemMap(this.zone, VuTru13Service.ITEM_CAI_TRANG_BUNNY, 1, ctX, ctY, ownerId);
                    int mainStat = Util.nextInt(42, 50);
                    ctBunny.options.add(new Item.ItemOption(50, mainStat)); // Sức đánh +#%
                    ctBunny.options.add(new Item.ItemOption(77, mainStat)); // HP +#%
                    ctBunny.options.add(new Item.ItemOption(103, mainStat)); // KI +#%
                    ctBunny.options.add(new Item.ItemOption(5, 25));        // +#% SĐ Chí mạng
                    if (!Util.isTrue(15, 100)) {
                        ctBunny.options.add(new Item.ItemOption(93, Util.nextInt(3, 7))); // Hạn 3-7 ngày (15% Vĩnh viễn)
                    }
                    Service.gI().dropItemMap(this.zone, ctBunny);
                } catch (Exception e) {
                    Logger.logException(ThanHuyDietVt13.class, e);
                }
            }

            // (f) Hồn Boss Tím & nguyên liệu cao cấp văng ra mặt đất
            dropBossSoul(this.zone, centerX, floorY, winner);
            dropStandardCrystalMaterialsStatic(this.zone, centerX, floorY, winner);
        }

        if (winner != null && winner.isPl()) {
            BadgesTaskService.updateCountBagesTask(winner, ConstTaskBadges.TRUM_SAN_BOSS, 1);
            TaskService.gI().checkDoneTaskKillBoss(winner, this);
            winner.vuTru13Stage |= VuTru13Service.STAGE_FLAG_THAN_HUY_DIET_SLAIN;
            VuTru13Service.gI().sendState(winner);
            PlayerDAO.savePlayerAsync(winner);
            PlayerService.gI().sendInfoHpMpMoney(winner);
            ServerNotify.gI().notify("HUYỀN THOẠI DRAGON BOY Z: Chiến binh " + winner.name
                    + " đã tung cú đấm kết liễu hạ gục THẦN HỦY DIỆT BUNNY (100 Tỷ HP), rơi 100 Thỏi vàng, Trứng Đệ Tử Thiên Sứ & [Ấn mở khóa " + droppedSealNumber + "]!");
        }
    }

    @Override
    public void leaveMap() {
        cancelRegenChanneling(false);
        this.preparingMapWideHakai = false;
        super.leaveMap();
        if (this.trialSummon) {
            BossManager.gI().removeBoss(this);
        }
    }
}
