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
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandStopFire extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg) throws CommandHandlingException {

        cfg.checkPermission(sender, "stopfire");
        CommandHandler.checkArgs(args, 0, 0);

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

        WorldGuardWorldConfiguration wcfg = cfg.getWorldConfig(worldName);



        if (!wcfg.fireSpreadDisableToggle) {
            cfg.getWorldGuardPlugin().getServer().broadcastMessage(ChatColor.YELLOW
                    + "Fire spread has been globally disabled by " + senderName + ".");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Fire spread was already globally disabled.");
        }

        wcfg.fireSpreadDisableToggle = true;

        return true;
    }
}
