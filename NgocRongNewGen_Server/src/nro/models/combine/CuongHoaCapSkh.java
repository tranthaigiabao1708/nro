package nro.models.combine;

import java.util.List;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.SkhCollectionService;
import nro.models.utils.Util;

/**
 * M4: Cường hóa Cấp độ Dòng Set Kích Hoạt (Lv1 -> Lv10) & Chế tạo Đá nâng cấp SKH [1826]
 * - Mỗi cấp cường hóa trên 1 món tăng +2% uy lực dòng SKH (Lv10 = +20%/món; mặc đủ 5 món Lv10 = +100% uy lực Set!).
 * - Thất bại chỉ mất 1 Đồ Hủy Diệt + 1 Đá nâng cấp SKH + 2 Tỷ vàng, GIỮ NGUYÊN trang bị SKH & cấp cường hóa hiện tại.
 */
public class CuongHoaCapSkh {

    public static final short DA_NANG_CAP_SKH_ID = 1826;
    public static final int KHOANG_TAI_CHE_ID = 1656;

    public static final int OPT_SKH_UPGRADE_LV = 258;
    public static final int OPT_SKH_UPGRADE_PCT = 259;

    public static final int MAX_SKH_UPGRADE_LEVEL = 10;
    public static final int PCT_PER_PIECE_LEVEL = 2; // 5 món * Lv10 * 2% = +100% uy lực Set Kích Hoạt

    public static final long GOLD_CUONG_HOA_SKH = 2_000_000_000L;
    public static final int RATIO_CUONG_HOA_SKH = 50;

    public static final int KHOANG_PER_DA_SKH = 10;
    public static final long GOLD_CHE_TAO_DA_SKH = 200_000_000L;

    public static int getSkhUpgradeLevel(Item item) {
        if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
            return 0;
        }
        for (ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == OPT_SKH_UPGRADE_LV) {
                return Math.max(0, Math.min(MAX_SKH_UPGRADE_LEVEL, io.param));
            }
        }
        return 0;
    }

    public static void applySkhUpgradeOptions(Item item, int newLevel) {
        if (item == null || item.itemOptions == null) {
            return;
        }
        int clamped = Math.max(0, Math.min(MAX_SKH_UPGRADE_LEVEL, newLevel));
        item.itemOptions.removeIf(io -> io != null && io.optionTemplate != null
                && (io.optionTemplate.id == OPT_SKH_UPGRADE_LV || io.optionTemplate.id == OPT_SKH_UPGRADE_PCT));
        if (clamped > 0) {
            item.itemOptions.add(new ItemOption(OPT_SKH_UPGRADE_LV, clamped));
            item.itemOptions.add(new ItemOption(OPT_SKH_UPGRADE_PCT, clamped * PCT_PER_PIECE_LEVEL));
        }
    }

    public static void showInfoCheTaoDaSkh(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.size() != 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Vui lòng đặt đúng 1 ô Khoáng tái chế (cần tối thiểu " + KHOANG_PER_DA_SKH + " viên)!", "Đóng");
            return;
        }
        Item khoang = list.get(0);
        if (khoang == null || !khoang.isNotNullItem() || khoang.template.id != KHOANG_TAI_CHE_ID) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Nguyên liệu không hợp lệ! Hãy đặt Khoáng tái chế [phân rã từ đồ SKH thường].", "Đóng");
            return;
        }
        String kColor = khoang.quantity >= KHOANG_PER_DA_SKH ? "|2|" : "|7|";
        String gColor = player.inventory.gold >= GOLD_CHE_TAO_DA_SKH ? "|2|" : "|7|";
        String npcSay = "|8|CHẾ TẠO ĐÁ NÂNG CẤP SKH\n"
                + kColor + "• Khoáng tái chế: " + khoang.quantity + "/" + KHOANG_PER_DA_SKH + "\n"
                + gColor + "• Vàng yêu cầu: " + Util.numberToMoney(GOLD_CHE_TAO_DA_SKH) + " vàng\n"
                + "|1|• Tỉ lệ thành công: 100% -> Nhận 1 Đá nâng cấp SKH";

        if (khoang.quantity < KHOANG_PER_DA_SKH || player.inventory.gold < GOLD_CHE_TAO_DA_SKH) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Còn thiếu\nnguyên liệu");
            return;
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                "Chế tạo ngay\n(" + Util.numberToMoney(GOLD_CHE_TAO_DA_SKH) + " vàng)", "Từ chối");
    }

    public static void thucHienCheTaoDaSkh(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0
                && InventoryService.gI().findItemBag(player, DA_NANG_CAP_SKH_ID) == null) {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất 1 ô trống!");
            return;
        }
        Item khoang = player.combineNew.itemsCombine.get(0);
        if (khoang == null || !khoang.isNotNullItem() || khoang.template.id != KHOANG_TAI_CHE_ID
                || !player.inventory.itemsBag.contains(khoang) || khoang.quantity < KHOANG_PER_DA_SKH) {
            Service.gI().sendThongBao(player, "Không đủ " + KHOANG_PER_DA_SKH + " Khoáng tái chế!");
            return;
        }
        if (player.inventory.gold < GOLD_CHE_TAO_DA_SKH) {
            Service.gI().sendThongBao(player, "Không đủ " + Util.numberToMoney(GOLD_CHE_TAO_DA_SKH) + " vàng!");
            return;
        }

        player.inventory.gold -= GOLD_CHE_TAO_DA_SKH;
        InventoryService.gI().subQuantityItemsBag(player, khoang, KHOANG_PER_DA_SKH);

        Item daSkh = ItemService.gI().createNewItem(DA_NANG_CAP_SKH_ID, 1);
        InventoryService.gI().addItemBag(player, daSkh);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendMoney(player);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
        Service.gI().sendThongBao(player, "Chế tạo thành công 1 Đá nâng cấp SKH!");
    }

    public static void showInfoCuongHoaSkh(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.size() != 3) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Cần đặt ĐÚNG 3 ô vật phẩm:\n"
                    + "|2|• 1 Trang bị đang có dòng Set Kích Hoạt (bất kỳ bậc nào)\n"
                    + "• 1 Trang bị Hủy Diệt (chưa có SKH - dùng làm phôi tế)\n"
                    + "• 1 Đá nâng cấp SKH", "Đóng");
            return;
        }

        Item itemSKH = null;
        Item itemHD = null;
        Item daSkh = null;

        for (Item it : list) {
            if (it == null || !it.isNotNullItem() || !player.inventory.itemsBag.contains(it)) {
                Service.gI().sendThongBao(player, "Có vật phẩm không hợp lệ trong hành trang!");
                return;
            }
            if (it.template.id == DA_NANG_CAP_SKH_ID) {
                daSkh = it;
            } else if (NangCapTrangBiKichHoat.isDoHuyDiet(it) && SkhCollectionService.getMainSkhOptionId(it) == -1) {
                itemHD = it;
            } else if (it.template.type >= 0 && it.template.type <= 4 && SkhCollectionService.getMainSkhOptionId(it) != -1) {
                itemSKH = it;
            }
        }

        if (itemSKH == null || itemHD == null || daSkh == null || daSkh.quantity < 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Đặt sai tổ hợp vật phẩm!\n"
                    + "|2|Bắt buộc có đủ:\n"
                    + "• 1 Trang bị đang có dòng Set Kích Hoạt\n"
                    + "• 1 Trang bị Hủy Diệt (chưa có SKH)\n"
                    + "• 1 Đá nâng cấp SKH", "Đóng");
            return;
        }

        int curLv = getSkhUpgradeLevel(itemSKH);
        if (curLv >= MAX_SKH_UPGRADE_LEVEL) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|8|Trang bị [" + itemSKH.template.name + "] đã đạt Cấp Cường hóa SKH tối đa (Cấp "
                    + MAX_SKH_UPGRADE_LEVEL + "/" + MAX_SKH_UPGRADE_LEVEL + " = +20% uy lực Set)!", "Đóng");
            return;
        }

        int nextLv = curLv + 1;
        int skhId = SkhCollectionService.getMainSkhOptionId(itemSKH);
        String setName = SkhCollectionService.getSetFullName(skhId);
        String gColor = player.inventory.gold >= GOLD_CUONG_HOA_SKH ? "|2|" : "|7|";

        String npcSay = "|8|CƯỜNG HÓA CẤP ĐỘ SET KÍCH HOẠT\n"
                + "|2|• Trang bị chính: " + itemSKH.template.name + " (" + setName + ")\n"
                + "|1|• Cấp cường hóa: Cấp " + curLv + " -> Cấp " + nextLv
                + " (+" + (nextLv * PCT_PER_PIECE_LEVEL) + "% uy lực Set; đủ 5 món Cấp 10 = +100%!)\n"
                + "|2|• Tỉ lệ thành công: " + RATIO_CUONG_HOA_SKH + "%\n"
                + gColor + "• Chi phí: 1 [" + itemHD.template.name + "] + 1 Đá nâng cấp SKH + " + Util.numberToMoney(GOLD_CUONG_HOA_SKH) + " vàng\n"
                + "|7|• Thất bại: Mất Đồ Hủy Diệt, Đá nâng cấp SKH & vàng (GIỮ NGUYÊN trang bị SKH & cấp hiện tại!)";

        if (player.inventory.gold < GOLD_CUONG_HOA_SKH) {
            npcSay += "\n|7|Còn thiếu " + Util.numberToMoney(GOLD_CUONG_HOA_SKH - player.inventory.gold) + " vàng!";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                "Cường hóa\nCấp " + nextLv + " (50%)", "Từ chối");
    }

    public static void thucHienCuongHoaSkh(Player player) {
        List<Item> list = player.combineNew.itemsCombine;
        if (list.size() != 3) {
            return;
        }

        Item itemSKH = null;
        Item itemHD = null;
        Item daSkh = null;

        for (Item it : list) {
            if (it == null || !it.isNotNullItem() || !player.inventory.itemsBag.contains(it)) {
                Service.gI().sendThongBao(player, "Có vật phẩm không còn trong hành trang!");
                return;
            }
            if (it.template.id == DA_NANG_CAP_SKH_ID) {
                daSkh = it;
            } else if (NangCapTrangBiKichHoat.isDoHuyDiet(it) && SkhCollectionService.getMainSkhOptionId(it) == -1) {
                itemHD = it;
            } else if (it.template.type >= 0 && it.template.type <= 4 && SkhCollectionService.getMainSkhOptionId(it) != -1) {
                itemSKH = it;
            }
        }

        if (itemSKH == null || itemHD == null || daSkh == null || itemHD.quantity < 1 || daSkh.quantity < 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu cường hóa SKH!");
            return;
        }

        int curLv = getSkhUpgradeLevel(itemSKH);
        if (curLv >= MAX_SKH_UPGRADE_LEVEL) {
            Service.gI().sendThongBao(player, "Trang bị này đã đạt cấp cường hóa SKH tối đa (Cấp 10)!");
            return;
        }
        if (player.inventory.gold < GOLD_CUONG_HOA_SKH) {
            Service.gI().sendThongBao(player, "Không đủ " + Util.numberToMoney(GOLD_CUONG_HOA_SKH) + " vàng!");
            return;
        }

        player.inventory.gold -= GOLD_CUONG_HOA_SKH;
        InventoryService.gI().subQuantityItemsBag(player, itemHD, 1);
        InventoryService.gI().subQuantityItemsBag(player, daSkh, 1);

        if (Util.isTrue(RATIO_CUONG_HOA_SKH, 100)) {
            int nextLv = curLv + 1;
            applySkhUpgradeOptions(itemSKH, nextLv);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Cường hóa thành công [" + itemSKH.template.name
                    + "] lên SKH Cấp " + nextLv + " (+" + (nextLv * PCT_PER_PIECE_LEVEL) + "% uy lực Set)!");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Cường hóa thất bại! Trang bị [" + itemSKH.template.name + "] vẫn được giữ nguyên an toàn.");
        }

        Service.gI().sendMoney(player);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
