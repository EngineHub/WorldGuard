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
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockIgniteListener implements Listener {

    private final Predicate<Block> predicate;
    private final Set<IgniteCause> igniteCauses;

    public BlockIgniteListener(Predicate<Block> predicate, IgniteCause... igniteCauses) {
        this.predicate = predicate;
        this.igniteCauses = new HashSet<IgniteCause>(Arrays.asList(igniteCauses));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        IgniteCause cause = event.getCause();
        Block block = event.getBlock();

        if (igniteCauses.contains(cause) && predicate.apply(block)) {
            event.setCancelled(true);
        }
    }

}
