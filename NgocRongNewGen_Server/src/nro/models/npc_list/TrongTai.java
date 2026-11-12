package nro.models.npc_list;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstNpc;
import nro.models.managers.SuperRankManager;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.NpcService;
import nro.models.matches.dai_hoi_vo_thuat.SuperRankService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.shop.ShopService;
import nro.models.utils.Util;

public class TrongTai extends Npc {

    public TrongTai(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    public static String buildUniverse6BossStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append("|7|BẢNG THEO DÕI DÀN BOSS GIẢI ĐẤU VŨ TRỤ 6");
        appendBossLine(sb, BossID.FROST_VT6, "1. Frost (3 cấp - Map 173)");
        appendBossLine(sb, BossID.AUTA_MAGETTA, "2. Auta Magetta (Map 174)");
        appendBossLine(sb, BossID.CABBA_VT6, "3. Saiyan Cabba (Map 176)");
        appendBossLine(sb, BossID.CAULIFLA_VT6, "4. SSJ2 Caulifla (Map 177)");
        appendBossLine(sb, BossID.HIT_VT6, "5. Sát Thủ Hit (2x Hồn - Map 145)");
        appendBossLine(sb, BossID.CHAMPA_VT6, "6. Trùm Ẩn Champa (3x Hồn - Map 145)");
        sb.append("\n|5|Mẹo: Hạ gục Sát Thủ Hit tại Võ Đài sẽ lập tức triệu hồi Thần Hủy Diệt Champa!");
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
        } else if (bossId == BossID.CHAMPA_VT6) {
            sb.append("|0|").append(label).append(": Đang ẩn mình (Giáng lâm khi Hit gục ngã)");
        } else {
            sb.append("|0|").append(label).append(": Đang nghỉ hồi sinh");
        }
    }

    private String buildArenaGreeting(Player player) {
        int honTim = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_HON_BOSS_TIM);
        int maHon = InventoryService.gI().getTotalQuantityBag(player, Boss.ITEM_MA_HON_HUY_DIET);
        int zenoSeal = InventoryService.gI().getTotalQuantityBag(player, 2029);
        return "|7|VÕ ĐÀI LIÊN VŨ TRỤ 6 & 7 - TRỌNG TÀI GIẢI ĐẤU"
                + "\n|2|Hồn Boss Tím hiện có: " + Util.numberToMoney(honTim)
                + "\n|7|Ma Hồn Hủy Diệt hiện có: " + Util.numberToMoney(maHon)
                + "\n|5|Ấn Triệu Tập Zeno hiện có: " + Util.numberToMoney(zenoSeal)
                + "\n|0|Quy tắc thưởng Hồn Vũ Trụ 6: Frost, Magetta, Cabba, Caulifla rơi 1x Ma Hồn,"
                + "\n|0|riêng Sát Thủ Hit rơi 2x Ma Hồn và Trùm ẩn Champa rơi 3x Ma Hồn Hủy Diệt!";
    }

    private void showPhase3BridgeInfo(Player player) {
        String info = "|7|THÔNG TIN CẦU NỐI PHASE 3 - ẤN TRIỆU TẬP ZENO"
                + "\n|2|1. Vật phẩm mở cổng duy nhất sang Phase 3:"
                + "\n|0|- Ấn Triệu Tập Zeno là tín vật tối thượng duy nhất dẫn tới Thánh Địa Siêu Cấp."
                + "\n|0|- Sở hữu bằng cách đổi 120 Ma Hồn Hủy Diệt tại Cửa hàng Giải Đấu"
                + "\n|0|hoặc săn trực tiếp Trùm ẩn Thần Hủy Diệt Champa (tỉ lệ rơi hiếm 2%)."
                + "\n|2|2. Chiến trường Ngọc Rồng Siêu Cấp (Phase 3 sắp mở):"
                + "\n|0|- Nơi các Bang Hội đại chiến PK tranh đoạt 7 viên Ngọc Rồng Siêu Cấp"
                + "\n|0|(tương tự Ngọc Rồng Đen & Ngọc Rồng Namek) để triệu hồi Rồng Thần Siêu Cấp!";
        createOtherMenu(player, ConstNpc.IGNORE_MENU, info, "Đóng");
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
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    buildArenaGreeting(player),
                    "Cửa hàng\nGiải Đấu",
                    "Tình trạng\nBoss VT6",
                    "Thông tin\nẤn Zeno",
                    "Về Đảo Kamê");
        } else {
            super.openBaseMenu(player);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player) || !player.idMark.isBaseMenu()) {
            return;
        }

        if (mapId == 113) {
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
            switch (select) {
                case 0 -> ShopService.gI().opendShop(player, "TRONG_TAI_VT6", false);
                case 1 -> createOtherMenu(player, ConstNpc.IGNORE_MENU, buildUniverse6BossStatusText(), "Đóng");
                case 2 -> showPhase3BridgeInfo(player);
                case 3 -> ChangeMapService.gI().changeMapNonSpaceship(player, 5, 220, 408);
            }
        }
    }
}
