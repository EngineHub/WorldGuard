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

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.blocks.BlockID;

/**
 * An applicator of the sponge feature.
 */
class SpongeApplicator {

    private final int spongeRadius;
    private final boolean useRedstone;
    private final boolean refillArea;

    /**
     * Construct a new applicator.
     *
     * @param spongeRadius the radius of the sponge's effect
     * @param useRedstone use Redstone
     * @param refillArea refill the area if a sponge is removed
     */
    public SpongeApplicator(int spongeRadius, boolean useRedstone, boolean refillArea) {
        this.spongeRadius = spongeRadius;
        this.useRedstone = useRedstone;
        this.refillArea = refillArea;
    }

    /**
     * Returns whether the block at the given position is water.
     *
     * @param world world
     * @param x x
     * @param y y
     * @param z z
     * @return true if it's water
     */
    private static boolean isWater(World world, int x, int y, int z) {
        int type = world.getBlockTypeIdAt(x, y, z);
        return type == BlockID.WATER || type == BlockID.STATIONARY_WATER;
    }

    /**
     * Returns whether the given sponge is an active sponge.
     *
     * @param block block to check
     * @return true if it's active
     */
    public boolean isActiveSponge(Block block) {
        return block.getType() == Material.SPONGE
                && (!useRedstone || !block.isBlockIndirectlyPowered());
    }

    /**
     * Return true if a given block near an active sponge.
     *
     * @param blockTo location of the block
     * @return true if it's near a sponge (and water should be cancelled)
     */
    public boolean isNearSponge(Block blockTo) {
        World world = blockTo.getWorld();
        int ox = blockTo.getX();
        int oy = blockTo.getY();
        int oz = blockTo.getZ();

        for (int cx = -spongeRadius; cx <= spongeRadius; cx++) {
            for (int cy = -spongeRadius; cy <= spongeRadius; cy++) {
                for (int cz = -spongeRadius; cz <= spongeRadius; cz++) {
                    Block sponge = world.getBlockAt(ox + cx, oy + cy, oz + cz);
                    if (isActiveSponge(sponge)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Remove water due to a sponge from the given position.
     *
     * @param block the block
     */
    public void clearWater(Block block) {
        World world = block.getWorld();
        int ox = block.getX();
        int oy = block.getY();
        int oz = block.getZ();

        for (int cx = -spongeRadius; cx <= spongeRadius; cx++) {
            for (int cy = -spongeRadius; cy <= spongeRadius; cy++) {
                for (int cz = -spongeRadius; cz <= spongeRadius; cz++) {
                    if (isWater(world, ox + cx, oy + cy, oz + cz)) {
                        world.getBlockAt(ox + cx, oy + cy, oz + cz).setTypeId(0);
                    }
                }
            }
        }
    }

    /**
     * Add water due to a sponge from the given position.
     *
     * @param block the block
     */
    public void placeWater(Block block) {
        if (!refillArea) return;

        World world = block.getWorld();
        int ox = block.getX();
        int oy = block.getY();
        int oz = block.getZ();

        // The negative x edge
        int cx = ox - spongeRadius - 1;
        for (int cy = oy - spongeRadius - 1; cy <= oy + spongeRadius + 1; cy++) {
            for (int cz = oz - spongeRadius - 1; cz <= oz + spongeRadius + 1; cz++) {
                if (isWater(world, cx, cy, cz)) {
                     world.getBlockAt(cx + 1, cy, cz).setType(Material.STATIONARY_WATER);
                }
            }
        }

        // The positive x edge
        cx = ox + spongeRadius + 1;
        for (int cy = oy - spongeRadius - 1; cy <= oy + spongeRadius + 1; cy++) {
            for (int cz = oz - spongeRadius - 1; cz <= oz + spongeRadius + 1; cz++) {
                if (isWater(world, cx, cy, cz)) {
                     world.getBlockAt(cx - 1, cy, cz).setType(Material.STATIONARY_WATER);
                }
            }
        }

        // The negative y edge
        int cy = oy - spongeRadius - 1;
        for (cx = ox - spongeRadius - 1; cx <= ox + spongeRadius + 1; cx++) {
            for (int cz = oz - spongeRadius - 1; cz <= oz + spongeRadius + 1; cz++) {
                if (isWater(world, cx, cy, cz)) {
                     world.getBlockAt(cx, cy + 1, cz).setType(Material.STATIONARY_WATER);
                }
            }
        }

        // The positive y edge
        cy = oy + spongeRadius + 1;
        for (cx = ox - spongeRadius - 1; cx <= ox + spongeRadius + 1; cx++) {
            for (int cz = oz - spongeRadius - 1; cz <= oz + spongeRadius + 1; cz++) {
                if (isWater(world, cx, cy, cz)) {
                     world.getBlockAt(cx, cy - 1, cz).setType(Material.STATIONARY_WATER);
                }
            }
        }

        // The negative z edge
        int cz = oz - spongeRadius - 1;
        for (cx = ox - spongeRadius - 1; cx <= ox + spongeRadius + 1; cx++) {
            for (cy = oy - spongeRadius - 1; cy <= oy + spongeRadius + 1; cy++) {
                if (isWater(world, cx, cy, cz)) {
                     world.getBlockAt(cx, cy, cz + 1).setType(Material.STATIONARY_WATER);
                }
            }
        }

        // The positive z edge
        cz = oz + spongeRadius + 1;
        for (cx = ox - spongeRadius - 1; cx <= ox + spongeRadius + 1; cx++) {
            for (cy = oy - spongeRadius - 1; cy <= oy + spongeRadius + 1; cy++) {
                if (isWater(world, cx, cy, cz)) {
                     world.getBlockAt(cx, cy, cz - 1).setType(Material.STATIONARY_WATER);
                }
            }
        }
    }
}
