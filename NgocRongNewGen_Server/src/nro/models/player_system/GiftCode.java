package nro.models.player_system;

import nro.models.item.Item.ItemOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import nro.models.player.Player;

/**
 *
 * @author By Mr Blue
 * 
 */

public class GiftCode {

    public String code;
    public int countLeft;
    public int id;
    public HashMap<Integer, Integer> detail = new LinkedHashMap<>();
    public HashMap<Integer, ArrayList<ItemOption>> option = new LinkedHashMap<>();
    public Timestamp datecreate;
    public Timestamp dateexpired;

    public boolean isUsedGiftCode(Player player) {
        return player.giftCode.isUsedGiftCode(code);
    }

    public boolean timeCode() {
        if (this.dateexpired == null) {
            return false;
        }
        if (this.datecreate != null && this.datecreate.getTime() > this.dateexpired.getTime()) {
            return true;
        }
        return System.currentTimeMillis() > this.dateexpired.getTime();
    }
}
