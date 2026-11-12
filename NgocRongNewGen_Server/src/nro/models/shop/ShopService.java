package nro.models.shop;

import nro.models.consts.ConstAchievement;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.shop.ItemShop;
import nro.models.shop.Shop;
import nro.models.shop.TabShop;
import nro.models.network.Message;
import nro.models.item.Item.ItemOption;
import java.util.ArrayList;
import nro.models.server.Manager;
import nro.models.services.InventoryService;
import nro.models.utils.Logger;
import nro.models.utils.Util;
import java.util.List;
import nro.models.npc.MagicTree;
import nro.models.player_badges.BadgesData;
import nro.models.player_badges.BadgesService;
import nro.models.player_badges.BagesTemplate;
import nro.models.services.AchievementService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services_func.Input;
import nro.models.services_func.BuyBackService;
import nro.models.services_func.UseItem;
import nro.models.map.service.NpcService;
import nro.models.task.BadgesTaskService;
import nro.models.utils.SkillUtil;
import nro.models.utils.TimeUtil;

/**
 *
 * @author By Mr Blue
 *
 */
public class ShopService {

    private static final byte COST_GOLD = 0;
    private static final byte COST_GEM = 1;
    private static final byte COST_RUBY = 3;
    private static final byte COST_COUPON = 4;

    private static final byte NORMAL_SHOP = 0;
    private static final byte SPEC_SHOP = 3;
    private static final byte KINANG_SHOP = 1;
    private int eventPointPrice;

    private static ShopService I;

    public static ShopService gI() {
        if (ShopService.I == null) {
            ShopService.I = new ShopService();
        }
        return ShopService.I;
    }

    public void opendShop(Player player, String tagName, boolean allGender) {
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            openShopType4(player, tagName, player.inventory.itemsBoxCrackBall);
            return;
        } else if (tagName.equals("ITEMS_DABAN")) {
            return;
        }
        try {
            Shop shop = this.getShop(tagName);
            for (TabShop tabShop : shop.tabShops) {
                for (ItemShop item : tabShop.itemShops) {
                    switch (item.temp.id) {
                        case 1627:// hành trang
                            if (player.inventory.itemsBag.size() >= 35) {
                                item.cost = ((player.inventory.itemsBag.size() - 35) + 1) * 2;
                            } else {
                                item.cost = 1;
                            }
                            break;
                    }
                }
            }
            shop = this.resolveShop(player, shop, allGender);
            switch (shop.typeShop) {
                case KINANG_SHOP:
                    openShopType1(player, shop);
                    break;
                case NORMAL_SHOP:
                    openShopType0(player, shop);
                    break;
                case SPEC_SHOP:
                    openShopType3(player, shop);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Service.gI().sendThongBao(player, ex.getMessage());
        }
    }

    private Shop getShop(String tagName) throws Exception {
        for (Shop s : Manager.SHOPS) {
            if (s.tagName != null && s.tagName.equals(tagName)) {
                return s;
            }
        }
        throw new Exception("Shop " + tagName + " không tồn tại!");
    }

    private Shop resolveShop(Player player, Shop shop, boolean allGender) {
        if (shop.tagName != null && (shop.tagName.equals("BUA_1H") || shop.tagName.equals("BUA_8H") || shop.tagName.equals("BUA_1M"))) {
            return this.resolveShopBua(player, new Shop(shop));
        }
        return allGender ? new Shop(shop) : new Shop(shop, player);
    }

    private Shop resolveShopBua(Player player, Shop s) {
        for (TabShop tabShop : s.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                long min = 0;
                switch (item.temp.id) {
                    case 213:
                        long timeTriTue = player.charms.tdTriTue;
                        long current = System.currentTimeMillis();
                        min = (timeTriTue - current) / 60000;

                        break;
                    case 214:
                        min = (player.charms.tdManhMe - System.currentTimeMillis()) / 60000;
                        break;
                    case 215:
                        min = (player.charms.tdDaTrau - System.currentTimeMillis()) / 60000;
                        break;
                    case 216:
                        min = (player.charms.tdOaiHung - System.currentTimeMillis()) / 60000;
                        break;
                    case 217:
                        min = (player.charms.tdBatTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 218:
                        min = (player.charms.tdDeoDai - System.currentTimeMillis()) / 60000;
                        break;
                    case 219:
                        min = (player.charms.tdThuHut - System.currentTimeMillis()) / 60000;
                        break;
                    case 522:
                        min = (player.charms.tdDeTu - System.currentTimeMillis()) / 60000;
                        break;
                    case 671:
                        min = (player.charms.tdTriTue3 - System.currentTimeMillis()) / 60000;
                        break;
                    case 672:
                        min = (player.charms.tdTriTue4 - System.currentTimeMillis()) / 60000;
                        break;
                }
                if (min > 0) {
                    item.options.clear();
                    if (min >= 1440) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new Item.ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new Item.ItemOption(65, (int) min));
                    }
                }
            }
        }
        return s;
    }

    private void openShopType0(Player player, Shop shop) {
        if (shop != null) {
            player.idMark.setShopOpen(shop);
            player.idMark.setTagNameShop(shop.tagName);
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(NORMAL_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        if (itemShop.typeSell == COST_GOLD) {
                            msg.writer().writeInt(itemShop.cost);
                            msg.writer().writeInt(0);
                        } else if (itemShop.typeSell == COST_GEM) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_RUBY) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_COUPON) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        }
                        msg.writer().writeByte(itemShop.options.size());
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopDanhHieu(Player player, Shop shop) {
        if (shop != null) {
            player.idMark.setShopOpen(shop);
            player.idMark.setTagNameShop(shop.tagName);
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(NORMAL_SHOP);
                msg.writer().writeByte(2);
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        if (itemShop.typeSell == COST_GOLD) {
                            msg.writer().writeInt(itemShop.cost);
                            msg.writer().writeInt(0);
                        } else if (itemShop.typeSell == COST_GEM) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_RUBY) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_COUPON) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        }
                        msg.writer().writeByte(itemShop.options.size());
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }

                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopType1(Player player, Shop shop) {
        if (shop != null) {
            player.idMark.setShopOpen(shop);
            player.idMark.setTagNameShop(shop.tagName);
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(KINANG_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        String[] subName = itemShop.temp.name.split("");
                        byte level = Byte.parseByte(subName[subName.length - 1]);

                        var skillTemplateId = SkillUtil.getTempSkillSkillByItemID(itemShop.temp.id);
                        var costPotential = SkillUtil.findSkillTemplate(skillTemplateId).skillss.stream()
                                .filter(s -> s.point == level)
                                .findFirst()
                                .map(s -> (int) s.powRequire) // Ép kiểu Long -> int
                                .orElse(0); // Giá trị mặc định là int
                        msg.writer().writeLong(costPotential);

                        msg.writer().writeByte(itemShop.options.size());
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);

                        msg.writer().writeByte(0);

                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private short resolveSpecIcon(ItemShop itemShop) {
        if (itemShop.iconSpec > 0) {
            return (short) itemShop.iconSpec;
        }
        return switch (itemShop.typeSell) {
            case COST_GOLD -> 930;
            case COST_GEM -> 932;
            case COST_RUBY -> 7743;
            default -> 932;
        };
    }

    private void openShopType3(Player player, Shop shop) {
        player.idMark.setShopOpen(shop);
        player.idMark.setTagNameShop(shop.tagName);
        if (shop != null) {
            Message msg = null;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(SPEC_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        msg.writer().writeShort(resolveSpecIcon(itemShop));
                        msg.writer().writeInt(itemShop.cost);
                        int packMultDisplay = getPackMultiplier(shop, itemShop);
                        msg.writer().writeByte(itemShop.options.size() + (packMultDisplay > 1 ? 1 : 0));
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        if (packMultDisplay > 1) {
                            msg.writer().writeByte(31);
                            msg.writer().writeShort(packMultDisplay);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(ShopService.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    private void openShopType4(Player player, String tagName, List<Item> items) {
        if (items == null) {
            return;
        }
        player.idMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Phần\nthưởng");
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("|7| LUCKY REWARD");
                msg.writer().writeByte(item.itemOptions.size() + 1);
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeByte(io.optionTemplate.id);
                    msg.writer().writeShort(io.param);
                }
                //số lượng
                msg.writer().writeByte(31);
                msg.writer().writeShort(item.quantity);
                //
                msg.writer().writeByte(1);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openShopType8(Player player, String tagName, List<Item> items) {
        if (items == null) {
            return;
        }
        player.idMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(8);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Mua lại");
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                int giamualaingoc = item.template.gem / 2;
                int giamualaivang = giamualaingoc == 0 ? (int) item.template.gold / 2 > 0 ? (int) item.template.gold / 2 : item.quantity * 100 : 0;
                msg.writer().writeShort(item.template.id);
                msg.writer().writeInt(giamualaivang);
                msg.writer().writeInt(giamualaingoc);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeByte(item.itemOptions.size());
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeByte(io.optionTemplate.id);
                    msg.writer().writeShort(io.param);
                }
                msg.writer().writeByte(0);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void takeItem(Player player, byte type, int tempId) {
        String tagName = player.idMark.getTagNameShop();
        if (tagName == null || tagName.length() <= 0) {
            return;
        }
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            getItemSideBoxLuckyRound(player, player.inventory.itemsBoxCrackBall, type, tempId);
            return;
        } else if (tagName.equals("ITEMS_REWARD")) {
            return;
        } else if (tagName.equals("ITEMS_DABAN")) {
            return;
        } else if (tagName.equals("BILL")) {
            buyItemHD(player, tempId);
            return;
        }

        if (player.idMark.getShopOpen() == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (tagName.equals("BUA_1H") || tagName.equals("BUA_8H") || tagName.equals("BUA_1M")) {
            buyItemBua(player, tempId);
        } else if (tagName.equals("SANTA_HEAD")) {
            Item itS = ItemService.gI().createNewItem((short) tempId);
            player.head = (short) itS.template.head;
            Service.gI().Send_Caitrang(player);
            Service.gI().point(player);
            Service.gI().sendThongBao(player, "Đổi kiểu tóc thành công");
        } else {
            buyItem(player, tempId);
        }
        Service.gI().sendMoney(player);
        nro.models.database.PlayerDAO.savePlayerAsync(player);
    }

    private boolean subMoneyByItemShop(Player player, ItemShop is) {
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        int coupon = 0;
        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;
        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng");
            return false;
        } else if (player.inventory.gem < gem) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
            return false;
        } else if (player.inventory.gem < ruby) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
            return false;
        } else if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBao(player, "Bạn không có đủ điểm");
            return false;
        }
        player.inventory.gold -= gold;
        player.inventory.gem -= gem;
        player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        return true;
    }

    private boolean subMoneyByItemShopV2(Player player, ItemShop is) {
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        int coupon = 0;
        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;
        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(player.inventory.gold - gold));
            return false;
        } else if (player.inventory.gem < gem) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ ngọc, còn thiếu " + Util.numberToMoney(player.inventory.gem - gem));
            return false;
        } else if (player.inventory.ruby < ruby) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ ngọc, còn thiếu " + Util.numberToMoney(player.inventory.ruby - ruby));
            return false;
        } else if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ điểm, còn thiếu " + Util.numberToMoney(player.inventory.coupon - coupon));
            return false;
        }
        player.inventory.gold -= gold;
        player.inventory.gem -= gem;
        player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        Service.gI().sendMoney(player);
        return true;
    }

    /**
     * Mua bùa
     *
     * @param player người chơi
     * @param itemTempId id template vật phẩm
     */
    private void buyItemBua(Player player, int itemTempId) {
        Shop shop = player.idMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (!subMoneyByItemShop(player, is)) {
            return;
        }
        InventoryService.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
        InventoryService.gI().sendItemBags(player);
        opendShop(player, shop.tagName, true);
    }

    /**
     * Mua vật phẩm trong cửa hàng
     *
     * @param player người chơi
     * @param itemTempId id template vật phẩm
     */
    private void learnKyNang(Player pl, ItemShop is) {

        if (pl.nPoint.power < is.temp.strRequire) {
            Service.gI().sendThongBao(pl, "Sức mạnh của bạn không đủ");
            return;
        }
        if (pl.nPoint.tiemNang < is.cost) {
            Service.gI().sendThongBao(pl, "Bạn không đủ tiềm năng để học chiêu thức này");
            return;
        }
        var skillPlayer = pl.playerSkill.getSkillbyId(SkillUtil.getSkillByItemID(pl, is.temp.id).template.id);
        String[] subName = is.temp.name.split("");
        byte level = Byte.parseByte(subName[subName.length - 1]);
        if (skillPlayer != null) {

            if (skillPlayer.point >= level) {
                Service.gI().sendThongBao(pl, "Bạn đã học kỹ năng này rồi");
                return;

            }
            if (level - skillPlayer.point != 1) {
                Service.gI().sendThongBao(pl, "Bạn chưa thể học kỹ năng này");
                return;
            }
        }
        if (pl.BoughtSkill.contains(is.temp.id)) {
            Service.gI().sendThongBao(pl, "Bạn đã học kỹ năng này rồi");
            return;
        }
        ArrayList<String> menu = new ArrayList<>();
        menu.add("Yes");
        menu.add("No");
        String[] menus = menu.toArray(String[]::new);
        long[] time = new long[]{900000, 1800000, 3600000, 86400000, 259200000, 604800000, 1296000000};
        var timeStudy = "";
        var timeLong = time[level - 1];
        switch (level) {
            case 0:
            case 1:
            case 2:
                timeStudy = TimeUtil.convertMillisecondToMinute(timeLong);
                break;
            case 3:
                timeStudy = TimeUtil.convertMillisecondToHour(timeLong);
                break;
            default:
                timeStudy = TimeUtil.convertMillisecondToDay(timeLong);
                break;
        }
        var skillTemplateId = SkillUtil.getTempSkillSkillByItemID(is.temp.id);

        var potential = SkillUtil.findSkillTemplate(skillTemplateId).skillss.stream()
                .filter(s -> s.point == level)
                .findFirst()
                .map(s -> (int) s.powRequire) // Ép kiểu Long -> int
                .orElse(0); // Giá trị mặc định là int
        String text = "Con có muốn học kỹ năng " + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(is.temp.id)).name + " cấp " + level + "\nCần " + potential + " điểm tiềm năng và thời gian học là " + timeStudy;
        pl.LearnSkill.ItemTemplateSkillId = is.temp.id;
        pl.LearnSkill.Time = -1;
        pl.LearnSkill.Potential = potential;

        NpcService.gI().createMenuConMeo(pl, 671, NpcService.gI().getAvatar(13 + pl.gender), text, menus);
    }

    public void buyItem(Player player, int itemTempId) {
        Shop shop = player.idMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        int[][] listDauThan = {{13, 293}, {60, 294}, {61, 295}, {62, 296}, {63, 297}, {64, 298}, {65, 299}, {352, 596}, {523, 597}};

        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }

        // Đổi bằng phiếu giảm giá
        if (is.tabShop.id == 30) {
            if (InventoryService.gI().subQuantityItemsBagPriorityLocked(player, 459, 1)) {
                Item item = ItemService.gI().createItemFromItemShop(is);
                InventoryService.gI().addItemBag(player, item);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Đổi thành công " + is.temp.name);
            } else {
                Service.gI().sendThongBao(player, "Bạn không có phiếu giảm giá!");
            }
            return;
        }

        // Đổi danh hiệu
        if (is.tabShop.id == 44) {
            buyDanhHieu(player, is);
            return;
        }

        // Đổi danh hiệu khác
        if (is.tabShop.id == 45) {
            changeDanhHieu(player, is);
            return;
        }

        // Shop chipi
        if (is.tabShop.id == 49) {
            Item item = ItemService.gI().createItemFromItemShop(is);
            if (Util.isTrue(5, 100)) {
                item.itemOptions.add(new ItemOption(73, 0)); // HSD vĩnh viễn
            } else {
                item.itemOptions.add(new ItemOption(93, Util.nextInt(3, 7))); // HSD 3-7 ngày
            }
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            return;
        }

        // Shop kỹ năng
        if (shop.typeShop == ShopService.KINANG_SHOP) {
            learnKyNang(player, is);
            return;
        }

        // Hành trang đầy
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy");
            return;
        }

        // Yêu cầu có cải trang Quy Lão Kame
        if (itemTempId == 711 && !InventoryService.gI().findItemSkinQuyLaoKame(player)) {
            Service.gI().sendThongBao(player, "Bạn phải có cải trang thành Quy Lão Kame mới có thể đổi.");
            return;
        }

        // Kiểm tra auto train
        if (itemTempId == 1524 || itemTempId == 1523 || itemTempId == 521) {
            if (!checkAutoTrainPurchase(player, itemTempId)) {
                return;
            }
        }

        // Đổi bằng điểm sự kiện
        if (is.tabShop.id == 59) {
            int eventPointPrice = 0;

            switch (is.temp.id) {
                case 1567:
                case 1731:
                    eventPointPrice = 999;
                    break;
                case 1711:
                    eventPointPrice = 750;
                    break;
                case 1840:
                    eventPointPrice = 99;
                    break;
                case 1713:
                    eventPointPrice = 499;
                    break;
                case 1608:
                    eventPointPrice = 9;
                    break;
                case 1682:
                    eventPointPrice = 499;
                    break;
                case 1698:
                    eventPointPrice = 499;
                    break;
                case 1821:
                    eventPointPrice = 199;
                    break;
                case 1757:
                    eventPointPrice = 99;
                    break;
                case 1592:
                    eventPointPrice = 99;
                    break;
                default:
            }

            if (player.event.getEventPoint() < eventPointPrice) {
                Service.gI().sendThongBao(player, "Không đủ điểm sự kiện để mua vật phẩm này!");
                return;
            }

            player.event.subEventPoint(eventPointPrice);
            Item item = ItemService.gI().createItemFromItemShop(is);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Đã đổi " + is.temp.name + " bằng " + eventPointPrice + " điểm sự kiện.");
            return;
        }
        // Đổi bằng điểm Capsule Bang
        if (is.tabShop.id == 60 || is.tabShop.id == 61 || is.tabShop.id == 62) {
            if (is.temp.id == 1423) {
                Service.gI().sendThongBaoOK(player,
                        "Hematite là vật phẩm giới thiệu (Không bán tại Shop Bang).\n"
                        + "Nguồn săn, chế tạo & mua:\n"
                        + "- Hạ Boss bất kỳ: 30% rơi x5 viên\n"
                        + "- Bà Hạt Mít: Ghép x20 Sao Pha Lê cấp 1 cùng loại + 50Tr vàng nhận 1 viên đá Hematite (tỉ lệ 100%)\n"
                        + "- Shop Santa (Cửa hàng): Mua với giá 100 Thỏi vàng");
                return;
            }
            if (is.temp.id == 1438) {
                Service.gI().sendThongBaoOK(player,
                        "Dùi đục là vật phẩm giới thiệu (Không bán tại Shop Bang).\n"
                        + "Nguồn săn, chế tạo & mua:\n"
                        + "- Hạ Boss bất kỳ: 10% rơi x5 viên\n"
                        + "- Bà Hạt Mít: Chế tạo từ x5 đá Hematite + 50Tr vàng\n"
                        + "- Shop Santa (Cửa hàng): Mua với giá 500 Thỏi vàng");
                return;
            }
            if (is.temp.id == 1439) {
                Service.gI().sendThongBaoOK(player,
                        "Đá mài là vật phẩm giới thiệu (Không bán tại Shop Bang).\n"
                        + "Nguồn săn, chế tạo & mua:\n"
                        + "- Hạ Boss bất kỳ: 10% rơi x5 viên\n"
                        + "- Bà Hạt Mít: Chế tạo từ x5 Dùi đục + 50Tr vàng\n"
                        + "- Shop Santa (Cửa hàng): Mua với giá 500 Thỏi vàng");
                return;
            }

            int capsuleClanPointPrice = 0;

            switch (is.temp.id) {
                case 1794:
                case 1204:
                case 987:
                case 1635:
                    capsuleClanPointPrice = 1;
                    break;
                case 1790:
                    capsuleClanPointPrice = 2;
                    break;
                case 1791:
                    capsuleClanPointPrice = 3;
                    break;
                case 1792:
                    capsuleClanPointPrice = 4;
                    break;
                case 1634:
                    capsuleClanPointPrice = 10;
                    break;
                case 1620:
                case 1748:
                case 1750:
                case 1729:
                case 1727:
                case 1714:
                case 1683:
                case 1682:
                case 1668:
                case 1629:
                case 1630:
                case 1631:
                case 1573:
                case 1550:
                case 1551:
                case 1541:
                case 1563:
                case 1724:
                case 1733:
                case 1734:
                case 1749:
                    capsuleClanPointPrice = 50;
                    break;
                default:
                    capsuleClanPointPrice = -1;
                    break;
            }

            if (capsuleClanPointPrice <= 0) {
                Service.gI().sendThongBao(player, "Vật phẩm này không thể mua bằng điểm Capsule Bang.");
                return;
            }

            if (player.clan == null) {
                Service.gI().sendThongBao(player, "Bạn không có trong bang hội!");
                return;
            }

            if (player.clan.capsuleClan < capsuleClanPointPrice) {
                Service.gI().sendThongBao(player, "Bang hội không đủ điểm Capsule Bang để mua vật phẩm này!");
                return;
            }

            player.clan.capsuleClan -= capsuleClanPointPrice;

            Item item = ItemService.gI().createItemFromItemShop(is);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);

            Service.gI().sendThongBao(player, "Đã đổi " + is.temp.name + " bằng " + capsuleClanPointPrice + " điểm Capsule Bang của bang hội.");
            return;
        }

        // Mua trực tiếp x1 khi bấm "Mua bằng X" (và tương thích mượt với nút Mua 20 / 50 / 100 lần của Client, không bật hộp thoại hỏi lại)
        if (shouldOpenBulkBuyMenu(shop, is, listDauThan)) {
            player.pendingBulkItemShop = is;
            executeBulkBuy(player, 1);
            return;
        }

        // Shop thường
        String specPaymentDetail = "";
        boolean usedLockedGoldBar = false;
        if (shop.typeShop == ShopService.NORMAL_SHOP) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }
        } // Shop đặc biệt
        else if (shop.typeShop == ShopService.SPEC_SHOP) {
            short effectiveIcon = resolveSpecIcon(is);
            short itSpec = ItemService.gI().getItemIdByIcon(effectiveIcon);
            int lockedBefore = (itSpec != 76 && itSpec != 188 && itSpec != 189 && itSpec != 190 && itSpec != 77 && itSpec != 861)
                    ? InventoryService.gI().getTotalLockedQuantityBag(player, itSpec) : 0;
            if (!this.subIemByItemShop(player, is)) {
                return;
            }
            if (lockedBefore > 0 && is.cost > 0) {
                int usedLocked = Math.min(lockedBefore, is.cost);
                int usedUnlocked = is.cost - usedLocked;
                if (itSpec == 457 && usedLocked > 0) {
                    usedLockedGoldBar = true;
                }
                String unitName = getShopCurrencyUnitName(shop, is);
                specPaymentDetail = usedUnlocked > 0
                        ? " (-" + Util.numberToMoney(usedLocked) + " " + unitName + " khóa, -" + Util.numberToMoney(usedUnlocked) + " " + unitName + " thường)"
                        : " (-" + Util.numberToMoney(usedLocked) + " " + unitName + " khóa)";
            }
        }

        // Tạo item và xử lý đặc biệt
        Item item = ItemService.gI().createItemFromItemShop(is);
        item = buyMagicPean(player, listDauThan, item);
        if (item.template.id == 1523 || item.template.id == 1524) {
            item = ItemService.gI().createNewItem((short) 521);
            item.itemOptions.addAll(is.options);
        }
        if (usedLockedGoldBar && item != null && item.isPhuKien() && !item.isHaveOption(30)) {
            item.itemOptions.add(new ItemOption(30, 0));
            specPaymentDetail += " [Khóa giao dịch]";
        }
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name + specPaymentDetail);

        if (itemTempId == 1523 || itemTempId == 1524 || itemTempId == 521) {
            updateAutoTrainPurchase(player, itemTempId);
            Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
        }
    }

    private boolean checkAutoTrainPurchase(Player player, int itemTempId) {
        int autoTrainState = player.autoTrainState;
        if (itemTempId == 1524 && autoTrainState != 2) {
            Service.gI().sendThongBao(player, "Bạn cần mua Tự động luyện tập 2 trước!");
            return false;
        } else if (itemTempId == 1523 && autoTrainState != 1) {  // sửa lại thành 'else if'
            Service.gI().sendThongBao(player, "Bạn cần mua Tự động luyện tập 1 trước!");
            return false;
        }

        return true;
    }

    private void updateAutoTrainPurchase(Player player, int itemTempId) {

        if (itemTempId == 1524) {
            player.autoTrainState = 0;

        } // Nếu mua Tự động luyện tập 1 (với ID 521)
        else if (itemTempId == 521) {
            // Nếu player đã có autoTrainState = 2, không thay đổi trạng thái nữa
            if (player.autoTrainState != 2) {
                player.autoTrainState = 1;
            }
        } else if (itemTempId == 1523) {
            player.autoTrainState = 2;
        }
    }

    private void buyDanhHieu(Player pl, ItemShop is) {
        int idEffect = BagesTemplate.fineIdEffectbyIdItem(is.temp.id);
        int percent = BadgesTaskService.sendPercenBadgesTask(pl, idEffect);

        if (percent < 100) {
            Service.gI().sendThongBao(pl, "Bạn chưa mở khóa danh hiệu này");
            return;
        }

        for (BadgesData badge : pl.dataBadges) {
            if (badge.idBadGes == idEffect) {
                Service.gI().sendThongBao(pl, "Bạn đã sở hữu danh hiệu này rồi");
                return;
            }
        }

        BadgesData danhHieu = new BadgesData(pl, idEffect, 30);
        pl.dataBadges.add(danhHieu);

        BagesTemplate template = BagesTemplate.fineBadgesbyIdItem(is.temp.id);
        String badgeName = template != null ? template.NAME : "không rõ";

        Service.gI().sendThongBao(pl, "Bạn đã nhận được danh hiệu " + badgeName);
    }

    private void changeDanhHieu(Player pl, ItemShop is) {
        if (pl.lastTimeChangeBadges - System.currentTimeMillis() > 0) {
            Service.gI().sendThongBao(pl, "Vui lòng đợi " + (pl.lastTimeChangeBadges - System.currentTimeMillis()) / 1000 + " giây nữa");
            return;
        }
        if (pl.badges.idBadges == BagesTemplate.fineIdEffectbyIdItem(is.temp.id)) {
            Service.gI().sendThongBao(pl, "Danh hiệu đang được sữ dụng, hãy chọn danh hiệu khác");
            pl.lastTimeChangeBadges = System.currentTimeMillis() + 3000;
            return;
        }
        BadgesService.turnOnBadges(pl, BagesTemplate.fineIdEffectbyIdItem(is.temp.id));
        Service.gI().sendThongBao(pl, "Đã đổi danh hiệu sang " + is.temp.name);
        pl.lastTimeChangeBadges = System.currentTimeMillis() + 3000;
        pl.nPoint.calPoint();
        pl.nPoint.setHp((int) pl.nPoint.hpMax);
        pl.nPoint.setMp((int) pl.nPoint.mpMax);
        pl.nPoint.setDame((int) pl.nPoint.dame);
        Service.gI().point(pl);
        Service.gI().Send_Info_NV(pl);
    }

    private boolean subIemByItemShop(Player pl, ItemShop itemShop) {
        boolean isBuy = false;
        short effectiveIcon = resolveSpecIcon(itemShop);
        short itSpec = ItemService.gI().getItemIdByIcon(effectiveIcon);
        int buySpec = itemShop.cost;
        Item itS = ItemService.gI().createNewItem(itSpec);
        switch (itS.template.id) {
            case 76:
            case 188:
            case 189:
            case 190:
                if (pl.inventory.gold >= buySpec) {
                    pl.inventory.gold -= buySpec;
                    isBuy = true;
                } else {
                    Service.gI().sendThongBao(pl, "Bạn Không Đủ Vàng Để Mua Vật Phẩm");
                    isBuy = false;
                }
                break;
            case 77:
                if (pl.inventory.gem >= buySpec) {
                    pl.inventory.gem -= buySpec;
                    isBuy = true;
                } else {
                    Service.gI().sendThongBao(pl, "Bạn Không Đủ Ngọc Để Mua Vật Phẩm");
                    isBuy = false;
                }
                break;
            case 861:
                if (pl.inventory.ruby >= buySpec) {
                    pl.inventory.ruby -= buySpec;
                    isBuy = true;
                } else {
                    Service.gI().sendThongBao(pl, "Bạn Không Đủ Hồng Ngọc Để Mua Vật Phẩm");
                    isBuy = false;
                }
                break;
            default:
                int totalHave = InventoryService.gI().getTotalQuantityBag(pl, itSpec);
                if (totalHave <= 0) {
                    Service.gI().sendThongBao(pl, "Không tìm thấy " + itS.template.name);
                    isBuy = false;
                } else if (totalHave < buySpec) {
                    Service.gI().sendThongBao(pl, "Bạn không có đủ " + buySpec + " " + itS.template.name);
                    isBuy = false;
                } else {
                    InventoryService.gI().subQuantityItemsBagPriorityLocked(pl, itSpec, buySpec);
                    isBuy = true;
                }
                break;
        }
        return isBuy;
    }

    private boolean isSkhEquipment(Item item) {
        if (item == null || !item.isNotNullItem() || item.template == null || item.itemOptions == null) {
            return false;
        }
        if (item.template.type < 0 || item.template.type >= 5) {
            return false;
        }
        for (ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null
                    && nro.models.services.SkhCollectionService.isAnySkhOptionId(io.optionTemplate.id)) {
                return true;
            }
        }
        return false;
    }

    public void showConfirmSellItem(Player pl, int where, int index) {
        Item item = null;
        if (where == 0) {
            if (index < 0) {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                return;
            }
            item = pl.inventory.itemsBody.get(index);
        } else {
            if (pl.getSession().version < 220) {
                index -= (pl.inventory.itemsBody.size() - 7);
            }
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            int quantity = item.quantity;
            String text;
            if (isSkhEquipment(item)) {
                text = "Bạn có muốn bán\nx" + quantity
                        + " " + item.template.name + "\nvới giá là " + quantity + " Thỏi vàng?";
            } else {
                long cost = item.template.gold;
                if (item.template.id == 457) {
                    if (quantity >= 1) {
                        Input.gI().createFormBanSLL(pl);
                        return;
                    }
                    quantity = 1;
                    cost = Inventory.GOLD_BAR_SELL_PRICE;
                } else {
                    cost /= 4;
                }
                if (cost <= 0) {
                    cost = 1;
                }
                cost *= quantity;

                text = "Bạn có muốn bán\nx" + quantity
                        + " " + item.template.name + "\nvới giá là " + Util.numberToMoney(cost) + " vàng?";
            }
            Message msg = null;
            try {
                msg = new Message(7);
                msg.writer().writeByte(where);
                msg.writer().writeShort(index);
                msg.writer().writeUTF(text);
                pl.sendMessage(msg);
            } catch (Exception e) {
            e.printStackTrace();
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void sellItem(Player pl, int where, int index) {
        if (pl.idMark.getShopOpen() == null || pl.idMark.getTagNameShop() == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        if (index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        Item item = null;
        if (where == 0) {
            item = pl.inventory.itemsBody.get(index);
        } else {
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            if (InventoryService.gI().getParam(pl, 93, item.template.id) > 0) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm có hạn sử dụng");
                return;
            }
            int quantity = item.quantity;
            if (isSkhEquipment(item)) {
                String soldName = item.template.name;
                if (where == 0) {
                    Item thoiVang = ItemService.gI().createNewItem((short) 457, quantity);
                    if (!InventoryService.gI().addItemBag(pl, thoiVang)) {
                        Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống để nhận Thỏi vàng");
                        return;
                    }
                    InventoryService.gI().subQuantityItemsBody(pl, item, quantity);
                    InventoryService.gI().sendItemBody(pl);
                    Service.gI().Send_Caitrang(pl);
                    InventoryService.gI().sendItemBags(pl);
                } else {
                    InventoryService.gI().subQuantityItemsBag(pl, item, quantity);
                    Item thoiVang = ItemService.gI().createNewItem((short) 457, quantity);
                    InventoryService.gI().addItemBag(pl, thoiVang);
                    InventoryService.gI().sendItemBags(pl);
                }
                Service.gI().sendThongBao(pl, "Đã bán " + soldName + " thu được " + quantity + " Thỏi vàng");
                if ("BUNMA".equals(pl.idMark.getTagNameShop())
                        || "DENDE".equals(pl.idMark.getTagNameShop())
                        || "APPULE".equals(pl.idMark.getTagNameShop())) {
                    AchievementService.gI().checkDoneTask(pl, ConstAchievement.TRUM_NHAT_VE_CHAI);
                }
                nro.models.database.PlayerDAO.savePlayerAsync(pl);
                return;
            }
            long cost = item.template.gold;
            if (item.template.id == 457) {
                quantity = 1;
                cost = Inventory.GOLD_BAR_SELL_PRICE;
            } else {
                cost /= 4;
            }
            if (cost <= 0) {
                cost = 1;
            }
            cost *= quantity;

            if (pl.inventory.gold > Inventory.LIMIT_GOLD - cost) {
                Service.gI().sendThongBao(pl, "Vàng sau khi bán vượt quá giới hạn 500 Tỷ vàng");
                return;
            }
            pl.inventory.addGold(cost);
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Đã bán " + item.template.name
                    + " thu được " + Util.numberToMoney(cost) + " vàng");

            //Add vật phẩm đã bán
            if (item.template.id != 457) {
                BuyBackService.gI().addItem(pl, item);
            }
            if (where == 0) {
                InventoryService.gI().subQuantityItemsBody(pl, item, quantity);
                InventoryService.gI().sendItemBody(pl);
                Service.gI().Send_Caitrang(pl);
            } else {
                InventoryService.gI().subQuantityItemsBag(pl, item, quantity);
                InventoryService.gI().sendItemBags(pl);
            }
            if ("BUNMA".equals(pl.idMark.getTagNameShop())
                    || "DENDE".equals(pl.idMark.getTagNameShop())
                    || "APPULE".equals(pl.idMark.getTagNameShop())) {
                AchievementService.gI().checkDoneTask(pl, ConstAchievement.TRUM_NHAT_VE_CHAI);
            }
            nro.models.database.PlayerDAO.savePlayerAsync(pl);
        } else {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void getItemSideBoxLuckyRound(Player player, List<Item> items, byte type, int index) {
        if (items == null) {
            return;
        }
        if (index < 0 || index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        switch (type) {
            case 0: //nhận
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                        InventoryService.gI().addItemBag(player, item);
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                        InventoryService.gI().sendItemBags(player);
                        items.remove(index);
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đã đầy");
                    }
                } else {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                }
                break;
            case 1: //xóa
                items.remove(index);
                Service.gI().sendThongBao(player, "Xóa vật phẩm thành công");
                break;
            case 2: //nhận hết
                for (int i = items.size() - 1; i >= 0; i--) {
                    item = items.get(i);
                    if (InventoryService.gI().addItemBag(player, item)) {
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                        items.remove(i);
                    }
                }
                InventoryService.gI().sendItemBags(player);
                break;
        }
        openShopType4(player, player.idMark.getTagNameShop(), items);
    }

    private void buyItemDaBan(Player player, List<Item> items, byte type, int index) {
        if (items == null) {
            return;
        }
        if (index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        int giamualaingoc = item.template.gem / 2;
        int giamualaivang = giamualaingoc == 0 ? (int) item.template.gold / 2 > 0 ? (int) item.template.gold / 2 : item.quantity * 100 : 0;
        if (giamualaivang > 0 && player.inventory.gold < giamualaivang) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng!");
            return;
        }
        if (giamualaingoc > 0 && player.inventory.gem < giamualaingoc) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc xanh!");
            return;
        }
        player.inventory.gem -= giamualaingoc;
        player.inventory.gold -= giamualaivang;
        Service.gI().sendMoney(player);
        if (item.isNotNullItem()) {
            if (InventoryService.gI().getCountEmptyBag(player) != 0) {
                InventoryService.gI().addItemBag(player, item);
                Service.gI().sendThongBao(player,
                        "Bạn nhận được " + (item.template.id == 189
                                ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                InventoryService.gI().sendItemBags(player);
                items.remove(index);
            } else {
                Service.gI().sendThongBao(player, "Hành trang đã đầy");
            }
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
        openShopType8(player, player.idMark.getTagNameShop(), items);
    }

    private void buyItemHD(Player player, int itemTempId) {
        Shop shop = player.idMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = ItemService.gI().createItemFromItemShop(is);
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy, không thể chứa thêm.");
            return;
        }
        if (!InventoryService.gI().fullSetThan(player)) {
            Service.gI().sendThongBao(player, "Bạn cần mặc đủ bộ 5 món Đồ Thần Linh trên người!");
            return;
        }
        Item doAn = null;
        if (item.template.level == 14) {
            doAn = player.inventory.itemsBag.stream()
                    .filter(it -> it != null && it.template != null
                    && (it.template.id >= 663 && it.template.id <= 667) && it.quantity >= 99)
                    .findFirst().orElse(null);
            if (doAn == null) {
                Service.gI().sendThongBao(player, "Không có đủ 99 phần thức ăn cùng loại!");
                return;
            }
        }
        if (!subMoneyByItemShopV2(player, is)) {
            return;
        }
        if (doAn != null) {
            InventoryService.gI().subQuantityItemsBag(player, doAn, 99);
        }
        Service.gI().sendMoney(player);
        int param = 0;
        if (item.template.level == 14) {
            int random = Util.nextInt(1, 100); // Số ngẫu nhiên từ 1 đến 100

            if (random <= 1) {
                param = 15; // 1%
            } else if (random <= 15) {
                param = Util.nextInt(11, 14); // 14% (2-15)
            } else if (random <= 35) {
                param = Util.nextInt(7, 10); // 20% (16-35)
            } else if (random <= 60) {
                param = Util.nextInt(4, 6); // 25% (36-55)
            } else {
                param = Util.nextInt(0, 3); // 40% còn lại
            }
        }

        List<ItemOption> itemoptions = new ArrayList<>();
        if (!item.itemOptions.isEmpty()) {
            for (ItemOption ios : item.itemOptions) {
                if (item.template.level == 14 && InventoryService.gI().optionCanUpgrade(ios.optionTemplate.id) && param > 0) {
                    int id = ios.optionTemplate.id;
                    int param1 = ios.param + (ios.param * param) / 100;
                    itemoptions.add(new ItemOption(id, param1));
                } else if (ios.optionTemplate.id != 164) {
                    itemoptions.add(new ItemOption(ios.optionTemplate.id, ios.param));
                }
            }
        } else {
            itemoptions.add(new ItemOption(73, (short) 0));
        }
        itemoptions.add(new ItemOption(30, (short) 0));
        item.itemOptions.clear();
        item.itemOptions.addAll(itemoptions);
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
    }

    private Item buyMagicPean(Player player, int[][] listDauThan, Item item) {
        for (int i = 0; i < listDauThan.length; i++) {
            if (item.template.id == listDauThan[i][1]) {
                item = ItemService.gI().createNewItem((short) listDauThan[i][0]);
                item.itemOptions.add(new Item.ItemOption(player.magicTree.level - 1 > 1 ? 2 : 48, MagicTree.PEA_PARAM[player.magicTree.level - 1]));
                item.quantity = 30;
                return item;
            }
        }
        return item;
    }

    private boolean shouldOpenBulkBuyMenu(Shop shop, ItemShop is, int[][] listDauThan) {
        if (shop == null || is == null || is.temp == null) {
            return false;
        }
        if (shop.typeShop != NORMAL_SHOP && shop.typeShop != SPEC_SHOP) {
            return false;
        }
        if (!is.temp.isUpToUp || is.cost <= 0) {
            return false;
        }
        int t = is.temp.type;
        int id = is.temp.id;
        if (t != 12 && t != 14 && t != 27 && t != 29 && t != 30 && t != 33 && id != 1787) {
            return false;
        }
        if (id == 194 || id == 400 || id == 517 || id == 518 || id == 1627
                || id == 521 || id == 1523 || id == 1524) {
            return false;
        }
        for (int[] pea : listDauThan) {
            if (id == pea[1]) {
                return false;
            }
        }
        if (is.options != null) {
            for (ItemOption io : is.options) {
                if (io != null && io.optionTemplate != null && io.optionTemplate.id == 93) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getShopCurrencyUnitName(Shop shop, ItemShop is) {
        if (shop.typeShop == NORMAL_SHOP) {
            return switch (is.typeSell) {
                case COST_GOLD -> "vàng";
                case COST_GEM -> "ngọc";
                case COST_RUBY -> "hồng ngọc";
                case COST_COUPON -> "điểm";
                default -> "vàng";
            };
        }
        short effectiveIcon = resolveSpecIcon(is);
        short itSpec = ItemService.gI().getItemIdByIcon(effectiveIcon);
        return switch (itSpec) {
            case 76, 188, 189, 190 -> "vàng";
            case 77 -> "ngọc";
            case 861 -> "hồng ngọc";
            default -> {
                Item specItem = ItemService.gI().createNewItem(itSpec);
                yield (specItem != null && specItem.template != null) ? specItem.template.name : "vật phẩm";
            }
        };
    }

    private int getPackMultiplier(Shop shop, ItemShop is) {
        if (shop != null && "CHICHI_SOUL".equals(shop.tagName)
                && is != null && is.temp != null && is.temp.type == 14) {
            return 100;
        }
        return 1;
    }

    private void openBulkBuyMenu(Player player, Shop shop, ItemShop is) {
        String currencyName = getShopCurrencyUnitName(shop, is);
        int packMult = getPackMultiplier(shop, is);
        String unitLabel = packMult > 1 ? ("gói x" + packMult + " viên") : "1 cái";
        String unitPriceText = Util.numberToMoney(is.cost) + " " + currencyName;
        String npcSay = "|7|" + is.temp.name
                + "\n|2|Đơn giá: " + unitPriceText + " / " + unitLabel
                + "\n|0|x10" + (packMult > 1 ? " (" + Util.numberToMoney(10L * packMult) + " viên)" : "") + ": " + Util.numberToMoney((long) is.cost * 10) + " " + currencyName
                + "\n|0|x100" + (packMult > 1 ? " (" + Util.numberToMoney(100L * packMult) + " viên)" : "") + ": " + Util.numberToMoney((long) is.cost * 100) + " " + currencyName
                + "\n|0|x1.000" + (packMult > 1 ? " (" + Util.numberToMoney(1000L * packMult) + " viên)" : "") + ": " + Util.numberToMoney((long) is.cost * 1000) + " " + currencyName;
        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_BULK_BUY_SHOP, -1, npcSay,
                "Mua x1", "Mua x10", "Mua x100", "Mua x1.000", "Nhập số\nlượng", "Đóng");
    }

    public void openCustomQuantityInput(Player player) {
        Shop shop = player.idMark != null ? player.idMark.getShopOpen() : null;
        ItemShop is = player.pendingBulkItemShop;
        if (shop == null || is == null || is.temp == null) {
            Service.gI().sendThongBao(player, "Phiên mua hàng đã hết hạn, vui lòng chọn lại vật phẩm!");
            return;
        }
        String unitPriceText = Util.numberToMoney(is.cost) + " " + getShopCurrencyUnitName(shop, is);
        Input.gI().createFormBulkBuyShop(player, is.temp.name, unitPriceText);
    }

    public void executeBulkBuy(Player player, int quantity) {
        Shop shop = player.idMark != null ? player.idMark.getShopOpen() : null;
        ItemShop is = player.pendingBulkItemShop;
        if (shop == null || is == null || is.temp == null) {
            Service.gI().sendThongBao(player, "Phiên mua hàng đã hết hạn, vui lòng mở lại cửa hàng!");
            return;
        }
        if (quantity <= 0) {
            Service.gI().sendThongBao(player, "Số lượng mua phải từ 1 trở lên!");
            return;
        }
        if (quantity > 99_999) {
            Service.gI().sendThongBao(player, "Mỗi lần chỉ có thể mua tối đa 99.999 vật phẩm!");
            return;
        }

        Item proto = ItemService.gI().createItemFromItemShop(is);
        if (proto.itemOptions.isEmpty()) {
            proto.itemOptions.add(new ItemOption(73, 0));
        }

        // Kiểm tra vật phẩm cộng dồn theo Option 1 / Option 31 (VD: Mảnh vỡ bông tai)
        int incOptionId = -1;
        int incBaseParam = 0;
        for (ItemOption io : proto.itemOptions) {
            if (io != null && io.optionTemplate != null
                    && (io.optionTemplate.id == 1 || io.optionTemplate.id == 31)) {
                incOptionId = io.optionTemplate.id;
                incBaseParam = Math.max(1, io.param);
                break;
            }
        }

        if (incOptionId != -1) {
            Item existing = InventoryService.gI().findItemBag(player, proto.template.id);
            int currentParam = 0;
            if (existing != null) {
                for (ItemOption io : existing.itemOptions) {
                    if (io != null && io.optionTemplate != null && io.optionTemplate.id == incOptionId) {
                        currentParam = io.param;
                        break;
                    }
                }
            } else if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                Service.gI().sendThongBao(player, "Hành trang đã đầy!");
                return;
            }
            long addedParam = (long) incBaseParam * quantity;
            if (currentParam + addedParam > 30_000) {
                int maxBuy = Math.max(0, (30_000 - currentParam) / incBaseParam);
                Service.gI().sendThongBao(player, maxBuy > 0
                        ? "Ô vật phẩm này chỉ chứa thêm được tối đa x" + maxBuy + "!"
                        : "Ô vật phẩm này đã đạt giới hạn xếp chồng (30.000)!");
                return;
            }
        } else {
            int packMult = getPackMultiplier(shop, is);
            long totalDeliver = (long) quantity * packMult;
            int maxPerSlot = ((proto.template.id >= 1066 && proto.template.id <= 1070)
                    || proto.template.id == 457 || proto.template.id == 610 || proto.template.type == 14
                    || proto.template.id == 2048 || (proto.template.id > 2049 && proto.template.id < 2056)
                    || proto.template.id == 821 || proto.template.id == 2075) ? 100_000_000 : 99_999;
            long availableCapacity = (long) InventoryService.gI().getCountEmptyBag(player) * maxPerSlot;
            for (Item it : player.inventory.itemsBag) {
                if (it != null && it.isNotNullItem() && it.template.id == proto.template.id
                        && (InventoryService.checkListsEqual(it.itemOptions, proto.itemOptions)
                        || proto.template.id == 2074 || proto.isDaNangCap() || proto.isManhTS())
                        && it.quantity < maxPerSlot) {
                    availableCapacity += (maxPerSlot - it.quantity);
                }
            }
            if (availableCapacity < totalDeliver) {
                Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống để chứa x"
                        + Util.numberToMoney(totalDeliver) + " " + is.temp.name + "!");
                return;
            }
        }

        long totalCost = (long) is.cost * quantity;
        String currencyName = getShopCurrencyUnitName(shop, is);
        boolean usedLockedGoldBar = false;

        // Kiểm tra và trừ tiền / vật phẩm trao đổi
        if (shop.typeShop == NORMAL_SHOP) {
            switch (is.typeSell) {
                case COST_GOLD -> {
                    if (player.inventory.gold < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.numberToMoney(totalCost) + " vàng!");
                        return;
                    }
                    player.inventory.subGold(totalCost);
                }
                case COST_GEM -> {
                    if (totalCost > Integer.MAX_VALUE || player.inventory.gem < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.numberToMoney(totalCost) + " ngọc!");
                        return;
                    }
                    player.inventory.gem -= (int) totalCost;
                }
                case COST_RUBY -> {
                    if (totalCost > Integer.MAX_VALUE || player.inventory.ruby < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.numberToMoney(totalCost) + " hồng ngọc!");
                        return;
                    }
                    player.inventory.ruby -= (int) totalCost;
                }
                case COST_COUPON -> {
                    if (totalCost > Integer.MAX_VALUE || player.inventory.coupon < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.numberToMoney(totalCost) + " điểm!");
                        return;
                    }
                    player.inventory.coupon -= (int) totalCost;
                }
                default -> {
                    Service.gI().sendThongBao(player, "Không thể thực hiện!");
                    return;
                }
            }
        } else if (shop.typeShop == SPEC_SHOP) {
            short effectiveIcon = resolveSpecIcon(is);
            short itSpec = ItemService.gI().getItemIdByIcon(effectiveIcon);
            switch (itSpec) {
                case 76, 188, 189, 190 -> {
                    if (player.inventory.gold < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.numberToMoney(totalCost) + " vàng!");
                        return;
                    }
                    player.inventory.subGold(totalCost);
                }
                case 77 -> {
                    if (totalCost > Integer.MAX_VALUE || player.inventory.gem < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.numberToMoney(totalCost) + " ngọc!");
                        return;
                    }
                    player.inventory.gem -= (int) totalCost;
                }
                case 861 -> {
                    if (totalCost > Integer.MAX_VALUE || player.inventory.ruby < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn không đủ " + Util.numberToMoney(totalCost) + " hồng ngọc!");
                        return;
                    }
                    player.inventory.ruby -= (int) totalCost;
                }
                default -> {
                    int totalHave = InventoryService.gI().getTotalQuantityBag(player, itSpec);
                    if (totalCost > Integer.MAX_VALUE || totalHave < totalCost) {
                        Service.gI().sendThongBao(player, "Bạn cần " + Util.numberToMoney(totalCost) + " "
                                + currencyName + " (hiện có " + Util.numberToMoney(totalHave) + ")!");
                        return;
                    }
                    int lockedBefore = InventoryService.gI().getTotalLockedQuantityBag(player, itSpec);
                    InventoryService.gI().subQuantityItemsBagPriorityLocked(player, itSpec, (int) totalCost);
                    if (lockedBefore > 0) {
                        int usedLocked = (int) Math.min(lockedBefore, totalCost);
                        int usedUnlocked = (int) totalCost - usedLocked;
                        if (itSpec == 457 && usedLocked > 0) {
                            usedLockedGoldBar = true;
                        }
                        currencyName = usedUnlocked > 0
                                ? currencyName + " khóa + " + Util.numberToMoney(usedUnlocked) + " " + currencyName + " thường"
                                : currencyName + " khóa";
                        totalCost = usedLocked;
                    }
                }
            }
        } else {
            return;
        }

        // Cộng vật phẩm vào hành trang
        if (incOptionId != -1) {
            for (ItemOption io : proto.itemOptions) {
                if (io != null && io.optionTemplate != null && io.optionTemplate.id == incOptionId) {
                    io.param = incBaseParam * quantity;
                    break;
                }
            }
            if (usedLockedGoldBar && proto.isPhuKien() && !proto.isHaveOption(30)) {
                proto.itemOptions.add(new ItemOption(30, 0));
            }
            proto.quantity = 1;
            InventoryService.gI().addItemBag(player, proto);
        } else {
            int packMult = getPackMultiplier(shop, is);
            long remaining = (long) quantity * packMult;
            int chunkMax = proto.template.type == 14 ? 100_000_000 : 99_999;
            while (remaining > 0) {
                Item chunk = ItemService.gI().createItemFromItemShop(is);
                int addQty = (int) Math.min(remaining, chunkMax);
                chunk.quantity = addQty;
                if (usedLockedGoldBar && chunk.isPhuKien() && !chunk.isHaveOption(30)) {
                    chunk.itemOptions.add(new ItemOption(30, 0));
                }
                InventoryService.gI().addItemBag(player, chunk);
                remaining -= addQty;
            }
        }

        int finalDisplayQty = quantity * getPackMultiplier(shop, is);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Đã mua x" + Util.numberToMoney(finalDisplayQty) + " " + is.temp.name
                + " (-" + Util.numberToMoney(totalCost) + " " + currencyName + ")");
        nro.models.database.PlayerDAO.savePlayerAsync(player);
    }

}
