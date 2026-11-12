package nro.models.boss.vodai_vt6;

import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class HitVt6 extends Universe6Boss {

    private static final long REAL_HP = 75_000_000_000L;
    private static final long FLAT_ARMOR = 50_000_000L;
    private static final int ARMOR_PCT = 78;

    private long lastTimeSkip;

    public HitVt6() throws Exception {
        super(BossID.HIT_VT6, BossesData.HIT_VT6);
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
    protected int getMaHonDropCount() {
        return 2;
    }

    @Override
    protected int getBonusPercentMaxHpPerHit() {
        return 11;
    }

    @Override
    protected void configureStageStats() {
        this.nPoint.khangTDHS = true;
        this.nPoint.tlKhangKhongChe = 60;
        this.nPoint.tlNeDon = 200;
        this.lastTimeSkip = System.currentTimeMillis();
    }

    @Override
    protected void onSpecialTick() {
        if (this.zone == null || !Util.canDoWithTime(this.lastTimeSkip, 12_000)) {
            return;
        }
        Player target = getPlayerAttack();
        if (target == null || target.isDie() || target.zone == null || !target.zone.equals(this.zone)) {
            return;
        }
        this.lastTimeSkip = System.currentTimeMillis();
        int behindX = clampZoneX(this.zone, target.location.x + (Util.isTrue(50, 100) ? 36 : -36));
        int groundY = this.zone.map.yPhysicInTop(behindX, target.location.y - 24);
        PlayerService.gI().playerMove(this, behindX, groundY);

        if (target.itemTime != null && target.itemTime.isUseAnPhaThoiGian) {
            this.chat("Hửm?! Ấn Phá Thời Gian đã hóa giải Bước Nhảy 0.5s của ta!");
            applyPercentMaxHpStrike(target, 10, true, false);
            if (target.isPl()) {
                Service.gI().sendThongBao(target, "Ấn Phá Thời Gian phát sáng, hóa giải đóng băng thời gian của Hit!");
            }
        } else {
            this.chat("Tokitobashi! Bước Nhảy Thời Gian 0.5 giây!");
            EffectSkillService.gI().startStun(target, System.currentTimeMillis(), 3000, true);
            applyPercentMaxHpStrike(target, 35, false, true);
            if (target.isPl()) {
                Service.gI().sendThongBao(target,
                        "Trúng Bước Nhảy Thời Gian của Hit (-35% HP & đóng băng 3s)! Hãy dùng Ấn Phá Thời Gian để khắc chế!");
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        dropUniverse6Rewards(plKill, 40, 20, 50, true, false, false);
        tryDropPermanentVipDisguise(plKill, 2026);
    }

    @Override
    public void die(Player plKill) {
        Zone deathZone = this.zone;
        int deathX = (this.location != null) ? this.location.x : 792;
        int deathY = (this.location != null) ? this.location.y : 504;
        super.die(plKill);
        if (deathZone != null) {
            ChampaVt6.summonAngryChampa(deathZone, deathX, deathY);
        }
    }
}

