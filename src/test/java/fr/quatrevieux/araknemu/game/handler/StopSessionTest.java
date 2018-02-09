package fr.quatrevieux.araknemu.game.handler;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.data.living.entity.account.Account;
import fr.quatrevieux.araknemu.data.living.entity.player.Player;
import fr.quatrevieux.araknemu.data.living.repository.player.PlayerRepository;
import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.account.AccountService;
import fr.quatrevieux.araknemu.game.account.GameAccount;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.in.SessionClosed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class StopSessionTest extends GameBaseCase {
    private StopSession handler;
    private AccountService accountService;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        handler = new StopSession();
        accountService = container.get(AccountService.class);
    }

    @Test
    void withAttachedAccount() throws ContainerException {
        GameAccount account = new GameAccount(
            new Account(2),
            container.get(AccountService.class),
            1
        );
        account.attach(session);

        handler.handle(session, new SessionClosed());

        assertFalse(session.isLogged());
        assertFalse(account.isLogged());
        assertFalse(accountService.isLogged(account.id()));
        assertNull(session.account());
    }

    @Test
    void withSimplePlayer() throws SQLException, ContainerException {
        GamePlayer player = gamePlayer();

        handler.handle(session, new SessionClosed());

        assertFalse(session.isLogged());
        assertFalse(player.account().isLogged());
        assertFalse(accountService.isLogged(player.account().id()));
        assertNull(session.account());
        assertNull(session.player());
    }

    @Test
    void withExplorationPlayer() throws SQLException, ContainerException {
        ExplorationPlayer player = explorationPlayer();

        handler.handle(session, new SessionClosed());

        assertFalse(session.isLogged());
        assertFalse(player.account().isLogged());
        assertFalse(accountService.isLogged(player.account().id()));
        assertNull(session.account());
        assertNull(session.player());
        assertNull(session.exploration());
        assertFalse(player.map().players().contains(player));
    }

    @Test
    void withPlayerWillSaveThePlayer() throws SQLException, ContainerException {
        GamePlayer player = gamePlayer(true);

        player.setPosition(new Position(1234, 56));
        player.characteristics().set(Characteristic.ACTION_POINT, 12);

        int playerid = gamePlayer().id();

        handler.handle(session, new SessionClosed());

        Player savedPlayer = container.get(PlayerRepository.class).get(new Player(playerid));

        assertEquals(new Position(1234, 56), savedPlayer.position());
        assertEquals(12, savedPlayer.stats().get(Characteristic.ACTION_POINT));
    }
}