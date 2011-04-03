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
import org.bukkit.inventory.ItemStack;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldedit.blocks.ItemType;

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
                player.sendMessage(ChatColor.YELLOW + "God mode enabled! Use /ungod to disable.");
                
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
    
    @Command(aliases = {"locate"},
            usage = "[player]",
            desc = "Locate a player",
            flags = "", min = 0, max = 1)
    @CommandPermissions({"worldguard.locate"})
    public static void locate(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        
        if (args.argsLength() == 0) {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            
            sender.sendMessage(ChatColor.YELLOW.toString() + "Compass reset to spawn.");
        } else {
            Player target = plugin.matchSinglePlayer(sender, args.getString(0));
            player.setCompassTarget(target.getLocation());
            
            sender.sendMessage(ChatColor.YELLOW.toString() + "Compass repointed.");
        }
    }
    
    @Command(aliases = {"stack"},
            usage = "",
            desc = "Stack items",
            flags = "", min = 0, max = 0)
    @CommandPermissions({"worldguard.stack"})
    public static void stack(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        
        ItemStack[] items = player.getInventory().getContents();
        int len = items.length;

        int affected = 0;
        
        for (int i = 0; i < len; i++) {
            ItemStack item = items[i];

            // Avoid infinite stacks and stacks with durability
            if (item == null || item.getAmount() <= 0
                    || ItemType.shouldNotStack(item.getTypeId())) {
                continue;
            }

            // Ignore buckets
            if (item.getTypeId() >= 325 && item.getTypeId() <= 327) {
                continue;
            }

            if (item.getAmount() < 64) {
                int needed = 64 - item.getAmount(); // Number of needed items until 64

                // Find another stack of the same type
                for (int j = i + 1; j < len; j++) {
                    ItemStack item2 = items[j];

                    // Avoid infinite stacks and stacks with durability
                    if (item2 == null || item2.getAmount() <= 0
                            || ItemType.shouldNotStack(item.getTypeId())) {
                        continue;
                    }

                    // Same type?
                    // Blocks store their color in the damage value
                    if (item2.getTypeId() == item.getTypeId() &&
                            (!ItemType.usesDamageValue(item.getTypeId())
                                    || item.getDurability() == item2.getDurability())) {
                        // This stack won't fit in the parent stack
                        if (item2.getAmount() > needed) {
                            item.setAmount(64);
                            item2.setAmount(item2.getAmount() - needed);
                            break;
                        // This stack will
                        } else {
                            items[j] = null;
                            item.setAmount(item.getAmount() + item2.getAmount());
                            needed = 64 - item.getAmount();
                        }

                        affected++;
                    }
                }
            }
        }

        if (affected > 0) {
            player.getInventory().setContents(items);
        }

        player.sendMessage(ChatColor.YELLOW + "Items compacted into stacks!");
    }
}
