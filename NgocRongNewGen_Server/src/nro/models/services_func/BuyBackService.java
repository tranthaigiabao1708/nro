package nro.models.services_func;

import nro.models.item.Item;
import nro.models.player.Player;

/**
 *
 * @author By Mr Blue
 * 
 */

public class BuyBackService {

    private static final byte MAX_ITEM_IN_BOX = 10;

    private static BuyBackService i;

    public static BuyBackService gI() {
        if (i == null) {
            i = new BuyBackService();
        }
        return i;
    }

    public void addItem(Player player, Item item) {
        // Tính năng mua lại vật phẩm đã bán đã được loại bỏ
    }
}
