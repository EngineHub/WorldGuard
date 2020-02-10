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
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.component.MessageBox;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.paste.ActorCallbackPaste;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.util.report.SystemInfoReport;
import com.sk89q.worldedit.util.task.FutureForwardingTask;
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
import com.sk89q.worldguard.util.report.ApplicableRegionsReport;
import com.sk89q.worldguard.util.report.ConfigReport;

import java.io.File;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
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
        sender.print("§a>> §7Версия WorldGuard " + WorldGuard.getVersion());
        sender.print("§a>> §7http://www.enginehub.org");
        sender.print("§a>> §7Перевел томатный админ DarkFort");
        sender.print("§a>> §bVK: https://vk.com/darkfortmc | TG: https://t.me/darkfort");

        sender.printDebug("----------- Платформа -----------");
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
        if (sender instanceof LocalPlayer) {
            report.add(new ApplicableRegionsReport((LocalPlayer) sender));
        }
        String result = report.toString();

        try {
            File dest = new File(worldGuard.getPlatform().getConfigDir().toFile(), "report.txt");
            Files.write(result, dest, StandardCharsets.UTF_8);
            sender.print("Отчет WorldGuard был сохранен в файл " + dest.getAbsolutePath());
        } catch (IOException e) {
            throw new CommandException("Ошибка записи файла: " + e.getMessage());
        }
        
        if (args.hasFlag('p')) {
            sender.checkPermission("worldguard.report.pastebin");
            ActorCallbackPaste.pastebin(worldGuard.getSupervisor(), sender, result, "Отчет WorldGuard: %s.report");
        }
    }

    @Command(aliases = {"profile"}, usage = "[-p] [-i <interval>] [-t <thread filter>] [<minutes>]",
            desc = "Профилирование использования CPU сервера", min = 0, max = 1,
            flags = "t:i:p")
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

        int interval = 20;
        if (args.hasFlag('i')) {
            interval = args.getFlagInteger('i');
            if (interval < 1 || interval > 100) {
                throw new CommandException("Интервал должен быть в пределах от 1 до 100 (в миллисекундах)");
            }
            if (interval < 10) {
                sender.printDebug("Заметка: Низкий интервал может вызвать дополнительное замедление во время профилирования.");
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
            builder.setInterval(interval);
            sampler = activeSampler = builder.start();
        }

        sender.print(TextComponent.of("Запуск профилирования CPU. Результаты будут доступны через " + minutes + " минут.", TextColor.LIGHT_PURPLE)
                .append(TextComponent.newline())
                .append(TextComponent.of("Используйте ", TextColor.GRAY))
                .append(TextComponent.of("/wg stopprofile", TextColor.AQUA)
                        .clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, "/wg stopprofile")))
                .append(TextComponent.of(" в любое время, чтобы отменить профилирование CPU.", TextColor.GRAY)));

        worldGuard.getSupervisor().monitor(FutureForwardingTask.create(
                sampler.getFuture(), "Результаты профилирования CPU будут доступны через " + minutes + " минут", sender));

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
                    Files.write(output, dest, StandardCharsets.UTF_8);
                    sender.print("Данные профилирования CPU записаны в " + dest.getAbsolutePath());
                } catch (IOException e) {
                    sender.printError("Не удалось записать данные профилирования CPU: " + e.getMessage());
                }

                if (pastebin) {
                    ActorCallbackPaste.pastebin(worldGuard.getSupervisor(), sender, output, "Результат профилирования: %s.profile");
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

        if (tasks.isEmpty()) {
            sender.print("В настоящее время нет запущенных задач.");
        } else {
            tasks.sort(new TaskStateComparator());
            MessageBox builder = new MessageBox("Выполнение задач", new TextComponentProducer());
            builder.append(TextComponent.of("Заметка: Некоторые 'запущенные' задачи могут ждать начала.", TextColor.GRAY));
            for (Task<?> task : tasks) {
                builder.append(TextComponent.newline());
                builder.append(TextComponent.of("(" + task.getState().name() + ") ", TextColor.BLUE));
                builder.append(TextComponent.of(CommandUtils.getOwnerName(task.getOwner()) + ": ", TextColor.YELLOW));
                builder.append(TextComponent.of(task.getName(), TextColor.WHITE));
            }
            sender.print(builder.create());
        }
    }

    @Command(aliases = {"debug"}, desc = "Команды отладки")
    @NestedCommand({DebuggingCommands.class})
    public void debug(CommandContext args, Actor sender) {}

}
