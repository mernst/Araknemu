package fr.quatrevieux.araknemu.game.player;

import fr.quatrevieux.araknemu.data.living.entity.player.Player;
import fr.quatrevieux.araknemu.data.value.Position;
import fr.quatrevieux.araknemu.data.world.entity.character.PlayerRace;
import fr.quatrevieux.araknemu.game.account.GameAccount;
import fr.quatrevieux.araknemu.game.chat.ChannelSet;
import fr.quatrevieux.araknemu.game.chat.ChannelType;
import fr.quatrevieux.araknemu.game.event.DefaultListenerAggregate;
import fr.quatrevieux.araknemu.game.event.Dispatcher;
import fr.quatrevieux.araknemu.game.event.ListenerAggregate;
import fr.quatrevieux.araknemu.game.event.listener.player.inventory.SendItemData;
import fr.quatrevieux.araknemu.game.event.listener.player.inventory.SendItemPosition;
import fr.quatrevieux.araknemu.game.event.listener.player.inventory.SendItemQuantity;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.player.inventory.InventoryService;
import fr.quatrevieux.araknemu.game.player.inventory.PlayerInventory;
import fr.quatrevieux.araknemu.network.game.GameSession;

import java.util.Set;

/**
 * GamePlayer object
 * A player is a logged character, with associated game session
 */
final public class GamePlayer extends AbstractCharacter implements Dispatcher, PlayerData {
    final private PlayerService service;
    final private GameSession session;
    final private PlayerRace race;
    final private PlayerCharacteristics characteristics;
    final private Set<ChannelType> channels;
    final private ListenerAggregate dispatcher;
    final private PlayerInventory inventory;

    public GamePlayer(GameAccount account, Player entity, PlayerRace race, GameSession session, PlayerService service, ListenerAggregate dispatcher, PlayerInventory inventory) {
        super(account, entity);

        this.race = race;
        this.session = session;
        this.service = service;
        this.dispatcher = dispatcher;
        this.channels = new ChannelSet(entity.channels(), dispatcher);
        this.inventory = inventory;

        characteristics = new PlayerCharacteristics(
            new BaseCharacteristics(
                race.baseStats(),
                entity.stats()
            )
        );
    }

    @Override
    public void dispatch(Object event) {
        dispatcher.dispatch(event);
    }

    public ListenerAggregate dispatcher() {
        return dispatcher;
    }

    @Override
    public PlayerCharacteristics characteristics() {
        return characteristics;
    }

    /**
     * Send a packet to the player
     */
    public void send(Object packet) {
        session.write(packet);
    }

    /**
     * Get the current player position
     */
    public Position position() {
        return entity.position();
    }

    /**
     * Set new position
     */
    public void setPosition(Position position) {
        entity.setPosition(position);
    }

    /**
     * Get the player race
     */
    public PlayerRace race() {
        return race;
    }

    public String name() {
        return entity.name();
    }

    /**
     * Get channels subscriptions
     */
    public Set<ChannelType> subscriptions() {
        return channels;
    }

    /**
     * Check if the player is exploring
     */
    public boolean isExploring() {
        return session.exploration() != null;
    }

    /**
     * Get the exploration player
     *
     * @throws IllegalStateException When the player is not on exploration state
     */
    public ExplorationPlayer exploration() {
        if (!isExploring()) {
            throw new IllegalStateException("The current player is not an exploration state");
        }

        return session.exploration();
    }

    /**
     * Save the player
     */
    public void save() {
        service.save(this);
    }

    public PlayerInventory inventory() {
        return inventory;
    }
}