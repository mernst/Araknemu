package fr.quatrevieux.araknemu.game;

import fr.quatrevieux.araknemu.Araknemu;
import fr.quatrevieux.araknemu.core.di.ContainerConfigurator;
import fr.quatrevieux.araknemu.core.di.ContainerModule;
import fr.quatrevieux.araknemu.data.living.constraint.player.PlayerConstraints;
import fr.quatrevieux.araknemu.data.living.repository.account.AccountRepository;
import fr.quatrevieux.araknemu.data.living.repository.environment.SubAreaRepository;
import fr.quatrevieux.araknemu.data.living.repository.player.PlayerItemRepository;
import fr.quatrevieux.araknemu.data.living.repository.player.PlayerRepository;
import fr.quatrevieux.araknemu.data.world.repository.character.PlayerRaceRepository;
import fr.quatrevieux.araknemu.data.world.repository.environment.MapTemplateRepository;
import fr.quatrevieux.araknemu.data.world.repository.environment.MapTriggerRepository;
import fr.quatrevieux.araknemu.data.world.repository.item.ItemTemplateRepository;
import fr.quatrevieux.araknemu.game.account.AccountService;
import fr.quatrevieux.araknemu.game.account.CharactersService;
import fr.quatrevieux.araknemu.game.account.TokenService;
import fr.quatrevieux.araknemu.game.account.generator.CamelizeName;
import fr.quatrevieux.araknemu.game.account.generator.NameCheckerGenerator;
import fr.quatrevieux.araknemu.game.account.generator.NameGenerator;
import fr.quatrevieux.araknemu.game.account.generator.SimpleNameGenerator;
import fr.quatrevieux.araknemu.game.admin.AdminService;
import fr.quatrevieux.araknemu.game.admin.account.AccountContextResolver;
import fr.quatrevieux.araknemu.game.admin.debug.DebugContextResolver;
import fr.quatrevieux.araknemu.game.admin.player.PlayerContextResolver;
import fr.quatrevieux.araknemu.game.chat.ChannelType;
import fr.quatrevieux.araknemu.game.chat.ChatService;
import fr.quatrevieux.araknemu.game.chat.channel.*;
import fr.quatrevieux.araknemu.game.connector.ConnectorService;
import fr.quatrevieux.araknemu.game.connector.RealmConnector;
import fr.quatrevieux.araknemu.game.event.DefaultListenerAggregate;
import fr.quatrevieux.araknemu.game.event.ListenerAggregate;
import fr.quatrevieux.araknemu.game.exploration.ExplorationService;
import fr.quatrevieux.araknemu.game.exploration.action.factory.ExplorationActionFactory;
import fr.quatrevieux.araknemu.game.exploration.area.AreaService;
import fr.quatrevieux.araknemu.game.exploration.map.ExplorationMapService;
import fr.quatrevieux.araknemu.game.handler.loader.*;
import fr.quatrevieux.araknemu.game.item.ItemService;
import fr.quatrevieux.araknemu.game.item.factory.*;
import fr.quatrevieux.araknemu.game.player.PlayerService;
import fr.quatrevieux.araknemu.game.player.inventory.InventoryService;
import fr.quatrevieux.araknemu.game.world.item.effect.mapping.EffectToCharacteristicMapping;
import fr.quatrevieux.araknemu.game.world.item.effect.mapping.EffectToSpecialMapping;
import fr.quatrevieux.araknemu.game.world.item.effect.mapping.EffectToWeaponMapping;
import fr.quatrevieux.araknemu.network.adapter.Server;
import fr.quatrevieux.araknemu.network.adapter.SessionHandler;
import fr.quatrevieux.araknemu.network.adapter.netty.NettyServer;
import fr.quatrevieux.araknemu.network.adapter.util.LoggingSessionHandler;
import fr.quatrevieux.araknemu.network.game.GameSessionHandler;
import fr.quatrevieux.araknemu.network.game.in.GameParserLoader;
import fr.quatrevieux.araknemu.network.in.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Module for game service
 */
final public class GameModule implements ContainerModule {
    final private Araknemu app;

    public GameModule(Araknemu app) {
        this.app = app;
    }

    @Override
    public void configure(ContainerConfigurator configurator) {
        configurator.factory(
            Logger.class,
            container -> LoggerFactory.getLogger(GameService.class)
        );

        configurator.factory(
            GameService.class,
            container -> new GameService(
                container.get(GameConfiguration.class),
                container.get(RealmConnector.class),
                container.get(Server.class),
                container.get(Logger.class),
                Arrays.asList(
                    container.get(AreaService.class),
                    container.get(ExplorationMapService.class),
                    container.get(ChatService.class),
                    container.get(ItemService.class)
                )
            )
        );

        configurator.factory(
            GameConfiguration.class,
            container -> app.configuration().module(GameConfiguration.class)
        );

        configurator.factory(
            Server.class,
            container -> new NettyServer(
                container.get(SessionHandler.class),
                container.get(GameConfiguration.class).port()
            )
        );

        configurator.factory(
            SessionHandler.class,
            container -> new LoggingSessionHandler(
                new GameSessionHandler(
                    container.get(Dispatcher.class),
                    container.get(PacketParser.class)
                ),
                container.get(Logger.class)
            )
        );

        configurator.factory(
            Dispatcher.class,
            container -> new DefaultDispatcher(
                new AggregateLoader(
                    new CommonLoader(),
                    new LoggedLoader(),
                    new PlayingLoader(),
                    new ExploringLoader(),
                    new AdminLoader()
                ).load(container)
            )
        );

        configurator.factory(
            PacketParser.class,
            container ->  new AggregatePacketParser(
                new AggregateParserLoader(
                    new ParserLoader[]{
                        new CommonParserLoader(),
                        new GameParserLoader()
                    }
                ).load()
            )
        );

        configurator.factory(
            PlayerConstraints.class,
            container -> new PlayerConstraints(
                container.get(PlayerRepository.class),
                container.get(GameConfiguration.class).player()
            )
        );

        configureServices(configurator);
    }

    private void configureServices(ContainerConfigurator configurator)
    {
        configurator.persist(
            ConnectorService.class,
            container -> new ConnectorService(
                container.get(TokenService.class),
                container.get(AccountService.class)
            )
        );

        configurator.persist(
            TokenService.class,
            container -> new TokenService()
        );

        configurator.persist(
            AccountService.class,
            container -> new AccountService(
                container.get(AccountRepository.class),
                container.get(GameConfiguration.class)
            )
        );

        configurator.persist(
            CharactersService.class,
            container -> new CharactersService(
                container.get(PlayerRepository.class),
                container.get(PlayerConstraints.class),
                container.get(PlayerRaceRepository.class),
                container.get(fr.quatrevieux.araknemu.game.event.Dispatcher.class)
            )
        );

        configurator.persist(
            PlayerService.class,
            container -> new PlayerService(
                container.get(PlayerRepository.class),
                container.get(PlayerRaceRepository.class),
                container.get(GameConfiguration.class),
                container.get(fr.quatrevieux.araknemu.game.event.Dispatcher.class),
                container.get(InventoryService.class)
            )
        );

        configurator.persist(
            ExplorationService.class,
            container -> new ExplorationService(
                container.get(ExplorationMapService.class),
                container.get(ExplorationActionFactory.class)
            )
        );

        configurator.persist(
            ExplorationMapService.class,
            container -> new ExplorationMapService(
                container.get(MapTemplateRepository.class),
                container.get(MapTriggerRepository.class)
            )
        );

        configurator.persist(
            ExplorationActionFactory.class,
            container -> new ExplorationActionFactory()
        );

        configurator.persist(
            ChatService.class,
            container -> new ChatService(
                container.get(ListenerAggregate.class),
                container.get(GameConfiguration.class).chat(),
                new Channel[] {
                    new MapChannel(),
                    new GlobalChannel(
                        ChannelType.INCARNAM,
                        container.get(PlayerService.class)
                    ),
                    new FloodGuardChannel(
                        new GlobalChannel(
                            ChannelType.TRADE,
                            container.get(PlayerService.class)
                        ),
                        container.get(GameConfiguration.class).chat()
                    ),
                    new FloodGuardChannel(
                        new GlobalChannel(
                            ChannelType.RECRUITMENT,
                            container.get(PlayerService.class)
                        ),
                        container.get(GameConfiguration.class).chat()
                    ),
                    new GlobalChannel(
                        ChannelType.ADMIN,
                        player -> player.account().isMaster(),
                        container.get(PlayerService.class)
                    ),
                    new NullChannel(ChannelType.MEETIC),
                    new PrivateChannel(
                        container.get(PlayerService.class)
                    )
                }
            )
        );

        configurator.persist(
            AreaService.class,
            container -> new AreaService(
                container.get(SubAreaRepository.class),
                container.get(ListenerAggregate.class)
            )
        );

        configurator.persist(
            InventoryService.class,
            container -> new InventoryService(
                container.get(PlayerItemRepository.class),
                container.get(ItemService.class),
                container.get(ListenerAggregate.class)
            )
        );

        configurator.persist(
            ListenerAggregate.class,
            container -> new DefaultListenerAggregate()
        );

        configurator.factory(
            fr.quatrevieux.araknemu.game.event.Dispatcher.class,
            container -> container.get(ListenerAggregate.class)
        );

        configurator.persist(
            NameGenerator.class,
            container -> new NameCheckerGenerator(
                new CamelizeName(
                    new SimpleNameGenerator(
                        container.get(GameConfiguration.class).player()
                    )
                ),
                container.get(PlayerRepository.class),
                container.get(GameConfiguration.class)
            )
        );

        configurator.persist(
            AdminService.class,
            container -> new AdminService(
                Arrays.asList(
                    container.get(PlayerContextResolver.class),
                    container.get(AccountContextResolver.class),
                    container.get(DebugContextResolver.class)
                )
            )
        );

        configurator.persist(
            ItemService.class,
            container -> new ItemService(
                container.get(ItemTemplateRepository.class),
                container.get(ItemFactory.class)
            )
        );

        configurator.factory(
            PlayerContextResolver.class,
            container -> new PlayerContextResolver(
                container.get(PlayerService.class),
                container.get(AccountContextResolver.class),
                container.get(ItemService.class)
            )
        );

        configurator.persist(
            AccountContextResolver.class,
            container -> new AccountContextResolver(
                container.get(AccountService.class),
                container.get(AccountRepository.class)
            )
        );

        configurator.persist(
            DebugContextResolver.class,
            DebugContextResolver::new
        );

        configurator.persist(
            EffectToCharacteristicMapping.class,
            container -> new EffectToCharacteristicMapping()
        );

        configurator.persist(
            EffectToSpecialMapping.class,
            container -> new EffectToSpecialMapping()
        );

        configurator.persist(
            EffectToWeaponMapping.class,
            container -> new EffectToWeaponMapping()
        );

        configurator.persist(
            ItemFactory.class,
            container -> new DefaultItemFactory(
                new ResourceFactory(
                    container.get(EffectToSpecialMapping.class)
                ),
                new WeaponFactory(
                    container.get(EffectToWeaponMapping.class),
                    container.get(EffectToCharacteristicMapping.class),
                    container.get(EffectToSpecialMapping.class)
                ),
                new WearableFactory(
                    container.get(EffectToCharacteristicMapping.class),
                    container.get(EffectToSpecialMapping.class)
                )
            )
        );
    }
}