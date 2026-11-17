package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class IwanVt13 extends Npc {

    public IwanVt13(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
        if ((player.vuTru13Stage & VuTru13Service.STAGE_FLAG_KHE_NUT_INTRO) == 0) {
            VuTru13Service.gI().triggerKheNutGodsCutscene(player, true);
            return;
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "|8|THẦN HỦY DIỆT IWAN - VŨ TRỤ 1\n"
                + "|2|Ngọn Tháp Ai Cập Cổ Đại này từng là phong ấn tối cao giam giữ Thần Hủy Diệt của Vũ Trụ 13 từ thuở sơ khai!\n"
                + "|1|- Sau khi hấp thụ 8 Hồn Năng Lượng Tím của Heles, Mosco, Quitela, Arak, Liquiir, Sidra, Rumsshi và Geene, sức mạnh của hắn đã vượt ngưỡng thần linh thông thường.\n"
                + "- Hãy nâng cấp cảnh giới Vô Cực tại Đại Thần Quan trước khi đối đầu trực diện với hắn!",
                "Thông tin kỹ năng\nBoss Thần Hủy Diệt",
                player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang\n(Hiện Chibi)",
                "Xem lại Cảnh\nTháp Ai Cập Vỡ",
                "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "|8|HỒ SƠ CỔ THẦN HỦY DIỆT VŨ TRỤ 13\n"
                        + "|7|1. Ngoại hình & Thần Khí:\n"
                        + "|1|- Hóa thân Cổ Thần Anubis Ai Cập Hắc Ám, xung quanh luôn có 8 Hồn Tím Thần Hủy Diệt xoay chuyển hộ thể.\n"
                        + "|7|2. Ba Tuyệt Kỹ Hủy Diệt:\n"
                        + "|1|- Quả Cầu Hakai Cổ Thần: Cầu hủy diệt quấn vòng cổ ngữ vàng nghiền nát Khiên Năng Lượng.\n"
                        + "- Trận Pháp Phán Quyết Ai Cập: Khóa mục tiêu trong trận đồ cổ đại và gây choáng.\n"
                        + "- Bão Hồn 8 Thần Hủy Diệt: Giải phóng chấn động Flash tím-trắng càn quét toàn khu vực!",
                        "Đã hiểu");
                case 1 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                case 2 -> VuTru13Service.gI().triggerKheNutGodsCutscene(player, false);
            }
        }
    }
}
