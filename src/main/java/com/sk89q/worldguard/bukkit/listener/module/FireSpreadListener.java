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

package com.sk89q.worldguard.bukkit.listener.module;

import com.google.common.base.Predicate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class FireSpreadListener implements Listener {

    public final static int VISIT_ADJACENT = 1;
    public final static int INDIRECT_IGNITE_CHECK = 2;

    private final Predicate<Block> predicate;
    private final int flags;

    public FireSpreadListener(Predicate<Block> predicate, int flags) {
        this.predicate = predicate;
        this.flags = flags;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();

        if (predicate.apply(event.getBlock())) {
            event.setCancelled(true);

            if ((flags & VISIT_ADJACENT) == VISIT_ADJACENT) {
                checkAndDestroyAround(block, Material.FIRE);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        IgniteCause cause = event.getCause();
        Block block = event.getBlock();

        if (cause == IgniteCause.SPREAD) {
            if ((flags & INDIRECT_IGNITE_CHECK) == INDIRECT_IGNITE_CHECK) {
                if (predicate.apply(block.getRelative(0, -1, 0))
                        || predicate.apply(block.getRelative(1, 0, 0))
                        || predicate.apply(block.getRelative(-1, 0, 0))
                        || predicate.apply(block.getRelative(0, 0, -1))
                        || predicate.apply(block.getRelative(0, 0, 1))) {
                    event.setCancelled(true);
                }
            } else if (predicate.apply(block)) {
                event.setCancelled(true);
            }
        }
    }

    private void checkAndDestroyAround(Block block, Material required) {
        checkAndDestroy(block.getRelative(0, 0, 1), required);
        checkAndDestroy(block.getRelative(0, 0, -1), required);
        checkAndDestroy(block.getRelative(0, 1, 0), required);
        checkAndDestroy(block.getRelative(0, -1, 0), required);
        checkAndDestroy(block.getRelative(1, 0, 0), required);
        checkAndDestroy(block.getRelative(-1, 0, 0), required);
    }

    private void checkAndDestroy(Block block, Material required) {
        if (block.getType() == required) {
            block.setType(Material.AIR);
        }
    }

}
