// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldguard.bukkit.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.LanguageManager;
import com.sk89q.worldguard.bukkit.RegionPermissionModel;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;
import com.sk89q.worldguard.protection.databases.migrators.AbstractDatabaseMigrator;
import com.sk89q.worldguard.protection.databases.migrators.MigrationException;
import com.sk89q.worldguard.protection.databases.migrators.MigratorKey;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

/**
 * Implements the /region commands for WorldGuard.
 */
public final class RegionCommands {

    private final WorldGuardPlugin plugin;
    private final LanguageManager lang;

    private MigratorKey migrateDBRequest;
    private Date migrateDBRequestDate;

    public RegionCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        lang = plugin.getLang();
    }

    /**
     * Get the permission model to lookup permissions.
     *
     * @param sender the sender
     * @return the permission model
     */
    private static RegionPermissionModel getPermissionModel(CommandSender sender) {
        return new RegionPermissionModel(WorldGuardPlugin.inst(), sender);
    }

    /**
     * Gets the world from the given flag, or falling back to the the current
     * player if the sender is a player, otherwise reporting an error.
     *
     * @param args the arguments
     * @param sender the sender
     * @param flag the flag (such as 'w')
     * @param lang the language manager object.
     * @return a world
     * @throws CommandException on error
     */
    private static World getWorld(CommandContext args, CommandSender sender,
            char flag, LanguageManager lang)
            throws CommandException {
        if (args.hasFlag(flag)) {
            return WorldGuardPlugin.inst().matchWorld(sender, args.getFlag(flag));
        } else {
            if (sender instanceof Player) {
                return WorldGuardPlugin.inst().checkPlayer(sender).getWorld();
            } else {
                throw new CommandException(
                        lang.getText("specify-the-world-with-vflag",
                                "%FLAG%", "" + flag));
            }
        }
    }

    /**
     * Validate a region ID.
     *
     * @param id the id
     * @param allowGlobal whether __global__ is allowed
     * @param lang the languaje object
     * @return the id given
     * @throws CommandException thrown on an error
     */
    private static String validateRegionId(String id, boolean allowGlobal, LanguageManager lang)
            throws CommandException {
        if (!ProtectedRegion.isValidId(id)) {
            throw new CommandException(
                    lang.getText("region-vid-contains-not-allowed-characters",
                            "%ID%", id));
        }

        if (!allowGlobal && id.equalsIgnoreCase("__global__")) { // Sorry, no global
            throw new CommandException(lang.getText("cant-use-global-here"));
        }

        return id;
    }

    /**
     * Get a protected region by a given name, otherwise throw a
     * {@link CommandException}.
     *
     * <p>
     * This also validates the region ID.</p>
     *
     * @param regionManager the region manager
     * @param id the name to search
     * @param lang the languaje object
     * @param allowGlobal true to allow selecting __global__
     * @throws CommandException thrown if no region is found by the given name
     */
    private static ProtectedRegion findExistingRegion(
            RegionManager regionManager, String id, boolean allowGlobal, LanguageManager lang)
            throws CommandException {
        // Validate the id
        validateRegionId(id, allowGlobal, lang);

        ProtectedRegion region = regionManager.getRegionExact(id);

        // No region found!
        if (region == null) {
            // But we want a __global__, so let's create one
            if (id.equalsIgnoreCase("__global__")) {
                region = new GlobalProtectedRegion(id);
                regionManager.addRegion(region);
                return region;
            }

            throw new CommandException(lang.getText("no-region-could-be-found-vid", "%ID%", id));
        }

        return region;
    }

    /**
     * Get the region at the player's location, if possible.
     *
     * <p>
     * If the player is standing in several regions, an error will be raised and
     * a list of regions will be provided.</p>
     *
     * @param regionManager the region manager
     * @param player the player
     * @param lang the language manager object.
     * @return a region
     * @throws CommandException thrown if no region was found
     */
    private static ProtectedRegion findRegionStandingIn(
            RegionManager regionManager, Player player, LanguageManager lang)
            throws CommandException {
        return findRegionStandingIn(regionManager, player, false, lang);
    }

    /**
     * Get the region at the player's location, if possible.
     *
     * <p>
     * If the player is standing in several regions, an error will be raised and
     * a list of regions will be provided.</p>
     *
     * <p>
     * If the player is not standing in any regions, the global region will
     * returned if allowGlobal is true and it exists.</p>
     *
     * @param regionManager the region manager
     * @param player the player
     * @param allowGlobal whether to search for a global region if no others are
     * found
     * @param lang the language manager object.
     * @return a region
     * @throws CommandException thrown if no region was found
     */
    private static ProtectedRegion findRegionStandingIn(
            RegionManager regionManager, Player player, boolean allowGlobal,
            LanguageManager lang) throws CommandException {
        ApplicableRegionSet set = regionManager.getApplicableRegions(
                player.getLocation());

        if (set.size() == 0) {
            if (allowGlobal) {
                ProtectedRegion global = findExistingRegion(regionManager, "__global__", true, lang);
                player.sendMessage(lang.getText("not-standing-in-any-regions"));
                return global;
            }
            throw new CommandException(lang.getText("not-standing-in-a-region"));
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

            throw new CommandException(lang.getText("standing-in-several-regions-vlist",
                    "%LIST%", builder.toString()));
        }

        return set.iterator().next();
    }

    /**
     * Get a WorldEdit selection for a player, or emit an exception if there is
     * none available.
     *
     * @param player the player
     * @param lang the language manager object.
     * @return the selection
     * @throws CommandException thrown on an error
     */
    private static Selection getSelection(Player player, LanguageManager lang)
            throws CommandException {
        WorldEditPlugin worldEdit = WorldGuardPlugin.inst().getWorldEdit();
        Selection selection = worldEdit.getSelection(player);

        if (selection == null) {
            throw new CommandException(lang.getText("select-an-area-first"));
        }

        return selection;
    }

    /**
     * Create a {@link ProtectedRegion} from the player's selection.
     *
     * @param player the player
     * @param id the ID of the new region
     * @param lang the language manager object.
     * @return a new region
     * @throws CommandException thrown on an error
     */
    private static ProtectedRegion createRegionFromSelection(Player player,
            String id, LanguageManager lang)
            throws CommandException {

        Selection selection = getSelection(player, lang);

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
            throw new CommandException(lang.getText("use-cuboids-and-polygons"));
        }
    }

    private static void commitChanges(CommandSender sender,
            RegionManager regionManager, LanguageManager lang)
            throws CommandException {
        commitChanges(sender, regionManager, false, lang);
    }

    private static void reloadChanges(CommandSender sender,
            RegionManager regionManager, LanguageManager lang)
            throws CommandException {
        reloadChanges(sender, regionManager, false, lang);
    }

    /**
     * Save the region database.
     *
     * @param sender the sender
     * @param regionManager the region manager
     * @param silent whether to suppress messages sent to the player
     * @param lang the language manager object.
     * @throws CommandException throw on an error
     */
    private static void commitChanges(CommandSender sender,
            RegionManager regionManager, boolean silent, LanguageManager lang)
            throws CommandException {
        try {
            if (!silent && regionManager.getRegions().size() >= 500) {
                sender.sendMessage(lang.getText("saving-region-list"));
            }
            regionManager.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException(lang.getText("regions-did-not-save-verror",
                    "%ERROR%", e.getMessage()));
        }
    }

    /**
     * Load the region database.
     *
     * @param sender the sender
     * @param regionManager the region manager
     * @param silent whether to suppress messages sent to the player
     * @param lang the language manager object.
     * @throws CommandException throw on an error
     */
    private static void reloadChanges(CommandSender sender,
            RegionManager regionManager, boolean silent, LanguageManager lang)
            throws CommandException {
        try {
            if (!silent && regionManager.getRegions().size() >= 500) {
                sender.sendMessage(lang.getText("loading-region-list"));
            }
            regionManager.load();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException(lang.getText("regions-did-not-load-verror",
                    "%ERROR%", e.getMessage()));
        }
    }

    /**
     * Set a player's selection to a given region.
     *
     * @param player the player
     * @param region the region
     * @param lang the language manager object.
     * @throws CommandException thrown on a command error
     */
    private static void setPlayerSelection(Player player,
            ProtectedRegion region, LanguageManager lang)
            throws CommandException {
        WorldEditPlugin worldEdit = WorldGuardPlugin.inst().getWorldEdit();

        World world = player.getWorld();

        // Set selection
        if (region instanceof ProtectedCuboidRegion) {
            ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
            Vector pt1 = cuboid.getMinimumPoint();
            Vector pt2 = cuboid.getMaximumPoint();
            CuboidSelection selection = new CuboidSelection(world, pt1, pt2);
            worldEdit.setSelection(player, selection);
            player.sendMessage(lang.getText("cuboid-region-selected"));

        } else if (region instanceof ProtectedPolygonalRegion) {
            ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion) region;
            Polygonal2DSelection selection = new Polygonal2DSelection(
                    world, poly2d.getPoints(),
                    poly2d.getMinimumPoint().getBlockY(),
                    poly2d.getMaximumPoint().getBlockY());
            worldEdit.setSelection(player, selection);
            player.sendMessage(lang.getText("polygon-region-selected"));

        } else if (region instanceof GlobalProtectedRegion) {
            throw new CommandException(lang.getText("cant-select-global-regions"));

        } else {
            throw new CommandException(lang.getText("unknown-region-type-vregion",
                    "%REGION%", region.getClass().getCanonicalName()));
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
    private static <V> void setFlag(ProtectedRegion region,
            Flag<V> flag, CommandSender sender, String value)
            throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(WorldGuardPlugin.inst(), sender, value));
    }

    /**
     * Defines a new region.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"define", "def", "d", "create"},
            usage = "<id> [<owner1> [<owner2> [<owners...>]]]",
            desc = "Defines a region",
            min = 1)
    public void define(CommandContext args, CommandSender sender)
            throws CommandException {
        Player player = plugin.checkPlayer(sender);

        // Check permissions
        if (!getPermissionModel(sender).mayDefine()) {
            throw new CommandPermissionsException();
        }

        // Get and validate the region ID
        String id = validateRegionId(args.getString(0), false, lang);

        // Can't replace regions with this command
        RegionManager regionManager = plugin.getGlobalRegionManager().get(player.getWorld());
        if (regionManager.hasRegion(id)) {
            throw new CommandException(lang.getText("region-already-defined-vid", "%ID%", id));
        }

        // Make a region from the user's selection
        ProtectedRegion region = createRegionFromSelection(player, id, lang);

        // Get the list of region owners
        if (args.argsLength() > 1) {
            region.setOwners(RegionDBUtil.parseDomainString(args.getSlice(1), 1));
        }

        regionManager.addRegion(region);
        commitChanges(sender, regionManager, lang); // Save to disk

        // Issue a warning about height
        int height = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY();
        if (height <= 2) {
            sender.sendMessage(lang.getText("height-of-the-region-was",
                    "%HEIGHT%", "" + (height + 1)));
        }

        // Hint
        if (regionManager.getRegions().size() <= 2) {
            sender.sendMessage(lang.getText("region-protected-from-others")
                    + lang.getText("use-passthrough-allow-vid", "%ID%", id, false));
        }

        // Tell the user
        sender.sendMessage(lang.getText("new-region-has-been-made-named-vname", "%ID%", id));
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
            desc = "Re-defines the shape of a region",
            min = 1, max = 1)
    public void redefine(CommandContext args, CommandSender sender)
            throws CommandException {
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();

        // Get and validate the region ID
        String id = validateRegionId(args.getString(0), false, lang);

        // Lookup the existing region
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion existing = findExistingRegion(regionManager, id, false, lang);

        // Check permissions
        if (!getPermissionModel(sender).mayRedefine(existing)) {
            throw new CommandPermissionsException();
        }

        // Make a region from the user's selection
        ProtectedRegion region = createRegionFromSelection(player, id, lang);

        // Copy details from the old region to the new one
        region.setMembers(existing.getMembers());
        region.setOwners(existing.getOwners());
        region.setFlags(existing.getFlags());
        region.setPriority(existing.getPriority());
        try {
            region.setParent(existing.getParent());
        } catch (CircularInheritanceException ignore) {
            // This should not be thrown
        }

        regionManager.addRegion(region); // Replace region
        commitChanges(sender, regionManager, lang); // Save to disk

        // Issue a warning about height
        int height = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY();
        if (height <= 2) {
            sender.sendMessage(lang.getText("height-of-the-region-was",
                    "%HEIGHT%", "" + (height + 1)));

        }

        sender.sendMessage(lang.getText("region-vid-updated-with-new-area", "%ID%", id));
    }

    /**
     * Claiming command for users.
     *
     * <p>
     * This command is a joke and it needs to be rewritten. It was contributed
     * code :(</p>
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"claim"},
            usage = "<id> [<owner1> [<owner2> [<owners...>]]]",
            desc = "Claim a region",
            min = 1)
    public void claim(CommandContext args, CommandSender sender) throws CommandException {
        Player player = plugin.checkPlayer(sender);
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        RegionPermissionModel permModel = getPermissionModel(sender);

        // Check permissions
        if (!permModel.mayClaim()) {
            throw new CommandPermissionsException();
        }

        // Get and validate the region ID
        String id = validateRegionId(args.getString(0), false, lang);

        // Can't replace existing regions
        RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
        if (mgr.hasRegion(id)) {
            throw new CommandException(lang.getText("region-already-exists"));
        }

        // Make a region from the user's selection
        ProtectedRegion region = createRegionFromSelection(player, id, lang);

        // Get the list of region owners
        if (args.argsLength() > 1) {
            region.setOwners(RegionDBUtil.parseDomainString(args.getSlice(1), 1));
        }

        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());

        // Check whether the player has created too many regions
        if (!permModel.mayClaimRegionsUnbounded()) {
            int maxRegionCount = wcfg.getMaxRegionCount(player);
            if (maxRegionCount >= 0
                    && mgr.getRegionCountOfPlayer(localPlayer) >= maxRegionCount) {
                throw new CommandException(lang.getText("you-own-too-many-regions"));
            }
        }

        ProtectedRegion existing = mgr.getRegionExact(id);

        // Check for an existing region
        if (existing != null) {
            if (!existing.getOwners().contains(localPlayer)) {
                throw new CommandException(lang.getText("not-own-region-already-exists"));
            }
        }

        // We have to check whether this region violates the space of any other reion
        ApplicableRegionSet regions = mgr.getApplicableRegions(region);

        // Check if this region overlaps any other region
        if (regions.size() > 0) {
            if (!regions.isOwnerOfAll(localPlayer)) {
                throw new CommandException(lang.getText("region-overlaps"));
            }
        } else {
            if (wcfg.claimOnlyInsideExistingRegions) {
                throw new CommandException(lang.getText("only-claim-regions-inside-yours"));
            }
        }

        // Check claim volume
        if (!permModel.mayClaimRegionsUnbounded()) {
            if (region.volume() > wcfg.maxClaimVolume) {
                player.sendMessage(lang.getText("region-too-large"));
                player.sendMessage(lang.getText("max-claim-volume",
                        "%MAXCLAIMVOLUME%", "" + wcfg.maxClaimVolume)
                        + lang.getText("your-volume", "%REGIONVOLUME%", "" + region.volume()));
                return;
            }
        }

        /*if (plugin.getGlobalConfiguration().getiConomy() != null && wcfg.useiConomy && wcfg.buyOnClaim) {
         if (iConomy.getBank().hasAccount(player.getName())) {
         Account account = iConomy.getBank().getAccount(player.getName());
         double balance = account.getBalance();
         double regionCosts = region.countBlocks() * wcfg.buyOnClaimPrice;
         if (balance >= regionCosts) {
         account.subtract(regionCosts);
         player.sendMessage(ChatColor.YELLOW + "You have bought that region for "
         + iConomy.getBank().format(regionCosts));
         account.save();
         } else {
         player.sendMessage(ChatColor.RED + "You have not enough money.");
         player.sendMessage(ChatColor.RED + "The region you want to claim costs "
         + iConomy.getBank().format(regionCosts));
         player.sendMessage(ChatColor.RED + "You have " + iConomy.getBank().format(balance));
         return;
         }
         } else {
         player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
         return;
         }
         }*/
        region.getOwners().addPlayer(player.getName());

        mgr.addRegion(region);
        commitChanges(sender, mgr, lang); // Save to disk
        sender.sendMessage(lang.getText("region-vid-updated-with-new-area", "%ID%", id));
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
            desc = "Load a region as a WorldEdit selection",
            min = 0, max = 1)
    public void select(CommandContext args, CommandSender sender) throws CommandException {
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion existing;

        // If no arguments were given, get the region that the player is inside
        if (args.argsLength() == 0) {
            existing = findRegionStandingIn(regionManager, player, lang);
        } else {
            existing = findExistingRegion(regionManager, args.getString(0), false, lang);
        }

        // Check permissions
        if (!getPermissionModel(sender).maySelect(existing)) {
            throw new CommandPermissionsException();
        }

        // Select
        setPlayerSelection(player, existing, lang);
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
            flags = "sw:",
            desc = "Get information about a region",
            min = 0, max = 1)
    public void info(CommandContext args, CommandSender sender) throws CommandException {
        World world = getWorld(args, sender, 'w', lang); // Get the world
        RegionPermissionModel permModel = getPermissionModel(sender);

        // Lookup the existing region
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion existing;

        if (args.argsLength() == 0) { // Get region from where the player is
            if (!(sender instanceof Player)) {
                throw new CommandException(lang.getText("specify-the-region"));
            }

            existing = findRegionStandingIn(regionManager, (Player) sender, true, lang);
        } else { // Get region from the ID
            existing = findExistingRegion(regionManager, args.getString(0), true, lang);
        }

        // Check permissions
        if (!permModel.mayLookup(existing)) {
            throw new CommandPermissionsException();
        }

        // Print region information
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing, lang);
        printout.appendRegionInfo();
        printout.send(sender);

        // Let the player also select the region
        if (args.hasFlag('s')) {
            // Check permissions
            if (!permModel.maySelect(existing)) {
                throw new CommandPermissionsException();
            }

            setPlayerSelection(plugin.checkPlayer(sender), existing, lang);
        }
    }

    /**
     * List regions.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"list"},
            usage = "[page]",
            desc = "Get a list of regions",
            flags = "p:w:",
            max = 1)
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        World world = getWorld(args, sender, 'w', lang); // Get the world
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

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        Map<String, ProtectedRegion> regions = mgr.getRegions();

        // Build a list of regions to show
        List<RegionListEntry> entries = new ArrayList<RegionListEntry>();

        int index = 0;
        for (String id : regions.keySet()) {
            RegionListEntry entry = new RegionListEntry(id, index++);

            // Filtering by owner?
            if (ownedBy != null) {
                entry.isOwner = regions.get(id).isOwner(ownedBy);
                entry.isMember = regions.get(id).isMember(ownedBy);

                if (!entry.isOwner && !entry.isMember) {
                    continue; // Skip
                }
            }

            entries.add(entry);
        }

        Collections.sort(entries);

        final int totalSize = entries.size();
        final int pageSize = 10;
        final int pages = (int) Math.ceil(totalSize / (float) pageSize);

        sender.sendMessage(
                (ownedBy == null
                ? lang.getText("region-page")
                : lang.getText("regions-for-vowner-page", "%OWNER%", ownedBy))
                + (page + 1) + lang.getText("-of-") + pages + "):");

        if (page < pages) {
            // Print
            for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
                if (i >= totalSize) {
                    break;
                }

                sender.sendMessage(ChatColor.YELLOW.toString() + entries.get(i));
            }
        }
    }

    /**
     * Set a flag.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"flag", "f"},
            usage = "<id> <flag> [-w world] [-g group] [value]",
            flags = "g:w:",
            desc = "Set flags",
            min = 2)
    public void flag(CommandContext args, CommandSender sender) throws CommandException {
        World world = getWorld(args, sender, 'w', lang); // Get the world
        String flagName = args.getString(1);
        String value = args.argsLength() >= 3 ? args.getJoinedStrings(2) : null;
        RegionGroup groupValue = null;
        RegionPermissionModel permModel = getPermissionModel(sender);

        // Lookup the existing region
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion existing = findExistingRegion(regionManager,
                args.getString(0), true, lang);

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

            sender.sendMessage(lang.getText("unknown-flag-specified-vflag", "%FLAGNAME%", flagName));
            sender.sendMessage(lang.getText("available-flags-vlist", "%LIST%", list.toString()));

            return;
        }

        // Also make sure that we can use this flag
        // This permission is confusing and probably should be replaced, but
        // but not here -- in the model
        if (!permModel.maySetFlag(existing, foundFlag)) {
            throw new CommandPermissionsException();
        }

        // -g for group flag
        if (args.hasFlag('g')) {
            String group = args.getFlag('g');
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();

            if (groupFlag == null) {
                throw new CommandException("Region flag '" + foundFlag.getName()
                        + "' does not have a group flag!");
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

            sender.sendMessage(lang.getText("region-flag-vflag",
                    "%FLAG%", foundFlag.getName())
                    + lang.getText("-set-on-vid", "%ID%", existing.getId(), false)
                    + lang.getText("-to-vvalue", "%VALUE%", value, false));

            // No value? Clear the flag, if -g isn't specified
        } else if (!args.hasFlag('g')) {
            // Clear the flag only if neither [value] nor [-g group] was given
            existing.setFlag(foundFlag, null);

            // Also clear the associated group flag if one exists
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
            if (groupFlag != null) {
                existing.setFlag(groupFlag, null);
            }

            sender.sendMessage(lang.getText("region-flag-vflag",
                    "%FLAG%", foundFlag.getName())
                    + lang.getText("-removed-from-vid", "%ID%", existing.getId(), false)
                    + lang.getText("any-groups-were-also-removed", false));
        }

        // Now set the group
        if (groupValue != null) {
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();

            // If group set to the default, then clear the group flag
            if (groupValue == groupFlag.getDefault()) {
                existing.setFlag(groupFlag, null);
                sender.sendMessage(lang.getText("region-group-flag-vflag-reset",
                        "%FLAG%", foundFlag.getName()));
            } else {
                existing.setFlag(groupFlag, groupValue);
                sender.sendMessage(lang.getText("region-group-flag-vflag-set",
                        "%FLAG%", foundFlag.getName()));
            }
        }

        commitChanges(sender, regionManager, lang); // Save to disk

        // Print region information
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(existing, lang);
        printout.append(ChatColor.GRAY);
        printout.append("(" + lang.getVerbatimText("current-flags"));
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
            usage = "<id> <priority>",
            flags = "w:",
            desc = "Set the priority of a region",
            min = 2, max = 2)
    public void setPriority(CommandContext args, CommandSender sender)
            throws CommandException {
        World world = getWorld(args, sender, 'w', lang); // Get the world
        int priority = args.getInteger(1);

        // Lookup the existing region
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion existing = findExistingRegion(regionManager,
                args.getString(0), false, lang);

        // Check permissions
        if (!getPermissionModel(sender).maySetPriority(existing)) {
            throw new CommandPermissionsException();
        }

        existing.setPriority(priority);
        commitChanges(sender, regionManager, lang); // Save to disk

        sender.sendMessage(lang.getText("priority-of-vid",
                "%ID%", existing.getId())
                + lang.getText("-set-to-vpriority",
                        "%PRIORITY%", "" + priority, false));
    }

    /**
     * Set the parent of a region.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"setparent", "parent", "par"},
            usage = "<id> [parent-id]",
            flags = "w:",
            desc = "Set the parent of a region",
            min = 1, max = 2)
    public void setParent(CommandContext args, CommandSender sender) throws CommandException {
        World world = getWorld(args, sender, 'w', lang); // Get the world
        ProtectedRegion parent;
        ProtectedRegion child;

        // Lookup the existing region
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);

        // Get parent and child
        child = findExistingRegion(regionManager, args.getString(0), false, lang);
        if (args.argsLength() == 2) {
            parent = findExistingRegion(regionManager, args.getString(1), false, lang);
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
            RegionPrintoutBuilder printout = new RegionPrintoutBuilder(parent, lang);
            printout.append(ChatColor.RED);
            printout.append("Uh oh! Setting '" + parent.getId() + "' to be the parent "
                    + "of '" + child.getId() + "' would cause circular inheritance.\n");
            printout.append(ChatColor.GRAY);
            printout.append("(Current inheritance on '" + parent.getId() + "':\n");
            printout.appendParentTree(true);
            printout.append(ChatColor.GRAY);
            printout.append(")");
            printout.send(sender);
            return;
        }

        commitChanges(sender, regionManager, lang); // Save to disk

        // Tell the user the current inheritance
        RegionPrintoutBuilder printout = new RegionPrintoutBuilder(child, lang);
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
        return;
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
            flags = "w:",
            desc = "Remove a region",
            min = 1, max = 1)
    public void remove(CommandContext args, CommandSender sender) throws CommandException {
        World world = getWorld(args, sender, 'w', lang); // Get the world

        // Lookup the existing region
        RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion existing = findExistingRegion(regionManager,
                args.getString(0), true, lang);

        // Check permissions
        if (!getPermissionModel(sender).mayDelete(existing)) {
            throw new CommandPermissionsException();
        }

        regionManager.removeRegion(existing.getId());
        commitChanges(sender, regionManager, lang); // Save to disk

        sender.sendMessage(lang.getText("region-vid-removed",
                "%ID%", existing.getId()));
    }

    /**
     * Reload the region database.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"load", "reload"},
            usage = "[world]",
            desc = "Reload regions from file",
            flags = "w:")
    public void load(CommandContext args, CommandSender sender) throws CommandException {
        World world = null;
        try {
            world = getWorld(args, sender, 'w', lang); // Get the world
        } catch (CommandException e) {
            // assume the user wants to reload all worlds
        }

        // Check permissions
        if (!getPermissionModel(sender).mayForceLoadRegions()) {
            throw new CommandPermissionsException();
        }

        if (world != null) {
            RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
            if (regionManager == null) {
                throw new CommandException(lang.getText("no-region-manager-for-vworld",
                        "%WORLD%", world.getName()));
            }
            reloadChanges(sender, regionManager, lang);
        } else {
            sender.sendMessage(lang.getText("loading-region-databases"));
            for (World w : plugin.getServer().getWorlds()) {
                RegionManager regionManager = plugin.getGlobalRegionManager().get(w);
                if (regionManager == null) {
                    continue;
                }
                reloadChanges(sender, regionManager, true, lang);
            }
        }

        sender.sendMessage(lang.getText("region-databases-loaded"));
    }

    /**
     * Re-save the region database.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"save", "write"},
            usage = "[world]",
            desc = "Re-save regions to file",
            flags = "w:")
    public void save(CommandContext args, CommandSender sender) throws CommandException {
        World world = null;
        try {
            world = getWorld(args, sender, 'w', lang); // Get the world
        } catch (CommandException e) {
            // assume user wants to save all worlds
        }

        // Check permissions
        if (!getPermissionModel(sender).mayForceSaveRegions()) {
            throw new CommandPermissionsException();
        }

        if (world != null) {
            RegionManager regionManager = plugin.getGlobalRegionManager().get(world);
            if (regionManager == null) {
                throw new CommandException(
                        lang.getText("no-region-manager-for-vworld",
                                "%WORLD%", world.getName()));
            }
            commitChanges(sender, regionManager, lang);
        } else {
            sender.sendMessage(lang.getText("saving-region-databases"));
            for (World w : plugin.getServer().getWorlds()) {
                RegionManager regionManager = plugin.getGlobalRegionManager().get(w);
                if (regionManager == null) {
                    continue;
                }
                commitChanges(sender, regionManager, true, lang);
            }
        }
        sender.sendMessage(lang.getText("region-databases-saved"));
    }

    /**
     * Migrate the region database.
     *
     * @param args the arguments
     * @param sender the sender
     * @throws CommandException any error
     */
    @Command(aliases = {"migratedb"}, usage = "<from> <to>",
            desc = "Migrate from one Protection Database to another.", min = 2, max = 2)
    public void migrateDB(CommandContext args, CommandSender sender) throws CommandException {
        // Check permissions
        if (!getPermissionModel(sender).mayMigrateRegionStore()) {
            throw new CommandPermissionsException();
        }

        String from = args.getString(0).toLowerCase().trim();
        String to = args.getString(1).toLowerCase().trim();

        if (from.equals(to)) {
            throw new CommandException(lang.getText("will-not-migrate"));
        }

        Map<MigratorKey, Class<? extends AbstractDatabaseMigrator>> migrators
                = AbstractDatabaseMigrator.getMigrators();
        MigratorKey key = new MigratorKey(from, to);

        if (!migrators.containsKey(key)) {
            throw new CommandException(lang.getText("no-migrator-found-for-comb-dir"));
        }

        long lastRequest = 10000000;
        if (this.migrateDBRequestDate != null) {
            lastRequest = new Date().getTime() - this.migrateDBRequestDate.getTime();
        }
        if (this.migrateDBRequest == null || lastRequest > 60000) {
            this.migrateDBRequest = key;
            this.migrateDBRequestDate = new Date();

            throw new CommandException(lang.getText("command-is-potentially-dangerous"));
        }

        Class<? extends AbstractDatabaseMigrator> cls = migrators.get(key);

        try {
            AbstractDatabaseMigrator migrator = cls.getConstructor(WorldGuardPlugin.class).newInstance(plugin);

            migrator.migrate();
        } catch (IllegalArgumentException ignore) {
        } catch (SecurityException ignore) {
        } catch (InstantiationException ignore) {
        } catch (IllegalAccessException ignore) {
        } catch (InvocationTargetException ignore) {
        } catch (NoSuchMethodException ignore) {
        } catch (MigrationException e) {
            throw new CommandException(lang.getText("error-migrating-db-verror"));
        }

        sender.sendMessage(lang.getText("regions-successfully-migrated"));
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
            desc = "Teleports you to the location associated with the region.",
            min = 1, max = 1)
    public void teleport(CommandContext args, CommandSender sender) throws CommandException {
        Player player = plugin.checkPlayer(sender);
        Location teleportLocation;

        // Lookup the existing region
        RegionManager regionManager = plugin.getGlobalRegionManager().get(player.getWorld());
        ProtectedRegion existing = findExistingRegion(regionManager,
                args.getString(0), false, lang);

        // Check permissions
        if (!getPermissionModel(sender).mayTeleportTo(existing)) {
            throw new CommandPermissionsException();
        }

        // -s for spawn location
        if (args.hasFlag('s')) {
            teleportLocation = existing.getFlag(DefaultFlag.SPAWN_LOC);

            if (teleportLocation == null) {
                throw new CommandException(lang.getText("region-has-no-spawn-point"));
            }
        } else {
            teleportLocation = existing.getFlag(DefaultFlag.TELE_LOC);

            if (teleportLocation == null) {
                throw new CommandException(lang.getText("region-has-no-teleport-point"));
            }
        }

        player.teleport(BukkitUtil.toLocation(teleportLocation));
        sender.sendMessage(lang.getText("teleported-you-to-the-region",
                "%REGION%", existing.getId()));
    }
}
