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

package com.sk89q.worldguard.internal.listener;

import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.cause.Causes;
import com.sk89q.worldguard.internal.event.block.BreakBlockEvent;
import com.sk89q.worldguard.internal.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.internal.event.block.UseBlockEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * Handle events that need to be processed by the chest protection.
 */
public class ChestProtectionListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public ChestProtectionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(PlaceBlockEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());
        Location target = event.getTarget();

        if (player != null) {
            WorldConfiguration wcfg = getWorldConfig(player);

            // Early guard
            if (!wcfg.signChestProtection) {
                return;
            }

            if (wcfg.getChestProtection().isChest(event.getEffectiveMaterial().getId()) && wcfg.isChestProtected(target.getBlock(), player)) {
                player.sendMessage(ChatColor.DARK_RED + "This spot is for a chest that you don't have permission for.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(BreakBlockEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());
        Location target = event.getTarget();

        if (player != null) {
            WorldConfiguration wcfg = getWorldConfig(player);

            // Early guard
            if (!wcfg.signChestProtection) {
                return;
            }

            if (wcfg.isChestProtected(target.getBlock(), player)) {
                player.sendMessage(ChatColor.DARK_RED + "This chest is protected.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(UseBlockEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());
        Location target = event.getTarget();

        if (player != null) {
            WorldConfiguration wcfg = getWorldConfig(player);

            // Early guard
            if (!wcfg.signChestProtection) {
                return;
            }

            if (wcfg.isChestProtected(target.getBlock(), player)) {
                player.sendMessage(ChatColor.DARK_RED + "This chest is protected.");
                event.setCancelled(true);
            }
        }
    }

}
