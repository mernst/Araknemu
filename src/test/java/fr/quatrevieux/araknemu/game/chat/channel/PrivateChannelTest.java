package fr.quatrevieux.araknemu.game.chat.channel;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.data.living.entity.account.Account;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.account.AccountService;
import fr.quatrevieux.araknemu.game.account.GameAccount;
import fr.quatrevieux.araknemu.game.chat.ChannelType;
import fr.quatrevieux.araknemu.game.chat.ChatException;
import fr.quatrevieux.araknemu.game.chat.event.ConcealedMessage;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.network.adapter.util.DummyChannel;
import fr.quatrevieux.araknemu.network.game.GameSession;
import fr.quatrevieux.araknemu.network.game.in.chat.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrivateChannelTest extends GameBaseCase {
    private PrivateChannel channel;
    private GamePlayer other;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        dataSet.pushRaces();
        gamePlayer();

        channel = new PrivateChannel(
            container.get(PlayerService.class)
        );

        GameSession otherSession = new GameSession(new DummyChannel());
        otherSession.attach(
            new GameAccount(
                new Account(3),
                container.get(AccountService.class),
                2
            )
        );

        int playerId = dataSet.pushPlayer("Robert", 3, 2).id();

        other = container.get(PlayerService.class).load(otherSession, playerId);
        otherSession.setPlayer(other);
    }

    @Test
    void notOnline() throws SQLException, ContainerException {
        assertThrows(ChatException.class, () -> channel.send(
            gamePlayer(),
            new Message(
                ChannelType.PRIVATE,
                "Not found",
                "",
                ""
            )
        ));
    }

    @Test
    void success() throws SQLException, ContainerException, ChatException {
        List<ConcealedMessage> events = new ArrayList<>();

        gamePlayer().dispatcher().add(ConcealedMessage.class, events::add);
        other.dispatcher().add(ConcealedMessage.class, events::add);

        channel.send(
            gamePlayer(),
            new Message(
                ChannelType.PRIVATE,
                "Robert",
                "Hello",
                ""
            )
        );

        assertCount(2, events);
        assertEquals("Hello", events.get(0).message());
        assertEquals(gamePlayer(), events.get(0).sender());
        assertEquals(other, events.get(0).receiver());
    }
}