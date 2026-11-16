package nro.models.player;

import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.services.InventoryService;
import nro.models.map.service.ItemMapService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class DropItem {

    private Player player;

    public DropItem(Player player) {
        this.player = player;
    }

    public void update() {
        // Đã tắt tự động rơi miễn phí Mảnh giấy có chữ MA [726] tại Đại Hội Võ Thuật (map 52).
        // Vật phẩm [726] nay farm từ quái Hành tinh Ngục Tù, map Nhẫn Thời Không và 10% từ mọi Boss.
    }

    public void dispose() {
        player = null;
    }
}
