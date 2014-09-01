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

public class BlockBlockPair {

    private final BlockMaterialPair block1;
    private final BlockMaterialPair block2;

    public BlockBlockPair(Block block1, Block block2) {
        this.block1 = new BlockMaterialPair(block1);
        this.block2 = new BlockMaterialPair(block2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockBlockPair that = (BlockBlockPair) o;

        if (!block1.equals(that.block1)) return false;
        if (!block2.equals(that.block2)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = block1.hashCode();
        result = 31 * result + block2.hashCode();
        return result;
    }

}
