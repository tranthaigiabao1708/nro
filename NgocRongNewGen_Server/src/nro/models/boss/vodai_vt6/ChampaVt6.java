package nro.models.boss.vodai_vt6;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.boss.BossesData;
import nro.models.consts.BossStatus;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public class ChampaVt6 extends Universe6Boss {

    private static final long REAL_HP = 110_000_000_000L;
    private static final long FLAT_ARMOR = 65_000_000L;
    private static final int ARMOR_PCT = 80;

    private long lastHakaiCycleTime;
    private boolean chargingHakai;
    private long hakaiDetonateAt;

    public ChampaVt6() throws Exception {
        super(BossID.CHAMPA_VT6, BossesData.CHAMPA_VT6);
    }

    public static void summonAngryChampa(Zone arenaZone, int spawnX, int spawnY) {
        if (arenaZone == null || arenaZone.map == null) {
            return;
        }
        try {
            for (Player bossPl : arenaZone.getBosses()) {
                if (bossPl instanceof ChampaVt6 && !bossPl.isDie()) {
                    return;
                }
            }
            ChampaVt6 champa = new ChampaVt6();
            champa.currentLevel = 0;
            champa.initBase();
            int safeX = clampZoneX(arenaZone, spawnX);
            int safeY = arenaZone.map.yPhysicInTop(safeX, spawnY - 24);
            ChangeMapService.gI().changeMap(champa, arenaZone, safeX, safeY);
            champa.lastTimeJoinZone = System.currentTimeMillis();
            champa.lastHakaiCycleTime = System.currentTimeMillis();
            champa.changeStatus(BossStatus.CHAT_S);
            ServerNotify.gI().notify("CẢNH BÁO: Sát Thủ Hit vừa gục ngã, THẦN HỦY DIỆT CHAMPA nổi cơn thịnh nộ giáng lâm xuống Võ Đài Siêu Cấp (Khu "
                    + arenaZone.zoneId + ")!");
        } catch (Exception e) {
            Logger.logException(ChampaVt6.class, e);
        }
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
        return 3;
    }

    @Override
    protected int getBonusPercentMaxHpPerHit() {
        return 13;
    }

    @Override
    protected void configureStageStats() {
        this.nPoint.khangTDHS = true;
        this.nPoint.tlKhangKhongChe = 70;
        this.chargingHakai = false;
        this.lastHakaiCycleTime = System.currentTimeMillis();
    }

    @Override
    protected void onSpecialTick() {
        if (this.zone == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!this.chargingHakai && Util.canDoWithTime(this.lastHakaiCycleTime, 18_000)) {
            this.chargingHakai = true;
            this.hakaiDetonateAt = now + 3_000L;
            this.lastHakaiCycleTime = now;
            this.chat("HAKAI! 3 giây nữa ta sẽ xóa sổ toàn bộ võ đài này!");
            notifyZonePlayers(this.zone,
                    "CẢNH BÁO HAKAI: 3 giây nữa Champa phóng Cầu Hủy Diệt! Bật Khiên Năng Lượng hoặc lùi xa 450px ngay!");
            return;
        }

        if (this.chargingHakai && now >= this.hakaiDetonateAt) {
            this.chargingHakai = false;
            this.lastHakaiCycleTime = now;
            this.chat("BIẾN MẤT ĐI! NĂNG LƯỢNG HỦY DIỆT HAKAI!");
            for (Player pl : this.zone.getNotBosses()) {
                if (pl == null || pl.isDie() || pl.nPoint == null || Util.getDistance(this, pl) > 450) {
                    continue;
                }
                if (pl.effectSkill != null && pl.effectSkill.isShielding) {
                    EffectSkillService.gI().breakShield(pl);
                    applyPercentMaxHpStrike(pl, 5, false, false);
                    if (pl.isPl()) {
                        Service.gI().sendThongBao(pl, "Khiên Năng Lượng đã đỡ đòn Hakai của Champa và vỡ tan!");
                    }
                } else {
                    pl.nPoint.hakaiNoPeaUntil = now + 5_000L;
                    applyPercentMaxHpStrike(pl, 75, false, false);
                    if (pl.isPl()) {
                        Service.gI().sendThongBao(pl,
                                "Trúng trực diện HAKAI của Champa (-75% HP & Phong ấn Đậu Thần 5 giây)!");
                    }
                }
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        dropUniverse6Rewards(plKill, 50, 30, 60, true, true, true);
        tryDropPermanentVipDisguise(plKill, 2027);
    }

    @Override
    public void autoLeaveMap() {
        super.autoLeaveMap();
        if (this.zone != null && this.zone.getNumOfPlayers() == 0
                && Util.canDoWithTime(this.lastTimeJoinZone, 1_200_000)) {
            this.leaveMap();
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.changeStatus(BossStatus.REST);
        BossManager.gI().removeBoss(this);
    }
}
