package nro.models.services_func;

import nro.models.item.Item;
import java.io.IOException;
import java.util.ArrayList;
import nro.models.player.Player;
import nro.models.network.Message;
import nro.models.services.RewardService;
import nro.models.services.Service;
import java.util.List;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;

/**
 *
 * @author By Mr Blue
 * 
 */

public class LuckyRound {

    private static final byte MAX_ITEM_IN_BOX = 100;

    public static final byte USING_GEM = 7;
    public static final byte USING_GOLD = 0;
    public static final byte USING_TICKET = 1;

    private static final byte PRICE_GEM = 4;
    private static final int PRICE_GOLD = 250000000;
    private static final int PRICE_TICKET = 1;
    private static final int TICKET = 821;

    private static LuckyRound instance;

    public static LuckyRound gI() {
        if (instance == null) {
            instance = new LuckyRound();
        }
        return instance;
    }

    public void openCrackBallUI(Player pl, byte type) {
        Service.gI().sendThongBao(pl, "Chức năng Vòng quay may mắn đã đóng!");
    }

    public void openCrackBallVipUI(Player pl, byte type) {
        Service.gI().sendThongBao(pl, "Chức năng Vòng quay may mắn đã đóng!");
    }

    public void readOpenBall(Player player, Message msg) {
        Service.gI().sendThongBao(player, "Chức năng Vòng quay may mắn đã đóng!");
    }

    private void openBallByGem(Player player, byte count) {
        int gemNeed = (count * PRICE_GEM);
        if (player.inventory.gem < gemNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc để mở");
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                player.inventory.gem -= gemNeed;
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByGold(Player player, byte count) {
        int goldNeed = (count * PRICE_GOLD);
        if (player.inventory.gold < goldNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng để mở");
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                player.inventory.gold -= goldNeed;
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, false);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void openBallByTicket(Player player, byte count) {
        int ticketNeed = (count * PRICE_TICKET);
        Item ticket = InventoryService.gI().findItemBag(player, TICKET);
        if (ticket == null || ticket.quantity < ticketNeed) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + ItemService.gI().createNewItem((short) TICKET).template.name + " để quay");
            sendReward(player, new ArrayList<>());
        } else {
            if (count + player.inventory.itemsBoxCrackBall.size() <= MAX_ITEM_IN_BOX) {
                InventoryService.gI().subQuantityItemsBag(player, ticket, ticketNeed);
                InventoryService.gI().sendItemBags(player);
                List<Item> list = RewardService.gI().getListItemLuckyRound(player, count, true);
                addItemToBox(player, list);
                sendReward(player, list);
                Service.gI().sendMoney(player);
            } else {
                Service.gI().sendThongBao(player, "Rương phụ đã đầy");
            }
        }
    }

    private void sendReward(Player player, List<Item> items) {
        Message msg = null;
        try {
            msg = new Message(-127);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.iconID);
            }
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void addItemToBox(Player player, List<Item> items) {
        player.inventory.itemsBoxCrackBall.addAll(items);
    }
}
