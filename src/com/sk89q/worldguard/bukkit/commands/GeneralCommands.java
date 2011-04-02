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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class GeneralCommands {
    
    @Command(aliases = {"god"},
            usage = "[player]",
            desc = "Enable godmode on a player",
            flags = "", min = 0, max = 1)
    public static void god(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        ConfigurationManager config = plugin.getGlobalConfiguration();
        
        Iterable<Player> targets = null;
        boolean included = false;
        
        // Detect arguments based on the number of arguments provided
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.god");
        } else if (args.argsLength() == 1) {            
            targets = plugin.matchPlayers(sender, args.getString(0));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.god.other");
        }

        for (Player player : targets) {
            config.enableGodMode(player);
            
            // Tell the user
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "God mode enabled!");
                
                // Keep track of this
                included = true;
            } else {
                player.sendMessage(ChatColor.YELLOW + "God enabled by "
                        + plugin.toName(sender) + ".");
                
            }
        }
        
        // The player didn't receive any items, then we need to send the
        // user a message so s/he know that something is indeed working
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players now have god mode.");
        }
    }
    
    @Command(aliases = {"ungod"},
            usage = "[player]",
            desc = "Disable godmode on a player",
            flags = "", min = 0, max = 1)
    public static void ungod(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        ConfigurationManager config = plugin.getGlobalConfiguration();
        
        Iterable<Player> targets = null;
        boolean included = false;
        
        // Detect arguments based on the number of arguments provided
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.god");
        } else if (args.argsLength() == 1) {            
            targets = plugin.matchPlayers(sender, args.getString(0));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.god.other");
        }

        for (Player player : targets) {
            config.disableGodMode(player);
            
            // Tell the user
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "God mode disabled!");
                
                // Keep track of this
                included = true;
            } else {
                player.sendMessage(ChatColor.YELLOW + "God disabled by "
                        + plugin.toName(sender) + ".");
                
            }
        }
        
        // The player didn't receive any items, then we need to send the
        // user a message so s/he know that something is indeed working
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players now have god mode.");
        }
    }
    
    @Command(aliases = {"heal"},
            usage = "[player]",
            desc = "Heal a player",
            flags = "", min = 0, max = 1)
    public static void heal(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        Iterable<Player> targets = null;
        boolean included = false;
        
        // Detect arguments based on the number of arguments provided
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.heal");
        } else if (args.argsLength() == 1) {            
            targets = plugin.matchPlayers(sender, args.getString(0));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.heal.other");
        }

        for (Player player : targets) {
            player.setHealth(20);
            
            // Tell the user
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "Healed!");
                
                // Keep track of this
                included = true;
            } else {
                player.sendMessage(ChatColor.YELLOW + "Healed by "
                        + plugin.toName(sender) + ".");
                
            }
        }
        
        // The player didn't receive any items, then we need to send the
        // user a message so s/he know that something is indeed working
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players healed.");
        }
    }
    
    @Command(aliases = {"slay"},
            usage = "[player]",
            desc = "Slay a player",
            flags = "", min = 0, max = 1)
    public static void slay(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        Iterable<Player> targets = null;
        boolean included = false;
        
        // Detect arguments based on the number of arguments provided
        if (args.argsLength() == 0) {
            targets = plugin.matchPlayers(plugin.checkPlayer(sender));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.slay");
        } else if (args.argsLength() == 1) {            
            targets = plugin.matchPlayers(sender, args.getString(0));
            
            // Check permissions!
            plugin.checkPermission(sender, "worldguard.slay.other");
        }

        for (Player player : targets) {
            player.setHealth(0);
            
            // Tell the user
            if (player.equals(sender)) {
                player.sendMessage(ChatColor.YELLOW + "Slayed!");
                
                // Keep track of this
                included = true;
            } else {
                player.sendMessage(ChatColor.YELLOW + "Slayed by "
                        + plugin.toName(sender) + ".");
                
            }
        }
        
        // The player didn't receive any items, then we need to send the
        // user a message so s/he know that something is indeed working
        if (!included && args.hasFlag('s')) {
            sender.sendMessage(ChatColor.YELLOW.toString() + "Players slayed.");
        }
    }
}
