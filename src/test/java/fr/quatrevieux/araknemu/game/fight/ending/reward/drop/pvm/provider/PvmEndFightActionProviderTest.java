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

import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.FightBaseCase;
import fr.quatrevieux.araknemu.game.fight.ending.EndFightResults;
import fr.quatrevieux.araknemu.game.fight.ending.reward.RewardType;
import fr.quatrevieux.araknemu.game.fight.ending.reward.drop.DropReward;
import fr.quatrevieux.araknemu.game.fight.team.MonsterGroupTeam;
import fr.quatrevieux.araknemu.game.monster.group.MonsterGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PvmEndFightActionProviderTest extends FightBaseCase {
    private PvmEndFightActionProvider provider;
    private Fight fight;
    private MonsterGroup group;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMonsterTemplates();

        provider = new PvmEndFightActionProvider();
        fight = createPvmFight();
        group = ((MonsterGroupTeam) fight.team(1)).group();
    }

    void withoutTeleport() {
        Position last = player.position();

        EndFightResults results = new EndFightResults(
            fight,
            Collections.singletonList(player.fighter()),
            new ArrayList<>(fight.team(1).fighters())
        );

        DropReward reward = new DropReward(RewardType.WINNER, player.fighter(), Collections.emptyList());

        provider.initialize(results).provide(reward);
        reward.apply();

        assertEquals(last, player.position());
    }

    void withTeleport() throws NoSuchFieldException, IllegalAccessException {
        setGroupTeleportPosition(new Position(10340, 125));

        EndFightResults results = new EndFightResults(
            fight,
            Collections.singletonList(player.fighter()),
            new ArrayList<>(fight.team(1).fighters())
        );

        DropReward reward = new DropReward(RewardType.WINNER, player.fighter(), Collections.emptyList());

        provider.initialize(results).provide(reward);
        reward.apply();

        assertEquals(new Position(10340, 125), player.position());
    }

    void monstersNotLoosers() throws NoSuchFieldException, IllegalAccessException {
        setGroupTeleportPosition(new Position(10340, 125));
        Position last = player.position();

        EndFightResults results = new EndFightResults(
            fight,
            new ArrayList<>(fight.team(1).fighters()),
            Collections.singletonList(player.fighter())
        );

        DropReward reward = new DropReward(RewardType.WINNER, player.fighter(), Collections.emptyList());

        provider.initialize(results).provide(reward);
        reward.apply();

        assertEquals(last, player.position());
    }

    private void setGroupTeleportPosition(Position position) throws NoSuchFieldException, IllegalAccessException {
        Field field = MonsterGroup.class.getDeclaredField("winFightTeleportPosition");

        field.setAccessible(true);
        field.set(group, position);
    }
}
