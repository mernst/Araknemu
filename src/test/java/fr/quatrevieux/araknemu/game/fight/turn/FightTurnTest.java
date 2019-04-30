package fr.quatrevieux.araknemu.game.fight.turn;

import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.FightBaseCase;
import fr.quatrevieux.araknemu.game.fight.castable.effect.buff.Buff;
import fr.quatrevieux.araknemu.game.fight.castable.effect.buff.BuffHook;
import fr.quatrevieux.araknemu.game.fight.exception.FightException;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.turn.action.Action;
import fr.quatrevieux.araknemu.game.fight.turn.action.ActionResult;
import fr.quatrevieux.araknemu.game.fight.turn.action.factory.TurnActionsFactory;
import fr.quatrevieux.araknemu.game.fight.turn.event.TurnStarted;
import fr.quatrevieux.araknemu.game.fight.turn.event.TurnStopped;
import fr.quatrevieux.araknemu.game.listener.fight.fighter.RefreshBuffs;
import fr.quatrevieux.araknemu.game.listener.fight.fighter.RefreshStates;
import fr.quatrevieux.araknemu.game.spell.Spell;
import fr.quatrevieux.araknemu.game.spell.effect.SpellEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FightTurnTest extends FightBaseCase {
    private FightTurn turn;
    private Fight fight;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        fight = createFight();
        fight.fighters().forEach(Fighter::init);
        fight.turnList().init(teams -> Arrays.asList(player.fighter(), other.fighter()));
        fight.turnList().start();

        fight.dispatcher().add(new RefreshBuffs());
        fight.dispatcher().add(new RefreshStates());

        turn = new FightTurn(player.fighter(), fight, Duration.ofMillis(50));
    }

    @Test
    void getters() {
        assertSame(player.fighter(), turn.fighter());
        assertSame(fight, turn.fight());
        assertEquals(Duration.ofMillis(50), turn.duration());
        assertFalse(turn.active());
        assertInstanceOf(TurnActionsFactory.class, turn.actions());
    }

    @Test
    void stopNotStarted() {
        turn.stop();
        assertFalse(turn.active());
    }

    @Test
    void start() {
        AtomicReference<TurnStarted> ref = new AtomicReference<>();
        fight.dispatcher().add(TurnStarted.class, ref::set);

        turn.start();

        assertSame(turn, ref.get().turn());
        assertTrue(turn.active());
        assertSame(turn, player.fighter().turn());
    }

    @Test
    void startWillInitPoints() {
        turn.start();

        assertInstanceOf(FighterTurnPoints.class, turn.points());
        assertEquals(3, turn.points().movementPoints());
    }

    @Test
    void autoStopOnTimeout() throws InterruptedException {
        turn.start();
        assertTrue(turn.active());

        Thread.sleep(55);
        assertFalse(turn.active());
    }

    @Test
    void stop() {
        turn.start();

        AtomicReference<TurnStopped> ref = new AtomicReference<>();
        fight.dispatcher().add(TurnStopped.class, ref::set);

        turn.stop();

        assertFalse(turn.active());
        assertThrows(FightException.class, () -> player.fighter().turn());
        assertSame(other.fighter(), fight.turnList().current().get().fighter());
    }

    @Test
    void performNotActive() {
        assertThrows(FightException.class, () -> turn.perform(Mockito.mock(Action.class)));
    }

    @Test
    void performInvalidAction() {
        turn.start();

        Action action = Mockito.mock(Action.class);
        Mockito.when(action.validate()).thenReturn(false);

        assertThrows(FightException.class, () -> turn.perform(action));
    }

    @Test
    void performSuccess() {
        turn.start();

        Action action = Mockito.mock(Action.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate()).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(result.success()).thenReturn(false);

        turn.perform(action);

        Mockito.verify(action).start();
    }

    @Test
    void performDead() {
        turn.start();

        turn.fighter().life().alter(turn.fighter(), -1000);
        assertTrue(turn.fighter().dead());

        Action action = Mockito.mock(Action.class);
        Mockito.when(action.validate()).thenReturn(false);

        assertThrows(FightException.class, () -> turn.perform(action));
    }

    @Test
    void terminate() {
        turn.start();

        Action action = Mockito.mock(Action.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate()).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofSeconds(30));
        Mockito.when(result.success()).thenReturn(true);

        turn.perform(action);
        turn.terminate();

        Mockito.verify(action).end();
    }

    @Test
    void stopWillWaitForActionTermination() {
        turn.start();

        AtomicReference<TurnStopped> ref = new AtomicReference<>();
        fight.dispatcher().add(TurnStopped.class, ref::set);

        Action action = Mockito.mock(Action.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate()).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(action.duration()).thenReturn(Duration.ofSeconds(30));
        Mockito.when(result.success()).thenReturn(true);

        turn.perform(action);
        turn.stop();

        assertNull(ref.get());

        turn.terminate();
        assertSame(turn, ref.get().turn());
    }

    @Test
    void stopWillDecrementBuffRemainingTurnsAndCallEndTurn() {
        SpellEffect effect = Mockito.mock(SpellEffect.class);

        Mockito.when(effect.duration()).thenReturn(5);
        Buff buff = new Buff(effect, Mockito.mock(Spell.class), other.fighter(), player.fighter(), new BuffHook() {});

        player.fighter().buffs().add(buff);

        assertTrue(turn.start());
        turn.stop();

        assertEquals(4, buff.remainingTurns());
        assertTrue(player.fighter().buffs().stream().anyMatch(other -> other.equals(buff)));
    }

    @Test
    void stopWillRemoveExpiredBuff() {
        SpellEffect effect = Mockito.mock(SpellEffect.class);
        BuffHook hook = Mockito.mock(BuffHook.class);

        Mockito.when(effect.duration()).thenReturn(0);
        Buff buff = new Buff(effect, Mockito.mock(Spell.class), other.fighter(), player.fighter(), hook);
        Mockito.when(hook.onStartTurn(buff)).thenReturn(true);

        player.fighter().buffs().add(buff);

        assertTrue(turn.start());
        turn.stop();

        assertFalse(player.fighter().buffs().stream().anyMatch(other -> other.equals(buff)));

        Mockito.verify(hook).onBuffTerminated(buff);
    }

    @Test
    void stopWillRemoveExpiredStates() {
        player.fighter().states().push(5, 1);

        assertTrue(turn.start());
        turn.stop();

        assertFalse(player.fighter().states().has(5));
    }

    @Test
    void startWithSkipTurnBuff() {
        AtomicReference<TurnStarted> ref = new AtomicReference<>();
        fight.dispatcher().add(TurnStarted.class, ref::set);

        SpellEffect effect = Mockito.mock(SpellEffect.class);
        BuffHook hook = Mockito.mock(BuffHook.class);

        Mockito.when(effect.duration()).thenReturn(5);

        Buff buff = new Buff(effect, Mockito.mock(Spell.class), other.fighter(), player.fighter(), hook);
        Mockito.when(hook.onStartTurn(buff)).thenReturn(false);

        player.fighter().buffs().add(buff);
        player.fighter().states().push(5, 1);

        assertFalse(turn.start());

        assertFalse(turn.active());
        Mockito.verify(hook).onStartTurn(buff);
        Mockito.verify(hook).onEndTurn(buff);
        assertEquals(4, buff.remainingTurns());
        assertNull(ref.get());
        assertFalse(player.fighter().states().has(5));
    }

    @Test
    void startStopWillCallBuffHook() {
        SpellEffect effect = Mockito.mock(SpellEffect.class);
        BuffHook hook = Mockito.mock(BuffHook.class);

        Mockito.when(effect.duration()).thenReturn(5);

        Buff buff = new Buff(effect, Mockito.mock(Spell.class), other.fighter(), player.fighter(), hook);
        Mockito.when(hook.onStartTurn(buff)).thenReturn(true);

        player.fighter().buffs().add(buff);

        assertTrue(turn.start());
        Mockito.verify(hook).onStartTurn(buff);

        turn.stop();
        Mockito.verify(hook).onEndTurn(buff);
    }

    @Test
    void laterNoPendingAction() {
        Runnable runnable = Mockito.mock(Runnable.class);

        turn.later(runnable);

        Mockito.verify(runnable).run();
    }

    @Test
    void laterWithPendingAction() {
        Action action = Mockito.mock(Action.class);
        ActionResult result = Mockito.mock(ActionResult.class);

        Mockito.when(action.validate()).thenReturn(true);
        Mockito.when(action.start()).thenReturn(result);
        Mockito.when(result.success()).thenReturn(true);
        Mockito.when(action.duration()).thenReturn(Duration.ofSeconds(30));

        Runnable runnable = Mockito.mock(Runnable.class);

        turn.start();
        turn.perform(action);

        turn.later(runnable);
        Mockito.verify(runnable, Mockito.never()).run();

        turn.terminate();
        Mockito.verify(runnable).run();
    }
}
