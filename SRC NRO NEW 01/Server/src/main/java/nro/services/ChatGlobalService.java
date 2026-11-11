package nro.services;

import nro.consts.Cmd;
import nro.models.Part;
import nro.models.PartManager;
import nro.models.player.Player;
import nro.server.Client;
import nro.server.Manager;
import nro.server.io.Message;
import nro.utils.Log;
import nro.utils.TimeUtil;
import nro.utils.Util;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author 💖 YTB KhanhDTK 💖
 * 
 *
 */
public class ChatGlobalService implements Runnable {

    private static int COUNT_CHAT = 60;
    private static int COUNT_WAIT = 10;
    private static ChatGlobalService i;

    private List<ChatGlobal> listChatting;
    private List<ChatGlobal> waitingChat;

    private ChatGlobalService() {
        this.listChatting = new ArrayList<>();
        this.waitingChat = new LinkedList<>();
        new Thread(this, "**Chat global").start();
    }

    public static ChatGlobalService gI() {
        if (i == null) {
            i = new ChatGlobalService();
        }
        return i;
    }

    public void chat(Player player, String text) {
        // if (true) {
        // Service.getInstance().sendThongBao(player, "Tính năng tạm thời bảo trì");
        // }
        if (!player.getSession().actived) {
            Service.getInstance().sendThongBaoFromAdmin(player,
                    "|5|VUI LÒNG KÍCH HOẠT TÀI KHOẢN TẠI\n|7|" + Manager.DOMAIN
                            + "\n|5|ĐỂ MỞ KHÓA TÍNH NĂNG CHAT THẾ GIỚI");
        } else if (waitingChat.size() >= COUNT_WAIT) {
            Service.getInstance().sendThongBao(player, "Kênh thế giới hiện đang quá tải, không thể chat lúc này");
        } else {
            boolean haveInChatting = false;
            for (ChatGlobal chat : listChatting) {
                if (chat.text.equals(text)) {
                    haveInChatting = true;
                    break;
                }
            }
            if (haveInChatting) {
                return;
            }

            if (player.inventory.getGold() >= 60000000) {
                if (player.isAdmin() || Util.canDoWithTime(player.lastTimeChatGlobal, 180000)) {
                    if (player.isAdmin() || player.nPoint.power >= 20000000L) {
                        player.inventory.subGold(60000000);
                        Service.getInstance().sendMoney(player);
                        player.lastTimeChatGlobal = System.currentTimeMillis();
                        waitingChat.add(new ChatGlobal(player, text.length() > 100 ? text.substring(0, 100) : text));
                    } else {
                        Service.getInstance().sendThongBao(player,
                                "Sức mạnh phải ít nhất 20 triệu sức mạnh mới có thể chat thế giới");
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Không thể chat thế giới lúc này, vui lòng đợi "
                            + TimeUtil.getTimeLeft(player.lastTimeChatGlobal, 120));
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không đủ vàng, yêu cầu 60tr vàng để chat thế giới");
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!listChatting.isEmpty()) {
                    ChatGlobal chat = listChatting.get(0);
                    if (Util.canDoWithTime(chat.timeSendToPlayer, 10000)) {
                        listChatting.remove(0);
                    }
                }

                if (!waitingChat.isEmpty()) {
                    ChatGlobal chat = waitingChat.get(0);
                    if (listChatting.size() < COUNT_CHAT) {
                        waitingChat.remove(0);
                        chat.timeSendToPlayer = System.currentTimeMillis();
                        listChatting.add(chat);
                        chatGlobal(chat);
                    }
                }
                Thread.sleep(100);
            } catch (Exception e) {
                Log.error(ChatGlobalService.class, e);
            }
        }
    }

    private void chatGlobal(ChatGlobal chat) {
        List<Player> list = Client.gI().getPlayers();
        for (Player pl : list) {
            if (pl != null) {
                try {
                    Message ms = new Message(Cmd.CHAT_THEGIOI_SERVER);
                    ms.writer().writeUTF(chat.playerName);
                    ms.writer().writeUTF("|5|" + chat.text);
                    ms.writer().writeInt((int) chat.playerId);
                    ms.writer().writeShort(chat.head);
                    if (pl.isVersionAbove(220)) {
                        Part part = PartManager.getInstance().find(chat.head);
                        ms.writer().writeShort(part.getIcon(0));
                    }
                    ms.writer().writeShort(chat.body);
                    ms.writer().writeShort(chat.bag); // bag
                    ms.writer().writeShort(chat.leg);
                    ms.writer().writeByte(0);
                    pl.sendMessage(ms);
                    ms.cleanup();
                } catch (Exception e) {
                }
            }
        }
    }

    private void transformText(ChatGlobal chat) {
        String text = chat.text;
        text = text.replaceAll("\\.com", "***")
                .replaceAll("\\.net", "***")
                .replaceAll("\\.xyz", "***")
                .replaceAll("\\.me", "***")
                .replaceAll("\\.pro", "***")
                .replaceAll("\\.mobi", "***")
                .replaceAll("\\.online", "***")
                .replaceAll("\\.info", "***")
                .replaceAll("\\.tk", "***")
                .replaceAll("\\.ml", "***")
                .replaceAll("\\.ga", "***")
                .replaceAll("\\.gq", "***")
                .replaceAll("\\.io", "***")
                .replaceAll("\\.club", "***")
                .replaceAll("cltx", "***")
                .replaceAll("cl", "***")
                .replaceAll("địt", "***")
                .replaceAll("lồn", "***")
                .replaceAll("cặc", "***");
        chat.text = text;
    }

    private class ChatGlobal {

        private Player player;
        public String playerName;
        public int playerId;
        public short head;
        public short body;
        public short leg;
        public short bag;
        public String text;
        public long timeSendToPlayer;

        public ChatGlobal(Player player, String text) {
            this.player = player;
            this.playerName = player.name;
            this.playerId = (int) player.id;
            this.head = player.getHead();
            this.body = player.getBody();
            this.leg = player.getLeg();
            this.bag = player.getFlagBag();
            this.text = text;
            transformText(this);
        }

    }

}
