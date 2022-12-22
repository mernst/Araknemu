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

package fr.quatrevieux.araknemu.game.spell.effect;

import fr.quatrevieux.araknemu.data.value.EffectArea;
import fr.quatrevieux.araknemu.game.GameBaseCase;
import fr.quatrevieux.araknemu.game.spell.effect.area.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpellEffectServiceTest extends GameBaseCase {
    private SpellEffectService service;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        service = new SpellEffectService();
    }

    void area() {
        assertSame(CellArea.INSTANCE, service.area(new EffectArea(EffectArea.Type.CELL, 0)));
        assertInstanceOf(CircleArea.class, service.area(new EffectArea(EffectArea.Type.CIRCLE, 3)));
        assertInstanceOf(LineArea.class, service.area(new EffectArea(EffectArea.Type.LINE, 3)));
        assertInstanceOf(CrossArea.class, service.area(new EffectArea(EffectArea.Type.CROSS, 3)));
        assertInstanceOf(PerpendicularLineArea.class, service.area(new EffectArea(EffectArea.Type.PERPENDICULAR_LINE, 3)));
        assertInstanceOf(RingArea.class, service.area(new EffectArea(EffectArea.Type.RING, 3)));
    }
}
