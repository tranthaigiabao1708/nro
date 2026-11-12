package nro.models.boss.vodai_vt6;

import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.consts.BossType;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstTaskBadges;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.matches.dai_hoi_vo_thuat.VoDaiVuTru6;
import nro.models.matches.giai_dau.VoDaiVuTru6Manager;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.services.EffectSkillService;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.skill.Skill;
import nro.models.task.BadgesTaskService;
import nro.models.utils.Logger;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

public abstract class Universe6Boss extends Boss {

    protected long lastTimeJoinZone;

    public Universe6Boss(int id, BossData... data) throws Exception {
        super(id, data);
    }

    public Universe6Boss(BossType bossType, int id, BossData... data) throws Exception {
        super(bossType, id, data);
    }

    protected abstract long getRealHpForCurrentStage();

    protected abstract long getFlatArmorForCurrentStage();

    protected abstract int getArmorPercentForCurrentStage();

    protected int getMaHonDropCount() {
        return 1;
    }

    protected int getBonusPercentMaxHpPerHit() {
        return 6;
    }

    protected void configureStageStats() {
    }

    protected long alterDamageAfterArmor(Player plAtt, long damageAfterArmor) {
        return damageAfterArmor;
    }

    protected void onSpecialTick() {
    }

    protected void onBossAttackHitPlayer(Player target) {
    }

    @Override
    public void initBase() {
        super.initBase();
        long realHp = Math.max(2_200_000_000L, getRealHpForCurrentStage());
        this.nPoint.bossRealHpMax = realHp;
        this.nPoint.bossRealHp = realHp;
        this.nPoint.hpg = 2_000_000_000;
        this.nPoint.hpMax = 2_000_000_000;
        this.nPoint.syncDisplayHpFromBossReal();
        configureStageStats();
    }

    @Override
    public void joinMap() {
        super.joinMap();
        this.lastTimeJoinZone = System.currentTimeMillis();
    }

    @Override
    public void active() {
        if (this.effectSkill != null && this.effectSkill.isBinh && this.nPoint.tlKhangKhongChe >= 100) {
            EffectSkillService.gI().BinhDown(this);
        }
        onSpecialTick();
        super.active();
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 250) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie() || pl.zone == null || !pl.zone.equals(this.zone)) {
                    return;
                }
                if (this.playerSkill.skills.isEmpty()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 180)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 60));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 40));
                        }
                    }
                    nro.models.services.SkillService.gI().useSkill(this, pl, null, -1, null);
                    int pctBonus = getBonusPercentMaxHpPerHit();
                    if (pctBonus > 0 && !pl.isDie()) {
                        applyPercentMaxHpStrike(pl, pctBonus, true, false);
                    }
                    if (!pl.isDie()) {
                        onBossAttackHitPlayer(pl);
                    }
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(2, 3)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                Logger.logException(Universe6Boss.class, ex);
            }
        }
    }

    protected void applyPercentMaxHpStrike(Player target, int pctMaxHp, boolean blockedByShield, boolean shatterShield) {
        if (target == null || target.isDie() || target.nPoint == null || pctMaxHp <= 0) {
            return;
        }
        if (target.effectSkill != null && target.effectSkill.isShielding) {
            if (shatterShield) {
                EffectSkillService.gI().breakShield(target);
            }
            if (blockedByShield) {
                return;
            }
        }
        long strike = (long) target.nPoint.hpMax * pctMaxHp / 100L;
        if (target.itemTime != null) {
            if (target.itemTime.isUseGiapXen2) {
                strike = strike * 70L / 100L;
            } else if (target.itemTime.isUseGiapXen) {
                strike = strike * 80L / 100L;
            }
        }
        if (strike <= 0L) {
            return;
        }
        if (target.zone != null && target.zone.map != null && target.zone.map.mapId == 145 && target.isPKDHVT) {
            if (strike >= target.nPoint.hp) {
                target.lostByDeath = true;
                VoDaiVuTru6 mc = VoDaiVuTru6Manager.gI().getMC(target.zone);
                if (mc != null) {
                    mc.die();
                }
                return;
            }
        }
        target.nPoint.subHP(strike);
        PlayerService.gI().sendInfoHpMp(target);
        Service.gI().Send_Info_NV(target);
        if (target.isDie()) {
            target.setDie();
        }
    }

    public static void notifyZonePlayers(Zone targetZone, String text) {
        if (targetZone == null || text == null || text.isEmpty()) {
            return;
        }
        for (Player pl : targetZone.getNotBosses()) {
            if (pl != null && pl.isPl()) {
                Service.gI().sendThongBao(pl, text);
            }
        }
    }

    protected int resolveAttackerArmorPen(Player plAtt) {
        if (plAtt == null || plAtt.nPoint == null) {
            return 0;
        }
        Skill activeSkill = null;
        if (plAtt.newSkill != null && plAtt.newSkill.isStartSkillSpecial && plAtt.newSkill.skillSelect != null) {
            activeSkill = plAtt.newSkill.skillSelect;
        } else if (plAtt.playerSkill != null) {
            activeSkill = plAtt.playerSkill.skillSelect;
        }
        if (activeSkill == null || activeSkill.template == null) {
            return 0;
        }
        int pen = switch (activeSkill.template.id) {
            case Skill.KAMEJOKO,
                 Skill.MASENKO,
                 Skill.ANTOMIC,
                 Skill.SUPER_KAME,
                 Skill.LIEN_HOAN_CHUONG,
                 Skill.MAKANKOSAPPO,
                 Skill.QUA_CAU_KENH_KHI -> plAtt.nPoint.tlxgc;
            case Skill.DRAGON,
                 Skill.DEMON,
                 Skill.GALICK,
                 Skill.LIEN_HOAN,
                 Skill.KAIOKEN -> plAtt.nPoint.tlxgcc;
            case Skill.TU_SAT -> Math.max(plAtt.nPoint.tlxgc, plAtt.nPoint.tlxgcc);
            default -> 0;
        };
        return Math.max(0, Math.min(100, pen));
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }
        int neDon = this.nPoint.tlNeDon;
        if (plAtt != null && plAtt.nPoint != null) {
            neDon = Math.max(0, neDon - plAtt.nPoint.tlchinhxac * 10);
        }
        if (!piercing && neDon > 0 && Util.isTrue(neDon, 1000)) {
            this.chat("Quá chậm!");
            return 0;
        }
        if (plAtt != null && plAtt.idNRNM != -1) {
            return 1;
        }

        // Hệ thống Giáp Boss Vũ Trụ 6 hai tầng (KHÔNG đặt trần sát thương nhân tạo mỗi đòn)
        int pen = resolveAttackerArmorPen(plAtt);
        long flatDef = Math.max(0L, getFlatArmorForCurrentStage());
        int armorPct = Math.max(0, Math.min(95, getArmorPercentForCurrentStage()));
        if (pen > 0) {
            flatDef -= flatDef * (pen * 25L) / 10_000L;
            armorPct = Math.max(0, armorPct - (armorPct * pen / 1000));
        }

        long finalDamage;
        long afterFlat = damage - flatDef;
        if (afterFlat <= 0L) {
            finalDamage = 1L;
        } else {
            finalDamage = afterFlat * Math.max(1, 100 - armorPct) / 100L;
            finalDamage = alterDamageAfterArmor(plAtt, finalDamage);
            if (finalDamage < 1L) {
                finalDamage = 1L;
            }
        }

        this.nPoint.subHP(finalDamage);
        if (isDie()) {
            this.setDie(plAtt);
            die(plAtt);
        }
        return (int) Math.min(2_147_483_647L, finalDamage);
    }

    protected boolean isFinalStage() {
        return this.data != null && this.currentLevel == this.data.length - 1;
    }

    @Override
    public void dropBossSoul(Zone targetZone, int baseX, int baseY, Player plKill) {
        if (!isFinalStage() || this.soulDroppedForCurrentLife) {
            return;
        }
        int soulId = getBossSoulItemId();
        int count = Math.max(1, getMaHonDropCount());
        if (soulId <= 0 || targetZone == null || targetZone.map == null || plKill == null || plKill.isBot) {
            return;
        }
        this.soulDroppedForCurrentLife = true;
        try {
            Player owner = resolveRewardOwner(plKill);
            long ownerId = (owner != null) ? owner.id : plKill.id;
            for (int i = 0; i < count; i++) {
                int offset = (i - (count - 1) / 2) * 16 + 8;
                int xDrop = clampZoneX(targetZone, baseX + offset);
                int yDrop = targetZone.map.yPhysicInTop(xDrop, baseY - 24);
                ItemMap soulMap = new ItemMap(targetZone, soulId, 1, xDrop, yDrop, ownerId);
                soulMap.options.add(new Item.ItemOption(73, 0));
                Service.gI().dropItemMap(targetZone, soulMap);
            }
        } catch (Exception e) {
            Logger.logException(Universe6Boss.class, e);
        }
    }

    protected static int clampZoneX(Zone targetZone, int x) {
        if (targetZone == null || targetZone.map == null) {
            return x;
        }
        return Math.max(24, Math.min(targetZone.map.mapWidth - 24, x));
    }

    protected void dropUniverse6Rewards(
            Player plKill,
            int goldBars,
            int godItemPercent,
            int hematitePercent,
            boolean dropAngelStone25Pct,
            boolean dropVipRecipe50Pct,
            boolean dropZenoSeal2Pct) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill == null || this.zone == null || this.zone.map == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : plKill.id;
        int baseX = this.location.x;
        int baseY = this.location.y;

        // 1. Thỏi vàng cố định + nội tại Cánh (20% nổ hũ +100 Thỏi vàng)
        dropGoldBarsStatic(this.zone, baseX, baseY, plKill, goldBars, true);

        // 2. Đồ Thần Linh + nội tại Cánh (+20%)
        tryDropGodItemStatic(this.zone, baseX, baseY, plKill, godItemPercent, true, false);

        // 3. Nguyên liệu Pha Lê cao cấp (10x Hematite / Dùi đục / Đá mài)
        if (hematitePercent > 0 && Util.isTrue(hematitePercent, 100)) {
            int xHema = clampZoneX(this.zone, baseX - 22);
            int yHema = this.zone.map.yPhysicInTop(xHema, baseY - 24);
            ItemMap hema = new ItemMap(this.zone, 1423, 10, xHema, yHema, ownerId);
            hema.options.add(new Item.ItemOption(167, 0));
            hema.options.add(new Item.ItemOption(168, 0));
            Service.gI().dropItemMap(this.zone, hema);
        }
        if (Util.isTrue(20, 100)) {
            int matId = Util.isTrue(50, 100) ? 1438 : 1439;
            int xMat = clampZoneX(this.zone, baseX + 22);
            int yMat = this.zone.map.yPhysicInTop(xMat, baseY - 24);
            ItemMap mat = new ItemMap(this.zone, matId, 10, xMat, yMat, ownerId);
            mat.options.add(new Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, mat);
        }

        // 4. 15% trọn bộ 7 viên Ngọc Rồng Bí Ngô [702..708]
        if (Util.isTrue(15, 100)) {
            for (int i = 0; i < 7; i++) {
                int xBall = clampZoneX(this.zone, baseX + (i - 3) * 14);
                int yBall = this.zone.map.yPhysicInTop(xBall, baseY - 24);
                ItemMap ball = new ItemMap(this.zone, 702 + i, 1, xBall, yBall, ownerId);
                ball.options.add(new Item.ItemOption(73, 0));
                Service.gI().dropItemMap(this.zone, ball);
            }
        }

        // 5. Đá nâng cấp / Đá may mắn Thiên Sứ [1074..1083]
        if (dropAngelStone25Pct && Util.isTrue(25, 100)) {
            int stoneId = Util.nextInt(1074, 1083);
            int xStone = clampZoneX(this.zone, baseX - 34);
            int yStone = this.zone.map.yPhysicInTop(xStone, baseY - 24);
            ItemMap stone = new ItemMap(this.zone, stoneId, 1, xStone, yStone, ownerId);
            stone.options.add(new Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, stone);
        }

        // 6. Công thức VIP Thiên Sứ [1084..1086] + Đá TS Cấp 5 [1078, 1083]
        if (dropVipRecipe50Pct && Util.isTrue(50, 100)) {
            int recipeId = Util.nextInt(1084, 1086);
            int xRec = clampZoneX(this.zone, baseX + 34);
            int yRec = this.zone.map.yPhysicInTop(xRec, baseY - 24);
            ItemMap recipe = new ItemMap(this.zone, recipeId, 1, xRec, yRec, ownerId);
            recipe.options.add(new Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, recipe);

            int lv5StoneId = Util.isTrue(50, 100) ? 1078 : 1083;
            int xStone5 = clampZoneX(this.zone, baseX - 38);
            int yStone5 = this.zone.map.yPhysicInTop(xStone5, baseY - 24);
            ItemMap stone5 = new ItemMap(this.zone, lv5StoneId, 1, xStone5, yStone5, ownerId);
            stone5.options.add(new Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, stone5);
        }

        // 7. Ấn Triệu Tập Zeno [2029] - Cầu nối duy nhất sang Phase 3
        if (dropZenoSeal2Pct && Util.isTrue(2, 100)) {
            int xSeal = clampZoneX(this.zone, baseX);
            int ySeal = this.zone.map.yPhysicInTop(xSeal, baseY - 24);
            ItemMap zenoSeal = new ItemMap(this.zone, 2029, 1, xSeal, ySeal, ownerId);
            zenoSeal.options.add(new Item.ItemOption(30, 0));
            Service.gI().dropItemMap(this.zone, zenoSeal);
            if (owner != null) {
                ServerNotify.gI().notify(owner.name + " vừa đoạt được ẤN TRIỆU TẬP ZENO từ tay Thần Hủy Diệt Champa!");
            }
        }
    }

    protected void tryDropPermanentVipDisguise(Player plKill, int disguiseTempId) {
        if (plKill == null || this.zone == null || this.zone.map == null || !Util.isTrue(1, 100)) {
            return;
        }
        try {
            Player owner = resolveRewardOwner(plKill);
            long ownerId = (owner != null) ? owner.id : plKill.id;
            int xDrop = clampZoneX(this.zone, this.location.x + 28);
            int yDrop = this.zone.map.yPhysicInTop(xDrop, this.location.y - 24);
            ItemMap ctMap = new ItemMap(this.zone, disguiseTempId, 1, xDrop, yDrop, ownerId);
            List<Item.ItemOption> shopOps = ItemService.gI().getListOptionItemShop((short) disguiseTempId);
            if (shopOps != null && !shopOps.isEmpty()) {
                ctMap.options.addAll(shopOps);
            } else {
                ctMap.options.add(new Item.ItemOption(50, 40));
                ctMap.options.add(new Item.ItemOption(77, 30));
                ctMap.options.add(new Item.ItemOption(103, 30));
                ctMap.options.add(new Item.ItemOption(30, 0));
            }
            Service.gI().dropItemMap(this.zone, ctMap);
            if (owner != null) {
                ServerNotify.gI().notify(owner.name + " cực kỳ may mắn rơi trực tiếp Cải Trang VIP Vĩnh Viễn từ " + this.name + "!");
            }
        } catch (Exception e) {
            Logger.logException(Universe6Boss.class, e);
        }
    }

    @Override
    public void autoLeaveMap() {
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            this.lastTimeJoinZone = System.currentTimeMillis();
        }
    }
}
