/*
 * This file is part of Araknemu.
 *
 * Araknemu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Araknemu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Araknemu.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2017-2019 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.fight.ending.reward.drop.pvm.provider;

import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.game.fight.ending.EndFightResults;
import fr.quatrevieux.araknemu.game.fight.ending.reward.drop.DropReward;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.monster.MonsterFighter;
import fr.quatrevieux.araknemu.game.fight.fighter.operation.FighterOperation;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Base formula for compute the Pvm experience
 */
public final class PvmXpProvider implements DropRewardProvider {
    private final double rate;

    public PvmXpProvider() {
        this(1.0);
    }

    public PvmXpProvider(double rate) {
        this.rate = rate;
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
        return (long) (results.applyToLoosers(new ExtractXp()).get() * rate);
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

    private static class Scope implements DropRewardProvider.Scope {
        private final long totalXp;
        private final double teamsLevelsRate;
        private final double teamLevelDeviationBonus;
        private final double winnersLevel;

        public Scope(long totalXp, double teamsLevelsRate, double teamLevelDeviationBonus, double winnersLevel) {
            this.totalXp = totalXp;
            this.teamsLevelsRate = teamsLevelsRate;
            this.teamLevelDeviationBonus = teamLevelDeviationBonus;
            this.winnersLevel = winnersLevel;
        }

        @Override
        public void provide(DropReward reward) {
            final long winXp = (long) (
                totalXp
                    * teamsLevelsRate
                    * teamLevelDeviationBonus
                    * (1 + ((double) reward.fighter().level() / winnersLevel))
                    * (1 + (double) reward.fighter().characteristics().get(Characteristic.WISDOM) / 100)
            );

            reward.setXp(Math.max(winXp, 0));
        }
    }

    private static class ExtractXp implements FighterOperation {
        private @NonNegative long xp;

        @Override
        public void onMonster(MonsterFighter fighter) {
            xp += fighter.reward().experience();
        }

        public @NonNegative long get() {
            return xp;
        }
    }
}
