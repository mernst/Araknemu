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

package fr.quatrevieux.araknemu.game.fight;

import fr.quatrevieux.araknemu.core.event.Listener;
import fr.quatrevieux.araknemu.core.network.util.DummyChannel;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.fight.castable.effect.EffectsHandler;
import fr.quatrevieux.araknemu.game.fight.event.FightCancelled;
import fr.quatrevieux.araknemu.game.fight.event.FightStarted;
import fr.quatrevieux.araknemu.game.fight.event.FightStopped;
import fr.quatrevieux.araknemu.game.fight.exception.InvalidFightStateException;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.event.FighterInitialized;
import fr.quatrevieux.araknemu.game.fight.fighter.invocation.InvocationFighter;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;
import fr.quatrevieux.araknemu.game.fight.map.FightMap;
import fr.quatrevieux.araknemu.game.fight.module.FightModule;
import fr.quatrevieux.araknemu.game.fight.spectator.Spectator;
import fr.quatrevieux.araknemu.game.fight.spectator.SpectatorFactory;
import fr.quatrevieux.araknemu.game.fight.spectator.Spectators;
import fr.quatrevieux.araknemu.game.fight.state.*;
import fr.quatrevieux.araknemu.game.fight.team.FightTeam;
import fr.quatrevieux.araknemu.game.fight.team.SimpleTeam;
import fr.quatrevieux.araknemu.game.fight.turn.action.factory.FightActionsFactoryRegistry;
import fr.quatrevieux.araknemu.game.fight.turn.order.AlternateTeamFighterOrder;
import fr.quatrevieux.araknemu.game.fight.type.ChallengeType;
import fr.quatrevieux.araknemu.game.monster.MonsterService;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.util.ExecutorFactory;
import io.github.artsok.RepeatedIfExceptionsTest;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FightTest extends GameBaseCase {
    private Fight fight;
    private FightMap map;
    private List<FightTeam.Factory> teams;
    private Logger logger;
    private ScheduledExecutorService executor;

    private PlayerFighter fighter1, fighter2;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps().pushSubAreas().pushAreas();

        final GamePlayer me = gamePlayer(true);
        final GamePlayer enemy = makeOtherPlayer();

        fight = new Fight(
            5,
            new ChallengeType(configuration.fight()),
            map = container.get(FightService.class).map(container.get(ExplorationMapService.class).load(10340)),
            teams = new ArrayList<>(Arrays.asList(
                fight -> new SimpleTeam(fight, fighter1 = new PlayerFighter(me), Arrays.asList(map.get(123)), 0),
                fight -> new SimpleTeam(fight, fighter2 = new PlayerFighter(enemy), Arrays.asList(map.get(321)), 1)
            )),
            new StatesFlow(
                new NullState(),
                new InitialiseState(),
                new PlacementState(),
                new ActiveState(),
                new FinishState()
            ),
            logger = Mockito.mock(Logger.class),
            executor = ExecutorFactory.createSingleThread(),
            container.get(FightActionsFactoryRegistry.class)
        );
    }

    @Override
    @AfterEach
    public void tearDown() throws fr.quatrevieux.araknemu.core.di.ContainerException {
        executor.shutdownNow();
        fight.cancel(true);

        super.tearDown();
    }

    @Test
    void getters() {
        assertEquals(5, fight.id());
        assertSame(map, fight.map());
        assertInstanceOf(NullState.class, fight.state());
        assertCount(2, fight.teams());
        assertContainsOnly(SimpleTeam.class, fight.teams());
        assertInstanceOf(ChallengeType.class, fight.type());
        assertInstanceOf(EffectsHandler.class, fight.effects());
        assertFalse(fight.active());
        assertTrue(fight.alive());
        assertInstanceOf(Spectators.class, fight.spectators());
        assertSame(container.get(FightActionsFactoryRegistry.class), fight.actions());
    }

    @Test
    void fighters() {
        assertCount(0, fight.fighters());

        new PlacementState().start(fight);

        assertEquals(Arrays.asList(fighter1, fighter2), fight.fighters());

        fight.start(new AlternateTeamFighterOrder());
        assertEquals(Arrays.asList(fighter1, fighter2), fight.fighters());
    }

    @Test
    void stateBadState() {
        assertThrows(InvalidFightStateException.class, () -> fight.state(PlacementState.class));
    }

    @Test
    void stateWithType() {
        assertInstanceOf(NullState.class, fight.state(NullState.class));

        fight.nextState();

        assertInstanceOf(PlacementState.class, fight.state(PlacementState.class));
    }

    @Test
    void teamByNumber() {
        assertInstanceOf(SimpleTeam.class, fight.team(0));
        assertSame(fighter1, fight.team(0).leader());
        assertInstanceOf(SimpleTeam.class, fight.team(1));
        assertSame(fighter2, fight.team(1).leader());
    }

    @Test
    void send() {
        fight.send("test");

        requestStack.assertLast("test");
    }

    @Test
    void sendWithSpectator() throws SQLException {
        GameSession otherSession = makeSimpleExplorationSession(5);

        Spectator spectator = container.get(SpectatorFactory.class).create(otherSession.player(), fight);

        fight.spectators().add(spectator);
        fight.send("test");

        requestStack.assertLast("test");
        new SendingRequestStack(DummyChannel.class.cast(otherSession.channel())).assertLast("test");
    }

    @RepeatedIfExceptionsTest
    void schedule() throws InterruptedException {
        AtomicBoolean ab = new AtomicBoolean(false);

        fight.schedule(() -> ab.set(true), Duration.ofMillis(10));

        assertFalse(ab.get());

        Thread.sleep(15);
        assertTrue(ab.get());
    }

    @RepeatedIfExceptionsTest
    void execute() throws InterruptedException {
        ExecutorFactory.disableDirectExecution();
        AtomicBoolean ab = new AtomicBoolean(false);

        fight.execute(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ab.set(true);
        });

        assertFalse(ab.get());

        Thread.sleep(15);
        assertTrue(ab.get());
    }

    @RepeatedIfExceptionsTest
    void executeWithExceptionShouldBeLogged() throws InterruptedException {
        RuntimeException raisedException = new RuntimeException("my error");

        fight.execute(() -> { throw raisedException; });

        Mockito.verify(logger).error("Error on fight executor : my error", raisedException);
    }

    @RepeatedIfExceptionsTest
    void scheduleWithExceptionShouldBeLogged() throws InterruptedException {
        RuntimeException raisedException = new RuntimeException("my error");

        fight.schedule(() -> { throw raisedException; }, Duration.ZERO);

        Thread.sleep(10);

        Mockito.verify(logger).error("Error on fight executor : my error", raisedException);
    }

    @Test
    void executeOnDeadFightShouldBeIgnored() {
        fight.cancel();

        assertThrows(IllegalStateException.class, () -> fight.execute(() -> {}));
        assertThrows(IllegalStateException.class, () -> fight.schedule(() -> {}, Duration.ZERO));
    }

    @Test
    void scheduleOnFightDeadShouldBeIgnored() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        fight.schedule(() -> executed.set(true), Duration.ofMillis(10));
        fight.cancel();

        Thread.sleep(100);

        Mockito.verify(logger).warn(Mockito.matches("Cannot run task .* on dead fight"));
        assertFalse(executed.get());
    }

    @Test
    void destroy() {
        fight.destroy();

        assertCount(0, fight.teams());
        assertFalse(fight.alive());
    }

    @Test
    void destroyShouldClearSpectators() throws SQLException {
        Spectator spectator = new Spectator(gamePlayer(), fight);
        fight.spectators().add(spectator);
        requestStack.clear();

        fight.destroy();
        fight.spectators().send("foo");
        requestStack.assertEmpty();
    }

    @RepeatedIfExceptionsTest
    void startStop() throws InterruptedException {
        AtomicReference<FightStarted> ref = new AtomicReference<>();
        AtomicReference<FightStopped> ref2 = new AtomicReference<>();

        fight.dispatcher().add(new Listener<FightStarted>() {
            @Override
            public void on(FightStarted event) {
                ref.set(event);
            }

            @Override
            public Class<FightStarted> event() {
                return FightStarted.class;
            }
        });

        fight.dispatcher().add(new Listener<FightStopped>() {
            @Override
            public void on(FightStopped event) {
                ref2.set(event);
            }

            @Override
            public Class<FightStopped> event() {
                return FightStopped.class;
            }
        });

        // Call join fight on fighters
        new PlacementState().start(fight);

        fight.start(new AlternateTeamFighterOrder());
        assertTrue(fight.active());
        assertNotNull(ref.get());
        assertSame(fight, ref.get().fight());

        Thread.sleep(205);

        assertTrue(fight.turnList().current().isPresent());

        fight.stop();
        assertSame(fight, ref2.get().fight());
        assertFalse(fight.active());
        assertFalse(fight.turnList().current().isPresent());

        assertBetween(205, 220, (int) fight.duration());
    }

    @Test
    void startShouldInitFighterAndOrderTurnList() {
        // Perform join fight on fighters
        new PlacementState().start(fight);

        Set<Fighter> initializedFighters = new HashSet<>();

        fight.dispatcher().add(new Listener<FighterInitialized>() {
            @Override
            public void on(FighterInitialized event) {
                initializedFighters.add(event.fighter());
            }

            @Override
            public Class<FighterInitialized> event() {
                return FighterInitialized.class;
            }
        });

        fight.start(t -> Arrays.asList(fighter2, fighter1));

        assertEquals(Arrays.asList(fighter2, fighter1), fight.turnList().fighters());

        assertCount(2, initializedFighters);
        assertContainsAll(initializedFighters, fighter1, fighter2);
    }

    @Test
    void cancelActive() {
        // Call join fight on fighters
        new PlacementState().start(fight);

        fight.start(new AlternateTeamFighterOrder());

        assertThrows(IllegalStateException.class, () -> fight.cancel());
        assertTrue(fight.alive());
    }

    @Test
    void cancel() {
        AtomicReference<FightCancelled> ref = new AtomicReference<>();
        fight.dispatcher().add(FightCancelled.class, ref::set);

        fight.cancel();

        assertSame(fight, ref.get().fight());
        assertCount(0, fight.teams());
        assertCount(0, fight.fighters());
        assertFalse(fight.alive());
    }

    @Test
    void cancelActiveForce() {
        AtomicReference<FightCancelled> ref = new AtomicReference<>();
        fight.dispatcher().add(FightCancelled.class, ref::set);

        // Call join fight on fighters
        new PlacementState().start(fight);

        fight.start(new AlternateTeamFighterOrder());

        fight.cancel(true);

        assertSame(fight, ref.get().fight());
        assertCount(0, fight.teams());
        assertFalse(fight.alive());
    }

    @Test
    void register() {
        FightModule module = Mockito.mock(FightModule.class);

        Mockito.when(module.listeners()).thenReturn(new Listener[0]);

        fight.register(module);

        Mockito.verify(module).effects(fight.effects());
        Mockito.verify(module).listeners();
    }

    @Test
    void nextStateWillNotifyModules() {
        fight.nextState();

        FightModule module = Mockito.mock(FightModule.class);
        Mockito.when(module.listeners()).thenReturn(new Listener[0]);
        fight.register(module);

        fight.nextState();

        Mockito.verify(module).stateChanged(fight.state());
    }

    @Test
    void attach() {
        Object attachment = new Object();
        fight.attach(attachment);

        assertSame(attachment, fight.attachment(Object.class));
    }

    @Test
    void dispatchToAll() throws SQLException {
        class Foo {}
        AtomicInteger ai = new AtomicInteger();

        Spectator spectator = new Spectator(makeSimpleGamePlayer(10), fight);
        spectator.join();

        new PlacementState().start(fight); // Init cell to ensure that Fighter#isOnFight() is true

        fighter1.dispatcher().add(Foo.class, foo -> ai.incrementAndGet());
        fighter2.dispatcher().add(Foo.class, foo -> ai.incrementAndGet());
        spectator.dispatcher().add(Foo.class, foo -> ai.incrementAndGet());

        fight.dispatchToAll(new Foo());

        assertEquals(3, ai.get());
    }

    @Test
    void dispatchToAllWithInitializedTurnList() throws SQLException {
        dataSet
            .pushMonsterSpellsInvocations()
            .pushMonsterTemplateInvocations()
        ;

        class Foo {}
        AtomicInteger ai = new AtomicInteger();

        Spectator spectator = new Spectator(makeSimpleGamePlayer(10), fight);
        spectator.join();

        new PlacementState().start(fight); // Init cell to ensure that Fighter#isOnFight() is true
        fight.start(new AlternateTeamFighterOrder());

        InvocationFighter invoc = new InvocationFighter(-5, container.get(MonsterService.class).load(36).get(1), fighter1.team(), fighter1);
        fight.turnList().add(invoc);
        invoc.joinFight(fight, fight.map().get(122));
        invoc.init();

        InvocationFighter notInFight = new InvocationFighter(-6, container.get(MonsterService.class).load(36).get(1), fighter1.team(), fighter1);
        fight.turnList().add(notInFight);

        fighter1.dispatcher().add(Foo.class, foo -> ai.incrementAndGet());
        fighter2.dispatcher().add(Foo.class, foo -> ai.incrementAndGet());
        invoc.dispatcher().add(Foo.class, foo -> ai.incrementAndGet());
        spectator.dispatcher().add(Foo.class, foo -> ai.incrementAndGet());

        fight.dispatchToAll(new Foo());

        assertEquals(4, ai.get());
    }

    @Test
    void turnListNotStartedShouldFailed() {
        assertThrows(IllegalStateException.class, fight::turnList);
    }

    @Test
    void stopNotStartedShouldFailed() {
        assertThrows(IllegalStateException.class, fight::stop);
    }
}
