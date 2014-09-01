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

import org.bukkit.Material;
import org.bukkit.block.Block;

import static com.google.common.base.Preconditions.checkNotNull;

public class BlockMaterialPair {

    private final Block block;
    private final Material material;

    public BlockMaterialPair(Block block) {
        checkNotNull(block);
        this.block = block;
        this.material = block.getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockMaterialPair blockKey = (BlockMaterialPair) o;

        if (!block.equals(blockKey.block)) return false;
        if (material != blockKey.material) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = block.hashCode();
        result = 31 * result + material.hashCode();
        return result;
    }

}
