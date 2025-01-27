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
 * Copyright (c) 2017-2020 Vincent Quatrevieux
 */

package fr.quatrevieux.araknemu.game.fight.map;

import fr.arakne.utils.maps.CoordinateCell;
import fr.arakne.utils.maps.serializer.CellData;
import fr.quatrevieux.araknemu.game.fight.exception.FightMapException;
import fr.quatrevieux.araknemu.game.fight.fighter.Fighter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Base fight cell
 */
public final class WalkableFightCell implements FightCell {
    private final FightMap map;
    private final CellData template;
    private final @NonNegative int id;
    private final CoordinateCell<BattlefieldCell> coordinate;

    private @Nullable Fighter fighter;

    @SuppressWarnings({"assignment", "argument"})
    public WalkableFightCell(FightMap map, CellData template, @NonNegative int id) {
        this.map = map;
        this.template = template;
        this.id = id;
        this.coordinate = new CoordinateCell<>(this);
    }

    @Override
    public @NonNegative int id() {
        return id;
    }

    @Override
    public FightMap map() {
        return map;
    }

    @Override
    public boolean walkable() {
        return fighter == null;
    }

    @Override
    public boolean walkableIgnoreFighter() {
        return true;
    }

    @Override
    public boolean sightBlocking() {
        return !template.lineOfSight() || fighter != null;
    }

    @Override
    public CoordinateCell<BattlefieldCell> coordinate() {
        return coordinate;
    }

    @Override
    public @Nullable Fighter fighter() {
        return fighter;
    }

    @Override
    public void set(Fighter fighter) {
        if (this.fighter != null) {
            throw new FightMapException("A fighter is already set on this cell (" + id + ")");
        }

        this.fighter = fighter;
    }

    @Override
    public void removeFighter() {
        if (this.fighter == null) {
            throw new FightMapException("No fighter found on cell " + id);
        }

        this.fighter = null;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final WalkableFightCell that = (WalkableFightCell) o;

        return id == that.id && map == that.map;
    }
}
