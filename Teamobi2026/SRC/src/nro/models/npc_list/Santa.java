package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.ItemTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.shop.ShopService;
import nro.models.map.service.ChangeMapService;
import nro.models.services_func.Input;
import nro.models.services.InventoryService;
import nro.models.services.Service;

public class Santa extends Npc {

    public Santa(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private List<String> buildBaseMenu(Player player) {
        Item pGG = InventoryService.gI().findItem(player.inventory.itemsBag, 459);
        int soLuong = pGG != null ? pGG.quantity : 0;
        List<String> menu = new ArrayList<>(Arrays.asList(
                "Cửa hàng",
                "Mở rộng\nHành trang\nRương đồ",
                "Nhập mã\nquà tặng",
                "Shop\nThỏi Vàng",
                "Tiệm\nHớt tóc",
                "Danh\nhiệu",
                "Hướng dẫn\nSet Kích Hoạt"));
        if (soLuong >= 1) {
            menu.add(1, "Giảm giá\n80%");
        }
        if (player.itemTime != null && player.itemTime.isUseVeKhuSkh) {
            menu.add("Vào lại\nKhu SKH");
        }
        return menu;
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            String[] menus = buildBaseMenu(player).toArray(new String[0]);
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Xin chào, ta có một số vật phẩm đặc biệt cậu có muốn xem không?", menus);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5 || this.mapId == 13 || this.mapId == 20) {
                if (player.idMark.isBaseMenu()) {
                    List<String> menu = buildBaseMenu(player);
                    if (select < 0 || select >= menu.size()) {
                        return;
                    }
                    switch (menu.get(select)) {
                        case "Cửa hàng":
                            ShopService.gI().opendShop(player, "SANTA", false);
                            break;
                        case "Giảm giá\n80%":
                            ShopService.gI().opendShop(player, "SANTA_GIAM_GIA_1", false);
                            break;
                        case "Mở rộng\nHành trang\nRương đồ":
                            ShopService.gI().opendShop(player, "SANTA_MO_RONG_HANH_TRANG", false);
                            break;
                        case "Nhập mã\nquà tặng":
                            Input.gI().createFormGiftCode(player);
                            break;
                        case "Shop\nThỏi Vàng":
                            ShopService.gI().opendShop(player, "SANTA_HAN_SU_DUNG", false);
                            break;
                        case "Tiệm\nHớt tóc":
                            ShopService.gI().opendShop(player, "SANTA_HEAD", false);
                            break;
                        case "Danh\nhiệu":
                            ShopService.gI().opendShop(player, "SANTA_DANH_HIEU", false);
                            break;
                        case "Hướng dẫn\nSet Kích Hoạt":
                            nro.models.services.SkhCollectionService.gI().showGuide(player);
                            break;
                        case "Vào lại\nKhu SKH":
                            if (player.itemTime != null && player.itemTime.isUseVeKhuSkh) {
                                int minsLeft = (int) Math.max(1L, (ItemTime.TIME_VE_KHU_SKH - (System.currentTimeMillis() - player.itemTime.lastTimeUseVeKhuSkh)) / 60000L);
                                ChangeMapService.gI().changeMapBySpaceShip(player, 164, -1, 870);
                                Service.gI().sendThongBao(player, "Bạn đã vào lại Khu up SKH (còn khoảng " + minsLeft + " phút hiệu lực)");
                            } else {
                                Service.gI().sendThongBao(player, "Vé khu up SKH đã hết thời gian hiệu lực!");
                            }
                            break;
                    }
                }
            }
        }
    }
}
