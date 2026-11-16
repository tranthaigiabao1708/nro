package nro.models.boss.vodai_vt6;

import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.utils.Util;

public class CauliflaVt6 extends Universe6Boss {

    private static final long[] STAGE_REAL_HP = {
        9_000_000_000L,
        21_000_000_000L
    };
    private static final long[] STAGE_FLAT_ARMOR = {
        11_666_667L,
        14_000_000L
    };
    private static final int[] STAGE_ARMOR_PCT = {25, 25};

    private long lastCrushCannonTime;

    public CauliflaVt6() throws Exception {
        super(BossID.CAULIFLA_VT6, BossesData.CAULIFLA_VT6_1, BossesData.CAULIFLA_VT6_2);
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
    public int getHitPercentMaxHp() {
        return this.currentLevel == 0 ? 29 : 33;
    }

    @Override
    protected void configureStageStats() {
        this.nPoint.tlKhangKhongChe = (this.currentLevel == 0) ? 35 : 50;
        this.nPoint.tlNeDon = (short) ((this.currentLevel == 0) ? 120 : 350);
    }

    @Override
    protected void onSpecialTick() {
        if (this.currentLevel < 1 || this.zone == null || !Util.canDoWithTime(this.lastCrushCannonTime, 10_000)) {
            return;
        }
        this.lastCrushCannonTime = System.currentTimeMillis();
        boolean hitAny = false;
        for (Player pl : this.zone.getNotBosses()) {
            if (pl == null || pl.isDie() || Util.getDistance(this, pl) > 360) {
                continue;
            }
            hitAny = true;
            applyPercentMaxHpStrike(pl, 14, true, false);
        }
        if (hitAny) {
            this.chat("Tuyệt kỹ SSJ2: Crush Cannon liên hoàn pháo!");
            notifyZonePlayers(this.zone,
                    "SSJ2 Caulifla phóng mưa đạn Crush Cannon càn quét phạm vi gần!");
        }
    }

    @Override
    public void reward(Player plKill) {
        if (!isFinalStage()) {
            return;
        }
        dropUniverse6Rewards(plKill, 40, 20, 50, true, false, false);
    }
}
