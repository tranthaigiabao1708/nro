package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.player_system.Template;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */

public class TaoDaHematite {

    private static final int GOLD_TAO_DA = 50_000_000;  // Vàng cần thiết để tạo đá Hematite
    private static final int RATIO_TAO_DA = 100;  // Tỉ lệ thành công tạo đá Hematite (100%)
    private static final int REQ_SPL_CAP_1 = 20;
    private static final int REWARD_HEMATITE_QTY = 1;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item saoPhaLe = player.combineNew.itemsCombine.get(0);
            if (saoPhaLe.template.id >= 441 && saoPhaLe.template.id <= 447 && saoPhaLe.quantity >= REQ_SPL_CAP_1) {
                player.combineNew.goldCombine = GOLD_TAO_DA;
                player.combineNew.gemCombine = 0;
                player.combineNew.ratioCombine = RATIO_TAO_DA;

                String npcSay = "|2|Tạo đá Hematite từ Sao Pha Lê\n";
                npcSay += "|2|Cần x" + REQ_SPL_CAP_1 + " " + saoPhaLe.template.name + " (Cấp 1)\n";
                npcSay += "|2|Nhận được: " + REWARD_HEMATITE_QTY + " viên đá Hematite\n";
                npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n";
                npcSay += "|2|Cần: " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng\n";

                // Kiểm tra tài nguyên và đưa ra menu
                if (player.inventory.gold < player.combineNew.goldCombine) {
                    npcSay += "|7|Còn thiếu " + Util.powerToString(player.combineNew.goldCombine - player.inventory.gold) + " vàng\n";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Tạo " + REWARD_HEMATITE_QTY + " đá Hematite\n" + Util.numberToMoney(player.combineNew.goldCombine) + " vàng\n", "Từ chối");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần x" + REQ_SPL_CAP_1 + " Sao Pha Lê Cấp 1 cùng loại để ghép " + REWARD_HEMATITE_QTY + " viên đá Hematite", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần x" + REQ_SPL_CAP_1 + " Sao Pha Lê Cấp 1 cùng loại để ghép " + REWARD_HEMATITE_QTY + " viên đá Hematite", "Đóng");
        }
    }

    public static void taoDaHematite(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            int gold = player.combineNew.goldCombine;

            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }

            Item saoPhaLe = player.combineNew.itemsCombine.get(0);

            // Kiểm tra xem có đủ sao pha lê không
            if (saoPhaLe.template.id >= 441 && saoPhaLe.template.id <= 447 && saoPhaLe.quantity >= REQ_SPL_CAP_1) {
                if (InventoryService.gI().getCountEmptyBag(player) <= 0
                        && InventoryService.gI().findItemBag(player, 1423) == null) {
                    Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang!");
                    return;
                }
                // Tiến hành trừ vàng và sao pha lê
                player.inventory.gold -= gold;
                InventoryService.gI().subQuantityItemsBag(player, saoPhaLe, REQ_SPL_CAP_1);

                // Tạo 1 viên đá Hematite (tỉ lệ 100%)
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    Template.ItemTemplate hematiteTemplate = ItemService.gI().getTemplate(1423); // ID của đá Hematite
                    Item hematite = new Item();
                    hematite.template = hematiteTemplate;
                    hematite.quantity = REWARD_HEMATITE_QTY;
                    ItemService.gI().ensureDefaultOptions(hematite);
                    InventoryService.gI().addItemBag(player, hematite);
                    CombineService.gI().sendEffectSuccessCombine(player);
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                }

                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            } else {
                Service.gI().sendThongBao(player, "Cần đủ x" + REQ_SPL_CAP_1 + " Sao Pha Lê Cấp 1 cùng loại để tạo đá Hematite");
            }
        }
    }
}
