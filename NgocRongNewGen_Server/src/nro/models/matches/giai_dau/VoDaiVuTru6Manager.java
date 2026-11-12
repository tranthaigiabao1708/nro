package nro.models.matches.giai_dau;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.NonNull;
import nro.models.map.Zone;
import nro.models.matches.dai_hoi_vo_thuat.VoDaiVuTru6;
import nro.models.player.Player;
import nro.models.server.Maintenance;
import nro.models.utils.Functions;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public class VoDaiVuTru6Manager implements Runnable {

    private static VoDaiVuTru6Manager instance;
    private long lastUpdate;
    private static final List<VoDaiVuTru6> list = new CopyOnWriteArrayList<>();

    public static VoDaiVuTru6Manager gI() {
        if (instance == null) {
            instance = new VoDaiVuTru6Manager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                long start = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - start), 10));
            } catch (Exception e) {
                Logger.logException(VoDaiVuTru6Manager.class, e);
            }
        }
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            for (VoDaiVuTru6 mc : list) {
                try {
                    mc.update();
                } catch (Exception e) {
                    Logger.logException(VoDaiVuTru6Manager.class, e);
                }
            }
        }
    }

    public void add(VoDaiVuTru6 mc) {
        if (mc != null && !list.contains(mc)) {
            list.add(mc);
        }
    }

    public void remove(VoDaiVuTru6 mc) {
        list.remove(mc);
    }

    public VoDaiVuTru6 getMC(@NonNull Zone zone) {
        for (VoDaiVuTru6 mc : list) {
            if (zone.equals(mc.getZone())) {
                return mc;
            }
        }
        return null;
    }

    public boolean plCheck(Player player) {
        if (player == null) {
            return false;
        }
        for (VoDaiVuTru6 mc : list) {
            if (mc.getPlayer() != null && mc.getPlayer().id == player.id) {
                return true;
            }
        }
        return false;
    }
}

