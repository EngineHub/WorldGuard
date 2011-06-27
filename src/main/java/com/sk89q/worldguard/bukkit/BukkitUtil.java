// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import java.util.List;

import com.sk89q.worldedit.blocks.BlockType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;

public class BukkitUtil {


    private BukkitUtil()  {
    }
    
    /**
     * Converts the location of a Bukkit block to a WorldEdit vector.
     * 
     * @param block
     * @return
     */
    public static BlockVector toVector(Block block) {
        return new BlockVector(block.getX(), block.getY(), block.getZ());
    }
    
    /**
     * Converts a Bukkit location to a WorldEdit vector.
     * 
     * @param loc
     * @return
     */
    public static Vector toVector(Location loc) {
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }
    
    /**
     * Converts a Bukkit vector to a WorldEdit vector.
     * 
     * @param vector
     * @return
     */
    public static Vector toVector(org.bukkit.util.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Converts a WorldEdit vector to a Bukkit location.
     * 
     * @param world
     * @param vec 
     * @return
     */
    public static Location toLocation(World world, Vector vec) {
        return new Location(world, vec.getX(), vec.getY(), vec.getZ());
    }
    
    /**
     * Matches one player based on name.
     * 
     * @param server
     * @param name
     * @return
     */
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
     * @param block
     */
    public static void dropSign(Block block) {
        block.setTypeId(0);
        block.getWorld().dropItemNaturally(block.getLocation(),
                new ItemStack(Material.SIGN));
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
     * Checks if the given block is water
     * 
     * @param world
     * @param ox
     * @param oy
     * @param oz
     * @return 
     */
    public static boolean isBlockWater(World world, int ox, int oy, int oz) {
        Block block = world.getBlockAt(ox, oy, oz);
        int id = block.getTypeId();
        if (id == 8 || id == 9) {
            return true;
        } else {
            return false;
        }
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

        while (y <= 129) {
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
                    if (y <= 2 && world.getBlockAt(x,0,z).getType() == Material.AIR) {
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
}
