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
 * Copyright (c) 2017-2021 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.fight.ai.action.util;

import fr.quatrevieux.araknemu.game.fight.ai.AI;
import fr.quatrevieux.araknemu.game.fight.ai.action.ActionGenerator;
import fr.quatrevieux.araknemu.game.fight.ai.action.AiActionFactory;
import fr.quatrevieux.araknemu.game.fight.ai.simulation.CastSimulation;
import fr.quatrevieux.araknemu.game.fight.ai.simulation.Simulator;
import fr.quatrevieux.araknemu.game.fight.ai.util.AIHelper;
import fr.quatrevieux.araknemu.game.fight.fighter.ActiveFighter;
import fr.quatrevieux.araknemu.game.fight.turn.action.Action;

import java.util.Optional;

/**
 * Try to cast the best spell
 */
public final class CastSpell<F extends ActiveFighter> implements ActionGenerator<F> {
    private final Simulator simulator;
    private final SimulationSelector selector;

    public CastSpell(Simulator simulator, SimulationSelector selector) {
        this.simulator = simulator;
        this.selector = selector;
    }

    @Override
    public void initialize(AI<F> ai) {
    }

    @Override
    public Optional<Action> generate(AI<F> ai, AiActionFactory actions) {
        final AIHelper helper = ai.helper();

        if (!helper.canCast() || !ai.enemy().isPresent()) {
            return Optional.empty();
        }

        return helper.spells().caster(actions.castSpellValidator())
            .simulate(simulator)
            .filter(selector::valid)
            .reduce((s1, s2) -> selector.compare(s2, s1) ? s2 : s1)
            .map(simulation -> actions.cast(simulation.spell(), simulation.target()))
        ;
    }

    public interface SimulationSelector {
        /**
         * Check if the simulation is valid
         */
        public boolean valid(CastSimulation simulation);

        /**
         * Compare the two simulation
         * Return true if a is better than b
         *
         * Note: The simulations may be null
         */
        public default boolean compare(CastSimulation a, CastSimulation b) {
            return score(a) > score(b);
        }

        /**
         * Compute the score for the given simulation
         *
         * @param simulation The simulation result
         *
         * @return The score of the simulation. 0 is null
         */
        public double score(CastSimulation simulation);
    }
}
