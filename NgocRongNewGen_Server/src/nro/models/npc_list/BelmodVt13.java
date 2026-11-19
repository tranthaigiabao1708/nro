package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class BelmodVt13 extends Npc {

    public BelmodVt13(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
        if ((player.vuTru13Stage & VuTru13Service.STAGE_FLAG_KHE_NUT_INTRO) == 0) {
            VuTru13Service.gI().triggerKheNutGodsCutscene(player, true);
            return;
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "|8|THẦN HỦY DIỆT BELMOD - VŨ TRỤ 11\n"
                + "|2|Không thể tin nổi... Ngay cả khi cả 12 Thần Hủy Diệt cùng ra tay, chỉ có ta, Beerus và Iwan là còn trụ vững sau đòn Hakai của Thần Hủy Diệt Bunny!\n"
                + "|1|- Ta đang giữ lá chắn năng lượng tại Khe Nứt này để ngăn chấn động lan sang các vũ trụ khác.\n"
                + "- Nếu ngươi cần hồi phục thể lực để tiếp tục chiến đấu, ta sẽ truyền thần lực hỗ trợ!",
                "Hồi phục\n100% HP/KI");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu() && select == 0) {
            player.nPoint.setFullHpMp();
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Thần Hủy Diệt Belmod (VT11) đã truyền thần lực hồi đầy 100% HP & KI cho bạn!");
        }
    }
}
