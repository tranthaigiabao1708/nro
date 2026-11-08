package nro.models.boss.fide;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.services.Service;
import nro.services.TaskService;
import nro.utils.Util;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class FideDaiCa1 extends FutureBoss {

    public FideDaiCa1() {
        super(BossFactory.FIDE_DAI_CA_1, BossData.FIDE_DAI_CA_1);
    }

    @Override
    protected boolean useSpecialSkill() {
        return false;
    }

    @Override
    public void rewards(Player pl) {
        /// Rơi vật phẩm
        if (Util.isTrue(1, 100)) {
            int[] vatpham = { 18, 19, 20 };
            int vatphamChon = vatpham[Util.nextInt(0, vatpham.length - 1)];
            int randomvitrix = Util.nextInt(5, 8);
            int xPosition = pl.location.x + randomvitrix;
            int yPosition = this.zone.map.yPhysicInTop(pl.location.x, pl.location.y - 24);
            ItemMap itemMap = new ItemMap(this.zone, vatphamChon, 1, xPosition, yPosition, -1);
            Service.getInstance().dropItemMap(this.zone, itemMap);
            /// Roi vật phẩm noel
        } else if (Util.isTrue(100, 100)) {
            /// Nv
            TaskService.gI().checkDoneTaskKillBoss(pl, this);
        }
    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[] { "Xem bản lĩnh của ngươi như nào đã", "Các ngươi tới số mới gặp phải ta" };
        this.textTalkAfter = new String[] { "Ác quỷ biến hình, hêy aaa......." };
    }

    @Override
    public void leaveMap() {
        Boss fd2 = BossFactory.createBoss(BossFactory.FIDE_DAI_CA_2);
        fd2.zone = this.zone;
        fd2.location.x = this.location.x;
        fd2.location.y = this.location.y;
        super.leaveMap();
        this.setJustRestToFuture();
    }
}
