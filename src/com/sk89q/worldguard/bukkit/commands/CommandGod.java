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
public class CommandGod extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg) throws CommandHandlingException {

        CommandHandler.checkArgs(args, 0, 1);

        // Allow setting other people invincible
        if (args.length > 0) {
            cfg.checkPermission(sender, "god.other");

            Player other = matchSinglePlayer(cfg.getWorldGuardPlugin().getServer(), args[0]);
            if (other == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            } else {
                if (!cfg.isInvinciblePlayer(other.getName())) {
                    cfg.addInvinciblePlayer(other.getName());
                    sender.sendMessage(ChatColor.YELLOW + other.getName() + " is now invincible!");
                    other.sendMessage(ChatColor.YELLOW + senderName + " has made you invincible!");
                } else {
                    cfg.removeInvinciblePlayer(other.getName());
                    sender.sendMessage(ChatColor.YELLOW + other.getName() + " is no longer invincible.");
                    other.sendMessage(ChatColor.YELLOW + senderName + " has taken away your invincibility.");
                }
            }
            // Invincibility for one's self
        } else if(sender instanceof Player) {
            cfg.checkPermission(sender, "god.self");
            Player player = (Player)sender;
            if (!cfg.isInvinciblePlayer(player.getName())) {
                cfg.addInvinciblePlayer(player.getName());
                player.sendMessage(ChatColor.YELLOW + "You are now invincible!");
            } else {
                cfg.removeInvinciblePlayer(player.getName());
                player.sendMessage(ChatColor.YELLOW + "You are no longer invincible.");
            }
        }

        return true;
    }
}
