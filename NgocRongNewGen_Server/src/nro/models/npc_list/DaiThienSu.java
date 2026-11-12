package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.shop.ShopService;

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (this.mapId == 145) {
            String say = "|7|THIÊN SỨ WHIS - KHÁN ĐÀI VÕ ĐÀI LIÊN VŨ TRỤ"
                    + "\n|2|Ngài Beerus và Champa đang theo dõi sát sao từng hiệp đấu của ngươi!"
                    + "\n|0|- Ta có thể hồi phục 100% thể lực trước khi lên đài hoặc đưa ngươi về Hành Tinh Bill chế tạo trang bị Thiên Sứ.";
            createOtherMenu(player, ConstNpc.BASE_MENU, say,
                    "Hồi phục\n100% HP/KI",
                    "Cửa hàng\nGiải Đấu",
                    "Về Hành Tinh\nBill",
                    "Về Đảo Kamê",
                    "Đóng");
            return;
        }
        super.openBaseMenu(player);
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (this.mapId == 145 && player.idMark.isBaseMenu()) {
            if (player.isPKDHVT) {
                Service.gI().sendThongBao(player, "Đang trong hiệp thi đấu, không thể sử dụng trợ giúp của Thiên Sứ!");
                return;
            }
            switch (select) {
                case 0 -> {
                    player.nPoint.frostPoisonUntil = 0L;
                    player.nPoint.hakaiNoPeaUntil = 0L;
                    player.nPoint.hp = player.nPoint.hpMax;
                    player.nPoint.mp = player.nPoint.mpMax;
                    Service.gI().Send_Info_NV(player);
                    PlayerService.gI().sendInfoHpMp(player);
                    if (player.pet != null && !player.pet.isDie() && player.pet.nPoint != null) {
                        player.pet.nPoint.hp = player.pet.nPoint.hpMax;
                        player.pet.nPoint.mp = player.pet.nPoint.mpMax;
                        Service.gI().Send_Info_NV(player.pet);
                    }
                    Service.gI().sendThongBao(player, "Thiên Sứ Whis đã hồi phục 100% HP & KI cho bạn!");
                }
                case 1 -> ShopService.gI().opendShop(player, "TRONG_TAI_VT6", false);
                case 2 -> ChangeMapService.gI().changeMapBySpaceShip(player, 154, -1, 456);
                case 3 -> ChangeMapService.gI().changeMapNonSpaceship(player, 5, 220, 408);
            }
        }
    }
}
