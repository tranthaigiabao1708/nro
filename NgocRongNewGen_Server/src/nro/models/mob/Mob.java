package nro.models.mob;

import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.map.service.ItemMapService;
import nro.models.consts.ConstMap;
import nro.models.consts.ConstMob;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstTask;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import java.util.List;
import nro.models.map.Zone;
import nro.models.player.Location;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.network.Message;
import java.io.IOException;
import nro.models.server.CustomConfig;
import nro.models.server.Maintenance;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import nro.models.consts.ConstTaskBadges;
import nro.models.services.AchievementService;
import nro.models.services_dungeon.TrainingService;
import nro.models.services.ChatGlobalService;
import nro.models.services.ItemService;
import nro.models.map.service.MapService;
import nro.models.skill.Skill;
import nro.models.task.BadgesTaskService;
import nro.models.utils.TimeUtil;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;
    private int mapId;

    public List<Player> temporaryEnemies = new ArrayList<>();
    public final Map<Long, Long> damageMap = new HashMap<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;
    public Player lastAttacker;

    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;

    public long lastTimeAttackPlayer;
    public long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp(this.point.getHpFull());
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
        if (this.tempId == 118) {
            this.type = 1; // loại mob thường
            this.status = 5; // trạng thái đứng rảnh
            this.timeAttack = 1500; // thời gian delay giữa các đòn đánh
        }
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        if (this.tempId == 118) {
            this.type = 1;
            this.status = 5;
            this.timeAttack = 1500;
        }
    }

    public void setTiemNang() {
        this.maxTiemNang = (long) this.point.getHpFull() * (long) (this.pTiemNang + Util.nextInt(-2, 2)) / 100L;
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    public synchronized void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (!this.isDie()) {
            if (damage >= this.point.hp) {
                damage = this.point.hp;
            }
            if (!dieWhenHpFull) {
                if (this.zone.map.mapId != 164) {
                    if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                        damage = this.point.hp - 1;
                    }
                    if ((this.tempId == ConstMob.MOC_NHAN || this.tempId == ConstMob.BU_NHIN_MA_QUAI) && damage > this.point.maxHp / 10) {
                        damage = this.point.maxHp / 10;
                    }
                }
            }
            if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
                boolean mob76Die = true;
                for (Mob mob : this.zone.mobs) {
                    if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                        mob76Die = false;
                        break;
                    }
                }
                if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.LIEN_HOAN, Skill.ANTOMIC, Skill.MASENKO, Skill.KAMEJOKO ->
                            damage = 1;
                    }
                }
            }
            if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0 && plAtt != null && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
                damage = (int) ((this.point.maxHp <= 20_000_000 ? this.point.maxHp * 1 : 2) * (10.0 / 100));
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2) && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }

            if (damage > 2_147_483_647) {
                damage = 2_147_483_647;
            }

            this.point.hp -= damage;
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, (int) damage);
                    TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                    AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                }
                if (this.id == 13) {
                    this.zone.isbulon1Alive = false;
                }
                if (this.id == 14) {
                    this.zone.isbulon2Alive = false;
                }
            } else {
                this.sendMobStillAliveAffterAttacked((int) damage, plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                Service.gI().addSMTN(plAtt, (byte) 2, getTiemNangForPlayer(plAtt, damage), true);
                TrainingService.gI().tangTnsmLuyenTap(plAtt, getTiemNangForPlayer(plAtt, damage));
                if (plAtt.isPl()) {
                    plAtt.total_damage_maydam += damage;
                }
                long realDamage = this.point.hp / 100 > 0 ? this.point.hp / 100 : 1;

                damage = this.point.hp / 100 > 0 ? this.point.hp / 100 : 1;
            }
        }
        if (MapService.gI().isMapCadic(this.zone.map.mapId) && plAtt != null) {
            boolean hasKilisBuff = plAtt.itemTime != null && plAtt.itemTime.isUseKilis;
            int successRate = hasKilisBuff ? 10 : 1;

            if (Util.isTrue(successRate, 333)) {
                if (plAtt != null) {
                    Item item = InventoryService.gI().findItemBag(plAtt, 1795);
                    if (item != null) {
                        boolean updated = false;
                        Item.ItemOption kilisOption = item.getOptionById(250);
                        if (kilisOption != null) {
                            kilisOption.param += 1;
                            updated = true;
                        } else {
                            item.itemOptions.add(new Item.ItemOption(250, 1));
                            updated = true;
                        }

                        if (updated) {
                            InventoryService.gI().sendItemBags(plAtt);
                            Service.gI().sendThongBao(plAtt, "Bạn vừa nhận được 1 chỉ số Kilis!");
                        }
                    }
                }
            }
        }
    }

    public long getTiemNangForPlayer(Player pl, long dame) {
        int levelPlayer = Service.gI().getCurrLevel(pl);
        int levelMob = this.level;
        int checkLevel = Math.abs(levelPlayer - this.level);
        long tiemNang = (long) (dame + (point.getHpFull() * 0.0005));
        switch (this.tempId) {
            case 0:
                tiemNang = 1;
                break;
        }
        if (checkLevel > 5 && levelPlayer > levelMob) {
            tiemNang = 1;
        } else {
            if (checkLevel < 0) {
                checkLevel = Math.abs(levelMob - levelPlayer);
            } else {
                tiemNang /= (int) (checkLevel * 0.5) + 1.25;
            }
        }
        if (tiemNang < 1) {
            tiemNang = 1;
        }
        if (pl.nPoint != null) {
            tiemNang = (int) pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }
        if (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124) {
            //tiemNang *= 2;
        }
        return tiemNang;
    }

    public void update() {
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }

        if (this.isDie() && !Maintenance.isRunning && !isBigBoss()) {
            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive && Util.canDoWithTime(lastTimeDie, 10000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    break;
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                    break;
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                    break;
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default:
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimeDie, 3000)) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        int hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
                    }
            }
        }

        effectSkill.update();
        attack();
    }

    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN || this.tempId == ConstMob.VUA_BACH_TUOC
                || this.tempId == ConstMob.ROBOT_BAO_VE || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA || this.tempId == ConstMob.GA_CHIN_CUA
                || this.tempId == ConstMob.NGUA_CHIN_LMAO || this.tempId == ConstMob.MAY_DO_SUC_MANH || this.tempId == ConstMob.PIANO);
    }

    public void attack() {
        Player player = getPlayerCanAttack();
        if (player == null) {
            return;
        }
        boolean canAttack = !isDie()
                && !effectSkill.isHaveEffectSkill()
                && tempId != ConstMob.MOC_NHAN
                && tempId != ConstMob.MAY_DO_SUC_MANH
                && tempId != ConstMob.BU_NHIN_MA_QUAI
                && tempId != ConstMob.CO_MAY_HUY_DIET
                && (!this.isBigBoss())
                && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId))
                && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack);

        if (canAttack) {
            this.mobAttackPlayer(player);
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            return plAttack;
        }
        int distance = 100;
        try {
            List<Player> players = this.zone.getNotBosses();
            for (Player pl : players) {
                if (!pl.isDie() && !pl.isBoss && !pl.isNewPet && (pl.satellite == null || !pl.satellite.isDefend) && (pl.effectSkin == null || !pl.effectSkin.isVoHinh) && (this.tempId > 18 || (this.tempId > 9 && this.type == 4)) || isBigBoss()) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (Player plAttt : playersMap) {
                    if (plAttt == null) {
                        continue;
                    }
                    if (plAttt.isDie() || plAttt.isBoss || (plAttt.satellite != null && plAttt.satellite.isDefend)
                            || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh) || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {
        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }

        int dameMob = this.point.getDameAttack();

        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }

        if (player.isPet) {
            Pet pet = (Pet) player;
            if (pet.master != null && pet.master.charms != null && pet.master.charms.tdDeTu > System.currentTimeMillis()) {
                dameMob /= 2;
            }
        }

        if (this.lvMob > 0 && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            dameMob = (int) (player.nPoint.hpMax * 0.1);
        }

        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }

        if (player.itemTime != null && player.itemTime.isUseCMS) {
            dameMob = (int) Math.round(dameMob * 0.1);
        }

        if (this.lvMob > 0 && player.charms != null && player.charms.tdOaiHung > System.currentTimeMillis()) {
            // giữ nguyên dameMob
        }

        int dame = player.injured(null, dameMob, false, true);
        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, int dame) {
        if (!player.isPet && !player.isBot && !player.isNewPet) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writer().writeInt(dame); //dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt(player.nPoint.hp);
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
    }

    public int lvMob() {
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap.lvMob > 0) {
                return 0;
            }
        }
        this.lvMob = this.tempId > 12 && this.tempId < 34 && !isBigBoss() ? Util.isTrue(0, 10000) ? 1 : 0 : 0;
        this.point.hp = this.lvMob > 0 ? this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000 : this.point.maxHp;
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(lvMob());
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoi_hp(int hp) {
        Message msg = null;
        try {
            this.point.sethp(this.point.gethp() + hp);
            int HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(HP);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, int dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);

        if (!player.isPet && !player.isBot && !player.isNewPet) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        } else if (player.isPet) {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId, true);
                }
            }
        } else if (player.isBot) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));
            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size());
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);
                msg.writer().writeShort(itemMap.itemTemplate.id);
                msg.writer().writeShort(itemMap.x);
                msg.writer().writeShort(itemMap.y);
                msg.writer().writeInt((int) itemMap.playerId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemReward;
    }

    private boolean isUsingCoBonLa(Player owner) {
        return owner != null && !owner.isPet && owner.itemTime != null && owner.itemTime.isUseCoBonLa;
    }

    private boolean isUsingLuckDragon(Player owner) {
        return owner != null && !owner.isPet && owner.itemTime != null && owner.itemTime.isUseLuckDragon;
    }

    public static boolean isItemActiveOnPlayer(Player wearer, Item item) {
        if (wearer == null || item == null || !item.isNotNullItem() || item.itemOptions == null) {
            return false;
        }
        if (item.template != null && item.template.gender != 3 && item.template.gender != wearer.gender) {
            return false;
        }
        boolean isFused = (wearer.fusion != null && wearer.fusion.typeFusion != ConstPlayer.NON_FUSION)
                || (wearer.isPet && ((Pet) wearer).master != null
                && ((Pet) wearer).master.fusion != null
                && ((Pet) wearer).master.fusion.typeFusion != ConstPlayer.NON_FUSION);
        for (Item.ItemOption op : item.itemOptions) {
            if (op.optionTemplate != null && op.optionTemplate.id == 38 && !isFused) {
                return false;
            }
        }
        return true;
    }

    private static Item getActiveOwnerMount(Player owner) {
        if (owner == null || owner.isPet || owner.inventory == null || owner.inventory.itemsBody == null
                || owner.inventory.itemsBody.size() <= 9) {
            return null;
        }
        Item mount = owner.inventory.itemsBody.get(9);
        if (!isItemActiveOnPlayer(owner, mount)) {
            return null;
        }
        return mount;
    }

    private static Item getActiveOwnerAura(Player owner) {
        if (owner == null || owner.isPet || owner.inventory == null || owner.inventory.itemsBody == null
                || owner.inventory.itemsBody.size() <= 10) {
            return null;
        }
        Item aura = owner.inventory.itemsBody.get(10);
        if (aura == null || !aura.isNotNullItem() || aura.template == null || aura.template.type != 35) {
            return null;
        }
        if (!isItemActiveOnPlayer(owner, aura)) {
            return null;
        }
        return aura;
    }

    public static double getMountDragonBallMult(Player owner) {
        Item mount = getActiveOwnerMount(owner);
        if (mount == null || mount.template == null) {
            return 1.0;
        }
        if (mount.template.id == 2005 || mount.isHaveOption(253)) {
            return 2.50;
        }
        return 1.0;
    }

    public static double getMountSubDragonBallMult(Player owner) {
        Item mount = getActiveOwnerMount(owner);
        if (mount == null || mount.template == null) {
            return 1.0;
        }
        if (mount.template.id == 2005 || mount.isHaveOption(253)) {
            return 1.50;
        }
        return 1.0;
    }

    public static double getMountGoldBarMult(Player owner) {
        Item mount = getActiveOwnerMount(owner);
        if (mount == null || mount.template == null) {
            return 1.0;
        }
        if (mount.template.id == 2006 || mount.isHaveOption(254)) {
            return 1.35;
        }
        if (mount.isHaveOption(232)) {
            return 1.15;
        }
        return 1.0;
    }

    public static double getMountUpgradeMatMult(Player owner) {
        Item mount = getActiveOwnerMount(owner);
        if (mount == null || mount.template == null) {
            return 1.0;
        }
        if (mount.template.id == 2005) {
            return 1.40;
        }
        if (mount.template.id == 2006 || mount.isHaveOption(235)) {
            return 1.30;
        }
        return 1.0;
    }

    /**
     * Tính % May mắn chung CHỈ trên nhân vật chính (owner).
     * Đệ tử (Pet) mặc đồ/phụ kiện May mắn hoặc Set Gohan đều KHÔNG được tính (bỏ phí 100%).
     */
    private double getGeneralLuckPercent(Player owner) {
        if (owner == null || owner.isPet) {
            return 0.0;
        }
        int armorOption236Param = 0;
        int directOption236Percent = 0;
        if (owner.inventory != null && owner.inventory.itemsBody != null) {
            for (Item item : owner.inventory.itemsBody) {
                if (isItemActiveOnPlayer(owner, item)) {
                    for (Item.ItemOption op : item.itemOptions) {
                        if (op.optionTemplate != null && op.optionTemplate.id == 236) {
                            if (item.template != null && item.template.type >= 5) {
                                directOption236Percent += op.param;
                            } else {
                                armorOption236Param += op.param;
                            }
                        }
                    }
                }
            }
        }
        if (armorOption236Param > 100) {
            armorOption236Param = 100;
        }

        double percentFromOption236 = Math.pow(armorOption236Param / 100.0, 1.5) * 20.0 + directOption236Percent;
        int luckDragon = isUsingLuckDragon(owner) ? 20 : 0; // Ước NR 1 Sao: +20% trong 60 phút
        int luckRX = (owner.itemTime != null && owner.itemTime.isUseLuckRX) ? 20 : 0; // Ước Rồng Xương: +20% May mắn
        int gohanLuck = (owner.setClothes != null && owner.setClothes.gohan == 5) ? 150 : 0; // 5 món Set Gohan trên Sư Phụ: +150%
        return percentFromOption236 + luckDragon + luckRX + gohanLuck;
    }

    private long scalePerMillion(double basePerMillion, double luckPercent) {
        long scaled = Math.round(basePerMillion * (1.0 + luckPercent / 100.0));
        if (scaled <= 0) {
            return 0;
        }
        return Math.min(1_000_000L, scaled);
    }

    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();
        if (player.isBoss) {
            return list;
        }

        if (this.tempId == 0) {
            return list;
        }
        Player owner = (player.isPet && ((Pet) player).master != null) ? ((Pet) player).master : player;
        boolean hasCoBonLa = isUsingCoBonLa(owner);
        double generalLuckNoClover = getGeneralLuckPercent(owner);
        double generalLuckWithClover = generalLuckNoClover + (hasCoBonLa ? 20.0 : 0.0);
        double dragonBallMult = getMountDragonBallMult(owner);
        double goldBarMult = getMountGoldBarMult(owner);
        double upgradeMatMult = getMountUpgradeMatMult(owner);
        int dropMult = Math.max(1, CustomConfig.DROP_RATE_MULT);

        int mapid = player.zone.map.mapId;
        //========================Capsul Kì Bí========================
        boolean hasMayDo = (owner.itemTime != null && owner.itemTime.isUseMayDo);
        if (hasMayDo
                && Util.isTrue(scalePerMillion(200_000.0, generalLuckWithClover), 1_000_000L)
                && this.tempId > 57 && this.tempId < 66) {
            list.add(new ItemMap(zone, 380, 1, x, yEnd, owner.id));
        }

        //========================TASK========================
        if (owner.isPl() && TaskService.gI().getIdTask(owner) == ConstTask.TASK_8_1) {
            if (owner.gender == 0 && this.tempId == 11 || owner.gender == 1 && this.tempId == 12 || owner.gender == 2 && this.tempId == 10) {
                list.add(new ItemMap(zone, 20, 1, x, yEnd, owner.id));
                TaskService.gI().checkDoneTaskFind7Stars(owner);
            }
        }

        //========================Map Bang Hội========================
        if (MapService.gI().isMapUpPorata(mapid)) {
            double base1 = (hasCoBonLa ? 150_000.0 : 100_000.0) * dropMult * upgradeMatMult;
            double base2 = (hasCoBonLa ? 80_000.0 : 50_000.0) * dropMult * upgradeMatMult;
            double base3 = (hasCoBonLa ? 4_000.0 : 2_000.0) * dropMult * upgradeMatMult;

            if (Util.isTrue(scalePerMillion(base1, generalLuckNoClover), 1_000_000L)) {
                ItemMap it = new ItemMap(zone, 933, 1, x, yEnd, owner.id);
                it.options.add(new Item.ItemOption(31, 1));
                list.add(it);
            } else if (owner.itemEvent != null && owner.itemEvent.canDropManhVo(150)
                    && Util.isTrue(scalePerMillion(base2, generalLuckNoClover), 1_000_000L)) {
                ItemMap it = new ItemMap(zone, 934, 1, x, yEnd, owner.id);
                it.options.add(new Item.ItemOption(31, 1));
                list.add(it);
            } else if (Util.isTrue(scalePerMillion(base3, generalLuckNoClover), 1_000_000L)) {
                ItemMap it = new ItemMap(zone, 935, 1, x, yEnd, owner.id);
                it.options.add(new Item.ItemOption(31, 1));
                list.add(it);
            }
        }

        //======================== Vàng Ngọc (Khóa chống lạm phát: KHÔNG nhân May mắn vào Vàng tươi) ========================
        int goldBonusPercent = player.nPoint != null ? player.nPoint.tlGold : 0;
        if (owner != player && owner.nPoint != null) {
            goldBonusPercent += owner.nPoint.tlGold;
        }
        if (MapService.gI().isMap3Planets(mapid)) {
            if (Util.isTrue(scalePerMillion(150_000.0 * dropMult, 0.0), 1_000_000L)) {
                int vang = Util.nextInt(500, 3000) * CustomConfig.DROP_GOLD_MULT;
                vang += (int) ((long) vang * goldBonusPercent / 100);
                if (vang < 1000) {
                    list.add(new ItemMap(zone, 76, vang, x, yEnd, owner.id));
                } else if (vang < 2000) {
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, owner.id));
                } else {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, owner.id));
                }
            }
        }
        if (MapService.gI().isMapNappa(mapid)) {
            if (Util.isTrue(scalePerMillion(50_000.0 * dropMult, 0.0), 1_000_000L)) {
                int vang = Util.nextInt(2000, 6000) * CustomConfig.DROP_GOLD_MULT;
                vang += (int) ((long) vang * goldBonusPercent / 100);
                if (vang < 3000) {
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, owner.id));
                } else if (vang < 5000) {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, owner.id));
                } else {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, owner.id));
                }
            }
        }
        if (MapService.gI().AllMap(mapid)) {
            if (owner.event != null && owner.event.luotNhanNgocMienPhi == 1) {
                ItemMap item1 = new ItemMap(zone, 77, 1, x, yEnd, owner.id);
                ItemMap item2 = new ItemMap(zone, 77, 1, x + 10, yEnd, owner.id);
                list.add(item1);
                list.add(item2);
                owner.event.luotNhanNgocMienPhi = 0;
            }
        }

        if (MapService.gI().isMapCold(mapid)) {
            if (Util.isTrue(scalePerMillion(300_000.0 * dropMult, 0.0), 1_000_000L)) {
                int vang = Util.nextInt(150000, 250000) * CustomConfig.DROP_GOLD_MULT;
                vang += (int) ((long) vang * goldBonusPercent / 100);
                if (vang < 10000) {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, owner.id));
                } else {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, owner.id));
                }
            }
        }
        if (MapService.gI().isMapTuongLai(mapid)) {
            if (Util.isTrue(scalePerMillion(150_000.0 * dropMult, 0.0), 1_000_000L)) {
                int vang = Util.nextInt(80000, 150000) * CustomConfig.DROP_GOLD_MULT;
                vang += (int) ((long) vang * goldBonusPercent / 100);
                if (vang < 6000) {
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, owner.id));
                } else if (vang < 10000) {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, owner.id));
                } else {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, owner.id));
                }
            }
        }
        if (MapService.gI().isMapPhoBan(mapid)) {
            if (Util.isTrue(scalePerMillion(50_000.0 * dropMult, 0.0), 1_000_000L)) {
                int vang = Util.nextInt(80000, 200000) * CustomConfig.DROP_GOLD_MULT;
                vang += (int) ((long) vang * goldBonusPercent / 100);
                if (hasCoBonLa) {
                    vang = (int) (vang * 1.20);
                }
                if (vang < 6000) {
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, owner.id));
                } else if (vang < 10000) {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, owner.id));
                } else {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, owner.id));
                }
            }
        }
        if (Util.isTrue(1, 1000000)) {
            int ngoc = Util.nextInt(1, 1);
            list.add(new ItemMap(zone, 77, ngoc, x, yEnd, owner.id));
        }

        //======================== Sét Kích Hoạt (SKH) ========================
        if (MapService.gI().isMapRiengTu(mapid) || MapService.gI().isMapUpSKH(mapid)) {
            // Tỉ lệ gốc + Khu Up SKH + Cỏ 4 Lá trên thang 10.000, nhân thêm May mắn chung (NR 1s + Set Gohan + Opt 236)
            int baseSkhPer10000 = CustomConfig.SKH_BASE_RATE;
            if (MapService.gI().isMapRiengTu(mapid)) {
                baseSkhPer10000 += CustomConfig.SKH_ZONE_BONUS;
            }
            if (hasCoBonLa) {
                baseSkhPer10000 += CustomConfig.SKH_CLOVER_BONUS;
            }

            long skhChancePerMillion = scalePerMillion(baseSkhPer10000 * 100.0 * dropMult, generalLuckNoClover);
            if (Util.isTrue(skhChancePerMillion, 1_000_000L)) {
                int dropGender = owner.gender;
                short itTemp = (short) ItemService.gI().randTempItemKichHoat(dropGender);
                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, owner.id);
                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                int[] opsrand = ItemService.gI().randOptionItemKichHoat(dropGender);
                for (int opId : opsrand) {
                    if (opId > 0) {
                        it.options.add(new Item.ItemOption(opId, 0));
                    }
                }
                list.add(it);
            }
        }

        //========================Đồ Sao Khác Vải Thô========================
        if (MapService.gI().isMapUpSKH(mapid)
                && Util.isTrue(scalePerMillion(125.0 * dropMult, generalLuckWithClover), 1_000_000L)) {
            if (Util.isTrue(1, 200)) {
                short itTemp = (short) ItemService.gI().randTempItemDoSao(owner.gender);
                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, owner.id);
                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                int randOption = Util.nextInt(100);
                boolean hasOption = false;
                if (randOption < 50) {
                    int randAddOption = Util.nextInt(100);
                    if (randAddOption < 50) {
                        it.options.add(new Item.ItemOption(107, 1));
                        hasOption = true;
                    } else if (randAddOption < 90) {
                        it.options.add(new Item.ItemOption(107, 2));
                        hasOption = true;
                    } else {
                        it.options.add(new Item.ItemOption(107, 3));
                        hasOption = true;
                    }
                }

                if (hasOption) {
                    list.add(it);
                }
            }
        }

        //========================Đồ Sao 3 Map Đầu========================
        if (MapService.gI().isMapUpSKH(mapid)
                && Util.isTrue(scalePerMillion(1_000.0 * dropMult, generalLuckWithClover), 1_000_000L)) {
            long ownerPower = owner.nPoint != null ? owner.nPoint.power : 0L;
            int powerReduction = (int) Math.min(ownerPower / 100000, 5) * 20;
            int finalRate = Math.max(50 - powerReduction, 0);

            if (finalRate > 0 && Util.nextInt(100) < finalRate) {
                short itTemp = (short) ItemService.gI().randDoSao(owner.gender);
                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, owner.id);
                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                int randOption = Util.nextInt(1, 100);
                boolean hasOption = false;
                if (randOption < 50) {
                    int randAddOption = Util.nextInt(100);
                    if (randAddOption < 60) {
                        it.options.add(new Item.ItemOption(107, 1));
                        hasOption = true;
                    } else if (randAddOption < 90) {
                        it.options.add(new Item.ItemOption(107, 2));
                        hasOption = true;
                    } else {
                        it.options.add(new Item.ItemOption(107, 3));
                        hasOption = true;
                    }
                }
                if (hasOption) {
                    list.add(it);
                }
            }
        }

        //========================Đồ Thần + Thức Ăn========================
        Item ownerAura = getActiveOwnerAura(owner);
        if (MapService.gI().isMapCold(mapid)) {
            // Cân chỉnh giữ độ hiếm Đồ Thần ở Cold (~14.5h không buff, ~3.5-4h kể cả Full Buff May mắn ~305%)
            // Nếu mặc Hào quang Vô Cực (#11 - thưởng đủ 5 Set SKH) có Opt 249: tăng nhẹ x1.25 tỉ lệ rơi Đồ Thần tại Cold
            double coldAuraMult = (ownerAura != null && (ownerAura.template.id == 2007 || ownerAura.isHaveOption(249))) ? 1.25 : 1.0;
            long coldCap = Math.round(90.0 * dropMult * coldAuraMult);
            long coldGodChance = Math.min(coldCap, scalePerMillion(22.0 * dropMult * coldAuraMult, generalLuckWithClover));
            if (Util.isTrue(coldGodChance, 1_000_000L)) {
                ItemMap it = ItemService.gI().randDoTL(this.zone, 1, x, yEnd, owner.id);
                list.add(it);
            }
        }

        boolean hasGodSet = (player.setClothes != null && player.setClothes.checkSetGod())
                || (owner.setClothes != null && owner.setClothes.checkSetGod());
        if (hasGodSet && Util.isTrue(scalePerMillion(9_009.0 * dropMult * upgradeMatMult, generalLuckWithClover), 1_000_000L)) {
            ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, owner.id);
            list.add(it);
        }

        if (MapService.gI().isMapCold(mapid)) {
            if (Util.isTrue(scalePerMillion(200_000.0 * dropMult * upgradeMatMult, generalLuckWithClover), 1_000_000L)) {
                int rand = Util.nextInt(0, 4);
                ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, owner.id);
                it.options.add(new Item.ItemOption(71 - rand, 0));
                list.add(it);
            }
        }

        if (MapService.gI().isMapDoanhTrai(mapid)
                && Util.isTrue(scalePerMillion(30_000.0 * dropMult * upgradeMatMult, generalLuckWithClover), 1_000_000L)) {
            ItemMap it = new ItemMap(zone, 225, 1, x, yEnd, owner.id);
            it.options.add(new Item.ItemOption(74, 0));
            list.add(it);
        }

        //======================== Ngọc Rồng Bí Ngô (702 - 708) - Bản Đồ Kho Báu ========================
        if (MapService.gI().isMapBanDoKhoBau(mapid)) {
            double eliteMult = (this.lvMob > 0) ? 2.0 : 1.0;
            double baseBiNgo = 80_000.0 * eliteMult * dropMult * getMountSubDragonBallMult(owner);
            if (Util.isTrue(scalePerMillion(baseBiNgo, generalLuckWithClover), 1_000_000L)) {
                int roll = Util.nextInt(100);
                int biNgoId;
                if (roll < 25) {
                    biNgoId = 708; // 25%: Bí ngô 7 sao
                } else if (roll < 47) {
                    biNgoId = 707; // 22%: Bí ngô 6 sao
                } else if (roll < 67) {
                    biNgoId = 706; // 20%: Bí ngô 5 sao
                } else if (roll < 92) {
                    biNgoId = 705; // 25%: Bí ngô 4 sao
                } else if (roll < 97) {
                    biNgoId = 704; // 5%: Bí ngô 3 sao
                } else if (roll < 99) {
                    biNgoId = 703; // 2%: Bí ngô 2 sao
                } else {
                    biNgoId = 702; // 1%: Bí ngô 1 sao
                }
                ItemMap biNgoIt = new ItemMap(zone, biNgoId, 1, x, yEnd, owner.id);
                biNgoIt.options.add(new Item.ItemOption(73, 0));
                list.add(biNgoIt);
            }
        }

        if (MapService.gI().isMap3Planets(mapid)
                && Util.isTrue(scalePerMillion(10_000.0 * dropMult * upgradeMatMult, generalLuckWithClover), 1_000_000L)) {
            ItemMap it = new ItemMap(zone, 225, 1, x, yEnd, owner.id);
            it.options.add(new Item.ItemOption(74, 0));
            list.add(it);
        }

        boolean hasDesSet = (player.setClothes != null && player.setClothes.checkSetDes())
                || (owner.setClothes != null && owner.setClothes.checkSetDes());
        if (MapService.gI().isMapNgucTu(mapid)) {
            if (hasDesSet) {
                double baseFoodDes = (owner.isActive() ? 103_600.0 : 100_000.0) * dropMult * upgradeMatMult;
                if (Util.isTrue(scalePerMillion(baseFoodDes, generalLuckWithClover), 1_000_000L)) {
                    list.add(new ItemMap(zone, Util.nextInt(1066, 1070), 1, x, yEnd, owner.id));
                }
            }
            double baseNgucTu = ((owner.isActive() && hasDesSet) ? 280_000.0 : 100_000.0) * dropMult * upgradeMatMult;
            if (Util.isTrue(scalePerMillion(baseNgucTu, generalLuckWithClover), 1_000_000L)) {
                list.add(new ItemMap(zone, 1229, 1, x, yEnd, owner.id));
            }
        }

        //======================== Trang Sách Cũ (1281) - Tương Lai / Cold / Ngục Tù ========================
        if ((MapService.gI().isMapTuongLai(mapid) || MapService.gI().isMapCold(mapid) || MapService.gI().isMapNgucTu(mapid))
                && Util.isTrue(scalePerMillion(50_000.0 * dropMult * upgradeMatMult, generalLuckWithClover), 1_000_000L)) {
            int bookPages = Util.nextInt(5, 20);
            ItemMap pageIt = new ItemMap(zone, 1281, bookPages, x, yEnd, owner.id);
            pageIt.options.add(new Item.ItemOption(73, 0));
            list.add(pageIt);
        }

        //======================== Ngọc Rồng 4 Sao - 7 Sao & Sao Pha Lê ========================
        if (this.zone.map.mapId >= 0) {
            // Tỉ lệ gốc 1.6% tổng cho NR 4s-7s (+0.4% nếu đã mở thành viên), nhân May mắn chung & x1.50 nếu cưỡi Sắc Điêu Vương
            double baseNro4To7 = (16_000.0 + (owner.isActive() ? 4_000.0 : 0.0)) * dropMult * getMountSubDragonBallMult(owner);
            if (Util.isTrue(scalePerMillion(baseNro4To7, generalLuckWithClover), 1_000_000L)) {
                int roll = Util.nextInt(100);
                int nroId;
                if (roll < 40) {
                    nroId = 20; // 40%: 7 sao
                } else if (roll < 70) {
                    nroId = 19; // 30%: 6 sao
                } else if (roll < 88) {
                    nroId = 18; // 18%: 5 sao
                } else {
                    nroId = 17; // 12%: 4 sao
                }
                ItemMap nroIt = new ItemMap(zone, nroId, 1, x, yEnd, owner.id);
                nroIt.options.add(new Item.ItemOption(73, 0));
                list.add(nroIt);
            }

            // Sao pha lê gốc 1% (1/100), nhân May mắn chung & Cỏ bốn lá
            if (Util.isTrue(scalePerMillion(10_000.0 * dropMult, generalLuckWithClover), 1_000_000L)) {
                list.add(new ItemMap(Util.spl(zone, Util.nextInt(441, 447), 1, x, yEnd, owner.id)));
            }
        }

        //======================== TÙY CHỈNH RƠI NGỌC & VẬT PHẨM TOÀN MAP (CustomConfig) ========================
        if (CustomConfig.DROP_GEM_PERCENT > 0
                && Util.isTrue(scalePerMillion(CustomConfig.DROP_GEM_PERCENT * 10_000.0, generalLuckWithClover), 1_000_000L)) {
            int gemItemId = Util.isTrue(50, 100) ? 77 : 861;
            list.add(new ItemMap(zone, gemItemId, CustomConfig.DROP_GEM_AMOUNT, x, yEnd, owner.id));
        }
        for (CustomConfig.GlobalDropEntry dropEntry : CustomConfig.CUSTOM_GLOBAL_DROPS) {
            long scaledChance;
            if (dropEntry.itemId == 457) {
                // Khóa chống lạm phát 2: May mắn chung chỉ tác dụng 50% hiệu lực lên Thỏi vàng khóa từ quái, sau đó nhân hệ số Thú cưỡi Thỏi vàng
                scaledChance = scalePerMillion(dropEntry.chancePer1000 * 1_000.0 * dropMult * goldBarMult, generalLuckWithClover * 0.5);
                // Hào quang Lam Cực / Tử Cực (Aura 29, 30) có Opt 232: cộng trực tiếp +0.1% (+1.000 / 1.000.000) tỉ lệ rơi Thỏi vàng khi farm
                if (ownerAura != null && ownerAura.isHaveOption(232)) {
                    scaledChance = Math.min(1_000_000L, scaledChance + 1_000L * dropMult);
                }
            } else if (dropEntry.itemId >= 14 && dropEntry.itemId <= 20) {
                scaledChance = scalePerMillion(dropEntry.chancePer1000 * 1_000.0 * dropMult * dragonBallMult, generalLuckWithClover);
            } else {
                scaledChance = scalePerMillion(dropEntry.chancePer1000 * 1_000.0 * dropMult, generalLuckWithClover);
            }
            if (Util.isTrue(scaledChance, 1_000_000L)) {
                ItemMap customIt = new ItemMap(zone, dropEntry.itemId, dropEntry.quantity, x, yEnd, owner.id);
                if (dropEntry.itemId == 457) {
                    // Thỏi vàng rơi từ farm quái là Thỏi vàng khóa (Không thể giao dịch)
                    customIt.options.add(new Item.ItemOption(30, 0));
                } else {
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) dropEntry.itemId);
                    if (ops != null && !ops.isEmpty()) {
                        customIt.options = ops;
                    } else {
                        customIt.options.add(new Item.ItemOption(73, 0));
                    }
                }
                list.add(customIt);
            }
        }

        return list;

    }

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
            case ConstMob.QUY_BAY_ME:
            case ConstMob.PHI_LONG_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(10, 10)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(int dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(this.point.gethp());
            msg.writer().writeInt(dameHit);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writer().writeInt(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendSieuQuai(int type) {
        Message msg;
        try {
            msg = new Message(-75);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(type);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDisable(boolean bool) {
        Message msg;
        try {
            msg = new Message(81);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDoneMove(boolean bool) {
        Message msg;
        try {
            msg = new Message(82);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendFire(boolean bool) {
        Message msg;
        try {
            msg = new Message(85);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendIce(boolean bool) {
        Message msg;
        try {
            msg = new Message(86);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendWind(boolean bool) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMobMaxHp(int maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt(maxHp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private void phanSatThuong(Player plTarget, long dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        if (plTarget == null || plTarget.inventory == null) {
            return;
        }
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            int damePST = (int) (long) (dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                int hpMob = this.point.hp;
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writer().writeInt(this.point.hp);
                msg.writer().writeInt(damePST);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (IOException e) {
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

}
