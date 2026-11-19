package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services.VuTru13Service;

public class IwanVt13 extends Npc {

    public IwanVt13(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
                "|8|THẦN HỦY DIỆT IWAN - VŨ TRỤ 1\n"
                + "|2|Sau khi hấp thụ 8 Nguồn Năng Lượng Tím của các Thần Hủy Diệt khác, sức mạnh của Thần Hủy Diệt Bunny đã vượt xa các vị thần thông thường!\n"
                + "|1|- Ta cùng Beerus và Belmod đang duy trì kết giới giữ ổn định không gian tại đây.\n"
                + "- Nếu ngươi cần hồi phục thể lực để tiếp tục chiến đấu, hãy để ta truyền thần lực hỗ trợ!",
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
            Service.gI().sendThongBao(player, "Thần Hủy Diệt Iwan (VT1) đã truyền thần lực hồi đầy 100% HP & KI cho bạn!");
        }
    }
}
