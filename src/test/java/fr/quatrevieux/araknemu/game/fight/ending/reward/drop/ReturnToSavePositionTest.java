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

package fr.quatrevieux.araknemu.game.fight.ending.reward.drop;

import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.FightBaseCase;
import fr.quatrevieux.araknemu.game.fight.ending.reward.RewardType;
import fr.quatrevieux.araknemu.game.fight.ending.reward.drop.action.ReturnToSavePosition;
import fr.quatrevieux.araknemu.game.fight.fighter.monster.MonsterFighter;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReturnToSavePositionTest extends FightBaseCase {
    private ReturnToSavePosition action;
    private Fight fight;
    private PlayerFighter fighter;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        action = new ReturnToSavePosition();
        fight = createPvmFight();
        fight.nextState();

        fighter = player.fighter();
    }

    void applyOnPlayerFighter() {
        player.setSavedPosition(new Position(123, 456));

        DropReward reward = new DropReward(RewardType.LOOSER, fighter, Collections.emptyList());

        action.apply(reward, fighter);

        assertEquals(new Position(123, 456), player.position());
    }

    void applyOnMonster() {
        MonsterFighter fighter = (MonsterFighter) fight.team(1).fighters().stream().findFirst().get();

        DropReward reward = new DropReward(RewardType.LOOSER, fighter, Collections.emptyList());

        action.apply(reward, fighter);
    }
}
