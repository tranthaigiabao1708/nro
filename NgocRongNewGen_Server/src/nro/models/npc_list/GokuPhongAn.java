package nro.models.npc_list;

import nro.models.consts.ConstNpc;
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
        boolean isUnsealed = (player.vuTru13Stage & VuTru13Service.STAGE_FLAG_GOKU_UNSEALED) != 0;
        if (!isUnsealed) {
            int runeCount = VuTru13Service.gI().countDistinctUnsealRunes(player);
            String sealState = (player.vuTru13Stage & VuTru13Service.STAGE_FLAG_BLACK_FIDE_SEAL) != 0
                    ? "|7|[KHẨN CẤP] Thần Hủy Diệt Bunny đang giữ 7 Ấn mở khóa tại Điện Thờ VT13!\n"
                    + "|1|Hãy tiêu diệt Boss Thần Hủy Diệt Bunny để thu thập đủ bộ Ấn mở khóa từ 1 đến 7 đưa cho ta!\n"
                    : "|1|Xiềng xích phong ấn của Vũ Trụ 13 đang khóa chặt Bản Năng Vô Cực của ta...\n"
                    + "|2|Hãy đánh bại Fide Đen và săn Thần Hủy Diệt Bunny tại Điện Thờ để tìm đủ 7 Ấn mở khóa (1 -> 7) giải thoát cho ta!\n";

            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "|8|SON GOKU (ĐANG BỊ PHONG ẤN TẠI VŨ TRỤ 13)\n"
                    + sealState
                    + VuTru13Service.gI().formatUnsealRuneProgress(player),
                    "Giải Phong Ấn\nGoku (" + runeCount + "/7 Ấn)");
        } else {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "|8|SON GOKU (ĐÃ GIẢI PHONG ẤN - BẢN NĂNG VÔ CỰC)\n"
                    + "|2|Cảm ơn cậu đã thu thập đủ 7 Ấn mở khóa phá tan xiềng xích ngục tối!\n"
                    + "|1|Nguồn khí Bản Năng Vô Cực của ta đã hồi phục hoàn toàn. Hãy hoàn thành chuỗi nhiệm vụ Thần Vực và tiêu diệt Thần Hủy Diệt Bunny!",
                    "Giải Phong Ấn\n(Đã Hoàn Tất)");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu() && select == 0) {
            VuTru13Service.gI().unsealGoku(player, this);
        }
    }
}
