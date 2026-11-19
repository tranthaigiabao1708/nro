package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * M2: Làm phép Nhập Đá (10 Mảnh đá vụn [225] + 1 Bình nước phép [226] + 10 Triệu vàng -> 80% ra 1 Đá nâng cấp [220..224])
 * - Chấp nhận đặt 2 nguyên liệu theo bất kỳ thứ tự nào trong khung đập đồ.
 */
public class LamPhepNhapDa {

    private static final int GOLD_TAO_DA = 10_000_000;
    private static final int RATIO_TAO_DA = 80;

    private static Item[] resolveMaterials(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            return null;
        }
        Item a = player.combineNew.itemsCombine.get(0);
        Item b = player.combineNew.itemsCombine.get(1);
        if (a == null || b == null || !a.isNotNullItem() || !b.isNotNullItem()
                || !player.inventory.itemsBag.contains(a) || !player.inventory.itemsBag.contains(b)) {
            return null;
        }
        if (a.template.id == 225 && b.template.id == 226) {
            return new Item[]{a, b};
        }
        if (b.template.id == 225 && a.template.id == 226) {
            return new Item[]{b, a};
        }
        return null;
    }

    public static void showInfoCombine(Player player) {
        Item[] mats = resolveMaterials(player);
        if (mats == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|7|Cần đặt đúng 2 vật phẩm:\n|2|• 10 Mảnh đá vụn\n• 1 Bình nước phép (mua tại Uron Siêu Thị)", "Đóng");
            return;
        }

        Item manhDaVun = mats[0];
        Item binhNuocPhep = mats[1];

        player.combineNew.goldCombine = GOLD_TAO_DA;
        player.combineNew.ratioCombine = RATIO_TAO_DA;

        String mColor = manhDaVun.quantity >= 10 ? "|2|" : "|7|";
        String bColor = binhNuocPhep.quantity >= 1 ? "|2|" : "|7|";
        String gColor = player.inventory.gold >= GOLD_TAO_DA ? "|2|" : "|7|";

        String npcSay = "|8|LÀM PHÉP NHẬP ĐÁ\n"
                + mColor + "• Mảnh đá vụn: " + manhDaVun.quantity + "/10\n"
                + bColor + "• Bình nước phép: " + binhNuocPhep.quantity + "/1\n"
                + "|1|• Tỉ lệ thành công: " + RATIO_TAO_DA + "% (Nhận 1 Đá nâng cấp ngẫu nhiên)\n"
                + gColor + "• Chi phí: " + Util.numberToMoney(GOLD_TAO_DA) + " vàng";

        if (manhDaVun.quantity < 10 || binhNuocPhep.quantity < 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Còn thiếu\nnguyên liệu");
        } else if (player.inventory.gold < GOLD_TAO_DA) {
            npcSay += "\n|7|Còn thiếu " + Util.numberToMoney(GOLD_TAO_DA - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Còn thiếu vàng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Làm phép\n(" + Util.numberToMoney(GOLD_TAO_DA) + " vàng)", "Từ chối");
        }
    }

    public static void lamphepnhapda(Player player) {
        Item[] mats = resolveMaterials(player);
        if (mats == null) {
            Service.gI().sendThongBao(player, "Cần đặt đúng 10 Mảnh đá vụn và 1 Bình nước phép!");
            return;
        }
        Item manhDaVun = mats[0];
        Item binhNuocPhep = mats[1];

        if (manhDaVun.quantity < 10 || binhNuocPhep.quantity < 1) {
            Service.gI().sendThongBao(player, "Không đủ 10 Mảnh đá vụn và 1 Bình nước phép!");
            return;
        }
        if (player.inventory.gold < GOLD_TAO_DA) {
            Service.gI().sendThongBao(player, "Không đủ " + Util.numberToMoney(GOLD_TAO_DA) + " vàng!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất 1 ô trống!");
            return;
        }

        player.inventory.gold -= GOLD_TAO_DA;
        InventoryService.gI().subQuantityItemsBag(player, manhDaVun, 10);
        InventoryService.gI().subQuantityItemsBag(player, binhNuocPhep, 1);

        if (Util.isTrue(RATIO_TAO_DA, 100)) {
            short randomId = (short) Util.nextInt(220, 224);
            Item newItem = ItemService.gI().createNewItem(randomId, 1);
            InventoryService.gI().addItemBag(player, newItem);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã nhập đá thành công nhận 1 " + newItem.template.name + "!");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Làm phép thất bại! Đã tiêu hao nguyên liệu.");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}
