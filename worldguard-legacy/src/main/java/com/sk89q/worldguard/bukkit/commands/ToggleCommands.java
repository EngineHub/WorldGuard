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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
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
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ToggleCommands {
    private final WorldGuardPlugin plugin;

    public ToggleCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"stopfire"}, usage = "[<мир>]",
            desc = "Отключить распространение огня", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void stopFire(CommandContext args, CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        BukkitWorldConfiguration wcfg =
                (BukkitWorldConfiguration) WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world));

        if (!wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(
                    ChatColor.YELLOW
                    + "Распространение огня в мире '" + world.getName() + "' было запрещено администратором "
                    + plugin.toName(sender) + " ");
        } else {
            sender.sendMessage(
                    ChatColor.YELLOW
                    + "Распространение огня запрещено во всех мирах.");
        }

        wcfg.fireSpreadDisableToggle = true;
    }

    @Command(aliases = {"allowfire"}, usage = "[<мир>]",
            desc = "Разрешить распространение огня", max = 1)
    @CommandPermissions({"worldguard.fire-toggle.stop"})
    public void allowFire(CommandContext args, CommandSender sender) throws CommandException {
        
        World world;
        
        if (args.argsLength() == 0) {
            world = plugin.checkPlayer(sender).getWorld();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
        }
        
        BukkitWorldConfiguration wcfg =
                (BukkitWorldConfiguration) WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world));

        if (wcfg.fireSpreadDisableToggle) {
            plugin.getServer().broadcastMessage(ChatColor.YELLOW
                    + "Распространение огня в мире '" + world.getName() + "' было разрешено администратором "
                    + plugin.toName(sender) + " ");
        } else {
            sender.sendMessage(ChatColor.YELLOW
                    + "Распространение огня разрешено во всех мирах.");
        }

        wcfg.fireSpreadDisableToggle = false;
    }

    @Command(aliases = {"halt-activity", "stoplag", "haltactivity"},
            desc = "Остановить//Возобновить всю активность на сервере", flags = "cis", max = 0)
    @CommandPermissions({"worldguard.halt-activity"})
    public void stopLag(CommandContext args, CommandSender sender) throws CommandException {

        ConfigurationManager configManager = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        if (args.hasFlag('i')) {
            if (configManager.activityHaltToggle) {
                 sender.sendMessage(ChatColor.YELLOW
                         + "Вся активность на сервере была остановлена.");
            } else {
                 sender.sendMessage(ChatColor.YELLOW
                         + "Вся активность на сервере была возобновлена.");
            }
        } else {
            configManager.activityHaltToggle = !args.hasFlag('c');

            if (configManager.activityHaltToggle) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.YELLOW
                            + "Вся активность на сервере была остановлена.");
                }

                if (!args.hasFlag('s')) {
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW
                             + "Вся активность на сервере была возобновлена администратором "
                             + plugin.toName(sender) + " ");
                } else {
                    sender.sendMessage(ChatColor.YELLOW
                        + "(Бесшумно) Вся активность на сервере была возобновлена администратором "
                        + plugin.toName(sender) + " ");
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
                        sender.sendMessage("" + removed + " (>10) было удалено в мире "
                                + world.getName());
                    }
                }
            } else {
                if (!args.hasFlag('s')) {
                    plugin.getServer().broadcastMessage(ChatColor.YELLOW
                            + "Вся интенсивная активность на сервере теперь возобновлена.");
                    
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.YELLOW
                                + "Вся интенсивная активность на сервере теперь возобновлена.");
                    }
                } else {
                    sender.sendMessage(ChatColor.YELLOW
                            + "(Бесшумно) Вся интенсивная активность на сервере теперь возобновлена.");
                }
            }
        }
    }
}
