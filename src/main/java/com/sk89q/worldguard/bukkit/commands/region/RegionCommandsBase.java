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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

class RegionCommandsBase {

    protected RegionCommandsBase() {
    }

    /**
     * Get the permission model to lookup permissions.
     *
     * @param sender the sender
     * @return the permission model
     */
    protected static RegionPermissionModel getPermissionModel(CommandSender sender) {
        return new RegionPermissionModel(WorldGuardPlugin.inst(), sender);
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
    protected static World checkWorld(CommandContext args, CommandSender sender, char flag) throws CommandException {
        if (args.hasFlag(flag)) {
            return WorldGuardPlugin.inst().matchWorld(sender, args.getFlag(flag));
        } else {
            if (sender instanceof Player) {
                return WorldGuardPlugin.inst().checkPlayer(sender).getWorld();
            } else {
                throw new CommandException("Please specify " + "the world with -" + flag + " world_name.");
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
                    "The region name of '" + id + "' contains characters that are not allowed.");
        }

        if (!allowGlobal && id.equalsIgnoreCase("__global__")) { // Sorry, no global
            throw new CommandException(
                    "Sorry, you can't use __global__ here.");
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
                    "No region could be found with the name of '" + id + "'.");
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
    protected static ProtectedRegion checkRegionStandingIn(RegionManager regionManager, Player player) throws CommandException {
        return checkRegionStandingIn(regionManager, player, false);
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
    protected static ProtectedRegion checkRegionStandingIn(RegionManager regionManager, Player player, boolean allowGlobal) throws CommandException {
        ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation());

        if (set.size() == 0) {
            if (allowGlobal) {
                ProtectedRegion global = checkExistingRegion(regionManager, "__global__", true);
                player.sendMessage(ChatColor.GRAY + "You're not standing in any " +
                        "regions. Using the global region for this world instead.");
                return global;
            }
            throw new CommandException(
                    "You're not standing in a region." +
                            "Specify an ID if you want to select a specific region.");
        } else if (set.size() > 1) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;

            for (ProtectedRegion region : set) {
                if (!first) {
                    builder.append(", ");
                }
                first = false;
                builder.append(region.getId());
            }

            throw new CommandException(
                    "You're standing in several regions (please pick one).\nYou're in: " + builder.toString());
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
    protected static Selection checkSelection(Player player) throws CommandException {
        WorldEditPlugin worldEdit = WorldGuardPlugin.inst().getWorldEdit();
        Selection selection = worldEdit.getSelection(player);

        if (selection == null) {
            throw new CommandException(
                    "Please select an area first. " +
                            "Use WorldEdit to make a selection! " +
                            "(wiki: http://wiki.sk89q.com/wiki/WorldEdit).");
        }

        return selection;
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
            throw new CommandException("A region with that name already exists. Please choose another name." +
                    (mayRedefine ? " To change the shape, use /region redefine " + id + "." : ""));
        }
    }

    /**
     * Check that the given region manager is not null.
     *
     * @param plugin the plugin
     * @param world the world
     * @throws CommandException thrown if the manager is null
     */
    protected static RegionManager checkRegionManager(WorldGuardPlugin plugin, World world) throws CommandException {
        if (!plugin.getGlobalStateManager().get(world).useRegions) {
            throw new CommandException("Region support is disabled in the target world. " +
                    "It can be enabled per-world in WorldGuard's configuration files. " +
                    "However, you may need to restart your server afterwards.");
        }

        RegionManager manager = plugin.getRegionContainer().get(world);
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
    protected static ProtectedRegion checkRegionFromSelection(Player player, String id) throws CommandException {
        Selection selection = checkSelection(player);

        // Detect the type of region from WorldEdit
        if (selection instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) selection;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            return new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (selection instanceof CuboidSelection) {
            BlockVector min = selection.getNativeMinimumPoint().toBlockVector();
            BlockVector max = selection.getNativeMaximumPoint().toBlockVector();
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
    protected static void warnAboutSaveFailures(CommandSender sender) {
        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        Set<RegionManager> failures = container.getSaveFailures();

        if (failures.size() > 0) {
            String failingList = Joiner.on(", ").join(Iterables.transform(failures, new Function<RegionManager, String>() {
                @Override
                public String apply(RegionManager regionManager) {
                    return "'" + regionManager.getName() + "'";
                }
            }));

            sender.sendMessage(ChatColor.GOLD +
                    "(Warning: The background saving of region data is failing for these worlds: " + failingList + ". " +
                    "Your changes are getting lost. See the server log for more information.)");
        }
    }

    /**
     * Warn the sender if the dimensions of the given region are worrying.
     *
     * @param sender the sender to send the message to
     * @param region the region
     */
    protected static void warnAboutDimensions(CommandSender sender, ProtectedRegion region) {
        int height = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY();
        if (height <= 2) {
            sender.sendMessage(ChatColor.GRAY + "(Warning: The height of the region was " + (height + 1) + " block(s).)");
        }
    }

    /**
     * Inform a new user about automatic protection.
     *
     * @param sender the sender to send the message to
     * @param manager the region manager
     * @param region the region
     */
    protected static void informNewUser(CommandSender sender, RegionManager manager, ProtectedRegion region) {
        if (manager.getRegions().size() <= 2) {
            sender.sendMessage(ChatColor.GRAY +
                    "(This region is NOW PROTECTED from modification from others. " +
                    "Don't want that? Use " +
                    ChatColor.AQUA + "/rg flag " + region.getId() + " passthrough allow" +
                    ChatColor.GRAY + ")");
        }
    }

    /**
     * Set a player's selection to a given region.
     *
     * @param player the player
     * @param region the region
     * @throws CommandException thrown on a command error
     */
    protected static void setPlayerSelection(Player player, ProtectedRegion region) throws CommandException {
        WorldEditPlugin worldEdit = WorldGuardPlugin.inst().getWorldEdit();

        World world = player.getWorld();

        // Set selection
        if (region instanceof ProtectedCuboidRegion) {
            ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
            Vector pt1 = cuboid.getMinimumPoint();
            Vector pt2 = cuboid.getMaximumPoint();
            CuboidSelection selection = new CuboidSelection(world, pt1, pt2);
            worldEdit.setSelection(player, selection);
            player.sendMessage(ChatColor.YELLOW + "Region selected as a cuboid.");

        } else if (region instanceof ProtectedPolygonalRegion) {
            ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion) region;
            Polygonal2DSelection selection = new Polygonal2DSelection(
                    world, poly2d.getPoints(),
                    poly2d.getMinimumPoint().getBlockY(),
                    poly2d.getMaximumPoint().getBlockY() );
            worldEdit.setSelection(player, selection);
            player.sendMessage(ChatColor.YELLOW + "Region selected as a polygon.");

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
    protected static <V> void setFlag(ProtectedRegion region, Flag<V> flag, CommandSender sender, String value) throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(WorldGuardPlugin.inst(), sender, value));
    }

}
