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

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.Events;
import com.sk89q.worldguard.internal.cause.Causes;
import com.sk89q.worldguard.internal.event.BlockInteractEvent;
import com.sk89q.worldguard.internal.event.Interaction;
import com.sk89q.worldguard.internal.event.ItemInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Collections;

public class CauseListener implements Listener {

    private final WorldGuardPlugin plugin;

    public CauseListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Block target = event.getBlock();

        // Previously, and perhaps still, the only way to catch cake eating
        // events was through here
        if (target.getType() == Material.CAKE_BLOCK) {
            Events.fireToCancel(event, new BlockInteractEvent(event, Causes.create(event.getPlayer()), Interaction.INTERACT, target));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Events.fireToCancel(event, new BlockInteractEvent(event, Collections.emptyList(), Interaction.BREAK, event.getBlock()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Events.fireToCancel(event, new BlockInteractEvent(event, Causes.create(event.getPlayer()), Interaction.BREAK, event.getBlock()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Events.fireToCancel(event, new BlockInteractEvent(event, Causes.create(event.getPlayer()), Interaction.PLACE, event.getBlock()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Events.fireToCancel(event, new BlockInteractEvent(event, Causes.create(event.getPlayer()), Interaction.INTERACT, event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        Events.fireToCancel(event, new ItemInteractEvent(event, Causes.create(event.getBlock()), Interaction.INTERACT, event.getBlock().getWorld(), event.getItem()));
    }

}
