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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.io.Files;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.logging.LoggerToChatHandler;
import com.sk89q.worldguard.bukkit.util.report.*;
import com.sk89q.worldguard.util.profiler.SamplerBuilder;
import com.sk89q.worldguard.util.profiler.SamplerBuilder.Sampler;
import com.sk89q.worldguard.util.profiler.ThreadIdFilter;
import com.sk89q.worldguard.util.profiler.ThreadNameFilter;
import com.sk89q.worldguard.util.report.ReportList;
import com.sk89q.worldguard.util.report.SystemInfoReport;
import com.sk89q.worldguard.util.task.Task;
import com.sk89q.worldguard.util.task.TaskStateComparator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldGuardCommands {

    private static final Logger log = Logger.getLogger(WorldGuardCommands.class.getCanonicalName());

    private final WorldGuardPlugin plugin;
    @Nullable
    private Sampler activeSampler;

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
            CommandUtils.pastebin(plugin, sender, result, "WorldGuard report: %s.report");
        }
    }

    @Command(aliases = {"profile"}, usage = "[<minutes>]",
            desc = "Profile the CPU usage of the server", min = 0, max = 1,
            flags = "t:p")
    @CommandPermissions("worldguard.profile")
    public void profile(final CommandContext args, final CommandSender sender) throws CommandException {
        Predicate<ThreadInfo> threadFilter;
        String threadName = args.getFlag('t');
        final boolean pastebin;

        if (args.hasFlag('p')) {
            plugin.checkPermission(sender, "worldguard.report.pastebin");
            pastebin = true;
        } else {
            pastebin = false;
        }

        if (threadName == null) {
            threadFilter = new ThreadIdFilter(Thread.currentThread().getId());
        } else if (threadName.equals("*")) {
            threadFilter = Predicates.alwaysTrue();
        } else {
            threadFilter = new ThreadNameFilter(threadName);
        }

        int minutes;
        if (args.argsLength() == 0) {
            minutes = 5;
        } else {
            minutes = args.getInteger(0);
            if (minutes < 1) {
                throw new CommandException("You must run the profile for at least 1 minute.");
            } else if (minutes > 10) {
                throw new CommandException("You can profile for, at maximum, 10 minutes.");
            }
        }

        Sampler sampler;

        synchronized (this) {
            if (activeSampler != null) {
                throw new CommandException("A profile is currently in progress! Please use /wg stopprofile to stop the current profile.");
            }

            SamplerBuilder builder = new SamplerBuilder();
            builder.setThreadFilter(threadFilter);
            builder.setRunTime(minutes, TimeUnit.MINUTES);
            sampler = activeSampler = builder.start();
        }

        AsyncCommandHelper.wrap(sampler.getFuture(), plugin, sender)
                .formatUsing(minutes)
                .registerWithSupervisor("Running CPU profiler for %d minute(s)...")
                .sendMessageAfterDelay("(Please wait... profiling for %d minute(s)...)")
                .thenTellErrorsOnly("CPU profiling failed.");

        sampler.getFuture().addListener(new Runnable() {
            @Override
            public void run() {
                synchronized (WorldGuardCommands.this) {
                    activeSampler = null;
                }
            }
        }, MoreExecutors.sameThreadExecutor());

        Futures.addCallback(sampler.getFuture(), new FutureCallback<Sampler>() {
            @Override
            public void onSuccess(Sampler result) {
                String output = result.toString();

                try {
                    File dest = new File(plugin.getDataFolder(), "profile.txt");
                    Files.write(output, dest, Charset.forName("UTF-8"));
                    sender.sendMessage(ChatColor.YELLOW + "CPU profiling data written to " + dest.getAbsolutePath());
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "Failed to write CPU profiling data: " + e.getMessage());
                }

                if (pastebin) {
                    CommandUtils.pastebin(plugin, sender, output, "Profile result: %s.profile");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    @Command(aliases = {"stopprofile"}, usage = "",desc = "Stop a running profile", min = 0, max = 0)
    @CommandPermissions("worldguard.profile")
    public void stopProfile(CommandContext args, final CommandSender sender) throws CommandException {
        synchronized (this) {
            if (activeSampler == null) {
                throw new CommandException("No CPU profile is currently running.");
            }

            activeSampler.cancel();
            activeSampler = null;
        }

        sender.sendMessage("The running CPU profile has been stopped.");
    }

    @Command(aliases = {"flushstates", "clearstates"},
            usage = "[player]", desc = "Flush the state manager", max = 1)
    @CommandPermissions("worldguard.flushstates")
    public void flushStates(CommandContext args, CommandSender sender) throws CommandException {
        if (args.argsLength() == 0) {
            plugin.getSessionManager().resetAllStates();
            sender.sendMessage("Cleared all states.");
        } else {
            Player player = plugin.getServer().getPlayer(args.getString(0));
            if (player != null) {
                plugin.getSessionManager().resetState(player);
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
