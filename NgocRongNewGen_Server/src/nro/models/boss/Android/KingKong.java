package nro.models.boss.Android;
import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.utils.Util;

public class KingKong extends Boss {

    public KingKong() throws Exception {
        super(BossID.KING_KONG, BossesData.KING_KONG);
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

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }
}
