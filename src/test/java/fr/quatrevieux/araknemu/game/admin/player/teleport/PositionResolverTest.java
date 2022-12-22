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
 * Copyright (c) 2017-2020 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.admin.player.teleport;

import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.map.GeolocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionResolverTest extends GameBaseCase {
    private PositionResolver resolver;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dataSet
            .pushSubAreas()
            .pushAreas()
            .pushMaps()
        ;

        resolver = new PositionResolver(gamePlayer(), container.get(GeolocationService.class));
    }

    void resolve() throws SQLException {
        explorationPlayer();
        Target target = new Target(explorationPlayer().map(), 123);

        resolver.resolve("3;6", target);

        assertEquals(123, target.cell());
        assertEquals(container.get(ExplorationMapService.class).load(10340), target.map());
    }

    void resolveWithComma() throws SQLException {
        explorationPlayer();
        Target target = new Target(explorationPlayer().map(), 123);

        resolver.resolve("3,6", target);

        assertEquals(123, target.cell());
        assertEquals(container.get(ExplorationMapService.class).load(10340), target.map());
    }

    void resolveNotExploring() throws SQLException {
        Target target = new Target(explorationPlayer().map(), 123);

        resolver.resolve("3,6", target);

        assertEquals(123, target.cell());
        assertEquals(container.get(ExplorationMapService.class).load(10340), target.map());
    }

    void resolveNotFound() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("0;0", new Target(null, 0)));
        explorationPlayer();
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("0;0", new Target(null, 0)));
    }
}
