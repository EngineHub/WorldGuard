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

package com.sk89q.worldguard.bukkit.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Chest;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

/**
 * Utility methods to deal with blocks.
 */
public final class Blocks {

    private Blocks() {
    }

    /**
     * Get a list of connected blocks to the given block, not including
     * the given block.
     *
     * @param block the block
     * @return a list of connected blocks, not including the given block
     */
    public static List<Block> getConnected(Block block) {
        BlockState state = block.getState();
        BlockData data = state.getBlockData();

        if (data instanceof Bed) {
            Bed bed = (Bed) data;
            return Collections.singletonList(block.getRelative(bed.getPart() == Bed.Part.FOOT
                    ? bed.getFacing() : bed.getFacing().getOppositeFace()));
        } else if (data instanceof Chest) {
            final Chest chest = (Chest) data;
            Chest.Type type = chest.getType();
            if (type == Chest.Type.SINGLE) {
                return Collections.emptyList();
            }
            Vector offset = chest.getFacing().getDirection().rotateAroundY(Math.PI / 2 * (type == Chest.Type.LEFT ? -1 : 1));
            return Collections.singletonList(block.getRelative((int) Math.round(offset.getX()), 0, (int) Math.round(offset.getZ())));
        } else {
            return Collections.emptyList();
        }
    }

}
