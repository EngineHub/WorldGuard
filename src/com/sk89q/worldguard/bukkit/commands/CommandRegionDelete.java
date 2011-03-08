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
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandRegionDelete extends WgRegionCommand {
    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg,
            WorldConfiguration wcfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        
        CommandHandler.checkArgs(args, 0, 1, "/region delete <id>");

        try {
            String id = args[0].toLowerCase();
            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(wcfg.getWorldName());

            if (!mgr.hasRegion(id)) {
                sender.sendMessage(ChatColor.RED + "A region with ID '"
                        + id + "' doesn't exist.");
                return true;
            }

            ProtectedRegion existing = mgr.getRegion(id);

            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (existing.isOwner(BukkitPlayer.wrapPlayer(plugin, player))) {
                    plugin.checkPermission(sender, "worldguard.region.delete.own");
                } else {
                    plugin.checkPermission(sender, "worldguard.region.delete");
                }
            } else {
                plugin.checkPermission(sender, "worldguard.region.delete");
            }

            mgr.removeRegion(id);
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region removed!");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
