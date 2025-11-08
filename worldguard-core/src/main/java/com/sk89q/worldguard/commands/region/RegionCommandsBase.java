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
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.component.SubtleFormat;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery.QueryOption;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;

import java.util.Set;
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
     * @param args the arguments
     * @param sender the sender
     * @param flag the flag (such as 'w')
     * @return a world
     * @throws CommandException on error
     */
    protected static World checkWorld(CommandContext args, Actor sender, char flag) throws CommandException {
        return checkWorld(args, sender, flag, true);
    }

    protected static World checkWorld(CommandContext args, Actor sender, char flag, boolean allowWorldEditOverride) throws CommandException {
        if (args.hasFlag(flag)) {
            return WorldGuard.getInstance().getPlatform().getMatcher().matchWorld(sender, args.getFlag(flag));
        } else {
            if (allowWorldEditOverride) {
                try {
                    World override = WorldEdit.getInstance().getSessionManager().get(sender).getWorldOverride();
                    if (override != null) {
                        if (sender instanceof LocalPlayer && !override.equals(((LocalPlayer) sender).getWorld())) {
                            sender.printDebug(TextComponent.of(message("commands.region.debug.world-override", override.getName())));
                        }
                        return override;
                    }
                } catch (NoSuchMethodError ignored) {
                }
            }
            if (sender instanceof LocalPlayer) {
                return ((LocalPlayer) sender).getWorld();
            } else {
                throw new CommandException(message("commands.region.error.specify-world", flag));
            }
        }
    }

    /**
     * Validate a region ID.
     *
     * @param id the id
     * @param allowGlobal whether __global__ is allowed
     * @return the id given
     * @throws CommandException thrown on an error
     */
    protected static String checkRegionId(String id, boolean allowGlobal) throws CommandException {
        if (!ProtectedRegion.isValidId(id)) {
            throw new CommandException(
                    message("commands.region.error.invalid-name", id));
        }

        if (!allowGlobal && id.equalsIgnoreCase("__global__")) { // Sorry, no global
            throw new CommandException(
                    message("commands.region.error.global-disallowed"));
        }

        return id;
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
                    message("commands.region.error.not-found", id));
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
        ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation().toVector().toBlockPoint(), QueryOption.SORT);

        if (set.size() == 0) {
            if (allowGlobal) {
                ProtectedRegion global = checkExistingRegion(regionManager, "__global__", true);
                player.printDebug(message("commands.region.standing.none-debug"));
                return global;
            }
            throw new CommandException(
                    message("commands.region.error.not-standing"));
        } else if (set.size() > 1) {
            boolean first = true;

            final TextComponent.Builder builder = TextComponent.builder("");
            builder.append(TextComponent.of(message("commands.region.list.header"), TextColor.GOLD));
            for (ProtectedRegion region : set) {
                if (!first) {
                    builder.append(TextComponent.of(message("commands.region.list.delimiter")));
                }
                first = false;
                TextComponent regionComp = TextComponent.of(region.getId(), TextColor.AQUA);
                if (rgCmd != null && rgCmd.contains("%id%")) {
                    regionComp = regionComp.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(message("commands.region.list.hover"))))
                            .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, rgCmd.replace("%id%", region.getId())));
                }
                builder.append(regionComp);
            }
            player.print(builder.build());
            throw new CommandException(message("commands.region.error.multiple-standing"));
        }

        return set.iterator().next();
    }

    /**
     * Get a WorldEdit selection for an actor, or emit an exception if there is none
     * available.
     *
     * @param actor the actor
     * @return the selection
     * @throws CommandException thrown on an error
     */
    protected static Region checkSelection(Actor actor) throws CommandException {
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(actor);
        try {
            if (localSession == null || localSession.getSelectionWorld() == null) {
                throw new IncompleteRegionException();
            }
            return localSession.getRegionSelector(localSession.getSelectionWorld()).getRegion();
        } catch (IncompleteRegionException e) {
            throw new CommandException(message("commands.region.error.selection-missing"));
        }
    }

    /**
     * Check that a region with the given ID does not already exist.
     *
     * @param manager the manager
     * @param id the ID
     * @throws CommandException thrown if the ID already exists
     */
    protected static void checkRegionDoesNotExist(RegionManager manager, String id, boolean mayRedefine) throws CommandException {
        if (manager.hasRegion(id)) {
            if (mayRedefine) {
                throw new CommandException(message("commands.region.error.exists-redefine", id));
            }
            throw new CommandException(message("commands.region.error.exists"));
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
            throw new CommandException(message("commands.region.error.disabled"));
        }

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (manager == null) {
            throw new CommandException(message("commands.region.error.load-failed"));
        }
        return manager;
    }

    /**
     * Create a {@link ProtectedRegion} from the actor's selection.
     *
     * @param actor the actor
     * @param id the ID of the new region
     * @return a new region
     * @throws CommandException thrown on an error
     */
    protected static ProtectedRegion checkRegionFromSelection(Actor actor, String id) throws CommandException {
        Region selection = checkSelection(actor);

        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DRegion) {
            Polygonal2DRegion polySel = (Polygonal2DRegion) selection;
            int minY = polySel.getMinimumPoint().y();
            int maxY = polySel.getMaximumPoint().y();
            return new ProtectedPolygonalRegion(id, polySel.getPoints(), minY, maxY);
        } else if (selection instanceof CuboidRegion) {
            BlockVector3 min = selection.getMinimumPoint();
            BlockVector3 max = selection.getMaximumPoint();
            return new ProtectedCuboidRegion(id, min, max);
        } else {
            throw new CommandException(message("commands.region.error.selection-type"));
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

            sender.print(TextComponent.of(message("commands.region.warning.save-failure", failingList), TextColor.GOLD));
        }
    }

    /**
     * Warn the sender if the dimensions of the given region are worrying.
     *
     * @param sender the sender to send the message to
     * @param region the region
     */
    protected static void warnAboutDimensions(Actor sender, ProtectedRegion region) {
        if (region instanceof GlobalProtectedRegion) {
            return;
        }
        int height = region.getMaximumPoint().y() - region.getMinimumPoint().y();
        if (height <= 2) {
            sender.printDebug(message("commands.region.warning.height", height + 1));
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
            sender.print(SubtleFormat.wrap(message("commands.region.info.auto-protect.prefix"))
                            .append(TextComponent.of("/rg flag " + region.getId() + " passthrough allow", TextColor.AQUA))
                            .append(TextComponent.of(message("commands.region.info.auto-protect.suffix"), TextColor.GRAY)));
        }
    }

    /**
     * Inform a user if the region overlaps spawn protection.
     *
     * @param sender the sender to send the message to
     * @param world the world the region is in
     * @param region the region
     */
    protected static boolean checkSpawnOverlap(Actor sender, World world, ProtectedRegion region) {
        ProtectedRegion spawn = WorldGuard.getInstance().getPlatform().getSpawnProtection(world);
        if (spawn != null) {
            if (!spawn.getIntersectingRegions(ImmutableList.of(region)).isEmpty()) {
                sender.print(ErrorFormat.wrap(message("commands.region.spawn.warning-title"))
                        .append(TextComponent.of(message("commands.region.spawn.warning-body"), TextColor.WHITE)));
                return true;
            }
        }
        return false;
    }

    /**
     * Set an actor's selection to a given region.
     *
     * @param actor the actor
     * @param region the region
     * @throws CommandException thrown on a command error
     */
    protected static void setPlayerSelection(Actor actor, ProtectedRegion region, World world) throws CommandException {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);

        RegionSelector selector = WorldEditRegionConverter.convertToSelector(region);
        if (selector != null) {
            selector.setWorld(world);
            session.setRegionSelector(world, selector);
            selector.explainRegionAdjust(actor, session);
            actor.print(message("commands.region.selection.set", region.getType().getName()));
        } else {
            throw new CommandException(message("commands.region.error.select-unsupported", region.getType().getName()));
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

    protected static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }
}
