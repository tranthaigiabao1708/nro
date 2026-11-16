package nro.models.combine;

import nro.models.consts.ConstFont;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author By Mr Blue
 */

public class GiamDinhSach {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ và bùa giám định.");
            return;
        }

        Item sachTuyetKy = null;
        Item buaGiamDinh = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item.isSachTuyetKy() || item.isSachTuyetKy2()) {
                sachTuyetKy = item;
            } else if (item.template.id == 1284) {
                buaGiamDinh = item;
            }
        }

        if (sachTuyetKy == null || buaGiamDinh == null) {
            Service.gI().sendDialogMessage(player, "Cần Sách Tuyệt Kỹ và bùa giám định.");
            return;
        }

        ItemService.gI().ensureDefaultOptions(sachTuyetKy);

        if (!sachTuyetKy.isHaveOption(217)) {
            Service.gI().sendDialogMessage(player, "Sách Tuyệt Kỹ này đã được giám định rồi! Hãy dùng Tẩy Sách nếu muốn giám định lại.");
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append(ConstFont.BOLD_GREEN)
            .append("Giám định ")
            .append(sachTuyetKy.template.name)
            .append(" ?\n")
            .append(buaGiamDinh.quantity >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
            .append("Bùa giám định ")
            .append(buaGiamDinh.quantity)
            .append("/1");

        if (buaGiamDinh.quantity < 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Còn thiếu\nBùa giám định");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, text.toString(), "Giám định", "Từ chối");
    }

    public static void giamDinhSach(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            return;
        }

        Item sachTuyetKy = null;
        Item buaGiamDinh = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item.isSachTuyetKy() || item.isSachTuyetKy2()) {
                sachTuyetKy = item;
            } else if (item.template.id == 1284) {
                buaGiamDinh = item;
            }
        }

        if (sachTuyetKy == null || buaGiamDinh == null || buaGiamDinh.quantity < 1) {
            Service.gI().sendThongBao(player, "Cần Sách Tuyệt Kỹ và ít nhất 1 Bùa giám định!");
            return;
        }

        ItemService.gI().ensureDefaultOptions(sachTuyetKy);

        if (!sachTuyetKy.isHaveOption(217)) {
            Service.gI().sendThongBao(player, "Sách Tuyệt Kỹ này đã được giám định rồi!");
            return;
        }

        int[] options = {77, 103, 50, 108, 94, 14, 80, 81, 175, 5, 214, 216};
        Set<Integer> usedOptionIds = new HashSet<>();
        for (Item.ItemOption io : sachTuyetKy.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id != 217) {
                usedOptionIds.add(io.optionTemplate.id);
            }
        }

        for (int i = 0; i < sachTuyetKy.itemOptions.size(); i++) {
            Item.ItemOption io = sachTuyetKy.itemOptions.get(i);
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == 217) {
                int randomOption = options[Util.nextInt(options.length)];
                for (int tries = 0; tries < 20 && usedOptionIds.contains(randomOption); tries++) {
                    randomOption = options[Util.nextInt(options.length)];
                }
                usedOptionIds.add(randomOption);
                int randomValue = Util.nextInt(1, 10 / Util.nextInt(1, 3));
                sachTuyetKy.itemOptions.set(i, new Item.ItemOption(randomOption, randomValue));
            }
        }

        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().subQuantityItemsBag(player, buaGiamDinh, 1);
        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
    }
}