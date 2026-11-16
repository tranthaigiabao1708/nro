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
        nro.models.npc_list.TrongTai.refreshDailyArenaFeesIfNeeded(player);
        Zone arenaZone = getAvailableArenaZone(player);
        if (arenaZone == null) {
            Service.gI().sendThongBao(player, "Tất cả 10 khu vực Võ Đài đang kín trận đấu, vui lòng thử lại sau ít giây!");
            return false;
        }
        boolean alreadyInArenaZone = (player.zone != null && player.zone.equals(arenaZone));
        if (alreadyInArenaZone) {
            Service.gI().setPos(player, 670, 504);
            if (player.pet != null && !player.pet.isDie() && arenaZone.equals(player.pet.zone)) {
                Service.gI().setPos(player.pet, 640, 504);
            }
            return initMatchInZone(player, arenaZone);
        } else {
            ChangeMapService.gI().changeMap(player, arenaZone, 670, 504);
            The23rdMartialArtCongressService.setTimeout(() -> initMatchInZone(player, arenaZone), 500);
        }
        return true;
    }

    private boolean initMatchInZone(Player player, Zone arenaZone) {
        try {
            if (player == null || player.zone == null || !player.zone.equals(arenaZone) || VoDaiVuTru6Manager.gI().plCheck(player)) {
                return false;
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
            VoDaiVuTru6Manager.gI().add(mc);
            mc.toTheNextRound();
            if (mc.endChallenge) {
                VoDaiVuTru6Manager.gI().remove(mc);
                return false;
            }
            Service.gI().sendThongBao(player, "Bước lên Võ Đài Khu " + arenaZone.zoneId + " - Chuẩn bị nghênh chiến Vòng " + (player.levelSuperChest + 1) + "/9!");
            Service.gI().releaseCooldownSkill(player);
            return true;
        } catch (Exception e) {
            Logger.logException(VoDaiVuTru6Service.class, e);
            return false;
        }
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
        return getAvailableArenaZone(null);
    }

    private void cleanupOrphanedArenaBosses(Zone z) {
        if (z == null || VoDaiVuTru6Manager.gI().getMC(z) != null || z.getBosses().isEmpty()) {
            return;
        }
        for (Player b : new java.util.ArrayList<>(z.getBosses())) {
            if (b instanceof nro.models.boss.vodai_vt6.VoDaiVt6Boss vt6Boss) {
                try {
                    vt6Boss.leaveMap();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public Zone getAvailableArenaZone(Player player) {
        Map map = MapService.gI().getMapById(145);
        if (map == null || map.zones == null) {
            return null;
        }
        for (Zone z : map.zones) {
            cleanupOrphanedArenaBosses(z);
        }
        // Nếu người chơi đang đứng sẵn một mình ở khu Võ Đài 145 trống trận đấu -> tái sử dụng luôn khu hiện tại
        if (player != null && player.zone != null && player.zone.map != null && player.zone.map.mapId == 145) {
            Zone cur = player.zone;
            if (cur.getNumOfBosses() == 0 && VoDaiVuTru6Manager.gI().getMC(cur) == null && cur.getNumOfPlayers() <= 1) {
                return cur;
            }
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

