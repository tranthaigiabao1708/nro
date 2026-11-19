package nro.models.combine;

import nro.models.consts.ConstFont;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.InventoryService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class DoiSachTuyetKy {

    private static final int REQUIRED_CUON_SACH_CU = 10;
    private static final int REQUIRED_KIM_BAM_GIAY = 1;
    private static final int REQUIRED_CON_DAU = 1;
    private static final int SUCCESS_RATE_PERCENT = 20;
    private static final int EXTRA_OPTION_CHANCE = 20;
    private static final short[] SACH_IDS = new short[]{1044, 1211, 1212};

    public static void showCombine(Player player) {
        int quantityCuonSachCu = InventoryService.gI().getTotalQuantityBag(player, 1283);
        int quantityKimBamGiay = InventoryService.gI().getTotalQuantityBag(player, 1285);
        int quantityConDau = InventoryService.gI().getTotalQuantityBag(player, 1794);

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_GREEN).append("Đổi sách Tuyệt Kỹ 1\n")
                .append(formatRequirement("Cuốn sách cũ", quantityCuonSachCu, REQUIRED_CUON_SACH_CU))
                .append(formatRequirement("Kìm bấm giấy", quantityKimBamGiay, REQUIRED_KIM_BAM_GIAY));

        boolean hasAllMaterials = quantityCuonSachCu >= REQUIRED_CUON_SACH_CU && quantityKimBamGiay >= REQUIRED_KIM_BAM_GIAY;
        text.append(hasAllMaterials ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                .append("Tỉ lệ thành công: ").append(SUCCESS_RATE_PERCENT).append("%\n");
        text.append(formatNote("Nếu dùng con dấu tỷ lệ thành công là 100%\nvà X5 tỷ lệ ra thêm dòng cho sách"));

        if (!hasAllMaterials) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Từ chối");
        } else if (quantityConDau >= REQUIRED_CON_DAU) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.DOI_SACH_TUYET_KY, text.toString(), "Đồng ý", "Dùng con dấu", "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.DOI_SACH_TUYET_KY, text.toString(), "Đồng ý", "Từ chối");
        }
    }

    private static String formatNote(String note) {
        return ConstFont.BOLD_RED + note + "\n";
    }

    private static String formatRequirement(String itemName, int current, int required) {
        return (current >= required ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                + itemName + " " + current + "/" + required + "\n";
    }

    public static void doiSachTuyetKy(Player player, boolean useConDau) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Cần 1 ô trống trong hành trang.");
            return;
        }

        Item cuonSachCu = InventoryService.gI().findItemBag(player, 1283);
        Item kimBamGiay = InventoryService.gI().findItemBag(player, 1285);
        Item conDau = useConDau ? InventoryService.gI().findItemBag(player, 1794) : null;

        if (cuonSachCu == null || InventoryService.gI().getTotalQuantityBag(player, 1283) < REQUIRED_CUON_SACH_CU
                || kimBamGiay == null || InventoryService.gI().getTotalQuantityBag(player, 1285) < REQUIRED_KIM_BAM_GIAY
                || (useConDau && (conDau == null || InventoryService.gI().getTotalQuantityBag(player, 1794) < REQUIRED_CON_DAU))) {
            Service.gI().sendThongBao(player, "Bạn không đủ nguyên liệu để đổi sách.");
            return;
        }
        if (useConDau) {
            CombineService.gI().sendAddItemCombine(player, ConstNpc.BA_HAT_MIT, cuonSachCu, kimBamGiay, conDau);
            processSuccess(player, cuonSachCu, kimBamGiay, conDau, true);
        } else {
            CombineService.gI().sendAddItemCombine(player, ConstNpc.BA_HAT_MIT, cuonSachCu, kimBamGiay);
            if (Util.isTrue(SUCCESS_RATE_PERCENT, 100)) {
                processSuccess(player, cuonSachCu, kimBamGiay, null, false);
            } else {
                processFailure(player, cuonSachCu, kimBamGiay);
            }
        }
    }

    private static void processSuccess(Player player, Item cuonSachCu, Item kimBamGiay, Item conDau, boolean useConDau) {
        InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 1283, REQUIRED_CUON_SACH_CU);
        InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 1285, REQUIRED_KIM_BAM_GIAY);
        if (useConDau) {
            InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 1794, REQUIRED_CON_DAU);
        }
        Item sachTuyetKy = createSach(player.gender, useConDau);
        InventoryService.gI().addItemBag(player, sachTuyetKy);
        InventoryService.gI().sendItemBags(player);

        CombineService.gI().sendEffSuccessVip(player, sachTuyetKy.template.iconID);

        Util.setTimeout(() -> {
            Service.gI().sendServerMessage(player, "Bạn nhận được " + sachTuyetKy.template.name);
            CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con nhé");
        }, 2000);
    }

    private static void processFailure(Player player, Item cuonSachCu, Item kimBamGiay) {
        InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 1283, REQUIRED_CUON_SACH_CU / 2);
        InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 1285, REQUIRED_KIM_BAM_GIAY);
        InventoryService.gI().sendItemBags(player);

        CombineService.gI().sendEffFailVip(player);

        String[] failMessages = {
            "Chúc con may mắn lần sau, đừng buồn con nhé.",
            "Xịt rồi! Lại tốn giấy, tiếc ghê!",
            "Con có chắc là đã cầu nguyện trước khi đập không?",
            "Hên xui mà, biết đâu lần sau ăn luôn!",
            "Không sao, nhân phẩm ngủ quên chút thôi.",
            "Bình tĩnh, rồi cũng lên!",
            "Lại xịt, hay là đi xin vía trước đã?",
            "Làm lại đi, ba tin con sẽ thành công!"
        };

        Util.setTimeout(() -> {
            CombineService.gI().baHatMit.npcChat(player, failMessages[Util.nextInt(failMessages.length)]);
        }, 2000);
    }

    private static Item createSach(byte gender, boolean useConDau) {
        short itemId = (gender >= 0 && gender < SACH_IDS.length) ? SACH_IDS[gender] : SACH_IDS[0];

        Item sach = ItemService.gI().createNewItem(itemId);
        sach.itemOptions.clear();

        if (useConDau) {
            // Dùng Con dấu: 100% ra 2 - 3 dòng chỉ số chưa giám định (X5 của 20% = 100%)
            int optionCount = Util.nextInt(2, 3);
            for (int i = 0; i < optionCount; i++) {
                sach.itemOptions.add(new Item.ItemOption(217, 0));
            }
        } else {
            // Không dùng Con dấu: 20% cơ hội ra 2 - 3 dòng, còn lại 1 dòng
            int optionCount = Util.isTrue(EXTRA_OPTION_CHANCE, 100) ? Util.nextInt(2, 3) : 1;
            for (int i = 0; i < optionCount; i++) {
                sach.itemOptions.add(new Item.ItemOption(217, 0));
            }
        }

        sach.itemOptions.add(new Item.ItemOption(21, 40));
        sach.itemOptions.add(new Item.ItemOption(30, 0));
        sach.itemOptions.add(new Item.ItemOption(87, 0));
        sach.itemOptions.add(new Item.ItemOption(219, 5));
        sach.itemOptions.add(new Item.ItemOption(212, 1000));
        sach.itemOptions.removeIf(opt -> opt.optionTemplate.id == 218);

        return sach;
    }

}
