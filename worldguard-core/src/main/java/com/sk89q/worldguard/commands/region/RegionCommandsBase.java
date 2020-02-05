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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.component.SubtleFormat;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.util.profile.Profile;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class RegionCommandsBase {

    protected RegionCommandsBase() {
    }

    /**
     * Get the permission model to lookup permissions.
     *
     * @param sender the sender
     * @return the permission model
     */
    protected static RegionPermissionModel getPermissionModel(Actor sender) {
        return new RegionPermissionModel(sender);
    }

    /**
     * Gets the world from the given flag, or falling back to the the current player
     * if the sender is a player, otherwise reporting an error.
     *
     * @param args   the arguments
     * @param sender the sender
     * @param flag   the flag (such as 'w')
     * @return a world
     * @throws CommandException on error
     */
    protected static World checkWorld(CommandContext args, Actor sender, char flag) throws CommandException {
        if (args.hasFlag(flag)) {
            return WorldGuard.getInstance().getPlatform().getMatcher().matchWorld(sender, args.getFlag(flag));
        } else {
            if (sender instanceof LocalPlayer) {
                return ((LocalPlayer) sender).getWorld();
            } else {
                throw new CommandException("Please specify " + "the world with -" + flag + " world_name.");
            }
        }
    }

    /**
     * Validate a region name.
     *
     * @param name        the name
     * @param allowGlobal whether __global__ is allowed
     * @throws CommandException thrown on an error
     */
    protected static void checkName(String name, boolean allowGlobal) throws CommandException {
        if (!RegionIdentifier.isValidName(name)) {
            throw new CommandException(
                    "The region name of '" + name + "' contains characters that are not allowed.");
        }

        if (!allowGlobal && name.equalsIgnoreCase("__global__")) { // Sorry, no global
            throw new CommandException(
                    "Sorry, you can't use __global__ here.");
        }
    }

    /**
     * Validate a region ID.
     *
     * @param id          the id
     * @param allowGlobal whether __global__ is allowed
     * @return the id given
     * @throws CommandException thrown on an error
     */
    @Deprecated
    protected static String checkRegionId(String id, boolean allowGlobal) throws CommandException {
        checkName(id, allowGlobal);
        return id;
    }

    /**
     * Validate a region namespace.
     *
     * @param namespace the namespace name
     * @throws CommandException thrown on an error
     */
    protected static void checkNamespace(String namespace) throws CommandException {
        if (!RegionIdentifier.isValidNamespace(namespace)) {
            throw new CommandException(
                    "The region namespace of '" + namespace + "' contains characters that are not allowed.");
        }
    }

    /**
     * Get the unqualified name from a possibly qualified region identifier.
     *
     * @param id the unprocessed region identifier
     * @return the unqualified name, or an empty string if not present
     */
    protected static String getUnqualifiedName(String id) {
        int namespaceSeparatorIndex = id.lastIndexOf(':');
        if (namespaceSeparatorIndex == -1) {
            return id;
        }

        int unqualifiedNameStartIndex = namespaceSeparatorIndex + 1;
        if (unqualifiedNameStartIndex == id.length()) {
            return "";
        }

        return id.substring(unqualifiedNameStartIndex);
    }

    /**
     * Get the namespace name from a possibly qualified region identifier.
     *
     * @param id the unprocessed region identifier
     * @return an optional containing the namespace name, or an empty string if qualified,
     * if not qualified, an empty optional is returned
     */
    protected static Optional<String> getNamespace(String id) {
        int namespaceSeparatorIndex = id.lastIndexOf(':');
        if (namespaceSeparatorIndex == -1) {
            return Optional.empty();
        }

        return Optional.of(id.substring(0, namespaceSeparatorIndex));
    }

    private static class InvalidMacroException extends RuntimeException {
        public InvalidMacroException(String message) {
            super(message);
        }
    }

    /***
     * Expand macros in the namespace name.
     *
     * @param namespace the unexpanded namespace name
     * @return the expanded namespace name
     */
    protected static String expandNamespace(String namespace) {
        if (namespace.startsWith("#")) {
            String playerName = namespace.substring(1);

            try {
                Profile profile = WorldGuard.getInstance().getProfileService().findByName(playerName);
                if (profile != null) {
                    return profile.getUniqueId().toString();
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

            throw new InvalidMacroException("Macro unresolvable: " + namespace);
        }

        return namespace;
    }

    /**
     * Get the macro expanded namespace name from a possibly qualified region identifier.
     *
     * @param id the unprocessed region identifier
     * @return an optional containing the expanded namespace name, or an empty string if qualified,
     * if not qualified, an empty optional is returned
     */
    protected static Optional<String> getExpandedNamespace(String id) throws CommandException {
        try {
            return getNamespace(id).map(RegionCommandsBase::expandNamespace);
        } catch (InvalidMacroException ex) {
            throw new CommandException(ex.getMessage());
        }
    }

    /**
     * Get the default namespace for a given actor.
     *
     * @param sender the sender who's default namespace we intend to retrieve.
     * @return the default namespace
     */
    protected static String getDefaultNamespace(Actor sender) {
        if (sender instanceof LocalPlayer) {
            return ((LocalPlayer) sender).getDefaultNamespace();
        }

        return null;
    }

    /**
     * Process a possibly qualified region identifier into a RegionIdentifier.
     *
     * This method takes a possibly qualified region identifier, and processes it, expanding any namespace macros,
     * running permissions checks, and validating the names for invalid character.
     *
     * @param sender the contextual sender to use for checks and macro expansions
     * @param id the possibly unqualified id
     * @param allowGlobal whether or not this method should allow use of the name __global__ name
     * @return the processed region id
     * @throws AuthorizationException if a permission check fails
     * @throws CommandException if a name validation check fails
     */
    protected static RegionIdentifier processRegionId(Actor sender, String id, boolean allowGlobal) throws AuthorizationException, CommandException {
        String unqualifiedName = getUnqualifiedName(id);
        checkName(unqualifiedName, allowGlobal);

        Optional<String> optProvidedNamespace = getExpandedNamespace(id);
        String namespace = optProvidedNamespace.orElse(getDefaultNamespace(sender));

        // TODO use more informative permission checks

        // This needs checked as a special case, before namespace name validation.
        if (namespace.equals("")) {
            sender.checkPermission("worldguard.region.namespace.global");
            return new RegionIdentifier(null, unqualifiedName);
        }

        checkNamespace(namespace);

        try {
            UUID namespacePlayerId = UUID.fromString(namespace);
            if (!namespacePlayerId.equals(sender.getUniqueId())) {
                sender.checkPermission("worldguard.region.namespace.player");
            }
        } catch (IllegalArgumentException ex) {
            sender.checkPermission("worldguard.region.namespace.custom." + namespace);
        }

        return new RegionIdentifier(namespace, unqualifiedName);
    }

    /**
     * Get a protected region by a given name, otherwise throw a
     * {@link CommandException}.
     *
     * <p>This also validates the region ID.</p>
     *
     * @param regionManager the region manager
     * @param id the name to search
     * @param allowGlobal true to allow selecting __global__
     * @throws CommandException thrown if no region is found by the given name
     */
    @Deprecated
    protected static ProtectedRegion checkExistingRegion(RegionManager regionManager, String id, boolean allowGlobal) throws CommandException {
        // Validate the id
        checkRegionId(id, allowGlobal);

        ProtectedRegion region = regionManager.getRegion(id);

        // No region found!
        if (region == null) {
            // But we want a __global__, so let's create one
            if (id.equalsIgnoreCase("__global__")) {
                region = new GlobalProtectedRegion(id);
                regionManager.addRegion(region);
                return region;
            }

            throw new CommandException(
                    "No region could be found with the name of '" + id + "'.");
        }

        return region;
    }

    /**
     * Get a protected region by a given name, otherwise throw a
     * {@link CommandException}.
     *
     * <p>This also validates the region ID.</p>
     *
     * @param actor the associated player
     * @param regionManager the region manager
     * @param id the name to search
     * @throws CommandException thrown if no region is found by the given name
     */
    protected static ProtectedRegion checkExistingRegion(Actor actor, RegionManager regionManager, RegionIdentifier id) throws CommandException {
        ProtectedRegion region = regionManager.getRegion(id);

        // No region found!
        if (region == null) {
            // But we want a __global__, so let's create one
            if (id.getQualifiedName().equals(":__global__")) {
                region = new GlobalProtectedRegion(id);
                regionManager.addRegion(region);
                return region;
            }

            throw new CommandException(
                    "No region could be found with the name of '" + id.getDisplayName(actor) + "'.");
        }

        return region;
    }


    /**
     * Get the region at the player's location, if possible.
     *
     * <p>If the player is standing in several regions, an error will be raised
     * and a list of regions will be provided.</p>
     *
     * @param regionManager the region manager
     * @param player the player
     * @return a region
     * @throws CommandException thrown if no region was found
     */
    protected static ProtectedRegion checkRegionStandingIn(RegionManager regionManager, LocalPlayer player, String rgCmd) throws CommandException {
        return checkRegionStandingIn(regionManager, player, false, rgCmd);
    }

    /**
     * Get the region at the player's location, if possible.
     *
     * <p>If the player is standing in several regions, an error will be raised
     * and a list of regions will be provided.</p>
     *
     * <p>If the player is not standing in any regions, the global region will
     * returned if allowGlobal is true and it exists.</p>
     *
     * @param regionManager the region manager
     * @param player the player
     * @param allowGlobal whether to search for a global region if no others are found
     * @return a region
     * @throws CommandException thrown if no region was found
     */
    protected static ProtectedRegion checkRegionStandingIn(RegionManager regionManager, LocalPlayer player, boolean allowGlobal, String rgCmd) throws CommandException {
        ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().toVector().toBlockPoint());

        if (set.size() == 0) {
            if (allowGlobal) {
                ProtectedRegion global = checkExistingRegion(regionManager, "__global__", true);
                player.printDebug("You're not standing in any " +
                        "regions. Using the global region for this world instead.");
                return global;
            }
            throw new CommandException(
                    "You're not standing in a region." +
                            "Specify an ID if you want to select a specific region.");
        } else if (set.size() > 1) {
            boolean first = true;

            final TextComponent.Builder builder = TextComponent.builder("");
            builder.append(TextComponent.of("Current regions: ", TextColor.GOLD));
            for (ProtectedRegion region : set) {
                if (!first) {
                    builder.append(TextComponent.of(", "));
                }
                first = false;

                RegionIdentifier id = region.getIdentifier();
                TextComponent regionComp = TextComponent.of(id.getDisplayName(player), TextColor.AQUA);
                if (rgCmd != null && rgCmd.contains("%id%")) {
                    regionComp = regionComp.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to pick this region")))
                            .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, rgCmd.replace("%id%", id.getQualifiedName())));
                }
                builder.append(regionComp);
            }
            player.print(builder.build());
            throw new CommandException("Several regions affect your current location (please pick one).");
        }

        return set.iterator().next();
    }

    /**
     * Get a WorldEdit selection for a player, or emit an exception if there is none
     * available.
     *
     * @param player the player
     * @return the selection
     * @throws CommandException thrown on an error
     */
    protected static Region checkSelection(LocalPlayer player) throws CommandException {
        try {
            return WorldEdit.getInstance().getSessionManager().get(player).getRegionSelector(player.getWorld()).getRegion();
        } catch (IncompleteRegionException e) {
            throw new CommandException(
                    "Please select an area first. " +
                            "Use WorldEdit to make a selection! " +
                            "(see: https://worldedit.enginehub.org/en/latest/usage/regions/selections/).");
        }
    }

    /**
     * Check that a region with the given ID does not already exist.
     *
     * @param manager the manager
     * @param id the ID
     * @throws CommandException thrown if the ID already exists
     */
    @Deprecated
    protected static void checkRegionDoesNotExist(RegionManager manager, String id, boolean mayRedefine) throws CommandException {
        checkRegionDoesNotExist(null, manager, new RegionIdentifier(id), mayRedefine);
    }

    /**
     * Check that a region with the given ID does not already exist.
     *
     * @param actor the associated player
     * @param manager the manager
     * @param id the identifier
     * @throws CommandException thrown if the ID already exists
     */
    protected static void checkRegionDoesNotExist(Actor actor, RegionManager manager, RegionIdentifier id, boolean mayRedefine) throws CommandException {
        if (manager.hasRegion(id)) {
            throw new CommandException("A region with that name already exists. Please choose another name." +
                    (mayRedefine ? " To change the shape, use /region redefine " + id.getDisplayName(actor) + "." : ""));
        }
    }

    /**
     * Check that the given region manager is not null.
     *
     * @param world the world
     * @throws CommandException thrown if the manager is null
     */
    protected static RegionManager checkRegionManager(World world) throws CommandException {
        if (!WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world).useRegions) {
            throw new CommandException("Region support is disabled in the target world. " +
                    "It can be enabled per-world in WorldGuard's configuration files. " +
                    "However, you may need to restart your server afterwards.");
        }

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (manager == null) {
            throw new CommandException("Region data failed to load for this world. " +
                    "Please ask a server administrator to read the logs to identify the reason.");
        }
        return manager;
    }

    /**
     * Create a {@link ProtectedRegion} from the player's selection.
     *
     * @param player the player
     * @param id the ID of the new region
     * @return a new region
     * @throws CommandException thrown on an error
     */
    @Deprecated
    protected static ProtectedRegion checkRegionFromSelection(LocalPlayer player, String id) throws CommandException {
        return checkRegionFromSelection(player, new RegionIdentifier(id));
    }

    /**
     * Create a {@link ProtectedRegion} from the player's selection.
     *
     * @param player the player
     * @param id the identifier of the new region
     * @return a new region
     * @throws CommandException thrown on an error
     */
    protected static ProtectedRegion checkRegionFromSelection(LocalPlayer player, RegionIdentifier id) throws CommandException {
        Region selection = checkSelection(player);

        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DRegion) {
            Polygonal2DRegion polySel = (Polygonal2DRegion) selection;
            int minY = polySel.getMinimumPoint().getBlockY();
            int maxY = polySel.getMaximumPoint().getBlockY();
            return new ProtectedPolygonalRegion(id, polySel.getPoints(), minY, maxY);
        } else if (selection instanceof CuboidRegion) {
            BlockVector3 min = selection.getMinimumPoint();
            BlockVector3 max = selection.getMaximumPoint();
            return new ProtectedCuboidRegion(id, min, max);
        } else {
            throw new CommandException("Sorry, you can only use cuboids and polygons for WorldGuard regions.");
        }
    }

    /**
     * Warn the region saving is failing.
     *
     * @param sender the sender to send the message to
     */
    protected static void warnAboutSaveFailures(Actor sender) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        Set<RegionManager> failures = container.getSaveFailures();

        if (!failures.isEmpty()) {
            String failingList = Joiner.on(", ").join(failures.stream()
                    .map(regionManager -> "'" + regionManager.getName() + "'").collect(Collectors.toList()));

            sender.print(TextComponent.of("(Warning: The background saving of region data is failing for these worlds: " + failingList + ". " +
                    "Your changes are getting lost. See the server log for more information.)", TextColor.GOLD));
        }
    }

    /**
     * Warn the sender if the dimensions of the given region are worrying.
     *
     * @param sender the sender to send the message to
     * @param region the region
     */
    protected static void warnAboutDimensions(Actor sender, ProtectedRegion region) {
        int height = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY();
        if (height <= 2) {
            sender.printDebug("(Warning: The height of the region was " + (height + 1) + " block(s).)");
        }
    }

    /**
     * Inform a new user about automatic protection.
     *
     * @param sender the sender to send the message to
     * @param manager the region manager
     * @param region the region
     */
    protected static void informNewUser(Actor sender, RegionManager manager, ProtectedRegion region) {
        if (manager.size() <= 2) {
            sender.print(SubtleFormat.wrap("(This region is NOW PROTECTED from modification from others. Don't want that? Use ")
                            .append(TextComponent.of("/rg flag " + region.getId() + " passthrough allow", TextColor.AQUA))
                            .append(TextComponent.of(")", TextColor.GRAY)));
        }
    }

    /**
     * Inform a user if the region overlaps spawn protection.
     *
     * @param sender the sender to send the message to
     * @param world the world the region is in
     * @param region the region
     */
    protected static void checkSpawnOverlap(Actor sender, World world, ProtectedRegion region) {
        ProtectedRegion spawn = WorldGuard.getInstance().getPlatform().getSpawnProtection(world);
        if (spawn != null) {
            if (!spawn.getIntersectingRegions(ImmutableList.of(region)).isEmpty()) {
                sender.print(ErrorFormat.wrap("Warning!")
                        .append(TextComponent.of(" This region overlaps vanilla's spawn protection. WorldGuard cannot " +
                                "override this, and only server operators will be able to interact with this area.")));
            }
        }
    }

    /**
     * Set a player's selection to a given region.
     *
     * @param player the player
     * @param region the region
     * @throws CommandException thrown on a command error
     */
    protected static void setPlayerSelection(LocalPlayer player, ProtectedRegion region) throws CommandException {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);

        // Set selection
        if (region instanceof ProtectedCuboidRegion) {
            ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
            BlockVector3 pt1 = cuboid.getMinimumPoint();
            BlockVector3 pt2 = cuboid.getMaximumPoint();

            CuboidRegionSelector selector = new CuboidRegionSelector(player.getWorld(), pt1, pt2);
            session.setRegionSelector(player.getWorld(), selector);
            selector.explainRegionAdjust(player, session);
            player.print("Region selected as a cuboid.");

        } else if (region instanceof ProtectedPolygonalRegion) {
            ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion) region;
            Polygonal2DRegionSelector selector = new Polygonal2DRegionSelector(
                    player.getWorld(), poly2d.getPoints(),
                    poly2d.getMinimumPoint().getBlockY(),
                    poly2d.getMaximumPoint().getBlockY());
            session.setRegionSelector(player.getWorld(), selector);
            selector.explainRegionAdjust(player, session);
            player.print("Region selected as a polygon.");

        } else if (region instanceof GlobalProtectedRegion) {
            throw new CommandException(
                    "Can't select global regions! " +
                            "That would cover the entire world.");

        } else {
            throw new CommandException("Unknown region type: " +
                    region.getClass().getCanonicalName());
        }
    }

    /**
     * Utility method to set a flag.
     *
     * @param region the region
     * @param flag the flag
     * @param sender the sender
     * @param value the value
     * @throws InvalidFlagFormat thrown if the value is invalid
     */
    protected static <V> V setFlag(ProtectedRegion region, Flag<V> flag, Actor sender, String value) throws InvalidFlagFormat {
        V val = flag.parseInput(FlagContext.create().setSender(sender).setInput(value).setObject("region", region).build());
        region.setFlag(flag, val);
        return val;
    }

}
