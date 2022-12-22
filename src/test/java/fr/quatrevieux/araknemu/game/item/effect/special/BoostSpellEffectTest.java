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

import fr.quatrevieux.araknemu.data.constant.Effect;
import fr.quatrevieux.araknemu.data.value.ItemTemplateEffectEntry;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.item.effect.SpecialEffect;
import fr.quatrevieux.araknemu.game.player.GamePlayer;
import fr.quatrevieux.araknemu.game.spell.boost.SpellsBoosts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoostSpellEffectTest extends GameBaseCase {
    private BoostSpellEffect handler;
    private GamePlayer player;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        handler = new BoostSpellEffect(SpellsBoosts.Modifier.DAMAGE);
        player = gamePlayer(true);
    }

    void applyFirstTime() {
        handler.apply(
            new SpecialEffect(handler, Effect.SPELL_ADD_DAMAGE, new int[] {3, 0, 15}, ""),
            player
        );

        assertEquals(15, player.properties().spells().boosts().modifiers(3).damage());
    }

    void applyWillAddToCurrentBoost() {
        player.properties().spells().boosts().set(3, SpellsBoosts.Modifier.DAMAGE, 5);

        handler.apply(
            new SpecialEffect(handler, Effect.SPELL_ADD_DAMAGE, new int[] {3, 0, 15}, ""),
            player
        );

        assertEquals(20, player.properties().spells().boosts().modifiers(3).damage());
    }

    void relieve() {
        player.properties().spells().boosts().set(3, SpellsBoosts.Modifier.DAMAGE, 20);

        handler.relieve(
            new SpecialEffect(handler, Effect.SPELL_ADD_DAMAGE, new int[] {3, 0, 15}, ""),
            player
        );

        assertEquals(5, player.properties().spells().boosts().modifiers(3).damage());
    }

    void create() {
        assertEquals(
            new SpecialEffect(handler, Effect.SPELL_ADD_DAMAGE, new int[] {3, 0, 5}, ""),
            handler.create(new ItemTemplateEffectEntry(Effect.SPELL_ADD_DAMAGE, 3, 0, 5, ""), true)
        );
    }
}
