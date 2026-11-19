package nro.models.combine;

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
 * M1: Chuyển hóa Trang bị bằng Vàng (Phí: 5 Thỏi vàng [457])
 * - Đặt 2 trang bị cùng loại (Áo/Quần/Găng/Giày/Rada-Nhẫn) & cùng hành tinh theo thứ tự bất kỳ (có thể đặt kèm hoặc để Thỏi vàng trong hành trang):
 *   + 1 Trang bị gốc cấp độ 1..12 đã đập từ [+4] trở lên
 *   + 1 Trang bị đích cao hơn đúng 1 bậc (tối đa bậc 13 - Đồ Thần Linh), chưa nâng cấp [+] & chưa pha lê hóa
 * - Chuyển toàn bộ cấp [+], số lần rớt cấp, lỗ sao pha lê và các Sao Pha Lê sang trang bị mới (không chuyển dòng SKH).
 */
public class ChuyenHoaTrangBi_Vang {

    public static final int THOI_VANG_ID = 457;
    public static final int REQUIRED_THOI_VANG = 5;

    public static Item[] resolvePair(Player player) {
        int size = player.combineNew.itemsCombine.size();
        if (size < 2 || size > 3) {
            Service.gI().sendThongBaoOK(player, "Cần đặt:\n- 1 trang bị gốc có cấp từ [+4] trở lên\n- 1 trang bị mới cao hơn đúng 1 bậc (chưa nâng cấp & chưa đục lỗ sao)\n- Phí: 5 Thỏi vàng");
            return null;
        }
        Item a = null;
        Item b = null;
        for (Item it : player.combineNew.itemsCombine) {
            if (it == null || !it.isNotNullItem() || !player.inventory.itemsBag.contains(it)) {
                Service.gI().sendThongBaoOK(player, "Vật phẩm không hợp lệ trong hành trang!");
                return null;
            }
            if (it.template.id == THOI_VANG_ID) {
                continue;
            }
            if (a == null) {
                a = it;
            } else if (b == null) {
                b = it;
            } else {
                Service.gI().sendThongBaoOK(player, "Chỉ đặt đúng 2 trang bị cần chuyển hóa (và tối đa 1 ô Thỏi vàng)!");
                return null;
            }
        }
        if (a == null || b == null) {
            Service.gI().sendThongBaoOK(player, "Cần đặt đủ 1 trang bị gốc từ [+4] trở lên và 1 trang bị mới cao hơn đúng 1 bậc!");
            return null;
        }
        if (a.template.type < 0 || a.template.type > 4 || b.template.type < 0 || b.template.type > 4) {
            Service.gI().sendThongBaoOK(player, "Chỉ áp dụng cho Áo, Quần, Găng, Giày hoặc Rada / Nhẫn!");
            return null;
        }
        if (SkhCollectionService.getMainSkhOptionId(a) != -1 || SkhCollectionService.getMainSkhOptionId(b) != -1) {
            Service.gI().sendThongBaoOK(player, "Không áp dụng Chuyển hóa cho trang bị Set Kích Hoạt (hãy dùng mục Nâng cấp Trang bị Kích hoạt)!");
            return null;
        }

        Item trangBiGoc = null;
        Item trangBiMoi = null;
        if (getUpgradeLevel(a) >= 4 && getUpgradeLevel(b) == 0) {
            trangBiGoc = a;
            trangBiMoi = b;
        } else if (getUpgradeLevel(b) >= 4 && getUpgradeLevel(a) == 0) {
            trangBiGoc = b;
            trangBiMoi = a;
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị gốc có cấp từ [+4] trở lên và 1 trang bị mới chưa nâng cấp [+]!");
            return null;
        }

        if (trangBiGoc.template.type != trangBiMoi.template.type
                || (trangBiMoi.template.type != 4 && trangBiGoc.template.gender != trangBiMoi.template.gender)) {
            Service.gI().sendThongBaoOK(player, "Trang bị gốc và trang bị mới bắt buộc phải cùng loại và cùng hành tinh!");
            return null;
        }

        int lvGoc = trangBiGoc.template.level;
        int lvMoi = trangBiMoi.template.level;
        if (lvGoc < 1 || lvGoc > 12 || lvMoi != lvGoc + 1 || lvMoi > 13) {
            Service.gI().sendThongBaoOK(player, "Trang bị mới phải cao hơn trang bị gốc đúng 1 bậc (tối đa chuyển từ đồ cấp 12 Lưỡng Long/Jean/Zealot lên Đồ Thần Linh cấp 13)!");
            return null;
        }

        boolean daPhaLe = trangBiMoi.itemOptions.stream()
                .anyMatch(io -> io != null && io.optionTemplate != null && (io.optionTemplate.id == 72 || io.optionTemplate.id == 102 || io.optionTemplate.id == 107));
        if (daPhaLe) {
            Service.gI().sendThongBaoOK(player, "Trang bị mới phải chưa nâng cấp [+] và chưa đục lỗ sao pha lê!");
            return null;
        }

        return new Item[]{trangBiGoc, trangBiMoi};
    }

    public static void chuyenHoaTrangBiVang(Player player) {
        Item[] pair = resolvePair(player);
        if (pair == null) {
            return;
        }
        Item trangBiGoc = pair[0];
        Item trangBiMoi = pair[1];

        int levelTrangBi = getUpgradeLevel(trangBiGoc);
        int soLanRotCap = getDropCount(trangBiGoc);
        int currentThoiVang = InventoryService.gI().getTotalQuantityBag(player, THOI_VANG_ID);

        StringBuilder npcSay = buildPreviewText(trangBiGoc, trangBiMoi, levelTrangBi, soLanRotCap);
        if (currentThoiVang < REQUIRED_THOI_VANG) {
            npcSay.append("|7|Chi phí: ").append(currentThoiVang).append("/").append(REQUIRED_THOI_VANG)
                    .append(" Thỏi vàng (còn thiếu ").append(REQUIRED_THOI_VANG - currentThoiVang).append(" Thỏi vàng)");
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Còn thiếu\nThỏi vàng");
            return;
        }
        npcSay.append("|2|Chi phí: ").append(REQUIRED_THOI_VANG).append(" Thỏi vàng (hiện có ").append(currentThoiVang).append(") - Tỉ lệ 100%");
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay.toString(), "Chuyển hóa\n(5 Thỏi vàng)", "Từ chối");
    }

    public static void thucHienChuyenHoa(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất 1 ô trống!");
            return;
        }
        Item[] pair = resolvePair(player);
        if (pair == null) {
            return;
        }
        int currentThoiVang = InventoryService.gI().getTotalQuantityBag(player, THOI_VANG_ID);
        if (currentThoiVang < REQUIRED_THOI_VANG) {
            Service.gI().sendThongBao(player, "Không đủ " + REQUIRED_THOI_VANG + " Thỏi vàng trong hành trang để chuyển hóa!");
            return;
        }

        Item trangBiGoc = pair[0];
        Item trangBiMoi = pair[1];
        Item result = createTransferredItem(trangBiGoc, trangBiMoi);

        InventoryService.gI().subQuantityItemsBagPriorityLocked(player, THOI_VANG_ID, REQUIRED_THOI_VANG);
        InventoryService.gI().subQuantityItemsBag(player, trangBiGoc, 1);
        InventoryService.gI().subQuantityItemsBag(player, trangBiMoi, 1);
        InventoryService.gI().addItemBag(player, result);
        InventoryService.gI().sendItemBags(player);
        player.combineNew.clearItemCombine();
        CombineService.gI().reOpenItemCombine(player);
        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Chuyển hóa thành công sang [" + result.template.name + "] (trừ 5 Thỏi vàng)!");
    }

    static int getUpgradeLevel(Item item) {
        if (item == null || item.itemOptions == null) {
            return 0;
        }
        for (ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == 72) {
                return io.param;
            }
        }
        return 0;
    }

    static int getDropCount(Item item) {
        int count = 0;
        if (item != null && item.itemOptions != null) {
            for (ItemOption io : item.itemOptions) {
                if (io != null && io.optionTemplate != null && io.optionTemplate.id == 209) {
                    count += io.param;
                }
            }
        }
        return count;
    }

    static StringBuilder buildPreviewText(Item trangBiGoc, Item trangBiMoi, int levelTrangBi, int soLanRotCap) {
        StringBuilder npcSay = new StringBuilder("|8|CHUYỂN HÓA TRANG BỊ (100% THÀNH CÔNG)\n");
        npcSay.append("|2|Trang bị nhận: ").append(trangBiMoi.getName()).append(" [+").append(levelTrangBi).append("]\n");
        for (ItemOption io : trangBiMoi.itemOptions) {
            if (io == null || io.optionTemplate == null || io.optionTemplate.id == 72) {
                continue;
            }
            int id = io.optionTemplate.id;
            if (isBaseStatOption(id)) {
                int scaled = Math.max(1, (int) Math.round(io.param * Math.pow(1.1, levelTrangBi) * Math.pow(0.9, soLanRotCap)));
                npcSay.append("|1|• ").append(io.getOptionString(scaled)).append("\n");
            } else if (!isIgnoredOption(id)) {
                npcSay.append("|1|• ").append(io.getOptionString()).append("\n");
            }
        }
        for (ItemOption io : trangBiGoc.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            int id = io.optionTemplate.id;
            if (!isIgnoredOption(id) && !isBaseStatOption(id) && id != 72 && id != 21 && id != 30) {
                npcSay.append("|0|• ").append(io.getOptionString()).append("\n");
            }
        }
        npcSay.append("|2|• Chuyển toàn bộ cấp [+] & Sao Pha Lê sang trang bị mới\n");
        return npcSay;
    }

    static Item createTransferredItem(Item trangBiGoc, Item trangBiMoi) {
        int levelTrangBi = getUpgradeLevel(trangBiGoc);
        int soLanRotCap = getDropCount(trangBiGoc);

        Item newItem = ItemService.gI().createNewItem(trangBiMoi.template.id);
        newItem.itemOptions.clear();

        for (ItemOption io : trangBiMoi.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            int id = io.optionTemplate.id;
            if (!isIgnoredOption(id) && id != 72) {
                int value = isBaseStatOption(id)
                        ? Math.max(1, (int) Math.round(io.param * Math.pow(1.1, levelTrangBi) * Math.pow(0.9, soLanRotCap)))
                        : io.param;
                newItem.itemOptions.add(new ItemOption(io.optionTemplate, value));
            }
        }

        for (ItemOption io : trangBiGoc.itemOptions) {
            if (io == null || io.optionTemplate == null) {
                continue;
            }
            int id = io.optionTemplate.id;
            if (!isIgnoredOption(id) && !isBaseStatOption(id) && id != 21) {
                if (id == 30 && newItem.isHaveOption(30)) {
                    continue;
                }
                newItem.itemOptions.add(new ItemOption(io.optionTemplate, io.param));
            }
        }
        return newItem;
    }

    static boolean isBaseStatOption(int id) {
        return id == 0 || id == 6 || id == 7 || id == 14 || id == 22 || id == 23 || id == 27 || id == 28 || id == 47;
    }

    static boolean isIgnoredOption(int id) {
        return SkhCollectionService.isAnySkhOptionId(id)
                || id == CuongHoaCapSkh.OPT_SKH_UPGRADE_LV
                || id == CuongHoaCapSkh.OPT_SKH_UPGRADE_PCT
                || id == 235 || id == 236
                || (id >= 210 && id <= 218)
                || (id >= 224 && id <= 227);
    }
}
