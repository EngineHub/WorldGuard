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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.util.DomainInputResolver;
import com.sk89q.worldguard.protection.util.DomainInputResolver.UserLocatorPolicy;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// @TODO: A lot of code duplication here! Need to fix.

public class RegionMemberCommands {

    private final WorldGuardPlugin plugin;

    public RegionMemberCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"addmember", "addmember", "addmem", "am"},
            usage = "<id> <members...>",
            flags = "nw:",
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
                throw new CommandException("No world specified. Use -w <worldname>.");
            }
        }

        String id = args.getString(0);

        RegionManager manager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = manager.matchRegion(id);

        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
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

        // Resolve members asynchronously
        DomainInputResolver resolver = new DomainInputResolver(
                plugin.getProfileService(), args.getParsedPaddedSlice(1, 0));
        resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_ONLY);

        // Then add it to the members
        ListenableFuture<DefaultDomain> future = Futures.transform(
                plugin.getExecutorService().submit(resolver),
                resolver.createAddAllFunction(region.getMembers()));

        AsyncCommandHelper.wrap(future, plugin, sender)
                .formatUsing(region.getId(), world.getName())
                .registerWithSupervisor("Adding members to the region '%s' on '%s'")
                .sendMessageAfterDelay("(Please wait... querying player names...)")
                .thenRespondWith("Region '%s' updated with new members.", "Failed to add new members");
    }

    @Command(aliases = {"addowner", "addowner", "ao"},
            usage = "<id> <owners...>",
            flags = "nw:",
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
                throw new CommandException("No world specified. Use -w <worldname>.");
            }
        }

        String id = args.getString(0);

        RegionManager manager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = manager.matchRegion(id);

        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }

        id = region.getId();

        Boolean flag = region.getFlag(DefaultFlag.BUYABLE);
        DefaultDomain owners = region.getOwners();
        if (localPlayer != null) {
            if (flag != null && flag && owners != null && owners.size() == 0) {
                if (!plugin.hasPermission(player, "worldguard.region.unlimited")) {
                    int maxRegionCount = plugin.getGlobalStateManager().get(world).getMaxRegionCount(player);
                    if (maxRegionCount >= 0 && manager.getRegionCountOfPlayer(localPlayer)
                            >= maxRegionCount) {
                        throw new CommandException("You already own the maximum allowed amount of regions.");
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

        // Resolve owners asynchronously
        DomainInputResolver resolver = new DomainInputResolver(
                plugin.getProfileService(), args.getParsedPaddedSlice(1, 0));
        resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_ONLY);

        // Then add it to the owners
        ListenableFuture<DefaultDomain> future = Futures.transform(
                plugin.getExecutorService().submit(resolver),
                resolver.createAddAllFunction(region.getOwners()));

        AsyncCommandHelper.wrap(future, plugin, sender)
                .formatUsing(region.getId(), world.getName())
                .registerWithSupervisor("Adding owners to the region '%s' on '%s'")
                .sendMessageAfterDelay("(Please wait... querying player names...)")
                .thenRespondWith("Region '%s' updated with new owners.", "Failed to add new owners");
    }

    @Command(aliases = {"removemember", "remmember", "removemem", "remmem", "rm"},
            usage = "<id> <owners...>",
            flags = "naw:",
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
                throw new CommandException("No world specified. Use -w <worldname>.");
            }
        }

        String id = args.getString(0);

        RegionManager manager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = manager.matchRegion(id);

        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
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

        ListenableFuture<?> future;

        if (args.hasFlag('a')) {
            region.getMembers().removeAll();

            future = Futures.immediateFuture(null);
        } else {
            if (args.argsLength() < 2) {
                throw new CommandException("List some names to remove, or use -a to remove all.");
            }

            // Resolve members asynchronously
            DomainInputResolver resolver = new DomainInputResolver(
                    plugin.getProfileService(), args.getParsedPaddedSlice(1, 0));
            resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_AND_NAME);

            // Then remove it from the members
            future = Futures.transform(
                    plugin.getExecutorService().submit(resolver),
                    resolver.createRemoveAllFunction(region.getMembers()));
        }

        AsyncCommandHelper.wrap(future, plugin, sender)
                .formatUsing(region.getId(), world.getName())
                .registerWithSupervisor("Removing members from the region '%s' on '%s'")
                .sendMessageAfterDelay("(Please wait... querying player names...)")
                .thenRespondWith("Region '%s' updated with members removed.", "Failed to remove members");
    }

    @Command(aliases = {"removeowner", "remowner", "ro"},
            usage = "<id> <owners...>",
            flags = "naw:",
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
                throw new CommandException("No world specified. Use -w <worldname>.");
            }
        }

        String id = args.getString(0);

        RegionManager manager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = manager.matchRegion(id);

        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
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

        ListenableFuture<?> future;

        if (args.hasFlag('a')) {
            region.getOwners().removeAll();

            future = Futures.immediateFuture(null);
        } else {
            if (args.argsLength() < 2) {
                throw new CommandException("List some names to remove, or use -a to remove all.");
            }

            // Resolve owners asynchronously
            DomainInputResolver resolver = new DomainInputResolver(
                    plugin.getProfileService(), args.getParsedPaddedSlice(1, 0));
            resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_AND_NAME);

            // Then remove it from the owners
            future = Futures.transform(
                    plugin.getExecutorService().submit(resolver),
                    resolver.createRemoveAllFunction(region.getOwners()));
        }

        AsyncCommandHelper.wrap(future, plugin, sender)
                .formatUsing(region.getId(), world.getName())
                .registerWithSupervisor("Removing owners from the region '%s' on '%s'")
                .sendMessageAfterDelay("(Please wait... querying player names...)")
                .thenRespondWith("Region '%s' updated with owners removed.", "Failed to remove owners");
    }
}
