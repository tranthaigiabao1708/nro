package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * M1: Chuyển hóa Trang bị bằng Ngọc (5.000 Ngọc xanh)
 * - Đặt 2 trang bị cùng loại & hành tinh theo thứ tự bất kỳ.
 * - Sửa lỗi bản cũ trừ nhầm vàng thay vì ngọc xanh & kiểm tra đủ ngọc + ô trống hành trang.
 */
public class ChuyenHoaTrangBi_Ngoc {

    public static final int GEM_CHUYEN_HOA = 5000;

    public static void chuyenHoaTrangBiNgoc(Player player) {
        Item[] pair = ChuyenHoaTrangBi_Vang.resolvePair(player);
        if (pair == null) {
            return;
        }
        Item trangBiGoc = pair[0];
        Item trangBiMoi = pair[1];

        int levelTrangBi = ChuyenHoaTrangBi_Vang.getUpgradeLevel(trangBiGoc);
        int soLanRotCap = ChuyenHoaTrangBi_Vang.getDropCount(trangBiGoc);

        StringBuilder npcSay = ChuyenHoaTrangBi_Vang.buildPreviewText(trangBiGoc, trangBiMoi, levelTrangBi, soLanRotCap);
        if (player.inventory.getGem() < GEM_CHUYEN_HOA) {
            npcSay.append("|7|Cần ").append(Util.numberToMoney(GEM_CHUYEN_HOA)).append(" ngọc xanh (còn thiếu ")
                    .append(Util.numberToMoney(GEM_CHUYEN_HOA - player.inventory.getGem())).append(" ngọc)");
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Còn thiếu ngọc");
            return;
        }
        npcSay.append("|2|Chi phí: ").append(Util.numberToMoney(GEM_CHUYEN_HOA)).append(" ngọc xanh (Tỉ lệ 100%)");
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay.toString(), "Chuyển hóa\n(5.000 Ngọc)", "Từ chối");
    }

    public static void thucHienChuyenHoa(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất 1 ô trống!");
            return;
        }
        Item[] pair = ChuyenHoaTrangBi_Vang.resolvePair(player);
        if (pair == null) {
            return;
        }
        if (player.inventory.getGem() < GEM_CHUYEN_HOA) {
            Service.gI().sendThongBao(player, "Không đủ " + Util.numberToMoney(GEM_CHUYEN_HOA) + " ngọc xanh để chuyển hóa!");
            return;
        }

        Item trangBiGoc = pair[0];
        Item trangBiMoi = pair[1];
        Item result = ChuyenHoaTrangBi_Vang.createTransferredItem(trangBiGoc, trangBiMoi);

        player.inventory.subGem(GEM_CHUYEN_HOA);
        Service.gI().sendMoney(player);
        InventoryService.gI().subQuantityItemsBag(player, trangBiGoc, 1);
        InventoryService.gI().subQuantityItemsBag(player, trangBiMoi, 1);
        InventoryService.gI().addItemBag(player, result);
        InventoryService.gI().sendItemBags(player);
        player.combineNew.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Chuyển hóa bằng ngọc thành công sang [" + result.template.name + "]!");
    }
}
