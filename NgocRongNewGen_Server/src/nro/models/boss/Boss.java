package nro.models.boss;

import nro.models.boss.Boss_Manager.BrolyManager;
import nro.models.boss.Boss_Manager.GasDestroyManager;
import nro.models.boss.Boss_Manager.RedRibbonHQManager;
import nro.models.boss.Boss_Manager.SkillSummonedManager;
import nro.models.boss.Boss_Manager.TreasureUnderSeaManager;
import nro.models.boss.Boss_Manager.FinalBossManager;
import nro.models.boss.Boss_Manager.YardartManager;
import nro.models.boss.Boss_Manager.SnakeWayManager;
import nro.models.boss.Boss_Manager.OtherBossManager;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.AppearType;
import nro.models.consts.BossStatus;
import nro.models.consts.BossType;
import nro.models.consts.ConstPlayer;
import static nro.models.consts.BossStatus.ACTIVE;
import static nro.models.consts.BossStatus.AFK;
import static nro.models.consts.BossStatus.CHAT_E;
import static nro.models.consts.BossStatus.CHAT_S;
import static nro.models.consts.BossStatus.DIE;
import static nro.models.consts.BossStatus.JOIN_MAP;
import static nro.models.consts.BossStatus.LEAVE_MAP;
import static nro.models.consts.BossStatus.RESPAWN;
import static nro.models.consts.BossStatus.REST;
import static nro.models.consts.BossType.BROLY;
import static nro.models.consts.BossType.CHRISTMAS_EVENT;
import static nro.models.consts.BossType.FINAL;
import static nro.models.consts.BossType.HALLOWEEN_EVENT;
import static nro.models.consts.BossType.HUNGVUONG_EVENT;
import static nro.models.consts.BossType.PHOBAN;
import static nro.models.consts.BossType.PHOBANBDKB;
import static nro.models.consts.BossType.PHOBANCDRD;
import static nro.models.consts.BossType.PHOBANDT;
import static nro.models.consts.BossType.PHOBANKGHD;
import static nro.models.consts.BossType.SKILLSUMMONED;
import static nro.models.consts.BossType.TET_EVENT;
import static nro.models.consts.BossType.TRUNGTHU_EVENT;
import static nro.models.consts.BossType.YARDART;
import nro.models.network.Message;
import java.util.List;
import nro.models.map.Zone;
import nro.models.mob.Mob;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.skill.Skill;
import nro.models.server.ServerNotify;
import nro.models.map.service.MapService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.services.TaskService;
import nro.models.map.service.ChangeMapService;
import nro.models.utils.Logger;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;
import nro.models.interfaces.IBoss;
import java.io.IOException;

public class Boss extends Player implements IBoss {

    public int currentLevel = -1;
    public final BossData[] data;

    public BossStatus bossStatus;

    protected Zone lastZone;

    protected long lastTimeRest;
    protected int secondsRest;

    protected long lastTimeChatS;
    protected int timeChatS;
    protected byte indexChatS;

    protected long lastTimeChatE;
    protected int timeChatE;
    protected byte indexChatE;

    protected long lastTimeChatM;
    protected int timeChatM;

    protected long lastTimeTargetPlayer;
    protected int timeTargetPlayer;
    protected Player playerTarger;

    protected Boss parentBoss;
    public Boss[][] bossAppearTogether;

    public Zone zoneFinal = null;

    public Player playerReward;

    public int lv;

    public int error;

    public volatile boolean prepareBom;

    public boolean isNotifyDisabled;
    public boolean isZone01SpawnDisabled;

    public Boss(int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(BossType bossType, int id, boolean isNotifyDisabled, boolean isZone01SpawnDisabled, BossData... data) throws Exception {
        this(bossType, id, data);
        this.isNotifyDisabled = isNotifyDisabled;
        this.isZone01SpawnDisabled = isZone01SpawnDisabled;
    }

    public Boss(int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }
        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        BossManager.gI().addBoss(this);

        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    Boss boss = BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                    if (boss != null) {
                        boss.parentBoss = this;
                        boss.lv = j;
                        this.bossAppearTogether[i][j] = boss;
                    }
                }
            }
        }
    }

    public Boss(BossType bossType, int id, BossData... data) throws Exception {
        this.id = id;
        this.isBoss = true;
        if (data == null || data.length == 0) {
            throw new Exception("Dữ liệu boss không hợp lệ");
        }
        this.data = data;
        this.secondsRest = this.data[0].getSecondsRest();
        this.bossStatus = BossStatus.REST;
        switch (bossType) {
            case YARDART ->
                YardartManager.gI().addBoss(this);
            case FINAL ->
                FinalBossManager.gI().addBoss(this);
            case SKILLSUMMONED ->
                SkillSummonedManager.gI().addBoss(this);
            case BROLY ->
                BrolyManager.gI().addBoss(this);
            case PHOBAN ->
                OtherBossManager.gI().addBoss(this);
            case PHOBANDT ->
                RedRibbonHQManager.gI().addBoss(this);
            case PHOBANBDKB ->
                TreasureUnderSeaManager.gI().addBoss(this);
            case PHOBANCDRD ->
                SnakeWayManager.gI().addBoss(this);
            case PHOBANKGHD ->
                GasDestroyManager.gI().addBoss(this);
            default ->
                BossManager.gI().addBoss(this);
        }

        this.bossAppearTogether = new Boss[this.data.length][];
        for (int i = 0; i < this.bossAppearTogether.length; i++) {
            if (this.data[i].getBossesAppearTogether() != null) {
                this.bossAppearTogether[i] = new Boss[this.data[i].getBossesAppearTogether().length];
                for (int j = 0; j < this.data[i].getBossesAppearTogether().length; j++) {
                    Boss boss = BossManager.gI().createBoss(this.data[i].getBossesAppearTogether()[j]);
                    if (boss != null) {
                        boss.parentBoss = this;
                        this.bossAppearTogether[i][j] = boss;
                    }
                }
            }
        }
    }

    public static final int ITEM_HON_BOSS_LAM = 2016;
    public static final int ITEM_HON_BOSS_LUC = 2017;
    public static final int ITEM_HON_BOSS_TIM = 2018;
    public static final int ITEM_MA_HON_HUY_DIET = 2019;

    protected boolean soulDroppedForCurrentLife;

    @Override
    public void initBase() {
        this.soulDroppedForCurrentLife = false;
        BossData data = this.data[this.currentLevel];
        this.name = String.format(data.getName(), Util.nextInt(0, 100));
        this.gender = data.getGender();
        this.nPoint.mpg = 31_07_2002;
        this.nPoint.dameg = data.getDame();
        long baseHp = data.getHp()[Util.nextInt(0, data.getHp().length - 1)];
        if (baseHp > 2_147_483_647L) {
            this.nPoint.bossRealHpMax = baseHp;
            this.nPoint.bossRealHp = baseHp;
            this.nPoint.hpg = 2_000_000_000;
        } else {
            this.nPoint.bossRealHpMax = 0L;
            this.nPoint.bossRealHp = 0L;
            this.nPoint.hpg = (int) baseHp;
        }
        this.nPoint.hp = this.nPoint.hpg;
        this.nPoint.calPoint();
        if (this.nPoint.bossRealHpMax > 2_147_483_647L) {
            this.nPoint.bossRealHp = this.nPoint.bossRealHpMax;
            this.nPoint.syncDisplayHpFromBossReal();
        }
        this.initSkill();
        this.resetBase();
    }

    protected void initSkill() {
        for (Skill skill : this.playerSkill.skills) {
            skill.dispose();
        }
        this.playerSkill.skills.clear();
        this.playerSkill.skillSelect = null;
        int[][] skillTemps = data[this.currentLevel].getSkillTemp();
        for (int[] skillTemp : skillTemps) {
            Skill skill = SkillUtil.createSkill(skillTemp[0], skillTemp[1]);
            if (skillTemp.length == 3) {
                skill.coolDown = skillTemp[2];
            }
            this.playerSkill.skills.add(skill);
        }
    }

    protected void resetBase() {
        this.lastTimeChatS = 0;
        this.lastTimeChatE = 0;
        this.timeChatS = 0;
        this.timeChatE = 0;
        this.indexChatS = 0;
        this.indexChatE = 0;
    }

    //.outfit.
    @Override
    public short getHead() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][0];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        }
        return this.data[this.currentLevel].getOutfit()[0];
    }

    @Override
    public short getBody() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][1];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        }
        return this.data[this.currentLevel].getOutfit()[1];
    }

    @Override
    public short getLeg() {
        if (effectSkill != null && effectSkill.isBinh) {
            return idOutfitMafuba[effectSkill.typeBinh][2];
        }
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        }
        return this.data[this.currentLevel].getOutfit()[2];

    }

    @Override
    public short getFlagBag() {
        return this.data[this.currentLevel].getOutfit()[3];
    }

    @Override
    public byte getAura() {
        return (byte) this.data[this.currentLevel].getOutfit()[4];
    }

    @Override
    public byte getEffFront() {
        return (byte) this.data[this.currentLevel].getOutfit()[5];
    }

    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0, this.data[this.currentLevel].getMapJoin().length - 1)];
        Zone map = MapService.gI().getMapWithRandZone(mapId);
        return map;
    }

    @Override
    public void changeStatus(BossStatus status) {
        this.bossStatus = status;
    }

    @Override
    public Player getPlayerAttack() {
        if (this.zone == null) {
            return null;
        }
        if (this.playerTarger != null && (this.playerTarger.isDie() || !this.zone.equals(this.playerTarger.zone))) {
            this.playerTarger = null;
        }
        if (this.playerTarger == null || Util.canDoWithTime(this.lastTimeTargetPlayer, this.timeTargetPlayer)) {
            this.playerTarger = this.zone.getRandomPlayerInMap();
            this.lastTimeTargetPlayer = System.currentTimeMillis();
            this.timeTargetPlayer = Util.nextInt(5000, 7000);
        }
        if (this.playerTarger != null && this.playerTarger.isPet && ((Pet) this.playerTarger).master != null && ((Pet) this.playerTarger).master.equals(this)) {
            this.playerTarger = null;
        }
        return this.playerTarger;
    }

    @Override
    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }

    @Override
    public void changeToTypeNonPK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.NON_PK);
    }

    @Override
    public void updateInfo() {
        super.update();
    }

    @Override
    public void update() {
        if (prepareBom) {
            return;
        }
        super.update();
        this.nPoint.mp = this.nPoint.mpg;
        if (this.effectSkill == null || this.effectSkill.isHaveEffectSkill() || (this.newSkill != null && this.newSkill.isStartSkillSpecial)) {
            return;
        }
        switch (this.bossStatus) {
            case CHAT_S, AFK, ACTIVE ->
                this.autoLeaveMap();
        }
        switch (this.bossStatus) {
            case REST ->
                this.rest();
            case RESPAWN -> {
                this.respawn();
                this.changeStatus(BossStatus.JOIN_MAP);
            }
            case JOIN_MAP ->
                this.joinMap();
            case CHAT_S -> {
                if (chatS()) {
                    this.doneChatS();
                    this.lastTimeChatM = System.currentTimeMillis();
                    this.timeChatM = 5000;
                    if (this.bossStatus != BossStatus.AFK) {
                        this.changeStatus(BossStatus.ACTIVE);
                    }
                }
            }
            case AFK ->
                this.afk();
            case ACTIVE -> {
                this.chatM();
                if (!this.isDie() && this.typePk != ConstPlayer.NON_PK && Util.canDoWithTime(this.lastTimeSyncTypePk, 2500)) {
                    this.lastTimeSyncTypePk = System.currentTimeMillis();
                    PlayerService.gI().sendTypePk(this);
                }
                if (this.typePk != ConstPlayer.NON_PK && (this.effectSkill.isCharging && !Util.isTrue(1, 20) || this.effectSkill.useTroi)) {
                    return;
                }
                this.active();
            }
            case DIE ->
                this.changeStatus(BossStatus.CHAT_E);
            case CHAT_E -> {
                if (chatE()) {
                    this.doneChatE();
                    this.changeStatus(BossStatus.LEAVE_MAP);
                }
            }
            case LEAVE_MAP ->
                this.leaveMap();
        }
    }

    @Override
    public void rest() {
        int nextLevel = this.currentLevel + 1;
        if (nextLevel >= this.data.length) {
            nextLevel = 0;
        }
        if (this.data[nextLevel].getTypeAppear() == AppearType.DEFAULT_APPEAR
                && Util.canDoWithTime(lastTimeRest, secondsRest * 1000)) {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void afk() {

    }

    @Override
    public void respawn() {
        this.currentLevel++;
        if (this.currentLevel >= this.data.length) {
            this.currentLevel = 0;
        }
        this.initBase();
        this.changeToTypeNonPK();
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            this.wakeupAnotherBossWhenAppear();
            return;
        }
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }
        if (this.zone == null) {
            this.zone = getMapJoin();
        }
        if (this.zone != null) {
            try {
                if (this.currentLevel == 0) {
                    if (this.parentBoss == null) {
                        int zoneid = 0;
                        //this.zone.map.mapId == 80 || this.zone.map.mapId == 103 || this.zone.map.mapId == 97 || this.zone.map.mapId == 102
                        // Chỉ cho boss xuất hiện từ khu 2 trở lên ở map thường
                        if (this.isZone01SpawnDisabled && this.zone.map.zones.size() > 2) {
                            zoneid = Util.nextInt(2, this.zone.map.zones.size() - 1);
                            while (zoneid < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
                                zoneid++;
                            }

                            if (zoneid < this.zone.map.zones.size()) {
                                this.zone = this.zone.map.zones.get(zoneid);
                            } else {
                                this.changeStatus(BossStatus.REST);
                                this.zone = null;
                                this.lastZone = null;
                                return;
                            }
                        } else {
                            // Check trong khu lớn hơn 10 người chuyển sang khu n + 1
                            while (zoneid < this.zone.map.zones.size() && this.zone.map.zones.get(zoneid).getNumOfPlayers() > 10) {
                                zoneid++;
                            }
                            // Check trong khu có boss sẽ chuyển sang khu n + 1
                            while (zoneid < this.zone.map.zones.size() && !this.zone.map.zones.get(zoneid).getBosses().isEmpty()) {
                                zoneid++;
                            }
                            if (zoneid < this.zone.map.zones.size()) {
                                this.zone = this.zone.map.zones.get(zoneid);
                            } else {
                                this.zone = this.zone.map.zones.get(0);
                            }
                        }
                        int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
                        int y = this.zone.map.yPhysicInTop(x, 100);
                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
                    } else {
                        int x = this.parentBoss.location.x - (this.lv + 1) * 30;
                        int y = this.zone.map.yPhysicInTop(x, 100);
                        ChangeMapService.gI().changeMap(this, this.zone, x, y);
                    }
                    this.wakeupAnotherBossWhenAppear();
                } else {
                    ChangeMapService.gI().changeMap(this, this.zone, this.location.x, this.location.y);
                }
                Service.gI().sendFlagBag(this);
                this.notifyJoinMap();
                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
                if (error < 5) {
                    Logger.error("Lỗi : " + e + "\n");
                    error++;
                }
            }
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    public void joinMapByZone(Zone zone) {
        this.soulDroppedForCurrentLife = false;
        if (zone != null) {
            this.zone = zone;
            int x = this.zone.map.mapWidth > 100 ? Util.nextInt(100, this.zone.map.mapWidth - 100) : Util.nextInt(100);
            int y = this.zone.map.yPhysicInTop(x, 100);
            ChangeMapService.gI().changeMap(this, this.zone, x, y);
        }
    }

    protected void notifyJoinMap() {
        if (canSendNotify()) {
            ServerNotify.gI().notify("BOSS " + this.name + " vừa xuất hiện tại " + this.zone.map.mapName);
        }
    }

    private boolean canSendNotify() {
        return !(this.isNotifyDisabled || this.zone.map.mapId == 140
                || this.zone.map.mapId == 111
                || MapService.gI().isMapPhoBan(this.zone.map.mapId)
                || MapService.gI().isMapMaBu(this.zone.map.mapId)
                || MapService.gI().isMapBlackBallWar(this.zone.map.mapId));
    }

    @Override
    public boolean chatS() {
        if (Util.canDoWithTime(lastTimeChatS, timeChatS)) {
            if (this.indexChatS == this.data[this.currentLevel].getTextS().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextS()[this.indexChatS];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.lastTimeChatS = System.currentTimeMillis();
            this.timeChatS = textChat.length() * 100;
            if (this.timeChatS > 2000) {
                this.timeChatS = 2000;
            }
            this.indexChatS++;
        }
        return false;
    }

    @Override
    public void doneChatS() {

    }

    @Override
    public void chatM() {
        if (this.typePk == ConstPlayer.NON_PK) {
            return;
        }
        if (this.data[this.currentLevel].getTextM().length == 0) {
            return;
        }
        if (!Util.canDoWithTime(this.lastTimeChatM, this.timeChatM)) {
            return;
        }
        String textChat = this.data[this.currentLevel].getTextM()[Util.nextInt(0, this.data[this.currentLevel].getTextM().length - 1)];
        int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
        textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
        this.chat(prefix, textChat);
        this.lastTimeChatM = System.currentTimeMillis();
        this.timeChatM = Util.nextInt(3000, 20000);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    protected long lastTimeAttack;
    protected long lastTimeSyncTypePk;

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                if (Util.getDistance(this, pl) <= this.getRangeCanAttackWithSkillSelect()) {
                    if (Util.isTrue(5, 20)) {
                        if (SkillUtil.isUseSkillChuong(this)) {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(20, 200)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 70));
                        } else {
                            this.moveTo(pl.location.x + (Util.getOne(-1, 1) * Util.nextInt(10, 40)),
                                    Util.nextInt(10) % 2 == 0 ? pl.location.y : pl.location.y - Util.nextInt(0, 50));
                        }
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void checkPlayerDie(Player player) {
        if (player.isDie()) {

        }
    }

    protected int getRangeCanAttackWithSkillSelect() {
        int skillId = this.playerSkill.skillSelect.template.id;
        if (skillId == Skill.KAMEJOKO || skillId == Skill.MASENKO || skillId == Skill.ANTOMIC) {
            return Skill.RANGE_ATTACK_CHIEU_CHUONG;
        } else if (skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK || skillId == Skill.LIEN_HOAN || skillId == Skill.KAIOKEN) {
            return Skill.RANGE_ATTACK_CHIEU_DAM;
        }
        return 500;
    }

    protected boolean useLegacyDefaultBossDrops() {
        return switch ((int) this.id) {
            case BossID.TAU_PAY_PAY_DONG_NAM_KARIN,
                 BossID.O_DO1,
                 BossID.XEN_CON_1,
                 BossID.XEN_CON_2,
                 BossID.XEN_CON_3,
                 BossID.XEN_CON_4,
                 BossID.XEN_CON_5,
                 BossID.XEN_CON_6,
                 BossID.XEN_CON_7 -> true;
            default -> false;
        };
    }

    public int getBossSoulItemId() {
        return switch ((int) this.id) {
            // Ma Hồn Hủy Diệt (2019) - Độc quyền Dàn Boss Giải Đấu Vũ Trụ 6
            case BossID.FROST_VT6,
                 BossID.AUTA_MAGETTA,
                 BossID.CABBA_VT6,
                 BossID.CAULIFLA_VT6,
                 BossID.HIT_VT6,
                 BossID.CHAMPA_VT6 -> ITEM_MA_HON_HUY_DIET;
            // Hồn Boss Tím (2018) - Dàn Boss Cấp Cao
            case BossID.COOLER,
                 BossID.BLACK_GOKU,
                 BossID.CUMBER,
                 BossID.BABY,
                 BossID.GOLDEN_FRIEZA -> ITEM_HON_BOSS_TIM;
            // Hồn Boss Lục (2017) - Boss Sự Kiện Khung Giờ & BigBoss
            case BossID.DRABURA,
                 BossID.BUI_BUI,
                 BossID.BUI_BUI_2,
                 BossID.YA_CON,
                 BossID.DRABURA_2,
                 BossID.GOKU,
                 BossID.CADIC,
                 BossID.DRABURA_3,
                 BossID.MABU_12H,
                 BossID.MABU,
                 BossID.SUPERBU,
                 BossID.GAU_TUONG_CUOP -> ITEM_HON_BOSS_LUC;
            // Hồn Boss Lam (2016) - Boss Thường, Tầm Trung & Phó Bản Bang Hội
            case BossID.KUKU,
                 BossID.MAP_DAU_DINH,
                 BossID.RAMBO,
                 BossID.SO_4,
                 BossID.SO_3,
                 BossID.SO_2,
                 BossID.SO_1,
                 BossID.TIEU_DOI_TRUONG,
                 BossID.TDST,
                 BossID.FIDE,
                 BossID.ANDROID_19,
                 BossID.DR_KORE,
                 BossID.ANDROID_13,
                 BossID.ANDROID_14,
                 BossID.ANDROID_15,
                 BossID.PIC,
                 BossID.POC,
                 BossID.KING_KONG,
                 BossID.SO_4_NM,
                 BossID.SO_3_NM,
                 BossID.SO_2_NM,
                 BossID.SO_1_NM,
                 BossID.TIEU_DOI_TRUONG_NM,
                 BossID.BUJIN,
                 BossID.KOGU,
                 BossID.ZANGYA,
                 BossID.BIDO,
                 BossID.BOJACK,
                 BossID.SUPER_BOJACK,
                 BossID.SUPER_BROLY,
                 BossID.XEN_BO_HUNG,
                 BossID.SIEU_BO_HUNG,
                 BossID.XEN_CON_1,
                 BossID.XEN_CON_2,
                 BossID.XEN_CON_3,
                 BossID.XEN_CON_4,
                 BossID.XEN_CON_5,
                 BossID.XEN_CON_6,
                 BossID.XEN_CON_7,
                 BossID.TRUNG_UY_TRANG,
                 BossID.TRUNG_UY_THEP,
                 BossID.TRUNG_UY_XANH_LO,
                 BossID.NINJA_AO_TIM,
                 BossID.ROBOT_VE_SI,
                 BossID.DR_LYCHEE,
                 BossID.HATCHIYACK -> ITEM_HON_BOSS_LAM;
            default -> -1;
        };
    }

    public static void dropBossSoulStatic(Zone targetZone, int baseX, int baseY, Player plKill, int soulItemId) {
        try {
            if (soulItemId <= 0 || targetZone == null || targetZone.map == null || plKill == null || plKill.isBot) {
                return;
            }
            Player owner = resolveRewardOwner(plKill);
            long ownerId = (owner != null) ? owner.id : plKill.id;
            int xDrop = clampDropX(targetZone, baseX + 8);
            int yDrop = targetZone.map.yPhysicInTop(xDrop, baseY - 24);
            nro.models.map.ItemMap soulMap = new nro.models.map.ItemMap(targetZone, soulItemId, 1, xDrop, yDrop, ownerId);
            soulMap.options.add(new nro.models.item.Item.ItemOption(73, 0));
            Service.gI().dropItemMap(targetZone, soulMap);
        } catch (Exception e) {
            Logger.logException(Boss.class, e);
        }
    }

    public void dropBossSoul(Zone targetZone, int baseX, int baseY, Player plKill) {
        if (this.soulDroppedForCurrentLife) {
            return;
        }
        int soulId = getBossSoulItemId();
        if (soulId <= 0 || targetZone == null || targetZone.map == null || plKill == null || plKill.isBot) {
            return;
        }
        this.soulDroppedForCurrentLife = true;
        dropBossSoulStatic(targetZone, baseX, baseY, plKill, soulId);
    }

    @Override
    public void die(Player plKill) {

        if (plKill != null
                && (this.zone.map.mapId != 140 || !MapService.gI().isMapMaBu(this.zone.map.mapId)
                || !MapService.gI().isMapDoanhTrai(this.zone.map.mapId)
                || !MapService.gI().isMapBanDoKhoBau(this.zone.map.mapId))) {
            if (!plKill.isBot) {
                reward(plKill);
                dropBossSoul(this.zone, this.location.x, this.location.y, plKill);
                if (useLegacyDefaultBossDrops()) {
                    dropThoiVangBoss(plKill);
                    dropMaterialBoss(plKill);
                }
            }
            ServerNotify.gI().notify(plKill.name + ": Đã tiêu diệt được " + this.name + " mọi người đều ngưỡng mộ.");
            this.changeStatus(BossStatus.DIE);
        } else {
            if (plKill != null && !plKill.isBot) {
                reward(plKill);
                dropBossSoul(this.zone, this.location.x, this.location.y, plKill);
                if (useLegacyDefaultBossDrops()) {
                    dropThoiVangBoss(plKill);
                    dropMaterialBoss(plKill);
                }
            }
            this.changeStatus(BossStatus.DIE);
        }
    }

    public static Player resolveRewardOwner(Player plKill) {
        if (plKill == null) {
            return null;
        }
        return (plKill.isPet && ((Pet) plKill).master != null) ? ((Pet) plKill).master : plKill;
    }

    private static int clampDropX(Zone targetZone, int x) {
        if (targetZone == null || targetZone.map == null) {
            return x;
        }
        return Math.max(24, Math.min(targetZone.map.mapWidth - 24, x));
    }

    /**
     * Rơi Thỏi vàng [457] cố định (KHÔNG bị ảnh hưởng bởi chỉ số buff thêm vàng).
     * Nếu highTierWingJackpot = true: kiểm tra riêng nội tại Cánh (Opt 251 -> 20% rơi thêm 100 Thỏi vàng),
     * tuyệt đối KHÔNG stack với chỉ số May mắn.
     */
    public static void dropGoldBarsStatic(Zone targetZone, int baseX, int baseY, Player plKill, int baseCount, boolean highTierWingJackpot) {
        try {
            if (targetZone == null || targetZone.map == null) {
                return;
            }
            Player owner = resolveRewardOwner(plKill);
            long ownerId = (owner != null) ? owner.id : -1L;
            if (baseCount > 0) {
                int xDrop = clampDropX(targetZone, baseX);
                int yDrop = targetZone.map.yPhysicInTop(xDrop, baseY - 24);
                nro.models.map.ItemMap tv = new nro.models.map.ItemMap(targetZone, 457, baseCount, xDrop, yDrop, ownerId);
                tv.options.add(new nro.models.item.Item.ItemOption(73, 0));
                Service.gI().dropItemMap(targetZone, tv);
            }
            if (highTierWingJackpot && owner != null && !owner.isPet && owner.nPoint != null
                    && owner.nPoint.tlJackpotThoiVangBoss > 0
                    && Util.isTrue(Math.min(100, owner.nPoint.tlJackpotThoiVangBoss), 100)) {
                int xJackpot = clampDropX(targetZone, baseX + 16);
                int yJackpot = targetZone.map.yPhysicInTop(xJackpot, baseY - 24);
                nro.models.map.ItemMap jackpotTV = new nro.models.map.ItemMap(targetZone, 457, 100, xJackpot, yJackpot, ownerId);
                jackpotTV.options.add(new nro.models.item.Item.ItemOption(73, 0));
                Service.gI().dropItemMap(targetZone, jackpotTV);
                Service.gI().sendThongBao(owner, "Nội tại Cánh kích hoạt: Rơi thêm 100 Thỏi vàng từ Boss!");
            }
        } catch (Exception e) {
            Logger.logException(Boss.class, e);
        }
    }

    /**
     * Rơi Đồ Thần Linh từ Boss.
     * - highTierWingBonus: cộng nội tại Cánh (+20% từ Opt 252), KHÔNG nhân May mắn.
     * - allowLuckNonStacking (riêng Cooler): có bị ảnh hưởng bởi chỉ số May mắn nhưng KHÔNG stack với Cánh
     *   (lấy Math.max giữa tỉ lệ có Cánh và tỉ lệ có May mắn).
     */
    public static void tryDropGodItemStatic(Zone targetZone, int baseX, int baseY, Player plKill, int basePercent, boolean highTierWingBonus, boolean allowLuckNonStacking) {
        try {
            if (targetZone == null || targetZone.map == null || basePercent <= 0) {
                return;
            }
            Player owner = resolveRewardOwner(plKill);
            long ownerId = (owner != null) ? owner.id : -1L;
            long basePerMillion = Math.max(0L, basePercent) * 10_000L;
            int wingBonusPct = (highTierWingBonus && owner != null && !owner.isPet && owner.nPoint != null)
                    ? Math.max(0, owner.nPoint.tlRoiDoThanBoss)
                    : 0;
            long wingChance = Math.min(1_000_000L, basePerMillion + wingBonusPct * 10_000L);
            long finalChance = wingChance;
            if (allowLuckNonStacking && owner != null) {
                long luckChance = Mob.scalePerMillion(basePerMillion, Mob.getPlayerTotalLuckPercent(owner));
                finalChance = Math.max(wingChance, luckChance);
            }
            if (Util.isTrue(finalChance, 1_000_000L)) {
                int xDrop = clampDropX(targetZone, baseX - 16);
                int yDrop = targetZone.map.yPhysicInTop(xDrop, baseY - 24);
                nro.models.map.ItemMap it = nro.models.services.ItemService.gI().randDoTLBoss(targetZone, 1, xDrop, yDrop, ownerId);
                if (it != null) {
                    Service.gI().dropItemMap(targetZone, it);
                }
            }
        } catch (Exception e) {
            Logger.logException(Boss.class, e);
        }
    }

    /**
     * Gói nguyên liệu pha lê tiêu chuẩn:
     * 30% 5x Hematite [1423] (không khóa), 10% 5x Dùi đục [1438], 10% 5x Đá mài [1439].
     */
    public static void dropStandardCrystalMaterialsStatic(Zone targetZone, int baseX, int baseY, Player plKill) {
        try {
            if (targetZone == null || targetZone.map == null || plKill == null || plKill.isBot) {
                return;
            }
            Player owner = resolveRewardOwner(plKill);
            if (owner == null) {
                return;
            }
            int[] materialIds = {1423, 1438, 1439};
            int offset = -24;
            for (int matId : materialIds) {
                int dropRate = (matId == 1423) ? 30 : 10;
                if (Util.isTrue(dropRate, 100)) {
                    int xDrop = clampDropX(targetZone, baseX + offset);
                    int yDrop = targetZone.map.yPhysicInTop(xDrop, baseY - 24);
                    nro.models.map.ItemMap matItem = new nro.models.map.ItemMap(targetZone, matId, 5, xDrop, yDrop, owner.id);
                    if (matId == 1423) {
                        matItem.options.add(new nro.models.item.Item.ItemOption(167, 0));
                        matItem.options.add(new nro.models.item.Item.ItemOption(168, 0));
                    } else {
                        matItem.options.add(new nro.models.item.Item.ItemOption(73, 0));
                    }
                    Service.gI().dropItemMap(targetZone, matItem);
                    offset += 14;
                }
            }
        } catch (Exception e) {
            Logger.logException(Boss.class, e);
        }
    }

    public static void dropLv12GearStatic(Zone targetZone, int baseX, int baseY, Player plKill) {
        try {
            if (targetZone == null || targetZone.map == null || plKill == null) {
                return;
            }
            Player owner = resolveRewardOwner(plKill);
            long ownerId = (owner != null) ? owner.id : plKill.id;
            int group = Util.nextInt(1, 100) <= 70 ? 0 : 1;
            int[][] drops = {
                {230, 231, 232, 234, 235, 236, 238, 239, 240, 242, 243, 244, 246, 247, 248, 250, 251, 252, 266, 267, 268, 270, 271, 272, 274, 275, 276},
                {254, 255, 256, 258, 259, 260, 262, 263, 264, 278, 279, 280}
            };
            int dropOptional = drops[group][Util.nextInt(0, drops[group].length - 1)];
            int xDrop = clampDropX(targetZone, baseX + 20);
            int yDrop = targetZone.map.yPhysicInTop(xDrop, baseY - 24);
            nro.models.map.ItemMap optionalItemMap = new nro.models.map.ItemMap(targetZone, dropOptional, 1, xDrop, yDrop, ownerId);
            List<nro.models.item.Item.ItemOption> optionalOps = nro.models.services.ItemService.gI().getListOptionItemShop((short) dropOptional);
            optionalOps.forEach(option -> option.param = (int) (option.param * Util.nextInt(100, 115) / 100.0));
            optionalItemMap.options.addAll(optionalOps);
            int rand = Util.nextInt(1, 100);
            int value = (rand <= 80) ? Util.nextInt(1, 3) : (rand <= 97 ? Util.nextInt(4, 5) : 6);
            optionalItemMap.options.add(new nro.models.item.Item.ItemOption(107, value));
            Service.gI().dropItemMap(targetZone, optionalItemMap);
        } catch (Exception e) {
            Logger.logException(Boss.class, e);
        }
    }

    /**
     * Nhóm 1: Kuku, Mập Đầu Đinh, Rambo, TĐST Fide, Fide Đại Ca, Android 19 & Dr.Kôrê, Android 13-14-15, Pic-Poc-King Kong
     * -> 100% 5 Thỏi vàng [457], 80% 1x NR 2 sao [15], 50% 1x Đá Hematite [1423] (không khóa).
     */
    protected void rewardGroupBasic5TV(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill == null || this.zone == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : plKill.id;
        dropGoldBarsStatic(this.zone, this.location.x, this.location.y, plKill, 5, false);
        if (Util.isTrue(80, 100)) {
            int xNr = clampDropX(this.zone, this.location.x - 14);
            int yNr = this.zone.map.yPhysicInTop(xNr, this.location.y - 24);
            nro.models.map.ItemMap nr2s = new nro.models.map.ItemMap(this.zone, 15, 1, xNr, yNr, ownerId);
            nr2s.options.add(new nro.models.item.Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, nr2s);
        }
        if (Util.isTrue(50, 100)) {
            int xHema = clampDropX(this.zone, this.location.x + 14);
            int yHema = this.zone.map.yPhysicInTop(xHema, this.location.y - 24);
            nro.models.map.ItemMap hema = new nro.models.map.ItemMap(this.zone, 1423, 1, xHema, yHema, ownerId);
            hema.options.add(new nro.models.item.Item.ItemOption(167, 0));
            hema.options.add(new nro.models.item.Item.ItemOption(168, 0));
            Service.gI().dropItemMap(this.zone, hema);
        }
    }

    /**
     * Nhóm 2: Tiểu Đội Sát Thủ Namek (mỗi thành viên)
     * -> 100% 5 Thỏi vàng [457], 80% 1x NR 2 sao [15], 50% 1x Hematite [1423], 20% 1x Dùi đục [1438], 20% 1x Đá mài [1439].
     */
    protected void rewardGroupTdstNamek(Player plKill) {
        rewardGroupBasic5TV(plKill);
        if (plKill == null || this.zone == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : plKill.id;
        if (Util.isTrue(20, 100)) {
            int xDui = clampDropX(this.zone, this.location.x - 26);
            int yDui = this.zone.map.yPhysicInTop(xDui, this.location.y - 24);
            nro.models.map.ItemMap duiDuc = new nro.models.map.ItemMap(this.zone, 1438, 1, xDui, yDui, ownerId);
            duiDuc.options.add(new nro.models.item.Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, duiDuc);
        }
        if (Util.isTrue(20, 100)) {
            int xDa = clampDropX(this.zone, this.location.x + 26);
            int yDa = this.zone.map.yPhysicInTop(xDa, this.location.y - 24);
            nro.models.map.ItemMap daMai = new nro.models.map.ItemMap(this.zone, 1439, 1, xDa, yDa, ownerId);
            daMai.options.add(new nro.models.item.Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, daMai);
        }
    }

    /**
     * Nhóm 3: Băng Bojack (mỗi thành viên), Siêu Bojack, Super Broly
     * -> 100% 10 Thỏi vàng [457], 50% trọn 1 bộ NR Bí Ngô 1-7 sao [702..708],
     *    30% 5x Hematite [1423], 10% 5x Dùi đục [1438], 10% 5x Đá mài [1439].
     */
    protected void rewardGroupBojackBroly(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill == null || this.zone == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : plKill.id;
        dropGoldBarsStatic(this.zone, this.location.x, this.location.y, plKill, 10, false);
        if (Util.isTrue(50, 100)) {
            for (int i = 0; i < 7; i++) {
                int xBall = clampDropX(this.zone, this.location.x + (i - 3) * 14);
                int yBall = this.zone.map.yPhysicInTop(xBall, this.location.y - 24);
                nro.models.map.ItemMap ball = new nro.models.map.ItemMap(this.zone, 702 + i, 1, xBall, yBall, ownerId);
                ball.options.add(new nro.models.item.Item.ItemOption(73, 0));
                Service.gI().dropItemMap(this.zone, ball);
            }
        }
        dropStandardCrystalMaterialsStatic(this.zone, this.location.x, this.location.y, plKill);
    }

    /**
     * Nhóm 4: Xên Bọ Hung cấp 1-2-3 & Siêu Bọ Hung (không tính Boss cấp cao)
     * -> 100% 20 Thỏi vàng [457], 50% 1x NR 1 sao [14],
     *    30% 5x Hematite [1423], 10% 5x Dùi đục [1438], 10% 5x Đá mài [1439].
     */
    protected void rewardGroupCell20TV(Player plKill) {
        nro.models.task.BadgesTaskService.updateCountBagesTask(plKill, nro.models.consts.ConstTaskBadges.TRUM_SAN_BOSS, 1);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill == null || this.zone == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : plKill.id;
        dropGoldBarsStatic(this.zone, this.location.x, this.location.y, plKill, 20, false);
        if (Util.isTrue(50, 100)) {
            int xNr = clampDropX(this.zone, this.location.x + 14);
            int yNr = this.zone.map.yPhysicInTop(xNr, this.location.y - 24);
            nro.models.map.ItemMap nr1s = new nro.models.map.ItemMap(this.zone, 14, 1, xNr, yNr, ownerId);
            nr1s.options.add(new nro.models.item.Item.ItemOption(73, 0));
            Service.gI().dropItemMap(this.zone, nr1s);
        }
        dropStandardCrystalMaterialsStatic(this.zone, this.location.x, this.location.y, plKill);
    }

    /**
     * Nhóm Boss cấp cao chuẩn (Black Goku, Super Black Goku, Cumber, Baby, Fide Vàng, Mabư 14h):
     * -> 100% 20 Thỏi vàng (+ Cánh 20% 100 TV), godPercent% Đồ Thần (+ Cánh 20%, không stack May mắn),
     *    tùy chọn 30% Nhẫn thời không sai lệch [992],
     *    10% trọn 1 bộ 7 viên NR Bí Ngô [702..708] cho Black Goku / Super Black Goku / Cumber / Baby / Fide Vàng (không nhân May mắn),
     *    và bộ nguyên liệu pha lê 30%/10%/10%.
     */
    protected void rewardHighTier20TVWithMaterials(Zone targetZone, int baseX, int baseY, Player plKill, int godPercent, boolean dropTimeRing30Pct, boolean updateSanBossBadge) {
        if (updateSanBossBadge) {
            nro.models.task.BadgesTaskService.updateCountBagesTask(plKill, nro.models.consts.ConstTaskBadges.TRUM_SAN_BOSS, 1);
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill == null || targetZone == null) {
            return;
        }
        Player owner = resolveRewardOwner(plKill);
        long ownerId = (owner != null) ? owner.id : plKill.id;
        dropGoldBarsStatic(targetZone, baseX, baseY, plKill, 20, true);
        tryDropGodItemStatic(targetZone, baseX, baseY, plKill, godPercent, true, false);
        if (dropTimeRing30Pct && Util.isTrue(30, 100)) {
            int xRing = clampDropX(targetZone, baseX + 26);
            int yRing = targetZone.map.yPhysicInTop(xRing, baseY - 24);
            nro.models.map.ItemMap ring = new nro.models.map.ItemMap(targetZone, 992, 1, xRing, yRing, ownerId);
            List<nro.models.item.Item.ItemOption> ops = nro.models.services.ItemService.gI().getListOptionItemShop((short) 992);
            if (ops != null && !ops.isEmpty()) {
                ring.options = ops;
            }
            Service.gI().dropItemMap(targetZone, ring);
        }
        if ((this.id == BossID.BLACK_GOKU || this.id == BossID.CUMBER
                || this.id == BossID.BABY || this.id == BossID.GOLDEN_FRIEZA)
                && Util.isTrue(10, 100)) {
            for (int i = 0; i < 7; i++) {
                int xBall = clampDropX(targetZone, baseX + (i - 3) * 14);
                int yBall = targetZone.map.yPhysicInTop(xBall, baseY - 24);
                nro.models.map.ItemMap ball = new nro.models.map.ItemMap(targetZone, 702 + i, 1, xBall, yBall, ownerId);
                ball.options.add(new nro.models.item.Item.ItemOption(73, 0));
                Service.gI().dropItemMap(targetZone, ball);
            }
        }
        dropStandardCrystalMaterialsStatic(targetZone, baseX, baseY, plKill);
        dropBossSoul(targetZone, baseX, baseY, plKill);
    }

    /**
     * Các Boss phụ Sự kiện Mabư 12h (Drabura, Bui Bui, Bui Bui 2, Yacon, Goku, Cadic, Drabura 3):
     * -> 100% 20 Thỏi vàng (+ Cánh 20% 100 TV), 5% Đồ Thần (+ Cánh 20%), 5% Đồ Cấp 12,
     *    30% 5x Hematite [1423], 10% 5x Dùi đục [1438], 10% 5x Đá mài [1439].
     */
    protected void rewardMabu12hSubBoss(Player plKill, byte mabuPoints) {
        if (plKill != null && plKill.fightMabu != null && mabuPoints > 0) {
            plKill.fightMabu.changePoint(mabuPoints);
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (plKill == null || this.zone == null) {
            return;
        }
        dropGoldBarsStatic(this.zone, this.location.x, this.location.y, plKill, 20, true);
        tryDropGodItemStatic(this.zone, this.location.x, this.location.y, plKill, 5, true, false);
        if (Util.isTrue(5, 100)) {
            dropLv12GearStatic(this.zone, this.location.x, this.location.y, plKill);
        }
        dropStandardCrystalMaterialsStatic(this.zone, this.location.x, this.location.y, plKill);
        dropBossSoul(this.zone, this.location.x, this.location.y, plKill);
    }

    public void dropMaterialBoss(Player plKill) {
        dropStandardCrystalMaterialsStatic(this.zone, this.location.x, this.location.y, plKill);
        dropBossSoul(this.zone, this.location.x, this.location.y, plKill);
    }

    public boolean isHighTierBoss() {
        return switch ((int) this.id) {
            case BossID.FROST_VT6,
                 BossID.AUTA_MAGETTA,
                 BossID.CABBA_VT6,
                 BossID.CAULIFLA_VT6,
                 BossID.HIT_VT6,
                 BossID.CHAMPA_VT6,
                 BossID.COOLER,
                 BossID.BLACK_GOKU,
                 BossID.CUMBER,
                 BossID.BABY,
                 BossID.GOLDEN_FRIEZA,
                 BossID.DRABURA,
                 BossID.BUI_BUI,
                 BossID.BUI_BUI_2,
                 BossID.YA_CON,
                 BossID.DRABURA_2,
                 BossID.GOKU,
                 BossID.CADIC,
                 BossID.DRABURA_3,
                 BossID.MABU_12H,
                 BossID.MABU,
                 BossID.SUPERBU -> true;
            default -> false;
        };
    }

    public int getGodDropChancePercent(Player plKill, int basePercent) {
        if (plKill == null || !isHighTierBoss()) {
            return basePercent;
        }
        Player owner = (plKill.isPet && ((Pet) plKill).master != null) ? ((Pet) plKill).master : plKill;
        int bonus = (owner != null && !owner.isPet && owner.nPoint != null) ? owner.nPoint.tlRoiDoThanBoss : 0;
        return Math.min(100, basePercent + Math.max(0, bonus));
    }

    protected void dropThoiVangBoss(Player plKill) {
        try {
            if (plKill != null && this.zone != null) {
                Player owner = (plKill.isPet && ((Pet) plKill).master != null) ? ((Pet) plKill).master : plKill;
                int yGround = this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24);
                nro.models.map.ItemMap thoiVangThuong = new nro.models.map.ItemMap(this.zone, 457, 1, this.location.x, yGround, owner.id);
                thoiVangThuong.options.add(new nro.models.item.Item.ItemOption(73, 0));
                Service.gI().dropItemMap(this.zone, thoiVangThuong);

                if (isHighTierBoss() && owner != null && !owner.isPet && owner.nPoint != null
                        && owner.nPoint.tlJackpotThoiVangBoss > 0
                        && Util.isTrue(Math.min(100, owner.nPoint.tlJackpotThoiVangBoss), 100)) {
                    int xJackpot = Math.max(24, Math.min(this.zone.map.mapWidth - 24, this.location.x + 15));
                    int yJackpot = this.zone.map.yPhysicInTop(xJackpot, this.location.y - 24);
                    nro.models.map.ItemMap jackpotTV = new nro.models.map.ItemMap(this.zone, 457, 100, xJackpot, yJackpot, owner.id);
                    jackpotTV.options.add(new nro.models.item.Item.ItemOption(73, 0));
                    Service.gI().dropItemMap(this.zone, jackpotTV);
                    Service.gI().sendThongBao(owner, "Nội tại Cánh kích hoạt: Rơi thêm 100 Thỏi vàng từ Boss!");
                }
            }
        } catch (Exception e) {
            Logger.logException(Boss.class, e);
        }
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public boolean chatE() {
        if (Util.canDoWithTime(lastTimeChatE, timeChatE)) {
            if (this.indexChatE == this.data[this.currentLevel].getTextE().length) {
                return true;
            }
            String textChat = this.data[this.currentLevel].getTextE()[this.indexChatE];
            int prefix = Integer.parseInt(textChat.substring(1, textChat.lastIndexOf("|")));
            textChat = textChat.substring(textChat.lastIndexOf("|") + 1);
            if (!this.chat(prefix, textChat)) {
                return false;
            }
            this.lastTimeChatE = System.currentTimeMillis();
            this.timeChatE = textChat.length() * 100;
            if (this.timeChatE > 2000) {
                this.timeChatE = 2000;
            }
            this.indexChatE++;
        }
        return false;
    }

    @Override
    public void doneChatE() {

    }

    @Override
    public void leaveMap() {
        if (this.currentLevel < this.data.length - 1) {
            this.lastZone = this.zone;
            this.changeStatus(BossStatus.RESPAWN);
        } else {
            ChangeMapService.gI().exitMap(this);
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
        }
        this.wakeupAnotherBossWhenDisappear();
    }

    @Override
    public synchronized int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            int neDon = this.nPoint.tlNeDon;
            if (plAtt != null && plAtt.nPoint != null) {
                neDon = Math.max(0, neDon - plAtt.nPoint.tlchinhxac * 10);
            }
            if (!piercing && Util.isTrue(neDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }

            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
            }
            if (!piercing) {
                damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);
            }
            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }

            return (int) Math.min(2_147_483_647L, Math.max(0L, damage));
        } else {
            return 0;
        }
    }

    @Override
    public void moveToPlayer(Player pl) {
        if (pl.location != null) {
            moveTo(pl.location.x, pl.location.y);
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(40, 60);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y + (Util.isTrue(3, 10) ? -50 : 0));
    }

    public void chat(String text) {
        Service.gI().chat(this, text);
    }

    protected boolean chat(int prefix, String textChat) {
        if (prefix == -1) {
            this.chat(textChat);
        } else if (prefix == -2) {
            if (this.zone != null) {
                Player plNear = null;
                List<Player> notBosses = this.zone.getNotBosses();
                for (int i = notBosses.size() - 1; i >= 0; i--) {
                    Player plCandidate = notBosses.get(i);
                    if (plCandidate != null && !plCandidate.isDie() && Util.getDistance(this, plCandidate) <= 600) {
                        plNear = plCandidate;
                        break;
                    }
                }
                if (plNear != null) {
                    Service.gI().chat(plNear, textChat);
                }
            }
        } else if (prefix == -3) {
            if (this.parentBoss != null && !this.parentBoss.isDie()) {
                this.parentBoss.chat(textChat);
            }
        } else if (prefix >= 0) {
            if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
                Boss boss = this.bossAppearTogether[this.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            } else if (this.parentBoss != null && this.parentBoss.bossAppearTogether != null
                    && this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] != null) {
                Boss boss = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][prefix];
                if (!boss.isDie()) {
                    boss.chat(textChat);
                }
            }
        }
        return true;
    }

    @Override
    public void wakeupAnotherBossWhenAppear() {
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            int nextLevelBoss = boss.currentLevel + 1;
            if (nextLevelBoss >= boss.data.length) {
                nextLevelBoss = 0;
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == AppearType.CALL_BY_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == AppearType.APPEAR_WITH_ANOTHER) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
                boss.changeStatus(BossStatus.RESPAWN);
            }
        }
    }

    @Override
    public void wakeupAnotherBossWhenDisappear() {
    }

    @Override
    public void autoLeaveMap() {

    }

    public void leaveMapNew() {
        if (this.data != null) {
            this.currentLevel = this.data.length;
        }
        this.changeStatus(BossStatus.LEAVE_MAP);
    }

    @Override
    public void setBom(Player plAtt) {
        try {
            if (!prepareBom) {
                prepareBom = true;
                this.nPoint.hp = 1;
                long lastTime = System.currentTimeMillis();
                //gồng tự sát
                Service.gI().chat(Boss.this, "Rồi, rồi, mày xong rồi!");
                Message msg;
                try {
                    msg = new Message(-45);
                    msg.writer().writeByte(7);
                    msg.writer().writeInt((int) Boss.this.id);
                    msg.writer().writeShort(104);
                    msg.writer().writeShort(2000);
                    Service.gI().sendMessAllPlayerInMap(Boss.this, msg);
                    msg.cleanup();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Thread.startVirtualThread(() -> {
                    try {
                        Thread.sleep(2500);
                        if (prepareBom && Boss.this.zone != null) {
                            setDie(Boss.this);
                            die(plAtt);
                            long dame = Boss.this.nPoint.hpMax;
                            for (Mob mob : Boss.this.zone.mobs) {
                                mob.injured(Boss.this, dame, true);
                            }
                            List<Player> playersMap = Boss.this.zone.getNotBosses();
                            if (!MapService.gI().isMapOffline(Boss.this.zone.map.mapId)) {
                                for (int i = playersMap.size() - 1; i >= 0; i--) {
                                    Player pl = playersMap.get(i);
                                    if (!Boss.this.equals(pl)) {
                                        pl.injured(Boss.this, dame, false, false);
                                        PlayerService.gI().sendInfoHpMpMoney(pl);
                                        Service.gI().Send_Info_NV(pl);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        setDie(Boss.this);
                        die(plAtt);
                    } finally {
                        prepareBom = false;
                    }
                });
            }
        } catch (Exception e) {
            if (prepareBom) {
                prepareBom = false;
            }
            setDie(this);
            die(plAtt);
        }
    }

}
