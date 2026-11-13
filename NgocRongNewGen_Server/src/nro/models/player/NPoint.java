package nro.models.player;

import nro.models.radar.Card;
import nro.models.radar.OptionCard;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstRatio;
import nro.models.intrinsic.Intrinsic;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.skill.Skill;
import nro.models.server.Manager;
import nro.models.services.EffectSkillService;
import nro.models.services.ItemService;
import nro.models.map.service.MapService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.SkhCollectionService;
import nro.models.services.TaskService;
import nro.models.utils.Logger;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;
import nro.models.mob.Mob;
import nro.models.player_badges.BagesTemplate;

/**
 *
 * @author By Mr Blue
 *
 */
public class NPoint {

    public static final byte MAX_LIMIT = 13;

    private Player player;
    long timeXinbatoBuff;

    public void setPlayer(Player player) {
        this.player = player;
    }

    public NPoint(Player player) {
        this.player = player;
        this.tlHp = new ArrayList<>();
        this.tlMp = new ArrayList<>();
        this.tlDef = new ArrayList<>();
        this.tlDame = new ArrayList<>();
        this.tlDameAttMob = new ArrayList<>();
        this.tlTNSM = new ArrayList<>();
        this.tlDameCrit = new ArrayList<>();
    }

    public boolean isCrit;
    public boolean isCrit100;
    public boolean isCritTele;

    private Intrinsic intrinsic;
    private int percentDameIntrinsic;
    public int dameAfter;

    /*-----------------------Chỉ số cơ bản------------------------------------*/
    public byte numAttack;
    public short stamina, maxStamina;

    public byte limitPower;
    public long power;
    public long tiemNang;

    public long hp, hpMax, hpg;
    public long bossRealHp, bossRealHpMax;
    public long mp, mpMax, mpg;
    public long dame, dameg;
    public int def, defg;
    public int crit, critg, critdragon;
    public int luckDragon;
    public byte speed = 8;

    public boolean teleport;

    public boolean khangTDHS;

    /**
     * Chỉ số cộng thêm
     */
    public long hpAdd, mpAdd, dameAdd, hpHoiAdd, mpHoiAdd, defAdd;
    public int critAdd;

    /**
     * //+#% sức đánh chí mạng
     */
    public List<Integer> tlDameCrit;
    public int tlSDCM;

    /**
     * Tỉ lệ hp, mp cộng thêm
     */
    public List<Integer> tlHp, tlMp;

    /**
     * Tỉ lệ giáp cộng thêm
     */
    public List<Integer> tlDef;

    /**
     * Tỉ lệ sức đánh/ sức đánh khi đánh quái
     */
    public List<Integer> tlDame, tlDameAttMob;

    /**
     * Lượng hp, mp hồi mỗi 30s, mp hồi cho người khác
     */
    public long hpHoi, mpHoi;
    public int mpHoiCute;

    /**
     * Tỉ lệ hp, mp hồi cộng thêm
     */
    public int tlHpHoi, tlMpHoi;

    /**
     * Tỉ lệ hp, mp hồi bản thân và đồng đội cộng thêm
     */
    public int tlHpHoiBanThanVaDongDoi, tlMpHoiBanThanVaDongDoi;

    /**
     * Tỉ lệ hút hp, mp khi đánh, hp khi đánh quái
     */
    public int tlHutHp, tlHutMp, tlHutHpMob;

    /**
     * Tỉ lệ hút hp, mp xung quanh mỗi 5s
     */
    public int tlHutHpMpXQ;

    /**
     * Tỉ lệ phản sát thương
     */
    public int tlPST;

    /**
     * Tỉ lệ tiềm năng sức mạnh
     */
    public List<Integer> tlTNSM;

    /**
     * Tỉ lệ vàng cộng thêm
     */
    public int tlGold;

    /**
     * Tỉ lệ né đòn
     */
    public int tlNeDon;
    public int tlNeDonXinbato;

    public int tlBom;

    public int tlGiap;

    public int tlxgcc;

    public int tlxgc;

    public int tlchinhxac;

    public int tlTNSMPet;
    public int xChuong;

    public short setltdb;
    public short setTinhAn;
    public short setNhatAn;
    public short setNguyetAn;

    /**
     * Tỉ lệ sức đánh đẹp cộng thêm cho bản thân và người xung quanh
     */
    public int tlSexyDame;

    /**
     * Tỉ lệ giảm sức đánh
     */
    public int tlSubSD;

    public int voHieuChuong;

    /*------------------------Effect skin-------------------------------------*/
    public Item trainArmor;
    public boolean wearingTrainArmor;

    public boolean wearingVoHinh;
    public boolean isKhongLanh;
    public boolean islinhthuydanhbac;
    public boolean isTinhAn;
    public boolean isNhatAn;
    public boolean isNguyetAn;
    public boolean isTanHinh;
    public boolean isHoaDa;
    public boolean isLamCham;
    public boolean isDoSPL;
    public boolean isThoBulma;

    public int tlHpGiamODo;

    public boolean isGogeta;

    public int tlSpeed;

    public int levelBT;

    public int tlNeDonBuffXinbato = 0;

    public int tlKhangKhongChe;
    public int tlJackpotThoiVangBoss;
    public int tlRoiDoThanBoss;
    public int tlDameTuyetKy;
    public int tlRangeTuyetKy;
    public boolean hasMountFlyRegen;
    public long lastTimeMountRegen;
    public long frostPoisonUntil;
    public long hakaiNoPeaUntil;

    /*-------------------------------------------------------------------------*/
    /**
     * Tính toán mọi chỉ số sau khi có thay đổi
     */
    public void calPoint() {
        if (this.player.pet != null) {
            this.player.pet.nPoint.setPointWhenWearClothes();
        }
        this.setPointWhenWearClothes();
    }

    public int getRealTlNeDon() {
        int total = this.tlNeDon;
        if (tlNeDonBuffXinbato > 0) {
            total += tlNeDonBuffXinbato;
        }
        return Math.min(total, 90);
    }

    private void setPointWhenWearClothes() {
        resetPoint();
        if (this.player.setClothes != null) {
            this.player.setClothes.setup();
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[2] > System.currentTimeMillis()) {
            tlHutHp += RewardBlackBall.R3S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[3] > System.currentTimeMillis()) {
            tlPST += RewardBlackBall.R4S_2;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[4] > System.currentTimeMillis()) {
            tlDameCrit.add(RewardBlackBall.R5S_1);
            tlSDCM += RewardBlackBall.R5S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[6] > System.currentTimeMillis()) {
            tlNeDon += RewardBlackBall.R7S_1;
        }
        // Lấy tất cả option danh hiệu
        List<Item.ItemOption> options = BagesTemplate.sendListItemOption(player);

        for (Item.ItemOption opt : options) {
            if (opt.optionTemplate.id == 108) {
                tlNeDon = (int) Math.min((long) Integer.MAX_VALUE, (long) tlNeDon + ((long) tlNeDon * opt.param / 100L));
            }
        }

        Card card = player.Cards.stream().filter(r -> r != null && r.Used == 1).findFirst().orElse(null);
        if (card != null) {
            for (OptionCard io : card.Options) {
                if (io.active == card.Level || (card.Level == -1 && io.active == 0)) {
                    switch (io.id) {
                        case 0: //Tấn công +#
                            this.dameAdd += io.param;
                            break;
                        case 2: //HP, KI+#000
                            this.hpAdd += io.param * 1000L;
                            this.mpAdd += io.param * 1000L;
                            break;
                        case 3:// vô hiệu chưởng
                            this.voHieuChuong += io.param;
                            break;
                        case 5: //+#% sức đánh chí mạng
                            this.tlDameCrit.add(io.param);
                            this.tlSDCM += io.param;
                            break;
                        case 6: //HP+#
                            this.hpAdd += io.param;
                            break;
                        case 7: //KI+#
                            this.mpAdd += io.param;
                            break;
                        case 8: //Hút #% HP, KI xung quanh mỗi 5 giây
                            this.tlHutHpMpXQ += io.param;
                            break;
                        case 14: //Chí mạng+#%
                            this.critAdd += io.param;
                            break;
                        case 16: // Speed
                        case 114:
                        case 148:
                            this.tlSpeed += io.param;
                            break;
                        case 18: //Chinh xac
                            this.tlchinhxac += io.param;
                            break;
                        case 19: //Tấn công+#% khi đánh quái
                            this.tlDameAttMob.add(io.param);
                            break;
                        case 22: //HP+#K
                            this.hpAdd += io.param * 1000L;
                            break;
                        case 23: //MP+#K
                            this.mpAdd += io.param * 1000L;
                            break;
                        case 27: //+# HP/30s
                            this.hpHoiAdd += io.param;
                            break;
                        case 28: //+# KI/30s
                            this.mpHoiAdd += io.param;
                            break;
                        case 33: //dịch chuyển tức thời
                            this.teleport = true;
                            break;
                        case 34:
                            this.setTinhAn += 1;
                            break;
                        case 35:
                            this.setNguyetAn += 1;
                            break;
                        case 36:
                            this.setNhatAn += 1;
                            break;
                        case 47: //Giáp+#
                            this.defAdd += io.param;
                            break;
                        case 48: //HP/KI+#
                            this.hpAdd += io.param;
                            this.mpAdd += io.param;
                            break;
                        case 49: //Tấn công+#%
                        case 50: //Sức đánh+#%
                            this.tlDame.add(io.param);
                            break;
                        case 77: //HP+#%
                            this.tlHp.add(io.param);
                            break;
                        case 80: //HP+#%/30s
                            this.tlHpHoi += io.param;
                            break;
                        case 81: //MP+#%/30s
                            this.tlMpHoi += io.param;
                            break;
                        case 88: //Cộng #% exp khi đánh quái
                            this.tlTNSM.add(io.param);
                            break;
                        case 94: //Giáp #%
                            this.tlGiap += io.param;
                            break;
                        case 95: //Hút #% HP theo sát thương
                            this.tlHutHp += io.param;
                            break;
                        case 96: //Hút #% KI theo sát thương
                            this.tlHutMp += io.param;
                            break;
                        case 97: //Phản #% sát thương
                            this.tlPST += io.param;
                            break;
                        case 98: //Xuyen giap chuong
                            this.tlxgc += io.param;
                            break;
                        case 99: //Xuyen giap can chien
                            this.tlxgcc += io.param;
                            break;
                        case 100: //+#% vàng từ quái
                            this.tlGold += io.param;
                            break;
                        case 101: //+#% TN,SM
                            this.tlTNSM.add(io.param);
                            break;
                        case 103: //KI +#%
                            this.tlMp.add(io.param);
                            break;
                        case 104: //Biến #% tấn công quái thành HP
                            this.tlHutHpMob += io.param;
                            break;
                        case 105: //Vô hình khi không đánh quái và boss
                            this.wearingVoHinh = true;
                            break;
                        case 106: //Không ảnh hưởng bởi cái lạnh
                            this.isKhongLanh = true;
                            break;
                        case 111:
                        case 108: //#% Né đòn
                            this.tlNeDon += io.param;
                            break;
                        case 109: //Hôi, giảm #% HP
                            this.tlHpGiamODo += io.param;
                            break;
                        case 116: //Kháng thái dương hạ san
                            this.khangTDHS = true;
                            break;
                        case 226:
                        case 117: //Đẹp +#% SĐ cho mình và người xung quanh
                            if (io.param > this.tlSexyDame) {
                                this.tlSexyDame = io.param;
                            }
                            break;
                        case 147: //+#% sức đánh
                            this.tlDame.add(io.param);
                            break;
                        case 156: //Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                            this.tlSubSD += 50;
                            this.tlTNSM.add(io.param);
                            this.tlGold += io.param;
                            break;
                        case 162: //Cute hồi #% KI/s bản thân và xung quanh
                            this.mpHoiCute += io.param;
                            break;
                        case 173: //Phục hồi #% HP và KI cho đồng đội
                            this.tlHpHoiBanThanVaDongDoi += io.param;
                            this.tlMpHoiBanThanVaDongDoi += io.param;
                            break;
                        case 211:
                            this.setltdb += 1;
                            break;
                        case 153: //% phát nổ sau khi chết
                            this.tlBom += io.param;
                            break;
                    }
                }
            }
        }

        // Bông tai cấp 2
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
            this.player.inventory.itemsBag.stream()
                    .filter(it -> it.isNotNullItem() && it.template.id == 921)
                    .findFirst()
                    .ifPresent(btc2 -> {
                        for (ItemOption io : btc2.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) {
                                this.levelBT = io.param;
                            }
                        }
                    });
        }
        if (this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
            this.player.inventory.itemsBag.stream()
                    .filter(it -> it.isNotNullItem() && it.template.id == 1819)
                    .findFirst()
                    .ifPresent(btc3 -> {
                        for (ItemOption io : btc3.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) {
                                this.levelBT = io.param;
                            }
                        }
                    });
        }

        boolean isFused = (this.player.fusion != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION)
                || (this.player.isPet && ((Pet) this.player).master != null
                && ((Pet) this.player).master.fusion != null
                && ((Pet) this.player).master.fusion.typeFusion != ConstPlayer.NON_FUSION);
        this.player.setClothes.worldcup = 0;
        for (Item item : this.player.inventory.itemsBody) {
            if (item.isNotNullItem()) {
                boolean hasOption38 = false;
                if (item.itemOptions != null) {
                    for (ItemOption io : item.itemOptions) {
                        if (io.optionTemplate != null && io.optionTemplate.id == 38) {
                            hasOption38 = true;
                            break;
                        }
                    }
                }
                if (hasOption38 && !isFused) {
                    continue;
                }
                switch (item.template.id) {
                    case 966:
                    case 982:
                    case 983:
                    case 883:
                    case 904:
                        player.setClothes.worldcup++;
                }
                if (item.template.id >= 592 && item.template.id <= 594) {
                    teleport = true;
                }
                long itemOpt50 = 0;
                long itemOpt77 = 0;
                long itemOpt103 = 0;
                for (ItemOption io : item.itemOptions) {
                    if (io == null || io.optionTemplate == null) {
                        continue;
                    }
                    int optId = io.optionTemplate.id;
                    if (optId == 50) {
                        itemOpt50 += io.param;
                    } else if (optId == 77) {
                        itemOpt77 += io.param;
                    } else if (optId == 103) {
                        itemOpt103 += io.param;
                    } else {
                        addOption(io);
                    }
                }
                if (itemOpt50 > 0) {
                    this.tlDame.add((int) Math.min(Integer.MAX_VALUE, itemOpt50));
                }
                if (itemOpt77 > 0) {
                    this.tlHp.add((int) Math.min(Integer.MAX_VALUE, itemOpt77));
                }
                if (itemOpt103 > 0) {
                    this.tlMp.add((int) Math.min(Integer.MAX_VALUE, itemOpt103));
                }
            }
        }
        setDameTrainArmor();
        if (this.player.isPet && ((Pet) this.player).master != null
                && ((Pet) this.player).master.inventory != null
                && ((Pet) this.player).master.inventory.itemsBody != null
                && ((Pet) this.player).master.inventory.itemsBody.size() > 7) {
            Item masterPetSlot = ((Pet) this.player).master.inventory.itemsBody.get(7);
            if (masterPetSlot != null && masterPetSlot.isNotNullItem()
                    && masterPetSlot.template != null && masterPetSlot.template.id == 1654
                    && masterPetSlot.isHaveOption(106)) {
                this.isKhongLanh = true;
            }
        }
        setBasePoint();
        setOutfitFusion();
        setSpeed();
    }

    private void addOption(ItemOption io) {
        switch (io.optionTemplate.id) {
            case 0: //Tấn công +#
                this.dameAdd += io.param;
                break;
            case 2: //HP, KI+#000
                this.hpAdd += io.param * 1000L;
                this.mpAdd += io.param * 1000L;
                break;
            case 3:// vô hiệu chưởng
                this.voHieuChuong += io.param;
                break;
            case 5: //+#% sức đánh chí mạng
                this.tlDameCrit.add(io.param);
                this.tlSDCM += io.param;
                break;
            case 6: //HP+#
                this.hpAdd += io.param;
                break;
            case 7: //KI+#
                this.mpAdd += io.param;
                break;
            case 8: //Hút #% HP, KI xung quanh mỗi 5 giây
                this.tlHutHpMpXQ += io.param;
                break;
            case 14: //Chí mạng+#%
                this.critAdd += io.param;
                break;
            case 16: // Speed
            case 114:
            case 148:
                this.tlSpeed += io.param;
                break;
            case 18: //Chinh xac
                this.tlchinhxac += io.param;
                break;
            case 19: //Tấn công+#% khi đánh quái
                this.tlDameAttMob.add(io.param);
                break;
            case 22: //HP+#K
                this.hpAdd += io.param * 1000L;
                break;
            case 23: //MP+#K
                this.mpAdd += io.param * 1000L;
                break;
            case 24: //Làm chậm
                this.isLamCham = true;
                break;
            case 25: //Tàn hình
                this.isTanHinh = true;
                break;
            case 26: //Hóa đá
                this.isHoaDa = true;
                break;
            case 27: //+# HP/30s
                this.hpHoiAdd += io.param;
                break;
            case 28: //+# KI/30s
                this.mpHoiAdd += io.param;
                break;
            case 33: //dịch chuyển tức thời
                this.teleport = true;
                break;
            case 34:
                this.setTinhAn += 1;
                break;
            case 35:
                this.setNguyetAn += 1;
                break;
            case 36:
                this.setNhatAn += 1;
                break;
            case 47: //Giáp+#
                this.defAdd += io.param;
                break;
            case 48: //HP/KI+#
                this.hpAdd += io.param;
                this.mpAdd += io.param;
                break;
            case 49: //Tấn công+#%
            case 50: //Sức đánh+#%
                this.tlDame.add(io.param);
                break;
            case 77: //HP+#%
                this.tlHp.add(io.param);
                break;
            case 80: //HP+#%/30s
                this.tlHpHoi += io.param;
                break;
            case 81: //MP+#%/30s
                this.tlMpHoi += io.param;
                break;
            case 88: //Cộng #% exp khi đánh quái
                this.tlTNSM.add(io.param);
                break;
            case 94: //Giáp #%
                this.tlGiap += io.param;
                break;
            case 95: //Biến #% tấn công thành HP
                this.tlHutHp += io.param;
                break;
            case 96: //Biến #% tấn công thành MP
                this.tlHutMp += io.param;
                break;
            case 97: //Phản #% sát thương
                this.tlPST += io.param;
                break;
            case 98: //Xuyen giap chuong
                this.tlxgc += io.param;
                break;
            case 99: //Xuyen giap can chien
                this.tlxgcc += io.param;
                break;
            case 100: //+#% vàng từ quái
                this.tlGold += io.param;
                break;
            case 101: //+#% TN,SM
                this.tlTNSM.add(io.param);
                break;
            case 103: //KI +#%
                this.tlMp.add(io.param);
                break;
            case 104: //Biến #% tấn công quái thành HP
                this.tlHutHpMob += io.param;
                break;
            case 105: //Vô hình khi không đánh quái và boss
                this.wearingVoHinh = true;
                break;
            case 106: //Không ảnh hưởng bởi cái lạnh
                this.isKhongLanh = true;
                break;
            case 111:
            case 108: //#% Né đòn
                this.tlNeDon += io.param;
                break;
            case 109: //Hôi, giảm #% HP
                this.tlHpGiamODo += io.param;
                break;
            case 110: //Do spl
                this.isDoSPL = true;
                break;
            case 116: //Kháng thái dương hạ san
                this.khangTDHS = true;
                break;
            case 226:
            case 117: //Đẹp +#% SĐ cho mình và người xung quanh
                if (io.param > this.tlSexyDame) {
                    this.tlSexyDame = io.param;
                }
                break;
            case 147: //+#% sức đánh
                this.tlDame.add(io.param);
                break;
            case 156: //Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                this.tlSubSD += 50;
                this.tlTNSM.add(io.param);
                this.tlGold += io.param;
                break;
            case 162: //Cute hồi #% KI/s bản thân và xung quanh
                this.mpHoiCute += io.param;
                break;
            case 159: // x chưởng
                this.xChuong = io.param;
                break;
            case 160: // TNSM PET;
                this.tlTNSMPet += io.param;
                break;
            case 173: //Phục hồi #% HP và KI cho đồng đội
                this.tlHpHoiBanThanVaDongDoi += io.param;
                this.tlMpHoiBanThanVaDongDoi += io.param;
                break;
            case 211:
                this.setltdb += 1;
                break;
            case 153: //% phát nổ sau khi chết
                this.tlBom += io.param;
                break;
            case 89: //Bay để phục hồi KI và HP
                if (!this.player.isPet) {
                    this.hasMountFlyRegen = true;
                }
                break;
            case 236: //+#% May mắn (Đệ tử mặc không có tác dụng)
            case 232: //Tăng nhẹ tỉ lệ rơi Thỏi vàng (chỉ cho sư phụ)
            case 235: //Tăng mạnh tỉ lệ rơi Bí kíp, nguyên liệu (chỉ cho sư phụ)
            case 253: //Tăng mạnh tỉ lệ rơi Ngọc Rồng (chỉ cho sư phụ)
            case 254: //Tăng mạnh tỉ lệ rơi Thỏi vàng (chỉ cho sư phụ)
                break;
            case 251: //#% cơ hội rơi 20 Thỏi vàng khi hạ Boss thượng cấp
                if (!this.player.isPet) {
                    this.tlJackpotThoiVangBoss += io.param;
                }
                break;
            case 252: //+#% tỉ lệ rơi Đồ Thần Linh từ Boss
                if (!this.player.isPet) {
                    this.tlRoiDoThanBoss += io.param;
                }
                break;
            case 214: //Phạm vi tuyệt kỹ +#%
                this.tlRangeTuyetKy += io.param;
                break;
            case 216: //Tuyệt kỹ +#% sát thương
                this.tlDameTuyetKy += io.param;
                break;
            case 227: //+#% Kháng khống chế (Choáng, Ru ngủ, Trói)
                if (!this.player.isPet) {
                    this.tlKhangKhongChe = Math.min(80, this.tlKhangKhongChe + io.param);
                }
                break;
        }
    }

    private void setSpeed() {
        if (player.isPl()) {
            speed = (byte) (8 + 8 * (tlSpeed / 100));
        }
    }

    private void setOutfitFusion() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isGogeta = skin.template.id == 2133 && pskin.template.id == 2134 || skin.template.id == 2134 && pskin.template.id == 2133;
        } else {
            this.isGogeta = false;
        }
    }

    private void setDameTrainArmor() {
        if (!this.player.isPet && !this.player.isBot && !this.player.isBoss) {
            if (this.player.inventory.itemsBody.size() < 7) {
                return;
            }
            try {
                Item gtl = this.player.inventory.itemsBody.get(6);
                if (gtl.isNotNullItem()) {
                    this.wearingTrainArmor = true;
                    this.player.inventory.trainArmor = gtl;
                    this.tlSubSD += ItemService.gI().getPercentTrainArmor(gtl);
                } else {
                    if (this.player.inventory.trainArmor == null
                            || !this.player.inventory.trainArmor.isNotNullItem()
                            || this.player.inventory.trainArmor.itemOptions == null
                            || !this.player.inventory.itemsBag.contains(this.player.inventory.trainArmor)) {
                        this.player.inventory.trainArmor = null;
                        gtl = this.player.inventory.itemsBag.stream().filter(item -> item != null && item.isNotNullItem() && item.template.type == 32 && item.itemOptions != null
                                && item.itemOptions.stream().filter(io -> io != null && io.optionTemplate != null && io.optionTemplate.id == 9 && io.param > 0).findFirst().orElse(null) != null).findFirst().orElse(null);
                        if (gtl == null) {
                            return;
                        }
                        this.player.inventory.trainArmor = gtl;
                    }
                    this.wearingTrainArmor = false;
                    for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                        if (io != null && io.optionTemplate != null && io.optionTemplate.id == 9 && io.param > 0) {
                            this.tlDame.add(ItemService.gI().getPercentTrainArmor(this.player.inventory.trainArmor));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Lỗi get giáp tập luyện " + this.player.name + "\n" + e + "\n");
            }
        }
    }

    public void setBasePoint() {
        setHpMax();
        setHp();
        setMpMax();
        setMp();
        setDame();
        setDef();
        setCrit();
        setHpHoi();
        setMpHoi();
        setLtdb();
        setThoBulma();
        setTinhNhatNguyetAn();
        if (this.player.setClothes != null && this.player.setClothes.setThienSu == 5) {
            this.tlxgc += 25;
            this.tlxgcc += 25;
        }
        if (!this.player.isPet && this.player.setClothes != null && this.player.setClothes.gohan == 5) {
            this.tlGold += 30;
        }
        if (!this.player.isPet && this.player.playerIntrinsic != null && this.player.playerIntrinsic.intrinsic != null
                && this.player.playerIntrinsic.intrinsic.id == 23) {
            this.tlGold += this.player.playerIntrinsic.intrinsic.param1;
        }
    }

    private void setLtdb() {
        this.islinhthuydanhbac = this.setltdb >= 5;
    }

    private void setThoBulma() {
        this.isThoBulma = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id == 584);
    }

    private void setTinhNhatNguyetAn() {
        this.isTinhAn = this.setTinhAn >= 5;
        this.isNhatAn = this.setNhatAn >= 5;
        this.isNguyetAn = this.setNguyetAn >= 5;
    }

    private void setHpHoi() {
        this.hpHoi = this.hpMax / 100;
        this.hpHoi += this.hpHoiAdd;

        if (this.tlHpHoi > 100) {
            this.tlHpHoi = 100;
        } else if (this.tlHpHoi < 0) {
            this.tlHpHoi = 0;
        }

        this.hpHoi += (this.hpMax * this.tlHpHoi / 100);

        if (this.tlHpHoiBanThanVaDongDoi > 100) {
            this.tlHpHoiBanThanVaDongDoi = 100;
        } else if (this.tlHpHoiBanThanVaDongDoi < 0) {
            this.tlHpHoiBanThanVaDongDoi = 0;
        }

        this.hpHoi += (this.hpMax * this.tlHpHoiBanThanVaDongDoi / 100);
    }

    private void setMpHoi() {
        this.mpHoi = this.mpMax / 100;
        this.mpHoi += this.mpHoiAdd;

        if (this.tlMpHoi > 100) {
            this.tlMpHoi = 100;
        } else if (this.tlMpHoi < 0) {
            this.tlMpHoi = 0;
        }

        this.mpHoi += (this.mpMax * this.tlMpHoi / 100);

        if (this.tlMpHoiBanThanVaDongDoi > 100) {
            this.tlMpHoiBanThanVaDongDoi = 100;
        } else if (this.tlMpHoiBanThanVaDongDoi < 0) {
            this.tlMpHoiBanThanVaDongDoi = 0;
        }

        this.mpHoi += ((this.mpMax * this.tlMpHoiBanThanVaDongDoi) / 100);
    }

    private void setHpMax() {
        long hpMax = this.hpg + this.hpAdd;

        if (this.tlHp != null) {
            for (Integer tl : new ArrayList<>(this.tlHp)) {
                if (tl != null) {
                    hpMax += (hpMax * tl / 100L);
                }
            }
        }

        // Xử lý set nappa
        if (this.player.setClothes.nappa == 5) {
            hpMax += (hpMax * 80L / 100L);
        }

        if (this.player.setClothes.cadicM >= 2) {
            hpMax += (hpMax * 30L / 100L);
        }
        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            hpMax += (hpMax * 10 / 100L);
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            hpMax += (hpMax * 10L / 100L);
        }

        // Xử lý set nhật ấn
        if (this.isNhatAn) {
            hpMax += (hpMax * 15L / 100L);
        }

        // Xử lý ngọc rồng đen 2 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            hpMax += (hpMax * RewardBlackBall.R2S_1 / 100L);
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentHpMonkey(player.effectSkill.levelMonkey);
                hpMax += (hpMax * percent / 100L);
            }
        }
        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 0 / 100L);
        }

        // Xử lý pet Uub
        if (this.player.isPet && ((Pet) this.player).typePet == 2 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 20 / 100L);
        }
        // Xử lý pet vageta
        if (this.player.isPet && ((Pet) this.player).typePet == 3 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 20 / 100L);
        }
        // Xử lý pet jiren
        if (this.player.isPet && ((Pet) this.player).typePet == 4 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            hpMax += (hpMax * 20 / 100L);
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            hpMax *= this.player.effectSkin.xHPKI;
        }

        // Xử lý thức ăn
        if (!this.player.isPet && this.player.itemTime != null && this.player.itemTime.isEatMeal || this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal) {
            hpMax += (hpMax * 10L / 100L);
        }

        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8062) {
            hpMax += (hpMax * 5 / 100L);
        }

        // Xử lý gogeta
        if (this.isGogeta) {
            hpMax += (hpMax * 10 / 100L);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            hpMax += 1_000_000;
        }

        // Đồng bộ công bằng Bổ Huyết / Bổ Huyết 2 nhân cho Sư phụ trước khi cộng Đệ tử (như Cuồng Nộ)
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet && !this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2L;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            hpMax = hpMax * 22L / 10L;
        }

        // Xử lý +hp đệ
        if (this.player.fusion.typeFusion != ConstPlayer.NON_FUSION && this.player.pet != null) {
            if (this.player.pet.typePet == 0) {
                hpMax += (this.player.pet.nPoint.hpMax * 40L / 100L);
            } else if (this.player.pet.typePet == 6) {
                hpMax += (this.player.pet.nPoint.hpMax * 60L / 100L);
            } else {
                hpMax += this.player.pet.nPoint.hpMax;
            }
        }

        // Xử lý huýt sáo
        if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
            if (this.player.effectSkill.tiLeHPHuytSao != 0) {
                hpMax += (hpMax * this.player.effectSkill.tiLeHPHuytSao / 100L);
            }
        }

        // Xử lý chibi (+20% HP thay vì x2 để tránh tràn trần 2.147 tỷ)
        if (this.player.effectSkill != null && this.player.effectSkill.isChibi && this.player.typeChibi == 3) {
            hpMax += (hpMax * 20L / 100L);
        }

        // Xử lý map lạnh
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            hpMax /= 2;
        }

        // Lấy tất cả option danh hiệu
        List<Item.ItemOption> options = BagesTemplate.sendListItemOption(player);

        for (Item.ItemOption opt : options) {
            if (opt.optionTemplate.id == 77) {
                hpMax += (hpMax * opt.param / 100L);
            }
        }

        if (hpMax <= 0) {
            hpMax = 1;
        }

        this.hpMax = hpMax;
        this.bossRealHpMax = this.hpMax;
    }

    private void setHp() {
        this.hp = Math.min(this.hp, this.hpMax);
        this.bossRealHp = this.hp;
    }

    public long getActualHp() {
        return this.hp;
    }

    public long getActualHpMax() {
        return this.hpMax;
    }

    public void syncDisplayHpFromBossReal() {
        if (this.bossRealHpMax > 0) {
            this.hpMax = this.bossRealHpMax;
            this.hp = Math.max(0L, Math.min(this.hpMax, this.bossRealHp));
        }
    }

    private void setMpMax() {
        // Tính toán giới hạn mpMax
        long mpMax = this.mpg + this.mpAdd;

        if (this.tlMp != null) {
            for (Integer tl : new ArrayList<>(this.tlMp)) {
                if (tl != null) {
                    mpMax += (mpMax * tl / 100L);
                }
            }
        }
        if (this.isNguyetAn) {
            mpMax += (mpMax * 15L / 100L);
        }

        // Xử lý ngọc rồng đen 6 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[5] > System.currentTimeMillis()) {
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            mpMax += (mpMax * 10L / 100L);
        }

        // xử lý pet mabu
        if (this.player.isPet && ((Pet) this.player).typePet == 1
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (mpMax * 0L / 100L);
        }

        // Xử lý pet Uub
        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (mpMax * 20L / 100L);
        }
        // xử lý pet beer
        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (mpMax * 20L / 100L);
        }
        // Xử lý pet jiren
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            mpMax += (mpMax * 20L / 100L);
        }

        // Xử lý thức ăn
        if (!this.player.isPet && this.player.itemTime != null && this.player.itemTime.isEatMeal || this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal) {
            mpMax += (mpMax * 10L / 100L);
        }

        // Xử lý phù
        if (this.player.zone
                != null && MapService.gI()
                        .isMapBlackBallWar(this.player.zone.map.mapId)) {
            mpMax *= this.player.effectSkin.xHPKI;
        }

        // Xử lý gogeta
        if (this.isGogeta) {
            mpMax += (mpMax * 10 / 100L);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            mpMax += 1_000_000;
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            mpMax += (mpMax * 10L / 100L);
        }

        // Đồng bộ công bằng Bổ Khí / Bổ Khí 2 nhân cho Sư phụ trước khi cộng Đệ tử (như Cuồng Nộ)
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi && !this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2L;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi2) {
            mpMax = mpMax * 22L / 10L;
        }

        // Xử lý hợp thể
        if (this.player.fusion.typeFusion != 0 && this.player.pet != null) {
            if (this.player.pet.typePet == 0) {
                mpMax += (this.player.pet.nPoint.mpMax * 40L / 100L);
            } else if (this.player.pet.typePet == 6) {
                mpMax += (this.player.pet.nPoint.mpMax * 60L / 100L);
            } else {
                mpMax += this.player.pet.nPoint.mpMax;
            }
        }

        // Lấy tất cả option danh hiệu
        List<Item.ItemOption> options = BagesTemplate.sendListItemOption(player);

        for (Item.ItemOption opt : options) {
            if (opt.optionTemplate.id == 103) {
                mpMax += (mpMax * opt.param / 100L);
            }
        }

        if (mpMax <= 0) {
            mpMax = 1;
        }

        this.mpMax = mpMax;
    }

    private void setMp() {
        this.mp = Math.min(this.mp, this.mpMax);
    }

    public long getHP() {
        return Math.min(this.hp, this.hpMax);
    }

    public void setHP(long hp) {
        if (hp > 0) {
            this.hp = (hp <= this.hpMax ? hp : this.hpMax);
            this.bossRealHp = this.hp;
        } else {
            this.hp = 0;
            this.bossRealHp = 0;
            player.setDie();
        }
    }

    public long getMP() {
        return Math.min(this.mp, this.mpMax);
    }

    public void setMP(long mp) {
        if (mp > 0) {
            this.mp = (mp <= this.mpMax ? mp : this.mpMax);
        } else {
            this.mp = 0;
        }
    }

    private void setDame() {
        long dame = this.dameg + this.dameAdd;

        if (this.tlDame != null) {
            for (Integer tl : new ArrayList<>(this.tlDame)) {
                if (tl != null) {
                    dame += (dame * tl / 100L);
                }
            }
        }

        // Xử lý pet mabư
        if (this.player.isPet && ((Pet) this.player).typePet == 1 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 0 / 100L);
        }

        // Xử lý pet Uub
        if (this.player.isPet && ((Pet) this.player).typePet == 2 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 20 / 100L);
        }
        // Xử lý pet beer
        if (this.player.isPet && ((Pet) this.player).typePet == 3 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 20 / 100L);
        }
        // Xử lý pet jiren
        if (this.player.isPet && ((Pet) this.player).typePet == 4 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2 || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3)) {
            dame += (dame * 20 / 100L);
        }

        // Xử lý set tinh ấn
        if (this.isTinhAn) {
            dame += (dame * 15L / 100L);
        }

        // Xử lý thức ăn
        if (!this.player.isPet && this.player.itemTime != null && this.player.itemTime.isEatMeal || this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal) {
            dame += (dame * 10 / 100L);
        }

        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8060) {
            dame += (dame * 5 / 100L);
        }

        if (this.player.setClothes.nail >= 2) {
            this.tlDameCrit.add(10);
            this.tlSDCM += 10;
        }
        // Xử lý thức ăn 2
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8061) {
            this.tlDameCrit.add(5);
            this.tlSDCM += 5;
        }

        // Xử lý cuồng nộ
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo && !this.player.itemTime.isUseCuongNo2) {
            dame *= 2L;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo2) {
            dame = dame * 22L / 10L;
        }

        // Xử lý ngọc rồng đen 1 sao
        if (this.player.rewardBlackBall.timeOutOfDateReward[0] > System.currentTimeMillis()) {
            dame += (dame * RewardBlackBall.R1S_2 / 100L);
        }

        // Xử lý set worldcup
        if (this.player.setClothes.worldcup == 2) {
            dame += (dame * 10 / 100L);
        }

        // Xử lý gogeta
        if (this.isGogeta) {
            dame += (dame * 10 / 100L);
        }

        // Phù map mabu
        if (this.player.isPhuHoMapMabu) {
            dame += 10_000;
        }

        // Xử lý rồng xương
        if (player.itemTime != null && player.itemTime.isUseRX) {
            dame += (dame * 10L / 100L);
        }

        // Xử lý phù
        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            dame *= this.player.effectSkin.xDame;
        }

        // Xử lý hợp thể
        if (this.player.fusion.typeFusion != 0 && this.player.pet != null) {
            if (this.player.pet.typePet == 0) {
                dame += (this.player.pet.nPoint.dame * 40L / 100L);
            } else if (this.player.pet.typePet == 6) {
                dame += (this.player.pet.nPoint.dame * 60L / 100L);
            } else {
                dame += this.player.pet.nPoint.dame;
            }
        }

        // Lấy tất cả option danh hiệu
        List<Item.ItemOption> options = BagesTemplate.sendListItemOption(player);

        for (Item.ItemOption opt : options) {
            if (opt.optionTemplate.id == 50) {
                dame += (dame * opt.param / 100L);
            }
        }

        if (this.player != null) {
            Player skhOwner = this.player.isPet ? ((Pet) this.player).master : this.player;
            if (skhOwner != null && SkhCollectionService.getCompletedSetCount(skhOwner) >= SkhCollectionService.NUM_SETS) {
                dame += (dame * 5L / 100L);
            }
        }

        // Xử lý khỉ
        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentDameMonkey(player.effectSkill.levelMonkey);
                dame += (dame * percent / 100L);
            }
        }
        int totalPercent = 0;
        for (Item.ItemOption opt : options) {
            if (opt.optionTemplate.id == 117) {
                tlSexyDame += opt.param;
            }
        }

        //Sức đánh đẹp
        dame += (dame * tlSexyDame / 100L);

        // Xử lý giảm dame
        dame -= (dame * tlSubSD / 100L);

        // Xử lý map cold
        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            dame /= 2;
        }

        if (dame <= 0) {
            dame = 1;
        }

        this.dame = dame;
    }

    public void setDame(long dame) {
        if (dame > 0) {
            this.dame = dame;
        } else {
            this.dame = 0;
        }
    }

    private void setDef() {
        long totalDef = (long) this.defg * 4L + this.defAdd;
        if (totalDef > Integer.MAX_VALUE) {
            totalDef = Integer.MAX_VALUE;
        } else if (totalDef < 0L) {
            totalDef = 0L;
        }
        this.def = (int) totalDef;
    }

    private void setCrit() {
        this.crit = this.critg;
        this.crit += this.critAdd;
        this.crit += this.critdragon;
        //biến khỉ
        if (this.player.effectSkill.isMonkey) {
            this.crit = 110;
        }

        if (player.setClothes.thanVuTruKaio >= 2) {
            this.crit += 15;
        }
    }

    private void resetPoint() {
        this.voHieuChuong = 0;
        this.hpAdd = 0;
        this.mpAdd = 0;
        this.dameAdd = 0;
        this.hpHoiAdd = 0;
        this.mpHoiAdd = 0;
        this.defAdd = 0;
        this.critAdd = 0;
        this.tlHp.clear();
        this.tlMp.clear();
        this.tlDef.clear();
        this.tlDame.clear();
        this.tlDameCrit.clear();
        this.tlDameAttMob.clear();
        this.tlSDCM = 0;
        this.tlHpHoiBanThanVaDongDoi = 0;
        this.tlMpHoiBanThanVaDongDoi = 0;
        this.hpHoi = 0;
        this.mpHoi = 0;
        this.mpHoiCute = 0;
        this.tlHpHoi = 0;
        this.tlMpHoi = 0;
        this.tlHutHp = 0;
        this.tlHutMp = 0;
        this.tlHutHpMob = 0;
        this.tlHutHpMpXQ = 0;
        this.tlPST = 0;
        this.tlTNSM.clear();
        this.tlDameAttMob.clear();
        this.tlGold = 0;
        this.tlNeDon = 0;
        this.tlNeDonXinbato = 0;
        this.tlBom = 0;
        this.tlGiap = 0;
        this.tlxgcc = 0;
        this.tlxgc = 0;
        this.tlchinhxac = 0;
        this.tlTNSMPet = 0;
        this.xChuong = 0;
        this.setltdb = 0;
        this.setTinhAn = 0;
        this.setNhatAn = 0;
        this.setNguyetAn = 0;
        this.tlSexyDame = 0;
        this.tlSubSD = 0;
        this.tlHpGiamODo = 0;
        this.tlSpeed = 0;
        this.teleport = false;
        this.wearingVoHinh = false;
        this.isKhongLanh = false;
        this.khangTDHS = false;
        this.isTanHinh = false;
        this.isHoaDa = false;
        this.isLamCham = false;
        this.isDoSPL = false;
        this.isThoBulma = false;
        this.tlKhangKhongChe = 0;
        this.tlJackpotThoiVangBoss = 0;
        this.tlRoiDoThanBoss = 0;
        this.tlDameTuyetKy = 0;
        this.tlRangeTuyetKy = 0;
        this.hasMountFlyRegen = false;
    }

    public void addHp(long hp) {
        if (hp > 0) {
            if (this.hp > Long.MAX_VALUE - hp) {
                this.hp = this.hpMax;
            } else {
                long potentialHp = this.hp + hp;
                this.hp = Math.min(potentialHp, this.hpMax);
            }
            this.bossRealHp = this.hp;
        }
    }

    public void addMp(long mp) {
        if (mp > 0 && this.mp > Long.MAX_VALUE - mp) {
            this.mp = this.mpMax;
            return;
        }
        long potentialMp = this.mp + mp;

        if (potentialMp > this.mpMax) {
            this.mp = this.mpMax;
        } else if (potentialMp < 0) {
            this.mp = 0;
        } else {
            this.mp = potentialMp;
        }
    }

    public void setHp(long hp) {
        if (hp < 0) {
            this.hp = 0;
        } else {
            this.hp = hp;
        }
        this.bossRealHp = this.hp;
    }

    public void setMp(long mp) {
        if (mp < 0) {
            this.mp = 0;
        } else {
            this.mp = mp;
        }
    }

    private void setIsCrit() {
        if (intrinsic != null && intrinsic.id == 25
                && this.getCurrPercentHP() <= intrinsic.param1) {
            isCrit = true;
        } else if (isCrit100) {
            isCrit100 = false;
            isCrit = true;
        } else {
            isCrit = Util.isTrue(this.crit, ConstRatio.PER100);
        }
    }

    public long getDameAttack(boolean isAttackMob) {
        return Math.max(0L, getDameAttackLong(isAttackMob));
    }

    public long getDameAttackLong(boolean isAttackMob) {
        setIsCrit();
        long dameAttack = this.dame;
        if (this.frostPoisonUntil > System.currentTimeMillis()
                && (this.player.itemTime == null || !this.player.itemTime.isUseThuocGiaiVt6)) {
            dameAttack = dameAttack * 70L / 100L;
        }
        intrinsic = this.player.playerIntrinsic.intrinsic;
        percentDameIntrinsic = 0;
        int percentDameSkill = 0;
        int percentXDame = 0;
        Skill skillSelect = (this.player.newSkill != null
                && this.player.newSkill.isStartSkillSpecial
                && this.player.newSkill.skillSelect != null)
                ? this.player.newSkill.skillSelect
                : player.playerSkill.skillSelect;
        if (skillSelect.template.id != Skill.DICH_CHUYEN_TUC_THOI && isCritTele) {
            isCrit = true;
            isCritTele = false;
        }
        switch (skillSelect.template.id) {
            case Skill.DRAGON:
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.KAMEJOKO:
                if (intrinsic != null && intrinsic.id == 2) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.songoku == 5) {
                    percentXDame = 100;
                }
                break;
            case Skill.GALICK:
                if (intrinsic != null && intrinsic.id == 16) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.kakarot == 5) {
                    percentXDame = 100;
                }
                break;
            case Skill.ANTOMIC:
                if (intrinsic != null && intrinsic.id == 17) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.TU_SAT:
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.DEMON:
                if (intrinsic != null && intrinsic.id == 8) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;
            case Skill.MASENKO:
                if (intrinsic != null && intrinsic.id == 9) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.nail == 5) {
                    percentXDame = 80;
                }
                break;

            case Skill.LIEN_HOAN:
                if (intrinsic != null && intrinsic.id == 13) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.ocTieu == 5) {
                    percentXDame = 100;
                }
                break;
            case Skill.KAIOKEN:
                if (intrinsic != null && intrinsic.id == 1) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (player.setClothes.thanVuTruKaio == 5) {
                    percentXDame = 120;
                }
                break;
            case Skill.SUPER_KAME:
            case Skill.LIEN_HOAN_CHUONG:
                percentDameSkill = skillSelect.damage;
                if (this.player.newSkill != null) {
                    if (this.player.newSkill.typePaint == 2) {
                        percentXDame += 10;
                    } else if (this.player.newSkill.typePaint == 3) {
                        percentXDame += 20;
                    }
                }
                if (this.tlDameTuyetKy > 0) {
                    percentXDame += this.tlDameTuyetKy;
                }
                break;
            case Skill.DICH_CHUYEN_TUC_THOI:
                isCrit = true;
                isCritTele = true;
                long spread = Math.max(1L, dameAttack / 20L);
                long randSpread = Util.nextLong(spread);
                dameAttack += Util.getOne(-1, 1) * randSpread;
                break;
            case Skill.MAKANKOSAPPO:
                percentDameSkill = skillSelect.damage;
                long dameSkill = this.mpMax / 100L * percentDameSkill + (this.mpMax % 100L) * percentDameSkill / 100L;
                if (this.player.setClothes.picolo == 5) {
                    dameSkill *= 2L;
                }
                return Math.max(1L, dameSkill);
            case Skill.QUA_CAU_KENH_KHI:
                long hpmob = 0;
                long hppl = 0;
                for (Mob mob : this.player.zone.mobs) {
                    if (!mob.isDie()
                            && Util.getDistance(this.player, mob) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hpmob += mob.point.hp;
                    }
                }

                for (Player pl : this.player.zone.getHumanoids()) {
                    if (!pl.isDie() && this.player.id != pl.id
                            && Util.getDistance(this.player, pl) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hppl += pl.nPoint.hp;
                    }
                }

                long dameqckk = (hpmob / 10L) + (hppl / 10L) + this.dame * 10L;

                if (this.player.setClothes.kirin == 5) {
                    dameqckk *= 2L;
                }

                dameqckk = dameqckk + (dameqckk / 100L * Util.nextInt(-5, 5));
                return Math.max(1L, dameqckk);
            case Skill.DE_TRUNG:
                if (player.setClothes.pikkoroDaimao == 5) {
                    dameAttack *= 4L;
                }
                return dameAttack;
        }

        if (intrinsic != null && intrinsic.id == 18 && this.player.effectSkill.isMonkey) {
            percentDameIntrinsic = intrinsic.param1;
        }

        if (percentDameSkill != 0) {
            dameAttack = dameAttack * percentDameSkill / 100L;
        }

        dameAttack += (dameAttack * percentDameIntrinsic / 100L);
        dameAttack += (dameAttack * dameAfter / 100L);
        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff && tlSexyDame == 0) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            dameAttack += (dameAttack * tiLeDame / 100L);
        }
        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl / 100L);
            }
            if (this.player.isPet && ((Pet) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameAttack *= 2L;
            }
        }

        dameAfter = 0;

        if (isCrit) {
            dameAttack *= 2L;
            dameAttack += (dameAttack * tlSDCM / 100L);
        }

        dameAttack += (dameAttack * percentXDame / 100L);

        long tempDameAttack = dameAttack / 100L * 5L;
        if (tempDameAttack <= 0) {
            tempDameAttack = 1;
        }
        long jitter = Util.nextLong(tempDameAttack);
        dameAttack += (long) Util.getOne(-1, 1) * jitter + 1L;

        if (player.effectSkin != null && player.effectSkin.isXChuong && (player.playerSkill.skillSelect.template.id == Skill.KAMEJOKO || player.playerSkill.skillSelect.template.id == Skill.ANTOMIC || player.playerSkill.skillSelect.template.id == Skill.MASENKO)) {
            dameAttack *= xChuong;
            player.effectSkin.isXDame = true;
            player.effectSkin.isXChuong = false;
            player.effectSkin.lastTimeXChuong = System.currentTimeMillis();
        }

        return Math.max(1L, dameAttack);
    }

    public int getCurrPercentHP() {
        if (this.hpMax <= 0) {
            return 100;
        }
        if (this.hp <= 0) {
            return 0;
        }
        if (this.hp >= this.hpMax) {
            return 100;
        }
        return (int) Math.min(100.0, Math.max(0.0, ((double) this.hp * 100.0) / (double) this.hpMax));
    }

    public int getCurrPercentMP() {
        if (this.mpMax <= 0) {
            return 100;
        }
        if (this.mp <= 0) {
            return 0;
        }
        if (this.mp >= this.mpMax) {
            return 100;
        }
        return (int) Math.min(100.0, Math.max(0.0, ((double) this.mp * 100.0) / (double) this.mpMax));
    }

    public void setFullHpMp() {
        this.hp = this.hpMax;
        this.bossRealHp = this.hpMax;
        this.mp = this.mpMax;
    }

    public void subHP(long sub) {
        if (sub <= 0) {
            return;
        }
        if (this.hp <= sub) {
            this.hp = 0;
            this.bossRealHp = 0;
            this.setHp(0);
        } else {
            this.hp -= sub;
            this.bossRealHp = this.hp;
        }
    }

    public void subMP(long sub) {
        if (sub <= 0) {
            return;
        }
        if (this.mp <= sub) {
            this.mp = 0;
        } else {
            this.mp -= sub;
        }
    }

    public long calSucManhTiemNang(long tiemNang) {
        if (power < getPowerLimit()) {
            if (this.tlTNSM != null) {
                for (Integer tl : this.tlTNSM) {
                    if (tl != null) {
                        tiemNang += (tiemNang * tl / 100);
                    }
                }
            }
            Player skhOwner = this.player.isPet ? ((Pet) this.player).master : this.player;
            if (skhOwner != null && SkhCollectionService.getCompletedSetCount(skhOwner) >= SkhCollectionService.NUM_SETS) {
                tiemNang += (tiemNang * 15L / 100L);
            }
            long tn = tiemNang;
            if (this.player.charms.tdTriTue > System.currentTimeMillis()) {
                tiemNang += tn;
            }
            if (this.player.charms.tdTriTue3 > System.currentTimeMillis()) {
                tiemNang += tn * 3;
            }
            if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
                tiemNang += tn * 4;
            }
            if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
                tiemNang += tn * 4;
            }
            if (this.player.timevip > System.currentTimeMillis()) {
                tiemNang += tn * 3;
            }
            if (this.player.effectSkill.isChibi && this.player.typeChibi == 2) {
                tiemNang += tn * 2;
            }
            if (this.player.getSession() != null && this.player.getSession().vip > 0 || this.player.isPet
                    && ((Pet) this.player).master.getSession() != null && ((Pet) this.player).master.getSession().vip > 0) {
                tiemNang += tn * 3;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseDK) {
                tiemNang += tn * 2;
            }
            if (player.zone.map.mapId >= 135 && player.zone.map.mapId <= 138) {
                if (this.player.itemTime != null && this.player.itemTime.isUseKhoBauX2) {
                    tiemNang += tn * 2;
                }
            }
            if (this.player.satellite != null && this.player.satellite.isIntelligent) {
                tiemNang += tn / 5;
            }
            if (this.intrinsic != null && this.intrinsic.id == 24) {
                tiemNang += (tiemNang * this.intrinsic.param1 / 100);
            }
            if (this.power >= 60_000_000_000L) {
                tiemNang -= (tiemNang * 80 / 100);
            }
            if (this.player.isPet) {
                if (((Pet) this.player).master.itemTime.isUseBuaSanta) {
                    tiemNang += tn * 2;
                }
                if (((Pet) this.player).master.nPoint != null && ((Pet) this.player).master.nPoint.tlTNSMPet > 0) {
                    tiemNang += (tn * ((Pet) this.player).master.nPoint.tlTNSMPet) / 100L;
                }
            }
            if (this.player.zone != null && this.player.zone.map != null
                    && MapService.gI().isMapNguHanhSon(this.player.zone.map.mapId)) {
                tiemNang *= 2;
            }
            if (MapService.gI().AllMap(this.player.zone.map.mapId)) {
                //  tiemNang *= 10; //x x3 tiềm năng toàn server
            }
            if (MapService.gI().isMapBanDoKhoBau(this.player.zone.map.mapId)) {
                tiemNang += tiemNang / 2;
            }
            if (this.player.cFlag != 0) {
                if (this.player.cFlag == 8) {
                    tiemNang += (tiemNang * 10 / 100);
                } else {
                    tiemNang += (tiemNang * 5 / 100);
                }
            }
            tiemNang *= Manager.RATE_EXP_SERVER;
            tiemNang = calSubTNSM(tiemNang);
            if (tiemNang <= 0) {
                tiemNang = 1;
            }
        } else {
            tiemNang = 10;
        }
        return tiemNang;
    }

    public long calSubTNSM(long tiemNang) {
        if (power >= 150_000_000_000L) {
            tiemNang /= 140;
        } else if (power >= 120_000_000_000L) {
            tiemNang /= 120;
        } else if (power >= 100_000_000_000L) {
            tiemNang /= 110;
        } else if (power >= 90_000_000_000L) {
            tiemNang /= 100;
        } else if (power >= 80_000_000_000L) {
            tiemNang /= 90;
        } else if (power >= 60_000_000_000L) {
            tiemNang /= 50;
        } else if (power >= 50_000_000_000L) {
            tiemNang /= 40;
        } else if (power >= 40_000_000_000L) {
            tiemNang /= 30;
        }
        return tiemNang;
    }

    public int getTileHutHp(boolean isMob) {
        if (isMob) {
            return Math.max(0, Math.min(80, this.tlHutHp + this.tlHutHpMob));
        } else {
            return Math.max(0, Math.min(50, this.tlHutHp));
        }
    }

    public int getTiLeHutMp() {
        return Math.max(0, Math.min(80, this.tlHutMp));
    }

    public long subDameInjureWithDeff(long dame) {
        return subDameInjureWithDeff(dame, null);
    }

    public long subDameInjureWithDeff(long dame, Player plAtt) {
        if (dame <= 0) {
            return 0;
        }
        long def = this.def;
        if (plAtt != null && plAtt.nPoint != null) {
            Skill activeSkill = null;
            if (plAtt.newSkill != null && plAtt.newSkill.isStartSkillSpecial && plAtt.newSkill.skillSelect != null) {
                activeSkill = plAtt.newSkill.skillSelect;
            } else if (plAtt.playerSkill != null) {
                activeSkill = plAtt.playerSkill.skillSelect;
            }
            if (activeSkill != null && activeSkill.template != null) {
                int pen = 0;
                switch (activeSkill.template.id) {
                    case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC,
                         Skill.MAKANKOSAPPO, Skill.QUA_CAU_KENH_KHI,
                         Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG, Skill.MA_PHONG_BA ->
                        pen = plAtt.nPoint.tlxgc;
                    case Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.LIEN_HOAN, Skill.KAIOKEN ->
                        pen = plAtt.nPoint.tlxgcc;
                    case Skill.TU_SAT ->
                        pen = Math.max(plAtt.nPoint.tlxgc, plAtt.nPoint.tlxgcc);
                }
                if (pen > 0) {
                    pen = Math.min(100, pen);
                    def -= def * pen / 100;
                }
            }
        }
        dame -= def;
        if (dame <= 0) {
            dame = 1;
        }
        return dame;
    }

    /*------------------------------------------------------------------------*/
    public boolean canOpenPower() {
        return this.power >= getPowerLimit();
    }

    public long getPowerLimit() {
        switch (limitPower) {
            case 0:
                return 17999999999L;
            case 1:
                return 19999999999L;
            case 2:
                return 24999999999L;
            case 3:
                return 29999999999L;
            case 4:
                return 39999999999L;
            case 5:
                return 50010000000L;
            case 6:
                return 60010000000L;
            case 7:
                return 70010000000L;
            case 8:
                return 80010000000L;
            case 9:
                return 90010000000L;
            case 10:
                return 100010000000L;
            case 11:
                return 120010000000L;
            case 12:
                return 150010000000L;
            case 13:
                return 200010000000L;
            default:
                return 200010000000L;
        }
    }

    public long getPowerNextLimit() {
        switch (limitPower + 1) {
            case 0:
                return 17999999999L;
            case 1:
                return 19999999999L;
            case 2:
                return 24999999999L;
            case 3:
                return 29999999999L;
            case 4:
                return 39999999999L;
            case 5:
                return 50010000000L;
            case 6:
                return 60010000000L;
            case 7:
                return 70010000000L;
            case 8:
                return 80010000000L;
            case 9:
                return 90010000000L;
            case 10:
                return 100010000000L;
            case 11:
                return 120010000000L;
            case 12:
                return 150010000000L;
            case 13:
                return 200010000000L;
            default:
                return 0;
        }
    }

    public long getHpMpLimit() {
        switch (limitPower) {
            case 0:
                return 220000;
            case 1:
                return 240000;
            case 2:
                return 300000;
            case 3:
                return 350000;
            case 4:
                return 400000;
            case 5:
                return 450000;
            case 6:
                return 500000;
            case 7:
                return 525000;
            case 8:
                return 550000;
            case 9:
                return 575000;
            case 10:
                return 600000;
            case 11:
                return 650000;
            case 12:
                return 700000;
            case 13:
                return 750000;
            default:
                return 750000;
        }
    }

    public long getDameLimit() {
        switch (limitPower) {
            case 0:
                return 11000;
            case 1:
                return 12000;
            case 2:
                return 15000;
            case 3:
                return 18000;
            case 4:
                return 20000;
            case 5:
                return 22000;
            case 6:
                return 24000;
            case 7:
                return 25000;
            case 8:
                return 26000;
            case 9:
                return 27000;
            case 10:
                return 28500;
            case 11:
                return 30500;
            case 12:
                return 32500;
            case 13:
                return 35000;
            default:
                return 35000;
        }
    }

    public short getDefLimit() {
        switch (limitPower) {
            case 0:
                return 550;
            case 1:
                return 600;
            case 2:
                return 700;
            case 3:
                return 800;
            case 4:
                return 1000;
            case 5:
                return 1200;
            case 6:
                return 1400;
            case 7:
                return 1500;
            case 8:
                return 1600;
            case 9:
                return 1800;
            case 10:
                return 2000;
            case 11:
                return 2200;
            case 12:
                return 2500;
            case 13:
                return 3000;
            default:
                return 3000;
        }
    }

    public byte getCritLimit() {
        switch (limitPower) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            case 6:
                return 7;
            case 7:
                return 8;
            case 8:
                return 9;
            case 9:
                return 10;
            case 10:
                return 11;
            case 11:
                return 12;
            case 12:
                return 13;
            case 13:
                return 14;
            default:
                return 14;
        }
    }

    private static final long[] CRIT_TIEM_NANG_COSTS = {
        50_000_000L, 250_000_000L, 1_250_000_000L, 5_000_000_000L, 15_000_000_000L,
        30_000_000_000L, 45_000_000_000L, 60_000_000_000L, 75_000_000_000L, 90_000_000_000L,
        110_000_000_000L, 130_000_000_000L, 150_000_000_000L, 170_000_000_000L
    };

    public void powerUp(long power) {
        this.power += power;
        TaskService.gI().checkDoneTaskPower(player, this.power);
    }

    public void tiemNangUp(long tiemNang) {
        this.tiemNang += tiemNang;
    }

    public void increasePoint(byte type, short point) {
        increasePoint(type, point, !player.isPet);
    }

    public void increasePoint(byte type, short point, boolean notifyLimit) {
        if (point <= 0 || point > 1000) {
            return;
        }
        Player notifyTarget = player.isPet ? ((Pet) player).master : player;
        long tiemNangUse;
        if (type == 0) {
            long maxPointsAdd = (getHpMpLimit() - this.hpg) / 20L;
            if (maxPointsAdd <= 0) {
                if (notifyLimit && notifyTarget != null) {
                    Service.gI().sendThongBaoOK(notifyTarget, "Vui lòng mở giới hạn sức mạnh");
                }
                return;
            }
            if (point > maxPointsAdd) {
                point = (short) maxPointsAdd;
            }
            long pointHp = (long) point * 20L;
            tiemNangUse = (long) point * (2L * (this.hpg + 1000L) + pointHp - 20L) / 2L;
            if (doUseTiemNang(tiemNangUse)) {
                hpg += pointHp;
            }
        }
        if (type == 1) {
            long maxPointsAdd = (getHpMpLimit() - this.mpg) / 20L;
            if (maxPointsAdd <= 0) {
                if (notifyLimit && notifyTarget != null) {
                    Service.gI().sendThongBaoOK(notifyTarget, "Vui lòng mở giới hạn sức mạnh");
                }
                return;
            }
            if (point > maxPointsAdd) {
                point = (short) maxPointsAdd;
            }
            long pointMp = (long) point * 20L;
            tiemNangUse = (long) point * (2L * (this.mpg + 1000L) + pointMp - 20L) / 2L;
            if (doUseTiemNang(tiemNangUse)) {
                mpg += pointMp;
            }
        }
        if (type == 2) {
            long maxPointsAdd = getDameLimit() - this.dameg;
            if (maxPointsAdd <= 0) {
                if (notifyLimit && notifyTarget != null) {
                    Service.gI().sendThongBaoOK(notifyTarget, "Vui lòng mở giới hạn sức mạnh");
                }
                return;
            }
            if (point > maxPointsAdd) {
                point = (short) maxPointsAdd;
            }
            TaskService.gI().checkDoneTaskNangCS(player);
            tiemNangUse = (long) point * (2L * this.dameg + point - 1L) / 2L * 100L;
            if (doUseTiemNang(tiemNangUse)) {
                dameg += point;
            }
            TaskService.gI().checkDoneTaskNangCS(player);
        }
        if (type == 3) {
            int maxPointsAdd = getDefLimit() - this.defg;
            if (maxPointsAdd <= 0) {
                if (notifyLimit && notifyTarget != null) {
                    Service.gI().sendThongBaoOK(notifyTarget, "Vui lòng mở giới hạn sức mạnh");
                }
                return;
            }
            if (point > maxPointsAdd) {
                point = (short) maxPointsAdd;
            }
            tiemNangUse = (long) point * (2L * (this.defg + 5L) + point - 1L) / 2L * 100000L;
            if (doUseTiemNang(tiemNangUse)) {
                defg += point;
            }
        }
        if (type == 4) {
            if ((this.critg + 1) > getCritLimit()) {
                if (notifyLimit && notifyTarget != null) {
                    Service.gI().sendThongBaoOK(notifyTarget, "Vui lòng mở giới hạn sức mạnh");
                }
                return;
            }
            int idx = Math.max(0, Math.min(this.critg, CRIT_TIEM_NANG_COSTS.length - 1));
            tiemNangUse = CRIT_TIEM_NANG_COSTS[idx];
            if (doUseTiemNang(tiemNangUse)) {
                critg += 1;
            }
        }
        Service.gI().point(player);
    }

    private boolean doUseTiemNang(long tiemNang) {
        if (this.tiemNang < tiemNang) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ tiềm năng");
            return false;
        }
        if (this.tiemNang >= tiemNang && this.tiemNang - tiemNang >= 0) {
            this.tiemNang -= tiemNang;
            TaskService.gI().checkDoneTaskUseTiemNang(player);
            return true;
        }
        return false;
    }

    public long getFullTN() {
        long tnhp = 0, tnki = 0, tnsd = 0, tng = 0, tncm = 0;

        if (hpg > 0) {
            tnhp = (((hpg / 20L) * (50L + (50L + (hpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (mpg > 0) {
            tnki = (((mpg / 20L) * (50L + (50L + (mpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (dameg > 0) {
            tnsd = ((dameg * (dameg - 1L) * 100L) / 2L);
        }
        if (defg > 0) {
            tng = ((defg * (500000L + (500000L + (defg - 1L) * 100000L))) / 2L);
        }
        if (critg > 0) {
            tncm = ((50L * (((long) Math.pow(5L, critg) - 1L)) / (5L - 1L) * 1000000L));
        }
        return tnhp + tnki + tnsd + tng + tncm;
    }

    //--------------------------------------------------------------------------
    private long lastTimeHoiPhuc;
    private long lastTimeHoiStamina;

    public void update() {
        if (player != null && player.effectSkill != null) {
            if (player.effectSkill.isCharging && player.effectSkill.countCharging < 10) {
                int tiLeHoiPhuc = SkillUtil.getPercentCharge(player.playerSkill.skillSelect.point);
                long curActualHp = getActualHp();
                long maxActualHp = getActualHpMax();
                if (player.effectSkill.isCharging && !player.isDie() && !player.effectSkill.isHaveEffectSkill()
                        && (curActualHp < maxActualHp || mp < mpMax)) {
                    long hpRecovered = maxActualHp / 100L * tiLeHoiPhuc;
                    long mpRecovered = mpMax / 100L * tiLeHoiPhuc;

                    if (curActualHp + hpRecovered > maxActualHp) {
                        hpRecovered = maxActualHp - curActualHp;
                    }
                    if (mp + mpRecovered > mpMax) {
                        mpRecovered = mpMax - mp;
                    }

                    PlayerService.gI().hoiPhuc(player, hpRecovered, mpRecovered);

                    if (player.effectSkill.countCharging % 3 == 0) {
                        Service.gI().chat(player, "Phục hồi năng lượng " + getCurrPercentHP() + "%");
                    }
                } else {
                    EffectSkillService.gI().stopCharge(player);
                }
                if (++player.effectSkill.countCharging >= 10) {
                    EffectSkillService.gI().stopCharge(player);
                }
            }

            if (player.isPl() && player.isFly && player.getMount() != -1 && this.hasMountFlyRegen
                    && !player.isDie() && Util.canDoWithTime(this.lastTimeMountRegen, 2000)) {
                this.lastTimeMountRegen = System.currentTimeMillis();
                long hpRegen = (this.hp < this.hpMax) ? Math.max(1L, this.hpMax / 50L) : 0L;
                long mpRegen = (this.mp < this.mpMax) ? Math.max(1L, this.mpMax / 50L) : 0L;
                if (hpRegen > 0 || mpRegen > 0) {
                    PlayerService.gI().hoiPhuc(this.player, hpRegen, mpRegen);
                }
            }

            if (Util.canDoWithTime(lastTimeHoiPhuc, 30000)) {
                PlayerService.gI().hoiPhuc(this.player, hpHoi, mpHoi);
                this.lastTimeHoiPhuc = System.currentTimeMillis();
            }

            if (Util.canDoWithTime(lastTimeHoiStamina, 60000) && this.stamina < this.maxStamina) {
                this.stamina++;
                this.lastTimeHoiStamina = System.currentTimeMillis();

                if (!this.player.isBoss && !this.player.isPet) {
                    PlayerService.gI().sendCurrentStamina(this.player);
                }
            }
        }
    }

    public void dispose() {
        this.intrinsic = null;
        this.player = null;
        this.tlHp = null;
        this.tlMp = null;
        this.tlDef = null;
        this.tlDame = null;
        this.tlDameAttMob = null;
        this.tlTNSM = null;
    }

    public long calPercent(long param, int percent) {
        return param * percent / 100;
    }

}
