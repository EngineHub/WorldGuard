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

package com.sk89q.worldguard.bukkit.listener.feature;

import com.google.common.base.Predicate;
import com.sk89q.worldguard.bukkit.util.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class ObsidianGeneratorListener implements Listener {

    private final Predicate<Block> predicate;

    public ObsidianGeneratorListener(Predicate<Block> predicate) {
        this.predicate = predicate;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block blockFrom = event.getBlock();
        Block blockTo = event.getToBlock();

        if (predicate.apply(blockTo) && (blockFrom.getType() == Material.AIR || Materials.isLava(blockFrom.getType()))
                && (blockTo.getType() == Material.REDSTONE_WIRE || blockTo.getType() == Material.TRIPWIRE)) {
            blockTo.setType(Material.AIR);
        }
    }

}
