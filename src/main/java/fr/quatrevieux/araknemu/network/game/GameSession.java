package fr.quatrevieux.araknemu.network.game;

import fr.quatrevieux.araknemu.game.account.GameAccount;
import fr.quatrevieux.araknemu.game.exploration.ExplorationPlayer;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.network.adapter.Channel;
import fr.quatrevieux.araknemu.network.adapter.Session;

/**
 * Session wrapper for game server
 */
final public class GameSession implements Session {
    final private Channel channel;

    private GameAccount account;
    private GamePlayer player;
    private ExplorationPlayer exploration;

    public GameSession(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public void write(Object packet) {
        channel.write(packet);
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public boolean isAlive() {
        return channel.isAlive();
    }

    /**
     * Attach an game account to the session
     *
     * @param account Account to attach
     */
    public void attach(GameAccount account) {
        this.account = account;
    }

    /**
     * Get the attached account
     */
    public GameAccount account() {
        return account;
    }

    /**
     * Check if an account is attached
     */
    public boolean isLogged() {
        return account != null;
    }

    /**
     * Remove the attached account
     * @return The attached account
     */
    public GameAccount detach() {
        return account = null;
    }

    /**
     * Set the logged player
     */
    public void setPlayer(GamePlayer player) {
        this.player = player;
    }

    /**
     * Get the logged player
     *
     * @return The player instance, or null is not in game
     */
    public GamePlayer player() {
        return player;
    }

    /**
     * Get the exploration player
     *
     * @return The player instance, or null if not on exploration
     */
    public ExplorationPlayer exploration() {
        return exploration;
    }

    /**
     * Set the exploration player
     */
    public void setExploration(ExplorationPlayer exploration) {
        this.exploration = exploration;
    }
}