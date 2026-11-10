package nro.models.combine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.SkhCollectionService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */

public class PhanRaTrangBiKichHoat {

    public class PhanRaTrangBi {

        private static final long GOLD_PHAN_RA = 200_000_000L; // 200 Tr vàng / 1 món SKH -> 1 Khoáng tái chế
        private static final int RATIO_PHAN_RA = 100;          // Tỉ lệ thành công 100%

        public static void showInfoCombine(Player player) {
            if (player.combineNew == null || player.combineNew.itemsCombine == null
                    || player.combineNew.itemsCombine.isEmpty()) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Hãy đặt ít nhất 1 Trang bị Kích Hoạt thường cần phân rã!", "Đóng");
                return;
            }

            List<Item> validItems = new ArrayList<>();
            Set<Item> seenSlots = Collections.newSetFromMap(new IdentityHashMap<>());

            for (Item it : player.combineNew.itemsCombine) {
                if (it == null || !it.isNotNullItem() || it.quantity < 1) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Có ô vật phẩm trống hoặc không hợp lệ, vui lòng chọn lại!", "Đóng");
                    return;
                }
                if (NangCapTrangBiKichHoat.isDoThanLinh(it)
                        || NangCapTrangBiKichHoat.isDoHuyDiet(it)
                        || NangCapTrangBiKichHoat.isDoThienSu(it)) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "|7|Không được đặt Trang bị Thần Linh / Hủy Diệt / Thiên Sứ vào!\n"
                            + "|2|Chỉ được phép đặt Trang bị Kích Hoạt thường:\n- Vi phạm: " + it.template.name,
                            "Đóng");
                    return;
                }
                if (!isValidItem(it)) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "|7|CHỈ ĐƯỢC PHÉP ĐẶT ĐỒ KÍCH HOẠT VÀO ĐỂ PHÂN RÃ!\n"
                            + "|2|Vật phẩm không phải Trang bị Kích Hoạt:\n- " + it.template.name,
                            "Đóng");
                    return;
                }
                if (seenSlots.add(it)) {
                    validItems.add(it);
                }
            }

            int count = validItems.size();
            if (count <= 0) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Hãy đặt ít nhất 1 Trang bị Kích Hoạt thường cần phân rã!", "Đóng");
                return;
            }

            long totalGold = GOLD_PHAN_RA * count;
            player.combineNew.goldCombine = (int) Math.min(Integer.MAX_VALUE, totalGold);
            player.combineNew.ratioCombine = RATIO_PHAN_RA;

            StringBuilder sb = new StringBuilder();
            if (count == 1) {
                Item first = validItems.get(0);
                String setName = SkhCollectionService.getSetFullName(SkhCollectionService.getMainSkhOptionId(first));
                sb.append("|2|Phân rã: ").append(first.template.name).append(" (").append(setName).append(")\n");
            } else {
                sb.append("|8|Phân rã cùng lúc: ").append(count).append(" Trang bị Kích Hoạt\n");
                int previewMax = Math.min(count, 4);
                for (int i = 0; i < previewMax; i++) {
                    Item it = validItems.get(i);
                    String setName = SkhCollectionService.getSetFullName(SkhCollectionService.getMainSkhOptionId(it));
                    sb.append("|2|- ").append(it.template.name).append(" (").append(setName).append(")\n");
                }
                if (count > previewMax) {
                    sb.append("|2|... và ").append(count - previewMax).append(" món kích hoạt khác\n");
                }
            }

            sb.append("|1|Nhận được: ").append(count).append(" Khoáng tái chế\n")
              .append("|2|Tỉ lệ thành công: ").append(RATIO_PHAN_RA).append("%\n")
              .append("|2|Tổng chi phí: ").append(Util.numberToMoney(totalGold)).append(" vàng\n");

            if (player.inventory.gold < totalGold) {
                sb.append("|7|Còn thiếu ").append(Util.numberToMoney(totalGold - player.inventory.gold)).append(" vàng");
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, sb.toString(), "Đóng");
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, sb.toString(),
                        "Phân rã x" + count + "\n(" + Util.numberToMoney(totalGold) + " vàng)", "Từ chối");
            }
        }

        public static void ThucHienPhanRa(Player player) {
            if (player.combineNew == null || player.combineNew.itemsCombine == null
                    || player.combineNew.itemsCombine.isEmpty()) {
                Service.gI().sendThongBao(player, "Hãy đặt ít nhất 1 Trang bị Kích Hoạt cần phân rã!");
                return;
            }

            List<Item> validItems = new ArrayList<>();
            Set<Item> seenSlots = Collections.newSetFromMap(new IdentityHashMap<>());

            for (Item it : player.combineNew.itemsCombine) {
                if (it == null || !it.isNotNullItem() || it.quantity < 1) {
                    Service.gI().sendThongBao(player, "Có vật phẩm không còn trong hành trang, vui lòng thử lại!");
                    return;
                }
                if (!player.inventory.itemsBag.contains(it)) {
                    Service.gI().sendThongBao(player, "Vật phẩm không nằm trong hành trang!");
                    return;
                }
                if (NangCapTrangBiKichHoat.isDoThanLinh(it)
                        || NangCapTrangBiKichHoat.isDoHuyDiet(it)
                        || NangCapTrangBiKichHoat.isDoThienSu(it)
                        || !isValidItem(it)) {
                    Service.gI().sendThongBao(player, "Chỉ được phép đặt Trang bị Kích Hoạt thường! Vi phạm: " + it.template.name);
                    return;
                }
                if (seenSlots.add(it)) {
                    validItems.add(it);
                }
            }

            int count = validItems.size();
            if (count <= 0) {
                Service.gI().sendThongBao(player, "Không có Trang bị Kích Hoạt hợp lệ để phân rã!");
                return;
            }

            long totalGold = GOLD_PHAN_RA * count;
            if (player.inventory.gold < totalGold) {
                Service.gI().sendThongBao(player, "Không đủ " + Util.numberToMoney(totalGold) + " vàng để phân rã!");
                return;
            }

            // Kiểm tra sức chứa cho Khoáng tái chế (sau khi trừ `count` món trang bị sẽ trống thêm `count` ô)
            Item existingKhoang = InventoryService.gI().findItemBag(player, 1656);
            boolean canStackExisting = existingKhoang != null && existingKhoang.quantity + count <= 99_999;
            if (!canStackExisting && InventoryService.gI().getCountEmptyBag(player) + count < 1) {
                Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong hành trang!");
                return;
            }

            player.inventory.subGold(totalGold);
            for (Item it : validItems) {
                InventoryService.gI().subQuantityItemsBag(player, it, 1);
            }

            if (Util.isTrue(RATIO_PHAN_RA, 100)) {
                Item khoang = ItemService.gI().createNewItem((short) 1656, count);
                khoang.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(player, khoang);
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player,
                        "Phân rã thành công " + count + " trang bị SKH! Nhận được " + count + " Khoáng tái chế.");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Phân rã thất bại!");
            }

            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            player.combineNew.clearItemCombine();
            CombineService.gI().reOpenItemCombine(player);
        }

        public static boolean isValidItem(Item item) {
            if (item == null || !item.isNotNullItem() || item.template == null || item.itemOptions == null) {
                return false;
            }
            if (item.template.type < 0 || item.template.type >= 5) {
                return false;
            }
            return SkhCollectionService.getMainSkhOptionId(item) != -1;
        }

    }
}
