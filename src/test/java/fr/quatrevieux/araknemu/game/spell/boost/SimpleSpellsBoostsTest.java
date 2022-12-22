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

package fr.quatrevieux.araknemu.game.spell.boost;

import fr.quatrevieux.araknemu.core.di.ContainerException;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.spell.Spell;
import fr.quatrevieux.araknemu.game.spell.SpellService;
import fr.quatrevieux.araknemu.game.spell.boost.spell.BoostedSpell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSpellsBoostsTest extends GameBaseCase {
    private SimpleSpellsBoosts boosts;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        boosts = new SimpleSpellsBoosts();

        dataSet.pushSpells();
    }

    void boostUnknownSpellWillSetTheValue() {
        assertEquals(15, boosts.boost(5, SpellsBoosts.Modifier.DAMAGE, 15));
        assertEquals(15, boosts.modifiers(5).damage());
    }

    void boostUnknownModifierWillSetTheValue() {
        boosts.boost(5, SpellsBoosts.Modifier.CRITICAL, 15);

        assertEquals(15, boosts.boost(5, SpellsBoosts.Modifier.DAMAGE, 15));
        assertEquals(15, boosts.modifiers(5).damage());
    }

    void boostWillAddValueModifier() {
        boosts.boost(5, SpellsBoosts.Modifier.CRITICAL, 10);
        assertEquals(20, boosts.boost(5, SpellsBoosts.Modifier.CRITICAL, 10));
        assertEquals(20, boosts.modifiers(5).criticalHit());
    }

    void boostWithNegativeValueShouldRemoveBoost() {
        Spell spell = container.get(SpellService.class).get(3).level(2);

        boosts.boost(3, SpellsBoosts.Modifier.CRITICAL, 10);
        assertNotSame(spell, boosts.get(spell));

        assertEquals(5, boosts.boost(3, SpellsBoosts.Modifier.CRITICAL, -5));
        assertNotSame(spell, boosts.get(spell));

        assertEquals(0, boosts.boost(3, SpellsBoosts.Modifier.CRITICAL, -5));
        assertSame(spell, boosts.get(spell));
    }

    void setNewValue() {
        assertEquals(5, boosts.set(1, SpellsBoosts.Modifier.DAMAGE, 5));
        assertEquals(5, boosts.modifiers(1).damage());
    }

    void setOverrideOldValue() {
        boosts.set(1, SpellsBoosts.Modifier.DAMAGE, 5);

        assertEquals(10, boosts.set(1, SpellsBoosts.Modifier.DAMAGE, 10));
        assertEquals(10, boosts.modifiers(1).damage());
    }

    void unsetNotSetValueDoNothing() {
        boosts.unset(1, SpellsBoosts.Modifier.DAMAGE);
    }

    void unsetWillRemoveValue() {
        boosts.set(1, SpellsBoosts.Modifier.DAMAGE, 5);
        boosts.unset(1, SpellsBoosts.Modifier.DAMAGE);

        assertFalse(boosts.modifiers(1).has(SpellsBoosts.Modifier.DAMAGE));
    }

    void getNotBoosted() throws ContainerException {
        Spell spell = container.get(SpellService.class).get(3).level(2);

        assertSame(spell, boosts.get(spell));
    }

    void getBoosted() throws ContainerException {
        boosts.set(3, SpellsBoosts.Modifier.DAMAGE, 5);
        boosts.set(3, SpellsBoosts.Modifier.LINE_OF_SIGHT, 1);

        Spell spell = container.get(SpellService.class).get(3).level(2);

        Spell boosted = boosts.get(spell);

        assertInstanceOf(BoostedSpell.class, boosted);

        assertEquals(3, boosted.effects().get(0).min());
        assertEquals(5, boosted.effects().get(0).boost());
        assertEquals(7, boosted.effects().get(0).max());
        assertFalse(boosted.constraints().lineOfSight());
    }

    void getWithBoostRemovedShouldNotReturnBoostedSpell() throws ContainerException {
        boosts.set(3, SpellsBoosts.Modifier.DAMAGE, 5);
        boosts.set(3, SpellsBoosts.Modifier.LINE_OF_SIGHT, 1);

        Spell spell = container.get(SpellService.class).get(3).level(2);

        assertInstanceOf(BoostedSpell.class, boosts.get(spell));

        boosts.unset(3, SpellsBoosts.Modifier.DAMAGE);
        assertInstanceOf(BoostedSpell.class, boosts.get(spell));

        boosts.unset(3, SpellsBoosts.Modifier.LINE_OF_SIGHT);
        assertSame(spell, boosts.get(spell));
    }

    void all() {
        boosts.set(3, SpellsBoosts.Modifier.DAMAGE, 5);
        boosts.set(6, SpellsBoosts.Modifier.LINE_OF_SIGHT, 1);

        assertCount(2, boosts.all());
        assertContainsOnly(MapSpellModifiers.class, boosts.all());
    }
}
