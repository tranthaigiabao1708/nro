package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Minh Du
 */
public class NangChiSoBongTai {
    private static final int GEM_NANG_BT = 1_000;
    private static final int RATIO_NANG_CAP = 50;
    private static final int ITEM_PARAM_INDEX = 31; 
    private static final int BONG_TAI_ID = 921;
    private static final int HON_BONG_TAI_ID = 934;
    private static final int DA_XANH_LAM_ID = 935;
    private static final int REQUIRED_HON_BONG_TAI = 99; 

    private static final short[] UPGRADE_OPTIONS = {50, 77, 103, 5, 94, 98, 99, 216};

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            Item bongTai = null;
            Item honBongTai = null;
            Item daXanhLam = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    switch (item.template.id) {
                        case BONG_TAI_ID -> bongTai = item;
                        case HON_BONG_TAI_ID -> honBongTai = item;
                        case DA_XANH_LAM_ID -> daXanhLam = item;
                    }
                }
            }
            if (bongTai != null && honBongTai != null && daXanhLam != null) {

                player.combineNew.gemCombine = GEM_NANG_BT;
                player.combineNew.ratioCombine = RATIO_NANG_CAP;

                // Lấy số lượng Mảnh hồn bông tai từ param (option 31)
                int currentHonSoLuong = InventoryService.gI().getParam(player, ITEM_PARAM_INDEX, HON_BONG_TAI_ID);

                String npcSay = "|2|Mở chỉ số Bông tai Porata [+2]" + "\n\n";
                npcSay += "|1|Nội tại hợp thể: +10% Sức đánh\n";
                npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                
                if (daXanhLam.quantity < 1) {
                    npcSay += "|2|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|7|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số chiến đấu ngẫu nhiên (10% - 25%)\n";
                    npcSay += "|2|Còn thiếu " + (1 - daXanhLam.quantity) + " " + daXanhLam.template.name;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else if (currentHonSoLuong < REQUIRED_HON_BONG_TAI) { // So sánh với số lượng từ param
                    npcSay += "|7|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số chiến đấu ngẫu nhiên (10% - 25%)\n";
                    npcSay += "|2|Còn thiếu " + (REQUIRED_HON_BONG_TAI - currentHonSoLuong) + " " + honBongTai.template.name;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else if (player.inventory.gem < player.combineNew.gemCombine) {
                    npcSay += "|2|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|7|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số chiến đấu ngẫu nhiên (10% - 25%)\n";
                    npcSay += "|2|Còn thiếu " + (player.combineNew.gemCombine - player.inventory.gem) + " ngọc";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else {
                    npcSay += "|2|Cần " + REQUIRED_HON_BONG_TAI + " " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số chiến đấu ngẫu nhiên (10% - 25%)";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\n" + player.combineNew.gemCombine + " ngọc", "Từ chối");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 Bông tai Porata cấp 2, X" + REQUIRED_HON_BONG_TAI + " Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 2, X" + REQUIRED_HON_BONG_TAI + " Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
        }
    }

    public static void nangChiSoBongTai(Player player) {
        try {
            int currentHonSoLuong = InventoryService.gI().getParam(player, ITEM_PARAM_INDEX, HON_BONG_TAI_ID);
            Item daXanhLam = InventoryService.gI().findItemBag(player, DA_XANH_LAM_ID);
            Item bongTai = player.combineNew.itemsCombine.stream()
                    .filter(item -> item != null && item.isNotNullItem() && item.template != null && item.template.id == BONG_TAI_ID)
                    .findFirst()
                    .orElse(null);

            if (bongTai == null || currentHonSoLuong < REQUIRED_HON_BONG_TAI || daXanhLam == null || daXanhLam.quantity < 1) {
                Service.gI().sendThongBao(player, "Không đủ vật phẩm để thực hiện.");
                return;
            }
            if (player.inventory.gem < player.combineNew.gemCombine) {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu " + (player.combineNew.gemCombine - player.inventory.gem) + " ngọc nữa!");
                return;
            }
            
            player.inventory.gem -= player.combineNew.gemCombine;

            if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                short optionId = UPGRADE_OPTIONS[Util.nextInt(0, UPGRADE_OPTIONS.length - 1)];
                int param = randomParamForOpt(optionId);
                
                // Xóa các option cũ trên bông tai và thêm nội tại hợp thể + option mới + cấp 2
                bongTai.itemOptions.clear();
                bongTai.itemOptions.add(new Item.ItemOption(255, 10));
                bongTai.itemOptions.add(new Item.ItemOption(optionId, param));
                bongTai.itemOptions.add(new Item.ItemOption(72, 2)); // Option cấp 2
                
                CombineService.gI().sendEffectSuccessCombine(player);
            } else {
                CombineService.gI().sendEffectFailCombine(player);
            }
            
            // Trừ vật phẩm
            InventoryService.gI().subParamItemsBag(player, HON_BONG_TAI_ID, ITEM_PARAM_INDEX, REQUIRED_HON_BONG_TAI);
            InventoryService.gI().subQuantityItemsBag(player, daXanhLam, 1);
            
            Service.gI().sendMoney(player);
            InventoryService.gI().sendItemBags(player);
            CombineService.gI().reOpenItemCombine(player);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int randomParamForOpt(short optId) {
        return switch (optId) {
            case 98, 99 -> Util.nextInt(5, 12);
            case 216 -> Util.nextInt(10, 25);
            default -> Util.nextInt(10, 22);
        };
    }
}