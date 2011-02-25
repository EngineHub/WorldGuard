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

import com.sk89q.worldguard.bukkit.WorldGuardConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardWorldConfiguration;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Michael
 */
public class CommandRegionPriority extends WgRegionCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg, WorldGuardWorldConfiguration wcfg) throws CommandHandlingException {

        CommandHandler.checkArgs(args, 1, 2, "/region priority <id> (<value>)");
        cfg.checkRegionPermission(sender, "region.priority");

        try {
            String id = args[0].toLowerCase();
            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(wcfg.getWorldName());

            if (!mgr.hasRegion(id)) {
                sender.sendMessage(ChatColor.RED + "A region with ID '"
                        + id + "' doesn't exist.");
                return true;
            }

            ProtectedRegion existing = mgr.getRegion(id);

            if (args.length > 1) {
                try {
                    Integer prio = Integer.valueOf(args[1]);
                    existing.setPriority(prio);
                    mgr.save();
                    sender.sendMessage(ChatColor.YELLOW + "Priority of region " + existing.getId() + " set to " + prio.toString());
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Not a valid number: " + args[1]);
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Priority of region " + existing.getId() + " is " + existing.getPriority());
            }


        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
