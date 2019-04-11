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

package com.sk89q.worldguard.commands;

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
import com.sk89q.worldedit.command.util.AsyncCommandHelper;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import com.sk89q.worldedit.util.task.Task;
import com.sk89q.worldedit.util.task.TaskStateComparator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.util.logging.LoggerToChatHandler;
import com.sk89q.worldguard.util.profiler.SamplerBuilder;
import com.sk89q.worldguard.util.profiler.SamplerBuilder.Sampler;
import com.sk89q.worldguard.util.profiler.ThreadIdFilter;
import com.sk89q.worldguard.util.profiler.ThreadNameFilter;
import com.sk89q.worldguard.util.report.ConfigReport;

import java.io.File;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

public class WorldGuardCommands {

    private final WorldGuard worldGuard;
    @Nullable
    private Sampler activeSampler;

    public WorldGuardCommands(WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Command(aliases = {"version"}, desc = "Показать версию WorldGuard", max = 0)
    public void version(CommandContext args, Actor sender) throws CommandException {
        sender.print("Версия WorldGuard " + WorldGuard.getVersion());
        sender.print("http://www.enginehub.org");
        sender.print("§7Перевел §eDarkFort");
        sender.print("https://vk.com/darkfortmc");

        sender.printDebug("----------- Платформы -----------");
        sender.printDebug(String.format("* %s (%s)", worldGuard.getPlatform().getPlatformName(), worldGuard.getPlatform().getPlatformVersion()));
    }

    @Command(aliases = {"reload"}, desc = "Перезагрузить конфигурацию WorldGuard", max = 0)
    @CommandPermissions({"worldguard.reload"})
    public void reload(CommandContext args, Actor sender) throws CommandException {
        // TODO: This is subject to a race condition, but at least other commands are not being processed concurrently
        List<Task<?>> tasks = WorldGuard.getInstance().getSupervisor().getTasks();
        if (!tasks.isEmpty()) {
            throw new CommandException("В настоящее время есть нерешенные задачи. Используйте /wg runnig для посмотра этих задач.");
        }
        
        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;
        
        if (sender instanceof LocalPlayer) {
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
            sender.print("Конфигурация WorldGuard перезагружена.");
        } catch (Throwable t) {
            sender.printError("Ошибка перезагрузки WorldGuard: " + t.getMessage());
        } finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }
    }
    
    @Command(aliases = {"report"}, desc = "Отослать отчет об ошибке WorldGuard", flags = "p", max = 0)
    @CommandPermissions({"worldguard.report"})
    public void report(CommandContext args, final Actor sender) throws CommandException, AuthorizationException {
        ReportList report = new ReportList("Report");
        worldGuard.getPlatform().addPlatformReports(report);
        report.add(new SystemInfoReport());
        report.add(new ConfigReport());
        String result = report.toString();

        try {
            File dest = new File(worldGuard.getPlatform().getConfigDir().toFile(), "report.txt");
            Files.write(result, dest, Charset.forName("UTF-8"));
            sender.print("Отчет WorldGuard был сохранен в файл " + dest.getAbsolutePath());
        } catch (IOException e) {
            throw new CommandException("Ошибка записи файла: " + e.getMessage());
        }
        
        if (args.hasFlag('p')) {
            sender.checkPermission("worldguard.report.pastebin");
            ActorCallbackPaste.pastebin(worldGuard.getSupervisor(), sender, result, "Отчет WorldGuard: %s.report", worldGuard.getExceptionConverter());
        }
    }

    @Command(aliases = {"profile"}, usage = "[<минуты>]",
            desc = "Профилирование использования CPU сервера", min = 0, max = 1,
            flags = "t:p")
    @CommandPermissions("worldguard.profile")
    public void profile(final CommandContext args, final Actor sender) throws CommandException, AuthorizationException {
        Predicate<ThreadInfo> threadFilter;
        String threadName = args.getFlag('t');
        final boolean pastebin;

        if (args.hasFlag('p')) {
            sender.checkPermission("worldguard.report.pastebin");
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
                throw new CommandException("Профилирование должно быть включено не менее 1 минуты.");
            } else if (minutes > 10) {
                throw new CommandException("Профилировать можно максимум 10 минут.");
            }
        }

        Sampler sampler;

        synchronized (this) {
            if (activeSampler != null) {
                throw new CommandException("Профилирование в настоящее время запущено! Пожалуйста, используйте /wg stopprofile для остановки текущего профилирования.");
            }

            SamplerBuilder builder = new SamplerBuilder();
            builder.setThreadFilter(threadFilter);
            builder.setRunTime(minutes, TimeUnit.MINUTES);
            sampler = activeSampler = builder.start();
        }

        AsyncCommandHelper.wrap(sampler.getFuture(), worldGuard.getSupervisor(), sender, worldGuard.getExceptionConverter())
                .formatUsing(minutes)
                .registerWithSupervisor("Используется CPU профилированием на %d минут(ы)...")
                .sendMessageAfterDelay("(Подождите... профилирование на %d минут(ы)...)")
                .thenTellErrorsOnly("Профилирование CPU не удалось.");

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
                    File dest = new File(worldGuard.getPlatform().getConfigDir().toFile(), "profile.txt");
                    Files.write(output, dest, Charset.forName("UTF-8"));
                    sender.print("Данные профилирования CPU записаны в " + dest.getAbsolutePath());
                } catch (IOException e) {
                    sender.printError("Не удалось записать данные профилирования CPU: " + e.getMessage());
                }

                if (pastebin) {
                    ActorCallbackPaste.pastebin(worldGuard.getSupervisor(), sender, output, "Profile result: %s.profile", worldGuard.getExceptionConverter());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
            }
        });
    }

    @Command(aliases = {"stopprofile"}, usage = "",desc = "Остановить запущенное профилирование", min = 0, max = 0)
    @CommandPermissions("worldguard.profile")
    public void stopProfile(CommandContext args, final Actor sender) throws CommandException {
        synchronized (this) {
            if (activeSampler == null) {
                throw new CommandException("Профилирование CPU в данный момент не запущено.");
            }

            activeSampler.cancel();
            activeSampler = null;
        }

        sender.print("Запущенное профилирование CPU остановлено.");
    }

    @Command(aliases = {"flushstates", "clearstates"},
            usage = "[player]", desc = "Сброс состояния менеджера", max = 1)
    @CommandPermissions("worldguard.flushstates")
    public void flushStates(CommandContext args, Actor sender) throws CommandException {
        if (args.argsLength() == 0) {
            WorldGuard.getInstance().getPlatform().getSessionManager().resetAllStates();
            sender.print("Очищены все состояния.");
        } else {
            LocalPlayer player = worldGuard.getPlatform().getMatcher().matchSinglePlayer(sender, args.getString(0));
            if (player != null) {
                WorldGuard.getInstance().getPlatform().getSessionManager().resetState(player);
                sender.print("Очищены состояния для игрока \"" + player.getName() + "\".");
            }
        }
    }

    @Command(aliases = {"running", "queue"}, desc = "Список выполняющихся задач", max = 0)
    @CommandPermissions("worldguard.running")
    public void listRunningTasks(CommandContext args, Actor sender) throws CommandException {
        List<Task<?>> tasks = WorldGuard.getInstance().getSupervisor().getTasks();

        if (!tasks.isEmpty()) {
            tasks.sort(new TaskStateComparator());
            StringBuilder builder = new StringBuilder();
            builder.append(Style.GRAY);
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append(" Выполнение задач ");
            builder.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
            builder.append("\n").append(Style.GRAY).append("Примечание: Некоторые 'бегущие' задачи могут ожидать запуска.");
            for (Task task : tasks) {
                builder.append("\n");
                builder.append(Style.BLUE).append("(").append(task.getState().name()).append(") ");
                builder.append(Style.YELLOW);
                builder.append(CommandUtils.getOwnerName(task.getOwner()));
                builder.append(": ");
                builder.append(Style.WHITE);
                builder.append(task.getName());
            }
            sender.printRaw(builder.toString());
        } else {
            sender.print("В настоящее время нет запущенных задач.");
        }
    }

    @Command(aliases = {"debug"}, desc = "Команды отладки")
    @NestedCommand({DebuggingCommands.class})
    public void debug(CommandContext args, Actor sender) {}

}
