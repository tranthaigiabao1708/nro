package nro.models.mob_bigboss;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nro.models.database.PlayerDAO;
import nro.models.mob.BigBoss;
import nro.models.mob.Mob;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.server.Manager;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Máy đo sức mạnh: 200 tỷ HP, không giáp (0 phòng thủ / 0 giảm sát thương),
 * tự động hồi đầy 200 tỷ HP và ghi lại số sát thương gần nhất vừa gây ra.
 */
public class MayDoSucManh extends BigBoss {

    public static final long DUMMY_MAX_HP = 200_000_000_000L;
    private static final long IDLE_REPORT_AND_REFILL_MS = 1500L;

    private static final class AttackSession {
        long lastHitDamage;
        long comboTotalDamage;
        int hitCount;
        long lastHitTime;
        boolean reported;
        boolean byPet;
    }

    private final Map<Long, Long> damageMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> milestoneMap = new ConcurrentHashMap<>();
    private final Map<Long, AttackSession> sessionMap = new ConcurrentHashMap<>();
    private volatile long lastAnyHitTime = 0L;

    public MayDoSucManh(Mob mob) {
        super(mob);
        this.lvMob = 0;
        this.point.setHpFull(DUMMY_MAX_HP);
        this.point.sethp(DUMMY_MAX_HP);
    }

    @Override
    public void setDie() {
        this.status = 5;
        this.point.setHpFull(DUMMY_MAX_HP);
        this.point.sethp(DUMMY_MAX_HP);
    }

    public void moveTo(int x, int y) {
        this.location.x = x;
        this.location.y = y;
    }

    @Override
    public void update() {
        if (this.point.getHpFull() != DUMMY_MAX_HP) {
            this.point.setHpFull(DUMMY_MAX_HP);
        }
        if (this.isDie()) {
            this.status = 5;
            this.point.sethp(DUMMY_MAX_HP);
            this.sendMobHoiSinh();
        }
        if (this.effectSkill != null) {
            this.effectSkill.update();
        }
        processPostAttackReportAndRefill();
    }

    @Override
    public synchronized void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (plAtt == null || damage <= 0) {
            if (plAtt != null && damage == 0) {
                this.sendMobStillAliveAffterAttacked(0L, false);
            }
            return;
        }
        if (this.point.getHpFull() != DUMMY_MAX_HP) {
            this.point.setHpFull(DUMMY_MAX_HP);
        }

        // Không giáp: giữ nguyên 100% sát thương thuần của người chơi (kể cả khi > 200 tỷ HP)
        long rawDamage = damage;
        long currentHp = this.point.gethp();
        long nextHp = (rawDamage >= currentHp) ? 1L : (currentHp - rawDamage);
        this.point.sethp(nextHp);

        boolean isCrit = plAtt.nPoint != null && plAtt.nPoint.isCrit;
        this.sendMobStillAliveAffterAttacked(rawDamage, isCrit);

        long now = System.currentTimeMillis();
        this.lastAnyHitTime = now;

        Player owner = plAtt;
        boolean isPetAttack = false;
        if (plAtt.isPet && plAtt instanceof Pet pet && pet.master != null) {
            owner = pet.master;
            isPetAttack = true;
        }

        if (owner != null && owner.isPl()) {
            if (plAtt.satellite != null && plAtt.satellite.isDefend) {
                plAtt.satellite.isDefend = false;
            }
            AttackSession sess = sessionMap.computeIfAbsent(owner.id, k -> new AttackSession());
            synchronized (sess) {
                if (sess.reported) {
                    sess.comboTotalDamage = 0L;
                    sess.hitCount = 0;
                    sess.reported = false;
                }
                sess.lastHitDamage = rawDamage;
                if (Long.MAX_VALUE - sess.comboTotalDamage < rawDamage) {
                    sess.comboTotalDamage = Long.MAX_VALUE;
                } else {
                    sess.comboTotalDamage += rawDamage;
                }
                sess.hitCount++;
                sess.lastHitTime = now;
                sess.byPet = isPetAttack;

                owner.last_damage_maydam = rawDamage;
                owner.last_combo_damage_maydam = sess.comboTotalDamage;
            }

            if (!isPetAttack) {
                updateMilestone(owner, rawDamage);
            } else {
                PlayerDAO.savePlayerAsync(owner);
            }
        }
    }

    public void updateMilestone(Player pl, long realDamage) {
        if (pl == null || realDamage <= 0) {
            return;
        }

        long playerId = pl.id;
        long prevTotal = damageMap.getOrDefault(playerId, 0L);
        long totalDamage = (Long.MAX_VALUE - prevTotal < realDamage) ? Long.MAX_VALUE : (prevTotal + realDamage);
        damageMap.put(playerId, totalDamage);

        long prevMilestone = milestoneMap.getOrDefault(playerId, 0L);
        long currentMilestone = totalDamage / 10_000_000L;

        if (currentMilestone > prevMilestone) {
            long milestonesPassed = currentMilestone - prevMilestone;
            long pointsAdded = milestonesPassed * 10L;
            long newPoints = Math.min((long) Integer.MAX_VALUE, (long) pl.point_maydam + pointsAdded);
            pl.point_maydam = (int) newPoints;
            milestoneMap.put(playerId, currentMilestone);

            if (!Manager.isTopMaydamChanged) {
                Manager.isTopMaydamChanged = true;
            }

            Service.gI().updatePlayerPointMayDam(pl);
        }

        if (Long.MAX_VALUE - pl.total_damage_maydam < realDamage) {
            pl.total_damage_maydam = Long.MAX_VALUE;
        } else {
            pl.total_damage_maydam += realDamage;
        }
        Service.gI().updatePlayerTotalDamage(pl);
    }

    private void processPostAttackReportAndRefill() {
        long now = System.currentTimeMillis();

        for (Map.Entry<Long, AttackSession> entry : sessionMap.entrySet()) {
            long playerId = entry.getKey();
            AttackSession sess = entry.getValue();
            long lastHit = 0L;
            long comboTotal = 0L;
            int hits = 0;
            boolean byPet = false;
            boolean shouldReport = false;

            synchronized (sess) {
                if (!sess.reported && sess.hitCount > 0 && (now - sess.lastHitTime) >= IDLE_REPORT_AND_REFILL_MS) {
                    sess.reported = true;
                    lastHit = sess.lastHitDamage;
                    comboTotal = sess.comboTotalDamage;
                    hits = sess.hitCount;
                    byPet = sess.byPet;
                    shouldReport = true;
                }
            }

            if (shouldReport) {
                Player targetPl = Client.gI().getPlayer(playerId);
                if (targetPl != null && targetPl.isPl() && !targetPl.isDisposed) {
                    String prefix = byPet ? "Máy đo sức mạnh [Đệ tử]: " : "Máy đo sức mạnh: ";
                    String msg;
                    if (hits <= 1) {
                        msg = prefix + "Sát thương vừa gây ra: "
                                + Util.formatNumber(lastHit) + " (" + Util.numberToMoney(lastHit) + ")";
                    } else {
                        msg = prefix + "Sát thương gần nhất: " + Util.formatNumber(lastHit)
                                + " | Tổng đợt đánh (" + hits + " đòn): "
                                + Util.formatNumber(comboTotal) + " (" + Util.numberToMoney(comboTotal) + ")";
                    }
                    Service.gI().sendThongBao(targetPl, msg);
                    PlayerDAO.savePlayerAsync(targetPl);
                }
            }
        }

        if (this.point.gethp() < DUMMY_MAX_HP && (now - this.lastAnyHitTime) >= IDLE_REPORT_AND_REFILL_MS) {
            synchronized (this) {
                if (this.point.gethp() < DUMMY_MAX_HP && (System.currentTimeMillis() - this.lastAnyHitTime) >= IDLE_REPORT_AND_REFILL_MS) {
                    long missingHp = DUMMY_MAX_HP - this.point.gethp();
                    if (missingHp > 0) {
                        this.hoi_hp(missingHp);
                    }
                }
            }
        }
    }
}
