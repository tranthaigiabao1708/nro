package nro.models.combine;

import java.util.ArrayList;
import java.util.List;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Tái tạo Capsule Kích Hoạt 2 bậc tại Bà Hạt Mít (Đảo Kame):
 * - Capsule Kích Hoạt Thường (ID 1655): 5 Khoáng tái chế + 500M vàng -> mở tự chọn Set KH thường & món đồ.
 * - Capsule Kích Hoạt VIP (ID 1990): 10 Khoáng tái chế + 1B vàng -> mở tự chọn Set KH Mới & món đồ.
 */
public class TaiTaoCapsuleKichHoat {

    private static final int KHOANG_TAI_CHE_ID = 1656;

    public static final int CAPSULE_THUONG_ID = 1655;
    public static final int KHOANG_THUONG = 5;
    public static final int GOLD_THUONG = 500_000_000;

    public static final int CAPSULE_VIP_ID = 1990;
    public static final int KHOANG_VIP = 10;
    public static final int GOLD_VIP = 1_000_000_000;

    private static final int RATIO_TAI_TAO = 100;

    public static void showInfoCombine(Player player) {
        boolean isVip = (player.combineNew.typeCombine == CombineService.TAI_TAO_CAPSULE_KH_VIP);
        int reqKhoang = isVip ? KHOANG_VIP : KHOANG_THUONG;
        int reqGold = isVip ? GOLD_VIP : GOLD_THUONG;
        String capsuleName = isVip ? "Capsule Kích Hoạt VIP" : "Capsule Kích Hoạt Thường";
        String setDesc = isVip ? "Tự chọn Set Kích Hoạt Mới (Kaio / Nail / Cađic M)" : "Tự chọn Set Kích Hoạt thường & Set Gohan";

        if (player.combineNew.itemsCombine.isEmpty()) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hãy đặt đủ " + reqKhoang + " Khoáng tái chế để tái tạo " + capsuleName + "!", "Đóng");
            return;
        }

        int countKhoang = 0;
        for (Item item : player.combineNew.itemsCombine) {
            if (item == null || !item.isNotNullItem()) {
                continue;
            }
            if (item.template.id != KHOANG_TAI_CHE_ID) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "|7|Chỉ được phép đặt Khoáng tái chế, không được đặt vật phẩm khác!", "Đóng");
                return;
            }
            countKhoang += item.quantity;
        }

        if (countKhoang < reqKhoang) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Không đủ Khoáng tái chế!\n"
                    + "|2|Cần " + reqKhoang + " Khoáng tái chế để tạo " + capsuleName + " (hiện đặt " + countKhoang + ").", "Đóng");
            return;
        }

        player.combineNew.goldCombine = reqGold;
        player.combineNew.ratioCombine = RATIO_TAI_TAO;

        String npcSay = "|8|" + capsuleName.toUpperCase() + "\n"
                + "|1|Công dụng: " + setDesc + "\n"
                + "|2|Tỉ lệ thành công: " + RATIO_TAI_TAO + "%\n"
                + "|2|Cần: " + reqKhoang + " Khoáng tái chế + " + Util.numberToMoney(reqGold) + " vàng";

        if (player.inventory.gold < reqGold) {
            npcSay += "\n|7|Còn thiếu " + Util.numberToMoney(reqGold - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Tái tạo\n" + Util.numberToMoney(reqGold) + " vàng", "Từ chối");
        }
    }

    public static void thucHienTaiTao(Player player) {
        boolean isVip = (player.combineNew.typeCombine == CombineService.TAI_TAO_CAPSULE_KH_VIP);
        int reqKhoang = isVip ? KHOANG_VIP : KHOANG_THUONG;
        int reqGold = isVip ? GOLD_VIP : GOLD_THUONG;
        int outCapsuleId = isVip ? CAPSULE_VIP_ID : CAPSULE_THUONG_ID;

        if (player.combineNew.itemsCombine.isEmpty()) {
            Service.gI().sendThongBao(player, "Hãy đặt đủ Khoáng tái chế!");
            return;
        }

        int countKhoang = 0;
        for (Item item : player.combineNew.itemsCombine) {
            if (item == null || !item.isNotNullItem()) {
                continue;
            }
            if (item.template.id != KHOANG_TAI_CHE_ID) {
                Service.gI().sendThongBao(player, "Chỉ được đặt Khoáng tái chế!");
                return;
            }
            countKhoang += item.quantity;
        }

        if (countKhoang < reqKhoang) {
            Service.gI().sendThongBao(player, "Cần đủ " + reqKhoang + " Khoáng tái chế!");
            return;
        }
        if (player.inventory.gold < reqGold) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện!");
            return;
        }
        if (InventoryService.gI().findItemBag(player, outCapsuleId) == null
                && InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang!");
            return;
        }

        player.inventory.gold -= reqGold;
        removeItem(player, KHOANG_TAI_CHE_ID, reqKhoang);

        Item newCapsule = ItemService.gI().createNewItem((short) outCapsuleId, 1);
        newCapsule.itemOptions.add(new ItemOption(73, 0));
        InventoryService.gI().addItemBag(player, newCapsule);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Tái tạo thành công 1 " + newCapsule.template.name + "!");

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        player.combineNew.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
    }

    private static void removeItem(Player player, int itemId, int quantityToRemove) {
        List<Item> items = new ArrayList<>(player.combineNew.itemsCombine);
        for (Item item : items) {
            if (item != null && item.template != null && quantityToRemove > 0) {
                if (item.template.id == itemId) {
                    int remove = Math.min(item.quantity, quantityToRemove);
                    InventoryService.gI().subQuantityItemsBag(player, item, remove);
                    quantityToRemove -= remove;
                }
            }
        }
    }
}
