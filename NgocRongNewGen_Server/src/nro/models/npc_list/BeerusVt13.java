package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class BeerusVt13 extends Npc {

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
        // Nếu người chơi chưa xem phân cảnh phong ấn vỡ thì kích hoạt ngay
        if ((player.vuTru13Stage & VuTru13Service.STAGE_FLAG_KHE_NUT_INTRO) == 0) {
            VuTru13Service.gI().triggerKheNutGodsCutscene(player, true);
            return;
        }

        boolean claimedChampaSoul = (player.vuTru13Stage & VuTru13Service.STAGE_FLAG_CHAMPA_SOUL) != 0;
        boolean defeatedBoss = (player.vuTru13Stage & VuTru13Service.STAGE_FLAG_THAN_HUY_DIET_SLAIN) != 0;

        String lore = "|8|THẦN HỦY DIỆT BEERUS - VŨ TRỤ 7\n"
                + "|2|Hừm... Ngươi cũng chứng kiến cả rồi đấy! Thần Hủy Diệt Bunny vừa thoát khỏi xiềng xích giam giữ đã đánh gục 9 vị Thần Hủy Diệt và hấp thụ 8 Nguồn Năng Lượng Tím!\n"
                + "|1|- Ta chỉ kịp lao ra giữ lại mảnh Năng Lượng Tím của Champa (Vũ Trụ 6) trước khi hắn rút về Điện Thờ Thần Hủy Diệt.\n"
                + (defeatedBoss
                        ? "|7|- Ngươi đã đánh bại Thần Hủy Diệt Bunny một lần rồi, khá lắm! Nhưng hắn vẫn xuất hiện lại mỗi đầu giờ tại Điện Thờ!"
                        : (claimedChampaSoul
                                ? "|7|- Ngươi đã nhận mảnh năng lượng Hồn Tím của Champa rồi. Hãy rèn luyện Bản Năng / Bản Ngã Vô Cực và đánh bại Thần Hủy Diệt Bunny!"
                                : "|7|- Hãy tiếp nhận mảnh năng lượng của Champa, rèn luyện Bản Năng / Bản Ngã Vô Cực rồi tiến vào Điện Thờ đánh bại hắn!"));

        if (!claimedChampaSoul) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, lore,
                    "Nhận Hồn Tím\nChampa (VT6)",
                    "Xem lại Cảnh\nPhong Ấn Vỡ");
        } else {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, lore,
                    "Xem lại Cảnh\nPhong Ấn Vỡ");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            boolean claimedChampaSoul = (player.vuTru13Stage & VuTru13Service.STAGE_FLAG_CHAMPA_SOUL) != 0;
            if (!claimedChampaSoul) {
                switch (select) {
                    case 0 -> handleChampaSoul(player);
                    case 1 -> VuTru13Service.gI().triggerKheNutGodsCutscene(player, false);
                }
            } else if (select == 0) {
                VuTru13Service.gI().triggerKheNutGodsCutscene(player, false);
            }
        }
    }

    private void handleChampaSoul(Player player) {
        if ((player.vuTru13Stage & VuTru13Service.STAGE_FLAG_CHAMPA_SOUL) != 0) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 2) {
            Service.gI().sendThongBao(player, "Hãy dọn trống ít nhất 2 ô hành trang để nhận mảnh năng lượng Hủy Diệt của Champa!");
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
