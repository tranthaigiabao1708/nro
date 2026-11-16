package nro.models.boss.vu_tru_13;

import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.vodai_vt6.Universe6Boss;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;
import nro.models.utils.Util;

public class DaiDeFideVt13 extends Universe6Boss {

    private static final long[] STAGE_REAL_HP = {
        8_000_000_000L,   // Phase 1: Đại Đế Fide (8 tỷ HP)
        20_000_000_000L   // Phase 2: Black Fide (20 tỷ HP)
    };
    private static final long[] STAGE_FLAT_ARMOR = {
        3_500_000L,
        7_000_000L
    };
    private static final int[] STAGE_ARMOR_PCT = {18, 25};

    public DaiDeFideVt13() throws Exception {
        super(BossID.DAI_DE_FIDE_VT13, BossesData.DAI_DE_FIDE_VT13, BossesData.BLACK_FIDE_VT13);
    }

    @Override
    protected long getRealHpForCurrentStage() {
        int idx = Math.max(0, Math.min(STAGE_REAL_HP.length - 1, this.currentLevel));
        return STAGE_REAL_HP[idx];
    }

    @Override
    protected long getFlatArmorForCurrentStage() {
        int idx = Math.max(0, Math.min(STAGE_FLAT_ARMOR.length - 1, this.currentLevel));
        return STAGE_FLAT_ARMOR[idx];
    }

    @Override
    protected int getArmorPercentForCurrentStage() {
        int idx = Math.max(0, Math.min(STAGE_ARMOR_PCT.length - 1, this.currentLevel));
        return STAGE_ARMOR_PCT[idx];
    }

    @Override
    protected int getMaHonDropCount() {
        return 5;
    }

    @Override
    protected void configureStageStats() {
        this.nPoint.tlKhangKhongChe = this.currentLevel >= 1 ? 45 : 25;
    }

    @Override
    protected void onBossAttackHitPlayer(Player target) {
        if (target == null || target.isDie() || target.nPoint == null) {
            return;
        }
        if (this.currentLevel >= 1 && Util.isTrue(25, 100)) {
            applyPercentMaxHpStrike(target, 12, true, true);
            if (Util.isTrue(30, 100) && target.effectSkill != null && !target.effectSkill.isStun) {
                this.chat("Hắc Ám Đế Vương Trảm!!");
                EffectSkillService.gI().startStun(target, System.currentTimeMillis(), 1500);
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        Player owner = resolveRewardOwner(plKill);
        if (!isFinalStage()) {
            // Hoàn thành mốc chạm trán Đại Đế Fide (Phase 1 -> biến hình Black Fide)
            if (owner != null) {
                TaskService.gI().checkDoneTaskKillBoss(owner, this);
                Service.gI().sendThongBao(owner, "Đại Đế Fide gầm rú hấp thụ tà khí cổ thần... Hắn đang biến hình thành BLACK FIDE!");
            }
            return;
        }
        // Phase cuối Black Fide bị đánh bại: rơi thưởng cấp cao + kích hoạt Cutscene mở phong ấn Thần Hủy Diệt VT13!
        dropUniverse6Rewards(owner != null ? owner : plKill, 40, 20, 45, true, false, false);
        VuTru13Service.gI().triggerBlackFideSealBreak(owner != null ? owner : plKill, this.zone);
    }
}
