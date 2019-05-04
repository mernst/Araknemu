package fr.quatrevieux.araknemu.game.fight.ending.reward.drop;

import fr.quatrevieux.araknemu.game.fight.ending.reward.FightReward;
import fr.quatrevieux.araknemu.game.fight.ending.reward.RewardType;
import fr.quatrevieux.araknemu.game.fight.ending.reward.drop.action.DropRewardAction;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import fr.quatrevieux.araknemu.game.fight.fighter.operation.FighterOperation;
import fr.quatrevieux.araknemu.game.fight.fighter.player.PlayerFighter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reward for drop
 */
final public class DropReward implements FightReward {
    final private RewardType type;
    final private Fighter fighter;
    final private List<DropRewardAction> actions;

    private long xp = 0;
    private long mountXp = 0;
    private long guildXp = 0;
    private int kamas = 0;
    private Map<Integer, Integer> items = new HashMap<>();

    public DropReward(RewardType type, Fighter fighter, List<DropRewardAction> actions) {
        this.type = type;
        this.fighter = fighter;
        this.actions = actions;
    }

    @Override
    public Fighter fighter() {
        return fighter;
    }

    @Override
    public RewardType type() {
        return type;
    }

    @Override
    public void apply() {
        actions.forEach(action -> action.apply(this, fighter));
    }

    @Override
    public String render() {
        return
            type().id() + ";" +
            fighter().id() + ";" +
            fighter().sprite().name() + ";" +
            fighter().level() + ";" +
            (fighter().dead() ? "1" : "0") + ";" +
            fighter().apply(new FormatExperience()).format() + ";" +
            (xp() != 0 ? xp() : "") + ";" +
            (guildXp() != 0 ? guildXp() : "") + ";" +
            (mountXp() != 0 ? mountXp() : "") + ";" +
            items.entrySet().stream()
                .map(entry -> entry.getKey() + "~" + entry.getValue())
                .collect(Collectors.joining(",")) + ";" +
            (kamas() != 0 ? kamas() : "")
        ;
    }

    /**
     * Get player win xp
     */
    public long xp() {
        return xp;
    }

    /**
     * Xp given to the player's guild
     */
    public long guildXp() {
        return guildXp;
    }

    /**
     * Xp given to the player's mount
     */
    public long mountXp() {
        return mountXp;
    }

    /**
     * Win kamas
     */
    public int kamas() {
        return kamas;
    }

    /**
     * Get list of win items
     *
     * The key is the item id
     * The value is the item quantity
     */
    public Map<Integer, Integer> items() {
        return items;
    }

    /**
     * Set the player win XP
     *
     * @see DropReward#xp()
     */
    public void setXp(long xp) {
        this.xp = xp;
    }

    /**
     * Set xp give to mount
     *
     * @see DropReward#mountXp()
     */
    public void setMountXp(long mountXp) {
        this.mountXp = mountXp;
    }

    /**
     * Set the xp give to guild
     *
     * @see DropReward#guildXp()
     */
    public void setGuildXp(long guildXp) {
        this.guildXp = guildXp;
    }

    /**
     * Set win kamas
     *
     * @see DropReward#kamas()
     */
    public void setKamas(int kamas) {
        this.kamas = kamas;
    }

    /**
     * Add an item to the win items
     *
     * @param itemId The item template id
     * @param quantity Quantity of the item to add
     *
     * @see fr.quatrevieux.araknemu.data.world.entity.item.ItemTemplate#id()
     */
    public void addItem(int itemId, int quantity) {
        items.put(itemId, items.getOrDefault(itemId, 0) + quantity);
    }
    /**
     * Add one item to the win items
     *
     * @param itemId The item template id
     *
     * @see fr.quatrevieux.araknemu.data.world.entity.item.ItemTemplate#id()
     * @see DropReward#addItem(int, int)
     */
    public void addItem(int itemId) {
        addItem(itemId, 1);
    }

    /**
     * Format the experience string for the reward line
     */
    static private class FormatExperience implements FighterOperation {
        private String format = "0;0;0";

        @Override
        public void onPlayer(PlayerFighter fighter) {
            format = fighter.properties().experience().min() + ";" + fighter.properties().experience().current() + ";" + fighter.properties().experience().max();
        }

        public String format() {
            return format;
        }
    }
}
