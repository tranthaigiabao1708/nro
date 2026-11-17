package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class BeerusVt13 extends Npc {

    private static final int MENU_CHALLENGE_BOSS_VT13 = 20321;

    public BeerusVt13(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
        // Nếu người chơi chưa xem phân cảnh Tháp Ai Cập vỡ thì kích hoạt ngay
        if ((player.vuTru13Stage & VuTru13Service.STAGE_FLAG_KHE_NUT_INTRO) == 0) {
            VuTru13Service.gI().triggerKheNutGodsCutscene(player, true);
            return;
        }

        boolean claimedChampaSoul = (player.vuTru13Stage & VuTru13Service.STAGE_FLAG_CHAMPA_SOUL) != 0;
        boolean defeatedBoss = (player.vuTru13Stage & VuTru13Service.STAGE_FLAG_THAN_HUY_DIET_SLAIN) != 0;

        String lore = "|8|THẦN HỦY DIỆT BEERUS - VŨ TRỤ 7 (TRẤN GIỮ PHẾ TÍCH THÁP AI CẬP)\n"
                + "|2|Hừm... Ngươi cũng chứng kiến cả rồi đấy! Tên Cổ Thần Hủy Diệt Vũ Trụ 13 vừa thoát khỏi Tháp Ai Cập đã đánh gục 9 vị Thần Hủy Diệt và nuốt chửng 8 Thần Hồn Tím!\n"
                + "|1|- Ta chỉ kịp lao ra đoạt lại duy nhất mảnh Hồn Năng Lượng Tím của Champa (Vũ Trụ 6) trước khi hắn rút về Điện Thờ Thần Hủy Diệt (Map 180).\n"
                + (defeatedBoss
                        ? "|7|- Ngươi đã đánh bại Cổ Thần Hủy Diệt Vũ Trụ 13 một lần rồi, khá lắm! Nhưng oán niệm của hắn vẫn hồi sinh tại Điện Thờ Sâu Thẳm!"
                        : "|7|- Hãy tiếp nhận mảnh thần lực của Champa, rèn luyện Bản Năng / Bản Ngã Vô Cực rồi tiến sâu vào Điện Thờ tiêu diệt hắn!");

        this.createOtherMenu(player, ConstNpc.BASE_MENU, lore,
                claimedChampaSoul ? "Bí mật Hồn Tím\nChampa (VT6)" : "Nhận Hồn Tím\nChampa (VT6)",
                "Xem lại Cảnh\nTháp Ai Cập Vỡ",
                "Khiêu chiến Boss\nThần Hủy Diệt VT13",
                "Tới Điện Thờ\nSăn Boss (Map 180)",
                "Về Thánh Điện\nZeno (Home)");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> handleChampaSoul(player);
                case 1 -> VuTru13Service.gI().triggerKheNutGodsCutscene(player, false);
                case 2 -> this.createOtherMenu(player, MENU_CHALLENGE_BOSS_VT13,
                        "|8|THÁCH ĐẤU THẦN HỦY DIỆT VŨ TRỤ 13\n"
                        + "|2|Chân thân của Cổ Thần Hủy Diệt VT13 (2 giai đoạn: 25 Tỷ -> 45 Tỷ HP) đang ngự trị tại Điện Thờ Thần Hủy Diệt (Map 180).\n"
                        + "|1|Ngoài ra, ta có thể dùng tàn dư thần lực dựng lên [Phân Thân Thử Nghiệm] của hắn ngay cạnh Tháp Ai Cập này để ngươi kiểm tra chiêu thức và hiệu ứng chiến đấu!",
                        "Triệu hồi Phân Thân\nNgay Tại Đây",
                        "Bay thẳng tới\nĐiện Thờ (Map 180)",
                        "Đóng");
                case 3 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_DIEN_THO_HUY_DIET_VT13, -1, 140);
                case 4 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_THANH_DIEN_ZENO_VT13, -1, 300);
            }
        } else if (player.idMark.getIndexMenu() == MENU_CHALLENGE_BOSS_VT13) {
            switch (select) {
                case 0 -> VuTru13Service.gI().summonTrialBossOnKheNut(player);
                case 1 -> ChangeMapService.gI().changeMapBySpaceShip(player, VuTru13Service.MAP_DIEN_THO_HUY_DIET_VT13, -1, 140);
            }
        }
    }

    private void handleChampaSoul(Player player) {
        if ((player.vuTru13Stage & VuTru13Service.STAGE_FLAG_CHAMPA_SOUL) != 0) {
            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "|8|MẢNH HỒN NĂNG LƯỢNG TÍM CỦA CHAMPA (VŨ TRỤ 6)\n"
                    + "|2|- Nhờ mảnh hồn thứ 9 mà ta đoạt lại từ tay Boss Thần Hủy Diệt VT13, vòng tuần hoàn 12 Thần Hồn của hắn bị khuyết 1 góc.\n"
                    + "|1|- Khi giao chiến với hắn, hãy coi chừng 3 tuyệt kỹ: [Quả Cầu Hakai Cổ Thần] xuyên phá khiên, [Trận Pháp Ai Cập] gây choáng và [Bão Hồn 8 Thần Hủy Diệt] phát nổ Flash diện rộng!",
                    "Đã rõ");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 2) {
            Service.gI().sendThongBao(player, "Hãy dọn trống ít nhất 2 ô hành trang để tiếp nhận di vật Hồn Hủy Diệt của Champa!");
            return;
        }
        Item maHon = ItemService.gI().createNewItem((short) 2019, 3);
        Item honTim = ItemService.gI().createNewItem((short) 2018, 5);
        InventoryService.gI().addItemBag(player, maHon);
        InventoryService.gI().addItemBag(player, honTim);
        InventoryService.gI().sendItemBags(player);

        player.vuTru13Stage |= VuTru13Service.STAGE_FLAG_CHAMPA_SOUL;
        VuTru13Service.gI().sendState(player);
        PlayerDAO.savePlayerAsync(player);

        Service.gI().sendThongBao(player, "Beerus trao cho bạn mảnh tinh hoa Hồn Tím VT6: +3 Ma Hồn Hủy Diệt & +5 Hồn Boss Tím!");
    }
}
