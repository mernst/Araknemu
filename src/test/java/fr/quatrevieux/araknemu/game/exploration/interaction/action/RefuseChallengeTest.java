package fr.quatrevieux.araknemu.game.exploration.interaction.action;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.exploration.interaction.challenge.ChallengeInvitation;
import fr.quatrevieux.araknemu.network.game.out.game.action.GameActionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class RefuseChallengeTest extends GameBaseCase {
    private ExplorationPlayer player;
    private ExplorationPlayer other;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        player = explorationPlayer();
        other = new ExplorationPlayer(makeOtherPlayer());

        other.join(player.map());
    }

    @Test
    void noInvitation() throws SQLException, ContainerException {
        RefuseChallenge action = new RefuseChallenge(explorationPlayer(), 5);

        assertThrows(IllegalArgumentException.class, () -> action.start(), "Invalid interaction type");
    }

    @Test
    void badTarget() throws Exception {
        player.interactions().start(
            new ChallengeInvitation(other, player)
        );

        RefuseChallenge action = new RefuseChallenge(explorationPlayer(), -5);

        assertThrows(IllegalArgumentException.class, () -> action.start(), "Invalid challenge target");
    }

    @Test
    void successFromInitiator() throws Exception {
        explorationPlayer().interactions().start(new ChallengeInvitation(player, other));

        RefuseChallenge action = new RefuseChallenge(player, player.id());

        action.start();

        assertFalse(player.interactions().interacting());
        assertFalse(other.interactions().interacting());

        requestStack.assertLast(
            new GameActionResponse("", ActionType.REFUSE_CHALLENGE, "" + player.id(), new Object[] {"" + other.id()})
        );
    }

    @Test
    void successFromChallenger() throws Exception {
        other.interactions().start(new ChallengeInvitation(other, player));

        RefuseChallenge action = new RefuseChallenge(player, other.id());

        action.start();

        assertFalse(player.interactions().interacting());
        assertFalse(other.interactions().interacting());

        requestStack.assertLast(
            new GameActionResponse("", ActionType.REFUSE_CHALLENGE, "" + player.id(), new Object[] {"" + other.id()})
        );
    }
}