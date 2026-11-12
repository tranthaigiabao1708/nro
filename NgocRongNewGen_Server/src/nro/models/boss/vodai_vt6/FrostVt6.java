package nro.models.boss.vodai_vt6;

import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class FrostVt6 extends Universe6Boss {

    private static final long[] STAGE_REAL_HP = {
        8_000_000_000L,
        12_000_000_000L,
        18_000_000_000L
    };
    private static final long[] STAGE_FLAT_ARMOR = {
        20_000_000L,
        24_000_000L,
        28_000_000L
    };
    private static final int[] STAGE_ARMOR_PCT = {72, 73, 74};

    public FrostVt6() throws Exception {
        super(BossID.FROST_VT6, BossesData.FROST_VT6_1, BossesData.FROST_VT6_2, BossesData.FROST_VT6_3);
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
    protected int getBonusPercentMaxHpPerHit() {
        return 5 + this.currentLevel * 2;
    }

    @Override
    protected void configureStageStats() {
        this.nPoint.tlKhangKhongChe = 25;
    }

    @Override
    protected void onBossAttackHitPlayer(Player target) {
        if (target == null || target.isDie() || target.nPoint == null) {
            return;
        }
        if (target.itemTime != null && target.itemTime.isUseThuocGiaiVt6) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean newlyPoisoned = now >= target.nPoint.frostPoisonUntil;
        target.nPoint.frostPoisonUntil = now + 10_000L;
        if (newlyPoisoned) {
            Service.gI().sendThongBao(target, "Trúng Kim Độc của Frost: Giảm 30% Sức đánh trong 10s! (Dùng Thuốc Giải VT6 để kháng)");
        }
        if (Util.isTrue(25, 100) && target.effectSkill != null && !target.effectSkill.isStun) {
            this.chat("Kim độc tê liệt!");
            EffectSkillService.gI().startStun(target, now, 2000);
        }
    }

    @Override
    public void reward(Player plKill) {
        if (!isFinalStage()) {
            return;
        }
        dropUniverse6Rewards(plKill, 30, 15, 40, false, false, false);
    }
}

