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
import org.bukkit.event.block.BlockPistonRetractEvent;

public class BlockPistonRetractKey {

    private final Block piston;
    private final Block retract;

    public BlockPistonRetractKey(BlockPistonRetractEvent event) {
        piston = event.getBlock();
        retract = event.getRetractLocation().getBlock();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockPistonRetractKey that = (BlockPistonRetractKey) o;

        if (!piston.equals(that.piston)) return false;
        if (!retract.equals(that.retract)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = piston.hashCode();
        result = 31 * result + retract.hashCode();
        return result;
    }

}
