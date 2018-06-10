package fr.quatrevieux.araknemu.game.fight.fighter;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.fight.FightBaseCase;
import fr.quatrevieux.araknemu.game.fight.event.FighterReadyStateChanged;
import fr.quatrevieux.araknemu.game.fight.exception.FightException;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighterSprite;
import fr.quatrevieux.araknemu.game.fight.turn.FightTurn;
import fr.quatrevieux.araknemu.game.listener.fight.SendFightJoined;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.fight.Fight;
import fr.quatrevieux.araknemu.game.fight.FightService;
import fr.quatrevieux.araknemu.game.fight.map.FightMap;
import fr.quatrevieux.araknemu.game.fight.team.FightTeam;
import fr.quatrevieux.araknemu.game.fight.team.SimpleTeam;
import fr.quatrevieux.araknemu.game.fight.type.ChallengeType;
import fr.quatrevieux.araknemu.game.listener.fight.fighter.ApplyEndFightReward;
import fr.quatrevieux.araknemu.game.listener.fight.fighter.LeaveOnDisconnect;
import fr.quatrevieux.araknemu.game.listener.fight.fighter.SendFightLeaved;
import fr.quatrevieux.araknemu.game.listener.fight.fighter.StopFightSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PlayerFighterTest extends FightBaseCase {
    private PlayerFighter fighter;
    private FightMap map;
    private Fight fight;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushMaps();

        fight = createFight();
        fighter = new PlayerFighter(
            gamePlayer(true)
        );

        map = container.get(FightService.class).map(
            container.get(ExplorationMapService.class).load(10340)
        );
    }

    @Test
    void player() throws SQLException, ContainerException {
        assertSame(gamePlayer(), fighter.player());
    }

    @Test
    void sprite() {
        assertInstanceOf(PlayerFighterSprite.class, fighter.sprite());
    }

    @Test
    void team() {
        FightTeam team = new SimpleTeam(fighter, new ArrayList<>(), 0);
        fighter.join(team);

        assertSame(team, fighter.team());
    }

    @Test
    void fight() {
        fighter.setFight(fight);

        assertSame(fight, fighter.fight());
    }

    @Test
    void moveFirstTime() {
        fighter.move(map.get(123));

        assertSame(map.get(123), fighter.cell());
        assertSame(fighter, map.get(123).fighter().get());
    }

    @Test
    void moveWillLeaveLastCell() {
        fighter.move(map.get(123));
        fighter.move(map.get(124));

        assertSame(map.get(124), fighter.cell());
        assertSame(fighter, map.get(124).fighter().get());

        assertFalse(map.get(123).fighter().isPresent());
    }

    @Test
    void send() {
        fighter.send("test");

        requestStack.assertLast("test");
    }

    @Test
    void dispatcher() {
        assertTrue(fighter.dispatcher().has(SendFightJoined.class));
        assertTrue(fighter.dispatcher().has(ApplyEndFightReward.class));
        assertTrue(fighter.dispatcher().has(StopFightSession.class));
        assertTrue(fighter.dispatcher().has(SendFightLeaved.class));
        assertTrue(fighter.dispatcher().has(LeaveOnDisconnect.class));
    }

    @Test
    void setReady() {
        fighter.setFight(fight);

        AtomicReference<FighterReadyStateChanged> ref = new AtomicReference<>();
        fight.dispatcher().add(FighterReadyStateChanged.class, ref::set);

        fighter.setReady(true);
        assertTrue(fighter.ready());
        assertSame(fighter, ref.get().fighter());
        assertTrue(ref.get().ready());
    }

    @Test
    void unsetReady() {
        fighter.setFight(fight);
        fighter.setReady(true);

        AtomicReference<FighterReadyStateChanged> ref = new AtomicReference<>();
        fight.dispatcher().add(FighterReadyStateChanged.class, ref::set);

        fighter.setReady(false);
        assertFalse(fighter.ready());
        assertSame(fighter, ref.get().fighter());
        assertFalse(ref.get().ready());
    }

    @Test
    void turnNotSet() {
        assertThrows(FightException.class, () -> fighter.turn());
    }

    @Test
    void playAndStop() {
        FightTurn turn = new FightTurn(fighter, fight, Duration.ZERO);
        turn.start();

        fighter.play(turn);

        assertSame(turn, fighter.turn());

        fighter.stop();

        assertThrows(FightException.class, () -> fighter.turn());
    }

    @Test
    void life() throws SQLException, ContainerException {
        assertEquals(gamePlayer().life().current(), fighter.life().current());
        assertEquals(gamePlayer().life().max(), fighter.life().max());
    }

    @Test
    void lifeAfterInitIsNotSyncWithPlayer() throws SQLException, ContainerException {
        gamePlayer().life().set(100);
        assertEquals(100, fighter.life().current());
        fighter.init();

        gamePlayer().life().set(120);
        assertEquals(100, fighter.life().current());
    }

    @Test
    void dead() {
        fighter.setFight(fight);
        fighter.init();
        assertFalse(fighter.dead());

        fighter.life().alter(fighter, -10000);

        assertTrue(fighter.dead());
    }

    @Test
    void characteristics() throws SQLException, ContainerException {
        assertEquals(gamePlayer().characteristics().initiative(), fighter.characteristics().initiative());
        assertEquals(gamePlayer().characteristics().get(Characteristic.ACTION_POINT), fighter.characteristics().get(Characteristic.ACTION_POINT));
    }
}
