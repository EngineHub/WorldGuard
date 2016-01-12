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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.blacklist.target.MaterialTarget;
import com.sk89q.worldguard.blacklist.target.Target;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.Enums;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitUtil {

    private static Method ONLINE_PLAYERS_METHOD;

    private BukkitUtil()  {

    }

    /**
     * Converts the location of a Bukkit block to a WorldEdit vector.
     *
     * @param block The block to convert
     * @return The block's location as a BlockVector
     */
    public static BlockVector toVector(Block block) {
        return new BlockVector(block.getX(), block.getY(), block.getZ());
    }

    /**
     * Converts a Bukkit location to a WorldEdit vector.
     *
     * @param loc A Bukkit Location
     * @return A Vector with the location's x, y, and z values
     */
    public static Vector toVector(Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Converts a Bukkit vector to a WorldEdit vector.
     *
     * @param vector The Bukkit vector
     * @return A WorldEdit vector with the same values as the Bukkit vector.
     */
    public static Vector toVector(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Converts a WorldEdit vector to a Bukkit location.
     *
     * @param world The World to create the new Location with
     * @param vec The vector to use for coordinates
     * @return The Vector as a location with a World of world
     */
    public static Location toLocation(World world, Vector vec) {
        return new Location(world, vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Create a dummy region that encompasses the size of a chunk.
     *
     * @param chunk The chunk
     * @return The dummy region
     */
    public static ProtectedRegion toRegion(Chunk chunk) {
        World world = chunk.getWorld();

        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;
        return new ProtectedCuboidRegion("_", new BlockVector(minX, 0, minZ), new BlockVector(minX + 15, world.getMaxHeight(), minZ + 15));
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
     * Drops a sign item and removes a sign.
     *
     * @param block The block
     * @deprecated see {@link org.bukkit.block.Block#breakNaturally()}
     */
    @Deprecated
    public static void dropSign(Block block) {
        block.setTypeId(0);
        block.getWorld().dropItemNaturally(block.getLocation(),
                new ItemStack(ItemID.SIGN, 1));
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
        int id = block.getTypeId();
        if (id == 0) {
            block.setTypeId(8);
        }
    }
    
    /**
     * Sets the given block to fluid lava.
     * Used by addSpongeLava()
     *
     * @param world
     * @param ox
     * @param oy
     * @param oz
     */
    public static void setBlockToLava(World world, int ox, int oy, int oz) {
        Block block = world.getBlockAt(ox, oy, oz);
        int id = block.getTypeId();
        if (id == 0) {
            block.setTypeId(10);
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
        int id = block.getTypeId();
        return id == 8 || id == 9;
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
    public static boolean isBlockLava(World world, int ox, int oy, int oz) {
        Block block = world.getBlockAt(ox, oy, oz);
        int id = block.getTypeId();
        return id == 10 || id == 11;
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
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     *
     * @param player
     */
    public static void findFreePosition(Player player) {
        Location loc = player.getLocation();
        int x = loc.getBlockX();
        int y = Math.max(0, loc.getBlockY());
        int origY = y;
        int z = loc.getBlockZ();
        World world = player.getWorld();

        byte free = 0;

        while (y <= world.getMaxHeight() + 1) {
            if (BlockType.canPassThrough(world.getBlockTypeIdAt(x, y, z))) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY || y == 1) {
                    loc.setX(x + 0.5);
                    loc.setY(y);
                    loc.setZ(z + 0.5);
                    if (y <= 2 && world.getBlockAt(x,0,z).getTypeId() == BlockID.AIR) {
                        world.getBlockAt(x,0,z).setTypeId(20);
                        loc.setY(2);
                    }
                    player.setFallDistance(0F);
                    player.teleport(loc);
                }
                return;
            }

            y++;
        }
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

    private static final org.bukkit.entity.EntityType armorStandType =
        Enums.findByValue(org.bukkit.entity.EntityType.class, "ARMOR_STAND");

    /**
     * Returns whether an entity should be removed for the halt activity mode.
     *
     * @param entity
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
                    && !(entity.getType() == armorStandType));
    }

    /**
     * Search an enum for a value, and return the first one found. Return null if the
     * enum entry is not found.
     *
     * @param enumType enum class
     * @param values values to test
     * @return a value in the enum or null
     * @deprecated use {@link Enums#findByValue(Class, String...)}
     */
    @Deprecated
    public static <T extends Enum<T>> T tryEnum(Class<T> enumType, String ... values) {
        for (String val : values) {
            try {
                return Enum.valueOf(enumType, val);
            } catch (IllegalArgumentException e) {
            }
        }

        return null;
    }

    /**
     * Get a blacklist target for the given block.
     *
     * @param block the block
     * @return a target
     */
    public static Target createTarget(Block block) {
        checkNotNull(block);
        return new MaterialTarget(block.getTypeId(), block.getData());
    }

    /**
     * Get a blacklist target for the given block.
     *
     * @param block the block
     * @param material a fallback material
     * @return a target
     */
    public static Target createTarget(Block block, Material material) {
        checkNotNull(material);
        if (block.getType() == material) {
            return new MaterialTarget(block.getTypeId(), block.getData());
        } else {
            return new MaterialTarget(material.getId(), (short) 0);
        }
    }

    /**
     * Get a blacklist target for the given item.
     *
     * @param item the item
     * @return a target
     */
    public static Target createTarget(ItemStack item) {
        checkNotNull(item);
        return new MaterialTarget(item.getTypeId(), item.getDurability());
    }

    /**
     * Get a blacklist target for the given material.
     *
     * @param material the material
     * @return a target
     */
    public static Target createTarget(Material material) {
        checkNotNull(material);
        return new MaterialTarget(material.getId(), (short) 0);
    }

    /**
     * Get a collection of the currently online players.
     *
     * @return The online players
     */
    @SuppressWarnings("unchecked")
    public static Collection<? extends Player> getOnlinePlayers() {
        try {
            return Bukkit.getServer().getOnlinePlayers();
        } catch (NoSuchMethodError ignored) {
        }

        try {
            if (ONLINE_PLAYERS_METHOD == null) {
                ONLINE_PLAYERS_METHOD = getOnlinePlayersMethod();
            }

            Object result = ONLINE_PLAYERS_METHOD.invoke(Bukkit.getServer());
            if (result instanceof Player[]) {
                return ImmutableList.copyOf((Player[]) result);
            } else if (result instanceof Collection<?>) {
                return (Collection<? extends Player>) result;
            } else {
                throw new RuntimeException("Result of getOnlinePlayers() call was not a known data type");
            }
        } catch (Exception e) {
            throw new RuntimeException("WorldGuard is not compatible with this version of Bukkit", e);
        }
    }

    private static Method getOnlinePlayersMethod() throws NoSuchMethodException {
        try {
            return Server.class.getMethod("getOnlinePlayers");
        } catch (NoSuchMethodException e1) {
            return Server.class.getMethod("_INVALID_getOnlinePlayers");
        }
    }

}
