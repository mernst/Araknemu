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

package fr.quatrevieux.araknemu.data.living.entity;

import fr.quatrevieux.araknemu.data.value.ItemTemplateEffectEntry;

import java.util.List;

/**
 * Base item type for item storage
 */
public interface Item {
    /**
     * The entry id
     * The id is unique for the bank, and generated by an increment
     *
     * Part of the primary key
     */
    public int entryId();

    /**
     * The item template id
     *
     * @see fr.quatrevieux.araknemu.data.world.entity.item.ItemTemplate#id()
     */
    public int itemTemplateId();

    /**
     * Effects of the item
     */
    public List<ItemTemplateEffectEntry> effects();

    /**
     * The item quantity
     */
    public int quantity();

    /**
     * Change the quantity
     *
     * @param quantity New quantity. Must be positive
     */
    public void setQuantity(int quantity);
}