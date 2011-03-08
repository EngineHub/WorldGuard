// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldguard.bukkit.commands;

import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.GlobalConfiguration;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandRegionSetParent extends WgRegionCommand {


    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg,
            WorldConfiguration wcfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        
        CommandHandler.checkArgs(args, 1, 2, "/region setparent <id> <parent-id>");

        String id = args[0].toLowerCase();
        String parentId = args.length > 1 ? args[1].toLowerCase() : null;
        RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(wcfg.getWorldName());

        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Could not find a region with ID: " + id);
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (region.isOwner(BukkitPlayer.wrapPlayer(plugin, player))) {
                plugin.checkPermission(sender, "worldguard.region.setparent.own");
            } else {
                plugin.checkPermission(sender, "worldguard.region.setparent");
            }
        } else {
            plugin.checkPermission(sender, "worldguard.region.setparent");
        }

        ProtectedRegion parent = null;

        // Set a parent
        if (parentId != null) {
            parent = mgr.getRegion(parentId);

            if (parent == null) {
                sender.sendMessage(ChatColor.RED + "Could not find a region with ID: " + parentId);
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (parent.isOwner(BukkitPlayer.wrapPlayer(plugin, player))) {
                    plugin.checkPermission(sender, "worldguard.region.setparent.own");
                } else {
                    plugin.checkPermission(sender, "worldguard.region.setparent");
                }
            } else {
                plugin.checkPermission(sender, "worldguard.region.setparent");
            }
        }

        try {
            region.setParent(parent);

            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
        } catch (CircularInheritanceException e) {
            sender.sendMessage(ChatColor.RED + "Circular inheritance detected. The operation failed.");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
