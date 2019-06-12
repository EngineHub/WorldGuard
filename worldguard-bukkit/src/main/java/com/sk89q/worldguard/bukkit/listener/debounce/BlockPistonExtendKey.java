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

package com.sk89q.worldguard.bukkit.listener.debounce;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPistonExtendEvent;

import java.util.List;

public class BlockPistonExtendKey {

    private final Block piston;
    private final List<Block> blocks;
    private final int blocksHashCode;

    public BlockPistonExtendKey(BlockPistonExtendEvent event) {
        piston = event.getBlock();
        blocks = event.getBlocks();
        blocksHashCode = blocks.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockPistonExtendKey that = (BlockPistonExtendKey) o;

        if (!blocks.equals(that.blocks)) return false;
        if (!piston.equals(that.piston)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = piston.hashCode();
        result = 31 * result + blocksHashCode;
        return result;
    }

}
