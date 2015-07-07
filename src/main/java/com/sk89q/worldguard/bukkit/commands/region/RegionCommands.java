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

package com.sk89q.worldguard.bukkit.commands.region;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.AsyncCommandHelper;
import com.sk89q.worldguard.bukkit.commands.CommandUtils;
import com.sk89q.worldguard.bukkit.commands.FutureProgressListener;
import com.sk89q.worldguard.bukkit.commands.MessageFutureCallback.Builder;
import com.sk89q.worldguard.bukkit.commands.task.RegionAdder;
import com.sk89q.worldguard.bukkit.commands.task.RegionLister;
import com.sk89q.worldguard.bukkit.commands.task.RegionManagerReloader;
import com.sk89q.worldguard.bukkit.commands.task.RegionManagerSaver;
import com.sk89q.worldguard.bukkit.commands.task.RegionRemover;
import com.sk89q.worldguard.bukkit.permission.RegionPermissionModel;
import com.sk89q.worldguard.bukkit.util.logging.LoggerToChatHandler;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
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
import com.sk89q.worldguard.protection.util.DomainInputResolver.UserLocatorPolicy;
import com.sk89q.worldguard.util.Enums;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements the /region commands for WorldGuard.
 */
public final class RegionCommands extends RegionCommandsBase {

    private static final Logger log = Logger.getLogger(RegionCommands.class.getCanonicalName());
    private final WorldGuardPlugin plugin;

    public RegionCommands(WorldGuardPlugin plugin) {
        checkNotNull(plugin);
        this.plugin = plugin;
    }
    
    /**
     * Defines a new region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"define", "def", "d", "create"},
             usage = "<название> [<владелец1> [<владелец2> [<владелецы...>]]]",
             flags = "ng",
             desc = "Назначить регион",
             min = 1)
    public void define(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);
        Player player = plugin.checkPlayer(sender);

        // Check permissions
        if (!getPermissionModel(sender).mayDefine()) {
            throw new CommandPermissionsException();
        }

        String id = checkRegionId(args.getString(0), false);

        RegionManager manager = checkRegionManager(plugin, player.getWorld());

        checkRegionDoesNotExist(manager, id, true);

        ProtectedRegion region;

        if (args.hasFlag('g')) {
            region = new GlobalProtectedRegion(id);
        } else {
            region = checkRegionFromSelection(player, id);
            warnAboutDimensions(player, region);
            informNewUser(player, manager, region);
        }

        RegionAdder task = new RegionAdder(plugin, manager, region);
        task.addOwnersFromCommand(args, 2);
        ListenableFuture<?> future = plugin.getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, plugin, player)
                .formatUsing(id)
                .registerWithSupervisor("Добавление региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите... Добавление '%s'...)")
                .thenRespondWith(
                        "Новый регион назван '%s'.",
                        "Ошибка при создании региона '%s'");
    }

    /**
     * Re-defines a region with a new selection.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"redefine", "update", "move"},
             usage = "<название>",
             desc = "Переназначить существующий регион",
             flags = "g",
             min = 1, max = 1)
    public void redefine(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);

        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();

        String id = checkRegionId(args.getString(0), false);

        RegionManager manager = checkRegionManager(plugin, world);

        ProtectedRegion existing = checkExistingRegion(manager, id, false);

        // Check permissions
        if (!getPermissionModel(sender).mayRedefine(existing)) {
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

        RegionAdder task = new RegionAdder(plugin, manager, region);
        ListenableFuture<?> future = plugin.getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, plugin, player)
                .formatUsing(id)
                .registerWithSupervisor("Обновление региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите... Обновление '%s'...)")
                .thenRespondWith(
                        "Регион '%s' переназначен на выделеную территорию.",
                        "Ошибка при создании региона '%s'");
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
             usage = "<название>",
             desc = "Заприватить регион",
             min = 1, max = 1)
    public void claim(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);

        Player player = plugin.checkPlayer(sender);
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        RegionPermissionModel permModel = getPermissionModel(sender);
        
        // Check permissions
        if (!permModel.mayClaim()) {
            throw new CommandPermissionsException();
        }

        String id = checkRegionId(args.getString(0), false);

        RegionManager manager = checkRegionManager(plugin, player.getWorld());

        checkRegionDoesNotExist(manager, id, false);
        ProtectedRegion region = checkRegionFromSelection(player, id);

        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());

        // Check whether the player has created too many regions
        if (!permModel.mayClaimRegionsUnbounded()) {
            int maxRegionCount = wcfg.getMaxRegionCount(player);
            if (maxRegionCount >= 0
                    && manager.getRegionCountOfPlayer(localPlayer) >= maxRegionCount) {
                throw new CommandException(
                        "У вас слишком много регионов, удалите старые прежде чем создать новый.");
            }
        }

        ProtectedRegion existing = manager.getRegion(id);

        // Check for an existing region
        if (existing != null) {
            if (!existing.getOwners().contains(localPlayer)) {
                throw new CommandException(
                        "Такой регион уже есть и вы не его владелец.");
            }
        }

        // We have to check whether this region violates the space of any other reion
        ApplicableRegionSet regions = manager.getApplicableRegions(region);

        // Check if this region overlaps any other region
        if (regions.size() > 0) {
            if (!regions.isOwnerOfAll(localPlayer)) {
                throw new CommandException("Этот регион пересекается с другии.");
            }
        } else {
            if (wcfg.claimOnlyInsideExistingRegions) {
                throw new CommandException("You may only claim regions inside " +
                        "existing regions that you or your group own.");
            }
        }

        if (wcfg.maxClaimVolume >= Integer.MAX_VALUE) {
            throw new CommandException("The maximum claim volume get in the configuration is higher than is supported. " +
                    "Currently, it must be " + Integer.MAX_VALUE+ " or smaller. Please contact a server administrator.");
        }

        // Check claim volume
        if (!permModel.mayClaimRegionsUnbounded()) {
            if (region instanceof ProtectedPolygonalRegion) {
                throw new CommandException("Polygons are currently not supported for /rg claim.");
            }

            if (region.volume() > wcfg.maxClaimVolume) {
                player.sendMessage(ChatColor.RED + "Вы выделили слишком большой участок.");
                player.sendMessage(ChatColor.RED +
                        "Максимальный размер: " + wcfg.maxClaimVolume + ", вы выделили размер: " + region.volume());
                return;
            }
        }

        RegionAdder task = new RegionAdder(plugin, manager, region);
        task.setLocatorPolicy(UserLocatorPolicy.UUID_ONLY);
        task.setOwnersInput(new String[]{player.getName()});
        ListenableFuture<?> future = plugin.getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, plugin, player)
                .formatUsing(id)
                .registerWithSupervisor("Создание региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите... создание '%s'...)")
                .thenRespondWith(
                        "Регион '%s' создан!",
                        "Ошибка при создании региона '%s'");
    }

    /**
     * Get a WorldEdit selection from a region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"select", "sel", "s"},
             usage = "[название]",
             desc = "Загрузка выделения из WorldEdit",
             min = 0, max = 1)
    public void select(CommandContext args, CommandSender sender) throws CommandException {
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        RegionManager manager = checkRegionManager(plugin, world);
        ProtectedRegion existing;
        
        // If no arguments were given, get the region that the player is inside
        if (args.argsLength() == 0) {
            existing = checkRegionStandingIn(manager, player);
        } else {
            existing = checkExistingRegion(manager, args.getString(0), false);
        }

        // Check permissions
        if (!getPermissionModel(sender).maySelect(existing)) {
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
             usage = "[название]",
             flags = "usw:",
             desc = "Получить информацию о регионе",
             min = 0, max = 1)
    public void info(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        RegionPermissionModel permModel = getPermissionModel(sender);

        // Lookup the existing region
        RegionManager manager = checkRegionManager(plugin, world);
        ProtectedRegion existing;

        if (args.argsLength() == 0) { // Get region from where the player is
            if (!(sender instanceof Player)) {
                throw new CommandException("Пожалуйста, используйте " +
                        "/region info -w <название мира> <название региона>.");
            }
            
            existing = checkRegionStandingIn(manager, (Player) sender, true);
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

            setPlayerSelection(plugin.checkPlayer(sender), existing);
        }

        // Print region information
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing, args.hasFlag('u') ? null : plugin.getProfileCache());
        ListenableFuture<?> future = Futures.transform(
                plugin.getExecutorService().submit(printout),
                CommandUtils.messageFunction(sender));

        // If it takes too long...
        FutureProgressListener.addProgressListener(
                future, sender, "(Пожалуйста, подождите... получение информации о регионах...)");

        // Send a response message
        Futures.addCallback(future,
                new Builder(plugin, sender)
                        .onFailure("Не удалось получить информацию")
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
             desc = "Получение списка ваших регионов",
             flags = "np:w:",
             max = 1)
    public void list(CommandContext args, CommandSender sender) throws CommandException {
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

        RegionManager manager = checkRegionManager(plugin, world);

        RegionLister task = new RegionLister(plugin, manager, sender);
        task.setPage(page);
        if (ownedBy != null) {
            task.filterOwnedByName(ownedBy, args.hasFlag('n'));
        }

        ListenableFuture<?> future = plugin.getExecutorService().submit(task);

        AsyncCommandHelper.wrap(future, plugin, sender)
                .registerWithSupervisor("Получение списка регионов...")
                .sendMessageAfterDelay("(Пожалуйста, подождите... Получение списка регионов...)")
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
             usage = "<название> <флаг> [-w мир] [-g группа] [значение]",
             flags = "g:w:e",
             desc = "Установить флаг на регион",
             min = 2)
    public void flag(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        String flagName = args.getString(1);
        String value = args.argsLength() >= 3 ? args.getJoinedStrings(2) : null;
        RegionGroup groupValue = null;
        RegionPermissionModel permModel = getPermissionModel(sender);

        if (args.hasFlag('e')) {
            if (value != null) {
                throw new CommandException("Вы не можнте установить пустое значение флага.");
            }

            value = "";
        }

        // Add color codes
        if (value != null) {
            value = CommandUtils.replaceColorMacros(value);
        }

        // Lookup the existing region
        RegionManager manager = checkRegionManager(plugin, world);
        ProtectedRegion existing = checkExistingRegion(manager, args.getString(0), true);

        // Check permissions
        if (!permModel.maySetFlag(existing)) {
            throw new CommandPermissionsException();
        }

        Flag<?> foundFlag = DefaultFlag.fuzzyMatchFlag(flagName);

        // We didn't find the flag, so let's print a list of flags that the user
        // can use, and do nothing afterwards
        if (foundFlag == null) {
            StringBuilder list = new StringBuilder();

            // Need to build a list
            for (Flag<?> flag : DefaultFlag.getFlags()) {
                // Can the user set this flag?
                if (!permModel.maySetFlag(existing, flag)) {
                    continue;
                }

                if (list.length() > 0) {
                    list.append(", ");
                }
                
                list.append(flag.getName());
            }

            sender.sendMessage(ChatColor.RED + "Неизвестный флаг: " + flagName);
            sender.sendMessage(ChatColor.RED + "Доступные " + "флаги: " + list);
            
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
                groupValue = groupFlag.parseInput(plugin, sender, group);
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

            sender.sendMessage(ChatColor.YELLOW
                    + "Флаг '" + foundFlag.getName() + "' установлен на регион' " +
                    existing.getId() + "' в значении '" + ChatColor.stripColor(value) + "'.");
        
        // No value? Clear the flag, if -g isn't specified
        } else if (!args.hasFlag('g')) {
            // Clear the flag only if neither [value] nor [-g group] was given
            existing.setFlag(foundFlag, null);

            // Also clear the associated group flag if one exists
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
            if (groupFlag != null) {
                existing.setFlag(groupFlag, null);
            }

            sender.sendMessage(ChatColor.YELLOW
                    + "Флаг '" + foundFlag.getName() + "' удалён с региона '" +
                    existing.getId() + "'. (Any -g(roups) were also removed.)");
        }

        // Now set the group
        if (groupValue != null) {
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();

            // If group set to the default, then clear the group flag
            if (groupValue == groupFlag.getDefault()) {
                existing.setFlag(groupFlag, null);
                sender.sendMessage(ChatColor.YELLOW
                        + "Групповой флаг '" + foundFlag.getName() + "' сброшен " +
                                "в стандартное значение.");
            } else {
                existing.setFlag(groupFlag, groupValue);
                sender.sendMessage(ChatColor.YELLOW
                        + "Групповой флаг '" + foundFlag.getName() + "' установлен.");
            }
        }

        // Print region information
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing, null);
        printout.append(ChatColor.GRAY);
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
             usage = "<название> <приоритет>",
             flags = "w:",
             desc = "Установить приоритет региона",
             min = 2, max = 2)
    public void setPriority(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        int priority = args.getInteger(1);

        // Lookup the existing region
        RegionManager manager = checkRegionManager(plugin, world);
        ProtectedRegion existing = checkExistingRegion(manager, args.getString(0), false);

        // Check permissions
        if (!getPermissionModel(sender).maySetPriority(existing)) {
            throw new CommandPermissionsException();
        }

        existing.setPriority(priority);

        sender.sendMessage(ChatColor.YELLOW
                + "Приоритет '" + existing.getId() + "' установлен на регион "
                + priority + " (higher numbers override).");
    }

    /**
     * Set the parent of a region.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"setparent", "parent", "par"},
             usage = "<название> [родительский регион]",
             flags = "w:",
             desc = "Установить родительский регион",
             min = 1, max = 2)
    public void setParent(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        ProtectedRegion parent;
        ProtectedRegion child;

        // Lookup the existing region
        RegionManager manager = checkRegionManager(plugin, world);
        
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
            printout.append(ChatColor.RED);
            assert parent != null;
            printout.append("Uh oh! Setting '" + parent.getId() + "' to be the parent " +
                    "of '" + child.getId() + "' would cause circular inheritance.\n");
            printout.append(ChatColor.GRAY);
            printout.append("(Current inheritance on '" + parent.getId() + "':\n");
            printout.appendParentTree(true);
            printout.append(ChatColor.GRAY);
            printout.append(")");
            printout.send(sender);
            return;
        }
        
        // Tell the user the current inheritance
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(child, null);
        printout.append(ChatColor.YELLOW);
        printout.append("Inheritance set for region '" + child.getId() + "'.\n");
        if (parent != null) {
            printout.append(ChatColor.GRAY);
            printout.append("(Current inheritance:\n");
            printout.appendParentTree(true);
            printout.append(ChatColor.GRAY);
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
             usage = "<название>",
             flags = "fuw:",
             desc = "Удалить регион",
             min = 1, max = 1)
    public void remove(CommandContext args, CommandSender sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        boolean removeChildren = args.hasFlag('f');
        boolean unsetParent = args.hasFlag('u');

        // Lookup the existing region
        RegionManager manager = checkRegionManager(plugin, world);
        ProtectedRegion existing = checkExistingRegion(manager, args.getString(0), true);

        // Check permissions
        if (!getPermissionModel(sender).mayDelete(existing)) {
            throw new CommandPermissionsException();
        }

        RegionRemover task = new RegionRemover(manager, existing);

        if (removeChildren && unsetParent) {
            throw new CommandException("You cannot use both -u (unset parent) and -f (remove children) together.");
        } else if (removeChildren) {
            task.setRemovalStrategy(RemovalStrategy.REMOVE_CHILDREN);
        } else if (unsetParent) {
            task.setRemovalStrategy(RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
        }

        AsyncCommandHelper.wrap(plugin.getExecutorService().submit(task), plugin, sender)
                .formatUsing(existing.getId())
                .registerWithSupervisor("Удаление региона '%s'...")
                .sendMessageAfterDelay("(Пожалуйста, подождите... Удаление региона '%s'...)")
                .thenRespondWith(
                        "Регион '%s' успешно удалён.",
                        "Ошибка при удалении региона '%s'");
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
            desc = "Перезагрузить конфиги",
            flags = "w:")
    public void load(CommandContext args, final CommandSender sender) throws CommandException {
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
            RegionManager manager = checkRegionManager(plugin, world);

            if (manager == null) {
                throw new CommandException("No region manager exists for world '" + world.getName() + "'.");
            }

            ListenableFuture<?> future = plugin.getExecutorService().submit(new RegionManagerReloader(manager));

            AsyncCommandHelper.wrap(future, plugin, sender)
                    .forRegionDataLoad(world, false);
        } else {
            // Load regions for all worlds
            List<RegionManager> managers = new ArrayList<RegionManager>();

            for (World w : Bukkit.getServer().getWorlds()) {
                RegionManager manager = plugin.getRegionContainer().get(w);
                if (manager != null) {
                    managers.add(manager);
                }
            }

            ListenableFuture<?> future = plugin.getExecutorService().submit(new RegionManagerReloader(managers));

            AsyncCommandHelper.wrap(future, plugin, sender)
                    .registerWithSupervisor("Загрузка регионов")
                    .sendMessageAfterDelay("(Пожалуйста, подождите... загрузка регионов с диска...)")
                    .thenRespondWith(
                            "Регионы успешно загружены!",
                            "Ошибка при загрузке регионов");
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
            desc = "Пересохранить все регионы на диск",
            flags = "w:")
    public void save(CommandContext args, final CommandSender sender) throws CommandException {
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
            RegionManager manager = checkRegionManager(plugin, world);

            if (manager == null) {
                throw new CommandException("No region manager exists for world '" + world.getName() + "'.");
            }

            ListenableFuture<?> future = plugin.getExecutorService().submit(new RegionManagerSaver(manager));

            AsyncCommandHelper.wrap(future, plugin, sender)
                    .forRegionDataSave(world, false);
        } else {
            // Save for all worlds
            List<RegionManager> managers = new ArrayList<RegionManager>();

            for (World w : Bukkit.getServer().getWorlds()) {
                RegionManager manager = plugin.getRegionContainer().get(w);
                if (manager != null) {
                    managers.add(manager);
                }
            }

            ListenableFuture<?> future = plugin.getExecutorService().submit(new RegionManagerSaver(managers));

            AsyncCommandHelper.wrap(future, plugin, sender)
                    .registerWithSupervisor("Сохранение регионов")
                    .sendMessageAfterDelay("(Пожалуйста, подождите... сохранение на диск всех регионов...)")
                    .thenRespondWith(
                            "Все регионы успешно сохранены!",
                            "Ошибка при сохранении регионов.");
        }
    }

    /**
     * Migrate the region database.
     * 
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"migratedb"}, usage = "<from> <to>",
             flags = "y",
             desc = "Migrate from one Protection Database to another.", min = 2, max = 2)
    public void migrateDB(CommandContext args, CommandSender sender) throws CommandException {
        // Check permissions
        if (!getPermissionModel(sender).mayMigrateRegionStore()) {
            throw new CommandPermissionsException();
        }

        DriverType from = Enums.findFuzzyByValue(DriverType.class, args.getString(0));
        DriverType to = Enums.findFuzzyByValue(DriverType.class, args.getString(1));

        if (from == null) {
            throw new CommandException("The value of 'from' is not a recognized type of region data database.");
        }

        if (to == null) {
            throw new CommandException("The value of 'to' is not a recognized type of region region data database.");
        }

        if (from.equals(to)) {
            throw new CommandException("It is not possible to migrate between the same types of region data databases.");
        }

        if (!args.hasFlag('y')) {
            throw new CommandException("This command is potentially dangerous.\n" +
                    "Please ensure you have made a backup of your data, and then re-enter the command with -y tacked on at the end to proceed.");
        }

        ConfigurationManager config = plugin.getGlobalStateManager();
        RegionDriver fromDriver = config.regionStoreDriverMap.get(from);
        RegionDriver toDriver = config.regionStoreDriverMap.get(to);

        if (fromDriver == null) {
            throw new CommandException("The driver specified as 'from' does not seem to be supported in your version of WorldGuard.");
        }

        if (toDriver == null) {
            throw new CommandException("The driver specified as 'to' does not seem to be supported in your version of WorldGuard.");
        }

        DriverMigration migration = new DriverMigration(fromDriver, toDriver);

        LoggerToChatHandler handler = null;
        Logger minecraftLogger = null;

        if (sender instanceof Player) {
            handler = new LoggerToChatHandler(sender);
            handler.setLevel(Level.ALL);
            minecraftLogger = Logger.getLogger("com.sk89q.worldguard");
            minecraftLogger.addHandler(handler);
        }

        try {
            RegionContainer container = plugin.getRegionContainer();
            sender.sendMessage(ChatColor.YELLOW + "Now performing migration... this may take a while.");
            container.migrate(migration);
            sender.sendMessage(ChatColor.YELLOW +
                    "Migration complete! This only migrated the data. If you already changed your settings to use " +
                    "the target driver, then WorldGuard is now using the new data. If not, you have to adjust your " +
                    "configuration to use the new driver and then restart your server.");
        } catch (MigrationException e) {
            log.log(Level.WARNING, "Failed to migrate", e);
            throw new CommandException("Error encountered while migrating: " + e.getMessage());
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
            desc = "Migrate loaded databases to use UUIDs", max = 0)
    public void migrateUuid(CommandContext args, CommandSender sender) throws CommandException {
        // Check permissions
        if (!getPermissionModel(sender).mayMigrateRegionNames()) {
            throw new CommandPermissionsException();
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
            RegionContainer container = plugin.getRegionContainer();
            RegionDriver driver = container.getDriver();
            UUIDMigration migration = new UUIDMigration(driver, plugin.getProfileService());
            migration.setKeepUnresolvedNames(config.keepUnresolvedNames);
            sender.sendMessage(ChatColor.YELLOW + "Now performing migration... this may take a while.");
            container.migrate(migration);
            sender.sendMessage(ChatColor.YELLOW + "Migration complete!");
        } catch (MigrationException e) {
            log.log(Level.WARNING, "Failed to migrate", e);
            throw new CommandException("Error encountered while migrating: " + e.getMessage());
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
             usage = "<название>",
             flags = "s",
             desc = "Телепортация  в регион.",
             min = 1, max = 1)
    public void teleport(CommandContext args, CommandSender sender) throws CommandException {
        Player player = plugin.checkPlayer(sender);
        Location teleportLocation;

        // Lookup the existing region
        RegionManager regionManager = checkRegionManager(plugin, player.getWorld());
        ProtectedRegion existing = checkExistingRegion(regionManager, args.getString(0), false);

        // Check permissions
        if (!getPermissionModel(sender).mayTeleportTo(existing)) {
            throw new CommandPermissionsException();
        }

        // -s for spawn location
        if (args.hasFlag('s')) {
            teleportLocation = existing.getFlag(DefaultFlag.SPAWN_LOC);
            
            if (teleportLocation == null) {
                throw new CommandException(
                        "В регионе не установлена точка спауна.");
            }
        } else {
            teleportLocation = existing.getFlag(DefaultFlag.TELE_LOC);
            
            if (teleportLocation == null) {
                throw new CommandException(
                        "В регионе не установлена точка спауна.");
            }
        }

        player.teleport(BukkitUtil.toLocation(teleportLocation));
        sender.sendMessage("Телепортация в регион '" + existing.getId() + "'.");
    }
}
