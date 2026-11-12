package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.player.Player;
import nro.models.services.TaskService;

public class OngParagus extends OngGohan {

    public OngParagus(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Con cố gắng theo Vua Vegeta học thành tài, đừng lo lắng cho ta.\n"
                        + "Ta có dựng sẵn Bảng Sưu Tập Set Kích Hoạt trong nhà cho con đây!",
                        "Đổi ngọc xanh\n1 TV = 1k ngọc",
                        "Nhận đệ tử",
                        "Bảng Sưu Tập\nSet Kích Hoạt",
                        "Nhận Hào Quang\n(Đủ 5 Set)",
                        "Hướng dẫn\nSet Kích Hoạt",
                        "Đóng");
            }
        }
    }
}
