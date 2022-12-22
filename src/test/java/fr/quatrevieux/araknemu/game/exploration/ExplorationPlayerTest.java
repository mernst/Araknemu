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

package fr.quatrevieux.araknemu.game.exploration;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.core.event.Listener;
import fr.quatrevieux.araknemu.game.exploration.event.MapChanged;
import fr.quatrevieux.araknemu.game.exploration.event.MapLeaved;
import fr.quatrevieux.araknemu.game.exploration.event.MapJoined;
import fr.quatrevieux.araknemu.game.exploration.event.OrientationChanged;
import fr.quatrevieux.araknemu.game.exploration.event.StopExploration;
import fr.quatrevieux.araknemu.game.exploration.interaction.action.BlockingAction;
import fr.quatrevieux.araknemu.game.exploration.interaction.event.PlayerMoveFinished;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMap;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.exploration.sprite.PlayerSprite;
import fr.quatrevieux.araknemu.game.player.characteristic.PlayerLife;
import fr.quatrevieux.araknemu.game.player.inventory.PlayerInventory;
import fr.quatrevieux.araknemu.game.exploration.creature.Operation;
import fr.arakne.utils.maps.constant.Direction;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.out.game.AddSprites;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ExplorationPlayerTest extends GameBaseCase {
    private ExplorationPlayer player;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps().pushSubAreas().pushAreas();
        player = new ExplorationPlayer(gamePlayer());
        session.setExploration(player);
    }

    void sprite() throws SQLException, ContainerException {
        assertEquals(
            new PlayerSprite(new ExplorationPlayer(gamePlayer())).toString(),
            player.sprite().toString()
        );
    }

    void join() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());

        AtomicReference<ExplorationMap> ref = new AtomicReference<>();
        Listener<MapJoined> listener = new Listener<MapJoined>() {
            @Override
            public void on(MapJoined event) {
                ref.set(event.map());
            }

            @Override
            public Class<MapJoined> event() {
                return MapJoined.class;
            }
        };

        assertThrows(IllegalStateException.class, () -> player.cell());
        assertNull(player.map());

        player.dispatcher().add(listener);
        player.join(map);

        assertSame(map, player.map());
        assertSame(map, ref.get());
    }

    void joinInvalidMap() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(10340);

        assertThrows(IllegalArgumentException.class, () -> player.join(map));
    }

    void joinInvalidCell() throws ContainerException {
        player.player().setPosition(new Position(10300, 1000));
        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);

        assertThrows(IllegalStateException.class, () -> player.join(map));
    }

    void move() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        AtomicReference<PlayerMoveFinished> ref = new AtomicReference<>();
        map.dispatcher().add(PlayerMoveFinished.class, ref::set);

        player.move(map.get(123), Direction.EAST);

        assertEquals(map.get(123), player.cell());
        assertEquals(123, player.position().cell());
        assertEquals(player.orientation(), Direction.EAST);

        assertEquals(123, ref.get().cell().id());
        assertSame(player, ref.get().player());
    }

    void moveInvalidCell() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        assertThrows(IllegalArgumentException.class, () -> player.move(container.get(ExplorationMapService.class).load(10340).get(123), Direction.EAST));
    }

    void moveNotOnMap() throws ContainerException {
        assertThrows(IllegalArgumentException.class, () -> player.move(container.get(ExplorationMapService.class).load(10340).get(123), Direction.EAST));
    }

    void leave() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        AtomicReference<ExplorationMap> ref = new AtomicReference<>();
        Listener<MapLeaved> listener = new Listener<MapLeaved>() {
            @Override
            public void on(MapLeaved event) {
                ref.set(event.map());
            }

            @Override
            public Class<MapLeaved> event() {
                return MapLeaved.class;
            }
        };

        player.dispatcher().add(listener);

        player.leave();

        assertFalse(map.creatures().contains(player));
        assertSame(map, ref.get());
        assertNull(player.map());
        assertThrows(IllegalStateException.class, () -> player.cell());
    }

    void changeCell() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        player.changeCell(147);

        assertEquals(147, player.position().cell());
        assertEquals(map.get(147), player.cell());

        requestStack.assertLast(
            new AddSprites(
                Collections.singleton(player.sprite())
            )
        );
    }

    void changeCellInvalidCell() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        assertThrows(IllegalArgumentException.class, () -> player.changeCell(1000));

        assertEquals(200, player.position().cell());
        assertEquals(map.get(200), player.cell());
    }

    void changeCellNotOnMap() throws ContainerException {
        assertThrows(IllegalStateException.class, () -> player.changeCell(147));
    }

    void changeMap() throws ContainerException {
        ExplorationMap map = container.get(ExplorationMapService.class).load(10300);

        AtomicReference<MapJoined> ref1 = new AtomicReference<>();
        AtomicReference<MapChanged> ref2 = new AtomicReference<>();

        player.dispatcher().add(new Listener<MapJoined>() {
            @Override
            public void on(MapJoined event) {
                ref1.set(event);
            }

            @Override
            public Class<MapJoined> event() {
                return MapJoined.class;
            }
        });
        player.dispatcher().add(MapChanged.class, ref2::set);

        try {
            player.changeMap(map, 85);
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        assertSame(map, player.map());
        assertSame(map, ref1.get().map());
        assertSame(map, ref2.get().map());
        assertEquals(map.get(85), player.cell());
        assertEquals(new Position(10300, 85), player.position());
    }

    void inventory() {
        assertInstanceOf(PlayerInventory.class, player.inventory());
    }

    void life() {
        assertInstanceOf(PlayerLife.class, player.properties().life());
    }

    void interactions() {
        assertFalse(player.interactions().busy());
    }

    void player() throws SQLException, ContainerException {
        assertSame(gamePlayer(), player.player());
    }

    void register() {
        player.register(session);
        assertSame(player, session.exploration());
    }

    void unregister() {
        session.setExploration(player);

        player.unregister(session);

        assertNull(session.exploration());
    }

    void unregisterShouldLeaveMap() throws ContainerException {
        session.setExploration(player);

        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        player.unregister(session);

        assertNull(session.exploration());
        assertFalse(map.creatures().contains(player));
    }

    void unregisterShouldStopExploration() throws ContainerException {
        session.setExploration(player);

        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        AtomicReference<StopExploration> ref = new AtomicReference<>();
        player.dispatcher().add(StopExploration.class, ref::set);

        player.unregister(session);

        assertNull(session.exploration());
        assertSame(session, ref.get().session());
    }

    void unregisterShouldStopInteractions() {
        session.setExploration(player);
        player.interactions().push(Mockito.mock(BlockingAction.class));

        player.unregister(session);

        assertNull(session.exploration());
        assertFalse(player.interactions().busy());
    }

    void restrictions() {
        assertTrue(player.restrictions().canChallenge());
        assertFalse(player.restrictions().canAttack());
        assertFalse(player.restrictions().isTomb());
        assertFalse(player.restrictions().isSlow());
    }

    void orientation() throws ContainerException {
        assertEquals(Direction.SOUTH_EAST, player.orientation());

        ExplorationMap map = container.get(ExplorationMapService.class).load(player.position().map());
        player.join(map);

        AtomicReference<OrientationChanged> ref = new AtomicReference<>();
        map.dispatcher().add(OrientationChanged.class, ref::set);

        player.setOrientation(Direction.WEST);
        assertEquals(Direction.WEST, player.orientation());

        assertSame(player, ref.get().player());
        assertSame(Direction.WEST, ref.get().orientation());
    }

    void apply() {
        Object o = new Object();
        Operation<Object> operation = Mockito.mock(Operation.class);
        Mockito.when(operation.onExplorationPlayer(player)).thenReturn(o);

        assertSame(o, player.apply(operation));

        Mockito.verify(operation).onExplorationPlayer(player);
    }
}
