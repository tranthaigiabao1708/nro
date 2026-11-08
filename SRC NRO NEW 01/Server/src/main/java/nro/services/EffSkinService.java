package nro.services;

import nro.consts.Cmd;
import nro.models.player.Player;
import nro.server.io.Message;
import nro.utils.Log;

/**
 * @author outcast c-cute hột me 😳
 */
public class EffSkinService {

    public static final byte TURN_ON_EFFECT = 1;
    public static final byte TURN_OFF_EFFECT = 0;
    public static final byte TURN_OFF_ALL_EFFECT = 2;
    public static final byte BLIND_EFFECT = 40;
    public static final byte SLEEP_EFFECT = 41;
    public static final byte STONE_EFFECT = 42;
    private static EffSkinService i;

    public static EffSkinService gI() {
        if (i == null) {
            i = new EffSkinService();
        }
        return i;
    }

    public void setInvisible(Player pl, long lastTimeInvisible, int timeInvisible) {
        pl.effectSkin.timeInvisible = timeInvisible;
        pl.effectSkin.lastTimeInvisible = lastTimeInvisible;
        pl.isInvisible = true;
        Service.getInstance().sendPlayerInfo(pl);
      try {
    Service.getInstance().sendPlayerInfo(pl.pet);
} catch (Exception e) {
    // Xử lý ngoại lệ ở đây
  
    // Hoặc bạn có thể xử lý ngoại lệ theo cách khác tùy vào yêu cầu của ứng dụng
}

    }

    public void removeInvisible(Player pl) {
        pl.isInvisible = false;
        Service.getInstance().sendPlayerInfo(pl);
        Service.getInstance().sendPlayerInfo(pl.pet);
    }

    public void setSlow(Player player, long lastTimeSlow, int timeSlow) {
        player.effectSkin.lastTimeSlow = lastTimeSlow;
        player.effectSkin.timeSlow = timeSlow;
        player.effectSkin.isSlow = true;
        Service.getInstance().point(player);
    }

    public void removeSlow(Player player) {
        player.effectSkin.isSlow = false;
        Service.getInstance().point(player);
    }

    public void setSocola(Player player, long lastTimeSocola, int timeSocola) {
        player.effectSkin.lastTimeSocola = lastTimeSocola;
        player.effectSkin.timeSocola = timeSocola;
        player.effectSkin.isSocola = true;
        Service.getInstance().point(player);
    }

    public void removeSocola(Player player) {
        player.effectSkin.isSocola = false;
        Service.getInstance().point(player);
        Service.getInstance().Send_Caitrang(player);
    }

//    public void setMaPhongBa(Player player, long lastTimeMaPhongBa, int timeMaPhongBa, int dameMaFuBa) {
//        player.effectSkin.lastTimeMaPhongBa = lastTimeMaPhongBa;
//        player.effectSkin.timeMaPhongBa = timeMaPhongBa;
//        player.effectSkin.isMaPhongBa = true;
//        player.dameMaFuBa = dameMaFuBa;
//        Service.getInstance().point(player);
//    }

    public void removeMaPhongBa(Player player) {
        player.effectSkin.isMaPhongBa = false;
        Service.getInstance().point(player);
        Service.getInstance().Send_Caitrang(player);
    }

    public void setHoaDa(Player player, long lastTimeHoaDa, int timeHoaDa) {
        player.effectSkin.isHoaDa = true;
        player.effectSkin.lastTimeHoaDa = lastTimeHoaDa;
        player.effectSkin.timeHoaDa = timeHoaDa;
    }

    public void removeHoaDa(Player player) {
        player.effectSkin.isHoaDa = false;
        Service.getInstance().Send_Caitrang(player);
        sendEffectPlayer(player, player, TURN_OFF_EFFECT, STONE_EFFECT);
    }

    public void setCuongNo(Player player, long lastTimeCuongNo, int timeCuongNo) {
        player.effectSkin.isNezuko = true;
        player.effectSkin.lastTimeNezuko = lastTimeCuongNo;
        player.effectSkin.timeNezuko = timeCuongNo;
        Service.getInstance().point(player);
    }

    public void removeCuongNo(Player player) {
        player.effectSkin.isNezuko = false;
        Service.getInstance().point(player);
    }

    public void setInosuke(Player player, long lastTimeTanjiro, int timeTanjiro) {
        player.effectSkin.isInosuke = true;
        player.effectSkin.lastTimeInosuke = lastTimeTanjiro;
        player.effectSkin.timeInosuke = timeTanjiro;
        Service.getInstance().point(player);
    }

    public void removeInosuke(Player player) {
        player.effectSkin.isInosuke = false;
        Service.getInstance().point(player);
    }

    public void setTanjiro(Player player, long lastTimeTanjiro, int timeTanjiro) {
        player.effectSkin.isTanjiro = true;
        player.effectSkin.lastTimeTanjiro = lastTimeTanjiro;
        player.effectSkin.timeTanjiro = timeTanjiro;
        Service.getInstance().point(player);
    }

    public void removeTanjiro(Player player) {
        player.effectSkin.isTanjiro = false;
        Service.getInstance().point(player);
    }

    public void setZenitsu(Player player, long lastTimeZenitsu, int timeZenitsu) {
        player.effectSkin.isZenitsu = true;
        player.effectSkin.lastTimeZenitsu = lastTimeZenitsu;
        player.effectSkin.timeZenitsu = timeZenitsu;
        Service.getInstance().point(player);
    }

    public void removeZenitsu(Player player) {
        player.effectSkin.isZenitsu = false;
        Service.getInstance().point(player);
    }

    public void setInoHashi(Player player, long lastTimeInoHashi, int timeInoHashi) {
        player.effectSkin.isInoHashi = true;
        player.effectSkin.lastTimeInoHashi = lastTimeInoHashi;
        player.effectSkin.timeInoHashi = timeInoHashi;
        Service.getInstance().point(player);
    }

    public void removeInoHashi(Player player) {
        player.effectSkin.isInoHashi = false;
        Service.getInstance().point(player);
    }

//    public void sendCharEffect_HoaBang(Player player) {
//        Message msg;
//        try {
//            msg = new Message(Cmd.CHAR_EFFECT);
//            msg.writer().writeByte(0);// 0 : adđ
//            msg.writer().writeInt((int) player.id);
//            msg.writer().writeShort(202);
//            msg.writer().writeByte(1);
//            msg.writer().writeByte(-1);
//            msg.writer().writeShort(10);
//            msg.writer().writeByte(0);
//            Service.getInstance().sendMessAllPlayerInMap(player, msg);
//            msg.cleanup();
//        } catch (Exception e) {
//        }
//    }

    public void sendEffectPlayer(Player plUseSkill, Player plTarget, byte toggle, byte effect) {
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(toggle); //0: hủy hiệu ứng, 1: bắt đầu hiệu ứng
            msg.writer().writeByte(0); //0: vào phần phayer, 1: vào phần mob
            if (toggle == TURN_OFF_ALL_EFFECT) {
                msg.writer().writeInt((int) plTarget.id);
            } else {
                msg.writer().writeByte(effect); //loại hiệu ứng
                msg.writer().writeInt((int) plTarget.id); //id player dính effect
                msg.writer().writeInt((int) plUseSkill.id); //id player dùng skill
            }
            Service.getInstance().sendMessAllPlayerInMap(plUseSkill, msg);
            msg.cleanup();
        } catch (Exception e) {
            Log.error(EffectSkillService.class, e);
        }
    }
}
