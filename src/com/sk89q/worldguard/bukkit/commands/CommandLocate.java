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

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.GlobalConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandLocate extends WgCommand {

    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }

        Player player = (Player) sender;
        plugin.checkPermission(sender, "locate");
        CommandHandler.checkArgs(args, 0, 3);

        if (args.length == 1) {
            String name = args[0];
            Player target = BukkitUtil.matchSinglePlayer(cfg.getWorldGuardPlugin().getServer(), name);
            if (target != null) {
                player.setCompassTarget(target.getLocation());
                player.sendMessage(ChatColor.YELLOW + "Compass target set to " + target.getName() + ".");
            } else {
                player.sendMessage(ChatColor.RED + "Could not find player.");
            }
        } else if (args.length == 3) {
            try {
                Location loc = new Location(
                        player.getWorld(),
                        Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]),
                        Integer.parseInt(args[2]));
                player.setCompassTarget(loc);
                player.sendMessage(ChatColor.YELLOW + "Compass target set to "
                        + loc.getBlockX() + ","
                        + loc.getBlockY() + ","
                        + loc.getBlockZ() + ".");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number specified");
            }
        } else if (args.length == 0) {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            player.sendMessage(ChatColor.YELLOW + "Compass reset to the spawn location.");
        } else {
            return false;
        }

        return true;
    }
}
