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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldguard.bukkit.LoggerToChatHandler;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldGuardCommands {

    @Command(aliases = {"version"},
            usage = "",
            desc = "Get the WorldGuard version",
            flags = "", min = 0, max = 0)
    public static void version(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.YELLOW
                + "WorldGuard " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW
                + "http://www.sk89q.com");
    }

    @Command(aliases = {"reload"},
            usage = "",
            desc = "Reload WorldGuard configuration",
            flags = "", min = 0, max = 0)
    @CommandPermissions({"worldguard.reload"})
    public static void relload(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;
        
        if (sender instanceof Player) {
            handler = new LoggerToChatHandler(sender);
            handler.setLevel(Level.ALL);
            minecraftLogger = Logger.getLogger("Minecraft");
            minecraftLogger.addHandler(handler);
        }

        try {
            plugin.getGlobalConfiguration().unload();
            plugin.getGlobalRegionManager().unload();
            plugin.getGlobalConfiguration().load();
            plugin.getGlobalRegionManager().preload();
            sender.sendMessage("WorldGuard configuration reloaded.");
        } catch (Throwable t) {
            sender.sendMessage("Error while reloading: "
                    + t.getMessage());
        } finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }

    }
}
