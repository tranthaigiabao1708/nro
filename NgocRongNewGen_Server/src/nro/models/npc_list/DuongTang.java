package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;

public class DuongTang extends Npc {

    public DuongTang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 0 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "A mi phò phò, thí chủ hãy giúp giải cứu đồ đệ của bần tăng đang bị phong ấn tại Ngũ Hành Sơn.",
                            "Đồng ý", "Từ chối");
                case 123 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "A mi phò phò, thí chủ có muốn quay về Làng Aru không?",
                            "Về\nLàng Aru", "Từ chối");
                case 122 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "A mi phò phò, đa tạ thí chủ đã đến Ngũ Hành Sơn tu luyện!",
                            "Về\nLàng Aru", "Đóng");
                default ->
                    super.openBaseMenu(player);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player) && player.idMark.isBaseMenu()) {
            switch (this.mapId) {
                case 0 -> {
                    if (select == 0) {
                        ChangeMapService.gI().changeMapInYard(player, 123, -1, 180);
                    }
                }
                case 123, 122 -> {
                    if (select == 0) {
                        ChangeMapService.gI().changeMapInYard(player, 0, -1, 460);
                    }
                }
            }
        }
    }
}

