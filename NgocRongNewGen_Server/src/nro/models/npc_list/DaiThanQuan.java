package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class DaiThanQuan extends Npc {

    private static final int MENU_CONFIRM_BAN_NANG = 20311;
    private static final int MENU_CONFIRM_BAN_NGA = 20312;

    public DaiThanQuan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
        int lv = Math.max(1, Math.min(5, player.voCucLevel));
        String curStatus;
        if (player.voCucType == VuTru13Service.BAN_NANG_VO_CUC) {
            curStatus = "|2|Hướng hiện tại: [BẢN NĂNG VÔ CỰC - Cấp " + lv + "/5] (Hào Quang Bạc)\n"
                    + "- Buff tại Vũ Trụ 13: +" + (10 + lv * 10) + "% Né đòn, +" + (20 + lv * 10) + "% Sức đánh, +" + (30 + lv * 10) + "% SĐ Chí mạng\n"
                    + "|7|- Về hành tinh thường: Giảm còn 1/10 (do thu liễm trạng thái chiến đấu)";
        } else if (player.voCucType == VuTru13Service.BAN_NGA_VO_CUC) {
            curStatus = "|2|Hướng hiện tại: [BẢN NGÃ VÔ CỰC - Cấp " + lv + "/5] (Hào Quang Tím Đen)\n"
                    + "- Buff tại Vũ Trụ 13: +" + (10 + lv * 10) + "% Giáp (+" + (30_000 * lv) + " Giáp), +" + (30 + lv * 10) + "% HP & KI, +"
                    + (10 + lv * 5) + "% Dame từ lượng HP đang có & tối đa +" + (50 + lv * 10) + "% Dame theo HP đã mất\n"
                    + "|7|- Về hành tinh thường: Giảm còn 1/10 (do thu liễm trạng thái chiến đấu)";
        } else {
            curStatus = "|1|Ngươi chưa chọn hướng phát triển Thần Lực Vô Cực!\n"
                    + "|2|- Hãy chọn 1 trong 2 con đường tối thượng: [Bản Năng Vô Cực] hoặc [Bản Ngã Vô Cực].\n"
                    + "- Sau khi chọn, Hào Quang Bạc / Tím Đen sẽ bao quanh hào quang set trang bị, và khi bỏ hoặc ẩn Cải Trang sẽ hóa thành Chibi Vô Cực!";
        }

        if (player.voCucType == VuTru13Service.VO_CUC_NONE) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "|8|ĐẠI THẦN QUAN - THÁNH ĐIỆN TOÀN VƯƠNG VT13\n" + curStatus,
                    "Chọn hướng\nBản Năng\nVô Cực",
                    "Chọn hướng\nBản Ngã\nVô Cực",
                    "Chi tiết\n2 Hướng",
                    player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang\n(Hiện Chibi)",
                    "Đóng");
        } else {
            String switchLabel = player.voCucType == VuTru13Service.BAN_NANG_VO_CUC
                    ? "Đổi sang\nBản Ngã\n(50 TV)"
                    : "Đổi sang\nBản Năng\n(50 TV)";
            String upgradeLabel = lv >= 5
                    ? "Cảnh Giới\nTối Đa (Lv5)"
                    : "Đột phá\nCấp " + (lv + 1) + "\n(" + (lv * 20) + " Hồn)";
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "|8|ĐẠI THẦN QUAN - THÁNH ĐIỆN TOÀN VƯƠNG VT13\n" + curStatus,
                    upgradeLabel,
                    switchLabel,
                    player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang\n(Hiện Chibi)",
                    "Chi tiết\n2 Hướng",
                    "Đóng");
        }
    }

    private void showConfirmPath(Player player, byte targetType) {
        int lv = Math.max(1, Math.min(5, player.voCucLevel));
        boolean isFree = (player.voCucType == VuTru13Service.VO_CUC_NONE);
        String costLine = isFree
                ? "|2|- Chi phí thức tỉnh lần đầu: MIỄN PHÍ 100%!"
                : "|7|- Chi phí chuyển đổi hướng (giữ nguyên Cấp " + lv + "): 50 Thỏi vàng.";
        if (targetType == VuTru13Service.BAN_NANG_VO_CUC) {
            this.createOtherMenu(player, MENU_CONFIRM_BAN_NANG,
                    "|8|[HƯỚNG 1] BẢN NĂNG VÔ CỰC (ULTRA INSTINCT)\n"
                    + "|2|- Hào quang: Hào Quang BẠC bao quanh bên ngoài hào quang set trang bị.\n"
                    + "- Ngoại hình mặc định (khi bỏ/ẩn Cải Trang): Chibi Goku Tóc Bạc Vô Cực.\n"
                    + "- Chỉ số Cấp " + lv + " tại Vũ Trụ 13: +" + (10 + lv * 10) + "% Né đòn, +" + (20 + lv * 10) + "% Dame, +" + (30 + lv * 10) + "% SĐ Chí mạng.\n"
                    + "- Khi về hành tinh thường: Giảm còn 1/10.\n"
                    + costLine,
                    isFree ? "Thức tỉnh\nBản Năng!" : "Xác nhận\nChuyển hướng",
                    "Quay lại");
        } else {
            this.createOtherMenu(player, MENU_CONFIRM_BAN_NGA,
                    "|8|[HƯỚNG 2] BẢN NGÃ VÔ CỰC (ULTRA EGO)\n"
                    + "|2|- Hào quang: Hào Quang TÍM ĐEN bao quanh bên ngoài hào quang set trang bị.\n"
                    + "- Ngoại hình mặc định (khi bỏ/ẩn Cải Trang): Chibi Vegeta Tóc Tím Bản Ngã.\n"
                    + "- Chỉ số Cấp " + lv + " tại Vũ Trụ 13: +" + (10 + lv * 10) + "% Giáp, +" + (30 + lv * 10) + "% HP & KI, +"
                    + (10 + lv * 5) + "% Dame từ HP đang có & tối đa +" + (50 + lv * 10) + "% Dame theo HP đã mất.\n"
                    + "- Khi về hành tinh thường: Giảm còn 1/10.\n"
                    + costLine,
                    isFree ? "Thức tỉnh\nBản Ngã!" : "Xác nhận\nChuyển hướng",
                    "Quay lại");
        }
    }

    private void showFullGuide(Player player) {
        this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "|8|BÍ PHÁP 2 HƯỚNG PHÁT TRIỂN VÔ CỰC\n"
                + "|2|1. BẢN NĂNG VÔ CỰC (Hào Quang Bạc - Chibi Tóc Bạc):\n"
                + "+ Tăng mạnh Tỉ lệ Né Đòn, Sức Đánh (%) và Sát Thương Chí Mạng (%).\n"
                + "|2|2. BẢN NGÃ VÔ CỰC (Hào Quang Tím Đen - Chibi Tóc Tím):\n"
                + "+ Tăng mạnh Giáp, +% HP và +% KI, tăng Dame dựa trên HP đã mất & tăng thêm Dame dựa trên lượng HP đang có.\n"
                + "|7|* Quy tắc Vũ Trụ: Về hành tinh thường không cần vào trạng thái chiến đấu nên buff giảm còn 1/10. Bỏ hoặc Ẩn Cải Trang sẽ hiện Chibi Vô Cực!",
                "Đã hiểu");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        int menuIdx = player.idMark.getIndexMenu();
        if (player.idMark.isBaseMenu()) {
            if (player.voCucType == VuTru13Service.VO_CUC_NONE) {
                switch (select) {
                    case 0 -> showConfirmPath(player, VuTru13Service.BAN_NANG_VO_CUC);
                    case 1 -> showConfirmPath(player, VuTru13Service.BAN_NGA_VO_CUC);
                    case 2 -> showFullGuide(player);
                    case 3 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                }
            } else {
                byte otherType = player.voCucType == VuTru13Service.BAN_NANG_VO_CUC
                        ? VuTru13Service.BAN_NGA_VO_CUC
                        : VuTru13Service.BAN_NANG_VO_CUC;
                switch (select) {
                    case 0 -> VuTru13Service.gI().upgradeVoCucLevel(player, this);
                    case 1 -> showConfirmPath(player, otherType);
                    case 2 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                    case 3 -> showFullGuide(player);
                }
            }
        } else if (menuIdx == MENU_CONFIRM_BAN_NANG) {
            if (select == 0) {
                VuTru13Service.gI().chooseVoCucPath(player, VuTru13Service.BAN_NANG_VO_CUC, this);
            } else {
                openBaseMenu(player);
            }
        } else if (menuIdx == MENU_CONFIRM_BAN_NGA) {
            if (select == 0) {
                VuTru13Service.gI().chooseVoCucPath(player, VuTru13Service.BAN_NGA_VO_CUC, this);
            } else {
                openBaseMenu(player);
            }
        }
    }
}
