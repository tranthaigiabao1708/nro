package nro.models.combine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Chế tạo Trang bị Thiên Sứ chuẩn tại NPC Whis (Map 154 - Hành tinh Bill).
 * - Tách chuẩn bước Xem trước (showInfoCombine) và bước Bấm nút Chế tạo (cheTaoTrangBiThienSu).
 * - Phân biệt rõ Công thức thường (1071-1073: gốc 25%, +0..25% chỉ số) và Công thức VIP (1084-1086: gốc 35%, +10..35% chỉ số, +10% may mắn dòng thưởng).
 * - Đá nâng cấp cấp 1-5 (1074-1078): +10% -> +50% tỉ lệ thành công.
 * - Đá may mắn cấp 1-5 (1079-1083): +10% -> +50% tỉ lệ nổ 1-3 dòng Chỉ số thưởng (option 41).
 * - Chỉ nhân % bonus vào dòng chỉ số chính (0, 14, 22, 23, 47), không nhân nhầm vào Yêu cầu sức mạnh 30 tỉ (option 21).
 */
public class CheTaoTrangBiThienSu {

    public static final int GOLD_COST = 200_000_000;
    public static final int SHARDS_REQUIRED = 999;
    public static final int SHARDS_LOST_ON_FAIL = 99;

    private static final short[][] ANGEL_ITEM_IDS = {
        {1048, 1051, 1054, 1057, 1060}, // Trái Đất: Áo, Quần, Găng, Giày, Nhẫn
        {1049, 1052, 1055, 1058, 1061}, // Namếc
        {1050, 1053, 1056, 1059, 1062}  // Xayda
    };

    private static boolean isCongThucThuong(Item item) {
        return item != null && item.template != null && item.template.id >= 1071 && item.template.id <= 1073;
    }

    private static boolean isCongThucVipOnly(Item item) {
        return item != null && item.template != null && item.template.id >= 1084 && item.template.id <= 1086;
    }

    private static boolean isAnyCongThuc(Item item) {
        return isCongThucThuong(item) || isCongThucVipOnly(item);
    }

    private static String getGenderName(int gender) {
        return switch (gender) {
            case 0 -> "Trái Đất";
            case 1 -> "Namếc";
            case 2 -> "Xayda";
            default -> "Tự động";
        };
    }

    private static Npc resolveWhisNpc(Player player) {
        if (player.idMark != null && player.idMark.getNpcChose() != null) {
            return player.idMark.getNpcChose();
        }
        return CombineService.gI().whis;
    }

    private static class ParsedIngredients {
        Item congThuc;
        Item manhTS;
        Item daNangCap;
        Item daMayMan;
        String errorMessage;
    }

    private static ParsedIngredients parseAndValidate(Player player, boolean checkInBag) {
        ParsedIngredients res = new ParsedIngredients();
        List<Item> list = player.combineNew != null ? player.combineNew.itemsCombine : null;
        if (list == null || list.isEmpty()) {
            res.errorMessage = "Vui lòng đặt Công thức, 999 Mảnh Thiên Sứ (và Đá nâng cấp / Đá may mắn nếu có) vào ô!";
            return res;
        }
        if (list.size() < 2 || list.size() > 4) {
            res.errorMessage = "Số ô nguyên liệu không hợp lệ (chỉ đặt từ 2 đến 4 ô:\n"
                    + "1 Công thức + 999 Mảnh Thiên Sứ + tối đa 1 Đá nâng cấp + 1 Đá may mắn)!";
            return res;
        }

        List<Item> seenRefs = new ArrayList<>();
        for (Item item : list) {
            if (item == null || !item.isNotNullItem()) {
                res.errorMessage = "Có ô vật phẩm rỗng không hợp lệ!";
                return res;
            }
            if (seenRefs.contains(item)) {
                res.errorMessage = "Không được đặt trùng 1 ô vật phẩm nhiều lần!";
                return res;
            }
            seenRefs.add(item);

            if (checkInBag && InventoryService.gI().getIndexItemBag(player, item) == -1) {
                res.errorMessage = "Nguyên liệu không còn nằm trong hành trang!";
                return res;
            }

            if (isAnyCongThuc(item)) {
                if (res.congThuc != null) {
                    res.errorMessage = "Chỉ được đặt đúng 1 Công thức (Thường hoặc VIP)!";
                    return res;
                }
                res.congThuc = item;
            } else if (item.isManhTS()) {
                if (res.manhTS != null) {
                    res.errorMessage = "Chỉ được đặt đúng 1 loại Mảnh Thiên Sứ!";
                    return res;
                }
                res.manhTS = item;
            } else if (item.isDaNangCap()) {
                if (res.daNangCap != null) {
                    res.errorMessage = "Chỉ được đặt tối đa 1 viên Đá nâng cấp!";
                    return res;
                }
                res.daNangCap = item;
            } else if (item.isDaMayMan()) {
                if (res.daMayMan != null) {
                    res.errorMessage = "Chỉ được đặt tối đa 1 viên Đá may mắn!";
                    return res;
                }
                res.daMayMan = item;
            } else {
                res.errorMessage = "Vật phẩm [" + item.template.name + "] không dùng để chế tạo Thiên Sứ!";
                return res;
            }
        }

        if (res.congThuc == null) {
            res.errorMessage = "Thiếu Công thức hoặc Công thức VIP!";
            return res;
        }
        if (res.manhTS == null) {
            res.errorMessage = "Thiếu Mảnh Thiên Sứ!";
            return res;
        }
        return res;
    }

    private static int calcSuccessRate(Item congThuc, Item daNangCap) {
        int rate = isCongThucVipOnly(congThuc) ? 35 : 25;
        if (daNangCap != null) {
            rate += (daNangCap.template.id - 1073) * 10; // Cấp 1..5 -> +10%..+50%
        }
        return Math.min(100, rate);
    }

    private static int calcLuckyBonusRate(Item congThuc, Item daMayMan) {
        int luckyRate = isCongThucVipOnly(congThuc) ? 10 : 0;
        if (daMayMan != null) {
            luckyRate += (daMayMan.template.id - 1078) * 10; // Cấp 1..5 -> +10%..+50%
        }
        return Math.min(100, luckyRate);
    }

    private static int rollMainStatBonusPercent(boolean isVipRecipe) {
        int r = Util.nextInt(1, 100);
        if (isVipRecipe) {
            if (r <= 50) {
                return Util.nextInt(10, 19);
            } else if (r <= 85) {
                return Util.nextInt(20, 29);
            } else {
                return Util.nextInt(30, 35);
            }
        } else {
            if (r <= 50) {
                return Util.nextInt(0, 10);
            } else if (r <= 85) {
                return Util.nextInt(11, 19);
            } else {
                return Util.nextInt(20, 25);
            }
        }
    }

    private static int rollBonusOptionCount(Item daMayMan) {
        int stoneLv = daMayMan != null ? (daMayMan.template.id - 1078) : 1;
        int r = Util.nextInt(1, 100);
        if (stoneLv >= 4) {
            if (r <= 35) {
                return 3;
            } else if (r <= 75) {
                return 2;
            }
            return 1;
        } else if (stoneLv >= 2) {
            if (r <= 15) {
                return 3;
            } else if (r <= 55) {
                return 2;
            }
            return 1;
        } else {
            if (r <= 5) {
                return 3;
            } else if (r <= 30) {
                return 2;
            }
            return 1;
        }
    }

    public static void showInfoCombine(Player player) {
        Npc npc = resolveWhisNpc(player);
        ParsedIngredients ing = parseAndValidate(player, false);
        if (ing.errorMessage != null) {
            if (npc != null) {
                npc.createOtherMenu(player, ConstNpc.IGNORE_MENU, "|7|" + ing.errorMessage, "Đóng");
            } else {
                Service.gI().sendThongBao(player, ing.errorMessage);
            }
            return;
        }

        boolean isVip = isCongThucVipOnly(ing.congThuc);
        int targetGender = ing.congThuc.template.gender > 2 ? player.gender : ing.congThuc.template.gender;
        int successRate = calcSuccessRate(ing.congThuc, ing.daNangCap);
        int luckyRate = calcLuckyBonusRate(ing.congThuc, ing.daMayMan);
        boolean enoughShards = ing.manhTS.quantity >= SHARDS_REQUIRED;
        boolean enoughGold = player.inventory.gold >= GOLD_COST;

        StringBuilder sb = new StringBuilder();
        sb.append("|1|Chế tạo ").append(ing.manhTS.typeNameManh()).append(" Thiên Sứ (")
                .append(getGenderName(targetGender)).append(")\n");
        sb.append("|2|- ").append(ing.congThuc.template.name).append(": 1/1\n");
        sb.append(enoughShards ? "|2|" : "|7|")
                .append("- ").append(ing.manhTS.template.name).append(": ")
                .append(ing.manhTS.quantity).append("/").append(SHARDS_REQUIRED).append("\n");
        if (ing.daNangCap != null) {
            sb.append("|2|- Đá nâng cấp ").append(ing.daNangCap.typeDanangcap())
                    .append(" (+").append((ing.daNangCap.template.id - 1073) * 10).append("% tỉ lệ)\n");
        } else {
            sb.append("|0|- Không dùng Đá nâng cấp\n");
        }
        if (ing.daMayMan != null) {
            sb.append("|2|- Đá may mắn ").append(ing.daMayMan.typeDaMayman())
                    .append(" (+").append((ing.daMayMan.template.id - 1078) * 10).append("% may mắn)\n");
        } else {
            sb.append("|0|- Không dùng Đá may mắn\n");
        }
        sb.append("|1|Tỉ lệ thành công: ").append(successRate).append("%\n");
        sb.append("|4|Chỉ số gốc cộng thêm: ").append(isVip ? "+10% đến +35%" : "+0% đến +25%").append("\n");
        sb.append("|4|Tỉ lệ nổ 1-3 dòng Chỉ số thưởng: ").append(luckyRate).append("%\n");
        sb.append(enoughGold ? "|2|" : "|7|")
                .append("Chi phí: ").append(Util.numberToMoney(GOLD_COST)).append(" vàng\n");
        sb.append("|7|(Thất bại mất Công thức, Đá hỗ trợ và ").append(SHARDS_LOST_ON_FAIL).append(" mảnh)");

        if (!enoughShards || !enoughGold) {
            if (npc != null) {
                npc.createOtherMenu(player, ConstNpc.IGNORE_MENU, sb.toString(), "Đóng");
            } else {
                Service.gI().sendThongBao(player, "Chưa đủ nguyên liệu hoặc vàng để chế tạo!");
            }
            return;
        }

        if (npc != null) {
            npc.createOtherMenu(player, CombineService.CHE_TAO_TRANG_BI_THIEN_SU,
                    sb.toString(), "Chế tạo", "Từ chối");
        }
    }

    public static void cheTaoTrangBiThienSu(Player player) {
        if (!player.setClothes.checkSetDes()) {
            Service.gI().sendThongBao(player, "Bạn cần mặc đủ 5 món Đồ Hủy Diệt trên người để chế tạo!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống trong hành trang!");
            return;
        }
        ParsedIngredients ing = parseAndValidate(player, true);
        if (ing.errorMessage != null) {
            Service.gI().sendThongBao(player, ing.errorMessage);
            return;
        }
        if (ing.manhTS.quantity < SHARDS_REQUIRED) {
            Service.gI().sendThongBao(player, "Không đủ " + SHARDS_REQUIRED + " " + ing.manhTS.template.name + "!");
            return;
        }
        if (player.inventory.gold < GOLD_COST) {
            Service.gI().sendThongBao(player, "Không đủ " + Util.numberToMoney(GOLD_COST) + " vàng để thực hiện!");
            return;
        }
        byte manhIndex = ing.manhTS.typeIdManh();
        if (manhIndex < 0 || manhIndex >= 5) {
            Service.gI().sendThongBao(player, "Mảnh Thiên Sứ không hợp lệ!");
            return;
        }

        boolean isVip = isCongThucVipOnly(ing.congThuc);
        int targetGender = ing.congThuc.template.gender > 2 ? player.gender : ing.congThuc.template.gender;
        int successRate = calcSuccessRate(ing.congThuc, ing.daNangCap);
        int luckyRate = calcLuckyBonusRate(ing.congThuc, ing.daMayMan);

        // Trừ phí vàng trước
        player.inventory.gold -= GOLD_COST;

        boolean isSuccess = Util.nextInt(1, 100) <= successRate;
        if (isSuccess) {
            short targetItemId = ANGEL_ITEM_IDS[targetGender][manhIndex];
            Item itemTS = ItemService.gI().DoThienSu(targetItemId, targetGender);

            // 1. Cộng thêm % chỉ số chính chuẩn (+0..25% CT thường, +10..35% CT VIP), KHÔNG nhân vào option 21 (SM 30 tỉ) & option 30
            int mainBonusPct = rollMainStatBonusPercent(isVip);
            if (mainBonusPct > 0) {
                for (ItemOption io : itemTS.itemOptions) {
                    if (io != null && io.optionTemplate != null
                            && InventoryService.gI().optionCanUpgrade(io.optionTemplate.id)) {
                        int delta = Math.max(1, Math.round(io.param * mainBonusPct / 100.0f));
                        io.param += delta;
                    }
                }
            }

            // 2. Xét nổ 1-3 dòng Chỉ số thưởng chuẩn (option 41: Chỉ số thưởng +N:)
            int bonusLines = 0;
            if (luckyRate > 0 && Util.nextInt(1, 100) <= luckyRate) {
                bonusLines = rollBonusOptionCount(ing.daMayMan);
                itemTS.itemOptions.add(new ItemOption(41, bonusLines));
                List<Integer> pool = new ArrayList<>(Arrays.asList(50, 77, 103, 94, 5));
                for (int i = 0; i < bonusLines && !pool.isEmpty(); i++) {
                    int pickIdx = Util.nextInt(0, pool.size() - 1);
                    int optId = pool.remove(pickIdx);
                    int optParam = isVip ? Util.nextInt(2, 5) : Util.nextInt(1, 4);
                    itemTS.itemOptions.add(new ItemOption(optId, optParam));
                }
            }

            // Trừ đủ 999 mảnh + công thức + đá hỗ trợ
            InventoryService.gI().subQuantityItemsBag(player, ing.congThuc, 1);
            InventoryService.gI().subQuantityItemsBag(player, ing.manhTS, SHARDS_REQUIRED);
            if (ing.daNangCap != null) {
                InventoryService.gI().subQuantityItemsBag(player, ing.daNangCap, 1);
            }
            if (ing.daMayMan != null) {
                InventoryService.gI().subQuantityItemsBag(player, ing.daMayMan, 1);
            }

            InventoryService.gI().addItemBag(player, itemTS);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Chúc mừng! Bạn chế tạo thành công " + itemTS.template.name
                    + " (+" + mainBonusPct + "% chỉ số gốc"
                    + (bonusLines > 0 ? ", +" + bonusLines + " dòng thưởng" : "") + ")!");
        } else {
            // Thất bại: mất Công thức + Đá hỗ trợ + chỉ mất 99 Mảnh Thiên Sứ
            InventoryService.gI().subQuantityItemsBag(player, ing.congThuc, 1);
            InventoryService.gI().subQuantityItemsBag(player, ing.manhTS, SHARDS_LOST_ON_FAIL);
            if (ing.daNangCap != null) {
                InventoryService.gI().subQuantityItemsBag(player, ing.daNangCap, 1);
            }
            if (ing.daMayMan != null) {
                InventoryService.gI().subQuantityItemsBag(player, ing.daMayMan, 1);
            }
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Chế tạo thất bại! Bạn bị mất Công thức, Đá hỗ trợ và "
                    + SHARDS_LOST_ON_FAIL + " " + ing.manhTS.template.name + ".");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        player.combineNew.itemsCombine.clear();
        CombineService.gI().reOpenItemCombine(player);
    }
}
