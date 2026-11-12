package nro.models.npc_list;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.boss.vodai_vt6.VoDaiVt6Boss;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstNpc;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.managers.SuperRankManager;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.NpcService;
import nro.models.matches.dai_hoi_vo_thuat.SuperRankService;
import nro.models.matches.dai_hoi_vo_thuat.VoDaiVuTru6Service;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

public class TrongTai extends Npc {

    private static final int MENU_CLAIM_SUPER_CHEST = 14501;

    private static final String TUTORIAL_VODAI_VT6 =
            "Giải Đấu Liên Vũ Trụ 6 & 7 quy tụ 9 vòng khiêu chiến liên hoàn:\b"
            + "Vòng 1-3: Frost (Dạng 1 -> Xung Kích -> Hoàn Thiện)\b"
            + "Vòng 4: Auta Magetta | Vòng 5: Saiyan Cabba (SSJ)\b"
            + "Vòng 6-7: Caulifla -> SSJ2 Caulifla\b"
            + "Vòng 8: Sát Thủ Hit | Vòng 9: Thần Hủy Diệt Champa!\n"
            + "Quyền lợi kép đặc biệt:\b"
            + "1) Hạ mỗi Boss trên đài RƠI ĐỒ TRỰC TIẾP (Thỏi vàng, Ma Hồn Hủy Diệt, tỉ lệ Đồ Thần, Hematite, Đá & Công thức Thiên Sứ VIP, Ấn Zeno, Cải trang VIP) và tự hút vào túi sau 5s!\b"
            + "2) Mỗi vòng vượt qua nâng cấp RƯƠNG SIÊU CẤP (tối đa Cấp 9/9) chứa thêm Thỏi vàng, Ngọc, Ma Hồn, Rương SPL VIP & báu vật Thiên Sứ!";

    public TrongTai(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    public static String buildUniverse6BossStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append("|7|BẢNG THEO DÕI DÀN BOSS VŨ TRỤ 6");
        appendBossLine(sb, BossID.FROST_VT6, "1. Frost (3 cấp - Map 173)");
        appendBossLine(sb, BossID.AUTA_MAGETTA, "2. Auta Magetta (Map 174)");
        appendBossLine(sb, BossID.CABBA_VT6, "3. Saiyan Cabba (Map 176)");
        appendBossLine(sb, BossID.CAULIFLA_VT6, "4. SSJ2 Caulifla (Map 177)");
        sb.append("\n|2|5. Sát Thủ Hit & Thần Hủy Diệt Champa: Đấu trực tiếp tại Vòng 8 & Vòng 9 Giải Đấu Võ Đài Siêu Cấp (Map 145)!");
        return sb.toString();
    }

    private static void appendBossLine(StringBuilder sb, int bossId, String label) {
        Boss found = null;
        for (Boss b : BossManager.gI().getBosses()) {
            if (b != null && b.id == bossId) {
                found = b;
                if (b.zone != null && !b.isDie()) {
                    break;
                }
            }
        }
        sb.append("\n");
        if (found != null && found.zone != null && !found.isDie() && found.bossStatus != BossStatus.REST) {
            int pct = found.nPoint != null ? found.nPoint.getCurrPercentHP() : 100;
            sb.append("|2|").append(label).append(": Đang xuất hiện tại ")
                    .append(found.zone.map.mapName).append(" Khu ").append(found.zone.zoneId)
                    .append(" (Còn ").append(pct).append("% HP)");
        } else {
            sb.append("|0|").append(label).append(": Đang nghỉ hồi sinh");
        }
    }

    private void refreshDailyArenaFeesIfNeeded(Player player) {
        if (player.goldSuperChallenge <= 0) {
            player.goldSuperChallenge = 50_000_000L;
        }
        if (player.gemSuperChallenge <= 0) {
            player.gemSuperChallenge = 10L;
        }
        if (Util.isAfterMidnight(player.lastTimePKVoDaiVt6)) {
            player.goldSuperChallenge = 50_000_000L;
            player.gemSuperChallenge = 10L;
        }
    }

    private String buildArenaGreeting(Player player) {
        int honTim = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_TIM);
        int maHon = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_MA_HON_HUY_DIET);
        int zenoSeal = InventoryService.gI().getTotalQuantityBag(player, 2029);
        int lv = Math.max(0, Math.min(VoDaiVt6Boss.TOTAL_ROUNDS, player.levelSuperChest));
        String progressLine = (lv >= VoDaiVt6Boss.TOTAL_ROUNDS)
                ? "ĐÃ VÔ ĐỊCH 9/9 VÒNG (Sẵn sàng nhận Rương Siêu Cấp Cấp 9!)"
                : ("Cấp " + lv + "/9 - Đối thủ kế tiếp: Vòng " + (lv + 1) + " (" + VoDaiVt6Boss.getRoundFighterTitle(lv) + ")");
        return "|7|VÕ ĐÀI LIÊN VŨ TRỤ 6 & 7 - ĐẤU TRƯỜNG 9 VÒNG"
                + "\n|2|Tiến độ Rương Siêu Cấp: " + progressLine
                + "\n|1|Ma Hồn Hủy Diệt: " + Util.numberToMoney(maHon)
                + " | Hồn Tím: " + Util.numberToMoney(honTim)
                + " | Ấn Zeno: " + Util.numberToMoney(zenoSeal)
                + "\n|0|Hạ mỗi Boss trên võ đài đều rơi trực tiếp vật phẩm + tăng cấp Rương Siêu Cấp!";
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (mapId == 113) {
            if (SuperRankManager.gI().awaiting(player)) {
                int ordinal = SuperRankManager.gI().ordinal(player.id);
                createOtherMenu(player, ConstNpc.BASE_MENU, "Vui lòng chờ, số thứ tự của bạn là " + ordinal, "OK", "Về\nĐại Hội\nVõ Thuật");
            } else {
                String ticketMsg = player.superRank.ticket > 0 ? "Miễn phí\nCòn " + player.superRank.ticket + " vé" : "Thi đấu";
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Đại hội võ thuật Siêu Hạng\ndiễn ra 24/7 kể cả ngày lễ và chủ nhật\nHãy thi đấu ngay để khẳng định đẳng cấp của mình nhé",
                        "Top 100\nCao Thủ", "Hướng\ndẫn\nthêm", ticketMsg, "Ưu tiên\nđấu ngay", "Về\nĐại Hội\nVõ Thuật");
            }
        } else if (mapId == 145) {
            if (player.isPKDHVT) {
                Service.gI().sendThongBao(player, "Bạn đang trong trận thi đấu Võ Đài!");
                return;
            }
            refreshDailyArenaFeesIfNeeded(player);
            String goldBtn = "Thi đấu\n" + Util.numberToMoney(player.goldSuperChallenge) + " vàng";
            String gemBtn = "Thi đấu\n" + Util.numberToMoney(player.gemSuperChallenge) + " ngọc";
            if (player.levelSuperChest > 0) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        buildArenaGreeting(player),
                        "Hướng dẫn\nGiải Đấu",
                        goldBtn,
                        gemBtn,
                        "Nhận thưởng\nRương Siêu Cấp\nCấp " + player.levelSuperChest,
                        "Cửa hàng\nGiải Đấu",
                        "Về Đảo Kamê");
            } else {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        buildArenaGreeting(player),
                        "Hướng dẫn\nGiải Đấu",
                        goldBtn,
                        gemBtn,
                        "Cửa hàng\nGiải Đấu",
                        "Tình trạng\nBoss VT6",
                        "Về Đảo Kamê");
            }
        } else {
            super.openBaseMenu(player);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (mapId == 113) {
            if (!player.idMark.isBaseMenu()) {
                return;
            }
            if (SuperRankManager.gI().awaiting(player)) {
                if (select == 1) {
                    ChangeMapService.gI().changeMapNonSpaceship(player, 52, player.location.x, 336);
                }
                return;
            }

            switch (select) {
                case 0 -> SuperRankService.gI().topList(player, 0);
                case 1 -> NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.THONG_TIN_SIEU_HANG);
                case 2 -> SuperRankService.gI().topList(player, 1);
                case 3 -> SuperRankService.gI().topList(player, 2);
                case 4 -> ChangeMapService.gI().changeMapNonSpaceship(player, 52, player.location.x, 336);
            }
        } else if (mapId == 145) {
            if (player.isPKDHVT) {
                return;
            }
            int idxMenu = player.idMark.getIndexMenu();
            if (idxMenu == ConstNpc.BASE_MENU) {
                refreshDailyArenaFeesIfNeeded(player);
                boolean hasChestProgress = player.levelSuperChest > 0;
                if (select == 0) {
                    NpcService.gI().createTutorial(player, tempId, avartar, TUTORIAL_VODAI_VT6);
                } else if (select == 1 || select == 2) {
                    handleStartArenaChallenge(player, select == 1);
                } else if (hasChestProgress && select == 3) {
                    createOtherMenu(player, MENU_CLAIM_SUPER_CHEST,
                            "|7|NHẬN THƯỞNG RƯƠNG SIÊU CẤP"
                            + "\n|2|Phần thưởng hiện tại của bạn đang ở Cấp " + player.levelSuperChest + " / " + VoDaiVt6Boss.TOTAL_ROUNDS
                            + "\n|0|- Sau khi nhận rương, tiến độ vòng sẽ đặt lại về 0 để bạn mở rương lấy thưởng."
                            + "\n|5|Bạn có chắc chắn muốn chốt nhận Rương Siêu Cấp Cấp " + player.levelSuperChest + " ngay bây giờ?",
                            "Nhận Rương", "Từ chối");
                } else if ((hasChestProgress && select == 4) || (!hasChestProgress && select == 3)) {
                    ShopService.gI().opendShop(player, "TRONG_TAI_VT6", false);
                } else if (!hasChestProgress && select == 4) {
                    createOtherMenu(player, ConstNpc.IGNORE_MENU, buildUniverse6BossStatusText(), "Đóng");
                } else if (select == 5) {
                    ChangeMapService.gI().changeMapNonSpaceship(player, 5, 220, 408);
                }
            } else if (idxMenu == MENU_CLAIM_SUPER_CHEST && select == 0) {
                handleClaimSuperChest(player);
            }
        }
    }

    private void handleStartArenaChallenge(Player player, boolean payWithGold) {
        if (player.levelSuperChest >= VoDaiVt6Boss.TOTAL_ROUNDS) {
            Service.gI().sendThongBao(player, "Bạn đã vô địch cả 9/9 vòng! Hãy nhận thưởng Rương Siêu Cấp Cấp 9 trước nhé!");
            return;
        }
        if (!InventoryService.gI().findItemSuperChest(player)) {
            Service.gI().sendThongBao(player, "Hãy mở Rương Siêu Cấp đang có trong hành trang hoặc rương đồ trước khi thi đấu tiếp!");
            return;
        }
        long goldCost = player.goldSuperChallenge;
        long gemCost = player.gemSuperChallenge;
        if (payWithGold) {
            if (player.inventory.gold < goldCost) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                        + Util.numberToMoney(goldCost - player.inventory.gold) + " vàng!");
                return;
            }
            if (VoDaiVuTru6Service.gI().startChallenge(player)) {
                player.inventory.gold -= goldCost;
                PlayerService.gI().sendInfoHpMpMoney(player);
                player.goldSuperChallenge = Math.min(1_500_000_000L, player.goldSuperChallenge * 2L);
                player.gemSuperChallenge = Math.min(200L, player.gemSuperChallenge + 10L);
            }
        } else {
            if (player.inventory.gem < gemCost) {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu "
                        + Util.numberToMoney(gemCost - player.inventory.gem) + " ngọc!");
                return;
            }
            if (VoDaiVuTru6Service.gI().startChallenge(player)) {
                player.inventory.gem -= (int) gemCost;
                PlayerService.gI().sendInfoHpMpMoney(player);
                player.goldSuperChallenge = Math.min(1_500_000_000L, player.goldSuperChallenge * 2L);
                player.gemSuperChallenge = Math.min(200L, player.gemSuperChallenge + 10L);
            }
        }
    }

    private void handleClaimSuperChest(Player player) {
        if (player.levelSuperChest <= 0) {
            Service.gI().sendThongBao(player, "Bạn chưa vượt qua vòng đấu nào để nhận Rương Siêu Cấp!");
            return;
        }
        if (!InventoryService.gI().findItemSuperChest(player)) {
            Service.gI().sendThongBao(player, "Hãy mở Rương Siêu Cấp đang có trước khi nhận rương mới!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            this.npcChat(player, "Hành trang đã đầy, cần ít nhất 1 ô trống để nhận Rương Siêu Cấp!");
            return;
        }
        int lv = Math.min(VoDaiVt6Boss.TOTAL_ROUNDS, player.levelSuperChest);
        Item chest = ItemService.gI().createNewItem((short) 2030);
        chest.itemOptions.add(new Item.ItemOption(72, lv));
        chest.itemOptions.add(new Item.ItemOption(30, 0));
        chest.createTime = System.currentTimeMillis();
        InventoryService.gI().addItemBag(player, chest);
        InventoryService.gI().sendItemBags(player);
        player.levelSuperChest = 0;
        PlayerDAO.savePlayerAsync(player);
        NpcService.gI().createMenuConMeo(player, -1, -1,
                "Bạn nhận được\n|1|Rương Siêu Cấp (Cấp " + lv + ")\n|2|Hãy mở ngay trong hành trang để thu hoạch kho báu Liên Vũ Trụ!",
                "OK");
    }
}
