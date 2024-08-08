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

package com.sk89q.worldguard.bukkit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.blacklist.target.BlockTarget;
import com.sk89q.worldguard.blacklist.target.ItemTarget;
import com.sk89q.worldguard.blacklist.target.Target;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BukkitUtil {

    private BukkitUtil() {
    }

    /**
     * Checks if the given potion is a vial of water.
     *
     * @param item the item to check
     * @return true if it's a water vial
     */
    public static boolean isWaterPotion(ItemStack item) {
        return (item.getDurability() & 0x3F) == 0;
    }

    /**
     * Get just the potion effect bits. This is to work around bugs with potion
     * parsing.
     *
     * @param item item
     * @return new bits
     */
    public static int getPotionEffectBits(ItemStack item) {
        return item.getDurability() & 0x3F;
    }

    /**
     * Get a blacklist target for the given block.
     *
     * @param block the block
     * @return a target
     */
    public static Target createTarget(Block block) {
        checkNotNull(block);
        checkNotNull(block.getType());
        return createTarget(block.getType());
    }

    /**
     * Get a blacklist target for the given item.
     *
     * @param item the item
     * @return a target
     */
    public static Target createTarget(ItemStack item) {
        checkNotNull(item);
        checkNotNull(item.getType());
        return createTarget(item.getType()); // Delegate it, ItemStacks can contain both Blocks and Items in Spigot
    }

    /**
     * Get a blacklist target for the given material.
     *
     * @param material the material
     * @return a target
     */
    public static Target createTarget(Material material) {
        checkNotNull(material);
        if (material.isBlock()) {
            return new BlockTarget(BukkitAdapter.asBlockType(material));
        } else {
            return new ItemTarget(BukkitAdapter.asItemType(material));
        }
    }
}
