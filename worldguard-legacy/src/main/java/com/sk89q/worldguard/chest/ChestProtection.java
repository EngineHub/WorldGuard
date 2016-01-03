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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

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
    public boolean isProtected(Block block, Player player);

    /**
     * Returns whether a location where a chest block is trying to be created 
     * is protected. 
     * 
     * @param block The block to check
     * @param player The player to check
     * @return Whether {@code player} can place a block at the specified block
     */
    public boolean isProtectedPlacement(Block block, Player player);

    /**
     * Returns whether an adjacent chest is protected.
     * 
     * @param searchBlock The block to check
     * @param player The player to check
     * @return Whether {@code searchBlock} is protected from access by {@code player}
     */
    public boolean isAdjacentChestProtected(Block searchBlock, Player player);

    /**
     * Returns whether a material is a chest.
     *
     * @param material The material to check
     * @deprecated see {@link #isChest(int)}
     * @return {@link #isChest(int)}
     */
    @Deprecated
    public boolean isChest(Material material);

    /**
     * Returns whether a material is a chest.
     *
     * @param type The type to check
     * @return Whether type is a 'chest' (block that can be protected)
     */
    public boolean isChest(int type);

}