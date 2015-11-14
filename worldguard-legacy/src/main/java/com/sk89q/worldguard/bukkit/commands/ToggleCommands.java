/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.commands;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ToggleCommands {
    private final WorldGuardPlugin plugin;

    public ToggleCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"stopfire"}, usage = "[<world>]",
            desc = "Disables all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void stopFire(CommandContext args, CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(world);

        if (!wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(
                    ChatColor.YELLOW
                    + "Fire spread has been globally disabled for '" + world.getName() + "' by "
                    + plugin.toName(sender) + ".");
        } else {
            sender.sendMessage(
                    ChatColor.YELLOW
                    + "Fire spread was already globally disabled.");
        }

        wcfg.fireSpreadDisableToggle = true;
    }

    @Command(aliases = {"allowfire"}, usage = "[<world>]",
            desc = "Allows all fire spread temporarily", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void allowFire(CommandContext args, CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(world);

        if (wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW
                    + "Fire spread has been globally for '" + world.getName() + "' re-enabled by "
                    + plugin.toName(sender) + ".");
        } else {
            sender.sendMessage(ChatColor.YELLOW
                    + "Fire spread was already globally enabled.");
        }

        wcfg.fireSpreadDisableToggle = false;
    }

    @Command(aliases = {"halt-activity", "stoplag", "haltactivity"},
            desc = "Attempts to cease as much activity in order to stop lag", flags = "cis", max = 0)
    @CommandPermissions({"worldguard.halt-activity"})
    public void stopLag(CommandContext args, CommandSender sender) throws CommandException {

        ConfigurationManager configManager = plugin.getGlobalStateManager();

        if (args.hasFlag('i')) {
            if (configManager.activityHaltToggle) {
                 sender.sendMessage(ChatColor.YELLOW
                         + "ALL intensive server activity is not allowed.");
            } else {
                 sender.sendMessage(ChatColor.YELLOW
                         + "ALL intensive server activity is allowed.");
            }
        } else {
            configManager.activityHaltToggle = !args.hasFlag('c');

            if (configManager.activityHaltToggle) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.YELLOW
                            + "ALL intensive server activity halted.");
                }

                if (!args.hasFlag('s')) {
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW
                             + "ALL intensive server activity halted by "
                             + plugin.toName(sender) + ".");
                } else {
                    sender.sendMessage(ChatColor.YELLOW
                        + "(Silent) ALL intensive server activity halted by "
                        + plugin.toName(sender) + ".");
                }

                for (World world : plugin.getServer().getWorlds()) {
                    int removed = 0;

                    for (Entity entity : world.getEntities()) {
                        if (BukkitUtil.isIntensiveEntity(entity)) {
                            entity.remove();
                            removed++;
                        }
                    }

                    if (removed > 10) {
                        sender.sendMessage("" + removed + " entities (>10) auto-removed from "
                                + world.getName());
                    }
                }
            } else {
                if (!args.hasFlag('s')) {
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW
                            + "ALL intensive server activity is now allowed.");
                    
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.YELLOW
                                + "ALL intensive server activity is now allowed.");
                    }
                } else {
                    sender.sendMessage(ChatColor.YELLOW
                            + "(Silent) ALL intensive server activity is now allowed.");
                }
            }
        }
    }
}
