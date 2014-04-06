// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit.commands;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// @TODO: A lot of code duplication here! Need to fix.

public class RegionMemberCommands {
    private final WorldGuardPlugin plugin;

    public RegionMemberCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"addmember", "addmember"},
            usage = "<id> <members...>",
            flags = "w:",
            desc = "Add a member to a region",
            min = 2)
    public void addMember(CommandContext args, CommandSender sender) throws CommandException {
        final World world;
        Player player = null;
        LocalPlayer localPlayer = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            localPlayer = plugin.wrapPlayer(player);
        }
        if (args.hasFlag('w')) {
            world = plugin.matchWorld(sender, args.getFlag('w'));
        } else {
            if (player != null) {
                world = player.getWorld();
            } else {
                throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoWorldSpecified);
            }
        }

        String id = args.getString(0);

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion);
        }

        id = region.getId();

        if (localPlayer != null) {
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.addmember.own." + id.toLowerCase());
            } else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.addmember.member." + id.toLowerCase());
            } else {
                plugin.checkPermission(sender, "worldguard.region.addmember." + id.toLowerCase());
            }
        }

        RegionDBUtil.addToDomain(region.getMembers(), args.getParsedPaddedSlice(1, 0), 0);

        sender.sendMessage(BukkitUtil.replaceColorMacros(
                plugin.getGlobalStateManager().getLocale().regionUpdated
                .replace("%regionId%", id)));

        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion
                    .replace("%errorMessage%", e.getMessage()));
        }
    }

    @Command(aliases = {"addowner", "addowner"},
            usage = "<id> <owners...>",
            flags = "w:",
            desc = "Add an owner to a region",
            min = 2)
    public void addOwner(CommandContext args, CommandSender sender) throws CommandException {
        final World world;
        Player player = null;
        LocalPlayer localPlayer = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            localPlayer = plugin.wrapPlayer(player);
        }
        if (args.hasFlag('w')) {
            world = plugin.matchWorld(sender, args.getFlag('w'));
        } else {
            if (player != null) {
                world = player.getWorld();
            } else {
                throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoWorldSpecified);
            }
        }

        String id = args.getString(0);

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion);
        }

        id = region.getId();

        Boolean flag = region.getFlag(DefaultFlag.BUYABLE);
        DefaultDomain owners = region.getOwners();
        if (localPlayer != null) {
            if (flag != null && flag && owners != null && owners.size() == 0) {
                if (!plugin.hasPermission(player, "worldguard.region.unlimited")) {
                    int maxRegionCount = plugin.getGlobalStateManager().get(world).getMaxRegionCount(player);
                    if (maxRegionCount >= 0 && mgr.getRegionCountOfPlayer(localPlayer)
                            >= maxRegionCount) {
                        throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionOwnMaxRegions);
                    }
                }
                plugin.checkPermission(sender, "worldguard.region.addowner.unclaimed." + id.toLowerCase());
            } else {
                if (region.isOwner(localPlayer)) {
                    plugin.checkPermission(sender, "worldguard.region.addowner.own." + id.toLowerCase());
                } else if (region.isMember(localPlayer)) {
                    plugin.checkPermission(sender, "worldguard.region.addowner.member." + id.toLowerCase());
                } else {
                    plugin.checkPermission(sender, "worldguard.region.addowner." + id.toLowerCase());
                }
            }
        }

        RegionDBUtil.addToDomain(region.getOwners(), args.getParsedPaddedSlice(1, 0), 0);

        sender.sendMessage(BukkitUtil.replaceColorMacros(
                plugin.getGlobalStateManager().getLocale().regionUpdated
                .replace("%regionId%", id)));

        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion
                    .replace("%errorMessage%", e.getMessage()));
        }
    }

    @Command(aliases = {"removemember", "remmember", "removemem", "remmem"},
            usage = "<id> <owners...>",
            flags = "aw:",
            desc = "Remove an owner to a region",
            min = 1)
    public void removeMember(CommandContext args, CommandSender sender) throws CommandException {
        final World world;
        Player player = null;
        LocalPlayer localPlayer = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            localPlayer = plugin.wrapPlayer(player);
        }
        if (args.hasFlag('w')) {
            world = plugin.matchWorld(sender, args.getFlag('w'));
        } else {
            if (player != null) {
                world = player.getWorld();
            } else {
                throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoWorldSpecified);
            }
        }

        String id = args.getString(0);

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion);
        }

        id = region.getId();

        if (localPlayer != null) {
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.removemember.own." + id.toLowerCase());
            } else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.removemember.member." + id.toLowerCase());
            } else {
                plugin.checkPermission(sender, "worldguard.region.removemember." + id.toLowerCase());
            }
        }

        if (args.hasFlag('a')) {
            region.getMembers().removeAll();
        } else {
            if (args.argsLength() < 2) {
                throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionRemoveWithoutName);
            }
            RegionDBUtil.removeFromDomain(region.getMembers(), args.getParsedPaddedSlice(1, 0), 0);
        }

        sender.sendMessage(BukkitUtil.replaceColorMacros(
                plugin.getGlobalStateManager().getLocale().regionUpdated
                .replace("%regionId%", id)));

        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion
                    .replace("%errorMessage%", e.getMessage()));
        }
    }

    @Command(aliases = {"removeowner", "remowner"},
            usage = "<id> <owners...>",
            flags = "aw:",
            desc = "Remove an owner to a region",
            min = 1)
    public void removeOwner(CommandContext args,
            CommandSender sender) throws CommandException {
        final World world;
        Player player = null;
        LocalPlayer localPlayer = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            localPlayer = plugin.wrapPlayer(player);
        }
        if (args.hasFlag('w')) {
            world = plugin.matchWorld(sender, args.getFlag('w'));
        } else {
            if (player != null) {
                world = player.getWorld();
            } else {
                throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoWorldSpecified);
            }
        }

        String id = args.getString(0);

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion);
        }

        id = region.getId();

        if (localPlayer != null) {
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.removeowner.own." + id.toLowerCase());
            } else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.removeowner.member." + id.toLowerCase());
            } else {
                plugin.checkPermission(sender, "worldguard.region.removeowner." + id.toLowerCase());
            }
        }

        if (args.hasFlag('a')) {
            region.getOwners().removeAll();
        } else {
            if (args.argsLength() < 2) {
                throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionRemoveWithoutName);
            }
            RegionDBUtil.removeFromDomain(region.getOwners(), args.getParsedPaddedSlice(1, 0), 0);
        }

        sender.sendMessage(BukkitUtil.replaceColorMacros(
                plugin.getGlobalStateManager().getLocale().regionUpdated
                .replace("%regionId%", id)));

        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException(plugin.getGlobalStateManager().getLocale().commandExceptionNoSuchRegion
                    .replace("%errorMessage%", e.getMessage()));
        }
    }
}
