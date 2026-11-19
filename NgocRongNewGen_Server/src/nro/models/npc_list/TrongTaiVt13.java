package nro.models.npc_list;

import java.util.ArrayList;
import java.util.List;
import nro.models.boss.vu_tru_13.ThanHuyDietVt13;
import nro.models.consts.ConstNpc;
import nro.models.database.SuperRankDAO;
import nro.models.map.service.ChangeMapService;
import nro.models.matches.dai_hoi_vo_thuat.SuperRankService;
import nro.models.matches.giai_dau.DaiHoiVt13Manager;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class TrongTaiVt13 extends Npc {

    private static final int MENU_ADMIN_TOOLS = 20360;

    public TrongTaiVt13(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        if (player.superRank != null && player.superRank.rank < 1) {
            SuperRankDAO.loadData(player);
        }

        String rankLine = (player.superRank != null && player.superRank.rank > 0)
                ? "\n|2|★ Hạng Khiêu Chiến hiện tại của bạn: Hạng #" + player.superRank.rank + " (Vé miễn phí: " + player.superRank.ticket + ")"
                : "";
        String bossLine = "\n|7|★ Boss Thần Hủy Diệt Bunny (100 Tỷ HP): " + ThanHuyDietVt13.getScheduleStatusText();

        List<String> opts = new ArrayList<>();
        if (DaiHoiVt13Manager.gI().hasPendingPrize(player)) {
            opts.add("Nhận Thưởng\nGiải Đấu!");
        }
        if (DaiHoiVt13Manager.gI().getState() == DaiHoiVt13Manager.STATE_REGISTERING && DaiHoiVt13Manager.gI().isRegistered(player)) {
            opts.add("Hủy Đăng Ký\nGiải Đấu");
        } else {
            opts.add("Đăng Ký\nGiải 20h00");
        }
        opts.add("Thách Đấu\nLeo Hạng");
        opts.add("Bảng Xếp Hạng\nTop 100");
        if (player.isAdmin()) {
            opts.add("[Admin] Mở Giải\n& Gọi Boss");
        }
        opts.add("Về Thánh Điện\n(Map Home)");

        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "|8|TRỌNG TÀI LIÊN VŨ TRỤ - ĐẠI HỘI VÕ THUẬT VT13 (MAP -1)\n"
                + DaiHoiVt13Manager.gI().getStatusText(player)
                + rankLine
                + bossLine,
                opts.toArray(new String[0]));
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        int menuIdx = player.idMark.getIndexMenu();
        if (player.idMark.isBaseMenu()) {
            boolean hasPrize = DaiHoiVt13Manager.gI().hasPendingPrize(player);
            int idx = select;
            if (hasPrize) {
                if (idx == 0) {
                    DaiHoiVt13Manager.gI().claimPendingPrize(player);
                    return;
                }
                idx--;
            }
            if (idx == 0) {
                if (DaiHoiVt13Manager.gI().getState() == DaiHoiVt13Manager.STATE_REGISTERING
                        && DaiHoiVt13Manager.gI().isRegistered(player)) {
                    DaiHoiVt13Manager.gI().unregisterPlayer(player);
                } else {
                    DaiHoiVt13Manager.gI().registerPlayer(player);
                }
            } else if (idx == 1) {
                SuperRankService.gI().topList(player, 2);
            } else if (idx == 2) {
                SuperRankService.gI().topList(player, 0);
            } else if (player.isAdmin() && idx == 3) {
                this.createOtherMenu(player, MENU_ADMIN_TOOLS,
                        "|8|[ADMIN] ĐIỀU KHIỂN NHANH ĐẠI HỘI VT13 & BOSS THEO GIỜ\n"
                        + "|2|• Mở Đăng Ký Ngay (90s): Cho phép test đăng ký 1 người (auto Top 1) hoặc nhiều người ghép cặp 1v1.\n"
                        + "• Khai Mạc Ghép Cặp Ngay: Bắt đầu vòng đấu lập tức không cần chờ hết giờ đăng ký.\n"
                        + "• Triệu Hồi Thần Hủy Diệt Bunny: Gọi ngay Boss 100 Tỷ HP tại Điện Thờ (Map 180).",
                        "Mở Đăng Ký\nNgay (90s)",
                        "Khai Mạc\nGhép Cặp Ngay",
                        "Triệu Hồi Boss\nBunny (Map 180)",
                        "Quay lại");
            } else {
                ChangeMapService.gI().changeMapNonSpaceship(player, VuTru13Service.MAP_THANH_DIEN_ZENO_VT13, 120, 456);
            }
        } else if (menuIdx == MENU_ADMIN_TOOLS && player.isAdmin()) {
            switch (select) {
                case 0 -> DaiHoiVt13Manager.gI().openRegistrationWindow(90, true);
                case 1 -> DaiHoiVt13Manager.gI().forceStartBracketByAdmin(player);
                case 2 -> ThanHuyDietVt13.summonNowByAdmin(player);
                default -> openBaseMenu(player);
            }
        }
    }
}
