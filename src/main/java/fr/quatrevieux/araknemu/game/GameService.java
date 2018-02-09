package fr.quatrevieux.araknemu.game;

import fr.quatrevieux.araknemu.core.BootException;
import fr.quatrevieux.araknemu.core.Service;
import fr.quatrevieux.araknemu.game.connector.RealmConnector;
import fr.quatrevieux.araknemu.network.adapter.Server;
import fr.quatrevieux.araknemu.realm.host.GameHost;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * Service for game server
 */
final public class GameService implements Service {
    final private GameConfiguration configuration;
    final private RealmConnector connector;
    final private Server server;
    final private Collection<PreloadableService> preloadables;
    private final Logger logger;

    public GameService(GameConfiguration configuration, RealmConnector connector, Server server, Logger logger, Collection<PreloadableService> preloadables) {
        this.configuration = configuration;
        this.connector = connector;
        this.server = server;
        this.preloadables = preloadables;
        this.logger = logger;
    }

    @Override
    public void boot() throws BootException {
        long startTime = System.currentTimeMillis();
        logger.info(
            "Starting game server {} at {}:{}",
            configuration.id(),
            configuration.ip(),
            configuration.port()
        );

        for (PreloadableService service : preloadables) {
            service.preload(logger);
        }

        try {
            server.start();
        } catch (Exception e) {
            throw new BootException(e);
        }

        connector.declare(
            configuration.id(),
            configuration.port(),
            configuration.ip()
        );

        connector.updateState(
            configuration.id(),
            GameHost.State.ONLINE,
            true
        );

        logger.info("Game server {} started in {}ms", configuration.id(), System.currentTimeMillis() - startTime);
    }

    @Override
    public void shutdown() {
        logger.info("Stopping game server {}", configuration.id());

        connector.updateState(
            configuration.id(),
            GameHost.State.OFFLINE,
            false
        );

        try {
            server.stop();
        } catch (Exception e) {
            logger.error("Error during stopping socket server", e);
        }

        logger.info("Game server {} stopped", configuration.id());
    }
}