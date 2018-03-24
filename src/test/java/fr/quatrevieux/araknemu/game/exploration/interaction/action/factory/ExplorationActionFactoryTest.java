package fr.quatrevieux.araknemu.game.exploration.interaction.action.factory;

import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.exploration.interaction.action.*;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.network.game.in.game.action.GameActionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExplorationActionFactoryTest extends GameBaseCase {
    private ExplorationActionFactory factory;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        factory = new ExplorationActionFactory();

        dataSet.pushMaps();
    }

    @Test
    void createNotFound() {
        assertThrows(
            Exception.class,
            () -> factory.create(explorationPlayer(), new GameActionRequest(ActionType.NONE, new String[] {})),
            "No factory found for game action : NONE"
        );
    }

    @Test
    void createMove() throws Exception {
        explorationPlayer().move(100);

        Action action = factory.create(explorationPlayer(), new GameActionRequest(ActionType.MOVE, new String[] {"ebIgbf"}));

        assertTrue(action instanceof Move);
        Move move = (Move) action;

        assertEquals(4, move.path().size());
        assertEquals(100, move.path().get(0).cell());
        assertEquals(99, move.path().get(1).cell());
        assertEquals(98, move.path().get(2).cell());
        assertEquals(69, move.path().get(3).cell());
    }

    @Test
    void createAskChallenge() throws Exception {
        ExplorationPlayer other = new ExplorationPlayer(makeOtherPlayer());

        explorationPlayer().map().add(other);

        Action action = factory.create(explorationPlayer(), new GameActionRequest(ActionType.CHALLENGE, new String[] {"" + other.id()}));

        assertInstanceOf(AskChallenge.class, action);
    }

    @Test
    void createAcceptChallenge() throws Exception {
        ExplorationPlayer other = new ExplorationPlayer(makeOtherPlayer());

        explorationPlayer().map().add(other);

        Action action = factory.create(explorationPlayer(), new GameActionRequest(ActionType.ACCEPT_CHALLENGE, new String[] {"" + other.id()}));

        assertInstanceOf(AcceptChallenge.class, action);
    }

    @Test
    void createRefuseChallenge() throws Exception {
        ExplorationPlayer other = new ExplorationPlayer(makeOtherPlayer());

        explorationPlayer().map().add(other);

        Action action = factory.create(explorationPlayer(), new GameActionRequest(ActionType.REFUSE_CHALLENGE, new String[] {"" + other.id()}));

        assertInstanceOf(RefuseChallenge.class, action);
    }
}