package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class ZenoSama extends Npc {

    private static final int MENU_FAST_TRAVEL_VT13 = 20315;

    public ZenoSama(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "|8|TOÀN VƯƠNG ZENO-SAMA (MAP HOME VŨ TRỤ 13)\n"
                + "|2|Chào bạn mới nha~ Đây là Thánh Điện Toàn Vương không có quái vật đâu, cứ nghỉ ngơi hồi sức thoải mái nhé!\n"
                + "|1|- Cổng bên TRÁI (Map -1): Đại Hội Võ Thuật Vũ Trụ 13 (Khiêu chiến BXH & Giải đấu 20h00 hằng ngày).\n"
                + "- Đại Thiên Sứ (Đại Thần Quan): Tu luyện Vô Cực & Đổi Set Thần Vực Vô Cực.\n"
                + "- Cổng bên PHẢI: Khe Nứt Nguyền Rủa -> Điện Thờ Thần Hủy Diệt VT13 ("
                + nro.models.boss.vu_tru_13.ThanHuyDietVt13.getScheduleStatusText() + ")",
                "Hồi phục\n100% HP/KI",
                "Mở Rương Đồ\nTại Nhà",
                "Dịch chuyển\nVũ Trụ 13",
                "Về Hành Tinh\nThường (Nhà)",
                "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> {
                    player.nPoint.setFullHpMp();
                    Service.gI().point(player);
                    Service.gI().sendThongBao(player, "Zeno-Sama đã ban phước hồi phục 100% HP & KI cho bạn!");
                }
                case 1 -> InventoryService.gI().openBox(player);
                case 2 -> this.createOtherMenu(player, MENU_FAST_TRAVEL_VT13,
                        "|8|CHỌN KHU VỰC TRONG VŨ TRỤ 13\n"
                        + "|2|Bạn muốn bay thẳng tới khu vực nào?",
                        "Map -1: Đại Hội\nVõ Thuật VT13",
                        "Map 1: Khe Nứt\nNguyền Rủa",
                        "Map 2: Vực Thẳm\nNguyền Rủa",
                        "Map 3: Ngục Tù\nPhong Ấn Goku",
                        "Map 4: Điện Thờ\nThần Hủy Diệt",
                        "Ở lại đây");
                case 3 -> ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
            }
        } else if (player.idMark.getIndexMenu() == MENU_FAST_TRAVEL_VT13) {
            switch (select) {
                case 0 -> ChangeMapService.gI().changeMapNonSpaceship(player, VuTru13Service.MAP_DAI_HOI_VT13, 840, 456);
                case 1 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_KHE_NUT_VT13, -1, 120);
                case 2 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_VUC_THAM_VT13, -1, 120);
                case 3 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_NGUC_TU_GOKU_VT13, -1, 160);
                case 4 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_DIEN_THO_HUY_DIET_VT13, -1, 120);
            }
        }
    }
}
