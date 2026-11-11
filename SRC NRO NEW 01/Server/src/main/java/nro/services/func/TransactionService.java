package nro.services.func;

import nro.models.player.Player;
import nro.server.io.Message;
import nro.services.Service;
import nro.utils.Log;
import nro.utils.Util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class TransactionService implements Runnable {

    private static final int TIME_DELAY_TRADE = 30000;

    static final Map<Player, Trade> PLAYER_TRADE = new HashMap<Player, Trade>();

    private static final byte SEND_INVITE_TRADE = 0;
    private static final byte ACCEPT_TRADE = 1;
    private static final byte ADD_ITEM_TRADE = 2;
    private static final byte CANCEL_TRADE = 3;
    private static final byte LOCK_TRADE = 5;
    private static final byte ACCEPT = 7;

    private static TransactionService i;

    private TransactionService() {
    }

    public static TransactionService gI() {
        if (i == null) {
            i = new TransactionService();
            new Thread(i).start();
        }
        return i;
    }

    public void controller(Player pl, Message msg) {
        if (pl.isUseMaBaoVe) {
            Service.getInstance().sendThongBao(pl, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
            return;
        }
        if (!pl.getSession().actived) {
            Service.getInstance().sendThongBao(pl, "Yêu cầu kích hoạt tài khoản để có thể mở khoá giao dịch");
            return;
        }
        try {
            byte action = msg.reader().readByte();
            int playerId = -1;
            Player plMap = null;
            Trade trade = PLAYER_TRADE.get(pl);
            switch (action) {
                case SEND_INVITE_TRADE:
                case ACCEPT_TRADE:
                    playerId = msg.reader().readInt();
                    plMap = pl.zone.getPlayerInMap(playerId);
                    if (plMap != null) {
                        trade = PLAYER_TRADE.get(pl);
                        if (trade == null) {
                            trade = PLAYER_TRADE.get(plMap);
                        }
                        if (trade == null) {
                            if (action == SEND_INVITE_TRADE) {
                                if (Util.canDoWithTime(pl.lastTimeTrade, TIME_DELAY_TRADE)
                                        && Util.canDoWithTime(plMap.lastTimeTrade, TIME_DELAY_TRADE)) {
                                    pl.playerTradeId = (int) plMap.id;
                                    sendInviteTrade(pl, plMap);
                                } else {
                                    Service.getInstance().sendThongBao(pl, "Không thể giao dịch ngay lúc này");
                                }
                            } else {
                                if (plMap.playerTradeId == pl.id) {
                                    pl.lastTimeTrade = System.currentTimeMillis();
                                    plMap.lastTimeTrade = System.currentTimeMillis();
                                    trade = new Trade(pl, plMap);
                                    trade.openTabTrade();
                                }
                            }
                        } else {
                            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                        }
                    }
                    break;
                case ADD_ITEM_TRADE:
                    if (trade != null) {
                        byte index = msg.reader().readByte();
                        int quantity = msg.reader().readInt();
                        if (quantity < 0) {
                            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
                            trade.cancelTrade();
                        }
                        if (quantity == 0) {// do
                            quantity = 1;
                        }
                        trade.addItemTrade(pl, index, quantity);
                    }
                    break;
                case CANCEL_TRADE:
                    if (trade != null) {
                        trade.cancelTrade();
                    }
                    break;
                case LOCK_TRADE:
                    if (trade != null) {
                        trade.lockTran(pl);
                    }
                    break;
                case ACCEPT:
                    if (trade != null) {
                        trade.acceptTrade();
                        if (trade.accept == 2) {
                            trade.dispose();
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            Log.error(this.getClass(), e);
        }
    }

    /**
     * Mời giao dịch
     */
    private void sendInviteTrade(Player plInvite, Player plReceive) {
        Message msg;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) plInvite.id);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Hủy giao dịch
     */
    public void cancelTrade(Player player) {
        Trade trade = PLAYER_TRADE.get(player);
        if (trade != null) {
            trade.cancelTrade();
        }
    }

    @Override
    public void run() {
        // while (true) {
        // try {
        // long st = System.currentTimeMillis();
        // Set<Map.Entry<Player, Trade>> entrySet = PLAYER_TRADE.entrySet();
        // for (Map.Entry entry : entrySet) {
        // ((Trade) entry.getValue()).update();
        // }
        // Thread.sleep(300 - (System.currentTimeMillis() - st));
        // } catch (Exception e) {
        // }
        // }
    }
}
