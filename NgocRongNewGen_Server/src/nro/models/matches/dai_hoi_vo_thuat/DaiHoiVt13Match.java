package nro.models.matches.dai_hoi_vo_thuat;

import nro.models.consts.ConstPlayer;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.matches.DHVT;
import nro.models.matches.giai_dau.DaiHoiVt13Manager;
import nro.models.player.Player;
import nro.models.server.Maintenance;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.VuTru13Service;
import nro.models.utils.Functions;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public final class DaiHoiVt13Match implements Runnable {

    public final Player player1;
    public final Player player2;
    public final Zone arenaZone;
    public final boolean isThirdPlaceMatch;
    public final boolean isFinalMatch;

    private volatile boolean competing = true;
    private int warmupSec = 0;
    private int fightSecLeft = 120;
    private Player winner;
    private Player loser;

    public DaiHoiVt13Match(Player player1, Player player2, Zone arenaZone, boolean isFinalMatch, boolean isThirdPlaceMatch) {
        this.player1 = player1;
        this.player2 = player2;
        this.arenaZone = arenaZone;
        this.isFinalMatch = isFinalMatch;
        this.isThirdPlaceMatch = isThirdPlaceMatch;
    }

    public void start() {
        try {
            if (player1 != null) {
                player1.isPKDHVT = true;
                player1.lostByDeath = false;
                player1.totalDamageTaken = 0;
                ChangeMapService.gI().changeMap(player1, arenaZone, 720, 456);
            }
            if (player2 != null) {
                player2.isPKDHVT = true;
                player2.lostByDeath = false;
                player2.totalDamageTaken = 0;
                ChangeMapService.gI().changeMap(player2, arenaZone, 960, 456);
            }
            new Thread(this, "DaiHoiVt13Match-" + arenaZone.zoneId).start();
        } catch (Exception e) {
            Logger.logException(DaiHoiVt13Match.class, e);
            resolveByFallback();
        }
    }

    public boolean isCompeting() {
        return competing;
    }

    private boolean isPresentInArena(Player pl) {
        return pl != null && !pl.isDisposed && pl.zone != null && pl.zone.equals(arenaZone)
                && pl.zone.map != null && pl.zone.map.mapId == VuTru13Service.MAP_DAI_HOI_VT13;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning && competing) {
            long st = System.currentTimeMillis();
            try {
                tick();
            } catch (Exception e) {
                Logger.logException(DaiHoiVt13Match.class, e);
                resolveByFallback();
                break;
            }
            long elapsed = System.currentTimeMillis() - st;
            Functions.sleep(Math.max(10L, 1000L - elapsed));
        }
    }

    private void tick() {
        boolean p1Here = isPresentInArena(player1);
        boolean p2Here = isPresentInArena(player2);
        if (!p1Here || !p2Here) {
            if (p1Here && !p2Here) {
                finishMatch(player1, player2, "Đối thủ đã rời võ đài!");
            } else if (!p1Here && p2Here) {
                finishMatch(player2, player1, "Đối thủ đã rời võ đài!");
            } else {
                finishMatch(player1 != null ? player1 : player2, player2, "Kết thúc trận đấu!");
            }
            return;
        }

        if (warmupSec < 5) {
            switch (warmupSec) {
                case 0 -> {
                    String matchLabel = isFinalMatch ? "[CHUNG KẾT] " : (isThirdPlaceMatch ? "[TRANH HẠNG 3] " : "");
                    String msg = matchLabel + "Cặp đấu: " + player1.name + " VS " + player2.name + " — Chuẩn bị!";
                    Service.gI().sendThongBao(player1, msg);
                    Service.gI().sendThongBao(player2, msg);
                    Service.gI().setPos0(player1, 720, 456);
                    Service.gI().setPos0(player2, 960, 456);
                    player1.nPoint.setFullHpMp();
                    player2.nPoint.setFullHpMp();
                    PlayerService.gI().sendInfoHpMp(player1);
                    PlayerService.gI().sendInfoHpMp(player2);
                }
                case 1 -> {
                    Service.gI().chat(player1, "3...");
                    Service.gI().chat(player2, "3...");
                }
                case 2 -> {
                    Service.gI().chat(player1, "2...");
                    Service.gI().chat(player2, "2...");
                }
                case 3 -> {
                    Service.gI().chat(player1, "1...");
                    Service.gI().chat(player2, "1...");
                }
                case 4 -> {
                    Service.gI().setPos0(player1, 720, 456);
                    Service.gI().setPos0(player2, 960, 456);
                    PlayerService.gI().changeAndSendTypePK(player1, ConstPlayer.PK_PVP);
                    PlayerService.gI().changeAndSendTypePK(player2, ConstPlayer.PK_PVP);
                    Service.gI().sendPVP(player1, player2);
                    Service.gI().sendPVP(player2, player1);
                    new DHVT(player1, player2);
                    Service.gI().sendThongBao(player1, "TRẬN ĐẤU BẮT ĐẦU!! (Giới hạn 120 giây)");
                    Service.gI().sendThongBao(player2, "TRẬN ĐẤU BẮT ĐẦU!! (Giới hạn 120 giây)");
                }
            }
            warmupSec++;
            return;
        }

        if (player2.isDie() || player2.lostByDeath) {
            finishMatch(player1, player2, "Hạ gục đối thủ trực tiếp!");
            return;
        }
        if (player1.isDie() || player1.lostByDeath) {
            finishMatch(player2, player1, "Hạ gục đối thủ trực tiếp!");
            return;
        }

        fightSecLeft--;
        if (fightSecLeft <= 0) {
            if (player1.totalDamageTaken <= player2.totalDamageTaken) {
                finishMatch(player1, player2, "Hết giờ! Chiến thắng nhờ chịu ít sát thương hơn!");
            } else {
                finishMatch(player2, player1, "Hết giờ! Chiến thắng nhờ chịu ít sát thương hơn!");
            }
        }
    }

    private void resolveByFallback() {
        if (!competing) {
            return;
        }
        Player w = player1 != null ? player1 : player2;
        Player l = w == player1 ? player2 : player1;
        finishMatch(w, l, "Xử định kết quả cặp đấu!");
    }

    private synchronized void finishMatch(Player winPl, Player losePl, String reason) {
        if (!competing) {
            return;
        }
        competing = false;
        this.winner = winPl;
        this.loser = losePl;

        cleanupFighter(player1);
        cleanupFighter(player2);

        if (winPl != null && !winPl.isDisposed) {
            Service.gI().sendThongBao(winPl, "CHIẾN THẮNG! (" + reason + ")");
        }
        if (losePl != null && !losePl.isDisposed) {
            Service.gI().sendThongBao(losePl, "Bạn đã dừng bước ở cặp đấu này! (" + reason + ")");
        }

        DaiHoiVt13Manager.gI().onMatchFinished(this, winPl, losePl);
    }

    private void cleanupFighter(Player pl) {
        if (pl == null || pl.isDisposed) {
            return;
        }
        try {
            pl.isPKDHVT = false;
            pl.lostByDeath = false;
            if (pl.isDie()) {
                Service.gI().hsChar(pl, pl.nPoint.hpMax, pl.nPoint.mpMax);
            } else {
                pl.nPoint.setFullHpMp();
                PlayerService.gI().sendInfoHpMp(pl);
            }
            PlayerService.gI().changeAndSendTypePK(pl, ConstPlayer.NON_PK);
            Service.gI().sendPlayerVS(pl, null, (byte) 0);
            if (pl.zone != null && pl.zone.map != null && pl.zone.map.mapId == VuTru13Service.MAP_DAI_HOI_VT13 && pl.zone.zoneId != 0) {
                ChangeMapService.gI().changeZoneBySystem(pl, 0);
                Service.gI().setPos(pl, Util.nextInt(760, 920), 456);
            }
        } catch (Exception e) {
            Logger.logException(DaiHoiVt13Match.class, e);
        }
    }
}
