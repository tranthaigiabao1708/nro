package nro.models.services;

import java.util.ArrayList;
import java.util.List;
import nro.models.consts.ConstAchievement;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.map.service.NpcService;
import nro.models.network.Message;
import nro.models.player.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Hệ thống Bảng Sưu Tập Set Kích Hoạt tại Nhà & Hướng dẫn tìm SKH (Ngọc Rồng NewGen).
 * - Mỗi hành tinh có 5 Set Kích Hoạt (3 Set cơ bản + Set Gohan + Set Kích Hoạt Mới).
 * - Chỉ khi thu thập đủ cả 5/5 Set (5x5 = 25 món) mới kích hoạt Buff vĩnh viễn:
 *   +5% Sức đánh và +15% TNSM nhận được cho cả Sư phụ & Đệ tử (không cộng lẻ từng Set).
 */
public class SkhCollectionService {

    public static final byte CMD_SKH_COLLECTION = 118;
    public static final int NUM_SETS = 5;
    public static final int NUM_SLOTS = 5;

    private static final int[][] PLANET_SET_OPTIONS = {
        {129, 128, 127, 233, 245}, // Trái Đất: Sôngôku, Kirin, Thên Xin Hăng, Gohan, Kaio
        {130, 131, 132, 233, 237}, // Namek: Picolo, Ốc Tiêu, Pikkoro Daimao, Gohan, Nail
        {133, 134, 135, 233, 241}  // Xayda: Kakarot, Ca Đíc, Nappa, Gohan, Cađic M
    };

    private static final String[][] PLANET_SET_TAB_NAMES = {
        {"Set Sôngôku", "Set Kirin", "Set Thên Xin Hăng", "Set Gohan", "Set Kaio"},
        {"Set Picolo", "Set Ốc Tiêu", "Set Pikkoro", "Set Gohan", "Set Nail"},
        {"Set Kakarot", "Set Ca Đíc", "Set Nappa", "Set Gohan", "Set Cađic M"}
    };

    private static final String[] SLOT_NAMES = {"Áo", "Quần", "Găng", "Giày", "Rada"};

    private static SkhCollectionService instance;

    public static SkhCollectionService gI() {
        if (instance == null) {
            instance = new SkhCollectionService();
        }
        return instance;
    }

    public static int[] getPlanetSetOptions(int gender) {
        int g = (gender >= 0 && gender <= 2) ? gender : 0;
        return PLANET_SET_OPTIONS[g];
    }

    public static String[] getPlanetSetTabNames(int gender) {
        int g = (gender >= 0 && gender <= 2) ? gender : 0;
        return PLANET_SET_TAB_NAMES[g];
    }

    public static String getSetFullName(int skhId) {
        return switch (skhId) {
            case 127 -> "Set Thên Xin Hăng";
            case 128 -> "Set Kirin";
            case 129 -> "Set Sôngôku";
            case 130 -> "Set Picolo";
            case 131 -> "Set Ốc Tiêu";
            case 132 -> "Set Pikkoro Daimao";
            case 133 -> "Set Kakarot";
            case 134 -> "Set Ca Đíc";
            case 135 -> "Set Nappa";
            case 233 -> "Set Gohan";
            case 237 -> "Set Nail (Mới)";
            case 241 -> "Set Cađic M (Mới)";
            case 245 -> "Set Thần Vũ Trụ Kaio (Mới)";
            default -> "Set Kích Hoạt";
        };
    }

    public static boolean isMainSkhOptionId(int optId) {
        return (optId >= 127 && optId <= 135)
                || optId == 233
                || optId == 237
                || optId == 241
                || optId == 245;
    }

    public static boolean isAnySkhOptionId(int optId) {
        return (optId >= 127 && optId <= 144)
                || optId == 233 || optId == 234
                || (optId >= 237 && optId <= 248);
    }

    public static int getMainSkhOptionId(Item item) {
        if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
            return -1;
        }
        for (ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null && isMainSkhOptionId(io.optionTemplate.id)) {
                return io.optionTemplate.id;
            }
        }
        return -1;
    }

    public static void ensureInitialized(Player player) {
        if (player == null) {
            return;
        }
        if (player.skhCollection == null) {
            player.skhCollection = new Item[NUM_SETS][NUM_SLOTS];
        }
        for (int t = 0; t < NUM_SETS; t++) {
            for (int s = 0; s < NUM_SLOTS; s++) {
                if (player.skhCollection[t][s] == null) {
                    player.skhCollection[t][s] = ItemService.gI().createItemNull();
                }
            }
        }
    }

    public static int getMountedCountInSet(Player player, int tabIndex) {
        if (player == null || player.skhCollection == null || tabIndex < 0 || tabIndex >= NUM_SETS) {
            return 0;
        }
        int count = 0;
        for (int s = 0; s < NUM_SLOTS; s++) {
            Item it = player.skhCollection[tabIndex][s];
            if (it != null && it.isNotNullItem()) {
                count++;
            }
        }
        return count;
    }

    public static int getCompletedSetCount(Player player) {
        if (player == null || player.skhCollection == null) {
            return 0;
        }
        int completed = 0;
        for (int t = 0; t < NUM_SETS; t++) {
            if (getMountedCountInSet(player, t) == NUM_SLOTS) {
                completed++;
            }
        }
        return completed;
    }

    public static void syncAchievements(Player player) {
        if (player == null || !player.isPl() || player.achievement == null) {
            return;
        }
        int completed = getCompletedSetCount(player);
        int[] achIds = {
            ConstAchievement.SUU_TAP_SKH_1,
            ConstAchievement.SUU_TAP_SKH_2,
            ConstAchievement.SUU_TAP_SKH_3,
            ConstAchievement.SUU_TAP_SKH_4,
            ConstAchievement.SUU_TAP_SKH_5
        };
        for (int achId : achIds) {
            if (achId < player.achievement.getAchievementList().size()) {
                player.achievement.doneNotAdd(achId, completed);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static String toJson(Player player) {
        ensureInitialized(player);
        JSONArray allTabs = new JSONArray();
        for (int t = 0; t < NUM_SETS; t++) {
            JSONArray tabArr = new JSONArray();
            for (int s = 0; s < NUM_SLOTS; s++) {
                Item item = player.skhCollection[t][s];
                JSONArray dataItem = new JSONArray();
                JSONArray opt = new JSONArray();
                if (item != null && item.isNotNullItem()) {
                    dataItem.add(item.template.id);
                    dataItem.add(item.quantity);
                    JSONArray options = new JSONArray();
                    if (item.itemOptions != null) {
                        for (ItemOption io : item.itemOptions) {
                            if (io != null && io.optionTemplate != null) {
                                opt.add(io.optionTemplate.id);
                                opt.add(io.param);
                                options.add(opt.toJSONString());
                                opt.clear();
                            }
                        }
                    }
                    dataItem.add(options.toJSONString());
                    dataItem.add(item.createTime);
                } else {
                    dataItem.add(-1);
                    dataItem.add(0);
                    dataItem.add(opt.toJSONString());
                    dataItem.add(0L);
                }
                tabArr.add(dataItem.toJSONString());
            }
            allTabs.add(tabArr.toJSONString());
        }
        allTabs.add(player.claimedSkhAura ? 1 : 0);
        return allTabs.toJSONString();
    }

    public static void loadFromJson(Player player, String rawJson) {
        ensureInitialized(player);
        player.claimedSkhAura = false;
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return;
        }
        try {
            JSONArray allTabs = (JSONArray) JSONValue.parse(rawJson);
            if (allTabs == null) {
                return;
            }
            if (allTabs.size() > NUM_SETS && allTabs.get(NUM_SETS) != null) {
                String flagStr = String.valueOf(allTabs.get(NUM_SETS)).trim();
                player.claimedSkhAura = "1".equals(flagStr) || "true".equalsIgnoreCase(flagStr);
            }
            for (int t = 0; t < allTabs.size() && t < NUM_SETS; t++) {
                JSONArray tabArr = (JSONArray) JSONValue.parse(String.valueOf(allTabs.get(t)));
                if (tabArr == null) {
                    continue;
                }
                for (int s = 0; s < tabArr.size() && s < NUM_SLOTS; s++) {
                    JSONArray dataItem = (JSONArray) JSONValue.parse(String.valueOf(tabArr.get(s)));
                    if (dataItem == null || dataItem.isEmpty()) {
                        continue;
                    }
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        int qty = Integer.parseInt(String.valueOf(dataItem.get(1)));
                        Item item = ItemService.gI().createNewItem(tempId, Math.max(1, qty));
                        JSONArray options = (JSONArray) JSONValue.parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                        if (options != null) {
                            for (int j = 0; j < options.size(); j++) {
                                JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                                if (opt != null && opt.size() >= 2) {
                                    item.itemOptions.add(new ItemOption(
                                            Integer.parseInt(String.valueOf(opt.get(0))),
                                            Integer.parseInt(String.valueOf(opt.get(1)))));
                                }
                            }
                        }
                        if (dataItem.size() > 3) {
                            item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                        }
                        player.skhCollection[t][s] = item;
                    } else {
                        player.skhCollection[t][s] = ItemService.gI().createItemNull();
                    }
                }
            }
        } catch (Exception e) {
            ensureInitialized(player);
        }
    }

    public static boolean isInPlayerHouse(Player player) {
        if (player == null || player.zone == null || player.zone.map == null) {
            return false;
        }
        int mapId = player.zone.map.mapId;
        return mapId == 21 || mapId == 22 || mapId == 23;
    }

    public void openCollectionUI(Player player, int activeTab) {
        if (player == null) {
            return;
        }
        ensureInitialized(player);
        syncAchievements(player);
        if (activeTab < 0 || activeTab >= NUM_SETS) {
            activeTab = 0;
        }

        Message msg = null;
        try {
            msg = new Message(CMD_SKH_COLLECTION);
            msg.writer().writeByte(0); // sub 0: open / sync UI
            msg.writer().writeByte(activeTab);
            msg.writer().writeByte(getCompletedSetCount(player));
            msg.writer().writeBoolean(isInPlayerHouse(player));

            int[] setOpts = getPlanetSetOptions(player.gender);
            String[] tabNames = getPlanetSetTabNames(player.gender);

            for (int t = 0; t < NUM_SETS; t++) {
                msg.writer().writeUTF(tabNames[t]);
                msg.writer().writeShort(setOpts[t]);
                msg.writer().writeByte(getMountedCountInSet(player, t));
                for (int s = 0; s < NUM_SLOTS; s++) {
                    Item it = player.skhCollection[t][s];
                    if (it != null && it.isNotNullItem()) {
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(it.template.iconID);
                        msg.writer().writeUTF(it.template.name);
                        List<ItemOption> opts = it.itemOptions != null ? it.itemOptions : new ArrayList<>();
                        int countOpt = Math.min(opts.size(), 30);
                        msg.writer().writeByte(countOpt);
                        for (int k = 0; k < countOpt; k++) {
                            ItemOption io = opts.get(k);
                            msg.writer().writeShort(io.optionTemplate.id);
                            msg.writer().writeInt(io.param);
                        }
                    } else {
                        msg.writer().writeShort(-1);
                    }
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

    public void handleClientMessage(Player player, Message msg) {
        if (player == null || msg == null) {
            return;
        }
        try {
            byte action = msg.reader().readByte();
            switch (action) {
                case 0 -> {
                    int tab = msg.reader().available() > 0 ? msg.reader().readByte() : 0;
                    openCollectionUI(player, tab);
                }
                case 1 -> {
                    int tab = msg.reader().readByte();
                    mountItemsFromBag(player, tab);
                }
                case 2 -> {
                    int tab = msg.reader().readByte();
                    int slot = msg.reader().readByte();
                    unmountItemToBag(player, tab, slot);
                }
                case 3 -> {
                    showGuide(player);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mountItemsFromBag(Player player, int tabIndex) {
        ensureInitialized(player);
        if (tabIndex < 0 || tabIndex >= NUM_SETS) {
            return;
        }
        if (!isInPlayerHouse(player)) {
            Service.gI().sendThongBao(player, "Hãy về Nhà để gắn trang bị lên Bảng Sưu Tập!");
            return;
        }

        int[] setOpts = getPlanetSetOptions(player.gender);
        int targetSetOpt = setOpts[tabIndex];
        String setName = getPlanetSetTabNames(player.gender)[tabIndex];
        int prevCompleted = getCompletedSetCount(player);
        int mountedNow = 0;

        for (int slot = 0; slot < NUM_SLOTS; slot++) {
            if (player.skhCollection[tabIndex][slot] != null && player.skhCollection[tabIndex][slot].isNotNullItem()) {
                continue;
            }
            for (Item bagItem : player.inventory.itemsBag) {
                if (bagItem == null || !bagItem.isNotNullItem()) {
                    continue;
                }
                if (bagItem.template.type != slot) {
                    continue;
                }
                if (slot != 4 && bagItem.template.gender != player.gender) {
                    continue;
                }
                if (getMainSkhOptionId(bagItem) == targetSetOpt) {
                    Item mountedCopy = ItemService.gI().copyItem(bagItem);
                    mountedCopy.quantity = 1;
                    player.skhCollection[tabIndex][slot] = mountedCopy;
                    InventoryService.gI().subQuantityItemsBag(player, bagItem, 1);
                    mountedNow++;
                    break;
                }
            }
        }

        if (mountedNow == 0) {
            if (getMountedCountInSet(player, tabIndex) == NUM_SLOTS) {
                Service.gI().sendThongBao(player, setName + " đã treo đủ 5/5 món rồi!");
            } else {
                Service.gI().sendThongBao(player, "Không tìm thấy món " + setName + " phù hợp nào trong Hành trang!");
            }
            openCollectionUI(player, tabIndex);
            return;
        }

        InventoryService.gI().sendItemBags(player);
        syncAchievements(player);
        Service.gI().point(player);
        if (player.pet != null) {
            player.pet.nPoint.calPoint();
        }
        openCollectionUI(player, tabIndex);

        int newCompleted = getCompletedSetCount(player);
        if (newCompleted > prevCompleted) {
            if (newCompleted >= NUM_SETS) {
                Service.gI().sendThongBao(player, "Chúc mừng thu thập đủ 5/5 Set Kích Hoạt (25/25 món)! Kích hoạt: +5% Sức đánh & +15% TNSM cho cả Sư phụ & Đệ tử!");
                if (!player.claimedSkhAura) {
                    claimFiveSetAuraReward(player, true);
                }
            } else {
                Service.gI().sendThongBao(player, "Hoàn thành " + setName + " (5/5)! Tiến độ: " + newCompleted + "/5 Bộ (Thu thập đủ 5/5 Bộ để nhận +5% SĐ & +15% TNSM)!");
            }
        } else {
            Service.gI().sendThongBao(player, "Đã gắn " + mountedNow + " món vào " + setName + " (" + getMountedCountInSet(player, tabIndex) + "/5)!");
        }
        nro.models.database.PlayerDAO.savePlayerAsync(player);
    }

    public void claimFiveSetAuraReward(Player player, boolean autoTrigger) {
        if (player == null || !player.isPl()) {
            return;
        }
        ensureInitialized(player);
        if (player.claimedSkhAura) {
            if (!autoTrigger) {
                Service.gI().sendThongBao(player, "Bạn đã nhận phần thưởng Hào quang Vô Cực trước đó rồi (chỉ nhận 1 lần)!");
            }
            return;
        }
        int completed = getCompletedSetCount(player);
        if (completed < NUM_SETS) {
            if (!autoTrigger) {
                Service.gI().sendThongBao(player, "Cần treo đủ 5/5 Set Kích Hoạt trong Bảng Sưu Tập (hiện tại: " + completed + "/5 Set)!");
            }
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Đủ 5/5 Set SKH! Hãy dọn trống 1 ô Hành trang rồi gặp Ông Gôhan/Moori/Paragus tại Nhà để nhận Hào quang Vô Cực!");
            return;
        }
        Item aura11 = ItemService.gI().createNewItem((short) 2007);
        aura11.itemOptions.add(new ItemOption(249, 0));
        aura11.itemOptions.add(new ItemOption(30, 0));
        if (InventoryService.gI().addItemBag(player, aura11)) {
            player.claimedSkhAura = true;
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Chúc mừng hoàn thành đủ 5 Set SKH! Bạn nhận được [Hào quang Vô Cực] (tăng tỉ lệ rơi Đồ Thần tại Cold)!");
            nro.models.database.PlayerDAO.savePlayerAsync(player);
        }
    }

    private void unmountItemToBag(Player player, int tabIndex, int slotIndex) {
        ensureInitialized(player);
        if (tabIndex < 0 || tabIndex >= NUM_SETS || slotIndex < 0 || slotIndex >= NUM_SLOTS) {
            return;
        }
        if (!isInPlayerHouse(player)) {
            Service.gI().sendThongBao(player, "Hãy về Nhà để tháo trang bị khỏi Bảng Sưu Tập!");
            return;
        }
        Item mounted = player.skhCollection[tabIndex][slotIndex];
        if (mounted == null || !mounted.isNotNullItem()) {
            Service.gI().sendThongBao(player, "Ô [" + SLOT_NAMES[slotIndex] + "] hiện đang trống!");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Cần ít nhất 1 ô trống trong Hành trang để tháo đồ!");
            return;
        }

        Item returned = ItemService.gI().copyItem(mounted);
        returned.quantity = 1;
        player.skhCollection[tabIndex][slotIndex] = ItemService.gI().createItemNull();

        InventoryService.gI().addItemBag(player, returned);
        InventoryService.gI().sendItemBags(player);
        syncAchievements(player);
        Service.gI().point(player);
        if (player.pet != null) {
            player.pet.nPoint.calPoint();
        }
        openCollectionUI(player, tabIndex);
        Service.gI().sendThongBao(player, "Đã tháo [" + returned.template.name + "] về Hành trang!");
        nro.models.database.PlayerDAO.savePlayerAsync(player);
    }

    public void showGuide(Player player) {
        String guide = "CẨM NANG SÉT KÍCH HOẠT - NGỌC RỒNG NEWGEN\n\n"
                + "1. LỘ TRÌNH CÀY ĐỒ SÉT KÍCH HOẠT (SKH) & CHỈ SỐ MAY MẮN:\n"
                + "- Đánh quái tại 3 map đầu mỗi hành tinh hoặc dùng Vé Khu Up SKH (quái x2 & cộng thẳng tỉ lệ nền SKH).\n"
                + "- Cỏ 4 Lá (mua tại Santa): Cộng thẳng tỉ lệ nền SKH & +20% May mắn rơi mọi vật phẩm trong 30 phút.\n"
                + "- Chỉ số May mắn chung (tăng tỉ lệ rơi cho TẤT CẢ vật phẩm: Đồ SKH, Thỏi vàng khóa, Ngọc Rồng, Đá nâng cấp, Sao pha lê, Đồ Thần...):\n"
                + "  + Ước Ngọc Rồng 1 Sao: +20% May mắn trong 60 phút.\n"
                + "  + Cải trang Mighty Mask (Shop Thỏi Vàng): +20% May mắn.\n"
                + "  + Mặc đủ 5 món Set Gohan: +150% May mắn & +30% Vàng quái.\n"
                + "  + Mẹo khởi đầu: Dùng Vé Khu SKH + Cỏ 4 Lá + Ước NR 1 Sao để farm phôi -> đổi Capsule Thường lấy đủ 5 món Set Gohan trước để tăng tốc cày toàn bộ tài nguyên!\n"
                + "- Phân bố Set khi rơi: 80% ra 3 Set cơ bản của hành tinh, 20% ra Set hiếm (Set Gohan & Set Mới: Kaio / Nail / Cađic M).\n\n"
                + "2. PHÂN RÃ & TÁI TẠO CAPSULE (BÀ HẠT MÍT ĐẢO KAME):\n"
                + "- Phân rã 1 hoặc nhiều đồ SKH thường dư thừa cùng lúc -> mỗi món nhận 1 Khoáng tái chế (200 Tr vàng/món).\n"
                + "- Capsule Kích Hoạt Thường (5 Khoáng + 500 Tr vàng): Mở tự chọn Set cơ bản (gồm cả Set Gohan) & món trang bị.\n"
                + "- Capsule Kích Hoạt VIP (10 Khoáng + 1 Tỷ vàng): Mở tự chọn Set Kích Hoạt Mới & món trang bị.\n\n"
                + "3. NÂNG CẤP ĐỒ THẦN LINH SKH & HỦY DIỆT SKH (BÀ HẠT MÍT ĐẢO KAME):\n"
                + "- Lên Đồ Thần Linh SKH (Tỉ lệ 40%): 1 Đồ SKH thường + 1 Đồ Thần Linh CÙNG LOẠI (Áo-Áo, Quần-Quần, Găng-Găng, Giày-Giày, Rada-Nhẫn) + 2 Khoáng tái chế + 500 Tr vàng.\n"
                + "- Lên Đồ Hủy Diệt SKH (Tỉ lệ 30%): 1 Đồ Thần Linh SKH + 1 Đồ Hủy Diệt CÙNG LOẠI + 5 Khoáng tái chế + 2 Tỷ vàng.\n"
                + "- Giữ nguyên đúng loại Set của món phôi SKH đưa vào.\n"
                + "- CẢNH BÁO: Không được bỏ thêm bất kỳ vật phẩm nào khác. Nếu nâng cấp THẤT BẠI sẽ mất luôn cả 2 trang bị!\n\n"
                + "4. BẢNG SƯU TẬP SET KÍCH HOẠT TẠI NHÀ:\n"
                + "- Gặp Ông Gôhan / Ông Moori / Ông Paragus trong Nhà (hoặc gõ chat 'skh') để mở Bảng Sưu Tập 5 Set.\n"
                + "- Chỉ khi thu thập đủ trọn bộ 5/5 Set (5x5 = 25 món) mới kích hoạt Buff vĩnh viễn: +5% Sức đánh và +15% TNSM cho cả Sư phụ & Đệ tử (không cộng lẻ từng Set)!\n"
                + "- Đặc biệt: Hoàn thành đủ 5/5 Set nhận thưởng 1 lần trang bị [Hào quang Vô Cực] (Aura #11 - tăng nhẹ tỉ lệ rơi Đồ Thần Linh khi farm tại Cold)!";
        NpcService.gI().createTutorial(player, -1, guide);
    }
}

