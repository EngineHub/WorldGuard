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

package com.sk89q.worldguard.commands.region;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.AsyncCommandHelper;
import com.sk89q.worldedit.command.util.FutureProgressListener;
import com.sk89q.worldedit.command.util.MessageFutureCallback.Builder;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.CommandUtils;
import com.sk89q.worldguard.commands.task.RegionAdder;
import com.sk89q.worldguard.commands.task.RegionLister;
import com.sk89q.worldguard.commands.task.RegionManagerReloader;
import com.sk89q.worldguard.commands.task.RegionManagerSaver;
import com.sk89q.worldguard.commands.task.RegionRemover;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.managers.migration.DriverMigration;
import com.sk89q.worldguard.protection.managers.migration.MigrationException;
import com.sk89q.worldguard.protection.managers.migration.UUIDMigration;
import com.sk89q.worldguard.protection.managers.storage.DriverType;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.util.DomainInputResolver.UserLocatorPolicy;
import com.sk89q.worldguard.util.Enums;
import com.sk89q.worldguard.util.logging.LoggerToChatHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the /region commands for WorldGuard.
 */
public final class RegionCommands extends RegionCommandsBase {

    private static final Logger log = Logger.getLogger(RegionCommands.class.getCanonicalName());
    private final WorldGuard worldGuard;

    public RegionCommands(WorldGuard worldGuard) {
        checkNotNull(worldGuard);
        this.worldGuard = worldGuard;
    }
    
    /**
     * Defines a new region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"define", "def", "d", "create"},
             usage = "<id> [<владелец1> [<владелец2> [<владельцы...>]]]",
             flags = "ng",
             desc = "Определить регион",
             min = 1)
    public void define(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);
        LocalPlayer player = worldGuard.checkPlayer(sender);

        // Check permissions
        if (!getPermissionModel(player).mayDefine()) {
            throw new CommandPermissionsException();
        }

        String id = checkRegionId(args.getString(0), false);

        RegionManager manager = checkRegionManager(player.getWorld());

        checkRegionDoesNotExist(manager, id, true);

        ProtectedRegion region;

        if (args.hasFlag('g')) {
            region = new GlobalProtectedRegion(id);
        } else {
            region = checkRegionFromSelection(player, id);
            warnAboutDimensions(player, region);
            informNewUser(player, manager, region);
        }

        RegionAdder task = new RegionAdder(manager, region);
        task.addOwnersFromCommand(args, 2);
        ListenableFuture<?> future = WorldGuard.getInstance().getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), player, worldGuard.getExceptionConverter())
                .formatUsing(id)
                .registerWithSupervisor("Добавление региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите...)")
                .thenRespondWith(
                        "Новый регион '%s' создан.",
                        "Не удалось создать регион '%s'");
    }

    /**
     * Re-defines a region with a new selection.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"redefine", "update", "move"},
             usage = "<id>",
             desc = "Переопределить размер региона",
             flags = "g",
             min = 1, max = 1)
    public void redefine(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        LocalPlayer player = worldGuard.checkPlayer(sender);

        String id = checkRegionId(args.getString(0), false);

        RegionManager manager = checkRegionManager(player.getWorld());

        ProtectedRegion existing = checkExistingRegion(manager, id, false);

        // Check permissions
        if (!getPermissionModel(player).mayRedefine(existing)) {
            throw new CommandPermissionsException();
        }

        ProtectedRegion region;

        if (args.hasFlag('g')) {
            region = new GlobalProtectedRegion(id);
        } else {
            region = checkRegionFromSelection(player, id);
            warnAboutDimensions(player, region);
            informNewUser(player, manager, region);
        }

        region.copyFrom(existing);

        RegionAdder task = new RegionAdder(manager, region);
        ListenableFuture<?> future = WorldGuard.getInstance().getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), player, worldGuard.getExceptionConverter())
                .formatUsing(id)
                .registerWithSupervisor("Обновление региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите...)")
                .thenRespondWith(
                        "Регион '%s' был обновлён до нового размера.",
                        "Не удалось обновить регион '%s'");
    }

    /**
     * Claiming command for users.
     * 
     * <p>This command is a joke and it needs to be rewritten. It was contributed
     * code :(</p>
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"claim"},
             usage = "<id>",
             desc = "Создать регион",
             min = 1, max = 1)
    public void claim(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        LocalPlayer player = worldGuard.checkPlayer(sender);
        RegionPermissionModel permModel = getPermissionModel(player);
        
        // Check permissions
        if (!permModel.mayClaim()) {
            throw new CommandPermissionsException();
        }

        String id = checkRegionId(args.getString(0), false);

        RegionManager manager = checkRegionManager(player.getWorld());

        checkRegionDoesNotExist(manager, id, false);
        ProtectedRegion region = checkRegionFromSelection(player, id);

        WorldConfiguration wcfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(player.getWorld());

        // Check whether the player has created too many regions
        if (!permModel.mayClaimRegionsUnbounded()) {
            int maxRegionCount = wcfg.getMaxRegionCount(player);
            if (maxRegionCount >= 0
                    && manager.getRegionCountOfPlayer(player) >= maxRegionCount) {
                throw new CommandException(
                        "У вас слишком много регионов, удалите ненужный.");
            }
        }

        ProtectedRegion existing = manager.getRegion(id);

        // Check for an existing region
        if (existing != null) {
            if (!existing.getOwners().contains(player)) {
                throw new CommandException(
                        "Такой регион уже существует, и вы не являетесь его владельцем.");
            }
        }

        // We have to check whether this region violates the space of any other reion
        ApplicableRegionSet regions = manager.getApplicableRegions(region);

        // Check if this region overlaps any other region
        if (regions.size() > 0) {
            if (!regions.isOwnerOfAll(player)) {
                throw new CommandException("Этот регион пересекается с чужим регионом.");
            }
        } else {
            if (wcfg.claimOnlyInsideExistingRegions) {
                throw new CommandException("Вы можете приватить только внутри " +
                        "существующих регионов, которые принадлежат вам или вашей группе.");
            }
        }

        if (wcfg.maxClaimVolume >= Integer.MAX_VALUE) {
            throw new CommandException("Этот регион слишком большой. " +
                    "Максимальный размер: " + Integer.MAX_VALUE+ ".");
        }

        // Check claim volume
        if (!permModel.mayClaimRegionsUnbounded()) {
            if (region instanceof ProtectedPolygonalRegion) {
                throw new CommandException("Полигональные регионы не поддерживаются.");
            }

            if (region.volume() > wcfg.maxClaimVolume) {
                player.printError("Вы не можете заприватить регион такого размера.");
                player.printError("Максимальный размер: " + wcfg.maxClaimVolume + ", размер твоего региона: " + region.volume());
                return;
            }
        }

        RegionAdder task = new RegionAdder(manager, region);
        task.setLocatorPolicy(UserLocatorPolicy.UUID_ONLY);
        task.setOwnersInput(new String[]{player.getName()});
        ListenableFuture<?> future = worldGuard.getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), player, worldGuard.getExceptionConverter())
                .formatUsing(id)
                .registerWithSupervisor("Создание региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите...)")
                .thenRespondWith(
                        "Новый регион '%s' создан.",
                        "Не удалось создать регион '%s'");
    }

    /**
     * Get a WorldEdit selection from a region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"select", "sel", "s"},
             usage = "[id]",
             desc = "Выделить регион с помощью WorldEdit",
             min = 0, max = 1)
    public void select(CommandContext args, Actor sender) throws CommandException {
        LocalPlayer player = worldGuard.checkPlayer(sender);
        RegionManager manager = checkRegionManager(player.getWorld());
        ProtectedRegion existing;
        
        // If no arguments were given, get the region that the player is inside
        if (args.argsLength() == 0) {
            existing = checkRegionStandingIn(manager, player);
        } else {
            existing = checkExistingRegion(manager, args.getString(0), false);
        }

        // Check permissions
        if (!getPermissionModel(player).maySelect(existing)) {
            throw new CommandPermissionsException();
        }

        // Select
        setPlayerSelection(player, existing);
    }

    /**
     * Get information about a region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"info", "i"},
             usage = "[id]",
             flags = "usw:",
             desc = "Получить информацию об регионе",
             min = 0, max = 1)
    public void info(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        RegionPermissionModel permModel = getPermissionModel(sender);

        // Lookup the existing region
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion existing;

        if (args.argsLength() == 0) { // Get region from where the player is
            if (!(sender instanceof LocalPlayer)) {
                throw new CommandException("Пожалуйста, укажите регион /region info -w название_мира название_региона.");
            }
            
            existing = checkRegionStandingIn(manager, (LocalPlayer) sender, true);
        } else { // Get region from the ID
            existing = checkExistingRegion(manager, args.getString(0), true);
        }

        // Check permissions
        if (!permModel.mayLookup(existing)) {
            throw new CommandPermissionsException();
        }

        // Let the player select the region
        if (args.hasFlag('s')) {
            // Check permissions
            if (!permModel.maySelect(existing)) {
                throw new CommandPermissionsException();
            }

            setPlayerSelection(worldGuard.checkPlayer(sender), existing);
        }

        // Print region information
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing, args.hasFlag('u') ? null : WorldGuard.getInstance().getProfileCache());
        ListenableFuture<?> future = Futures.transform(
                WorldGuard.getInstance().getExecutorService().submit(printout),
                CommandUtils.messageFunction(sender)::apply);

        // If it takes too long...
        FutureProgressListener.addProgressListener(
                future, sender, "(Пожалуйста, подождите...)");

        // Send a response message
        Futures.addCallback(future,
                new Builder(sender)
                        .exceptionConverter(worldGuard.getExceptionConverter())
                        .onFailure("Не удалось получить информацию о регионе")
                        .build());
    }

    /**
     * List regions.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"list"},
             usage = "[страница]",
             desc = "Показать список всех регионов",
             flags = "np:w:",
             max = 1)
    public void list(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        String ownedBy;

        // Get page
        int page = args.getInteger(0, 1) - 1;
        if (page < 0) {
            page = 0;
        }

        // -p flag to lookup a player's regions
        if (args.hasFlag('p')) {
            ownedBy = args.getFlag('p');
        } else {
            ownedBy = null; // List all regions
        }

        // Check permissions
        if (!getPermissionModel(sender).mayList(ownedBy)) {
            ownedBy = sender.getName(); // assume they only want their own
            if (!getPermissionModel(sender).mayList(ownedBy)) {
                throw new CommandPermissionsException();
            }
        }

        RegionManager manager = checkRegionManager(world);

        RegionLister task = new RegionLister(manager, sender);
        task.setPage(page);
        if (ownedBy != null) {
            task.filterOwnedByName(ownedBy, args.hasFlag('n'));
        }

        ListenableFuture<?> future = WorldGuard.getInstance().getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), sender, worldGuard.getExceptionConverter())
                .registerWithSupervisor("Получение списка регионов...")
                .sendMessageAfterDelay("(Пожалуйста, подождите...)")
                .thenTellErrorsOnly("Не удалось получить список регионов");
    }

    /**
     * Set a flag.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"flag", "f"},
             usage = "<id> <флаг> [-w мир] [-g группа] [значение]",
             flags = "g:w:e",
             desc = "Установить флаг",
             min = 2)
    public void flag(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        String flagName = args.getString(1);
        String value = args.argsLength() >= 3 ? args.getJoinedStrings(2) : null;
        RegionGroup groupValue = null;
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        RegionPermissionModel permModel = getPermissionModel(sender);

        if (args.hasFlag('e')) {
            if (value != null) {
                throw new CommandException("Вы не можете использовать -e со значением флага.");
            }

            value = "";
        }

        // Add color codes
        if (value != null) {
            value = CommandUtils.replaceColorMacros(value);
        }

        // Lookup the existing region
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion existing = checkExistingRegion(manager, args.getString(0), true);

        // Check permissions
        if (!permModel.maySetFlag(existing)) {
            throw new CommandPermissionsException();
        }

        Flag<?> foundFlag = Flags.fuzzyMatchFlag(flagRegistry, flagName);

        // We didn't find the flag, so let's print a list of flags that the user
        // can use, and do nothing afterwards
        if (foundFlag == null) {
            ArrayList<String> flagList = new ArrayList<>();

            // Need to build a list
            for (Flag<?> flag : flagRegistry) {
                // Can the user set this flag?
                if (!permModel.maySetFlag(existing, flag)) {
                    continue;
                }

                flagList.add(flag.getName());
            }

            Collections.sort(flagList);

            StringBuilder list = new StringBuilder();

            for (int i = 0; i < flagList.size(); i++) {
                String flag = flagList.get(i);

                if (i % 2 == 0) {
                    list.append(Style.GRAY);
                } else {
                    list.append(Style.WHITE);
                }

                list.append(flag);
                if ((i + 1) < flagList.size()) {
                    list.append(", ");
                }
            }

            sender.printError("Неизвестный флаг: " + flagName);
            sender.print(Style.YELLOW + "Доступные флаги: " + list);
            
            return;
        }
        
        // Also make sure that we can use this flag
        // This permission is confusing and probably should be replaced, but
        // but not here -- in the model
        if (!permModel.maySetFlag(existing, foundFlag, value)) {
            throw new CommandPermissionsException();
        }

        // -g for group flag
        if (args.hasFlag('g')) {
            String group = args.getFlag('g');
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
            
            if (groupFlag == null) {
                throw new CommandException("Флаг '" + foundFlag.getName()
                        + "' не может быть групповым флагом!");
            }

            // Parse the [-g group] separately so entire command can abort if parsing
            // the [value] part throws an error.
            try {
                groupValue = groupFlag.parseInput(FlagContext.create().setSender(sender).setInput(group).setObject("region", existing).build());
            } catch (InvalidFlagFormat e) {
                throw new CommandException(e.getMessage());
            }

        }

        // Set the flag value if a value was set
        if (value != null) {
            // Set the flag if [value] was given even if [-g group] was given as well
            try {
                setFlag(existing, foundFlag, sender, value);
            } catch (InvalidFlagFormat e) {
                throw new CommandException(e.getMessage());
            }

            sender.print("Флаг " + foundFlag.getName() + " в регионе '" + existing.getId() + "' изменен на '" + Style.stripColor(value) + "'.");
        
        // No value? Clear the flag, if -g isn't specified
        } else if (!args.hasFlag('g')) {
            // Clear the flag only if neither [value] nor [-g group] was given
            existing.setFlag(foundFlag, null);

            // Also clear the associated group flag if one exists
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
            if (groupFlag != null) {
                existing.setFlag(groupFlag, null);
            }

            sender.print("Флаг " + foundFlag.getName() + " удален из региона '" + existing.getId() + "'. (Для групп -g был также удален.)");
        }

        // Now set the group
        if (groupValue != null) {
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();

            // If group set to the default, then clear the group flag
            if (groupValue == groupFlag.getDefault()) {
                existing.setFlag(groupFlag, null);
                sender.print("Групповой флаг '" + foundFlag.getName() + "' был сброшен на значение.");
            } else {
                existing.setFlag(groupFlag, groupValue);
                sender.print("Групповой флаг '" + foundFlag.getName() + "' установлен.");
            }
        }

        // Print region information
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing, null);
        printout.append(Style.GRAY);
        printout.append("(Текущие флаги: ");
        printout.appendFlagsList(false);
        printout.append(")");
        printout.send(sender);
    }

    /**
     * Set the priority of a region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"setpriority", "priority", "pri"},
             usage = "<id> <приоритет>",
             flags = "w:",
             desc = "Установить приоритет региону",
             min = 2, max = 2)
    public void setPriority(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        int priority = args.getInteger(1);

        // Lookup the existing region
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion existing = checkExistingRegion(manager, args.getString(0), false);

        // Check permissions
        if (!getPermissionModel(sender).maySetPriority(existing)) {
            throw new CommandPermissionsException();
        }

        existing.setPriority(priority);

        sender.print("Приоритет региона '" + existing.getId() + "' установлен на " + priority + ".");
    }

    /**
     * Set the parent of a region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"setparent", "parent", "par"},
             usage = "<id> [родительский-id]",
             flags = "w:",
             desc = "Установить родительский регион",
             min = 1, max = 2)
    public void setParent(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        ProtectedRegion parent;
        ProtectedRegion child;

        // Lookup the existing region
        RegionManager manager = checkRegionManager(world);
        
        // Get parent and child
        child = checkExistingRegion(manager, args.getString(0), false);
        if (args.argsLength() == 2) {
            parent = checkExistingRegion(manager, args.getString(1), false);
        } else {
            parent = null;
        }

        // Check permissions
        if (!getPermissionModel(sender).maySetParent(child, parent)) {
            throw new CommandPermissionsException();
        }

        try {
            child.setParent(parent);
        } catch (CircularInheritanceException e) {
            // Tell the user what's wrong
            RegionPrintoutBuilder printout = new RegionPrintoutBuilder(parent, null);
            printout.append(Style.RED);
            assert parent != null;
            printout.append("Регион '" + parent.getId() + "' и так родитель " +
                    "региона '" + child.getId() + "', это призведет к зацикливанию.\n");
            printout.append(Style.GRAY);
            printout.append("(Текущее родительские регионы '" + parent.getId() + "':\n");
            printout.appendParentTree(true);
            printout.append(Style.GRAY);
            printout.append(")");
            printout.send(sender);
            return;
        }
        
        // Tell the user the current inheritance
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(child, null);
        printout.append(Style.YELLOW);
        printout.append("Родительский регион для '" + child.getId() + "' выставлен.\n");
        if (parent != null) {
            printout.append(Style.GRAY);
            printout.append("(Родительские регионы:\n");
            printout.appendParentTree(true);
            printout.append(Style.GRAY);
            printout.append(")");
        }
        printout.send(sender);
    }

    /**
     * Remove a region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"remove", "delete", "del", "rem"},
             usage = "<id>",
             flags = "fuw:",
             desc = "Удалить регион",
             min = 1, max = 1)
    public void remove(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        boolean removeChildren = args.hasFlag('f');
        boolean unsetParent = args.hasFlag('u');

        // Lookup the existing region
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion existing = checkExistingRegion(manager, args.getString(0), true);

        // Check permissions
        if (!getPermissionModel(sender).mayDelete(existing)) {
            throw new CommandPermissionsException();
        }

        RegionRemover task = new RegionRemover(manager, existing);

        if (removeChildren && unsetParent) {
            throw new CommandException("Вы не можете использовать оба параметра -u (удалить родительский) и -f (удалить дочерний) вместе.");
        } else if (removeChildren) {
            task.setRemovalStrategy(RemovalStrategy.REMOVE_CHILDREN);
        } else if (unsetParent) {
            task.setRemovalStrategy(RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
        }

        AsyncCommandHelper.wrap(WorldGuard.getInstance().getExecutorService().submit(task), worldGuard.getSupervisor(), sender, worldGuard.getExceptionConverter())
                .formatUsing(existing.getId())
                .registerWithSupervisor("Удаление региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите...)")
                .thenRespondWith(
                        "Регион '%s' удален.",
                        "Не удалось удалить регион '%s'");
    }

    /**
     * Reload the region database.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"load", "reload"},
            usage = "[мир]",
            desc = "Перезагрузить регионы из файла",
            flags = "w:")
    public void load(CommandContext args, final Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = null;
        try {
            world = checkWorld(args, sender, 'w'); // Get the world
        } catch (CommandException e) {
            // assume the user wants to reload all worlds
        }

        // Check permissions
        if (!getPermissionModel(sender).mayForceLoadRegions()) {
            throw new CommandPermissionsException();
        }

        if (world != null) {
            RegionManager manager = checkRegionManager(world);

            if (manager == null) {
                throw new CommandException("Для мира '" + world.getName() + "' нет менеджера регионов.");
            }

            ListenableFuture<?> future = WorldGuard.getInstance().getExecutorService().submit(new RegionManagerReloader(manager));

            AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), sender, worldGuard.getExceptionConverter())
                    .forRegionDataLoad(world, false);
        } else {
            // Load regions for all worlds
            List<RegionManager> managers = new ArrayList<>();

            for (World w : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
                RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(w);
                if (manager != null) {
                    managers.add(manager);
                }
            }

            ListenableFuture<?> future = WorldGuard.getInstance().getExecutorService().submit(new RegionManagerReloader(managers));

            AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), sender, worldGuard.getExceptionConverter())
                    .registerWithSupervisor("Загрузка регионов для всех миров... это может занять некоторое время")
                    .sendMessageAfterDelay("(Пожалуйста, подождите...)")
                    .thenRespondWith(
                            "База регионов для всех миров загружена.",
                            "Не удалось загрузить регионы для всех миров");
        }
    }

    /**
     * Re-save the region database.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"save", "write"},
            usage = "[мир]",
            desc = "Пересохранить мир",
            flags = "w:")
    public void save(CommandContext args, final Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = null;
        try {
            world = checkWorld(args, sender, 'w'); // Get the world
        } catch (CommandException e) {
            // assume user wants to save all worlds
        }

        // Check permissions
        if (!getPermissionModel(sender).mayForceSaveRegions()) {
            throw new CommandPermissionsException();
        }

        if (world != null) {
            RegionManager manager = checkRegionManager(world);

            if (manager == null) {
                throw new CommandException("Для мира '" + world.getName() + "' нет менеджера регионов.");
            }

            ListenableFuture<?> future = WorldGuard.getInstance().getExecutorService().submit(new RegionManagerSaver(manager));

            AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), sender, worldGuard.getExceptionConverter())
                    .forRegionDataSave(world, false);
        } else {
            // Save for all worlds
            List<RegionManager> managers = new ArrayList<>();

            for (World w : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
                RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(w);
                if (manager != null) {
                    managers.add(manager);
                }
            }

            ListenableFuture<?> future = WorldGuard.getInstance().getExecutorService().submit(new RegionManagerSaver(managers));

            AsyncCommandHelper.wrap(future, worldGuard.getSupervisor(), sender, worldGuard.getExceptionConverter())
                    .registerWithSupervisor("Сохранение регионов для всех миров...")
                    .sendMessageAfterDelay("(Пожалуйста, подождите...)")
                    .thenRespondWith(
                            "База регионов для всех миров сохранена.",
                            "Не удалось загрузить регионы для всех миров");
        }
    }

    /**
     * Migrate the region database.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"migratedb"}, usage = "<из> <в>",
             flags = "y",
             desc = "Мигрирование одной базы в другую.", min = 2, max = 2)
    public void migrateDB(CommandContext args, Actor sender) throws CommandException {
        // Check permissions
        if (!getPermissionModel(sender).mayMigrateRegionStore()) {
            throw new CommandPermissionsException();
        }

        DriverType from = Enums.findFuzzyByValue(DriverType.class, args.getString(0));
        DriverType to = Enums.findFuzzyByValue(DriverType.class, args.getString(1));

        if (from == null) {
            throw new CommandException("Значение 'from' не является распознанным типом базы регинов.");
        }

        if (to == null) {
            throw new CommandException("Значение 'to' не является распознанным типом базы регинов.");
        }

        if (from.equals(to)) {
            throw new CommandException("Источник и цель являются одной и тойже базой.");
        }

        if (!args.hasFlag('y')) {
            throw new CommandException("Эта команда потенциально опасна.\n" +
                    "Пожалуйста, удостовертесь что вы сделали резервные копии перед тем как запускать миграцию..");
        }

        ConfigurationManager config = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        RegionDriver fromDriver = config.regionStoreDriverMap.get(from);
        RegionDriver toDriver = config.regionStoreDriverMap.get(to);

        if (fromDriver == null) {
            throw new CommandException("Драйвер, указанный как 'from' не поддерживается в этой версии WorldGuard.");
        }

        if (toDriver == null) {
            throw new CommandException("Драйвер, указанный как 'to' не поддерживается в этой версии WorldGuard.");
        }

        DriverMigration migration = new DriverMigration(fromDriver, toDriver, WorldGuard.getInstance().getFlagRegistry());

        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;

        if (sender instanceof LocalPlayer) {
            handler = new LoggerToChatHandler(sender);
            handler.setLevel(Level.ALL);
            minecraftLogger = Logger.getLogger("com.sk89q.worldguard");
            minecraftLogger.addHandler(handler);
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            sender.print("Выполняется миграция... это может занять некоторое время.");
            container.migrate(migration);
            sender.print(
                    "Регионы успешно мигрированы! Если вы хотите использовать новую базу как основную " +
                    "исправьте файл настроек WorldGuard.");
        } catch (MigrationException e) {
            log.log(Level.WARNING, "Не удалось выполнить миграцию", e);
            throw new CommandException("Ошибка при миграции: " + e.getMessage());
        } finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }
    }

    /**
     * Migrate the region databases to use UUIDs rather than name.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"migrateuuid"},
            desc = "Мигрирование одной базы в другую используя UUID", max = 0)
    public void migrateUuid(CommandContext args, Actor sender) throws CommandException {
        // Check permissions
        if (!getPermissionModel(sender).mayMigrateRegionNames()) {
            throw new CommandPermissionsException();
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
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionDriver driver = container.getDriver();
            UUIDMigration migration = new UUIDMigration(driver, WorldGuard.getInstance().getProfileService(), WorldGuard.getInstance().getFlagRegistry());
            migration.setKeepUnresolvedNames(config.keepUnresolvedNames);
            sender.print("Выполняется миграция... это может занять некоторое время.");
            container.migrate(migration);
            sender.print("Миграция завершена!");
        } catch (MigrationException e) {
            log.log(Level.WARNING, "Не удалось выполнить миграцию", e);
            throw new CommandException("Ошибка миграции базы: " + e.getMessage());
        } finally {
            if (minecraftLogger != null) {
                minecraftLogger.removeHandler(handler);
            }
        }
    }

    /**
     * Teleport to a region
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"teleport", "tp"},
             usage = "<id>",
             flags = "s",
             desc = "Телепортироваться на заданную точку в регионе.",
             min = 1, max = 1)
    public void teleport(CommandContext args, Actor sender) throws CommandException {
        LocalPlayer player = worldGuard.checkPlayer(sender);
        Location teleportLocation;

        // Lookup the existing region
        RegionManager regionManager = checkRegionManager(player.getWorld());
        ProtectedRegion existing = checkExistingRegion(regionManager, args.getString(0), false);

        // Check permissions
        if (!getPermissionModel(player).mayTeleportTo(existing)) {
            throw new CommandPermissionsException();
        }

        // -s for spawn location
        if (args.hasFlag('s')) {
            teleportLocation = existing.getFlag(Flags.SPAWN_LOC);
            
            if (teleportLocation == null) {
                throw new CommandException(
                        "В данном регионе нет точки спавна.");
            }
        } else {
            teleportLocation = existing.getFlag(Flags.TELE_LOC);
            
            if (teleportLocation == null) {
                throw new CommandException("В данном регионе нет точки телепорта.");
            }
        }

        player.setLocation(teleportLocation);
        sender.print("Перемещение в регион '" + existing.getId() + "'.");
    }
}
