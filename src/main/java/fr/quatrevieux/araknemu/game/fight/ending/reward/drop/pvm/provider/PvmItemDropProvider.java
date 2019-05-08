package fr.quatrevieux.araknemu.game.fight.ending.reward.drop.pvm.provider;

import fr.quatrevieux.araknemu.data.world.entity.monster.MonsterRewardItem;
import fr.quatrevieux.araknemu.game.fight.ending.EndFightResults;
import fr.quatrevieux.araknemu.game.fight.ending.reward.drop.DropReward;
import fr.quatrevieux.araknemu.game.fight.fighter.operation.FighterOperation;
import fr.quatrevieux.araknemu.game.fight.fighter.monster.MonsterFighter;
import fr.quatrevieux.araknemu.util.RandomUtil;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Provider for dropped items on a Pvm fight
 *
 * - get all item rewards from monsters
 * - filters items with a discernment rate higher than the team discernment
 * - randomize the dropped items
 * - distribute the same number of items between all winners
 * - for each items, remove one from the quantity, and test the rate
 *
 * Note: each winners can only have one occurrence of an item, per monster
 */
final public class PvmItemDropProvider implements DropRewardProvider {
    private class Scope implements DropRewardProvider.Scope {
        final private List<Pair<MonsterRewardItem, Integer>> dropsAndQuantity;
        final private int maxPerFighter;

        public Scope(List<Pair<MonsterRewardItem, Integer>> dropsAndQuantity, int maxPerFighter) {
            this.dropsAndQuantity = dropsAndQuantity;
            this.maxPerFighter = maxPerFighter;
        }

        @Override
        public void provide(DropReward reward) {
            ItemDropIterator iterator = new ItemDropIterator(dropsAndQuantity);

            for (int count = maxPerFighter; count > 0 && iterator.hasNext(); --count) {
                MonsterRewardItem drop = iterator.next();
                iterator.remove();

                if (random.decimal(100) > drop.rate()) {
                    continue;
                }

                reward.addItem(drop.itemTemplateId());
            }
        }
    }

    static private class ItemDropIterator implements Iterator<MonsterRewardItem> {
        final private List<Pair<MonsterRewardItem, Integer>> dropsAndQuantity;

        private int current = -1;

        public ItemDropIterator(List<Pair<MonsterRewardItem, Integer>> dropsAndQuantity) {
            this.dropsAndQuantity = dropsAndQuantity;
        }

        @Override
        public boolean hasNext() {
            return dropsAndQuantity.size() > current + 1;
        }

        @Override
        public MonsterRewardItem next() {
            return dropsAndQuantity.get(++current).getKey();
        }

        @Override
        public void remove() {
            Pair<MonsterRewardItem, Integer> currentPair = dropsAndQuantity.get(current);

            // Remove one from the quantity
            currentPair.setValue(currentPair.getValue() - 1);

            // Out of quantity : remove the item and move cursor to left
            if (currentPair.getValue() == 0) {
                dropsAndQuantity.remove(current--);
            }
        }
    }

    static private class ExtractDrops implements FighterOperation {
        final private int discernment;

        final private List<Pair<MonsterRewardItem, Integer>> dropsAndQuantity = new ArrayList<>();

        public ExtractDrops(int discernment) {
            this.discernment = discernment;
        }

        @Override
        public void onMonster(MonsterFighter fighter) {
            for (MonsterRewardItem item : fighter.reward().items()) {
                if (discernment >= item.discernment()) {
                    dropsAndQuantity.add(new MutablePair<>(item, item.quantity()));
                }
            }
        }
    }

    final private RandomUtil random = new RandomUtil();

    @Override
    public DropRewardProvider.Scope initialize(EndFightResults results) {
        ExtractDrops operation = new ExtractDrops(
            results.winners().stream()
                .mapToInt(fighter -> fighter.characteristics().discernment())
                .sum()
        );

        results.applyToLoosers(operation);

        return new Scope(
            random.shuffle(operation.dropsAndQuantity),
            (int) Math.ceil((double) operation.dropsAndQuantity.size() / (double) results.winners().size())
        );
    }
}