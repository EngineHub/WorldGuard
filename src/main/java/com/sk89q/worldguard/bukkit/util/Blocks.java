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
import org.bukkit.material.Bed;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
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
        MaterialData data = block.getState().getData();

        if (data instanceof Bed) {
            Bed bed = (Bed) data;
            if (bed.isHeadOfBed()) {
                return Arrays.asList(block.getRelative(bed.getFacing().getOppositeFace()));
            } else {
                return Arrays.asList(block.getRelative(bed.getFacing()));
            }
        } else {
            return Collections.emptyList();
        }
    }

}
