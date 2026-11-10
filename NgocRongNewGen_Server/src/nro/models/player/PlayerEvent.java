package nro.models.player;

import lombok.Getter;
import lombok.Setter;

/**
 * @author By Mr Blue
 */
@Setter
@Getter
public class PlayerEvent {
    private int eventPoint;
    private Player player;
    public int luotNhanNgocMienPhi = 1;
    public int luotNhanCapsuleBang = 1;

    public PlayerEvent(Player player) {
        this.player = player;
    }

    public void addEventPoint(int num) {
        // Sự kiện đang tắt: không cộng điểm sự kiện
    }

    public void subEventPoint(int num) {
        // Sự kiện đang tắt
    }

    public void update() {
    }
}
