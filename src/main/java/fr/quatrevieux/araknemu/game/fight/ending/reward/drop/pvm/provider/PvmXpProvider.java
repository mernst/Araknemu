package fr.quatrevieux.araknemu.game.fight.ending.reward.drop.pvm.provider;

import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.game.fight.ending.EndFightResults;
import fr.quatrevieux.araknemu.game.fight.ending.reward.drop.DropReward;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.operation.FighterOperation;
import fr.quatrevieux.araknemu.game.fight.fighter.monster.MonsterFighter;

/**
 * Base formula for compute the Pvm experience
 */
final public class PvmXpProvider implements DropRewardProvider {
    static private class Scope implements DropRewardProvider.Scope {
        final private long totalXp;
        final private double teamsLevelsRate;
        final private double teamLevelDeviationBonus;
        final private double winnersLevel;

        public Scope(long totalXp, double teamsLevelsRate, double teamLevelDeviationBonus, double winnersLevel) {
            this.totalXp = totalXp;
            this.teamsLevelsRate = teamsLevelsRate;
            this.teamLevelDeviationBonus = teamLevelDeviationBonus;
            this.winnersLevel = winnersLevel;
        }

        @Override
        public void provide(DropReward reward) {
            long winXp = (long) (
                totalXp
                * teamsLevelsRate
                * teamLevelDeviationBonus
                * (1 + ((double) reward.fighter().level() / winnersLevel))
                * (1 + (double) reward.fighter().characteristics().get(Characteristic.WISDOM) / 100)
            );

            reward.setXp(winXp);
        }
    }

    static private class ExtractXp implements FighterOperation {
        private long xp;

        @Override
        public void onMonster(MonsterFighter fighter) {
            xp += fighter.reward().experience();
        }

        public long get() {
            return xp;
        }
    }

    @Override
    public DropRewardProvider.Scope initialize(EndFightResults results) {
        return new Scope(
            totalXp(results),
            teamsLevelsRate(results),
            teamLevelDeviationBonus(results),
            results.winners().stream().mapToInt(Fighter::level).sum()
        );
    }

    private long totalXp(EndFightResults results) {
        return results.applyToLoosers(new ExtractXp()).get();
    }

    private double teamLevelDeviationBonus(EndFightResults results) {
        final int level = results.winners().stream().mapToInt(Fighter::level).max().orElse(0) / 3;

        int number = 0;

        for (Fighter winner : results.winners()) {
            if (winner.level() > level) {
                ++number;
            }
        }

        switch (number) {
            case 1:
                return 1.0;

            case 2:
                return 1.1;

            case 3:
                return 1.3;

            case 4:
                return 2.2;

            case 5:
                return 2.5;

            case 6:
                return 2.8;

            case 7:
                return 3.1;

            default:
                return 3.5;
        }
    }

    private double teamsLevelsRate(EndFightResults results) {
        final double winnersLevel = results.winners().stream().mapToInt(Fighter::level).sum();
        final double loosersLevel = results.loosers().stream().mapToInt(Fighter::level).sum();

        return Math.min(
            1.3,
            1 + loosersLevel / winnersLevel
        );
    }
}