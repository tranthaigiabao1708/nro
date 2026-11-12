package nro.models.npc_list;

import nro.models.boss.Boss;
import nro.models.consts.ConstNpc;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

public class ChiChi extends Npc {

    private static final int ITEM_THOI_VANG = 457;
    private static final int COST_HON_LAM_TO_LUC = 10;
    private static final int COST_TV_LAM_TO_LUC = 20;
    private static final int COST_HON_LUC_TO_TIM = 10;
    private static final int COST_TV_LUC_TO_TIM = 50;

    public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private String buildSoulSummary(Player player) {
        int honLam = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_LAM);
        int honLuc = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_LUC);
        int honTim = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_TIM);
        int maHon = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_MA_HON_HUY_DIET);
        int thoiVang = InventoryService.gI().getTotalQuantityBag(player, ITEM_THOI_VANG);
        return "|7|CỬA HÀNG & LUYỆN HỒN BOSS - CHI CHI"
                + "\n|2|1. Hồn Boss Lam - Đang có: " + Util.numberToMoney(honLam)
                + "\n|0|Nguồn rơi (100%): Kuku, Mập, Rambo, TĐST, Fide, Android,"
                + "\n|0|Pic-Poc-KingKong, Bojack, Super Broly, Xên, BĐKB, DT, KGHD"
                + "\n|1|2. Hồn Boss Lục - Đang có: " + Util.numberToMoney(honLuc)
                + "\n|0|Nguồn rơi (100%): Sự kiện Mabư 12h & 14h, Hirudegarn, Gấu Tướng"
                + "\n|2|3. Hồn Boss Tím - Đang có: " + Util.numberToMoney(honTim)
                + "\n|0|Nguồn rơi (100%): Cooler 1-2, Black Goku, Cumber, Baby, Fide Vàng"
                + "\n|7|4. Ma Hồn Hủy Diệt - Đang có: " + Util.numberToMoney(maHon)
                + "\n|0|Nguồn rơi độc quyền VT6: Frost, Magetta, Cabba, Caulifla (1x), Hit (2x), Champa (3x)"
                + "\n|5|Thỏi vàng hiện có: " + Util.numberToMoney(thoiVang) + " (Hồn Lam, Lục có thể Luyện Hồn nâng bậc)";
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    buildSoulSummary(player),
                    "Cửa hàng\nHồn Boss",
                    "Luyện Hồn\nNâng Bậc",
                    "Hướng dẫn\nChi tiết",
                    "Đóng");
        }
    }

    private void openRefineMenu(Player player) {
        int honLam = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_LAM);
        int honLuc = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_LUC);
        int honTim = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_TIM);
        int thoiVang = InventoryService.gI().getTotalQuantityBag(player, ITEM_THOI_VANG);
        String say = "|7|LÒ LUYỆN HỒN NÂNG BẬC (TỈ LỆ 100%)"
                + "\n|2|1) 10 Hồn Boss Lam + 20 Thỏi vàng -> 1 Hồn Boss Lục"
                + "\n|2|2) 10 Hồn Boss Lục + 50 Thỏi vàng -> 1 Hồn Boss Tím"
                + "\n|7|Lưu ý: Ma Hồn Hủy Diệt khóa độc quyền chỉ rơi từ Boss Vũ Trụ 6!"
                + "\n|0|Hiện có: " + Util.numberToMoney(honLam) + " Hồn Lam - "
                + Util.numberToMoney(honLuc) + " Hồn Lục - "
                + Util.numberToMoney(honTim) + " Hồn Tím"
                + "\n|1|Thỏi vàng trong hành trang: " + Util.numberToMoney(thoiVang);
        createOtherMenu(player, ConstNpc.MENU_CHICHI_LUYEN_HON, say,
                "Luyện x1\nHồn Lục",
                "Luyện tối đa\nHồn Lục",
                "Luyện x1\nHồn Tím",
                "Luyện tối đa\nHồn Tím",
                "Quay lại");
    }

    private void showGuide(Player player) {
        String guide = "|7|CẨM NANG HỒN BOSS & TÍN VẬT VŨ TRỤ 6"
                + "\n|2|1. Cơ chế rơi Hồn (100% đúng 1 Hồn mỗi Boss):"
                + "\n|0|- Không phụ thuộc May mắn, Cánh hay Buff vàng."
                + "\n|0|- Đá nâng cấp không rơi từ quái, đổi gói x100 viên bằng 5 Hồn Lam."
                + "\n|2|2. Luyện Hồn tại Chi Chi (100% thành công):"
                + "\n|0|- 10 Hồn Lam + 20 Thỏi vàng = 1 Hồn Lục"
                + "\n|0|- 10 Hồn Lục + 50 Thỏi vàng = 1 Hồn Tím"
                + "\n|2|3. Tín Vật Xuyên Vũ Trụ 6 (La Bàn, Hải Đồ, Lệnh Bài):"
                + "\n|0|- Bảo vật vĩnh viễn dùng hoài không mất (như Nhẫn thời không),"
                + "\n|0|mở cổng không gian 5s xuyên thẳng sang Vũ Trụ 6 & Võ Đài Siêu Cấp!";
        createOtherMenu(player, ConstNpc.IGNORE_MENU, guide, "Đóng");
    }

    private int getAvailableSoulCapacity(Player player, int targetSoulId) {
        long cap = (long) InventoryService.gI().getCountEmptyBag(player) * 99_999L;
        for (Item it : player.inventory.itemsBag) {
            if (it != null && it.isNotNullItem() && it.template.id == targetSoulId && it.quantity < 99_999) {
                cap += (99_999 - it.quantity);
            }
        }
        return (int) Math.min(99_999L, cap);
    }

    private void refineSoul(Player player, boolean toPurple, boolean maxAll) {
        int srcSoulId = toPurple ? Boss.ITEM_HON_BOSS_LUC : Boss.ITEM_HON_BOSS_LAM;
        int dstSoulId = toPurple ? Boss.ITEM_HON_BOSS_TIM : Boss.ITEM_HON_BOSS_LUC;
        int soulReq = toPurple ? COST_HON_LUC_TO_TIM : COST_HON_LAM_TO_LUC;
        int tvReq = toPurple ? COST_TV_LUC_TO_TIM : COST_TV_LAM_TO_LUC;
        String srcName = toPurple ? "Hồn Boss Lục" : "Hồn Boss Lam";
        String dstName = toPurple ? "Hồn Boss Tím" : "Hồn Boss Lục";

        int haveSoul = InventoryService.gI().getTotalQuantityBag(player, srcSoulId);
        int haveTv = InventoryService.gI().getTotalQuantityBag(player, ITEM_THOI_VANG);
        int maxBySoul = haveSoul / soulReq;
        int maxByTv = haveTv / tvReq;
        int maxCraftable = Math.min(maxBySoul, maxByTv);

        if (maxCraftable <= 0) {
            if (haveSoul < soulReq) {
                Service.gI().sendThongBao(player, "Cần ít nhất " + soulReq + " " + srcName + " (hiện có " + haveSoul + ")!");
            } else {
                Service.gI().sendThongBao(player, "Cần ít nhất " + tvReq + " Thỏi vàng (hiện có " + haveTv + ")!");
            }
            return;
        }

        int craftCount = maxAll ? maxCraftable : 1;
        int bagCap = getAvailableSoulCapacity(player, dstSoulId);
        if (bagCap <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy, hãy dọn trống ít nhất 1 ô trước khi luyện hồn!");
            return;
        }
        craftCount = Math.min(craftCount, bagCap);

        int totalSoulCost = craftCount * soulReq;
        int totalTvCost = craftCount * tvReq;

        if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, srcSoulId, totalSoulCost)) {
            Service.gI().sendThongBao(player, "Không đủ " + srcName + " để luyện hồn!");
            return;
        }
        if (!InventoryService.gI().subQuantityItemsBagPriorityLocked(player, ITEM_THOI_VANG, totalTvCost)) {
            Item refund = ItemService.gI().createNewItem((short) srcSoulId, totalSoulCost);
            refund.itemOptions.add(new Item.ItemOption(73, 0));
            InventoryService.gI().addItemBag(player, refund);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Không đủ Thỏi vàng để luyện hồn!");
            return;
        }

        Item result = ItemService.gI().createNewItem((short) dstSoulId, craftCount);
        result.itemOptions.add(new Item.ItemOption(73, 0));
        InventoryService.gI().addItemBag(player, result);
        InventoryService.gI().sendItemBags(player);
        PlayerDAO.savePlayerAsync(player);

        Service.gI().sendThongBao(player, "Luyện thành công x" + Util.numberToMoney(craftCount) + " " + dstName
                + " (-" + Util.numberToMoney(totalSoulCost) + " " + srcName
                + ", -" + Util.numberToMoney(totalTvCost) + " Thỏi vàng)!");
        openRefineMenu(player);
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> ShopService.gI().opendShop(player, "CHICHI_SOUL", true);
                case 1 -> openRefineMenu(player);
                case 2 -> showGuide(player);
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_CHICHI_LUYEN_HON) {
            switch (select) {
                case 0 -> refineSoul(player, false, false);
                case 1 -> refineSoul(player, false, true);
                case 2 -> refineSoul(player, true, false);
                case 3 -> refineSoul(player, true, true);
                case 4 -> openBaseMenu(player);
            }
        }
    }
}

