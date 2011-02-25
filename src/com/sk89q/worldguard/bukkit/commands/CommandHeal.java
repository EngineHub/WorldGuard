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
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

/**
 *
 * @author Michael
 */
public class CommandHeal extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg) throws CommandHandlingException {

        CommandHandler.checkArgs(args, 0, 1);

        // Allow healing other people
        if (args.length > 0) {
            cfg.checkPermission(sender, "heal.other");

            Player other = matchSinglePlayer(cfg.getWorldGuardPlugin().getServer(), args[0]);
            if (other == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            } else {
                other.setHealth(20);
                sender.sendMessage(ChatColor.YELLOW + other.getName() + " has been healed!");
                other.sendMessage(ChatColor.YELLOW + senderName + " has healed you!");
            }
        } else if (sender instanceof Player){
            cfg.checkPermission(sender, "heal.self");
            Player player = (Player)sender;
            player.setHealth(20);
            player.sendMessage(ChatColor.YELLOW + "You have been healed!");
        }

        return true;
    }
}
