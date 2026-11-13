package nro.models.mob_bigboss;

import nro.models.item.Item.ItemOption;
import nro.models.map.ItemMap;
import nro.models.mob.BigBoss;
import nro.models.mob.Mob;
import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class Hirudegarn extends BigBoss {

    private int errors;
    private Player lastAttacker;

    public Hirudegarn(Mob mob) {
        super(mob);
    }

    @Override
    public synchronized void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (plAtt != null && !plAtt.isBot) {
            this.lastAttacker = plAtt;
        }
        damage = this.point.hp / 300 > 0 ? this.point.hp / 300 : 1;
        super.injured(plAtt, damage, false);
    }

    @Override
    public void update() {
        super.update();
        if (isDie() && (System.currentTimeMillis() - lastTimeDie) > 600000 && lvMob == 3) {
            lvMob = 0;
            action = 0;
            this.location.x = Util.nextInt(100, 900);
            this.location.y = 360;
            this.point.hp = this.point.getHpFull();
            Service.gI().sendBigBoss2(this.zone, action, this);
            Message msg = null;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                msg.writer().writeLong(this.point.gethp());
                msg.writer().writeLong(1L);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            } catch (Exception e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        } else if (isDie() && (System.currentTimeMillis() - lastTimeDie) > 5000 && lvMob <= 2) {
            switch (lvMob) {
                case 0 -> {
                    lvMob = 1;
                    action = 6;
                    this.point.hp = this.point.getHpFull();
                }
                case 1 -> {
                    lvMob = 2;
                    action = 5;
                    this.point.hp = this.point.getHpFull();
                }
                case 2 -> {
                    lvMob = 3;
                    action = 9;
                }
                default -> {
                }
            }

            Player owner = nro.models.boss.Boss.resolveRewardOwner(this.lastAttacker);
            long ownerId = (owner != null) ? owner.id : -1L;
            nro.models.boss.Boss.dropGoldBarsStatic(this.zone, this.location.x, this.location.y, this.lastAttacker, 20, true);
            nro.models.boss.Boss.dropBossSoulStatic(this.zone, this.location.x, this.location.y, this.lastAttacker, nro.models.boss.Boss.ITEM_HON_BOSS_LUC);
            if (Util.isTrue(20, 100)) {
                int xBinh = Math.max(24, Math.min(this.zone.map.mapWidth - 24, this.location.x));
                ItemMap it = new ItemMap(this.zone, 1795, 1, xBinh,
                        this.zone.map.yPhysicInTop(xBinh, this.location.y - 24), ownerId);
                it.options.add(new ItemOption(250, 0));
                Service.gI().dropItemMap(this.zone, it);
            }
            nro.models.boss.Boss.tryDropGodItemStatic(this.zone, this.location.x, this.location.y, this.lastAttacker, 2, true, false);
            Service.gI().sendBigBoss2(this.zone, action, this);
            if (lvMob <= 2) {
                Message msg = null;
                try {
                    msg = new Message(-9);
                    msg.writer().writeByte(this.id);
                    msg.writer().writeLong(this.point.gethp());
                    msg.writer().writeLong(1L);
                    Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                } catch (Exception e) {
                } finally {
                    if (msg != null) {
                        msg.cleanup();
                    }
                }
            } else {
                this.location.x = -1000;
                this.location.y = -1000;
            }
        }
        if (isDie() && lvMob == 3 && (System.currentTimeMillis() - lastTimeDie) > 15000) {
            lvMob = 0;
            action = 0;
            this.location.x = Util.nextInt(100, 900);
            this.location.y = 360;
            this.point.hp = this.point.getHpFull();
            Service.gI().sendBigBoss2(this.zone, action, this);
            Message msg = null;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                msg.writer().writeLong(this.point.gethp());
                msg.writer().writeLong(1L);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            } catch (Exception e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    @Override
    public void attack() {
        if (!isDie() && !effectSkill.isHaveEffectSkill() && Util.canDoWithTime(lastBigBossAttackTime, 3000)) {
            Message msg = null;
            try {
                int[] idAction = new int[]{1, 2, 3, 7};
                if (this.lvMob >= 2) {
                    idAction = new int[]{1, 2};
                }
                action = action == 7 ? 0 : idAction[Util.nextInt(0, idAction.length - 1)];
                if (this.zone.getPlayers().isEmpty()) {
                    return;
                }
                int index = Util.nextInt(0, this.zone.getPlayers().size() - 1);
                Player player = this.zone.getPlayers().get(index);
                if (player == null || player.isDie()) {
                    return;
                }
                if (action == 1) {
                    this.location.x = (short) player.location.x;
                    Service.gI().sendBigBoss2(this.zone, 8, this);
                }
                msg = new Message(101);
                msg.writer().writeByte(action);
                if (action >= 0 && action <= 4) {
                    switch (action) {
                        case 1:
                            msg.writer().writeByte(1);
                            long dame = player.injured(null, this.point.getDameAttack(), false, true);
                            msg.writer().writeInt((int) player.id);
                            msg.writer().writeLong(dame);
                            break;
                        case 3:
                            this.location.x = (short) player.location.x;
                            msg.writer().writeShort(this.location.x);
                            msg.writer().writeShort(this.location.y);
                            break;
                        default:
                            msg.writer().writeByte(this.zone.getPlayers().size());
                            for (int i = 0; i < this.zone.getPlayers().size(); i++) {
                                Player pl = this.zone.getPlayers().get(i);
                                dame = pl.injured(null, this.point.getDameAttack(), false, true);
                                msg.writer().writeInt((int) pl.id);
                                msg.writer().writeLong(dame);
                            }
                            break;
                    }
                } else {
                    if (action == 6 || action == 8) {
                        this.location.x = (short) player.location.x;
                        msg.writer().writeShort(this.location.x);
                        msg.writer().writeShort(this.location.y);
                    }
                }
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                lastBigBossAttackTime = System.currentTimeMillis();
            } catch (Exception e) {
                if (errors < 5) {
                    errors++;
                    e.printStackTrace();
                }
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

}
