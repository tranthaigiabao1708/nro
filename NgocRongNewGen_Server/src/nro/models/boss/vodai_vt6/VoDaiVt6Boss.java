package nro.models.boss.vodai_vt6;

import java.util.concurrent.atomic.AtomicInteger;
import nro.models.boss.BossData;
import nro.models.boss.Boss_Manager.OtherBossManager;
import nro.models.boss.BossesData;
import nro.models.consts.BossStatus;
import nro.models.consts.BossType;
import nro.models.consts.ConstPlayer;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public class VoDaiVt6Boss extends Universe6Boss {

    private static final AtomicInteger NEXT_ARENA_BOSS_ID = new AtomicInteger(-950_000);

    public static final int TOTAL_ROUNDS = 9;

    private final Player challenger;
    private final int roundIndex;

    // Trạng thái riêng theo từng cao thủ Vũ Trụ 6
    private long lastMagmaEruptTime;
    private volatile long insultedVulnerableUntil;
    private volatile long lastInsultTriggerTime;
    private boolean awakenedSuperSaiyan;
    private long lastShieldBreakTime;
    private long lastCrushCannonTime;
    private long lastTimeSkip;
    private long lastHakaiCycleTime;
    private boolean chargingHakai;
    private long hakaiDetonateAt;

    public VoDaiVt6Boss(Player challenger, int roundIndex) throws Exception {
        super(BossType.PHOBAN, allocateArenaBossId(), resolveBossDataForRound(roundIndex));
        this.challenger = challenger;
        this.roundIndex = Math.max(0, Math.min(TOTAL_ROUNDS - 1, roundIndex));
        this.bossStatus = BossStatus.RESPAWN;
    }

    private static int allocateArenaBossId() {
        int id = NEXT_ARENA_BOSS_ID.decrementAndGet();
        if (id > -950_000 || id < -1_900_000_000) {
            NEXT_ARENA_BOSS_ID.set(-950_000);
            id = NEXT_ARENA_BOSS_ID.decrementAndGet();
        }
        return id;
    }

    public static BossData resolveBossDataForRound(int round) {
        return switch (round) {
            case 0 -> BossesData.FROST_VT6_1;
            case 1 -> BossesData.FROST_VT6_2;
            case 2 -> BossesData.FROST_VT6_3;
            case 3 -> BossesData.AUTA_MAGETTA;
            case 4 -> BossesData.CABBA_VT6;
            case 5 -> BossesData.CAULIFLA_VT6_1;
            case 6 -> BossesData.CAULIFLA_VT6_2;
            case 7 -> BossesData.HIT_VT6;
            default -> BossesData.CHAMPA_VT6;
        };
    }

    public static String getRoundFighterTitle(int round) {
        return switch (round) {
            case 0 -> "Frost Dạng 1";
            case 1 -> "Frost Xung Kích";
            case 2 -> "Frost Hoàn Thiện";
            case 3 -> "Auta Magetta";
            case 4 -> "Saiyan Cabba";
            case 5 -> "Caulifla";
            case 6 -> "SSJ2 Caulifla";
            case 7 -> "Sát Thủ Hit";
            default -> "Thần Hủy Diệt Champa";
        };
    }

    public boolean isMagettaRound() {
        return this.roundIndex == 3;
    }

    public boolean isInsultedVulnerable() {
        return isMagettaRound() && System.currentTimeMillis() < this.insultedVulnerableUntil;
    }

    public void triggerInsultVulnerability() {
        if (!isMagettaRound() || this.isDie()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastInsultTriggerTime >= 14_000L) {
            this.lastInsultTriggerTime = now;
            this.insultedVulnerableUntil = now + 8_000L;
            this.chat("Hức... ngươi dám chê bai ngoại hình tộc Metalman ngay trên võ đài sao?!");
            notifyZonePlayers(this.zone,
                    "Auta Magetta bị tổn thương tâm lý: Giáp hợp kim sụt giảm mạnh trong 8 giây!");
        }
    }

    @Override
    public short getHead() {
        if (this.roundIndex == 4 && this.awakenedSuperSaiyan) {
            return 526; // Đầu Super Saiyan Cabba
        }
        return super.getHead();
    }

    @Override
    public boolean isHighTierBoss() {
        return true;
    }

    @Override
    public int getBossSoulItemId() {
        return ITEM_MA_HON_HUY_DIET;
    }

    @Override
    protected boolean isFinalStage() {
        return true;
    }

    @Override
    protected int getMaHonDropCount() {
        return switch (this.roundIndex) {
            case 6, 7 -> 2;
            case 8 -> 3;
            default -> 1;
        };
    }

    @Override
    protected long getRealHpForCurrentStage() {
        return switch (this.roundIndex) {
            case 0 -> 6_000_000_000L;
            case 1 -> 10_000_000_000L;
            case 2 -> 16_000_000_000L;
            case 3 -> 28_000_000_000L;
            case 4 -> 35_000_000_000L;
            case 5 -> 42_000_000_000L;
            case 6 -> 55_000_000_000L;
            case 7 -> 75_000_000_000L;
            default -> 110_000_000_000L;
        };
    }

    @Override
    protected long getFlatArmorForCurrentStage() {
        return switch (this.roundIndex) {
            case 0 -> 16_000_000L;
            case 1 -> 22_000_000L;
            case 2 -> 28_000_000L;
            case 3 -> isInsultedVulnerable() ? 20_000_000L : 42_000_000L;
            case 4 -> 35_000_000L;
            case 5 -> 38_000_000L;
            case 6 -> 44_000_000L;
            case 7 -> 50_000_000L;
            default -> 65_000_000L;
        };
    }

    @Override
    protected int getArmorPercentForCurrentStage() {
        return switch (this.roundIndex) {
            case 0 -> 70;
            case 1 -> 72;
            case 2 -> 74;
            case 3 -> isInsultedVulnerable() ? 66 : 77;
            case 4 -> 75;
            case 5 -> 75;
            case 6 -> 77;
            case 7 -> 78;
            default -> 80;
        };
    }

    @Override
    protected int getBonusPercentMaxHpPerHit() {
        return switch (this.roundIndex) {
            case 0 -> 5;
            case 1 -> 6;
            case 2 -> 8;
            case 3 -> 8;
            case 4 -> this.awakenedSuperSaiyan ? 11 : 7;
            case 5 -> 8;
            case 6 -> 11;
            case 7 -> 11;
            default -> 13;
        };
    }

    @Override
    protected void configureStageStats() {
        long now = System.currentTimeMillis();
        this.awakenedSuperSaiyan = false;
        this.chargingHakai = false;
        this.lastMagmaEruptTime = now;
        this.lastShieldBreakTime = now;
        this.lastCrushCannonTime = now;
        this.lastTimeSkip = now;
        this.lastHakaiCycleTime = now;

        switch (this.roundIndex) {
            case 0, 1, 2 -> this.nPoint.tlKhangKhongChe = 25;
            case 3 -> {
                this.nPoint.khangTDHS = true;
                this.nPoint.tlKhangKhongChe = 100;
            }
            case 4 -> this.nPoint.tlKhangKhongChe = 35;
            case 5 -> {
                this.nPoint.tlKhangKhongChe = 35;
                this.nPoint.tlNeDon = 120;
            }
            case 6 -> {
                this.nPoint.tlKhangKhongChe = 50;
                this.nPoint.tlNeDon = 300;
            }
            case 7 -> {
                this.nPoint.khangTDHS = true;
                this.nPoint.tlKhangKhongChe = 60;
                this.nPoint.tlNeDon = 200;
            }
            default -> {
                this.nPoint.khangTDHS = true;
                this.nPoint.tlKhangKhongChe = 70;
            }
        }
    }

    @Override
    protected long alterDamageAfterArmor(Player plAtt, long damageAfterArmor) {
        if (this.roundIndex == 3 && !isInsultedVulnerable()) {
            if (plAtt != null && plAtt.nPoint != null && !plAtt.nPoint.isCrit) {
                return Math.max(1L, damageAfterArmor * 65L / 100L);
            }
        }
        return damageAfterArmor;
    }

    @Override
    protected void onBossAttackHitPlayer(Player target) {
        if (target == null || target.isDie() || target.nPoint == null) {
            return;
        }
        if (this.roundIndex <= 2) {
            if (target.itemTime != null && target.itemTime.isUseThuocGiaiVt6) {
                return;
            }
            long now = System.currentTimeMillis();
            boolean newlyPoisoned = now >= target.nPoint.frostPoisonUntil;
            target.nPoint.frostPoisonUntil = now + 10_000L;
            if (newlyPoisoned && target.isPl()) {
                Service.gI().sendThongBao(target, "Trúng Kim Độc của Frost: Giảm 30% Sức đánh trong 10s! (Dùng Thuốc Giải VT6 để kháng)");
            }
            if (this.roundIndex == 2 && Util.isTrue(25, 100) && target.effectSkill != null && !target.effectSkill.isStun) {
                this.chat("Kim độc tê liệt!");
                EffectSkillService.gI().startStun(target, now, 2000);
            }
        } else if (this.roundIndex == 4) {
            if (target.effectSkill != null && target.effectSkill.isShielding && Util.canDoWithTime(this.lastShieldBreakTime, 9_000)) {
                this.lastShieldBreakTime = System.currentTimeMillis();
                this.chat("Galick Pháo Sadala xuyên phá!");
                EffectSkillService.gI().breakShield(target);
            }
        }
    }

    @Override
    protected void onSpecialTick() {
        if (this.zone == null || this.challenger == null || this.challenger.isDie() || !this.zone.equals(this.challenger.zone)) {
            return;
        }
        long now = System.currentTimeMillis();
        switch (this.roundIndex) {
            case 3 -> { // Auta Magetta dung nham
                if (Util.canDoWithTime(this.lastMagmaEruptTime, 10_000)) {
                    this.lastMagmaEruptTime = now;
                    if (Util.getDistance(this, this.challenger) <= 450) {
                        this.chat("Chugga chugga! Phun trào dung nham!");
                        if (this.challenger.itemTime == null || !this.challenger.itemTime.isUseThuocGiaiVt6) {
                            long mpBurn = this.challenger.nPoint.mpMax * 15L / 100L;
                            this.challenger.nPoint.subMP(mpBurn);
                            applyPercentMaxHpStrike(this.challenger, 8, true, false);
                            PlayerService.gI().sendInfoHpMp(this.challenger);
                            Service.gI().sendThongBao(this.challenger, "Hơi nóng Dung Nham của Magetta đốt cháy 15% KI và 8% HP!");
                        }
                    }
                }
            }
            case 4 -> { // Cabba thức tỉnh Super Saiyan
                if (!this.awakenedSuperSaiyan && this.nPoint.getCurrPercentHP() <= 50) {
                    this.awakenedSuperSaiyan = true;
                    this.nPoint.dameg = this.nPoint.dameg / 100L * 135L;
                    this.nPoint.calPoint();
                    this.nPoint.syncDisplayHpFromBossReal();
                    Service.gI().Send_Caitrang(this);
                    this.chat("Vì niềm kiêu hãnh của Hành Tinh Sadala! Hây aaaaa Super Saiyan!");
                    Service.gI().sendThongBao(this.challenger, "Saiyan Cabba bùng nổ trạng thái Super Saiyan!");
                }
            }
            case 6 -> { // SSJ2 Caulifla Crush Cannon
                if (Util.canDoWithTime(this.lastCrushCannonTime, 10_000)) {
                    this.lastCrushCannonTime = now;
                    if (Util.getDistance(this, this.challenger) <= 380) {
                        this.chat("Tuyệt kỹ SSJ2: Crush Cannon liên hoàn pháo!");
                        applyPercentMaxHpStrike(this.challenger, 14, true, false);
                    }
                }
            }
            case 7 -> { // Sát Thủ Hit Time-Skip
                if (Util.canDoWithTime(this.lastTimeSkip, 12_000)) {
                    this.lastTimeSkip = now;
                    int behindX = Math.max(380, Math.min(1160, this.challenger.location.x + (Util.isTrue(50, 100) ? 36 : -36)));
                    PlayerService.gI().playerMove(this, behindX, 504);
                    if (this.challenger.itemTime != null && this.challenger.itemTime.isUseAnPhaThoiGian) {
                        this.chat("Hửm?! Ấn Phá Thời Gian đã hóa giải Bước Nhảy 0.5s của ta!");
                        applyPercentMaxHpStrike(this.challenger, 10, true, false);
                        Service.gI().sendThongBao(this.challenger, "Ấn Phá Thời Gian phát sáng, hóa giải đóng băng thời gian của Hit!");
                    } else {
                        this.chat("Tokitobashi! Bước Nhảy Thời Gian 0.5 giây!");
                        EffectSkillService.gI().startStun(this.challenger, now, 3000, true);
                        applyPercentMaxHpStrike(this.challenger, 35, false, true);
                        Service.gI().sendThongBao(this.challenger,
                                "Trúng Bước Nhảy Thời Gian của Hit (-35% HP & đóng băng 3s)! Hãy dùng Ấn Phá Thời Gian để khắc chế!");
                    }
                }
            }
            case 8 -> { // Thần Hủy Diệt Champa Hakai
                if (!this.chargingHakai && Util.canDoWithTime(this.lastHakaiCycleTime, 18_000)) {
                    this.chargingHakai = true;
                    this.hakaiDetonateAt = now + 3_000L;
                    this.lastHakaiCycleTime = now;
                    this.chat("HAKAI! 3 giây nữa ta sẽ xóa sổ toàn bộ võ đài này!");
                    Service.gI().sendThongBao(this.challenger,
                            "CẢNH BÁO HAKAI: 3 giây nữa Champa phóng Cầu Hủy Diệt! Bật Khiên Năng Lượng hoặc lùi xa 450px ngay!");
                } else if (this.chargingHakai && now >= this.hakaiDetonateAt) {
                    this.chargingHakai = false;
                    this.lastHakaiCycleTime = now;
                    this.chat("BIẾN MẤT ĐI! NĂNG LƯỢNG HỦY DIỆT HAKAI!");
                    if (Util.getDistance(this, this.challenger) <= 450) {
                        if (this.challenger.effectSkill != null && this.challenger.effectSkill.isShielding) {
                            EffectSkillService.gI().breakShield(this.challenger);
                            applyPercentMaxHpStrike(this.challenger, 5, false, false);
                            Service.gI().sendThongBao(this.challenger, "Khiên Năng Lượng đã đỡ đòn Hakai của Champa và vỡ tan!");
                        } else {
                            this.challenger.nPoint.hakaiNoPeaUntil = now + 5_000L;
                            applyPercentMaxHpStrike(this.challenger, 75, false, false);
                            Service.gI().sendThongBao(this.challenger,
                                    "Trúng trực diện HAKAI của Champa (-75% HP & Phong ấn Đậu Thần 5 giây)!");
                        }
                    }
                }
            }
        }
    }

    @Override
    public Player getPlayerAttack() {
        if (this.challenger != null && !this.challenger.isDie() && this.zone != null && this.zone.equals(this.challenger.zone)) {
            return this.challenger;
        }
        return null;
    }

    @Override
    public void joinMap() {
        if (this.challenger != null && this.challenger.zone != null) {
            this.zone = this.challenger.zone;
            ChangeMapService.gI().changeMap(this, this.zone, 870, 504);
            this.lastTimeJoinZone = System.currentTimeMillis();
        }
    }

    @Override
    public void afk() {
        if (this.challenger == null || this.challenger.zone == null || this.zone == null || !this.zone.equals(this.challenger.zone)) {
            this.leaveMap();
        }
    }

    @Override
    public void update() {
        try {
            super.updateInfo();
            if ((this.effectSkill != null && this.effectSkill.isHaveEffectSkill())
                    || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
                return;
            }
            switch (this.bossStatus) {
                case RESPAWN -> {
                    this.respawn();
                    this.changeStatus(BossStatus.JOIN_MAP);
                }
                case JOIN_MAP -> {
                    joinMap();
                    if (this.zone != null) {
                        this.nPoint.mp = this.nPoint.mpg;
                        this.typePk = ConstPlayer.NON_PK;
                        this.changeStatus(BossStatus.AFK);
                    }
                }
                case AFK -> afk();
                case ACTIVE -> {
                    if (this.effectSkill != null && this.effectSkill.isBinh && this.nPoint.tlKhangKhongChe >= 100) {
                        EffectSkillService.gI().BinhDown(this);
                    }
                    this.nPoint.mp = this.nPoint.mpg;
                    onSpecialTick();
                    if (!this.playerSkill.prepareTuSat && !this.playerSkill.prepareLaze && !this.playerSkill.prepareQCKK) {
                        this.attack();
                    }
                }
                default -> {
                }
            }
        } catch (Exception e) {
            Logger.logException(VoDaiVt6Boss.class, e);
        }
    }

    @Override
    public void reward(Player plKill) {
        Player target = (this.challenger != null) ? this.challenger : plKill;
        if (target == null || this.zone == null) {
            return;
        }
        // Dịch tâm rơi đồ lại gần người chơi trên mặt sàn võ đài để tiện nhặt
        if (target.location != null) {
            this.location.x = Math.max(420, Math.min(1120, target.location.x));
            this.location.y = 504;
        }
        switch (this.roundIndex) {
            case 0 -> dropUniverse6Rewards(target, 10, 5, 25, false, false, false);
            case 1 -> dropUniverse6Rewards(target, 12, 8, 30, false, false, false);
            case 2 -> dropUniverse6Rewards(target, 15, 10, 35, false, false, false);
            case 3 -> dropUniverse6Rewards(target, 20, 12, 40, false, false, false);
            case 4 -> dropUniverse6Rewards(target, 25, 15, 45, false, false, false);
            case 5 -> dropUniverse6Rewards(target, 25, 15, 45, true, false, false);
            case 6 -> dropUniverse6Rewards(target, 30, 18, 50, true, false, false);
            case 7 -> {
                dropUniverse6Rewards(target, 40, 20, 50, true, false, false);
                tryDropPermanentVipDisguise(target, 2026);
            }
            default -> {
                dropUniverse6Rewards(target, 50, 30, 60, true, true, true);
                tryDropPermanentVipDisguise(target, 2027);
            }
        }
    }

    @Override
    public void die(Player plKill) {
        Player target = (this.challenger != null) ? this.challenger : plKill;
        if (target != null && !target.isBot && this.zone != null) {
            reward(target);
            int dropX = (target.location != null) ? Math.max(420, Math.min(1120, target.location.x)) : this.location.x;
            dropBossSoul(this.zone, dropX, 504, target);
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        OtherBossManager.gI().removeBoss(this);
        this.dispose();
    }
}

