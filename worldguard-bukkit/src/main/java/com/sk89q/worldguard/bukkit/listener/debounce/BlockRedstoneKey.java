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

import java.util.Objects;

/**
 * A key for debouncing BlockRedstoneEvents.
 *
 * Identifies a redstone change by the block location and new power state, so
 * that repeated events for the same transition are coalesced into a single
 * sponge check.
 */
public class BlockRedstoneKey {

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private final boolean powered;

    public BlockRedstoneKey(Block block, boolean powered) {
        this.worldName = block.getWorld().getName();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.powered = powered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockRedstoneKey)) return false;
        BlockRedstoneKey that = (BlockRedstoneKey) o;
        return x == that.x && y == that.y && z == that.z
                && powered == that.powered && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z, powered);
    }
}