package nro.models.boss.Cell;


import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.boss.BossesData;
import java.util.Random;
import nro.models.consts.ConstTaskBadges;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.map.service.ChangeMapService;
import nro.models.task.BadgesTaskService;
import nro.models.utils.Util;

public class XENCON4 extends Boss {

    private long st;

    public XENCON4() throws Exception {
        super(BossID.XEN_CON_4, BossesData.XEN_CON_4);
    }

    @Override
    public void reward(Player plKill) {
        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill == null || this.zone == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : plKill.id;
        tryDropGodItemStatic(this.zone, this.location.x, this.location.y, plKill, 5, true, false);
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, Util.nextInt(20000, 30001),
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), ownerId));
        if (Util.isTrue(80, 100)) {
            int[] items = new int[]{18, 19, 20};
            int randomItem = items[new Random().nextInt(items.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, randomItem, 1,
                    this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), ownerId));
        }
    }
    @Override
    public void joinMap() {
        this.soulDroppedForCurrentLife = false;
        this.manhGiay726DroppedForCurrentLife = false;
        st = System.currentTimeMillis();
        this.zone = this.parentBoss.zone;
        ChangeMapService.gI().changeMap(this, this.zone,
                this.parentBoss.location.x + Util.nextInt(-100, 100), this.parentBoss.location.y);
        Service.gI().sendFlagBag(this);
        this.notifyJoinMap();
        this.changeStatus(BossStatus.CHAT_S);
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if ((boss.id == BossID.XEN_CON_2 || boss.id == BossID.XEN_CON_3 || boss.id == BossID.XEN_CON_1 || boss.id == BossID.XEN_CON_5 || boss.id == BossID.XEN_CON_6 || boss.id == BossID.XEN_CON_7) && !boss.isDie()) {
                return;
            }
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
//        BossManager.gI().removeBoss(this);
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }
}
