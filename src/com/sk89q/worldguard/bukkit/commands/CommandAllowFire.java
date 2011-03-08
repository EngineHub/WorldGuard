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
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandAllowFire extends WgCommand {

    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        plugin.checkPermission(sender, "worldguard.fire-toggle.allow");

        String worldName;

        if (sender instanceof Player) {
            CommandHandler.checkArgs(args, 0, 0);
            worldName = ((Player)sender).getWorld().getName();
        } else {
            CommandHandler.checkArgs(args, 1, 1);
            worldName = args[0];
        }

        Server server = cfg.getWorldGuardPlugin().getServer();
        if(server.getWorld(worldName) == null)
        {
            sender.sendMessage("Invalid world specified.");
            return true;
        }

        WorldConfiguration wcfg = cfg.getWorldConfig(worldName);

        if (wcfg.fireSpreadDisableToggle) {
            server.broadcastMessage(ChatColor.YELLOW
                    + "Fire spread has been globally re-enabled by " + senderName + ".");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Fire spread was already globally enabled.");
        }

        wcfg.fireSpreadDisableToggle = false;

        return true;
    }
}
