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

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ToggleCommands {

    @Command(aliases = {"stopfire"},
            usage = "[<world>]", desc = "Disables all fire spread temporarily",
            flags = "", min = 0, max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public static void stopFire(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = plugin.getGlobalConfiguration()
                .forWorld(world.getName());

        if (!wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(
                    ChatColor.YELLOW
                    + "Fire spread has been globally disabled by "
                    + plugin.toName(sender) + ".");
        } else {
            sender.sendMessage(
                    ChatColor.YELLOW
                    + "Fire spread was already globally disabled.");
        }

        wcfg.fireSpreadDisableToggle = true;
    }

    @Command(aliases = {"allowfire"},
            usage = "[<world>]", desc = "Allows all fire spread temporarily",
            flags = "", min = 0, max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public static void allowFire(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = plugin.getGlobalConfiguration()
                .forWorld(world.getName());

        if (wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW
                    + "Fire spread has been globally re-enabled by "
                    + plugin.toName(sender) + ".");
        } else {
            sender.sendMessage(ChatColor.YELLOW
                    + "Fire spread was already globally enabled.");
        }

        wcfg.fireSpreadDisableToggle = false;
    }
}
