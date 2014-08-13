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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.odeum.task.Task;
import com.sk89q.odeum.task.TaskStateComparator;
import com.sk89q.worldguard.bukkit.LoggerToChatHandler;
import com.sk89q.worldguard.bukkit.ReportWriter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.util.PastebinPoster;
import com.sk89q.worldguard.util.PastebinPoster.PasteCallback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldGuardCommands {
    private final WorldGuardPlugin plugin;

    public WorldGuardCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"version"}, desc = "Get the WorldGuard version", max = 0)
    public void version(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.YELLOW
                + "WorldGuard " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW
                + "http://www.sk89q.com");
    }

    @Command(aliases = {"reload"}, desc = "Reload WorldGuard configuration", max = 0)
    @CommandPermissions({"worldguard.reload"})
    public void reload(CommandContext args, CommandSender sender) throws CommandException {
        // TODO: This is subject to a race condition, but at least other commands are not being processed concurrently
        List<Task<?>> tasks = plugin.getSupervisor().getTasks();
        if (!tasks.isEmpty()) {
            throw new CommandException("There are currently pending tasks. Use /wg running to monitor these tasks first.");
        }
        
        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;
        
        if (sender instanceof Player) {
            handler = new LoggerToChatHandler(sender);
            handler.setLevel(Level.ALL);
            minecraftLogger = Logger.getLogger("Minecraft");
            minecraftLogger.addHandler(handler);
        }

        try {
            plugin.getGlobalStateManager().unload();
            plugin.getGlobalRegionManager().unloadAll();
            plugin.getGlobalStateManager().load();
            plugin.getGlobalRegionManager().loadAll(Bukkit.getServer().getWorlds());
            // WGBukkit.cleanCache();
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
    
    @Command(aliases = {"report"}, desc = "Writes a report on WorldGuard", flags = "p", max = 0)
    @CommandPermissions({"worldguard.report"})
    public void report(CommandContext args, final CommandSender sender) throws CommandException {
        
        File dest = new File(plugin.getDataFolder(), "report.txt");
        ReportWriter report = new ReportWriter(plugin);
        
        try {
            report.write(dest);
            sender.sendMessage(ChatColor.YELLOW + "WorldGuard report written to "
                    + dest.getAbsolutePath());
        } catch (IOException e) {
            throw new CommandException("Failed to write report: " + e.getMessage());
        }
        
        if (args.hasFlag('p')) {
            plugin.checkPermission(sender, "worldguard.report.pastebin");
            
            sender.sendMessage(ChatColor.YELLOW + "Now uploading to Pastebin...");
            PastebinPoster.paste(report.toString(), new PasteCallback() {
                
                public void handleSuccess(String url) {
                    // Hope we don't have a thread safety issue here
                    sender.sendMessage(ChatColor.YELLOW + "WorldGuard report (1 hour): " + url);
                }
                
                public void handleError(String err) {
                    // Hope we don't have a thread safety issue here
                    sender.sendMessage(ChatColor.YELLOW + "WorldGuard report pastebin error: " + err);
                }
            });
        }

    }

    @Command(aliases = {"flushstates", "clearstates"},
            usage = "[player]", desc = "Flush the state manager", max = 1)
    @CommandPermissions("worldguard.flushstates")
    public void flushStates(CommandContext args, CommandSender sender) throws CommandException {
        if (args.argsLength() == 0) {
            plugin.getFlagStateManager().forgetAll();
            sender.sendMessage("Cleared all states.");
        } else {
            Player player = plugin.getServer().getPlayer(args.getString(0));
            if (player != null) {
                plugin.getFlagStateManager().forget(player);
                sender.sendMessage("Cleared states for player \"" + player.getName() + "\".");
            }
        }
    }

    @Command(aliases = {"running", "queue"}, desc = "List running tasks", max = 0)
    @CommandPermissions("worldguard.running")
    public void listRunningTasks(CommandContext args, CommandSender sender) throws CommandException {
        List<Task<?>> tasks = plugin.getSupervisor().getTasks();

        if (!tasks.isEmpty()) {
            Collections.sort(tasks, new TaskStateComparator());
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.GRAY);
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append(" Running tasks ");
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append("\n").append(ChatColor.GRAY).append("Note: Some 'running' tasks may be waiting to be start.");
            for (Task task : tasks) {
                builder.append("\n");
                builder.append(ChatColor.BLUE).append("(").append(task.getState().name()).append(") ");
                builder.append(ChatColor.YELLOW);
                builder.append(CommandUtils.getOwnerName(task.getOwner()));
                builder.append(": ");
                builder.append(ChatColor.WHITE);
                builder.append(task.getName());
            }
            sender.sendMessage(builder.toString());
        } else {
            sender.sendMessage(ChatColor.YELLOW + "There are currently no running tasks.");
        }
    }

}
