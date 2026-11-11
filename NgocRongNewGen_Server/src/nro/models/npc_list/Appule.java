package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.services.TaskService;
import nro.models.shop.ShopService;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Appule extends Npc {

    public Appule(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (player.gender != 2) {
                    NpcService.gI().createTutorial(player, tempId, this.avartar, "Về hành tinh hạ đẳng của ngươi mà mua đồ cùi nhé. Tại đây ta chỉ bán đồ cho người Xayda thôi");
                } else {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi cần trang bị gì cứ đến chỗ ta nhé", "Cửa\nhàng", "Đổi ngọc xanh\n1 TV = 1k ngọc");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {
                        //Shop
                        if (player.gender == ConstPlayer.XAYDA) {
                            ShopService.gI().opendShop(player, "APPULE", true);
                        } else {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Về hành tinh hạ đẳng của ngươi mà mua đồ cùi nhé. Tại đây ta chỉ bán đồ cho người Xayda thôi", "Đóng");
                        }
                    }
                    case 1 -> {
                        nro.models.services_func.Input.gI().createFormDoiThoiVangRaNgoc(player);
                    }
                }
            }
        }
    }
}
