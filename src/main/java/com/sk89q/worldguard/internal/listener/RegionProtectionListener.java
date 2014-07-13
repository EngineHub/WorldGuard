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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.cause.Causes;
import com.sk89q.worldguard.internal.event.Action;
import com.sk89q.worldguard.internal.event.BlockInteractEvent;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

/**
 * Handle events that need to be processed by region protection.
 */
public class RegionProtectionListener extends AbstractListener {

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public RegionProtectionListener(WorldGuardPlugin plugin) {
        super(plugin);
    }

    private void tellErrorMessage(CommandSender sender, Object subject) {
        sender.sendMessage(ChatColor.DARK_RED + "You don't have permission for this area.");
    }

    @EventHandler(ignoreCancelled = true)
    public void handleBlockInteract(BlockInteractEvent event) {
        Player player = Causes.getInvolvedPlayer(event.getCauses());
        Block target = event.getTarget();

        if (player != null) {
            if (!getPlugin().getGlobalRegionManager().canBuild(player, target)) {
                tellErrorMessage(player, target);
                event.setCancelled(true);
                return;
            }

            if (event.getAction() != Action.INTERACT) {
                if (!getPlugin().getGlobalRegionManager().canConstruct(player, target)) {
                    tellErrorMessage(player, target);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
