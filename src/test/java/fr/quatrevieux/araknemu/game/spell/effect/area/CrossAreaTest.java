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

package fr.quatrevieux.araknemu.game.spell.effect.area;

import fr.quatrevieux.araknemu.data.value.EffectArea;
import fr.quatrevieux.araknemu.data.world.repository.environment.MapTemplateRepository;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.fight.map.FightMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrossAreaTest extends GameBaseCase {
    private FightMap map;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps().pushSubAreas().pushAreas();
        map = new FightMap(container.get(MapTemplateRepository.class).get(10340));
    }

    void getters() {
        CrossArea area = new CrossArea(new EffectArea(EffectArea.Type.CROSS, 2));

        assertEquals(EffectArea.Type.CROSS, area.type());
        assertEquals(2, area.size());
    }

    void resolveSize0() {
        assertEquals(
            Collections.singleton(map.get(123)),
            new CrossArea(new EffectArea(EffectArea.Type.LINE, 0)).resolve(map.get(123), map.get(123))
        );
    }

    void resolve() {
        assertCollectionEquals(
            new CrossArea(new EffectArea(EffectArea.Type.LINE, 3)).resolve(map.get(123), map.get(137)),

            map.get(123),

            map.get(109),
            map.get(95),
            map.get(81),

            map.get(138),
            map.get(153),
            map.get(168),

            map.get(137),
            map.get(151),
            map.get(165),

            map.get(108),
            map.get(93),
            map.get(78)
        );
    }
}
