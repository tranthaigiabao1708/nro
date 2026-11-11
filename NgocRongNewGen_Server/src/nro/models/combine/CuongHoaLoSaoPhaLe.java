package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class CuongHoaLoSaoPhaLe {

    private static final int COST = 500_000_000;

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 3) {
                Item item = null, Hematite = null, DuiDuc = null;

                for (Item i : player.combineNew.itemsCombine) {
                    if (CombineSystem.isTrangBiPhaLeHoa(i)) {
                        item = i;
                    } else if (i.template.id == 1423) { // Hematite
                        Hematite = i;
                    } else if (i.template.id == 1438) { // Dùi Đục
                        DuiDuc = i;
                    }
                }

                if (item != null && Hematite != null && DuiDuc != null && Hematite.quantity >= 1 && DuiDuc.quantity >= 1) {
                    int star = 0;
                    int starSocketed = 0;
                    int cuongHoaSlot = 0;
                    for (ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id == 107) {
                            star = io.param;
                        } else if (io.optionTemplate.id == 102) {
                            starSocketed = io.param;
                        } else if (io.optionTemplate.id == 228) {
                            cuongHoaSlot = io.param;
                        }
                    }

                    if (star < 8) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Trang bị phải được Pha lê hóa lên 8 hoặc 9 sao trước khi cường hóa lỗ sao!", "Đóng");
                        return;
                    }
                    if (starSocketed < 7 && cuongHoaSlot < 8) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Trang bị cần ép đủ 7 sao pha lê trước khi cường hóa lỗ thứ 8!", "Đóng");
                        return;
                    }
                    if (cuongHoaSlot >= 9) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Trang bị đã cường hóa tối đa tới ô sao pha lê thứ 9!", "Đóng");
                        return;
                    }
                    if (cuongHoaSlot == 8 && star < 9) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Trang bị đã cường hóa ô thứ 8. Hãy Pha lê hóa lên 9 sao để cường hóa tiếp ô thứ 9!", "Đóng");
                        return;
                    }

                    int targetSlot = (cuongHoaSlot < 8) ? 8 : 9;
                    String npcSay = item.template.name + "\n|2|";
                    npcSay += "Cường hóa mở khóa Ô sao pha lê thứ " + targetSlot + "\n"
                            + "Tỉ lệ thành công: 50%\n"
                            + "|7|Cần 1 " + Hematite.template.name + "\n"
                            + "|7|Cần 1 " + DuiDuc.template.name + "\n"
                            + "|1|Cần " + Util.numberToMoney(COST) + " vàng";

                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Cường Hóa\nÔ thứ " + targetSlot, "Từ chối");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public static void cuongHoaLoSaoPhaLe(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang.");
            return;
        }

        if (player.inventory.gold < COST) {
            Service.gI().sendThongBao(player, "Con cần thêm vàng để cường hóa...");
            return;
        }

        if (player.combineNew.itemsCombine.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có vật phẩm để cường hóa.");
            return;
        }

        Item item = null;
        Item hematite = null;
        Item duiDuc = null;

        for (Item i : player.combineNew.itemsCombine) {
            if (CombineSystem.isTrangBiPhaLeHoa(i)) {
                item = i;
            } else if (i.template.id == 1423) {
                hematite = i;
            } else if (i.template.id == 1438) {
                duiDuc = i;
            }
        }

        if (item == null || hematite == null || duiDuc == null || hematite.quantity < 1 || duiDuc.quantity < 1) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm hoặc số lượng không đủ.");
            return;
        }

        int star = 0;
        int starSocketed = 0;
        ItemOption opt228 = null;
        boolean hasOption218 = false;

        for (ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 107) {
                star = io.param;
            } else if (io.optionTemplate.id == 102) {
                starSocketed = io.param;
            } else if (io.optionTemplate.id == 228) {
                opt228 = io;
            } else if (io.optionTemplate.id == 218) {
                hasOption218 = true;
            }
        }

        int currentCuongHoa = (opt228 != null) ? opt228.param : 0;
        if (star < 8) {
            Service.gI().sendThongBao(player, "Vui lòng Pha lê hóa trang bị lên 8 hoặc 9 sao trước khi cường hóa.");
            return;
        }
        if (currentCuongHoa < 8 && starSocketed < 7) {
            Service.gI().sendThongBao(player, "Trang bị cần ép đủ 7 sao pha lê trước khi cường hóa lỗ thứ 8.");
            return;
        }
        if (currentCuongHoa >= 9) {
            Service.gI().sendThongBao(player, "Trang bị đã cường hóa tối đa lỗ thứ 9.");
            return;
        }
        if (currentCuongHoa == 8 && star < 9) {
            Service.gI().sendThongBao(player, "Hãy Pha lê hóa trang bị lên 9 sao trước khi cường hóa lỗ thứ 9.");
            return;
        }

        int targetSlot = (currentCuongHoa < 8) ? 8 : 9;
        boolean success = Util.isTrue(50, 100);
        player.inventory.gold -= COST;
        InventoryService.gI().subQuantityItemsBag(player, hematite, 1);
        InventoryService.gI().subQuantityItemsBag(player, duiDuc, 1);

        if (success) {
            if (!hasOption218) {
                item.itemOptions.add(new ItemOption(218, 0));
            }
            if (opt228 == null) {
                item.itemOptions.add(new ItemOption(228, targetSlot));
            } else {
                opt228.param = targetSlot;
            }
            CombineService.gI().sendEffectSuccessCombine(player);
            CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con đã cường hóa thành công ô sao pha lê thứ " + targetSlot + "!");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            CombineService.gI().baHatMit.npcChat(player, "Cường hóa thất bại rồi, thử lại nhé con!");
        }

        Service.gI().sendMoney(player);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
    }

}
