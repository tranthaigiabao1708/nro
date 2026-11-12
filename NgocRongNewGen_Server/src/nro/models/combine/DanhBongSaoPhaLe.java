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

public class DanhBongSaoPhaLe {

    private static final int GOLD_NANG_CAP = 100_000_000;
    private static final int RATIO_NANG_CAP = 100;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item saoPhaLe = null;
            Item daMai = null;
            for (Item item : player.combineNew.itemsCombine) {
                if ((item.template.id >= 1416 && item.template.id <= 1422)
                        || item.template.id == 964 || item.template.id == 965
                        || item.template.id == 2011 || item.template.id == 2013) {
                    saoPhaLe = item;
                } else if (item.template.id == 1439) {
                    daMai = item;
                }
            }

            if (saoPhaLe != null && daMai != null && saoPhaLe.quantity >= 2) {
                player.combineNew.goldCombine = GOLD_NANG_CAP;
                player.combineNew.gemCombine = 0;
                player.combineNew.ratioCombine = RATIO_NANG_CAP;

                String npcSay = "|2|Nâng cấp Sao Pha Lê từ cấp 2 lên Sao Pha Lê lấp lánh\n";
                npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n";
                npcSay += "|2|Cần x2 " + saoPhaLe.template.name + " và 1 đá mài\n";
                npcSay += "|2|Cần: " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng\n";
                npcSay += "|7|Thất bại -1 đá mài\n";
                if (player.inventory.gold < player.combineNew.goldCombine) {
                    npcSay += "|7|Còn thiếu " + Util.powerToString(player.combineNew.goldCombine - player.inventory.gold) + " vàng\n";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\n" + Util.numberToMoney(player.combineNew.goldCombine) + " vàng", "Từ chối");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần x2 Sao Pha Lê cấp 2 và 1 đá mài", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần x2 Sao Pha Lê cấp 2 và 1 đá mài", "Đóng");
        }
    }
    
    public static void danhBongSaoPhaLe(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }

            Item saoPhaLe = null;
            Item daMai = null;
            for (Item item : player.combineNew.itemsCombine) {
                if ((item.template.id >= 1416 && item.template.id <= 1422)
                        || item.template.id == 964 || item.template.id == 965
                        || item.template.id == 2011 || item.template.id == 2013) {
                    saoPhaLe = item;
                } else if (item.template.id == 1439) {
                    daMai = item;
                }
            }

            if (saoPhaLe != null && daMai != null && saoPhaLe.quantity >= 2) {
                int saoPhaLeLapLanhId = saoPhaLeLapLanhId(saoPhaLe.template.id);
                if (saoPhaLeLapLanhId == -1) {
                    return;
                }
                if (InventoryService.gI().getCountEmptyBag(player) <= 0
                        && InventoryService.gI().findItemBag(player, saoPhaLeLapLanhId) == null
                        && saoPhaLe.quantity > 2 && daMai.quantity > 1) {
                    Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang!");
                    return;
                }
                player.inventory.gold -= gold;

                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    ItemService.gI().ensureDefaultOptions(saoPhaLe);
                    Template.ItemTemplate newTemplate = ItemService.gI().getTemplate(saoPhaLeLapLanhId);
                    Item newItem = new Item();
                    newItem.template = newTemplate;
                    newItem.quantity = 1;
                    if (saoPhaLe.template.id != 2011 && saoPhaLe.template.id != 2013) {
                        for (Item.ItemOption option : saoPhaLe.itemOptions) {
                            if (option != null && option.optionTemplate != null && option.optionTemplate.id != 73) {
                                Item.ItemOption newOption = new Item.ItemOption(option.optionTemplate.id, option.param * 2);
                                newItem.itemOptions.add(newOption);
                            }
                        }
                    }
                    ItemService.gI().ensureDefaultOptions(newItem);
                    InventoryService.gI().addItemBag(player, newItem);
                    InventoryService.gI().subQuantityItemsBag(player, saoPhaLe, 2);
                    CombineService.gI().sendEffectSuccessCombine(player);
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                }

                InventoryService.gI().subQuantityItemsBag(player, daMai, 1);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            }
        }
    }

    private static int saoPhaLeLapLanhId(int saoPhaLeCap1Id) {
        switch (saoPhaLeCap1Id) {
            case 1416:
                return 1426;
            case 1417:
                return 1427;
            case 1418:
                return 1428;
            case 1419:
                return 1429;
            case 1420:
                return 1430;
            case 1421:
                return 1431;
            case 1422:
                return 1432;
            case 964:
                return 1433;
            case 965:
                return 1434;
            case 2011:
                return 2012; // Sao pha lê huyết lấp lánh (+11% HP)
            case 2013:
                return 2014; // Sao pha lê khí lấp lánh (+11% KI)
            default:
                return -1;
        }
    }
}
