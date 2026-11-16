package nro.models.managers;
import nro.models.data.LocalManager;
import nro.models.player_system.GiftCode;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import java.util.ArrayList;
import nro.models.services.InventoryService;

public class GiftCodeManager {

    public String name;
    public final ArrayList<GiftCode> listGiftCode = new ArrayList<>();

    private static GiftCodeManager instance;

    public static GiftCodeManager gI() {
        if (instance == null) {
            instance = new GiftCodeManager();
        }
        return instance;
    }

    public GiftCode checkUseGiftCode(Player player, String code) {
        if (code == null) {
            Service.gI().sendThongBao(player, "Code không chính xác!");
            return null;
        }
        String normalized = code.trim();
        synchronized (listGiftCode) {
            for (GiftCode giftCode : listGiftCode) {
                if (giftCode.code != null && giftCode.code.equalsIgnoreCase(normalized)) {
                    if (giftCode.timeCode()) {
                        Service.gI().sendThongBaoOK(player, "Giftcode đã hết hạn!");
                        return null;
                    }
                    if (giftCode.countLeft <= 0) {
                        Service.gI().sendThongBaoOK(player, "Giftcode đã hết lượt sử dụng!");
                        return null;
                    } else if (giftCode.isUsedGiftCode(player)) {
                        Service.gI().sendThongBaoOK(player, "Bạn đã sử dụng Giftcode này rồi!");
                        return null;
                    }
                    int requiredBagSlots = 0;
                    for (Integer itemId : giftCode.detail.keySet()) {
                        if (itemId != null && itemId >= 0) {
                            requiredBagSlots++;
                        }
                    }
                    if (InventoryService.gI().getCountEmptyBag(player) < requiredBagSlots) {
                        Service.gI().sendThongBaoOK(player, "Cần tối thiểu " + requiredBagSlots + " ô hành trang trống!");
                        return null;
                    }
                    if (giftCode.countLeft < 99_999_999) {
                        giftCode.countLeft -= 1;
                        updateGiftCode(giftCode);
                    }
                    player.giftCode.add(giftCode.code);
                    return giftCode;
                }
            }
        }
        Service.gI().sendThongBao(player, "Code không chính xác!");
        return null;
    }

    public void updateGiftCode(GiftCode giftcode) {
        try {
            LocalManager.executeUpdate("update giftcode set count_left = ? where id = ?", giftcode.countLeft, giftcode.id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkInfomationGiftCode(Player p) {
        StringBuilder sb = new StringBuilder();
        for (GiftCode giftCode : listGiftCode) {
            sb.append("Code: ").append(giftCode.code).append(", Số lượng còn lại: ").append(giftCode.countLeft).append("\b")
                    .append("Ngày tạo: ")
                    .append(giftCode.datecreate).append(", Ngày hết hạn: ").append(giftCode.dateexpired)
                    .append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        NpcService.gI().createTutorial(p, 5073, sb.toString());
    }

}