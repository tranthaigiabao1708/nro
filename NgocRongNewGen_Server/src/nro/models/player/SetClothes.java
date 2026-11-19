package nro.models.player;

import nro.models.item.Item;

/**
 *
 * @author By Mr Blue
 * 
 */

public class SetClothes {

    private Player player;

    public SetClothes(Player player) {
        this.player = player;
    }

    public byte songoku;
    public byte thienXinHang;
    public byte kirin;
    public byte kaioken;
    public byte thanVuTruKaio;

    public byte ocTieu;
    public byte pikkoroDaimao;
    public byte picolo;
    public byte lienHoan;
    public byte nail;

    public byte kakarot;
    public byte cadic;
    public byte nappa;
    public byte giamSatThuong;
    public byte cadicM;
    public byte gohan;

    public int skhBonusSongoku;
    public int skhBonusThienXinHang;
    public int skhBonusKirin;
    public int skhBonusThanVuTruKaio;
    public int skhBonusOcTieu;
    public int skhBonusPikkoroDaimao;
    public int skhBonusPicolo;
    public int skhBonusNail;
    public int skhBonusKakarot;
    public int skhBonusCadic;
    public int skhBonusNappa;
    public int skhBonusCadicM;
    public int skhBonusGohan;

    public byte worldcup;
    public byte setDHD;
    public byte setThienSu;
    public byte setVoCuc;

    public boolean godClothes;
    public int ctHaiTac = -1;

    public void setup() {
        setDefault();
        if (this.player == null || this.player.inventory == null || this.player.inventory.itemsBody == null) {
            return;
        }
        setupSKT();

        if (this.player.inventory.itemsBody.size() >= 5) {
            this.godClothes = true;
            for (int i = 0; i < 5; i++) {
                Item item = this.player.inventory.itemsBody.get(i);
                if (item != null && item.isNotNullItem()) {
                    if (item.template.id > 567 || item.template.id < 555) {
                        this.godClothes = false;
                        break;
                    }
                } else {
                    this.godClothes = false;
                }
            }
        }
        if (this.player.inventory.itemsBody.size() > 5) {
            Item ct = this.player.inventory.itemsBody.get(5);
            if (ct != null && ct.isNotNullItem()) {
                switch (ct.template.id) {
                    case 618:
                    case 619:
                    case 620:
                    case 621:
                    case 622:
                    case 623:
                    case 624:
                    case 626:
                    case 627:
                        this.ctHaiTac = ct.template.id;
                        break;

                }
            }
        }

    }

    private void setupSKT() {
        int limit = Math.min(5, this.player.inventory.itemsBody.size());
        for (int i = 0; i < limit; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
                continue;
            }
            if (item.template != null && item.template.id >= 1048 && item.template.id <= 1062) {
                this.setThienSu++;
            }
            if (item.template != null && item.template.id >= 2035 && item.template.id <= 2049) {
                this.setVoCuc++;
            }
            boolean isActSet = false;
            boolean countedDHD = false;
            if (item.isDHD()) {
                this.setDHD++;
                countedDHD = true;
            }
            int itemSkhUpgradePct = 0;
            for (Item.ItemOption io : item.itemOptions) {
                if (io != null && io.optionTemplate != null) {
                    if (io.optionTemplate.id == 259) {
                        itemSkhUpgradePct = Math.max(0, Math.min(20, io.param));
                        break;
                    } else if (io.optionTemplate.id == 258 && itemSkhUpgradePct == 0) {
                        itemSkhUpgradePct = Math.max(0, Math.min(20, io.param * 2));
                    }
                }
            }
            for (Item.ItemOption io : item.itemOptions) {
                if (io == null || io.optionTemplate == null) {
                    continue;
                }
                if (!countedDHD && io.optionTemplate.id == 21 && io.param == 80) {
                    setDHD++;
                    countedDHD = true;
                }
                if (isActSet) {
                    continue;
                }
                switch (io.optionTemplate.id) {
                    case 129:
                    case 141:
                        isActSet = true;
                        songoku++;
                        skhBonusSongoku = Math.min(100, skhBonusSongoku + itemSkhUpgradePct);
                        break;
                    case 127:
                    case 139:
                        isActSet = true;
                        thienXinHang++;
                        skhBonusThienXinHang = Math.min(100, skhBonusThienXinHang + itemSkhUpgradePct);
                        break;
                    case 128:
                    case 140:
                        isActSet = true;
                        kirin++;
                        skhBonusKirin = Math.min(100, skhBonusKirin + itemSkhUpgradePct);
                        break;
                    case 131:
                    case 143:
                        isActSet = true;
                        ocTieu++;
                        skhBonusOcTieu = Math.min(100, skhBonusOcTieu + itemSkhUpgradePct);
                        break;
                    case 132:
                    case 144:
                        isActSet = true;
                        pikkoroDaimao++;
                        skhBonusPikkoroDaimao = Math.min(100, skhBonusPikkoroDaimao + itemSkhUpgradePct);
                        break;
                    case 130:
                    case 142:
                        isActSet = true;
                        picolo++;
                        skhBonusPicolo = Math.min(100, skhBonusPicolo + itemSkhUpgradePct);
                        break;
                    case 135:
                    case 138:
                        isActSet = true;
                        nappa++;
                        skhBonusNappa = Math.min(100, skhBonusNappa + itemSkhUpgradePct);
                        break;
                    case 133:
                    case 136:
                        isActSet = true;
                        kakarot++;
                        skhBonusKakarot = Math.min(100, skhBonusKakarot + itemSkhUpgradePct);
                        break;
                    case 134:
                    case 137:
                        isActSet = true;
                        cadic++;
                        skhBonusCadic = Math.min(100, skhBonusCadic + itemSkhUpgradePct);
                        break;
                    case 253:
                        isActSet = true;
                        kaioken++;
                        break;
                    case 250:
                        isActSet = true;
                        lienHoan++;
                        break;
                    case 252:
                    case 255:
                        isActSet = true;
                        giamSatThuong++;
                        break;
                    case 245:
                    case 246:
                    case 247:
                    case 248:
                        isActSet = true;
                        thanVuTruKaio++;
                        skhBonusThanVuTruKaio = Math.min(100, skhBonusThanVuTruKaio + itemSkhUpgradePct);
                        break;
                    case 237:
                    case 238:
                    case 239:
                    case 240:
                        isActSet = true;
                        nail++;
                        skhBonusNail = Math.min(100, skhBonusNail + itemSkhUpgradePct);
                        break;
                    case 241:
                    case 242:
                    case 243:
                    case 244:
                        isActSet = true;
                        cadicM++;
                        skhBonusCadicM = Math.min(100, skhBonusCadicM + itemSkhUpgradePct);
                        break;
                    case 233:
                    case 234:
                        isActSet = true;
                        gohan++;
                        skhBonusGohan = Math.min(100, skhBonusGohan + itemSkhUpgradePct);
                        break;
                }
            }
        }
    }

    private void setDefault() {
        this.songoku = 0;
        this.thienXinHang = 0;
        this.kirin = 0;
        this.kaioken = 0;
        this.ocTieu = 0;
        this.pikkoroDaimao = 0;
        this.picolo = 0;
        this.lienHoan = 0;
        this.kakarot = 0;
        this.cadic = 0;
        this.nappa = 0;
        this.giamSatThuong = 0;
        this.thanVuTruKaio = 0;
        this.nail = 0;
        this.cadicM = 0;
        this.gohan = 0;
        this.skhBonusSongoku = 0;
        this.skhBonusThienXinHang = 0;
        this.skhBonusKirin = 0;
        this.skhBonusThanVuTruKaio = 0;
        this.skhBonusOcTieu = 0;
        this.skhBonusPikkoroDaimao = 0;
        this.skhBonusPicolo = 0;
        this.skhBonusNail = 0;
        this.skhBonusKakarot = 0;
        this.skhBonusCadic = 0;
        this.skhBonusNappa = 0;
        this.skhBonusCadicM = 0;
        this.skhBonusGohan = 0;
        this.setDHD = 0;
        this.setThienSu = 0;
        this.setVoCuc = 0;
        this.worldcup = 0;
        this.godClothes = false;
        this.ctHaiTac = -1;
    }

    public boolean checkSetGod() {
        if (this.player == null || this.player.isBot || this.player.inventory == null
                || this.player.inventory.itemsBody == null || this.player.inventory.itemsBody.size() < 5) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item != null && item.isNotNullItem() && item.template != null) {
                if (item.template.id < 555 || item.template.id > 567) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean checkSetDes() {
        if (this.player == null || this.player.isBot || this.player.inventory == null
                || this.player.inventory.itemsBody == null || this.player.inventory.itemsBody.size() < 5) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item != null && item.isNotNullItem() && item.template != null) {
                if (item.template.id < 650 || item.template.id > 662) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean checkSetThienSu() {
        if (this.player == null || this.player.inventory == null || this.player.inventory.itemsBody == null
                || this.player.inventory.itemsBody.size() < 5) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item == null || !item.isNotNullItem() || item.template == null
                    || item.template.id < 1048 || item.template.id > 1062) {
                return false;
            }
        }
        return true;
    }

    public void dispose() {
        this.player = null;
    }
}
