package nro.models.services.shenron;

import nro.models.network.Message;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.player.Player;
import lombok.Getter;
import lombok.Setter;
import nro.models.map.Zone;
import nro.models.server.Client;
import nro.models.services.ItemService;
import nro.models.services.InventoryService;
import nro.models.services.ItemTimeService;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.services.IntrinsicService;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Shenron_Event {

    @Setter
    @Getter
    private Player player;

    @Setter
    @Getter
    private Zone zone;

    public long playerId;
    public boolean isPlayerDisconnect;
    public byte select;
    public int shenronType;
    public boolean leaveMap;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;

    public static final byte DRAGON_EVENT = 1;

    public long lastTimeShenronWait;
    public static int timeResummonShenron = 60000;
    public static int timeShenronWait = 300000;

    public static final String SHENRONEVENT_SAY
            = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy chọn đi:\n1) Đổi skill 3, 4 đệ tử (có thể trùng skill trước đó).\n2) Thay đổi nội tại.\n3) Cải trang siêu thần HSD 90 ngày.\n4) Cải trang Black Gohan Rose HSD 90 ngày.";

    public static final String[] SHENRON_WISHES
            = new String[]{"Điều ước 1", "Điều ước 2", "Điều ước 3", "Điều ước 4"};

    public static final String SHENRON_BI_NGO_SAY
            = "Ta là Rồng Xương, ta sẽ ban cho ngươi 1 điều ước, ngươi có 5 phút, hãy chọn đi:\n"
            + "1) 50% tỉ lệ có Trứng đệ tử Mabư thường (hợp thể 60% chỉ số) (Trứng Mabư Vip săn boss 12h).\n"
            + "2) Thay đổi chiêu 4 đệ tử.\n"
            + "3) Tăng 20% may mắn (30 phút).\n"
            + "4) Tăng 10% HP, KI, Sức đánh (30 phút).\n"
            + "5) 200 Thỏi vàng.";

    public static final String[] SHENRON_BI_NGO_WISHES
            = new String[]{"50% Trứng\nMabư thường\n(HT 60%)", "Đổi chiêu 4\nđệ tử", "Tăng 20%\nMay mắn", "Tăng 10%\nHP, KI, SĐ", "200\nThỏi vàng"};

    public boolean shenronLeave;

    public void update() {
        try {
            if (!shenronLeave) {
                if (isPlayerDisconnect) {
                    Player pl = Client.gI().getPlayer(playerId);
                    if (pl != null) {
                        player = pl;
                        if (player.zone != null && player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14
                                && player.zone.map.mapId != 21 && player.zone.map.mapId != 22 && player.zone.map.mapId != 23) {
                            player.shenronEvent = this;
                            zone = player.zone;
                            player.idMark.setShenronType(shenronType);
                            isPlayerDisconnect = false;
                            reSummonShenron();
                        }
                    }
                }
                if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                    leaveMap = true;
                    NpcService.gI().createMenuRongThieng(player, ConstNpc.IGNORE_MENU, "Còn cái nịt =))\nCó không ước mất đừng tìm.", "Xin vĩnh biệt cụ........");
                    shenronLeave();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reSummonShenron() {
        activeShenron(true, DRAGON_EVENT);
        sendBlackGokuhesShenron();
    }

    public void sendBlackGokuhesShenron() {
        if (player.idMark.getShenronType() == 1) {
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_EVENT_CONFIRM, SHENRON_BI_NGO_SAY, SHENRON_BI_NGO_WISHES);
        } else {
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_EVENT_CONFIRM, SHENRONEVENT_SAY, SHENRON_WISHES);
        }
    }

    public void showConfirmShenron(byte select) {
        this.select = select;
        String wish = null;
        switch (player.idMark.getShenronType()) {
            case 0:
                if (select >= 0 && select < SHENRON_WISHES.length) {
                    wish = SHENRON_WISHES[select];
                }
                break;
            case 1:
                if (select >= 0 && select < SHENRON_BI_NGO_WISHES.length) {
                    wish = SHENRON_BI_NGO_WISHES[select];
                }
                break;
        }
        if (wish == null) {
            return;
        }
        NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_EVENT_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void activeShenron(boolean appear, byte type) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(player.zone.map.mapId);
                msg.writer().writeShort(player.zone.map.bgId);
                msg.writer().writeByte(player.zone.zoneId);
                msg.writer().writeInt((int) player.id);
                msg.writer().writeUTF("null");
                msg.writer().writeShort(player.location.x);
                msg.writer().writeShort(player.location.y);
                msg.writer().writeByte(type);
                playerId = player.id;
                shenronType = player.idMark.getShenronType();
                zone.shenronType = shenronType;
                lastTimeShenronWait = System.currentTimeMillis();
                player.isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
        }
    }

    public void confirmWish() {
        switch (player.idMark.getShenronType()) {
            case 0:
                switch (this.select) {
                     case 0: //thay chiêu 3-4 đệ tử
                        if (player.pet != null) {
                            if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                player.pet.openSkill3();
                                if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                    player.pet.openSkill4();
                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                    case 1:
                        if (player.getSession().player.nPoint.power >= 10_000_000_000L) {
                        IntrinsicService.gI().doinoitai(player);
                          } else {
                            Service.gI().sendThongBao(player, "10Tỷ Sức Mạnh?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                        
                    case 2:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            byte gender = this.player.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 905
                                    : gender == ConstPlayer.NAMEC ? 907 : 911));
                            avtVip.itemOptions.add(new Item.ItemOption(50, 22));
                            avtVip.itemOptions.add(new Item.ItemOption(47, 400));
                            avtVip.itemOptions.add(new Item.ItemOption(108, 30));
                            avtVip.itemOptions.add(new Item.ItemOption(33, 1));
                            avtVip.itemOptions.add(new Item.ItemOption(93, 90));
                            InventoryService.gI().addItemBag(player, avtVip);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy");
                            reSummonShenron();
                            return;
                        }
                        break;
                    case 3:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            byte gender = this.player.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 883
                                    : gender == ConstPlayer.NAMEC ? 883 : 883));
                            avtVip.itemOptions.add(new Item.ItemOption(50, 24));
                            avtVip.itemOptions.add(new Item.ItemOption(14, 3));
                            avtVip.itemOptions.add(new Item.ItemOption(103, 19));
                            avtVip.itemOptions.add(new Item.ItemOption(80, 10));
                            avtVip.itemOptions.add(new Item.ItemOption(93, 90));
                            InventoryService.gI().addItemBag(player, avtVip);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy");
                            reSummonShenron();
                            return;
                        }
                        break;
                    case 4:// Tăng hp, ki, sd
                        int timeRX = player.itemTime.timeRX / 1000 + 1800;
                        int maxTimeInSeconds = 32767;
                        if (timeRX >= maxTimeInSeconds) {
                            Service.gI().sendThongBao(player, "Ước ít thôi con :v");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemTime.isUseRX = true;
                        player.itemTime.timeRX = timeRX * 1000;
                        player.itemTime.lastTimeUseRX = System.currentTimeMillis();
                        ItemTimeService.gI().sendItemTime(player, 6581, timeRX);
                        player.nPoint.calPoint();
                        player.nPoint.setHp((int) player.nPoint.hpMax);
                        player.nPoint.setMp((int) player.nPoint.mpMax);
                        Service.gI().point(player);
                        Service.gI().Send_Info_NV(player);
                        break;
                    case 99: //quần đang đeo lên 1 cấp
                        Item item = this.player.inventory.itemsBody.get(1);
                        if (item.isNotNullItem()) {
                            int level = 0;
                            for (Item.ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id == 72) {
                                    level = io.param;
                                    if (level < 7) {
                                        io.param++;
                                    }
                                    break;
                                }
                            }
                            if (level < 7) {
                                if (level == 0) {
                                    item.itemOptions.add(new Item.ItemOption(72, 1));
                                }
                                for (Item.ItemOption io : item.itemOptions) {
                                    if (InventoryService.gI().optionCanUpgrade(io.optionTemplate.id)) {
                                        io.param += (io.param * 10 / 100);
                                    }
                                }
                                InventoryService.gI().sendItemBody(player);
                            } else {
                                Service.gI().sendThongBao(player, "Quần của ngươi đã đạt cấp tối đa");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi hiện tại có mang quần đâu");
                            sendBlackGokuhesShenron();
                            return;
                        }
                }
                break;
            case 1: // Rồng Xương (Ngọc Rồng Bí Ngô 702 - 708)
                switch (this.select) {
                    case 0: // 50% tỉ lệ có Trứng đệ tử Mabư thường (hợp thể 60% chỉ số)
                        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy, vui lòng dọn trống ít nhất 1 ô!");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        if (Util.isTrue(50, 100)) {
                            Item trungMabu = ItemService.gI().createNewItem((short) 568, 1);
                            InventoryService.gI().addItemBag(player, trungMabu);
                            InventoryService.gI().sendItemBags(player);
                            Service.gI().sendThongBao(player, "Chúc mừng! Ngươi đã may mắn nhận được Trứng Mabư thường (Hợp thể 60% chỉ số)!");
                        } else {
                            Service.gI().sendThongBao(player, "Rất tiếc! Ngươi chưa may mắn nhận được Trứng Mabư thường lần này (Tỉ lệ 50%).");
                        }
                        break;
                    case 1: // Thay đổi chiêu 4 đệ tử
                        if (player.pet != null) {
                            if (player.pet.playerSkill.skills.size() > 3 && player.pet.playerSkill.skills.get(3).skillId != -1) {
                                player.pet.openSkill4();
                                Service.gI().sendThongBao(player, "Đã thay đổi chiêu 4 cho đệ tử!");
                            } else {
                                Service.gI().sendThongBao(player, "Đệ tử của ngươi chưa mở chiêu 4!");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                    case 2: // Tăng 20% may mắn trong 30 phút
                        int currentRemainLuckRX = 0;
                        if (player.itemTime.isUseLuckRX) {
                            currentRemainLuckRX = Math.max(0, (int) ((player.itemTime.timeLuckRX - (System.currentTimeMillis() - player.itemTime.lastTimeUseLuckRX)) / 1000));
                        }
                        int timeLuckRX = currentRemainLuckRX + 1800;
                        if (timeLuckRX >= 32767) {
                            Service.gI().sendThongBao(player, "Thời gian hiệu lực May mắn Rồng Xương đã đạt tối đa!");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemTime.isUseLuckRX = true;
                        player.itemTime.timeLuckRX = timeLuckRX * 1000;
                        player.itemTime.lastTimeUseLuckRX = System.currentTimeMillis();
                        ItemTimeService.gI().sendItemTime(player, 6579, timeLuckRX);
                        Service.gI().sendThongBao(player, "Ngươi được Rồng Xương ban phúc tăng 20% May mắn trong 30 phút!");
                        break;
                    case 3: // Tăng 10% HP, KI, Sức đánh trong 30 phút
                        int currentRemainRX = 0;
                        if (player.itemTime.isUseRX) {
                            currentRemainRX = Math.max(0, (int) ((player.itemTime.timeRX - (System.currentTimeMillis() - player.itemTime.lastTimeUseRX)) / 1000));
                        }
                        int timeRX = currentRemainRX + 1800;
                        int maxTimeInSeconds = 32767;
                        if (timeRX >= maxTimeInSeconds) {
                            Service.gI().sendThongBao(player, "Thời gian hiệu lực Rồng Xương đã đạt tối đa!");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemTime.isUseRX = true;
                        player.itemTime.timeRX = timeRX * 1000;
                        player.itemTime.lastTimeUseRX = System.currentTimeMillis();
                        ItemTimeService.gI().sendItemTime(player, 6581, timeRX);
                        player.nPoint.calPoint();
                        player.nPoint.setHp(player.nPoint.hpMax);
                        player.nPoint.setMp(player.nPoint.mpMax);
                        Service.gI().point(player);
                        Service.gI().Send_Info_NV(player);
                        Service.gI().sendThongBao(player, "Ngươi được Rồng Xương ban phúc tăng 10% HP, KI, Sức đánh trong 30 phút!");
                        break;
                    case 4: // Nhận 200 Thỏi vàng
                        Item thoiVang = ItemService.gI().createNewItem((short) 457, 200);
                        if (!InventoryService.gI().addItemBag(player, thoiVang)) {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy, vui lòng dọn trống ít nhất 1 ô!");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Ngươi đã nhận được 200 Thỏi vàng từ Rồng Xương!");
                        break;
                }
                break;
        }
        shenronLeave();
    }

    public void shenronLeave() {
        if (!shenronLeave) {
            shenronLeave = true;
            if (player != null && player.zone != null) {
                player.shenronEvent = null;
                if (!leaveMap) {
                    NpcService.gI().createTutorial(player, 0, "Điều ước của ngươi đã được thực hiện...tạm biệt");
                }
                activeShenron(false, DRAGON_EVENT);
                player.isShenronAppear = false;
                select = -1;
            }
            zone.shenronType = -1;
            player.lastTimeShenronAppeared = System.currentTimeMillis();
            Shenron_Manager.gI().remove(this);
        }
    }
}
