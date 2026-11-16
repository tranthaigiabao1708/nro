package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class GokuPhongAn extends Npc {

    public GokuPhongAn(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
        String sealState = player.vuTru13Stage >= 2
                ? "|7|[CẢNH BÁO] Phong Ấn Thần Hủy Diệt Vũ Trụ 13 đã bị Black Fide phá vỡ! Hàng loạt cổ thần sắp thức giấc!"
                : "|1|Xiềng xích nguyền rủa của Vũ Trụ 13 đang khóa chặt thần lực của ta...\n"
                + "|2|Đại Đế Fide đang hấp thụ tà khí cổ thần ở Điện Thờ phía đông để biến thành Black Fide và mở phong ấn Thần Hủy Diệt!";

        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "|8|SON GOKU (ĐANG BỊ PHONG ẤN TẠI VŨ TRỤ 13)\n"
                + sealState,
                "Đến Điện Thờ\nThần Hủy Diệt",
                "Luyện tập\nVực Thẳm VT13",
                "Tìm hiểu\nVũ Trụ 13",
                player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang\n(Hiện Chibi)",
                "Về Thánh Điện\nZeno (Home)");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_DIEN_THO_HUY_DIET_VT13, -1, 120);
                case 1 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_VUC_THAM_VT13, -1, 200);
                case 2 -> this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "|8|BÍ ẨN VŨ TRỤ SỐ 13 - THẾ GIỚI BỊ NGUYỀN RỦA\n"
                        + "|2|- Vũ Trụ 13 từng bị xóa sổ nửa chừng rồi phong ấn vĩnh viễn cùng những vị thần hung bạo nhất.\n"
                        + "- Khi bước vào đây, ta đã bị 4 Cột Đá Hắc Diện Thạch và xiềng xích cổ thần trấn áp toàn bộ Bản Năng Vô Cực.\n"
                        + "|7|- Tại Điện Thờ Thần Hủy Diệt VT13, Đại Đế Fide đã luyện thành hình thái BLACK FIDE nhằm phá ấn giải phóng Thần Hủy Diệt Vũ Trụ 13!",
                        "Đã hiểu");
                case 3 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                case 4 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_THANH_DIEN_ZENO_VT13, -1, 300);
            }
        }
    }
}
