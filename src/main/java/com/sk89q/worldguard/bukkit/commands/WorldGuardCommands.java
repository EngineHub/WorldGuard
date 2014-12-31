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

import com.google.common.io.Files;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.logging.LoggerToChatHandler;
import com.sk89q.worldguard.bukkit.util.report.*;
import com.sk89q.worldguard.util.paste.Pastebin;
import com.sk89q.worldguard.util.report.ReportList;
import com.sk89q.worldguard.util.report.SystemInfoReport;
import com.sk89q.worldguard.util.task.Task;
import com.sk89q.worldguard.util.task.TaskStateComparator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldGuardCommands {

    private static final Logger log = Logger.getLogger(WorldGuardCommands.class.getCanonicalName());

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
            minecraftLogger = Logger.getLogger("com.sk89q.worldguard");
            minecraftLogger.addHandler(handler);
        }

        try {
            ConfigurationManager config = plugin.getGlobalStateManager();
            config.unload();
            config.load();
            for (World world : Bukkit.getServer().getWorlds()) {
                config.get(world);
            }
            plugin.getRegionContainer().reload();
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
        ReportList report = new ReportList("Report");
        report.add(new SystemInfoReport());
        report.add(new ServerReport());
        report.add(new PluginReport());
        report.add(new SchedulerReport());
        report.add(new ServicesReport());
        report.add(new WorldReport());
        report.add(new PerformanceReport());
        report.add(new ConfigReport(plugin));
        String result = report.toString();

        try {
            File dest = new File(plugin.getDataFolder(), "report.txt");
            Files.write(result, dest, Charset.forName("UTF-8"));
            sender.sendMessage(ChatColor.YELLOW + "WorldGuard report written to " + dest.getAbsolutePath());
        } catch (IOException e) {
            throw new CommandException("Failed to write report: " + e.getMessage());
        }
        
        if (args.hasFlag('p')) {
            plugin.checkPermission(sender, "worldguard.report.pastebin");
            
            sender.sendMessage(ChatColor.YELLOW + "Now uploading to Pastebin...");

            Futures.addCallback(new Pastebin().paste(result), new FutureCallback<URL>() {
                @Override
                public void onSuccess(URL url) {
                    sender.sendMessage(ChatColor.YELLOW + "WorldGuard report: " + url);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    log.log(Level.WARNING, "Failed to submit pastebin", throwable);
                    sender.sendMessage(ChatColor.RED + "The WorldGuard report could not be saved to a pastebin service. Please see console for the error.");
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
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append(" Running tasks ");
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
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

    @Command(aliases = {"debug"}, desc = "Debugging commands")
    @NestedCommand({DebuggingCommands.class})
    public void debug(CommandContext args, CommandSender sender) {}

}
