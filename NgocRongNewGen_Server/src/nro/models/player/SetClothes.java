package nro.models.player;

import nro.models.item.Item;
import nro.models.services.ItemService;

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

    public byte worldcup;
    public byte setDHD;
    public byte setThienSu;

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
            boolean isActSet = false;
            boolean countedDHD = false;
            if (item.isDHD()) {
                this.setDHD++;
                countedDHD = true;
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
                        break;
                    case 127:
                    case 139:
                        isActSet = true;
                        thienXinHang++;
                        break;
                    case 128:
                    case 140:
                        isActSet = true;
                        kirin++;
                        break;
                    case 131:
                    case 143:
                        isActSet = true;
                        ocTieu++;
                        break;
                    case 132:
                    case 144:
                        isActSet = true;
                        pikkoroDaimao++;
                        break;
                    case 130:
                    case 142:
                        isActSet = true;
                        picolo++;
                        break;
                    case 135:
                    case 138:
                        isActSet = true;
                        nappa++;
                        break;
                    case 133:
                    case 136:
                        isActSet = true;
                        kakarot++;
                        break;
                    case 134:
                    case 137:
                        isActSet = true;
                        cadic++;
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
                        break;
                    case 237:
                    case 238:
                    case 239:
                    case 240:
                        isActSet = true;
                        nail++;
                        break;
                    case 241:
                    case 242:
                    case 243:
                    case 244:
                        isActSet = true;
                        cadicM++;
                        break;
                    case 233:
                    case 234:
                        isActSet = true;
                        gohan++;
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
        this.setDHD = 0;
        this.setThienSu = 0;
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
