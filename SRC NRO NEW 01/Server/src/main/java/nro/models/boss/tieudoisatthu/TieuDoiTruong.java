package nro.models.boss.tieudoisatthu;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossFactory;
import nro.models.boss.FutureBoss;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.server.ServerNotify;
import nro.services.Service;
import nro.services.TaskService;
import nro.services.func.ChangeMapService;
import nro.utils.Util;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class TieuDoiTruong extends FutureBoss {

    public TieuDoiTruong() {
        super(BossFactory.TIEU_DOI_TRUONG, BossData.TIEU_DOI_TRUONG);
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
        }
        TaskService.gI().checkDoneTaskKillBoss(pl, this);
    }

    @Override
    public void idle() {

    }

    @Override
    public void checkPlayerDie(Player pl) {

    }

    @Override
    public void initTalk() {
        this.textTalkMidle = new String[] { "Oải rồi hả?", "Ê cố lên nhóc",
                "Chán", "Đại ca Fide có nhầm không nhỉ" };

    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        this.changeToIdle();
    }

    @Override
    public void joinMap() {
        if (this.zone == null) {
            this.zone = getMapCanJoin(mapJoin[Util.nextInt(0, mapJoin.length - 1)]);
        }
        if (this.zone != null) {
            BossFactory.createBoss(BossFactory.SO4).zone = this.zone;
            BossFactory.createBoss(BossFactory.SO3).zone = this.zone;
            BossFactory.createBoss(BossFactory.SO2).zone = this.zone;
            BossFactory.createBoss(BossFactory.SO1).zone = this.zone;
            ChangeMapService.gI().changeMapBySpaceShip(this, this.zone, ChangeMapService.TENNIS_SPACE_SHIP);
            ServerNotify.gI().notify("Boss " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }

}
