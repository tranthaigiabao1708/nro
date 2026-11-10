package nro.models.network;

import java.net.Socket;

import nro.models.player.Player;
import nro.models.server.Controller;
import nro.models.data.DataGame;
import nro.models.database.MrBlue;
import nro.models.item.Item;
import java.io.IOException;
import nro.models.network.Message;
import nro.models.server.Client;
import nro.models.server.Maintenance;
import nro.models.server.Manager;
import nro.models.player_system.AntiLogin;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.TimeUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MySession extends Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> LOGIN_LOCKS = new ConcurrentHashMap<>();
    public Player player;

    public byte timeWait = 100;
    public boolean sentKey;

    public static final byte[] KEYS = {0};
    public byte curR, curW;

    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel = 2;

    public long lastTimeLogout;
    public boolean joinedGame;

    public long lastTimeReadMessage;

    public boolean actived;

    public boolean check;

    public int goldBar;
    public long gold;
    public int eventPoint;
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;
    public double bdPlayer;

    public int version;
    public int vnd;
    public int tongnap;
    public int vip;
    public int luotquay;

    public boolean finishUpdate;

    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (IOException e) {
        }
    }

    public void login(String username, String password) {
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }

        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRunning) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }
        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player == null) {
            String lockKey = (username == null ? "" : username.trim().toLowerCase());
            Object accountLock = LOGIN_LOCKS.computeIfAbsent(lockKey, k -> new Object());
            synchronized (accountLock) {
                if (!this.isConnected() || this.player != null) {
                    return;
                }
                Player pl = null;
                try {
                    long st = System.currentTimeMillis();
                    this.uu = username;
                    this.pp = password;
                    pl = MrBlue.login(this, al);
                    if (pl != null) {
                        if (!this.isConnected()) {
                            pl.dispose();
                            return;
                        }
                        Player existing = Client.gI().getPlayerByUser(this.userId);
                        if (existing != null && existing != pl) {
                            Client.gI().kickSession(existing.getSession());
                        }
                        Player existingById = Client.gI().getPlayer(pl.id);
                        if (existingById != null && existingById != pl) {
                            Client.gI().kickSession(existingById.getSession());
                        }

                        DataGame.sendSmallVersion(this);
                        DataGame.sendBgItemVersion(this);

                        this.timeWait = 0;
                        this.joinedGame = true;
                        pl.nPoint.calPoint();
                        pl.nPoint.setHp(pl.nPoint.hp);
                        pl.nPoint.setMp(pl.nPoint.mp);
                        if (pl.pet != null) {
                            pl.pet.nPoint.calPoint();
                            pl.pet.nPoint.setHp(pl.pet.nPoint.hp);
                            pl.pet.nPoint.setMp(pl.pet.nPoint.mp);
                        }

                        pl.setSession(this);
                        synchronized (this) {
                            if (!this.isConnected()) {
                                pl.dispose();
                                return;
                            }
                            this.player = pl;
                            pl.zone.addPlayer(pl);
                            Client.gI().put(pl);
                        }
                        if (!this.isConnected()) {
                            Client.gI().kickSession(this);
                            return;
                        }
                        DataGame.sendVersionGame(this);
                        DataGame.sendDataItemBG(this);
                        Controller.gI().sendInfo(this);
                        Logger.warning("[" + TimeUtil.getCurrHour() + ":" + TimeUtil.getCurrMin() + "] - Player Login: " + pl.name + ": " + (System.currentTimeMillis() - st) + " ms\n");
                        if (pl.notify != null && !pl.notify.equals("null") && !pl.notify.isEmpty() && pl.notify.length() > 0) {
                            Service.gI().sendThongBao(pl, pl.notify);
                            pl.notify = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (pl != null) {
                        pl.dispose();
                    }
                }
            }
        }
    }
}
