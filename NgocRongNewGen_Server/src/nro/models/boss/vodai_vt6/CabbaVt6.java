package nro.models.boss.vodai_vt6;

import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.utils.Util;

public class CabbaVt6 extends Universe6Boss {

    private static final long REAL_HP = 36_000_000_000L;
    private static final long FLAT_ARMOR = 35_000_000L;
    private static final int ARMOR_PCT = 75;

    private boolean awakenedSuperSaiyan;
    private long lastShieldBreakTime;

    public CabbaVt6() throws Exception {
        super(BossID.CABBA_VT6, BossesData.CABBA_VT6);
    }

    @Override
    protected long getRealHpForCurrentStage() {
        return REAL_HP;
    }

    @Override
    protected long getFlatArmorForCurrentStage() {
        return FLAT_ARMOR;
    }

    @Override
    protected int getArmorPercentForCurrentStage() {
        return ARMOR_PCT;
    }

    @Override
    protected int getBonusPercentMaxHpPerHit() {
        return this.awakenedSuperSaiyan ? 11 : 7;
    }

    @Override
    protected void configureStageStats() {
        this.awakenedSuperSaiyan = false;
        this.nPoint.tlKhangKhongChe = 35;
    }

    @Override
    protected void onSpecialTick() {
        if (!this.awakenedSuperSaiyan && this.nPoint.getCurrPercentHP() <= 50) {
            this.awakenedSuperSaiyan = true;
            this.nPoint.dameg = this.nPoint.dameg / 100L * 135L;
            this.nPoint.calPoint();
            this.nPoint.syncDisplayHpFromBossReal();
            this.chat("Vì niềm kiêu hãnh của Hành Tinh Sadala! Hây aaaaa Super Saiyan!");
            if (this.zone != null) {
                notifyZonePlayers(this.zone,
                        "Saiyan Cabba bùng nổ trạng thái Super Saiyan, sức công phá tăng vọt!");
            }
        }
    }

    @Override
    protected void onBossAttackHitPlayer(Player target) {
        if (target == null || target.isDie() || target.effectSkill == null) {
            return;
        }
        if (target.effectSkill.isShielding && Util.canDoWithTime(this.lastShieldBreakTime, 9_000)) {
            this.lastShieldBreakTime = System.currentTimeMillis();
            this.chat("Galick Pháo Sadala xuyên phá!");
            EffectSkillService.gI().breakShield(target);
        }
    }

    @Override
    public void reward(Player plKill) {
        dropUniverse6Rewards(plKill, 30, 15, 45, false, false, false);
    }
}
