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

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

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
        Player player = event.getCause().getPlayerRootCause();
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
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();

        WorldConfiguration wcfg = getWorldConfig(target.getWorld());

        // Early guard
        if (!wcfg.signChestProtection) {
            return;
        }

        if (player != null) {
            if (wcfg.isChestProtected(target.getBlock(), player)) {
                player.sendMessage(ChatColor.DARK_RED + "This chest is protected.");
                event.setCancelled(true);
            }
        } else {
            if (wcfg.isChestProtected(target.getBlock())) {
                // No player? Deny anyway
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseBlock(UseBlockEvent event) {
        Player player = event.getCause().getPlayerRootCause();
        Location target = event.getTarget();

        WorldConfiguration wcfg = getWorldConfig(target.getWorld());

        // Early guard
        if (!wcfg.signChestProtection) {
            return;
        }

        if (player != null) {
            if (wcfg.isChestProtected(target.getBlock(), player)) {
                player.sendMessage(ChatColor.DARK_RED + "This chest is protected.");
                event.setCancelled(true);
            }
        } else {
            if (wcfg.isChestProtected(target.getBlock())) {
                // No player? Deny anyway
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        WorldConfiguration wcfg = getWorldConfig(player);

        if (wcfg.signChestProtection) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                if (wcfg.isChestProtectedPlacement(event.getBlock(), player)) {
                    player.sendMessage(ChatColor.DARK_RED + "You do not own the adjacent chest.");
                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (event.getBlock().getTypeId() != BlockID.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "The [Lock] sign must be a sign post, not a wall sign.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                if (!event.getLine(1).equalsIgnoreCase(player.getName())) {
                    player.sendMessage(ChatColor.RED
                            + "The first owner line must be your name.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                int below = event.getBlock().getRelative(0, -1, 0).getTypeId();

                if (below == BlockID.TNT || below == BlockID.SAND
                        || below == BlockID.GRAVEL || below == BlockID.SIGN_POST) {
                    player.sendMessage(ChatColor.RED
                            + "That is not a safe block that you're putting this sign on.");

                    event.getBlock().breakNaturally();
                    event.setCancelled(true);
                    return;
                }

                event.setLine(0, "[Lock]");
                player.sendMessage(ChatColor.YELLOW
                        + "A chest or double chest above is now protected.");
            }
        } else if (!wcfg.disableSignChestProtectionCheck) {
            if (event.getLine(0).equalsIgnoreCase("[Lock]")) {
                player.sendMessage(ChatColor.RED
                        + "WorldGuard's sign chest protection is disabled.");

                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }

}
