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
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.util.logging.LoggerToChatHandler;
import com.sk89q.worldguard.bukkit.util.report.PerformanceReport;
import com.sk89q.worldguard.bukkit.util.report.PluginReport;
import com.sk89q.worldguard.bukkit.util.report.SchedulerReport;
import com.sk89q.worldguard.bukkit.util.report.ServerReport;
import com.sk89q.worldguard.bukkit.util.report.ServicesReport;
import com.sk89q.worldguard.bukkit.util.report.WorldReport;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.util.profiler.SamplerBuilder;
import com.sk89q.worldguard.util.profiler.SamplerBuilder.Sampler;
import com.sk89q.worldguard.util.profiler.ThreadIdFilter;
import com.sk89q.worldguard.util.profiler.ThreadNameFilter;
import com.sk89q.worldguard.util.report.ConfigReport;
import com.sk89q.worldguard.util.task.Task;
import com.sk89q.worldguard.util.task.TaskStateComparator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

public class WorldGuardCommands {

    private static final Logger log = Logger.getLogger(WorldGuardCommands.class.getCanonicalName());

    private final WorldGuardPlugin plugin;
    @Nullable
    private Sampler activeSampler;

    public WorldGuardCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"version"}, desc = "Показать версию WorldGuard", max = 0)
    public void version(CommandContext args, CommandSender sender) throws CommandException {
        sender.sendMessage(ChatColor.GREEN
                + "Версия WorldGuard " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GREEN
                + "https://vk.com/darkfortmc §7§l| §7Перевел §eDarkFort");
    }

    @Command(aliases = {"reload"}, desc = "Перезагрузить конфигурацию WorldGuard", max = 0)
    @CommandPermissions({"worldguard.reload"})
    public void reload(CommandContext args, CommandSender sender) throws CommandException {
        // TODO: This is subject to a race condition, but at least other commands are not being processed concurrently
        List<Task<?>> tasks = WorldGuard.getInstance().getSupervisor().getTasks();
        if (!tasks.isEmpty()) {
            throw new CommandException("В настоящее время есть нерешенные задачи. Используйте /wg runnig для посмотра этих задач.");
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
            ConfigurationManager config = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
            config.unload();
            config.load();
            for (World world : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
                config.get(world);
            }
            WorldGuard.getInstance().getPlatform().getRegionContainer().reload();
            // WGBukkit.cleanCache();
            sender.sendMessage("Конфигурация WorldGuard перезагружена.");
        } catch (Throwable t) {
            sender.sendMessage("Ошибка перезагрузки WorldGuard: "
                    + t.getMessage());
        } finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }
    }

    @Command(aliases = {"report"}, desc = "Отослать отчет об ошибке WorldGuard", flags = "p", max = 0)
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
        report.add(new ConfigReport());
        String result = report.toString();

        try {
            File dest = new File(plugin.getDataFolder(), "report.txt");
            Files.write(result, dest, Charset.forName("UTF-8"));
            sender.sendMessage(ChatColor.YELLOW + "Отчет WorldGuard был сохранен в файл " + dest.getAbsolutePath());
        } catch (IOException e) {
            throw new CommandException("Ошибка записи файла: " + e.getMessage());
        }

        if (args.hasFlag('p')) {
            plugin.checkPermission(sender, "worldguard.report.pastebin");
            CommandUtils.pastebin(plugin, sender, result, "Отчет WorldGuard: %s.report");
        }
    }

    @Command(aliases = {"profile"}, usage = "[<минут>]",
            desc = "Профиль использования CPU сервера", min = 0, max = 1,
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
            threadFilter = thread -> true;
        } else {
            threadFilter = new ThreadNameFilter(threadName);
        }

        int minutes;
        if (args.argsLength() == 0) {
            minutes = 5;
        } else {
            minutes = args.getInteger(0);
            if (minutes < 1) {
                throw new CommandException("Вы должны управлять профилем по крайней мере в течении 1 минуты.");
            } else if (minutes > 10) {
                throw new CommandException("Профилировать можно максимум 10 минут.");
            }
        }

        Sampler sampler;

        synchronized (this) {
            if (activeSampler != null) {
                throw new CommandException("Профиль в настоящее время работает! Используйте /wg stopprofile чтобы остановить текущий профиль.");
            }

            SamplerBuilder builder = new SamplerBuilder();
            builder.setThreadFilter(threadFilter);
            builder.setRunTime(minutes, TimeUnit.MINUTES);
            sampler = activeSampler = builder.start();
        }

        AsyncCommandHelper.wrap(sampler.getFuture(), plugin, sender)
                .formatUsing(minutes)
                .registerWithSupervisor("Используется CPU профилированием на %d минут(ы)...")
                .sendMessageAfterDelay("(Подождите... профилирование на %d минут(ы)...)")
                .thenTellErrorsOnly("CPU профилирование не удалось.");

        sampler.getFuture().addListener(() -> {
            synchronized (WorldGuardCommands.this) {
                activeSampler = null;
            }
        }, MoreExecutors.directExecutor());

        Futures.addCallback(sampler.getFuture(), new FutureCallback<Sampler>() {
            @Override
            public void onSuccess(Sampler result) {
                String output = result.toString();

                try {
                    File dest = new File(plugin.getDataFolder(), "profile.txt");
                    Files.write(output, dest, Charset.forName("UTF-8"));
                    sender.sendMessage(ChatColor.YELLOW + "CPU профилирующие данные записанны " + dest.getAbsolutePath());
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "Не удалось записать данные профилирования CPU: " + e.getMessage());
                }

                if (pastebin) {
                    CommandUtils.pastebin(plugin, sender, output, "Результат профиля: %s.profile");
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    @Command(aliases = {"stopprofile"}, usage = "",desc = "Остановить запущенный профиль", min = 0, max = 0)
    @CommandPermissions("worldguard.profile")
    public void stopProfile(CommandContext args, final CommandSender sender) throws CommandException {
        synchronized (this) {
            if (activeSampler == null) {
                throw new CommandException("Ни один из профилей CPU в настоящее время не работает.");
            }

            activeSampler.cancel();
            activeSampler = null;
        }

        sender.sendMessage("Профиль CPU остановлен.");
    }

    @Command(aliases = {"flushstates", "clearstates"},
            usage = "[игрок]", desc = "Сбрасывает состояние менеджера", max = 1)
    @CommandPermissions("worldguard.flushstates")
    public void flushStates(CommandContext args, CommandSender sender) throws CommandException {
        if (args.argsLength() == 0) {
            WorldGuard.getInstance().getPlatform().getSessionManager().resetAllStates();
            sender.sendMessage("Все состояния сброшены.");
        } else {
            Player player = plugin.getServer().getPlayer(args.getString(0));
            if (player != null) {
                LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
                WorldGuard.getInstance().getPlatform().getSessionManager().resetState(localPlayer);
                sender.sendMessage("Все состояния игрока \"" + localPlayer.getName() + "\" сброшены.");
            }
        }
    }

    @Command(aliases = {"running", "queue"}, desc = "Список запущенных задач", max = 0)
    @CommandPermissions("worldguard.running")
    public void listRunningTasks(CommandContext args, CommandSender sender) throws CommandException {
        List<Task<?>> tasks = WorldGuard.getInstance().getSupervisor().getTasks();

        if (!tasks.isEmpty()) {
            Collections.sort(tasks, new TaskStateComparator());
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.GRAY);
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append(" Запущенные задачи ");
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append("\n").append(ChatColor.GRAY).append("Примечание: Некоторые 'бегущие' задачи могут ждать, чтобы запуститься.");
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
            sender.sendMessage(ChatColor.YELLOW + "Сейчас нет запущеных задач.");
        }
    }

    @Command(aliases = {"debug"}, desc = "Команды отладки")
    @NestedCommand({DebuggingCommands.class})
    public void debug(CommandContext args, CommandSender sender) {}

}
