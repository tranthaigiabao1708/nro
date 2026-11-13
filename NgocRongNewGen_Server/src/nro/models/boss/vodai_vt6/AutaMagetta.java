package nro.models.boss.vodai_vt6;

import java.text.Normalizer;
import java.util.Locale;
import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.player.Player;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class AutaMagetta extends Universe6Boss {

    private static final long REAL_HP = 32_000_000_000L;
    private static final long FLAT_ARMOR_NORMAL = 45_000_000L;
    private static final long FLAT_ARMOR_INSULTED = 20_000_000L;
    private static final int ARMOR_PCT_NORMAL = 78;
    private static final int ARMOR_PCT_INSULTED = 68;

    private static final String[] INSULT_KEYWORDS = {
        "do sat vun",
        "sat vun",
        "noi com",
        "cuc sat",
        "phe lieu",
        "ri set",
        "do ngoc",
        "xau xi",
        "thung phuy"
    };

    private long lastMagmaEruptTime;
    private volatile long insultedVulnerableUntil;
    private volatile long lastInsultTriggerTime;

    public AutaMagetta() throws Exception {
        super(BossID.AUTA_MAGETTA, BossesData.AUTA_MAGETTA);
    }

    public boolean isInsultedVulnerable() {
        return System.currentTimeMillis() < this.insultedVulnerableUntil;
    }

    @Override
    protected long getRealHpForCurrentStage() {
        return REAL_HP;
    }

    @Override
    protected long getFlatArmorForCurrentStage() {
        return isInsultedVulnerable() ? FLAT_ARMOR_INSULTED : FLAT_ARMOR_NORMAL;
    }

    @Override
    protected int getArmorPercentForCurrentStage() {
        return isInsultedVulnerable() ? ARMOR_PCT_INSULTED : ARMOR_PCT_NORMAL;
    }

    @Override
    protected int getBonusPercentMaxHpPerHit() {
        return 8;
    }

    @Override
    protected void configureStageStats() {
        this.nPoint.khangTDHS = true;
        this.nPoint.tlKhangKhongChe = 100;
        this.insultedVulnerableUntil = 0L;
        this.lastInsultTriggerTime = 0L;
    }

    @Override
    protected long alterDamageAfterArmor(Player plAtt, long damageAfterArmor) {
        if (isInsultedVulnerable()) {
            return damageAfterArmor;
        }
        // Vỏ hợp kim siêu cứng: đòn không chí mạng bị giảm thêm 35% sát thương
        if (plAtt != null && plAtt.nPoint != null && !plAtt.nPoint.isCrit) {
            return Math.max(1L, damageAfterArmor * 65L / 100L);
        }
        return damageAfterArmor;
    }

    @Override
    protected void onSpecialTick() {
        if (this.zone == null || !Util.canDoWithTime(this.lastMagmaEruptTime, 10_000)) {
            return;
        }
        this.lastMagmaEruptTime = System.currentTimeMillis();
        boolean hitAnyone = false;
        for (Player pl : this.zone.getNotBosses()) {
            if (pl == null || pl.isDie() || pl.nPoint == null || Util.getDistance(this, pl) > 420) {
                continue;
            }
            hitAnyone = true;
            if (pl.itemTime != null && pl.itemTime.isUseThuocGiaiVt6) {
                continue;
            }
            long mpBurn = pl.nPoint.mpMax * 15L / 100L;
            pl.nPoint.subMP(mpBurn);
            applyPercentMaxHpStrike(pl, 8, true, false);
            PlayerService.gI().sendInfoHpMp(pl);
            if (pl.isPl()) {
                Service.gI().sendThongBao(pl, "Hơi nóng Dung Nham của Magetta đốt cháy 15% KI và 8% HP!");
            }
        }
        if (hitAnyone) {
            this.chat("Chugga chugga! Phun trào dung nham!");
        }
    }

    public static void checkPlayerInsult(Player speaker, String text) {
        if (speaker == null || speaker.zone == null || speaker.zone.map == null
                || (speaker.zone.map.mapId != 174 && speaker.zone.map.mapId != 145)
                || text == null || text.isEmpty()) {
            return;
        }
        String normalized = normalizeAscii(text);
        boolean matched = false;
        for (String kw : INSULT_KEYWORDS) {
            if (normalized.contains(kw)) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            return;
        }
        long now = System.currentTimeMillis();
        for (Player bossPl : speaker.zone.getBosses()) {
            if (bossPl instanceof AutaMagetta magetta && !magetta.isDie()) {
                if (now - magetta.lastInsultTriggerTime >= 14_000L) {
                    magetta.lastInsultTriggerTime = now;
                    magetta.insultedVulnerableUntil = now + 8_000L;
                    magetta.chat("Hức... ngươi dám chê bai ngoại hình tộc Metalman sao?!");
                    notifyZonePlayers(speaker.zone,
                            "Auta Magetta bị tổn thương tâm lý: Giáp hợp kim sụt giảm mạnh trong 8 giây!");
                }
            } else if (bossPl instanceof VoDaiVt6Boss arenaBoss && !arenaBoss.isDie() && arenaBoss.isMagettaRound()) {
                arenaBoss.triggerInsultVulnerability();
            }
        }
    }

    private static String normalizeAscii(String input) {
        String lower = input.toLowerCase(Locale.ROOT).replace('đ', 'd');
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "");
    }

    @Override
    public void reward(Player plKill) {
        dropUniverse6Rewards(plKill, 30, 15, 40, false, false, false);
    }
}
