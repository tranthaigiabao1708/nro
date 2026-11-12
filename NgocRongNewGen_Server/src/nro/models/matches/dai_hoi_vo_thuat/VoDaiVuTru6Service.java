package nro.models.matches.dai_hoi_vo_thuat;

import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.matches.giai_dau.VoDaiVuTru6Manager;
import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Logger;

public class VoDaiVuTru6Service {

    private static VoDaiVuTru6Service i;

    public static VoDaiVuTru6Service gI() {
        if (i == null) {
            i = new VoDaiVuTru6Service();
        }
        return i;
    }

    public boolean startChallenge(Player player) {
        if (player == null || VoDaiVuTru6Manager.gI().plCheck(player)) {
            return false;
        }
        Zone arenaZone = getAvailableArenaZone();
        if (arenaZone == null) {
            Service.gI().sendThongBao(player, "Tất cả 10 khu vực Võ Đài đang kín trận đấu, vui lòng thử lại sau ít giây!");
            return false;
        }
        ChangeMapService.gI().changeMap(player, arenaZone, 670, 504);
        The23rdMartialArtCongressService.setTimeout(() -> {
            try {
                if (player.zone == null || !player.zone.equals(arenaZone)) {
                    return;
                }
                VoDaiVuTru6 mc = new VoDaiVuTru6();
                mc.setZone(arenaZone);
                mc.setPlayer(player);
                mc.setNpc(arenaZone.getNpc());
                mc.setRound(player.levelSuperChest);
                mc.endChallenge = false;
                player.isPKDHVT = true;
                player.lostByDeath = false;
                player.lastTimePKVoDaiVt6 = System.currentTimeMillis();
                mc.toTheNextRound();
                VoDaiVuTru6Manager.gI().add(mc);
                Service.gI().sendThongBao(player, "Bước lên Võ Đài Khu " + arenaZone.zoneId + " - Chuẩn bị nghênh chiến Vòng " + (player.levelSuperChest + 1) + "/9!");
                Service.gI().releaseCooldownSkill(player);
            } catch (Exception e) {
                Logger.logException(VoDaiVuTru6Service.class, e);
            }
        }, 500);
        return true;
    }

    public void sendTypePK(Player player, Player boss) {
        try {
            Message msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(VoDaiVuTru6Service.class, e);
        }
    }

    public Zone getAvailableArenaZone() {
        Map map = MapService.gI().getMapById(145);
        if (map == null || map.zones == null) {
            return null;
        }
        // Ưu tiên khu vực hoàn toàn trống người chơi & trống trận đấu trước
        for (Zone z : map.zones) {
            if (z != null && z.getNumOfBosses() == 0 && VoDaiVuTru6Manager.gI().getMC(z) == null && z.getNumOfPlayers() == 0) {
                return z;
            }
        }
        for (Zone z : map.zones) {
            if (z != null && z.getNumOfBosses() == 0 && VoDaiVuTru6Manager.gI().getMC(z) == null && !z.isFullPlayer()) {
                return z;
            }
        }
        return null;
    }
}

