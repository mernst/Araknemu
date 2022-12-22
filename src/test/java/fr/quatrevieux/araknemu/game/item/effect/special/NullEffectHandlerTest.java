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

package fr.quatrevieux.araknemu.game.item.effect.special;

import fr.quatrevieux.araknemu._test.TestCase;
import fr.quatrevieux.araknemu.data.constant.Effect;
import fr.quatrevieux.araknemu.data.value.ItemTemplateEffectEntry;
import fr.quatrevieux.araknemu.game.item.effect.SpecialEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NullEffectHandlerTest extends TestCase {
    void create() {
        assertEquals(
            new SpecialEffect(NullEffectHandler.INSTANCE, Effect.NULL1, new int[] {1, 2, 3}, "a"),
            new NullEffectHandler().create(new ItemTemplateEffectEntry(Effect.NULL1, 1, 2, 3, "a"), false)
        );
    }

    void instance() {
        assertInstanceOf(NullEffectHandler.class, NullEffectHandler.INSTANCE);
    }
}
