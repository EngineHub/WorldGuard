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
import com.sk89q.worldguard.blacklist.target.ItemTarget;
import com.sk89q.worldguard.blacklist.target.Target;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BukkitUtil {

    private BukkitUtil() {
    }

    /**
     * Matches one player based on name.
     *
     * @param server The server to check
     * @param name The name to attempt to match
     * @deprecated see {@link WorldGuardPlugin#matchSinglePlayer(org.bukkit.command.CommandSender, String)}
     * @return The matched player if any, otherwise null
     */
    @Deprecated
    public static Player matchSinglePlayer(Server server, String name) {
        List<Player> players = server.matchPlayer(name);
        if (players.size() == 0) {
            return null;
        }
        return players.get(0);
    }

    /**
     * Sets the given block to fluid water.
     * Used by addSpongeWater()
     *
     * @param world
     * @param ox
     * @param oy
     * @param oz
     */
    public static void setBlockToWater(World world, int ox, int oy, int oz) {
        Block block = world.getBlockAt(ox, oy, oz);
        if (block.getType() == Material.AIR) {
            block.setType(Material.WATER);
        }
    }

    /**
     * Checks if the given block is water
     *
     * @param world the world
     * @param ox x
     * @param oy y
     * @param oz z
     * @return true if it's water
     */
    public static boolean isBlockWater(World world, int ox, int oy, int oz) {
        Block block = world.getBlockAt(ox, oy, oz);
        return block.getType() == Material.WATER;
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
     * Replace color macros in a string. The macros are in the form of `[char]
     * where char represents the color. R is for red, Y is for yellow,
     * G is for green, C is for cyan, B is for blue, and P is for purple.
     * The uppercase versions of those are the darker shades, while the
     * lowercase versions are the lighter shades. For white, it's 'w', and
     * 0-2 are black, dark grey, and grey, respectively.
     *
     * @param str
     * @return color-coded string
     */
    public static String replaceColorMacros(String str) {
        str = str.replace("&r", ChatColor.RED.toString());
        str = str.replace("&R", ChatColor.DARK_RED.toString());

        str = str.replace("&y", ChatColor.YELLOW.toString());
        str = str.replace("&Y", ChatColor.GOLD.toString());

        str = str.replace("&g", ChatColor.GREEN.toString());
        str = str.replace("&G", ChatColor.DARK_GREEN.toString());

        str = str.replace("&c", ChatColor.AQUA.toString());
        str = str.replace("&C", ChatColor.DARK_AQUA.toString());

        str = str.replace("&b", ChatColor.BLUE.toString());
        str = str.replace("&B", ChatColor.DARK_BLUE.toString());

        str = str.replace("&p", ChatColor.LIGHT_PURPLE.toString());
        str = str.replace("&P", ChatColor.DARK_PURPLE.toString());

        str = str.replace("&0", ChatColor.BLACK.toString());
        str = str.replace("&1", ChatColor.DARK_GRAY.toString());
        str = str.replace("&2", ChatColor.GRAY.toString());
        str = str.replace("&w", ChatColor.WHITE.toString());

        str = str.replace("&k", ChatColor.MAGIC.toString());
        str = str.replace("&l", ChatColor.BOLD.toString());
        str = str.replace("&m", ChatColor.STRIKETHROUGH.toString());
        str = str.replace("&n", ChatColor.UNDERLINE.toString());
        str = str.replace("&o", ChatColor.ITALIC.toString());

        str = str.replace("&x", ChatColor.RESET.toString());

        return str;
    }

    /**
     * Returns whether an entity should be removed for the halt activity mode.
     *
     * @param entity The entity
     * @return true if it's to be removed
     */
    public static boolean isIntensiveEntity(Entity entity) {
        return entity instanceof Item
                || entity instanceof TNTPrimed
                || entity instanceof ExperienceOrb
                || entity instanceof FallingBlock
                || (entity instanceof LivingEntity
                    && !(entity instanceof Tameable)
                    && !(entity instanceof Player)
                    && !(entity instanceof ArmorStand));
    }

    /**
     * Get a blacklist target for the given block.
     *
     * @param block the block
     * @return a target
     */
    public static Target createTarget(Block block) {
        checkNotNull(block);
        return new ItemTarget(BukkitAdapter.adapt(block.getBlockData()).getBlockType().getItemType());
    }

    /**
     * Get a blacklist target for the given item.
     *
     * @param item the item
     * @return a target
     */
    public static Target createTarget(ItemStack item) {
        checkNotNull(item);
        return new ItemTarget(BukkitAdapter.adapt(item).getType());
    }

    /**
     * Get a blacklist target for the given material.
     *
     * @param material the material
     * @return a target
     */
    public static Target createTarget(Material material) {
        checkNotNull(material);
        return new ItemTarget(BukkitAdapter.adapt(material));
    }
}
