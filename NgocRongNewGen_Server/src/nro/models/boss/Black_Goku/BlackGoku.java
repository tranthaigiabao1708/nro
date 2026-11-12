package nro.models.boss.Black_Goku;

import nro.models.boss.Boss;
import nro.models.boss.BossesData;
import nro.models.boss.BossID;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import java.util.List;
import nro.models.consts.ConstTaskBadges;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;
import nro.models.services.SkillService;
import nro.models.services.TaskService;
import nro.models.task.BadgesTaskService;

public class BlackGoku extends Boss {

    private long st;
    private int timeLeaveMap;

    public BlackGoku() throws Exception {
        super(BossID.BLACK_GOKU, false, true, BossesData.BLACK_GOKU, BossesData.SUPER_BLACK_GOKU);
    }

    @Override
    public void reward(Player plKill) {
        rewardHighTier20TVWithMaterials(this.zone, this.location.x, this.location.y, plKill, 10, true, true);
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (damage <= 0) {
                return 0;
            }
            int neDon = this.nPoint.tlNeDon;
            if (plAtt != null && plAtt.nPoint != null) {
                neDon = Math.max(0, neDon - plAtt.nPoint.tlchinhxac * 10);
            }
            if (!piercing && Util.isTrue(neDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
            }
            if (this.currentLevel != 0) {
                damage = Math.max(1L, damage / 2L);
            }
            if (!piercing) {
                long reduced = damage > 100_000L ? (damage - Util.nextInt(100_000)) : damage;
                damage = this.nPoint.subDameInjureWithDeff(Math.max(1L, reduced), plAtt);
            }
            if (!piercing && effectSkill.isShielding) {
                if (damage > Math.max(1, nPoint.hpMax / 20)) {
                    EffectSkillService.gI().breakShield(this);
                } else {
                    damage = Math.max(1L, damage / 4L);
                }
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return (int) Math.min(2_147_483_647L, Math.max(0L, damage));
        } else {
            return 0;
        }
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, timeLeaveMap)) {
            if (Util.isTrue(1, 2)) {
                this.leaveMap();
            } else {
                this.leaveMapNew();
            }
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
            timeLeaveMap = Util.nextInt(300000, 900000);
        }
    }

    @Override
    public void joinMap() {
        this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1, 100);
        super.joinMap();
        st = System.currentTimeMillis();
        timeLeaveMap = Util.nextInt(600000, 900000);
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                int dis = Util.getDistance(this, pl);
                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int move = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int move = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
