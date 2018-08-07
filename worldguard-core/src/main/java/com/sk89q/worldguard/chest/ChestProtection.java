/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.chest;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.LocalPlayer;

/**
 * Interface for chest protection.
 */
public interface ChestProtection {

    /**
     * Returns whether a block is protected.
     * 
     * @param block The block to check
     * @param player The player to check
     * @return Whether the block is protected for player
     */
    boolean isProtected(Location block, LocalPlayer player);

    /**
     * Returns whether a location where a chest block is trying to be created 
     * is protected. 
     * 
     * @param block The block to check
     * @param player The player to check
     * @return Whether {@code player} can place a block at the specified block
     */
    boolean isProtectedPlacement(Location block, LocalPlayer player);

    /**
     * Returns whether an adjacent chest is protected.
     * 
     * @param searchBlock The block to check
     * @param player The player to check
     * @return Whether {@code searchBlock} is protected from access by {@code player}
     */
    boolean isAdjacentChestProtected(Location searchBlock, LocalPlayer player);

    /**
     * Returns whether a blockType is a chest.
     *
     * @param blockType The blockType to check
     * @return Whether a type is a 'chest' (protectable block)
     */
    default boolean isChest(BlockType blockType) {
        return blockType == BlockTypes.CHEST
                || blockType == BlockTypes.DISPENSER
                || blockType == BlockTypes.FURNACE
                || blockType == BlockTypes.TRAPPED_CHEST
                || blockType == BlockTypes.DROPPER;
    }

}