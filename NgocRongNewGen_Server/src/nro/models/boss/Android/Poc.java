package nro.models.boss.Android;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.utils.Util;

public class Poc extends Boss {

    public Poc() throws Exception {
        super(BossID.POC, BossesData.POC);
    }

    @Override
    public void reward(Player plKill) {
        rewardGroupBasic5TV(plKill);
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

    @Override
    public void joinMap() {
        super.joinMap(); //To change body of generated methods, choose Tools | Templates.
        st = System.currentTimeMillis();
    }
    private long st;

//    @Override
//    public void wakeupAnotherBossWhenDisappear() {
//        if (this.parentBoss != null && !this.parentBoss.isDie()) {
//            this.parentBoss.changeToTypePK();
//        }
//    }
    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel]) {
            if (boss.id == BossID.PIC && !boss.isDie()) {
                boss.changeStatus(BossStatus.ACTIVE);
                break;
            }
        }
    }

}
