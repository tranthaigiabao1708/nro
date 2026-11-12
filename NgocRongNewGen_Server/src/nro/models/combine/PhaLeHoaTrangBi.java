package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class PhaLeHoaTrangBi {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 vật phẩm để pha lê hóa", "Đóng");
            return;
        }

        Item item = player.combineNew.itemsCombine.get(0);
        if (!CombineSystem.isTrangBiPhaLeHoa(item)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể đục lỗ", "Đóng");
            return;
        }

        int star = 0;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 107) {
                star = io.param;
            }
        }
        if (star >= CombineService.MAX_STAR_ITEM) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
            return;
        }

        QuyTrinh(player, item, star);
    }

    private static void QuyTrinh(Player player, Item item, int star) {
        player.combineNew.goldCombine = CombineSystem.getGoldPhaLeHoa(star);
        player.combineNew.gemCombine = CombineSystem.getGemPhaLeHoa(star);
        player.combineNew.ratioCombine = getFakeRatio(star);

        String npcSay = item.template.name + "\n|2|";
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id != 102) {
                npcSay += io.getOptionString().replace("$", "") + "\n";
            }
        }
        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";

        if (player.combineNew.goldCombine <= player.inventory.gold) {
            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc", "Nâng cấp 10 lần", "Nâng cấp 100 lần", "Khô máu +1");
        } else {
            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        }
    }

    public static float getRatio(int star) {
        switch (star) {
            case 0:
                return 50f;
            case 1:
                return 20f;
            case 2:
                return 10f;
            case 3:
                return 5f;
            case 4:
                return 1f;
            case 5:
                return 0.7f;
            case 6:
                return 0.5f;
            case 7:
                return 0.1f;
            case 8:
                return 0.1f;
        }

        return 0;
    }

    private static int getFakeRatio(int star) {
        return switch (star) {
            case 0 ->
                80;
            case 1 ->
                40;
            case 2 ->
                30;
            case 3 ->
                20;
            case 4 ->
                10;
            case 5 ->
                5;
            case 6 ->
                3;
            case 7 ->
                2;
            case 8 ->
                1;
            default ->
                0;
        };
    }

    public static void phaLeHoa(Player player, int... numm) {
        if (player.idMark != null && !Util.canDoWithTime(player.idMark.getLastTimeCombine(), 500)) {
            return;
        }
        player.idMark.setLastTimeCombine(System.currentTimeMillis());
        int n = numm.length > 0 ? numm[0] : 1;
        boolean isKhoMau = (n == -1);

        if (!player.combineNew.itemsCombine.isEmpty()) {
            Item item = player.combineNew.itemsCombine.get(0);
            if (item == null || !CombineSystem.isTrangBiPhaLeHoa(item)) {
                return;
            }

            int star = 0;
            Item.ItemOption optionStar = null;
            for (Item.ItemOption io : item.itemOptions) {
                if (io.optionTemplate.id == 107) {
                    star = io.param;
                    optionStar = io;
                    break;
                }
            }
            if (star >= CombineService.MAX_STAR_ITEM) {
                Service.gI().sendThongBao(player, "Đã đạt số sao tối đa");
                return;
            }

            long goldPerTry = CombineSystem.getGoldPhaLeHoa(star);
            int gemPerTry = CombineSystem.getGemPhaLeHoa(star);
            player.combineNew.goldCombine = (int) goldPerTry;
            player.combineNew.gemCombine = gemPerTry;

            if (player.inventory.gold < goldPerTry) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            } else if (player.inventory.gem < gemPerTry) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }

            float baseRatio = getRatio(star);
            boolean success = false;
            int attempts = 0;
            long totalGoldSpent = 0L;
            long totalGemSpent = 0L;
            int maxTries = isKhoMau ? 1_000_000 : n;

            for (int i = 0; i < maxTries; i++) {
                if (player.inventory.gem < gemPerTry || player.inventory.gold < goldPerTry) {
                    break;
                }
                if (goldPerTry <= 0 && gemPerTry <= 0) {
                    break;
                }

                attempts++;
                player.inventory.gold -= goldPerTry;
                player.inventory.gem -= gemPerTry;
                totalGoldSpent += goldPerTry;
                totalGemSpent += gemPerTry;

                if (Util.isTrue(baseRatio, 100)) {
                    success = true;
                    break;
                }
            }

            if (success) {
                star++;
                if (optionStar == null) {
                    item.itemOptions.add(new Item.ItemOption(107, star));
                } else {
                    optionStar.param = star;
                }
                CombineService.gI().sendEffectSuccessCombine(player);
                if (isKhoMau) {
                    Service.gI().sendThongBao(player, "Khô máu +1 thành công lên " + star + " sao sau "
                            + Util.format(attempts) + " lần đập! (-" + Util.numberToMoney(totalGoldSpent)
                            + " vàng, -" + Util.format(totalGemSpent) + " ngọc)");
                } else {
                    Service.gI().sendThongBao(player, "Chúc mừng! Đã pha lê hóa thành công lên " + star + " sao pha lê!");
                }
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                if (isKhoMau) {
                    Service.gI().sendThongBao(player, "Đã đập " + Util.format(attempts)
                            + " lần nhưng hết tài nguyên trước khi lên cấp! (-" + Util.numberToMoney(totalGoldSpent)
                            + " vàng, -" + Util.format(totalGemSpent) + " ngọc)");
                } else {
                    String[] failMessages = {
                        "Tay run à, đập kiểu gì thế?",
                        "Lại xịt rồi, hahaha...",
                        "Ngon bắt được con lợn béo rồi...!",
                        "Làm lại đi, biết đâu lần sau đỏ!",
                        "Lần sau nhớ khấn trước khi đập!",
                        "Kỹ năng quá kém?",
                        "Hên xui thôi mà, đừng cay!",
                        "Còn vàng còn ngọc, đập tiếp đi!"
                    };
                    String msg = failMessages[Util.nextInt(failMessages.length)];
                    Service.gI().sendThongBao(player, msg);
                }
            }

            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);

            if (isKhoMau) {
                String reportNpc;
                if (success) {
                    reportNpc = "|2|Khô máu +1 thành công!\n"
                            + item.template.name + " đã lên " + star + " Sao Pha Lê\n"
                            + "|1|Số lần đã đập: " + Util.format(attempts) + " lần\n"
                            + "|7|Đã trừ: " + Util.numberToMoney(totalGoldSpent) + " vàng & " + Util.format(totalGemSpent) + " ngọc";
                } else {
                    reportNpc = "|7|Khô máu +1 dừng lại do hết tài nguyên!\n"
                            + "|1|Số lần đã đập: " + Util.format(attempts) + " lần\n"
                            + "Đã trừ: " + Util.numberToMoney(totalGoldSpent) + " vàng & " + Util.format(totalGemSpent) + " ngọc";
                }
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, reportNpc, "Đóng");
            }
        }
    }
}
