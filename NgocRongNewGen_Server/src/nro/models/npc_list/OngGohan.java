package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.SkhCollectionService;
import nro.models.services.TaskService;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Con cố gắng theo Quy Lão Kame học thành tài, đừng lo lắng cho ta.\n"
                        + "Ta có dựng sẵn Bảng Sưu Tập Set Kích Hoạt trong nhà cho con đây!",
                        "Bảng Sưu Tập\nSet Kích Hoạt",
                        "Nhận Hào Quang\n(Đủ 5 Set)",
                        "Hướng dẫn\nSet Kích Hoạt",
                        "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> SkhCollectionService.gI().openCollectionUI(player, 0);
                    case 1 -> SkhCollectionService.gI().claimFiveSetAuraReward(player, false);
                    case 2 -> SkhCollectionService.gI().showGuide(player);
                }
            }
        }
    }
}
