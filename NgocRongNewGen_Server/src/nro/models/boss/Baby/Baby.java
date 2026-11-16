package nro.models.boss.Baby;

import nro.models.consts.ConstPlayer;
import nro.models.boss.Boss;
import nro.models.boss.BossesData;
import nro.models.boss.BossID;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.utils.Util;

public class Baby extends Boss {

    public Baby() throws Exception {
        super(BossID.BABY, BossesData.BABY, BossesData.BABY_2, BossesData.BABY_3);
    }

    @Override
    public void reward(Player plKill) {
        Player owner = resolveRewardOwner(plKill);
        if (owner != null) {
            owner.bossBabyDefeatParticipationCount++;
        }
        rewardHighTier20TVWithMaterials(this.zone, this.location.x, this.location.y, plKill, 10, false, true);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack
    ) {
        if (!this.isDie()) {
            if (damage <= 0) {
                return 0;
            }
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (plAtt != null && plAtt.idNRNM != -1) {
                return 1;
            }
            if (!piercing) {
                damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);
            }
            damage = Math.max(1L, (damage * 7L) / 20L);

            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 4;
            }

            this.nPoint.subHP(damage);

            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            return Math.max(0L, damage);
        } else {
            return 0;
        }
    }

}
