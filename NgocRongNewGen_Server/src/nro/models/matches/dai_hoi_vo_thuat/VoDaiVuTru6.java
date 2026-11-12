package nro.models.matches.dai_hoi_vo_thuat;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import nro.models.boss.Boss;
import nro.models.boss.vodai_vt6.VoDaiVt6Boss;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.service.ItemMapService;
import nro.models.matches.DHVT;
import nro.models.matches.giai_dau.VoDaiVuTru6Manager;
import nro.models.player.Player;
import nro.models.server.ServerNotify;
import nro.models.services.EffectSkillService;
import nro.models.services.ItemTimeService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public class VoDaiVuTru6 {

    @Setter
    @Getter
    private Player player;

    @Setter
    @Getter
    private Boss boss;

    @Setter
    private Player npc;

    @Setter
    private int time;
    @Setter
    private int round;
    @Setter
    private int timeWait;
    @Setter
    private int lootWait;

    public boolean endChallenge;

    @Setter
    @Getter
    private Zone zone;

    public void update() {
        if (player == null || player.zone == null || zone == null || !player.zone.equals(zone)) {
            this.endChallenge();
            return;
        }

        // Giai đoạn nghỉ 5s nhặt chiến lợi phẩm sau khi hạ Boss mỗi vòng
        if (lootWait > 0) {
            lootWait--;
            if (lootWait <= 0) {
                autoCollectArenaDrops();
                if (round >= VoDaiVt6Boss.TOTAL_ROUNDS) {
                    champion();
                } else {
                    toTheNextRound();
                }
            }
            return;
        }

        // Giai đoạn Trọng Tài đếm ngược giới thiệu hiệp đấu
        if (timeWait > 0) {
            switch (timeWait) {
                case 10 -> {
                    if (round == 0 || round == 3 || round == 6 || round == 8) {
                        Service.gI().releaseCooldownSkill(player);
                    }
                    if (boss != null) {
                        EffectSkillService.gI().startStun(boss, System.currentTimeMillis(), 9500);
                        Service.gI().sendInfoPlayerEatPea(boss);
                    }
                    EffectSkillService.gI().startStun(player, System.currentTimeMillis(), 9500);
                    ItemTimeService.gI().sendItemTime(player, 3779, 9);
                    healFullPlayerAndPet();
                }
                case 8 -> {
                    if (npc != null && boss != null) {
                        PlayerService.gI().playerMove(npc, 768, 504);
                        Service.gI().chat(npc, "Vòng " + (round + 1) + "/" + VoDaiVt6Boss.TOTAL_ROUNDS
                                + " Giải Đấu Liên Vũ Trụ: " + player.name + " vs " + boss.name + "!");
                    }
                }
                case 5 -> {
                    if (npc != null) {
                        Service.gI().chat(npc, "Khán đài Vũ Trụ 6 & Vũ Trụ 7 đang sôi sục! Trận đấu bắt đầu sau 3 giây!");
                    }
                }
                case 2 -> {
                    if (npc != null) {
                        Service.gI().chat(npc, "TRẬN ĐẤU BẮT ĐẦU!");
                        PlayerService.gI().playerMove(npc, 768, 360);
                    }
                    Service.gI().chat(player, "Lên nào!");
                    if (boss != null) {
                        Service.gI().chat(boss, "Nhào vô đây Vũ Trụ 7!");
                    }
                }
                case 1 -> {
                    EffectSkillService.gI().removeStun(player);
                    if (boss != null) {
                        EffectSkillService.gI().removeStun(boss);
                        VoDaiVuTru6Service.gI().sendTypePK(player, boss);
                        PlayerService.gI().changeAndSendTypePK(this.player, ConstPlayer.PK_PVP);
                        PlayerService.gI().changeAndSendTypePK(this.boss, ConstPlayer.PK_ALL);
                        boss.changeStatus(BossStatus.ACTIVE);
                        new DHVT(player, boss);
                    }
                    setTime(181);
                }
            }
            timeWait--;
            return;
        }

        // Giai đoạn giao chiến 180 giây
        if (time > 0) {
            time--;
            if (player.isDie() || player.lostByDeath) {
                die();
                return;
            }
            if (player.location != null && player.isPKDHVT && !player.isDie() && player.zone != null) {
                if (boss != null && boss.isDie()) {
                    String defeatedName = boss.name;
                    round++;
                    updateProgressReward();
                    clearArenaDebuffs();
                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
                    Service.gI().sendPlayerVS(player, null, (byte) 0);
                    boss.leaveMap();
                    boss = null;
                    if (npc != null) {
                        Service.gI().chat(npc, player.name + " đã hạ gục " + defeatedName
                                + "! Rương Siêu Cấp đạt Cấp " + player.levelSuperChest + "/" + VoDaiVt6Boss.TOTAL_ROUNDS + "!");
                    }
                    Service.gI().sendThongBao(player,
                            "Chiến thắng Vòng " + round + "! Nhặt vật phẩm rơi (tự thu gom vào túi sau 5s)...");
                    this.lootWait = 5;
                    return;
                }
                // Rơi khỏi sàn đấu nổi (Ring-out)
                if (player.location.x < 300 || player.location.x > 1240 || player.location.y > 600) {
                    leave();
                    return;
                }
                if (!player.isPKDHVT) {
                    leave();
                }
            } else {
                endChallenge();
            }
        } else {
            timeOut();
        }
    }

    public void toTheNextRound() {
        try {
            if (round >= VoDaiVt6Boss.TOTAL_ROUNDS) {
                champion();
                return;
            }
            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
            VoDaiVt6Boss nextBoss = new VoDaiVt6Boss(player, round);
            Service.gI().setPos(player, 670, 504);
            if (player.pet != null && !player.pet.isDie() && player.zone != null && player.zone.equals(player.pet.zone)) {
                Service.gI().setPos(player.pet, 640, 504);
            }
            setBoss(nextBoss);
            setTimeWait(10);
        } catch (Exception e) {
            Logger.logException(VoDaiVuTru6.class, e);
            endChallenge();
        }
    }

    private void healFullPlayerAndPet() {
        if (player != null && player.nPoint != null) {
            clearArenaDebuffs();
            player.nPoint.hp = player.nPoint.hpMax;
            player.nPoint.mp = player.nPoint.mpMax;
            Service.gI().Send_Info_NV(player);
            PlayerService.gI().sendInfoHpMp(player);
        }
        if (player != null && player.pet != null && !player.pet.isDie() && player.pet.nPoint != null) {
            player.pet.nPoint.hp = player.pet.nPoint.hpMax;
            player.pet.nPoint.mp = player.pet.nPoint.mpMax;
            Service.gI().Send_Info_NV(player.pet);
        }
    }

    private void clearArenaDebuffs() {
        if (player != null && player.nPoint != null) {
            player.nPoint.frostPoisonUntil = 0L;
            player.nPoint.hakaiNoPeaUntil = 0L;
        }
    }

    private void autoCollectArenaDrops() {
        if (player == null || player.effectSkill == null || player.inventory == null || player.zone == null
                || zone == null || zone.items == null || zone.items.isEmpty()) {
            return;
        }
        try {
            List<ItemMap> snapshot = new ArrayList<>(zone.items);
            for (ItemMap itemMap : snapshot) {
                if (itemMap != null && !itemMap.isPickedUp && (itemMap.playerId == player.id || itemMap.playerId == -1)) {
                    ItemMapService.gI().pickItem(player, itemMap.itemMapId, true);
                }
            }
        } catch (Exception e) {
            Logger.logException(VoDaiVuTru6.class, e);
        }
    }

    public void die() {
        player.lostByDeath = false;
        String winnerName = (boss != null) ? boss.name : "Đối thủ Vũ Trụ 6";
        Service.gI().sendThongBao(player, "Bạn đã bị hạ gục! Hãy gặp Trọng Tài để nhận Rương Siêu Cấp hoặc thách đấu lại!");
        if (npc != null) {
            Service.gI().chat(npc, winnerName + " đã giành chiến thắng hiệp này!");
        }
        endChallenge();
    }

    private void timeOut() {
        String winnerName = (boss != null) ? boss.name : "Đối thủ Vũ Trụ 6";
        Service.gI().sendThongBao(player, "Hết thời gian 180s thi đấu! Bạn bị xử thua hiệp này.");
        if (npc != null) {
            Service.gI().chat(npc, "Hết thời gian thi đấu! " + winnerName + " giành chiến thắng!");
        }
        endChallenge();
    }

    private void champion() {
        Service.gI().sendThongBao(player, "CHÚC MỪNG! Bạn đã quét sạch cả 9 vòng Võ Đài Liên Vũ Trụ 6 & 7! Hãy nhận Rương Siêu Cấp Cấp 9 tại Trọng Tài!");
        if (npc != null) {
            Service.gI().chat(npc, "TÂN QUÁN QUÂN LIÊN VŨ TRỤ 6 & 7 GỌI TÊN " + player.name.toUpperCase() + "!");
        }
        ServerNotify.gI().notify("CHÚC MỪNG " + player.name + " vừa đánh bại cả Thần Hủy Diệt Champa, đăng quang QUÁN QUÂN VÕ ĐÀI VŨ TRỤ 6 & 7!");
        endChallenge();
    }

    public void leave() {
        if (player.levelSuperChest < VoDaiVt6Boss.TOTAL_ROUNDS) {
            setTime(0);
            if (player.effectSkill != null) {
                EffectSkillService.gI().removeStun(player);
            }
            String winnerName = (boss != null) ? boss.name : "Đối thủ Vũ Trụ 6";
            Service.gI().sendThongBao(player, "Phạm quy rơi khỏi sàn võ đài! Bạn bị xử thua hiệp này.");
            if (npc != null) {
                Service.gI().chat(npc, "Đấu thủ đã rơi khỏi võ đài, " + winnerName + " giành chiến thắng!");
            }
            endChallenge();
        }
    }

    private void updateProgressReward() {
        if (player != null && player.levelSuperChest < round) {
            player.levelSuperChest = round;
        }
    }

    public void endChallenge() {
        if (!endChallenge) {
            endChallenge = true;
            try {
                updateProgressReward();
                autoCollectArenaDrops();
                if (player != null) {
                    clearArenaDebuffs();
                    player.isPKDHVT = false;
                    if (player.effectSkill != null && player.nPoint != null) {
                        EffectSkillService.gI().removeStun(player);
                        if (player.zone != null) {
                            healFullPlayerAndPet();
                            PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.NON_PK);
                            if (player.zone.map != null && player.zone.map.mapId == 145) {
                                Service.gI().setPos(player, Util.nextInt(760, 825), 504);
                            }
                            Service.gI().sendPlayerVS(player, null, (byte) 0);
                        }
                    }
                }
                if (npc != null && zone != null) {
                    PlayerService.gI().playerMove(npc, 768, 360);
                }
            } catch (Exception e) {
                Logger.logException(VoDaiVuTru6.class, e);
            } finally {
                if (boss != null) {
                    try {
                        boss.leaveMap();
                    } catch (Exception ignored) {
                    }
                    boss = null;
                }
                zone = null;
                VoDaiVuTru6Manager.gI().remove(this);
            }
        }
    }
}

