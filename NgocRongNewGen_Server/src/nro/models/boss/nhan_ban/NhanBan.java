package nro.models.boss.nhan_ban;


import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstRatio;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.matches.PKCommeson;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

public class NhanBan extends Boss {

    private Player playerAtt;
    private long timeJoinMap;

    public NhanBan(Player player, BossData bossData) throws Exception {
        super(Util.createIdBossClone((int) player.id), bossData);
        this.playerAtt = player;
        this.isCopy = true;
    }

    @Override
    public void reward(Player plKill) {
        if (this.zone == null || this.zone.map == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : (plKill != null ? plKill.id : -1L);
        int xBinh = Math.max(24, Math.min(this.zone.map.mapWidth - 24, this.location.x));
        int yBinh = this.zone.map.yPhysicInTop(xBinh, this.location.y - 24);
        ItemMap it = new ItemMap(this.zone, 638, 1, xBinh, yBinh, ownerId);
        it.options.clear();
        it.options.add(new Item.ItemOption(93, 30));
        it.options.add(new Item.ItemOption(30, 0));
        Service.gI().dropItemMap(this.zone, it);

        int[] superBuffIds = {1152, 1151, 1150}; // Siêu bổ huyết, Siêu bổ khí, Siêu cuồng nộ
        int offset = -18;
        for (int buffId : superBuffIds) {
            if (Util.isTrue(33, 100)) {
                int xDrop = Math.max(24, Math.min(this.zone.map.mapWidth - 24, this.location.x + offset));
                int yDrop = this.zone.map.yPhysicInTop(xDrop, this.location.y - 24);
                ItemMap buffItem = new ItemMap(this.zone, buffId, 1, xDrop, yDrop, ownerId);
                java.util.List<Item.ItemOption> shopOpts = nro.models.services.ItemService.gI().getListOptionItemShop((short) buffId);
                if (shopOpts != null && !shopOpts.isEmpty()) {
                    buffItem.options.addAll(shopOpts);
                } else {
                    buffItem.options.add(new Item.ItemOption(73, 0));
                }
                Service.gI().dropItemMap(this.zone, buffItem);
                offset += 18;
            }
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            new PKCommeson(playerAtt, this);
        }
        this.attack();
    }

    @Override
    public void joinMap() {
        this.zone = this.playerAtt.zone;
        ChangeMapService.gI().changeMap(this, this.zone,
                this.playerAtt.location.x + Util.nextInt(-200, 200), this.playerAtt.location.y);
        this.changeStatus(BossStatus.CHAT_S);
    }

    private void goToXY(int x, int y, boolean isTeleport) {
        if (!isTeleport) {
            byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
            byte move = (byte) Util.nextInt(50, 100);
            PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
        } else {
            ChangeMapService.gI().changeMapYardrat(this, this.zone, x, y);
        }
    }

    private void goToPlayer(Player pl, boolean isTeleport) {
        goToXY(pl.location.x, pl.location.y, isTeleport);
    }

    @Override
    public void attack() {
        try {
            if (playerAtt.typePk == ConstPlayer.NON_PK) {
                PlayerService.gI().changeAndSendTypePK(playerAtt, ConstPlayer.PK_PVP);
            }
            if (this.typePk == ConstPlayer.NON_PK) {
                PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_PVP);
            }
            if (playerAtt.location != null && playerAtt != null && playerAtt.zone != null && this.zone != null && this.zone.equals(playerAtt.zone)) {
                if (this.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, playerAtt) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(15, ConstRatio.PER100) && SkillUtil.isUseSkillChuong(this)) {
                        goToXY(playerAtt.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 80)), Util.nextInt(10) % 2 == 0 ? playerAtt.location.y : playerAtt.location.y - Util.nextInt(0, 50), false);
                    }
                    SkillService.gI().useSkill(this, playerAtt, null, -1, null);
                    checkPlayerDie(playerAtt);
                } else {
                    goToPlayer(playerAtt, false);
                }
            }
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
    }

    @Override
    public void die(Player plKill) {
        if (plKill != null) {
            reward(plKill);
            ServerNotify.gI().notify(plKill.name + " đã đánh bại bản sao Commeson, mọi người đều ngưỡng mộ");
        }
        this.changeStatus(BossStatus.DIE);
    }

    @Override
    public void autoLeaveMap() {
        if (playerAtt == null || playerAtt.effectSkill == null) {
            this.leaveMap();
            return;
        }
        if (!playerAtt.effectSkill.isPKCommeson) {
            Service.gI().sendThongBao(playerAtt, "Bạn đã thất bại, hãy gặp Potage để thử sức lại!");
            this.leaveMap();
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
        BossManager.gI().removeBoss(this);
        this.dispose();
    }
}
