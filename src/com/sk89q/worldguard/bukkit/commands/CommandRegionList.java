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

import com.sk89q.worldguard.bukkit.GlobalConfiguration;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Arrays;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Michael
 */
public class CommandRegionList extends WgRegionCommand {

    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg,
            WorldConfiguration wcfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        
        plugin.checkPermission(sender, "region.list");
        CommandHandler.checkArgs(args, 0, 1, "/region list [page]");

        int page = 0;

        if (args.length >= 1) {
            try {
                page = Math.max(0, Integer.parseInt(args[0]) - 1);
            } catch (NumberFormatException e) {
                page = 0;
            }
        }

        RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().get(wcfg.getWorldName());
        Map<String, ProtectedRegion> regions = mgr.getRegions();
        int size = regions.size();
        int pages = (int) Math.ceil(size / (float) CommandHandler.CMD_LIST_SIZE);

        String[] regionIDList = new String[size];
        int index = 0;
        for (String id : regions.keySet()) {
            regionIDList[index] = id;
            index++;
        }
        Arrays.sort(regionIDList);

        sender.sendMessage(ChatColor.RED + "Regions (page "
                + (page + 1) + " of " + pages + "):");

        if (page < pages) {
            for (int i = page * CommandHandler.CMD_LIST_SIZE; i < page * CommandHandler.CMD_LIST_SIZE + CommandHandler.CMD_LIST_SIZE; i++) {
                if (i >= size) {
                    break;
                }
                sender.sendMessage(ChatColor.YELLOW.toString() + (i + 1) + ". " + regionIDList[i]);
            }
        }

        return true;
    }
}
