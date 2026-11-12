package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.shop.ShopService;

public class Vados extends Npc {

    public Vados(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        String say = "|7|THIÊN SỨ VADOS - DẪN ĐƯỜNG VŨ TRỤ 6"
                + "\n|2|Chào mừng chiến binh Vũ Trụ 7 đặt chân tới Tinh Vân Champa!"
                + "\n|0|- Phía trước là Đế Chế Frost (độc khí rút máu) và Lò Dung Nham Metalman (rút máu & KI)."
                + "\n|0|- Hãy chuẩn bị Thuốc Giải & Cách Nhiệt VT6 trước khi tiến sâu."
                + "\n|5|- Bí mật nhỏ: Auta Magetta rất nhạy cảm, bị chê là đồ sắt vụn sẽ tụt mạnh giáp đó!";
        createOtherMenu(player, ConstNpc.BASE_MENU, say,
                "Tiếp tế\nGiải Đấu",
                "Tình trạng\nBoss VT6",
                "Đến Võ Đài\nSiêu Cấp",
                "Về Đảo Kamê");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || !player.idMark.isBaseMenu()) {
            return;
        }
        switch (select) {
            case 0 -> ShopService.gI().opendShop(player, "TRONG_TAI_VT6", false);
            case 1 -> createOtherMenu(player, ConstNpc.IGNORE_MENU, TrongTai.buildUniverse6BossStatusText(), "Đóng");
            case 2 -> {
                if (InventoryService.gI().getTotalQuantityBag(player, 2022) > 0) {
                    ChangeMapService.gI().changeMapBySpaceShip(player, 145, -1, 792);
                } else {
                    Service.gI().sendThongBao(player, "Cần sở hữu Lệnh Bài Võ Đài trong hành trang để bay thẳng tới Võ Đài Siêu Cấp!");
                }
            }
            case 3 -> ChangeMapService.gI().changeMapNonSpaceship(player, 5, 220, 408);
        }
    }
}
