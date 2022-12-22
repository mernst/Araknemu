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

package fr.quatrevieux.araknemu.data.transformer;

import fr.quatrevieux.araknemu.data.constant.Characteristic;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.Characteristics;
import fr.quatrevieux.araknemu.game.world.creature.characteristics.DefaultCharacteristics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacteristicsTransformerTest {
    private CharacteristicsTransformer transformer;

    void setUp() {
        transformer = new CharacteristicsTransformer();
    }

    void serializeSameValue() {
        DefaultCharacteristics characteristics = new DefaultCharacteristics();

        characteristics.set(Characteristic.RESISTANCE_ACTION_POINT, 24);
        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 3);
        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 3);

        assertEquals(
            transformer.serialize(characteristics),
            transformer.serialize(characteristics)
        );
    }

    void serializeIgnoreNull() {
        DefaultCharacteristics characteristics = new DefaultCharacteristics();

        characteristics.set(Characteristic.STRENGTH, 3000);
        characteristics.set(Characteristic.RESISTANCE_ACTION_POINT, 0);
        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 0);
        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 3);

        assertEquals("a:2to;h:3;", transformer.serialize(characteristics));
    }

    void serializeOneStat() {
        DefaultCharacteristics characteristics = new DefaultCharacteristics();

        characteristics.set(Characteristic.STRENGTH, 200);

        assertEquals("a:68;", transformer.serialize(characteristics));
    }

    void serializeNegativeValue() {
        DefaultCharacteristics characteristics = new DefaultCharacteristics();

        characteristics.set(Characteristic.STRENGTH, -200);

        assertEquals("a:-68;", transformer.serialize(characteristics));
    }

    void serializeEmpty() {
        assertEquals("", transformer.serialize(new DefaultCharacteristics()));
    }

    void serializeNull() {
        assertNull(transformer.serialize(null));
    }

    void unserializeNull() {
        assertNull(transformer.unserialize(null));
    }

    void unserializeNegativeValue() {
        DefaultCharacteristics characteristics = new DefaultCharacteristics();

        characteristics.set(Characteristic.STRENGTH, -200);

        assertEquals(characteristics, transformer.unserialize("a:-68"));
    }

    void unserializeEmpty() {
        assertEquals(new DefaultCharacteristics(), transformer.unserialize(""));
    }

    void functional() {
        DefaultCharacteristics characteristics = new DefaultCharacteristics();

        characteristics.set(Characteristic.RESISTANCE_ACTION_POINT, 24);
        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 3);
        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 3);

        assertEquals(
            characteristics,
            transformer.unserialize(
                transformer.serialize(characteristics)
            )
        );
    }

    void serializeRaceStats() {
        DefaultCharacteristics characteristics = new DefaultCharacteristics();

        characteristics.set(Characteristic.ACTION_POINT, 6);
        characteristics.set(Characteristic.MOVEMENT_POINT, 3);
        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 1);

        assertEquals("8:6;9:3;h:1;", transformer.serialize(characteristics));

        characteristics.set(Characteristic.MAX_SUMMONED_CREATURES, 3);

        assertEquals("8:6;9:3;h:3;", transformer.serialize(characteristics));
    }
}
