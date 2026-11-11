package nro.models.combine;

import java.util.ArrayList;
import java.util.List;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.SkhCollectionService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Nâng cấp Trang bị Thần Linh Kích Hoạt & Hủy Diệt Kích Hoạt tại Bà Hạt Mít (Đảo Kame).
 * - Chỉ chấp nhận đúng phôi SKH + Đồ Thần Linh / Hủy Diệt cùng loại trang bị (Áo-Áo, Quần-Quần, Găng-Găng, Giày-Giày, Rada-Nhẫn) + Khoáng tái chế.
 * - Không cho phép bỏ thêm bất kỳ vật phẩm nào khác.
 * - Thành công: giữ nguyên đúng loại Set của phôi SKH sang món Thần Linh / Hủy Diệt.
 * - Thất bại: MẤT LUÔN cả Đồ Thần Linh / Hủy Diệt VÀ Đồ SKH dùng để ép.
 */
public class NangCapTrangBiKichHoat {

    private static final int KHOANG_TAI_CHE_ID = 1656;

    // Cấu hình lên Thần Linh SKH
    public static final int GOLD_THAN_LINH_SKH = 500_000_000;
    public static final int KHOANG_THAN_LINH_SKH = 2;
    public static final int RATIO_THAN_LINH_SKH = 40;

    // Cấu hình lên Hủy Diệt SKH
    public static final int GOLD_HUY_DIET_SKH = 2_000_000_000;
    public static final int KHOANG_HUY_DIET_SKH = 5;
    public static final int RATIO_HUY_DIET_SKH = 30;

    public static boolean isDoThanLinh(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        int id = item.template.id;
        return id >= 555 && id <= 567;
    }

    public static boolean isDoHuyDiet(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        int id = item.template.id;
        return id >= 650 && id <= 662;
    }

    public static boolean isDoThienSu(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        int id = item.template.id;
        return id >= 1048 && id <= 1062;
    }

    public static int getMainSkhOptionId(Item item) {
        return SkhCollectionService.getMainSkhOptionId(item);
    }

    private static String getSlotTypeName(int type) {
        return switch (type) {
            case 0 -> "Áo";
            case 1 -> "Quần";
            case 2 -> "Găng";
            case 3 -> "Giày";
            case 4 -> "Rada / Nhẫn";
            default -> "Trang bị";
        };
    }

    private static String getGenderName(int gender) {
        return switch (gender) {
            case 0 -> "Trái Đất";
            case 1 -> "Namek";
            case 2 -> "Xayda";
            default -> "Dùng chung";
        };
    }

    public static void showInfoThanLinhSKH(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.size() != 3) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Chỉ được phép đặt ĐÚNG 3 ô vật phẩm:\n"
                    + "|2|- 1 Trang bị Kích Hoạt thường (phôi truyền Set)\n"
                    + "- 1 Trang bị Thần Linh cùng loại (chưa có SKH)\n"
                    + "- " + KHOANG_THAN_LINH_SKH + " Khoáng tái chế\n"
                    + "|7|Tuyệt đối không đặt bất kỳ vật phẩm nào khác!", "Đóng");
            return;
        }

        Item itemSKH = null;
        Item itemTL = null;
        Item itemKhoang = null;

        for (Item it : list) {
            if (it == null || !it.isNotNullItem()) {
                continue;
            }
            if (it.template.id == KHOANG_TAI_CHE_ID) {
                itemKhoang = it;
            } else if (isDoThanLinh(it) && getMainSkhOptionId(it) == -1) {
                itemTL = it;
            } else if (it.template.type >= 0 && it.template.type <= 4
                    && !isDoThanLinh(it) && !isDoHuyDiet(it) && !isDoThienSu(it)
                    && getMainSkhOptionId(it) != -1) {
                itemSKH = it;
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "|7|Vật phẩm [" + it.template.name + "] không hợp lệ!\n"
                        + "|2|Chỉ được phép sử dụng:\n"
                        + "- 1 Đồ Kích Hoạt thường\n"
                        + "- 1 Đồ Thần Linh chưa kích hoạt\n"
                        + "- " + KHOANG_THAN_LINH_SKH + " Khoáng tái chế", "Đóng");
                return;
            }
        }

        if (itemSKH == null || itemTL == null || itemKhoang == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Đặt sai tổ hợp vật phẩm!\n"
                    + "|2|Bắt buộc có đủ:\n"
                    + "- 1 Đồ Kích Hoạt thường\n"
                    + "- 1 Đồ Thần Linh chưa kích hoạt\n"
                    + "- " + KHOANG_THAN_LINH_SKH + " Khoáng tái chế", "Đóng");
            return;
        }

        if (itemSKH.template.type != itemTL.template.type) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Sai loại trang bị! Không thể ghép [" + getSlotTypeName(itemSKH.template.type)
                    + "] với [" + getSlotTypeName(itemTL.template.type) + "]!\n"
                    + "|2|Bắt buộc phải dùng cùng loại:\n"
                    + "Áo - Áo | Quần - Quần | Găng - Găng | Giày - Giày | Rada - Nhẫn", "Đóng");
            return;
        }

        if (itemTL.template.type != 4 && itemSKH.template.gender != itemTL.template.gender) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Hai trang bị không cùng hành tinh!\n"
                    + "|2|- Phôi SKH: " + getGenderName(itemSKH.template.gender) + "\n"
                    + "- Đồ Thần Linh: " + getGenderName(itemTL.template.gender) + "\n"
                    + "Bắt buộc phải cùng hành tinh!", "Đóng");
            return;
        }

        if (itemKhoang.quantity < KHOANG_THAN_LINH_SKH) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Không đủ Khoáng tái chế!\n"
                    + "|2|Cần " + KHOANG_THAN_LINH_SKH + " Khoáng tái chế (hiện có " + itemKhoang.quantity + ")", "Đóng");
            return;
        }

        int skhId = getMainSkhOptionId(itemSKH);
        String setName = SkhCollectionService.getSetFullName(skhId);

        player.combineNew.goldCombine = GOLD_THAN_LINH_SKH;
        player.combineNew.ratioCombine = RATIO_THAN_LINH_SKH;

        String npcSay = "|8|NÂNG CẤP ĐỒ THẦN LINH KÍCH HOẠT\n"
                + "|2|Trang bị nhận dòng: " + itemTL.template.name + "\n"
                + "|1|Thuộc tính nhận: " + setName + "\n"
                + "|2|(Chỉ thêm thuộc tính Kích Hoạt, không chuyển cấp [+] và sao pha lê)\n"
                + "|2|Tỉ lệ thành công: " + RATIO_THAN_LINH_SKH + "%\n"
                + "|2|Cần: " + KHOANG_THAN_LINH_SKH + " Khoáng tái chế + " + Util.numberToMoney(GOLD_THAN_LINH_SKH) + " vàng\n"
                + "|7|LƯU Ý: Nếu THẤT BẠI sẽ MẤT LUÔN cả [" + itemTL.template.name + "] và [" + itemSKH.template.name + "]!";

        if (player.inventory.gold < GOLD_THAN_LINH_SKH) {
            npcSay += "\n|7|Còn thiếu " + Util.numberToMoney(GOLD_THAN_LINH_SKH - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Nâng cấp\n" + Util.numberToMoney(GOLD_THAN_LINH_SKH) + " vàng", "Từ chối");
        }
    }

    public static void thucHienThanLinhSKH(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.size() != 3) {
            Service.gI().sendThongBao(player, "Chỉ được đặt đúng 1 Đồ SKH thường + 1 Đồ Thần Linh cùng loại + 2 Khoáng tái chế!");
            return;
        }

        Item itemSKH = null;
        Item itemTL = null;
        Item itemKhoang = null;

        for (Item it : list) {
            if (it == null || !it.isNotNullItem()) {
                continue;
            }
            if (it.template.id == KHOANG_TAI_CHE_ID) {
                itemKhoang = it;
            } else if (isDoThanLinh(it) && getMainSkhOptionId(it) == -1) {
                itemTL = it;
            } else if (it.template.type >= 0 && it.template.type <= 4
                    && !isDoThanLinh(it) && !isDoHuyDiet(it) && !isDoThienSu(it)
                    && getMainSkhOptionId(it) != -1) {
                itemSKH = it;
            } else {
                Service.gI().sendThongBao(player, "Không được phép đặt vật phẩm khác ngoài Đồ SKH, Đồ Thần và Khoáng tái chế!");
                return;
            }
        }

        if (itemSKH == null || itemTL == null || itemKhoang == null
                || itemSKH.quantity < 1 || itemTL.quantity < 1 || itemKhoang.quantity < KHOANG_THAN_LINH_SKH) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu nâng cấp!");
            return;
        }
        if (itemSKH.template.type != itemTL.template.type
                || (itemTL.template.type != 4 && itemSKH.template.gender != itemTL.template.gender)) {
            Service.gI().sendThongBao(player, "Bắt buộc phải dùng trang bị cùng loại và cùng hành tinh!");
            return;
        }
        if (player.inventory.gold < GOLD_THAN_LINH_SKH) {
            Service.gI().sendThongBao(player, "Không đủ vàng để nâng cấp!");
            return;
        }

        int skhId = getMainSkhOptionId(itemSKH);
        String setName = SkhCollectionService.getSetFullName(skhId);

        player.inventory.gold -= GOLD_THAN_LINH_SKH;
        InventoryService.gI().subQuantityItemsBag(player, itemKhoang, KHOANG_THAN_LINH_SKH);

        if (Util.isTrue(RATIO_THAN_LINH_SKH, 100)) {
            transferSkhOptions(skhId, itemTL);
            InventoryService.gI().subQuantityItemsBag(player, itemSKH, 1);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã nâng cấp thành công " + itemTL.template.name + " (" + setName + ")!");
        } else {
            String tlName = itemTL.template.name;
            String skhName = itemSKH.template.name;
            InventoryService.gI().subQuantityItemsBag(player, itemTL, 1);
            InventoryService.gI().subQuantityItemsBag(player, itemSKH, 1);
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Nâng cấp thất bại! Đã mất " + tlName + " và " + skhName + "!");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        player.combineNew.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
    }

    public static void showInfoHuyDietSKH(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.size() != 3) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Chỉ được phép đặt ĐÚNG 3 ô vật phẩm:\n"
                    + "|2|- 1 Trang bị Thần Linh Kích Hoạt (phôi truyền Set)\n"
                    + "- 1 Trang bị Hủy Diệt cùng loại (chưa có SKH)\n"
                    + "- " + KHOANG_HUY_DIET_SKH + " Khoáng tái chế\n"
                    + "|7|Tuyệt đối không đặt bất kỳ vật phẩm nào khác!", "Đóng");
            return;
        }

        Item itemTLSKH = null;
        Item itemHD = null;
        Item itemKhoang = null;

        for (Item it : list) {
            if (it == null || !it.isNotNullItem()) {
                continue;
            }
            if (it.template.id == KHOANG_TAI_CHE_ID) {
                itemKhoang = it;
            } else if (isDoHuyDiet(it) && getMainSkhOptionId(it) == -1) {
                itemHD = it;
            } else if (isDoThanLinh(it) && getMainSkhOptionId(it) != -1) {
                itemTLSKH = it;
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "|7|Vật phẩm [" + it.template.name + "] không hợp lệ!\n"
                        + "|2|Chỉ được phép sử dụng:\n"
                        + "- 1 Đồ Thần Linh Kích Hoạt\n"
                        + "- 1 Đồ Hủy Diệt chưa kích hoạt\n"
                        + "- " + KHOANG_HUY_DIET_SKH + " Khoáng tái chế", "Đóng");
                return;
            }
        }

        if (itemTLSKH == null || itemHD == null || itemKhoang == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Đặt sai tổ hợp vật phẩm!\n"
                    + "|2|Bắt buộc có đủ:\n"
                    + "- 1 Đồ Thần Linh Kích Hoạt\n"
                    + "- 1 Đồ Hủy Diệt chưa kích hoạt\n"
                    + "- " + KHOANG_HUY_DIET_SKH + " Khoáng tái chế", "Đóng");
            return;
        }

        if (itemTLSKH.template.type != itemHD.template.type) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Sai loại trang bị! Không thể ghép [" + getSlotTypeName(itemTLSKH.template.type)
                    + "] với [" + getSlotTypeName(itemHD.template.type) + "]!\n"
                    + "|2|Bắt buộc phải dùng cùng loại:\n"
                    + "Áo - Áo | Quần - Quần | Găng - Găng | Giày - Giày | Nhẫn - Nhẫn", "Đóng");
            return;
        }

        if (itemHD.template.type != 4 && itemTLSKH.template.gender != itemHD.template.gender) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Hai trang bị không cùng hành tinh!\n"
                    + "|2|- Đồ Thần SKH: " + getGenderName(itemTLSKH.template.gender) + "\n"
                    + "- Đồ Hủy Diệt: " + getGenderName(itemHD.template.gender) + "\n"
                    + "Bắt buộc phải cùng hành tinh!", "Đóng");
            return;
        }

        if (itemKhoang.quantity < KHOANG_HUY_DIET_SKH) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Không đủ Khoáng tái chế!\n"
                    + "|2|Cần " + KHOANG_HUY_DIET_SKH + " Khoáng tái chế (hiện có " + itemKhoang.quantity + ")", "Đóng");
            return;
        }

        int skhId = getMainSkhOptionId(itemTLSKH);
        String setName = SkhCollectionService.getSetFullName(skhId);

        player.combineNew.goldCombine = GOLD_HUY_DIET_SKH;
        player.combineNew.ratioCombine = RATIO_HUY_DIET_SKH;

        String npcSay = "|8|NÂNG CẤP ĐỒ HỦY DIỆT KÍCH HOẠT\n"
                + "|2|Trang bị nhận dòng: " + itemHD.template.name + "\n"
                + "|1|Thuộc tính nhận: " + setName + "\n"
                + "|2|(Chỉ thêm thuộc tính Kích Hoạt, không chuyển cấp [+] và sao pha lê)\n"
                + "|2|Tỉ lệ thành công: " + RATIO_HUY_DIET_SKH + "%\n"
                + "|2|Cần: " + KHOANG_HUY_DIET_SKH + " Khoáng tái chế + " + Util.numberToMoney(GOLD_HUY_DIET_SKH) + " vàng\n"
                + "|7|LƯU Ý: Nếu THẤT BẠI sẽ MẤT LUÔN cả [" + itemHD.template.name + "] và [" + itemTLSKH.template.name + "]!";

        if (player.inventory.gold < GOLD_HUY_DIET_SKH) {
            npcSay += "\n|7|Còn thiếu " + Util.numberToMoney(GOLD_HUY_DIET_SKH - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Nâng cấp\n" + Util.numberToMoney(GOLD_HUY_DIET_SKH) + " vàng", "Từ chối");
        }
    }

    public static void thucHienHuyDietSKH(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.size() != 3) {
            Service.gI().sendThongBao(player, "Chỉ được đặt đúng 1 Đồ Thần Linh SKH + 1 Đồ Hủy Diệt cùng loại + 5 Khoáng tái chế!");
            return;
        }

        Item itemTLSKH = null;
        Item itemHD = null;
        Item itemKhoang = null;

        for (Item it : list) {
            if (it == null || !it.isNotNullItem()) {
                continue;
            }
            if (it.template.id == KHOANG_TAI_CHE_ID) {
                itemKhoang = it;
            } else if (isDoHuyDiet(it) && getMainSkhOptionId(it) == -1) {
                itemHD = it;
            } else if (isDoThanLinh(it) && getMainSkhOptionId(it) != -1) {
                itemTLSKH = it;
            } else {
                Service.gI().sendThongBao(player, "Không được phép đặt vật phẩm khác ngoài Đồ Thần SKH, Đồ Hủy Diệt và Khoáng tái chế!");
                return;
            }
        }

        if (itemTLSKH == null || itemHD == null || itemKhoang == null
                || itemTLSKH.quantity < 1 || itemHD.quantity < 1 || itemKhoang.quantity < KHOANG_HUY_DIET_SKH) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu nâng cấp!");
            return;
        }
        if (itemTLSKH.template.type != itemHD.template.type
                || (itemHD.template.type != 4 && itemTLSKH.template.gender != itemHD.template.gender)) {
            Service.gI().sendThongBao(player, "Bắt buộc phải dùng trang bị cùng loại và cùng hành tinh!");
            return;
        }
        if (player.inventory.gold < GOLD_HUY_DIET_SKH) {
            Service.gI().sendThongBao(player, "Không đủ vàng để nâng cấp!");
            return;
        }

        int skhId = getMainSkhOptionId(itemTLSKH);
        String setName = SkhCollectionService.getSetFullName(skhId);

        player.inventory.gold -= GOLD_HUY_DIET_SKH;
        InventoryService.gI().subQuantityItemsBag(player, itemKhoang, KHOANG_HUY_DIET_SKH);

        if (Util.isTrue(RATIO_HUY_DIET_SKH, 100)) {
            transferSkhOptions(skhId, itemHD);
            InventoryService.gI().subQuantityItemsBag(player, itemTLSKH, 1);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã nâng cấp thành công " + itemHD.template.name + " (" + setName + ")!");
        } else {
            String hdName = itemHD.template.name;
            String tlName = itemTLSKH.template.name;
            InventoryService.gI().subQuantityItemsBag(player, itemHD, 1);
            InventoryService.gI().subQuantityItemsBag(player, itemTLSKH, 1);
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Nâng cấp thất bại! Đã mất " + hdName + " và " + tlName + "!");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        player.combineNew.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
    }

    private static boolean isStarOrUpgradeOption(int id) {
        return id == 72 || id == 209 || id == 102 || id == 107 || id == 218 || id == 228
                || (id >= 95 && id <= 101)
                || id == 50 || id == 77 || id == 80 || id == 81 || id == 103 || id == 153 || id == 160;
    }

    private static void transferSkhOptions(int skhId, Item targetItem) {
        if (targetItem.itemOptions == null) {
            targetItem.itemOptions = new ArrayList<>();
        }
        targetItem.itemOptions.removeIf(io -> io != null && io.optionTemplate != null
                && (SkhCollectionService.isAnySkhOptionId(io.optionTemplate.id)
                || isStarOrUpgradeOption(io.optionTemplate.id)));
        targetItem.itemOptions.add(new ItemOption(skhId, 0));
        for (int subId : ItemService.gI().getOptionIdsBySKH(skhId)) {
            targetItem.itemOptions.add(new ItemOption(subId, 0));
        }
    }
}

