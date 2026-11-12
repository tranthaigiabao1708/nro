package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class EpSaoTrangBi {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineSystem.isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (CombineSystem.isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0;
            int starEmpty = 0;
            if (trangBi != null && daPhaLe != null) {
                int optionIdCheck = CombineSystem.getOptionDaPhaLe(daPhaLe);
                if (CombineSystem.isPhuKienMoRong(trangBi) && !CombineSystem.isSplDaDungChoPhuKien(optionIdCheck)) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "|7|Phụ kiện mở rộng (Đeo lưng, Thú cưỡi, Linh thú, Giáp tập luyện, Sách tuyệt kỹ) chỉ được ép Sao Pha Lê Đa Dụng:\n"
                            + "|2|- Hút HP / Hút KI\n- Phản sát thương\n- Xuyên giáp chưởng / cận chiến\n- % Vàng / % Tiềm năng sức mạnh\n"
                            + "|1|(Ngọc Rồng & SPL % SĐ, % HP, % KI chỉ ép vào 6 Trang bị chính: Áo, Quần, Găng, Giày, Rada, Cải trang)",
                            "Đóng");
                    return;
                }
                for (ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 102) {
                        star = io.param;
                    } else if (io.optionTemplate.id == 107) {
                        starEmpty = io.param;
                    }
                }

                if (starEmpty <= 9) {
                    int nextSlot = star + 1;
                    if (nextSlot >= 8 && !CombineService.gI().CheckSlot(trangBi, nextSlot)) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần cường hóa lỗ sao pha lê thứ " + nextSlot + " này trước", "Đóng");
                        return;
                    }

                    player.combineNew.gemCombine = CombineSystem.getGemEpSao(star);
                    String npcSay = trangBi.template.name + "\n|2|";
                    for (ItemOption io : trangBi.itemOptions) {
                        if (io.optionTemplate.id != 102) {
                            npcSay += io.getOptionString() + "\n";
                        }
                    }

                    if (daPhaLe.template.type == 30) {
                        ItemService.gI().ensureDefaultOptions(daPhaLe);
                        for (ItemOption io : daPhaLe.itemOptions) {
                            if (io.optionTemplate.id != 73) {
                                npcSay += "|7|" + io.getOptionString() + "\n";
                            }
                        }
                    } else {
                        npcSay += "|7|" + ItemService.gI().getItemOptionTemplate(CombineSystem.getOptionDaPhaLe(daPhaLe)).name
                                .replaceAll("#", CombineSystem.getParamDaPhaLe(daPhaLe) + "") + "\n";
                    }
                    npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.gemCombine) + " ngọc";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào, và lỗ sao tối đa là 9", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
        }
    }

    public static void epSaoTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }

            Item trangBi = null;
            Item daPhaLe = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (CombineSystem.isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (CombineSystem.isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }

            if (trangBi == null || daPhaLe == null) {
                return;
            }

            int optionId = CombineSystem.getOptionDaPhaLe(daPhaLe);
            int param = CombineSystem.getParamDaPhaLe(daPhaLe);
            if (optionId == -1 || param <= 0) {
                Service.gI().sendThongBao(player, "Đá pha lê không hợp lệ");
                return;
            }
            if (CombineSystem.isPhuKienMoRong(trangBi) && !CombineSystem.isSplDaDungChoPhuKien(optionId)) {
                Service.gI().sendThongBao(player, "Phụ kiện mở rộng chỉ được ép Sao Pha Lê Đa Dụng (% TNSM, Vàng, Hút HP/KI, Xuyên giáp, Phản ST)!");
                return;
            }

            int star = 0;
            int starEmpty = 0;
            ItemOption optionStar = null;

            for (ItemOption io : trangBi.itemOptions) {
                if (io.optionTemplate.id == 102) {
                    star = io.param;
                    optionStar = io;
                } else if (io.optionTemplate.id == 107) {
                    starEmpty = io.param;
                }
            }

            if (star >= starEmpty) {
                Service.gI().sendThongBao(player, "Không thể ép sao cao hơn lỗ");
                return;
            }

            int nextSlot = star + 1;
            if (nextSlot >= 8 && !CombineService.gI().CheckSlot(trangBi, nextSlot)) {
                Service.gI().sendThongBao(player, "Cần cường hóa lỗ sao pha lê thứ " + nextSlot + " này trước");
                return;
            }

            player.inventory.subGem(gem);

            // Gộp tất cả các ô từ 1 đến 9 vào chung 1 dòng % trên từng món trang bị
            boolean merged = false;
            for (ItemOption io : trangBi.itemOptions) {
                if (io.optionTemplate.id == optionId) {
                    io.param += param;
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                trangBi.itemOptions.add(new ItemOption(optionId, param));
            }

            if (optionStar != null) {
                optionStar.param += 1;
            } else {
                trangBi.itemOptions.add(new ItemOption(102, 1));
            }

            InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }

}
