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

package com.sk89q.worldguard.util;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.WorldConfiguration;

import java.util.Map;

public final class SpongeUtil {

    private static Map<BlockType, Property<Object>> waterloggable = Maps.newHashMap();

    private SpongeUtil() {
    }

    private static boolean isReplacable(BlockType blockType) {
        return blockType == BlockTypes.WATER || blockType == BlockTypes.SEAGRASS
                || blockType == BlockTypes.TALL_SEAGRASS || blockType == BlockTypes.KELP_PLANT
                || blockType == BlockTypes.KELP;
    }

    /**
     * Remove water around a sponge.
     *
     * @param world The world the sponge is in
     * @param ox The x coordinate of the 'sponge' block
     * @param oy The y coordinate of the 'sponge' block
     * @param oz The z coordinate of the 'sponge' block
     */
    public static void clearSpongeWater(World world, int ox, int oy, int oz) {
        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

        for (int cx = -wcfg.spongeRadius; cx <= wcfg.spongeRadius; cx++) {
            for (int cy = -wcfg.spongeRadius; cy <= wcfg.spongeRadius; cy++) {
                for (int cz = -wcfg.spongeRadius; cz <= wcfg.spongeRadius; cz++) {
                    BlockVector3 vector = BlockVector3.at(ox + cx, oy + cy, oz + cz);
                    BaseBlock block = world.getFullBlock(vector);
                    BlockType blockType = block.getBlockType();
                    if (isReplacable(blockType)) {
                        try {
                            world.setBlock(vector, BlockTypes.AIR.getDefaultState());
                        } catch (WorldEditException e) {
                            e.printStackTrace();
                        }
                    } else {
                        @SuppressWarnings("unchecked")
                        Property<Object> waterloggedProp = waterloggable.computeIfAbsent(blockType,
                                (bt -> (Property<Object>) bt.getPropertyMap().get("waterlogged")));
                        if (waterloggedProp != null) {
                            try {
                                world.setBlock(vector, block.with(waterloggedProp, false));
                            } catch (WorldEditException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the given block to fluid water.
     * Used by addSpongeWater()
     *
     * @param world the world
     * @param ox x
     * @param oy y
     * @param oz z
     */
    private static void setBlockToWater(World world, int ox, int oy, int oz) throws WorldEditException {
        BlockVector3 vector = BlockVector3.at(ox, oy, oz);
        if (world.getBlock(vector).getBlockType().getMaterial().isAir()) {
            world.setBlock(vector, BlockTypes.WATER.getDefaultState());
        }
    }

    /**
     * Add water around a sponge.
     * 
     * @param world The world the sponge is located in
     * @param ox The x coordinate of the 'sponge' block
     * @param oy The y coordinate of the 'sponge' block
     * @param oz The z coordinate of the 'sponge' block
     */
    public static void addSpongeWater(World world, int ox, int oy, int oz) {
        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

        // The negative x edge
        int cx = ox - wcfg.spongeRadius - 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                BlockVector3 vector = BlockVector3.at(cx, cy, cz);
                if (isReplacable(world.getBlock(vector).getBlockType())) {
                    try {
                        setBlockToWater(world, cx + 1, cy, cz);
                    } catch (WorldEditException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // The positive x edge
        cx = ox + wcfg.spongeRadius + 1;
        for (int cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                BlockVector3 vector = BlockVector3.at(cx, cy, cz);
                if (isReplacable(world.getBlock(vector).getBlockType())) {
                    try {
                        setBlockToWater(world, cx - 1, cy, cz);
                    } catch (WorldEditException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // The negative y edge
        int cy = oy - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                BlockVector3 vector = BlockVector3.at(cx, cy, cz);
                if (isReplacable(world.getBlock(vector).getBlockType())) {
                    try {
                        setBlockToWater(world, cx, cy + 1, cz);
                    } catch (WorldEditException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // The positive y edge
        cy = oy + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (int cz = oz - wcfg.spongeRadius - 1; cz <= oz + wcfg.spongeRadius + 1; cz++) {
                BlockVector3 vector = BlockVector3.at(cx, cy, cz);
                if (isReplacable(world.getBlock(vector).getBlockType())) {
                    try {
                        setBlockToWater(world, cx, cy - 1, cz);
                    } catch (WorldEditException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // The negative z edge
        int cz = oz - wcfg.spongeRadius - 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
                BlockVector3 vector = BlockVector3.at(cx, cy, cz);
                if (isReplacable(world.getBlock(vector).getBlockType())) {
                    try {
                        setBlockToWater(world, cx, cy, cz + 1);
                    } catch (WorldEditException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // The positive z edge
        cz = oz + wcfg.spongeRadius + 1;
        for (cx = ox - wcfg.spongeRadius - 1; cx <= ox + wcfg.spongeRadius + 1; cx++) {
            for (cy = oy - wcfg.spongeRadius - 1; cy <= oy + wcfg.spongeRadius + 1; cy++) {
                BlockVector3 vector = BlockVector3.at(cx, cy, cz);
                if (isReplacable(world.getBlock(vector).getBlockType())) {
                    try {
                        setBlockToWater(world, cx, cy, cz - 1);
                    } catch (WorldEditException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
