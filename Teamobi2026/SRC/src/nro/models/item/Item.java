package nro.models.item;

import nro.models.player_system.Template;
import nro.models.player_system.Template.ItemTemplate;
import nro.models.services.ItemService;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Item {

    public ItemTemplate template;
    private static Map<String, String> OPTION_STRING = new HashMap<String, String>();

    public String info;

    public String content;

    public int quantity;

    public int quantityGD = 0;

    public List<ItemOption> itemOptions;

    public long createTime;
    public int id;
    public Object text;
    public long expire;
    public Iterable<ItemOption> options;

    public boolean isNotNullItem() {
        return this.template != null;
    }

    public String getName() {
        return template.name;
    }

    public Item() {
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public Item(short itemId) {
        this.template = ItemService.gI().getTemplate(itemId);
        this.itemOptions = new ArrayList<>();
        this.createTime = System.currentTimeMillis();
    }

    public String getInfo() {
        String strInfo = "";
        for (ItemOption itemOption : itemOptions) {
            strInfo += itemOption.getOptionString();
        }
        return strInfo;
    }

    public String getContent() {
        return "Yêu cầu sức mạnh " + this.template.strRequire + " trở lên";
    }

    public void dispose() {
        this.template = null;
        this.info = null;
        this.content = null;
        if (this.itemOptions != null) {
            for (ItemOption io : this.itemOptions) {
                io.dispose();
            }
            this.itemOptions.clear();
        }
        this.itemOptions = null;
    }

    public static class ItemOption {

        public int param;

        public Template.ItemOptionTemplate optionTemplate;

        public ItemOption() {
        }

        public ItemOption(ItemOption io) {
            this.param = io.param;
            this.optionTemplate = io.optionTemplate;
        }

        public ItemOption(int tempId, int param) {
            this.optionTemplate = ItemService.gI().getItemOptionTemplate(tempId);
            this.param = param;
        }

        public ItemOption(Template.ItemOptionTemplate temp, int param) {
            this.optionTemplate = temp;
            this.param = param;
        }

        public String getOptionString() {
            return Util.replace(this.optionTemplate.name, "#", String.valueOf(this.param));
        }

        public void dispose() {
            this.optionTemplate = null;
        }

        @Override
        public String toString() {
            final String n = "\"";
            return "{"
                    + n + "id" + n + ":" + n + optionTemplate.id + n + ","
                    + n + "param" + n + ":" + n + param + n
                    + "}";
        }

        public String getOptionString(int param) {
            String key = this.optionTemplate.name + "#" + param + "#";
            String value = OPTION_STRING.get(key);
            if (value == null) {
                value = Util.replace(this.optionTemplate.name, "#", String.valueOf(param));
                OPTION_STRING.put(key, value);
            }
            return value;
        }
    }

    public short getId() {
        return template.id;
    }

    public byte getType() {
        return template.type;
    }

    public boolean isSKH() {
        for (ItemOption itemOption : itemOptions) {
            if (itemOption != null && itemOption.optionTemplate != null) {
                int opId = itemOption.optionTemplate.id;
                if (nro.models.services.SkhCollectionService.isAnySkhOptionId(opId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLinhThu() {
        if (this.template == null) {
            return false;
        }
        if (this.template.type == 21 || this.template.type == 72) {
            return true;
        }
        if (this.template.type != 27) {
            return false;
        }
        return switch (this.template.id) {
            case 892, 893, 908, 909, 910, 916, 917, 918, 919, 936,
                 942, 943, 944, 967, 1008, 1039, 1040, 1046, 1107, 1114,
                 1188, 1202, 1203, 1207, 1224, 1225, 1226, 1243, 1244, 1256,
                 1318, 1347, 1414, 1435, 1452, 1458, 1482, 1497, 1550, 1551,
                 1564, 1568, 1573, 1596, 1597, 1611, 1620, 1621, 1622, 1629,
                 1630, 1631, 1633, 1654, 1668, 1682, 1683, 1686, 1727, 1729,
                 1750, 1765, 1766, 1767, 1768, 1769, 1770, 1771, 1789,
                 2003, 2004 -> true;
            default -> false;
        };
    }

    public boolean isPhuKien() {
        if (this.template == null) {
            return false;
        }
        int t = this.template.type;
        return t == 5 || t == 11 || t == 23 || t == 24 || t == 25 || t == 32 || t == 35 || isLinhThu();
    }

    public boolean hasCannotTradeOrExpiry() {
        if (this.itemOptions != null) {
            for (ItemOption io : this.itemOptions) {
                if (io != null && io.optionTemplate != null) {
                    int opId = io.optionTemplate.id;
                    if (opId == 30 || opId == 93) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isVaiTho() {
        if (this.template.id >= 0 && this.template.id <= 65) {
            return true;
        }
        return false;
    }

    public boolean isDTS() {
        if (this.template.id >= 1048 && this.template.id <= 1062) {
            return true;
        }
        return false;
    }

    public boolean isDTL() {
        if (this.template.id >= 555 && this.template.id <= 567) {
            return true;
        }
        return false;
    }

    public boolean isDHD() {
        if (this.template.id >= 650 && this.template.id <= 662) {
            return true;
        }
        return false;
    }

    public boolean isManhTS() {
        if (this.template.id >= 1066 && this.template.id <= 1070) {
            return true;
        }
        return false;
    }

    public boolean isGiayMau() {
        if (this.template.id == 1505) {
            return true;
        }
        return false;
    }

    public boolean isDaNangCap() {
        if (this.template.id >= 1074 && this.template.id <= 1078) {
            return true;
        } else if (this.template.id == -1) {
        }
        return false;
    }

    public int getOptionParam(int id) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                return itemOption.param;
            }
        }
        return 0;
    }

    public void addOptionParam(int id, int param) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                itemOption.param += param;
                return;
            }
        }
        this.itemOptions.add(new ItemOption(id, param));
    }

    public ItemOption getOptionDaPhaLe() {
        return switch (template.id) {
            case 20 ->
                new ItemOption(77, 5);
            case 19 ->
                new ItemOption(103, 5);
            case 18 ->
                new ItemOption(80, 5);
            case 17 ->
                new ItemOption(81, 5);
            case 16 ->
                new ItemOption(50, 3);
            case 15 ->
                new ItemOption(50, 6);
            case 14 ->
                new ItemOption(50, 9);

            case 441 ->
                new ItemOption(95, 5);
            case 442 ->
                new ItemOption(96, 5);
            case 443 ->
                new ItemOption(97, 5);
            case 444 ->
                new ItemOption(98, 5);
            case 445 ->
                new ItemOption(99, 5);
            case 446 ->
                new ItemOption(100, 5);
            case 447 ->
                new ItemOption(101, 5);

            case 1416 ->
                new ItemOption(95, 5);
            case 1417 ->
                new ItemOption(96, 5);
            case 1418 ->
                new ItemOption(97, 5);
            case 1419 ->
                new ItemOption(98, 5);
            case 1420 ->
                new ItemOption(99, 5);
            case 1421 ->
                new ItemOption(100, 5);
            case 1422 ->
                new ItemOption(101, 5);

            case 1426 ->
                new ItemOption(95, 5);
            case 1427 ->
                new ItemOption(96, 5);
            case 1428 ->
                new ItemOption(97, 5);
            case 1429 ->
                new ItemOption(98, 5);
            case 1430 ->
                new ItemOption(99, 5);
            case 1431 ->
                new ItemOption(100, 5);
            case 1432 ->
                new ItemOption(101, 5);
            case 1433 ->
                new ItemOption(153, 5);
            case 1434 ->
                new ItemOption(160, 5);
            default ->
                itemOptions.get(0);
        };
    }

    public boolean canPhaLeHoa() {
        return this.template != null && (this.template.type <= 5 || this.isPhuKien());
    }

    public Item cloneItem() {
        Item item = new Item();
        item.itemOptions = new ArrayList<>();
        item.template = this.template;
        item.info = this.info;
        item.content = this.content;
        item.quantity = this.quantity;
        item.createTime = this.createTime;
        for (Item.ItemOption io : this.itemOptions) {
            item.itemOptions.add(new Item.ItemOption(io));
        }
        return item;
    }

    public String getOptionInfo() {
        StringJoiner optionInfo = new StringJoiner("\n");
        for (ItemOption io : this.itemOptions) {
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107 && io.optionTemplate.id != 218) {
                optionInfo.add(io.getOptionString());
            }
        }
        return optionInfo.toString();
    }

    public boolean isThanLinh() {
        if (this.template.id >= 555 && this.template.id <= 567) {
            return true;
        }
        return false;
    }

    public boolean isThucAn() {
        if (this.template.id >= 663 && this.template.id <= 667) {
            return true;
        }
        return false;
    }

    public String getOptionInfo(Item item) {
        boolean haveOption = false;
        StringJoiner optionInfo = new StringJoiner("\n");
        Item itC = this.cloneItem();
        ItemOption iodpl = item.getOptionDaPhaLe();
        for (ItemOption io : itC.itemOptions) {
            if (!haveOption && io.optionTemplate.id == iodpl.optionTemplate.id) {
                io.param += iodpl.param;
                haveOption = true;
            }
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107) {
                optionInfo.add(io.getOptionString());
            }
        }
        if (!haveOption) {
            optionInfo.add(iodpl.getOptionString());
        }
        itC.dispose();
        return optionInfo.toString();
    }

    public String getOptionInfoCuongHoa(Item item) {
        StringJoiner optionInfo = new StringJoiner("\n");
        Item itC = this.cloneItem();
        ItemOption iodpl = item.getOptionDaPhaLe();
        for (ItemOption io : itC.itemOptions) {
            if (io.optionTemplate.id != 72 && io.optionTemplate.id != 73 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107 && io.optionTemplate.id != 218) {
                optionInfo.add(io.getOptionString());
            }
        }
        optionInfo.add(iodpl.getOptionString());
        itC.dispose();
        return optionInfo.toString();
    }

    public void subOptionParam(int id, int param) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                itemOption.param -= param;
                return;
            }
        }
    }

    public boolean isDaNangCap1() {
        if (this.template.id >= 1074 && this.template.id <= 1078) {
            return true;
        } else if (this.template.id == -1) {
        }
        return false;
    }

    public String typeName() {
        switch (this.template.type) {
            case 0:
                return "Áo";
            case 1:
                return "Quần";
            case 2:
                return "Găng";
            case 3:
                return "Giày";
            case 4:
                return "Rada";
            default:
                return "";
        }
    }

    public String typeHanhTinh() {
        switch (this.template.id) {
            case 1071:
                return "Trái đất";
            case 1084:
                return "Trái đất";
            case 1072:
                return "Namếc";
            case 1085:
                return "Namếc";
            case 1073:
                return "Xayda";
            case 1086:
                return "Xayda";
            default:
                return "";
        }
    }

    public byte typeIdManh() {
        if (!isManhTS()) {
            return -1;
        }
        switch (this.template.id) {
            case 1066:
                return 0;
            case 1067:
                return 1;
            case 1070:
                return 2;
            case 1068:
                return 3;
            case 1069:
                return 4;
            default:
                return -1;
        }
    }

    public String typeNameManh() {
        switch (this.template.id) {
            case 1066:
                return "Áo";
            case 1067:
                return "Quần";
            case 1070:
                return "Găng";
            case 1068:
                return "Giày";
            case 1069:
                return "Nhẫn";
            default:
                return "";
        }
    }

    public String typeDanangcap() {
        switch (this.template.id) {
            case 1074:
                return "cấp 1";
            case 1075:
                return "cấp 2";
            case 1076:
                return "cấp 3";
            case 1077:
                return "cấp 4";
            case 1078:
                return "cấp 5";
            default:
                return "";
        }
    }

    public String typeDaMayman() {
        switch (this.template.id) {
            case 1079:
                return "cấp 1";
            case 1080:
                return "cấp 2";
            case 1081:
                return "cấp 3";
            case 1082:
                return "cấp 4";
            case 1083:
                return "cấp 5";
            default:
                return "";
        }

    }

    public boolean isDaMayMan() {
        return this.template.id >= 1079 && this.template.id <= 1083;
    }

    public boolean isCongThucVip() {
        return (this.template.id >= 1071 && this.template.id <= 1073) || (this.template.id >= 1084 && this.template.id <= 1086);
    }

    public boolean isDoKyGui() {
        return this.template != null && (this.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 86) || this.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 87) || this.template.type == 14 || this.template.type == 15 || this.template.type == 6 || this.template.id >= 14 && this.template.id <= 20);
    }

    public String getInfoItem() {
        String strInfo = "|1|" + template.name + "\n|0|";
        for (ItemOption itemOption : itemOptions) {
            strInfo += itemOption.getOptionString() + "\n";
        }
        strInfo += "|2|" + template.description;
        return strInfo;
    }

    public boolean isHaveOption(int id) {
        for (int i = 0; i < this.itemOptions.size(); i++) {
            ItemOption itemOption = this.itemOptions.get(i);
            if (itemOption != null && itemOption.optionTemplate.id == id) {
                return true;
            }
        }
        return false;
    }

    public boolean isSachTuyetKy() {
        return template.id == 1044 || template.id == 1211 || template.id == 1212;
    }

    public boolean isSachTuyetKy2() {
        return template.id >= 1278 && template.id <= 1280;
    }

    public ItemOption getOptionById(int id) {
        if (this.itemOptions == null) {
            return null;
        }
        for (ItemOption option : this.itemOptions) {
            if (option.optionTemplate != null && option.optionTemplate.id == id) {
                return option;
            }
        }
        return null;
    }

    public boolean hasOption(int optionId, int minParam) {
        for (ItemOption option : this.itemOptions) {
            if (option != null && option.optionTemplate != null
                    && option.optionTemplate.id == optionId && option.param >= minParam) {
                return true;
            }
        }
        return false;
    }

}
