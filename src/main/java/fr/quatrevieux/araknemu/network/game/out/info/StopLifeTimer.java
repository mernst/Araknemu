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
 * Copyright (c) 2017-2021 Vincent Quatrevieux, Jean-Alexandre Valentin
 */

package fr.quatrevieux.araknemu.network.game.out.info;

/**
 * This packet tells the client to stop the life regneration animation
 */
final public class StopLifeTimer {
    /**
     * Can be the amount of life regenerated
     * It is usually used after the sit animation
     */
    final private int life;

    public StopLifeTimer(int life) {
        this.life = life;
    }

    public StopLifeTimer() {
        this(0);
    }

    @Override
    public String toString() {
        return "ILF" + this.life;
    }
}
