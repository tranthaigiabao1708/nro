package nro.models.boss.vu_tru_13;

import java.util.List;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.boss.vodai_vt6.Universe6Boss;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstTaskBadges;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.player.Pet;
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

/**
 * Boss Fide Đen (20 Tỷ HP) xuất hiện độc lập tại các map Vũ Trụ 13:
 * - Khe Nứt Nguyền Rủa VT13 (Map 178)
 * - Ngục Tù Goku VT13 (Map 179)
 * - Vực Thẳm VT13 (Map 182)
 *
 * Bộ kỹ năng:
 * 1. Cầu Hắc Ám (Dark Death Ball): Phá khiên mục tiêu, gây sát thương %HP tối đa + nổ lan vùng lân cận.
 * 2. Trói Ki Đen (Dark Ki Bind): Phóng xiềng xích Ki Đen khóa chặt mục tiêu 2.5s và rút %HP.
 * 3. Khiên Hấp Thụ Hắc Ám (Dark Absorption Shield): Giảm mạnh sát thương nhận vào và chuyển hóa đòn đánh thành hồi máu (khắc chế bằng hiệu ứng khống chế).
 */
public class FideDen extends Universe6Boss {

    public static final long REAL_HP = 20_000_000_000L; // 20 Tỷ HP
    public static final long FLAT_ARMOR = 6_500_000L;
    public static final int ARMOR_PCT = 25;

    // Cooldown & trạng thái bộ 3 chiêu thức
    private long lastDarkBallTime;
    private long lastDarkBindTime;
    private long lastAbsorbShieldTime;
    private boolean absorbShieldActive;
    private long absorbShieldStartTime;
    private long lastAbsorbHealTick;

    public FideDen() throws Exception {
        super(BossID.FIDE_DEN, BossesData.FIDE_DEN);
        this.isZone01SpawnDisabled = false;
    }

    public void resetCombatTimersOnTestSummon() {
        this.absorbShieldActive = false;
        long now = System.currentTimeMillis();
        this.lastDarkBallTime = now - 3500L;
        this.lastDarkBindTime = now - 8500L;
        this.lastAbsorbShieldTime = now - 15_000L;
        this.lastAbsorbHealTick = 0L;
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
        return 3; // Rơi 3 Hồn Boss Tím [2018]
    }

    @Override
    protected void configureStageStats() {
        // Giữ 0% kháng khống chế để người chơi có thể dùng Trói / Choáng / Socola / Thôi miên phá Khiên Hấp Thụ!
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

    private void deactivateAbsorbShield(boolean shatteredByCc) {
        if (!this.absorbShieldActive) {
            return;
        }
        this.absorbShieldActive = false;
        if (this.effectSkill != null) {
            this.effectSkill.isShielding = false;
        }
        if (this.zone != null) {
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_DARK_SHIELD_BREAK,
                    this.location.x, this.location.y, this.location.x, this.location.y);
            if (shatteredByCc) {
                this.chat("Gừ!! Khiên Hấp Thụ Hắc Ám của ta bị phá vỡ rồi!!");
                notifyZonePlayers(this.zone, "Khiên Hấp Thụ Hắc Ám của Fide Đen đã bị khống chế đánh vỡ!");
            }
        }
    }

    @Override
    public void joinMap() {
        this.absorbShieldActive = false;
        long now = System.currentTimeMillis();
        // Khởi tạo cooldown ngắn hơn ở nhịp đầu để Fide Đen tung đủ bộ chiêu động sớm khi chạm trán
        this.lastDarkBallTime = now - 3000L;
        this.lastDarkBindTime = now - 6000L;
        this.lastAbsorbShieldTime = now - 12_000L;
        this.lastAbsorbHealTick = 0L;

        // Xóa ghim khu cũ và chọn hoàn toàn ngẫu nhiên 1 trong 3 map {178, 179, 182}, khu ngẫu nhiên & tọa độ bất kỳ
        this.lastZone = null;
        this.zone = null;
        int[] spawnMaps = new int[]{178, 179, 182};
        int chosenMapId = spawnMaps[Util.nextInt(0, spawnMaps.length - 1)];
        nro.models.map.Map chosenMap = nro.models.map.service.MapService.gI().getMapById(chosenMapId);
        if (chosenMap != null && chosenMap.zones != null && !chosenMap.zones.isEmpty()) {
            int randZoneIdx = Util.nextInt(0, chosenMap.zones.size() - 1);
            for (int attempt = 0; attempt < chosenMap.zones.size(); attempt++) {
                int zIdx = (randZoneIdx + attempt) % chosenMap.zones.size();
                nro.models.map.Zone candidateZone = chosenMap.zones.get(zIdx);
                if (candidateZone != null && candidateZone.getBosses().isEmpty()) {
                    this.zoneFinal = candidateZone;
                    break;
                }
            }
            if (this.zoneFinal == null) {
                this.zoneFinal = chosenMap.zones.get(randZoneIdx);
            }
        }
        super.joinMap();
        this.zoneFinal = null;
        if (this.zone != null && this.zone.map != null) {
            ServerNotify.gI().notify("BOSS VŨ TRỤ 13: Fide Đen (20 Tỷ HP) vừa xuất hiện tại "
                    + this.zone.map.mapName + " (Khu " + this.zone.zoneId + ")!");
        }
    }

    @Override
    public Player getPlayerAttack() {
        if (this.zone == null) {
            return null;
        }
        if (this.playerTarger != null && (this.playerTarger.isDie()
                || !this.zone.equals(this.playerTarger.zone)
                || VuTru13Service.gI().isKheNutMobsSuppressed(this.playerTarger))) {
            this.playerTarger = null;
        }
        if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            Player candidate = this.zone.getRandomPlayerInMap();
            if (candidate != null && !VuTru13Service.gI().isKheNutMobsSuppressed(candidate)) {
                this.playerTarger = candidate;
            } else {
                this.playerTarger = null;
            }
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(5000, 7000);
        }
        if (this.playerTarger != null && this.playerTarger.isPet
                && ((Pet) this.playerTarger).master != null
                && ((Pet) this.playerTarger).master.equals(this)) {
            this.playerTarger = null;
        }
        return this.playerTarger;
    }

    @Override
    public void update() {
        if (!this.isDie() && this.zone != null && this.bossStatus == BossStatus.ACTIVE) {
            long now = System.currentTimeMillis();
            if (this.absorbShieldActive) {
                if (isUnderCrowdControl()) {
                    deactivateAbsorbShield(true);
                } else if (now - this.absorbShieldStartTime >= 6000L) {
                    deactivateAbsorbShield(false);
                }
            }
        }
        super.update();
    }

    @Override
    protected void onSpecialTick() {
        if (this.isDie() || this.zone == null || this.bossStatus != BossStatus.ACTIVE) {
            return;
        }
        if (isUnderCrowdControl()) {
            return;
        }
        long now = System.currentTimeMillis();

        // Chiêu 3: Khiên Hấp Thụ Hắc Ám Động (mỗi 20 giây, duy trì 6 giây bằng vòm cầu 3D đa khung hình)
        if (!this.absorbShieldActive && now - this.lastAbsorbShieldTime >= 20_000L) {
            List<Player> players = this.zone.getNotBosses();
            boolean hasActiveTarget = false;
            for (Player pl : players) {
                if (pl != null && pl.isPl() && !pl.isDie() && !VuTru13Service.gI().isKheNutMobsSuppressed(pl)) {
                    hasActiveTarget = true;
                    break;
                }
            }
            if (hasActiveTarget) {
                this.absorbShieldActive = true;
                this.absorbShieldStartTime = now;
                this.lastAbsorbShieldTime = now;
                VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_DARK_ABSORB_SHIELD,
                        this.location.x, this.location.y, this.location.x, this.location.y);
                this.chat("KHIÊN HẤP THỤ HẮC ÁM!! Mọi đòn đánh của các ngươi sẽ nuôi dưỡng sức mạnh cho ta!");
                notifyZonePlayers(this.zone, "CẢNH BÁO: Fide Đen vừa bật [Khiên Hấp Thụ Hắc Ám] (6s) — Hãy dùng chiêu khống chế để phá khiên hoặc ngừng dồn sát thương!");
                return;
            }
        }

        // Chiêu 2: Trói Ki Đen Động (mỗi 11 giây phóng xích sấm sét 3D khóa chặt tối đa 2 mục tiêu suốt 2.5 giây)
        if (now - this.lastDarkBindTime >= 11_000L) {
            List<Player> players = this.zone.getNotBosses();
            int boundCount = 0;
            for (Player pl : players) {
                if (pl != null && pl.isPl() && !pl.isDie()
                        && !VuTru13Service.gI().isKheNutMobsSuppressed(pl)
                        && Util.getDistance(this, pl) <= 440) {
                    if (boundCount == 0) {
                        this.lastDarkBindTime = now;
                        this.chat("TRÓI KI ĐEN!! Đứng yên chịu trận đi lũ sâu bọ!");
                    }
                    VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_DARK_KI_BIND,
                            this.location.x, this.location.y, pl.location.x, pl.location.y);
                    applyPercentMaxHpStrike(pl, 10, true, false);
                    if (pl.effectSkill != null && !pl.effectSkill.isStun) {
                        EffectSkillService.gI().startStun(pl, now, 2500);
                        Service.gI().sendThongBao(pl, "Bạn bị Trói Ki Đen của Fide Đen khóa chặt trong 2.5 giây!");
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
    protected void onBossAttackHitPlayer(Player target) {
        if (target == null || target.isDie() || target.nPoint == null || this.zone == null) {
            return;
        }
        long now = System.currentTimeMillis();
        // Chiêu 1: Cầu Hắc Ám Tối Thượng Động — Phá khiên mục tiêu + nổ lan xung quanh
        if (now - this.lastDarkBallTime >= 4500L && Util.isTrue(65, 100)) {
            this.lastDarkBallTime = now;
            this.chat("CẦU HẮC ÁM TỐI THƯỢNG!! Tan thành tro bụi đi!!");
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_DARK_DEATH_BALL,
                    this.location.x, this.location.y, target.location.x, target.location.y);
            applyPercentMaxHpStrike(target, 14, true, true);

            // Sát thương nổ lan 8% HP tối đa cho người chơi đứng gần điểm nổ (180px)
            for (Player near : this.zone.getNotBosses()) {
                if (near != null && near.isPl() && !near.isDie() && !near.equals(target)
                        && !VuTru13Service.gI().isKheNutMobsSuppressed(near)
                        && Util.getDistance(target, near) <= 180) {
                    applyPercentMaxHpStrike(near, 8, true, false);
                }
            }
        } else if (Util.isTrue(50, 100)) {
            // Đòn đánh cận chiến / chưởng thường cũng bùng nổ hiệu ứng Hắc Ám đa khung hình động!
            VuTru13Service.gI().broadcastBossSkillVfx(this.zone, VuTru13Service.BOSS_VFX_DARK_SHIELD_BREAK,
                    target.location.x, target.location.y, target.location.x, target.location.y);
        }
    }

    @Override
    protected long alterDamageAfterArmor(Player plAtt, long damageAfterArmor) {
        if (this.absorbShieldActive && !this.isDie()) {
            long now = System.currentTimeMillis();
            // Hấp thụ đòn đánh chuyển thành hồi 1.5% HP tối đa (tối đa 1 lần / giây)
            if (now - this.lastAbsorbHealTick >= 1000L && this.nPoint.hp < this.nPoint.hpMax) {
                this.lastAbsorbHealTick = now;
                long heal = Math.max(1L, this.nPoint.hpMax * 15L / 1000L); // 1.5% của 20 tỷ = 300 triệu HP
                this.nPoint.addHp(heal);
                PlayerService.gI().sendInfoHpMp(this);
                Service.gI().Send_Info_NV(this);
            }
            // Giảm 65% sát thương nhận vào khi Khiên Hấp Thụ đang bật
            return Math.max(1L, damageAfterArmor * 35L / 100L);
        }
        return damageAfterArmor;
    }

    @Override
    public void reward(Player plKill) {
        deactivateAbsorbShield(false);
        Player owner = resolveRewardOwner(plKill);
        Player winner = owner != null ? owner : plKill;

        if (winner != null) {
            BadgesTaskService.updateCountBagesTask(winner, ConstTaskBadges.TRUM_SAN_BOSS, 1);
            TaskService.gI().checkDoneTaskKillBoss(winner, this);
            winner.vuTru13Stage |= VuTru13Service.STAGE_FLAG_BLACK_FIDE_SEAL;
            VuTru13Service.gI().sendState(winner);
        }
        if (plKill != null && plKill != winner && plKill.isPl()) {
            TaskService.gI().checkDoneTaskKillBoss(plKill, this);
            plKill.vuTru13Stage |= VuTru13Service.STAGE_FLAG_BLACK_FIDE_SEAL;
            VuTru13Service.gI().sendState(plKill);
        }

        if (this.zone == null || this.zone.map == null) {
            return;
        }

        long ownerId = winner != null ? winner.id : -1L;
        int baseX = clampZoneX(this.zone, this.location.x);
        int baseY = this.location.y;

        // 1. 100% rơi 2 -> 5 Mảnh Thần Vực Vô Cực [2034]
        try {
            int shardQty = Util.nextInt(2, 5);
            int x1 = clampZoneX(this.zone, baseX - 24);
            int y1 = this.zone.map.yPhysicInTop(x1, baseY - 24);
            ItemMap shardMap = new ItemMap(this.zone, VuTru13Service.ITEM_MANH_THAN_VUC_VO_CUC, shardQty, x1, y1, ownerId);
            shardMap.options.add(new Item.ItemOption(30, 0));
            Service.gI().dropItemMap(this.zone, shardMap);
        } catch (Exception e) {
            Logger.logException(FideDen.class, e);
        }

        // 2. 65% rơi 1 -> 3 Đá nâng cấp SKH [1826]
        if (Util.isTrue(65, 100)) {
            try {
                int stoneQty = Util.nextInt(1, 3);
                int x2 = clampZoneX(this.zone, baseX);
                int y2 = this.zone.map.yPhysicInTop(x2, baseY - 24);
                ItemMap skhStone = new ItemMap(this.zone, 1826, stoneQty, x2, y2, ownerId);
                skhStone.options.add(new Item.ItemOption(73, 0));
                Service.gI().dropItemMap(this.zone, skhStone);
            } catch (Exception e) {
                Logger.logException(FideDen.class, e);
            }
        }

        // 3. 40% rơi 1 -> 2 Con dấu [1794]
        if (Util.isTrue(40, 100)) {
            try {
                int sealQty = Util.nextInt(1, 2);
                int x3 = clampZoneX(this.zone, baseX + 24);
                int y3 = this.zone.map.yPhysicInTop(x3, baseY - 24);
                ItemMap conDau = new ItemMap(this.zone, 1794, sealQty, x3, y3, ownerId);
                conDau.options.add(new Item.ItemOption(73, 0));
                Service.gI().dropItemMap(this.zone, conDau);
            } catch (Exception e) {
                Logger.logException(FideDen.class, e);
            }
        }
    }

    @Override
    public void leaveMap() {
        deactivateAbsorbShield(false);
        super.leaveMap();
    }
}
