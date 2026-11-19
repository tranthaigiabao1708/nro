package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class DaiThanQuan extends Npc {

    private static final int MENU_CONFIRM_BAN_NANG = 20311;
    private static final int MENU_CONFIRM_BAN_NGA = 20312;
    private static final int MENU_CONFIRM_UPGRADE = 20313;

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
        int curHonTim = InventoryService.gI().getTotalQuantityBag(player, 2018);
        int curThoiVang = InventoryService.gI().getTotalQuantityBag(player, 457);

        String upgradeRequirementLine = lv >= 5
                ? "\n|8|★ Sức mạnh Vô Cực đã đạt tối đa (Cấp 5/5)!"
                : "\n|8|▶ Nâng cấp lên Cấp " + (lv + 1) + " cần: " + (lv * 20) + " Hồn Boss Tím (có " + curHonTim + ") + "
                        + (lv * 50) + " Thỏi vàng (có " + curThoiVang + ")";

        String curStatus;
        if (player.voCucType == VuTru13Service.BAN_NANG_VO_CUC) {
            curStatus = "|2|Hướng hiện tại: [BẢN NĂNG VÔ CỰC - Cấp " + lv + "/5] (Hào Quang Bạc)\n"
                    + "- Buff tại Vũ Trụ 13: +" + (10 + lv * 10) + "% Né đòn, +" + (20 + lv * 10) + "% Sức đánh, +" + (30 + lv * 10) + "% SĐ Chí mạng\n"
                    + "|7|- Về hành tinh thường: Giảm còn 1/10 (kiềm nén sức mạnh khi rời Vũ Trụ 13)"
                    + upgradeRequirementLine;
        } else if (player.voCucType == VuTru13Service.BAN_NGA_VO_CUC) {
            curStatus = "|2|Hướng hiện tại: [BẢN NGÃ VÔ CỰC - Cấp " + lv + "/5] (Hào Quang Tím Đen)\n"
                    + "- Buff tại Vũ Trụ 13: +" + (10 + lv * 10) + "% Giáp (+" + (30_000 * lv) + " Giáp), +" + (30 + lv * 10) + "% HP & KI, +"
                    + (10 + lv * 5) + "% Dame từ lượng HP đang có & tối đa +" + (50 + lv * 10) + "% Dame theo HP đã mất\n"
                    + "|7|- Về hành tinh thường: Giảm còn 1/10 (kiềm nén sức mạnh khi rời Vũ Trụ 13)"
                    + upgradeRequirementLine;
        } else {
            curStatus = "|1|Ngươi chưa chọn hướng phát triển Sức Mạnh Vô Cực!\n"
                    + "|2|- Hãy chọn 1 trong 2 trạng thái tối thượng: [Bản Năng Vô Cực] hoặc [Bản Ngã Vô Cực].\n"
                    + "- Sau khi chọn, Hào Quang Bạc / Tím Đen sẽ bao quanh hào quang set trang bị, và khi bỏ hoặc ẩn Cải Trang sẽ hóa thành Chibi Vô Cực!\n"
                    + "|8|- Nguyên liệu nâng cấp (Cấp 1 -> 5): Sử dụng [Hồn Boss Tím] + [Thỏi vàng].";
        }

        if (player.voCucType == VuTru13Service.VO_CUC_NONE) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "|8|ĐẠI THIÊN SỨ - THÁNH ĐIỆN VT13\n" + curStatus,
                    "Đổi Trang Bị\nThần Vực\nVô Cực",
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
                    ? "Đã Đạt Cấp\nTối Đa (Lv5)"
                    : "Nâng lên Cấp " + (lv + 1) + "\n(" + (lv * 20) + " Hồn Boss Tím\n+ " + (lv * 50) + " Thỏi vàng)";
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "|8|ĐẠI THIÊN SỨ - THÁNH ĐIỆN VT13\n" + curStatus,
                    "Đổi Trang Bị\nThần Vực\nVô Cực",
                    upgradeLabel,
                    switchLabel,
                    player.hideCaiTrang ? "Hiện lại\nCải Trang" : "Ẩn Cải Trang\n(Hiện Chibi)",
                    "Chi tiết\n2 Hướng",
                    "Đóng");
        }
    }

    private void showConfirmUpgrade(Player player) {
        int curLv = Math.max(1, Math.min(5, player.voCucLevel));
        if (curLv >= 5) {
            VuTru13Service.gI().upgradeVoCucLevel(player, this);
            return;
        }
        int nextLv = curLv + 1;
        int reqHonTim = curLv * 20;
        int reqThoiVang = curLv * 50;
        int curHonTim = InventoryService.gI().getTotalQuantityBag(player, 2018);
        int curThoiVang = InventoryService.gI().getTotalQuantityBag(player, 457);

        String pathTitle = player.voCucType == VuTru13Service.BAN_NANG_VO_CUC
                ? "BẢN NĂNG VÔ CỰC (ULTRA INSTINCT)"
                : "BẢN NGÃ VÔ CỰC (ULTRA EGO)";

        String nextBuffPreview;
        if (player.voCucType == VuTru13Service.BAN_NANG_VO_CUC) {
            nextBuffPreview = "- Chỉ số sau nâng cấp (Cấp " + nextLv + " tại VT13): +" + (10 + nextLv * 10)
                    + "% Né đòn, +" + (20 + nextLv * 10) + "% Sức đánh, +" + (30 + nextLv * 10) + "% SĐ Chí mạng.";
        } else {
            nextBuffPreview = "- Chỉ số sau nâng cấp (Cấp " + nextLv + " tại VT13): +" + (10 + nextLv * 10)
                    + "% Giáp (+" + (30_000 * nextLv) + " Giáp), +" + (30 + nextLv * 10) + "% HP & KI, +"
                    + (10 + nextLv * 5) + "% Dame từ HP hiện có & tối đa +" + (50 + nextLv * 10) + "% Dame theo HP đã mất.";
        }

        String honColor = curHonTim >= reqHonTim ? "|2|" : "|7|";
        String tvColor = curThoiVang >= reqThoiVang ? "|2|" : "|7|";

        this.createOtherMenu(player, MENU_CONFIRM_UPGRADE,
                "|8|NÂNG CẤP " + pathTitle + " (CẤP " + curLv + " -> CẤP " + nextLv + ")\n"
                + "|2|" + nextBuffPreview + "\n"
                + "|8|Nguyên liệu yêu cầu:\n"
                + honColor + "• Hồn Boss Tím: cần " + reqHonTim + " (hành trang đang có: " + curHonTim + ")\n"
                + tvColor + "• Thỏi vàng (gộp cả khóa + thường): cần " + reqThoiVang + " (đang có: " + curThoiVang + ")\n"
                + "|1|(Mẹo kiếm Hồn Boss Tím: Săn Boss Cấp Cao / Boss Vũ Trụ 13, nhận quà từ Beerus Map 1 VT13 hoặc ghép từ Hồn Boss Lam/Lục tại Chi Chi Đảo Kamê)",
                "Xác nhận\nNâng Cấp " + nextLv,
                "Quay lại");
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
                    + "- Nguyên liệu nâng cấp (Cấp 1 -> 5): [Hồn Boss Tím] + [Thỏi vàng].\n"
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
                    + "- Nguyên liệu nâng cấp (Cấp 1 -> 5): [Hồn Boss Tím] + [Thỏi vàng].\n"
                    + "- Khi về hành tinh thường: Giảm còn 1/10.\n"
                    + costLine,
                    isFree ? "Thức tỉnh\nBản Ngã!" : "Xác nhận\nChuyển hướng",
                    "Quay lại");
        }
    }

    private void showFullGuide(Player player) {
        this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "|8|CHI TIẾT 2 HƯỚNG PHÁT TRIỂN VÔ CỰC\n"
                + "|2|1. BẢN NĂNG VÔ CỰC (Hào Quang Bạc - Chibi Tóc Bạc):\n"
                + "+ Tăng mạnh Tỉ lệ Né Đòn, Sức Đánh (%) và Sát Thương Chí Mạng (%).\n"
                + "|2|2. BẢN NGÃ VÔ CỰC (Hào Quang Tím Đen - Chibi Tóc Tím):\n"
                + "+ Tăng mạnh Giáp, +% HP và +% KI, tăng Dame dựa trên HP đã mất & tăng thêm Dame dựa trên lượng HP đang có.\n"
                + "|8|3. NGUYÊN LIỆU NÂNG CẤP (Cấp 1 -> 5):\n"
                + "+ Sử dụng [Hồn Boss Tím] + [Thỏi vàng] (Cấp tiếp theo cần Cấp hiện tại x20 Hồn Boss Tím & x50 Thỏi vàng).\n"
                + "|7|* Quy tắc Vũ Trụ: Về hành tinh thường không ở trong vùng trọng lực đặc biệt của Vũ Trụ 13 nên chỉ số cộng thêm giảm còn 1/10. Bỏ hoặc Ẩn Cải Trang sẽ hiện Chibi Vô Cực!",
                "Đã hiểu");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        int menuIdx = player.idMark.getIndexMenu();
        if (VuTru13Service.gI().handleVoCucSetMenuConfirm(player, this, menuIdx, select)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            if (player.voCucType == VuTru13Service.VO_CUC_NONE) {
                switch (select) {
                    case 0 -> VuTru13Service.gI().openVoCucSetPlanetMenu(player, this);
                    case 1 -> showConfirmPath(player, VuTru13Service.BAN_NANG_VO_CUC);
                    case 2 -> showConfirmPath(player, VuTru13Service.BAN_NGA_VO_CUC);
                    case 3 -> showFullGuide(player);
                    case 4 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                }
            } else {
                byte otherType = player.voCucType == VuTru13Service.BAN_NANG_VO_CUC
                        ? VuTru13Service.BAN_NGA_VO_CUC
                        : VuTru13Service.BAN_NANG_VO_CUC;
                switch (select) {
                    case 0 -> VuTru13Service.gI().openVoCucSetPlanetMenu(player, this);
                    case 1 -> showConfirmUpgrade(player);
                    case 2 -> showConfirmPath(player, otherType);
                    case 3 -> VuTru13Service.gI().toggleHideCaiTrang(player);
                    case 4 -> showFullGuide(player);
                }
            }
        } else if (menuIdx == MENU_CONFIRM_UPGRADE) {
            if (select == 0) {
                VuTru13Service.gI().upgradeVoCucLevel(player, this);
            } else {
                openBaseMenu(player);
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
