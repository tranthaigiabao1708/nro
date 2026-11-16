package nro.models.player;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author By Mr Blue
 * 
 */

public class GiftCode {

    public List<String> rewards;

    public GiftCode() {
        this.rewards = new ArrayList<>();
    }

    public void add(String code) {
        this.rewards.add(code);
    }

    public boolean isUsedGiftCode(String code) {
        if (code == null || rewards == null) {
            return false;
        }
        for (String used : rewards) {
            if (used != null && used.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        if (rewards != null) {
            rewards.clear();
            rewards = null;
        }
    }

}
