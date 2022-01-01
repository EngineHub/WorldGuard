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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.command.util.AsyncCommandBuilder;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.component.SubtleFormat;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.CustomDomain;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.domains.registry.DomainFactory;
import com.sk89q.worldguard.domains.registry.DomainRegistry;
import com.sk89q.worldguard.domains.registry.InvalidDomainFormat;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.DomainInputResolver;
import com.sk89q.worldguard.protection.util.DomainInputResolver.UserLocatorPolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

public class MemberCommands extends RegionCommandsBase {

    private final WorldGuard worldGuard;

    public MemberCommands(WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Command(aliases = {"addmember", "addmember", "addmem", "am"},
            usage = "<id> <members...>",
            flags = "nw:",
            desc = "Add a member to a region",
            min = 2)
    public void addMember(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        String id = args.getString(0);
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion region = checkExistingRegion(manager, id, true);

        // Check permissions
        if (!getPermissionModel(sender).mayAddMembers(region)) {
            throw new CommandPermissionsException();
        }

        // Resolve members asynchronously
        DomainInputResolver resolver = new DomainInputResolver(
                WorldGuard.getInstance().getProfileService(), args.getParsedPaddedSlice(1, 0));
        resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_ONLY);


        final String description = String.format("Adding members to the region '%s' on '%s'", region.getId(), world.getName());
        AsyncCommandBuilder.wrap(resolver, sender)
                .registerWithSupervisor(worldGuard.getSupervisor(), description)
                .onSuccess(String.format("Region '%s' updated with new members.", region.getId()), region.getMembers()::addAll)
                .onFailure("Failed to add new members", worldGuard.getExceptionConverter())
                .buildAndExec(worldGuard.getExecutorService());
    }

    @Command(aliases = {"addowner", "addowner", "ao"},
            usage = "<id> <owners...>",
            flags = "nw:",
            desc = "Add an owner to a region",
            min = 2)
    public void addOwner(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world

        String id = args.getString(0);

        RegionManager manager = checkRegionManager(world);
        ProtectedRegion region = checkExistingRegion(manager, id, true);

        // Check permissions
        if (!getPermissionModel(sender).mayAddOwners(region)) {
            throw new CommandPermissionsException();
        }

        // Resolve owners asynchronously
        DomainInputResolver resolver = new DomainInputResolver(
                WorldGuard.getInstance().getProfileService(), args.getParsedPaddedSlice(1, 0));
        resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_ONLY);


        final String description = String.format("Adding owners to the region '%s' on '%s'", region.getId(), world.getName());
        AsyncCommandBuilder.wrap(checkedAddOwners(sender, manager, region, world, resolver), sender)
                .registerWithSupervisor(worldGuard.getSupervisor(), description)
                .onSuccess(String.format("Region '%s' updated with new owners.", region.getId()), region.getOwners()::addAll)
                .onFailure("Failed to add new owners", worldGuard.getExceptionConverter())
                .buildAndExec(worldGuard.getExecutorService());
    }

    private static Callable<DefaultDomain> checkedAddOwners(Actor sender, RegionManager manager, ProtectedRegion region,
                                                            World world, DomainInputResolver resolver) {
        return () -> {
            DefaultDomain owners = resolver.call();
            // TODO this was always broken and never checked other players
            if (sender instanceof LocalPlayer) {
                LocalPlayer player = (LocalPlayer) sender;
                if (owners.contains(player) && !sender.hasPermission("worldguard.region.unlimited")) {
                    int maxRegionCount = WorldGuard.getInstance().getPlatform().getGlobalStateManager()
                            .get(world).getMaxRegionCount(player);
                    if (maxRegionCount >= 0 && manager.getRegionCountOfPlayer(player)
                            >= maxRegionCount) {
                        throw new CommandException("You already own the maximum allowed amount of regions.");
                    }
                }
            }
            if (region.getOwners().size() == 0) {
                boolean anyOwners = false;
                ProtectedRegion parent = region;
                while ((parent = parent.getParent()) != null) {
                    if (parent.getOwners().size() > 0) {
                        anyOwners = true;
                        break;
                    }
                }
                if (!anyOwners) {
                    sender.checkPermission("worldguard.region.addowner.unclaimed." + region.getId().toLowerCase());
                }
            }
            return owners;
        };
    }

    @Command(aliases = {"removemember", "remmember", "removemem", "remmem", "rm"},
            usage = "<id> <owners...>",
            flags = "naw:",
            desc = "Remove an owner to a region",
            min = 1)
    public void removeMember(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        String id = args.getString(0);
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion region = checkExistingRegion(manager, id, true);

        // Check permissions
        if (!getPermissionModel(sender).mayRemoveMembers(region)) {
            throw new CommandPermissionsException();
        }

        Callable<DefaultDomain> callable;
        if (args.hasFlag('a')) {
            callable = region::getMembers;
        } else {
            if (args.argsLength() < 2) {
                throw new CommandException("List some names to remove, or use -a to remove all.");
            }

            // Resolve members asynchronously
            DomainInputResolver resolver = new DomainInputResolver(
                    WorldGuard.getInstance().getProfileService(), args.getParsedPaddedSlice(1, 0));
            resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_AND_NAME);

            callable = resolver;
        }

        final String description = String.format("Removing members from the region '%s' on '%s'", region.getId(), world.getName());
        AsyncCommandBuilder.wrap(callable, sender)
                .registerWithSupervisor(worldGuard.getSupervisor(), description)
                .sendMessageAfterDelay("(Please wait... querying player names...)")
                .onSuccess(String.format("Region '%s' updated with members removed.", region.getId()), region.getMembers()::removeAll)
                .onFailure("Failed to remove members", worldGuard.getExceptionConverter())
                .buildAndExec(worldGuard.getExecutorService());
    }

    @Command(aliases = {"removeowner", "remowner", "ro"},
            usage = "<id> <owners...>",
            flags = "naw:",
            desc = "Remove an owner to a region",
            min = 1)
    public void removeOwner(CommandContext args, Actor sender) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        String id = args.getString(0);
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion region = checkExistingRegion(manager, id, true);

        // Check permissions
        if (!getPermissionModel(sender).mayRemoveOwners(region)) {
            throw new CommandPermissionsException();
        }

        Callable<DefaultDomain> callable;
        if (args.hasFlag('a')) {
            callable = region::getOwners;
        } else {
            if (args.argsLength() < 2) {
                throw new CommandException("List some names to remove, or use -a to remove all.");
            }

            // Resolve owners asynchronously
            DomainInputResolver resolver = new DomainInputResolver(
                    WorldGuard.getInstance().getProfileService(), args.getParsedPaddedSlice(1, 0));
            resolver.setLocatorPolicy(args.hasFlag('n') ? UserLocatorPolicy.NAME_ONLY : UserLocatorPolicy.UUID_AND_NAME);

            callable = resolver;
        }

        final String description = String.format("Removing owners from the region '%s' on '%s'", region.getId(), world.getName());
        AsyncCommandBuilder.wrap(callable, sender)
                .registerWithSupervisor(worldGuard.getSupervisor(), description)
                .sendMessageAfterDelay("(Please wait... querying player names...)")
                .onSuccess(String.format("Region '%s' updated with owners removed.", region.getId()), region.getOwners()::removeAll)
                .onFailure("Failed to remove owners", worldGuard.getExceptionConverter())
                .buildAndExec(worldGuard.getExecutorService());
    }


    /**
     * Modify a custom domain for owner.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"ownerdomain"},
            usage = "<id> <domain> [-w world] [value]",
            flags = "w:",
            desc = "Set flags",
            min = 2)
    public void ownerDomain(CommandContext args, Actor sender) throws CommandException {
        domain(args, sender, true);
    }

    /**
     * Modify a custom domain for member.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"memberdomain"},
            usage = "<id> <domain> [-w world] [value]",
            flags = "w:",
            desc = "Set flags",
            min = 2)
    public void onMemberDomain(CommandContext args, Actor sender) throws CommandException {
        domain(args, sender, false);
    }

    private void domain(CommandContext args, Actor sender, boolean isOwner) throws CommandException {
        warnAboutSaveFailures(sender);

        World world = checkWorld(args, sender, 'w'); // Get the world
        String domainName = args.getString(1);
        String value = args.argsLength() >= 3 ? args.getJoinedStrings(2) : null;
        DomainRegistry domainRegistry = WorldGuard.getInstance().getDomainRegistry();
        RegionPermissionModel permModel = getPermissionModel(sender);

        // Lookup the existing region
        RegionManager manager = checkRegionManager(world);
        ProtectedRegion existing = checkExistingRegion(manager, args.getString(0), true);

        // Check permissions
        if (!permModel.mayModifyCustomDomain(existing, isOwner, domainName)) {
            throw new CommandPermissionsException();
        }
        String regionId = existing.getId();

        DomainFactory<?> domainFactory = domainRegistry.get(domainName);

        // We didn't find the domain, so let's print a list of domains that the user
        // can use, and do nothing afterwards
        if (domainFactory == null) {
            AsyncCommandBuilder.wrap(new DomainListBuilder(domainRegistry, permModel, existing, world,
                    regionId, sender, domainName, isOwner), sender)
                    .registerWithSupervisor(WorldGuard.getInstance().getSupervisor(), "Domain list for invalid domain command.")
                    .onSuccess((Component) null, sender::print)
                    .onFailure((Component) null, WorldGuard.getInstance().getExceptionConverter())
                    .buildAndExec(WorldGuard.getInstance().getExecutorService());
            return;
        }

        DefaultDomain usedDomain = isOwner ? existing.getOwners() : existing.getMembers();

        // Set the flag value if a value was set
        if (value != null) {
            // Set the flag if [value] was given even if [-g group] was given as well
            try {
                CustomDomain customDomain = domainFactory.create(domainName);
                setDomain(existing, usedDomain, customDomain, sender, value);
            } catch (InvalidDomainFormat e) {
                throw new CommandException(e.getMessage());
            }
            // No value? Clear the flag, if -g isn't specified
        } else {
            usedDomain.removeCustomDomain(domainName);
        }

        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(world.getName(), existing, null, sender);
        printout.append(SubtleFormat.wrap("(Current domains:"));
        printout.newline();
        printout.appendDomain();
        printout.append(SubtleFormat.wrap(")"));
        printout.send(sender);
        checkSpawnOverlap(sender, world, existing);

    }

    private static class DomainListBuilder implements Callable<Component> {
        private final DomainRegistry domainRegistry;
        private final RegionPermissionModel permModel;
        private final ProtectedRegion existing;
        private final World world;
        private final String regionId;
        private final Actor sender;
        private final boolean isOwner;
        private final String domainName;

        DomainListBuilder(DomainRegistry domainRegistry, RegionPermissionModel permModel, ProtectedRegion existing,
                          World world, String regionId, Actor sender, String domainName, boolean isOwner) {
            this.domainRegistry = domainRegistry;
            this.permModel = permModel;
            this.existing = existing;
            this.world = world;
            this.regionId = regionId;
            this.sender = sender;
            this.domainName = domainName;
            this.isOwner = isOwner;
        }

        @Override
        public Component call() {
            ArrayList<String> domainList = new ArrayList<>();

            // Need to build a list
            for (Map.Entry<String, DomainFactory<?>> domainEntry : domainRegistry.getAll().entrySet()) {
                if (!permModel.mayModifyCustomDomain(existing, isOwner, domainEntry.getKey())) {
                    continue;
                }
                domainList.add(domainEntry.getKey());
            }
            Collections.sort(domainList);

            final TextComponent.Builder builder = TextComponent.builder("Available domains: ");

            final HoverEvent clickToSet = HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to set"));
            for (int i = 0; i < domainList.size(); i++) {
                String flag = domainList.get(i);

                builder.append(TextComponent.of(flag, i % 2 == 0 ? TextColor.GRAY : TextColor.WHITE)
                        .hoverEvent(clickToSet).clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND,
                                "/rg " + (isOwner ? "ownerdomains" : "memberdomains") +" -w \"" + world.getName() + "\" " + regionId + " " + flag + " ")));
                if (i < domainList.size() + 1) {
                    builder.append(TextComponent.of(", "));
                }
            }

            return ErrorFormat.wrap("Unknown domain specified: " + domainName)
                    .append(TextComponent.newline())
                    .append(builder.build());
        }
    }
}
