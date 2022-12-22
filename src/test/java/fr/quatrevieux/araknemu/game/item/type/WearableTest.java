/*
 * This file is part of Araknemu.
 *
 * Araknemu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Araknemu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Araknemu.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2017-2019 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.item.type;

import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.data.constant.Effect;
import fr.quatrevieux.araknemu.data.value.ItemTemplateEffectEntry;
import fr.quatrevieux.araknemu.data.world.entity.item.ItemTemplate;
import fr.quatrevieux.araknemu.data.world.entity.item.ItemType;
import fr.quatrevieux.araknemu.game.item.SuperType;
import fr.quatrevieux.araknemu.game.item.effect.CharacteristicEffect;
import fr.quatrevieux.araknemu.game.item.effect.SpecialEffect;
import fr.quatrevieux.araknemu.game.item.effect.special.NullEffectHandler;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.DefaultCharacteristics;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.MutableCharacteristics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class WearableTest {
    void getters() {
        Wearable wearable = new Wearable(
            new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
            new ItemType(1, "Amulette", SuperType.AMULET, null),
            null,
            Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)),
            new ArrayList<>()
        );

        assertEquals(39, wearable.template().id());
        assertEquals(Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)), wearable.characteristics());
    }

    void equalsBadType() {
        assertNotEquals(
            new Wearable(
                new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
                new ItemType(1, "Amulette", SuperType.AMULET, null),
                null,
                Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)),
                new ArrayList<>()
            ),
            new Object()
        );
    }

    void equalsBadCharacteristics() {
        assertNotEquals(
            new Wearable(
                new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
                new ItemType(1, "Amulette", SuperType.AMULET, null),
                null,
                Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)),
                new ArrayList<>()
            ),new Wearable(
                new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
                new ItemType(1, "Amulette", SuperType.AMULET, null),
                null,
                Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 3, +1, Characteristic.INTELLIGENCE)),
                new ArrayList<>()
            )
        );
    }

    void equalsOk() {
        assertEquals(
            new Wearable(
                new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
                new ItemType(1, "Amulette", SuperType.AMULET, null),
                null,
                Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)),
                new ArrayList<>()
            ),
            new Wearable(
                new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
                new ItemType(1, "Amulette", SuperType.AMULET, null),
                null,
                Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)),
                new ArrayList<>()
            )
        );
    }

    void effects() {
        Wearable wearable = new Wearable(
            new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
            new ItemType(1, "Amulette", SuperType.AMULET, null),
            null,
            Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)),
            Arrays.asList(new SpecialEffect(NullEffectHandler.INSTANCE, Effect.NULL1, new int[] {0, 0, 0}, ""))
        );

        assertEquals(
            Arrays.asList(
                new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE),
                new SpecialEffect(NullEffectHandler.INSTANCE, Effect.NULL1, new int[] {0, 0, 0}, "")
            ),
            wearable.effects()
        );
    }

    void apply() {
        Wearable wearable = new Wearable(
            new ItemTemplate(39, 1, "Petite Amulette du Hibou", 1, Arrays.asList(new ItemTemplateEffectEntry(Effect.ADD_INTELLIGENCE, 2, 0, 0, "0d0+2")), 4, "", 0, "", 100),
            new ItemType(1, "Amulette", SuperType.AMULET, null),
            null,
            Arrays.asList(new CharacteristicEffect(Effect.ADD_INTELLIGENCE, 2, +1, Characteristic.INTELLIGENCE)),
            Arrays.asList(new SpecialEffect(NullEffectHandler.INSTANCE, Effect.NULL1, new int[] {0, 0, 0}, ""))
        );

        MutableCharacteristics characteristics = new DefaultCharacteristics();
        characteristics.set(Characteristic.INTELLIGENCE, 5);

        wearable.apply(characteristics);

        assertEquals(7, characteristics.get(Characteristic.INTELLIGENCE));
    }
}
