package nro.models.boss.vu_tru_13;

import java.util.List;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.vodai_vt6.Universe6Boss;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.services.EffectSkillService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;
import nro.models.utils.Util;

public class ThanHuyDietVt13 extends Universe6Boss {

    private static final long[] STAGE_REAL_HP = {
        25_000_000_000L, // Phase 1: Thần Hủy Diệt Vũ Trụ 13 (25 tỷ HP)
        45_000_000_000L  // Phase 2: Cổ Thần Hủy Diệt Tối Thượng (45 tỷ HP)
    };
    private static final long[] STAGE_FLAT_ARMOR = {
        6_000_000L,
        10_000_000L
    };
    private static final int[] STAGE_ARMOR_PCT = {22, 30};

    private final boolean trialSummon;
    private Zone pinnedTrialZone;
    private final int spawnX;
    private final int spawnY;
    private long lastSoulStormTime;
    private long lastHakaiSphereTime;
    private long lastJudgmentSigilTime;
    private boolean announcedPhase2;

    public ThanHuyDietVt13() throws Exception {
        super(BossID.THAN_HUY_DIET_VT13, BossesData.THAN_HUY_DIET_VT13, BossesData.CO_THAN_HUY_DIET_VT13);
        this.trialSummon = false;
        this.spawnX = 920;
        this.spawnY = 456;
    }

    public ThanHuyDietVt13(Zone trialZone, int x, int y) throws Exception {
        super(BossID.THAN_HUY_DIET_VT13_TRIAL, BossesData.THAN_HUY_DIET_VT13_TRIAL);
        this.trialSummon = true;
        this.pinnedTrialZone = trialZone;
        this.spawnX = x;
        this.spawnY = y;
    }

    @Override
    protected long getRealHpForCurrentStage() {
        if (this.trialSummon) {
            return 12_000_000_000L;
        }
        int idx = Math.max(0, Math.min(STAGE_REAL_HP.length - 1, this.currentLevel));
        return STAGE_REAL_HP[idx];
    }

    @Override
    protected long getFlatArmorForCurrentStage() {
        if (this.trialSummon) {
            return 3_500_000L;
        }
        int idx = Math.max(0, Math.min(STAGE_FLAT_ARMOR.length - 1, this.currentLevel));
        return STAGE_FLAT_ARMOR[idx];
    }

    @Override
    protected int getArmorPercentForCurrentStage() {
        if (this.trialSummon) {
            return 15;
        }
        int idx = Math.max(0, Math.min(STAGE_ARMOR_PCT.length - 1, this.currentLevel));
        return STAGE_ARMOR_PCT[idx];
    }

    @Override
    protected int getMaHonDropCount() {
        return this.trialSummon ? 2 : 8;
    }

    @Override
    protected void configureStageStats() {
        this.nPoint.tlKhangKhongChe = this.currentLevel >= 1 ? 65 : 40;
    }

    @Override
    public void joinMap() {
        if (this.trialSummon) {
            if (this.pinnedTrialZone == null) {
                this.changeStatus(BossStatus.LEAVE_MAP);
                return;
            }
            ChangeMapService.gI().changeMap(this, this.pinnedTrialZone, this.spawnX, this.spawnY);
            this.changeStatus(BossStatus.CHAT_S);
            this.lastTimeJoinZone = System.currentTimeMillis();
            this.lastSoulStormTime = System.currentTimeMillis();
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                    this.location.x, this.location.y, this.location.x, this.location.y);
            return;
        }
        super.joinMap();
        this.lastSoulStormTime = System.currentTimeMillis();
        if (this.zone != null) {
            if (this.currentLevel >= 1 && !this.announcedPhase2) {
                this.announcedPhase2 = true;
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                        this.location.x, this.location.y, this.location.x, this.location.y);
            } else {
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_EGYPT_SIGIL_PILLAR,
                        this.location.x, this.location.y, this.location.x, this.location.y);
            }
        }
    }

    @Override
    protected void onSpecialTick() {
        if (this.zone == null || this.isDie() || this.typePk != ConstPlayer.PK_ALL) {
            return;
        }
        // Tự hủy phân thân thử nghiệm sau 10 phút nếu không có người đánh
        if (this.trialSummon && Util.canDoWithTime(this.lastTimeJoinZone, 600_000)) {
            notifyZonePlayers(this.zone, "Phân thân Thần Hủy Diệt Vũ Trụ 13 đã thu hồi thần lực và biến mất!");
            this.changeStatus(BossStatus.LEAVE_MAP);
            return;
        }

        // Bão Hồn 8 Thần Hủy Diệt & Nổ Flash định kỳ mỗi 16 giây
        int stormCooldown = this.currentLevel >= 1 ? 13_500 : 16_500;
        if (Util.canDoWithTime(this.lastSoulStormTime, stormCooldown)) {
            List<Player> players = this.zone.getNotBosses();
            boolean hasTarget = false;
            for (Player pl : players) {
                if (pl != null && pl.isPl() && !pl.isDie() && Util.getDistance(this, pl) <= 460) {
                    hasTarget = true;
                    break;
                }
            }
            if (hasTarget) {
                this.lastSoulStormTime = System.currentTimeMillis();
                this.chat("BÃO HỒN 8 THẦN HỦY DIỆT — HAKAI FLASH!!");
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                        this.location.x, this.location.y, this.location.x, this.location.y);
                int pct = this.currentLevel >= 1 ? 14 : 10;
                for (Player pl : players) {
                    if (pl != null && pl.isPl() && !pl.isDie() && Util.getDistance(this, pl) <= 420) {
                        applyPercentMaxHpStrike(pl, pct, true, false);
                    }
                }
            }
        }
    }

    @Override
    protected void onBossAttackHitPlayer(Player target) {
        if (target == null || target.isDie() || target.nPoint == null || this.zone == null) {
            return;
        }
        long now = System.currentTimeMillis();
        // Kỹ năng 1: Quả Cầu Hakai Cổ Thần (bay từ tay Boss tới mục tiêu + nổ xung kích tím-vàng)
        if (now - this.lastHakaiSphereTime >= 4200 && Util.isTrue(45, 100)) {
            this.lastHakaiSphereTime = now;
            this.chat("Quả Cầu Hakai Cổ Thần!! Tan thành bụi trần!");
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_HAKAI_SPHERE,
                    this.location.x, this.location.y, target.location.x, target.location.y);
            int pct = this.currentLevel >= 1 ? 15 : 11;
            applyPercentMaxHpStrike(target, pct, true, true);
            return;
        }

        // Kỹ năng 2: Trận Pháp Phán Quyết Ai Cập (khóa chân + cột hắc hỏa kim tự tháp)
        if (now - this.lastJudgmentSigilTime >= 6500 && Util.isTrue(38, 100)) {
            this.lastJudgmentSigilTime = now;
            this.chat("Trận Pháp Ai Cập Nguyền Rủa — PHÁN QUYẾT!!");
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_EGYPT_SIGIL_PILLAR,
                    this.location.x, this.location.y, target.location.x, target.location.y);
            applyPercentMaxHpStrike(target, this.currentLevel >= 1 ? 12 : 9, true, false);
            if (target.effectSkill != null && !target.effectSkill.isStun) {
                EffectSkillService.gI().startStun(target, now, 1500);
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        Player owner = resolveRewardOwner(plKill);
        if (this.trialSummon) {
            if (owner != null) {
                Service.gI().sendThongBao(owner, "Bạn đã đánh bại Phân Thân Thử Nghiệm của Thần Hủy Diệt Vũ Trụ 13! Hãy tới Điện Thờ (Map 180) để săn chân thân!");
            }
            if (this.zone != null) {
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                        this.location.x, this.location.y, this.location.x, this.location.y);
            }
            return;
        }
        if (!isFinalStage()) {
            this.announcedPhase2 = false;
            if (owner != null) {
                Service.gI().sendThongBao(owner, "Thần Hủy Diệt Vũ Trụ 13 kích hoạt 8 Thần Hồn Tím vừa cướp đoạt... Hắn hóa thành CỔ THẦN HỦY DIỆT TỐI THƯỢNG!");
            }
            if (this.zone != null) {
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                        this.location.x, this.location.y, this.location.x, this.location.y);
            }
            return;
        }

        // Hạ gục chân thân Trùm Cuối Thần Hủy Diệt Vũ Trụ 13 tại Map 180
        Player winner = owner != null ? owner : plKill;
        dropUniverse6Rewards(winner, 65, 35, 65, true, true, false);
        if (this.zone != null) {
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_SOUL_FLASH_STORM,
                    this.location.x, this.location.y, this.location.x, this.location.y);
        }
        if (winner != null && winner.isPl()) {
            TaskService.gI().checkDoneTaskKillBoss(winner, this);
            winner.vuTru13Stage |= VuTru13Service.STAGE_FLAG_THAN_HUY_DIET_SLAIN;
            VuTru13Service.gI().sendState(winner);
            nro.models.database.PlayerDAO.savePlayerAsync(winner);
            PlayerService.gI().sendInfoHpMpMoney(winner);
            ServerNotify.gI().notify("Kỳ tích 13 Vũ Trụ: Chiến binh " + winner.name
                    + " đã đánh bại Trùm Cuối CỔ THẦN HỦY DIỆT VŨ TRỤ 13 tại Điện Thờ Sâu Thẳm!");
        }
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        if (this.trialSummon) {
            nro.models.boss.Boss_Manager.BossManager.gI().removeBoss(this);
        }
    }
}
