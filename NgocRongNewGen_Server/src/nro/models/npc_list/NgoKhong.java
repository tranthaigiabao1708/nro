package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;

public class NgoKhong extends Npc {

    public NgoKhong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Chu mi nga! Đợi sư phụ niệm chú giải phong ấn cho Lão Tôn nhé!",
                    "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
        }
    }
}

