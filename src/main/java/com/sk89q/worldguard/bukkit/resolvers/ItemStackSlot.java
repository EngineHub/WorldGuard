// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.resolvers;

import org.bukkit.inventory.ItemStack;

/**
 * Indicates an item stack slot that can be updated with a new stack.
 *
 * @author sk89q
 */
public interface ItemStackSlot {

    /**
     * Get the current item stack.
     *
     * @return item stack, or null
     */
    ItemStack get();

    /**
     * Update the slot with a new item stack.
     *
     * @param stack the new item stack
     */
    void update(ItemStack stack);

}
