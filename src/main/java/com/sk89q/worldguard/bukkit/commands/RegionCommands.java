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
import com.sk89q.minecraft.util.commands.CommandPermissions;
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
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
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
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.migrators.AbstractDatabaseMigrator;
import com.sk89q.worldguard.protection.databases.migrators.MigrationException;
import com.sk89q.worldguard.protection.databases.migrators.MigratorKey;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;

public class RegionCommands {
    private final WorldGuardPlugin plugin;

    private MigratorKey migrateDBRequest;
    private Date migrateDBRequestDate;
    
    public RegionCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Command(aliases = {"define", "def", "d"}, usage = "<id> [<owner1> [<owner2> [<owners...>]]]",
            desc = "Defines a region", min = 1)
    @CommandPermissions({"worldguard.region.define"})
    public void define(CommandContext args, CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        WorldEditPlugin worldEdit = plugin.getWorldEdit();
        String id = args.getString(0);
        
        if (!ProtectedRegion.isValidId(id)) {
            throw new CommandException("Invalid region ID specified!");
        }
        
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("A region cannot be named __global__");
        }
        
        // Attempt to get the player's selection from WorldEdit
        Selection sel = worldEdit.getSelection(player);
        
        if (sel == null) {
            throw new CommandException("Select a region with WorldEdit first.");
        }
        
        RegionManager mgr = plugin.getGlobalRegionManager().get(sel.getWorld());
        if (mgr.hasRegion(id)) {
            throw new CommandException("That region is already defined. Use redefine instead.");
        }
        
        ProtectedRegion region;

        // Detect the type of region from WorldEdit
        if (sel instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (sel instanceof CuboidSelection) {
            BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
            BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
            region = new ProtectedCuboidRegion(id, min, max);
        } else {
            throw new CommandException(
                    "The type of region selected in WorldEdit is unsupported in WorldGuard!");
        }

        // Get the list of region owners
        if (args.argsLength() > 1) {
            region.setOwners(RegionDBUtil.parseDomainString(args.getSlice(1), 1));
        }
        
        mgr.addRegion(region);
        
        try {
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (ProtectionDatabaseException e) {
            throw new CommandException("Failed to write regions: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"redefine", "update", "move"}, usage = "<id>",
            desc = "Re-defines the shape of a region", min = 1, max = 1)
    public void redefine(CommandContext args, CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        WorldEditPlugin worldEdit = plugin.getWorldEdit();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        String id = args.getString(0);
        
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("The region cannot be named __global__");
        }

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion existing = mgr.getRegionExact(id);

        if (existing == null) {
            throw new CommandException("Could not find a region by that ID.");
        }

        if (existing.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.redefine.own");
        } else if (existing.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.redefine.member");
        } else {
            plugin.checkPermission(sender, "worldguard.region.redefine");
        } 
        
        // Attempt to get the player's selection from WorldEdit
        Selection sel = worldEdit.getSelection(player);
        
        if (sel == null) {
            throw new CommandException("Select a region with WorldEdit first.");
        }
        
        ProtectedRegion region;
        
        // Detect the type of region from WorldEdit
        if (sel instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (sel instanceof CuboidSelection) {
            BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
            BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
            region = new ProtectedCuboidRegion(id, min, max);
        } else {
            throw new CommandException(
                    "The type of region selected in WorldEdit is unsupported in WorldGuard!");
        }

        region.setMembers(existing.getMembers());
        region.setOwners(existing.getOwners());
        region.setFlags(existing.getFlags());
        region.setPriority(existing.getPriority());
        try {
            region.setParent(existing.getParent());
        } catch (CircularInheritanceException ignore) {
        }
        
        mgr.addRegion(region);
        
        sender.sendMessage(ChatColor.YELLOW + "Region updated with new area.");
        
        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException("Failed to write regions: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"claim"}, usage = "<id> [<owner1> [<owner2> [<owners...>]]]",
            desc = "Claim a region", min = 1)
    @CommandPermissions({"worldguard.region.claim"})
    public void claim(CommandContext args, CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        WorldEditPlugin worldEdit = plugin.getWorldEdit();
        String id = args.getString(0);
        
        if (!ProtectedRegion.isValidId(id)) {
            throw new CommandException("Invalid region ID specified!");
        }
        
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("A region cannot be named __global__");
        }
        
        // Attempt to get the player's selection from WorldEdit
        Selection sel = worldEdit.getSelection(player);
        
        if (sel == null) {
            throw new CommandException("Select a region with WorldEdit first.");
        }
        
        RegionManager mgr = plugin.getGlobalRegionManager().get(sel.getWorld());

        if (mgr.hasRegion(id)) {
            throw new CommandException("That region already exists. Please choose a different name.");
        }
        
        ProtectedRegion region;

        // Detect the type of region from WorldEdit
        if (sel instanceof Polygonal2DSelection) {
            Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
            int minY = polySel.getNativeMinimumPoint().getBlockY();
            int maxY = polySel.getNativeMaximumPoint().getBlockY();
            region = new ProtectedPolygonalRegion(id, polySel.getNativePoints(), minY, maxY);
        } else if (sel instanceof CuboidSelection) {
            BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
            BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
            region = new ProtectedCuboidRegion(id, min, max);
        } else {
            throw new CommandException(
                    "The type of region selected in WorldEdit is unsupported in WorldGuard!");
        }

        // Get the list of region owners
        if (args.argsLength() > 1) {
            region.setOwners(RegionDBUtil.parseDomainString(args.getSlice(1), 1));
        }

        WorldConfiguration wcfg = plugin.getGlobalStateManager().get(player.getWorld());
        
        if (!plugin.hasPermission(sender, "worldguard.region.unlimited")) {
            // Check whether the player has created too many regions 
            int maxRegionCount = wcfg.getMaxRegionCount(player);
            if (maxRegionCount >= 0
                    && mgr.getRegionCountOfPlayer(localPlayer) >= maxRegionCount) {
                throw new CommandException("You own too many regions, delete one first to claim a new one.");
            }
        }

        ProtectedRegion existing = mgr.getRegionExact(id);

        // Check for an existing region
        if (existing != null) {
            if (!existing.getOwners().contains(localPlayer)) {
                throw new CommandException("This region already exists and you don't own it.");
            }
        }
        
        ApplicableRegionSet regions = mgr.getApplicableRegions(region);
        
        // Check if this region overlaps any other region
        if (regions.size() > 0) {
            if (!regions.isOwnerOfAll(localPlayer)) {
                throw new CommandException("This region overlaps with someone else's region.");
            }
        } else {
            if (wcfg.claimOnlyInsideExistingRegions) {
                throw new CommandException("You may only claim regions inside " +
                        "existing regions that you or your group own.");
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

        if (!plugin.hasPermission(sender, "worldguard.region.unlimited")) {
            if (region.volume() > wcfg.maxClaimVolume) {
                player.sendMessage(ChatColor.RED + "This region is too large to claim.");
                player.sendMessage(ChatColor.RED +
                        "Max. volume: " + wcfg.maxClaimVolume + ", your volume: " + region.volume());
                return;
            }
        }

        region.getOwners().addPlayer(player.getName());
        mgr.addRegion(region);
        
        try {
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (ProtectionDatabaseException e) {
            throw new CommandException("Failed to write regions: "
                    + e.getMessage());
        }
    }

    @Command(aliases = {"select", "sel", "s"}, usage = "[id]",
            desc = "Load a region as a WorldEdit selection", min = 0, max = 1)
    public void select(CommandContext args, CommandSender sender) throws CommandException {

        final Player player = plugin.checkPlayer(sender);
        final World world = player.getWorld();
        final LocalPlayer localPlayer = plugin.wrapPlayer(player);

        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);

        final String id;
        if (args.argsLength() == 0) {
            final Vector pt = localPlayer.getPosition();
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            if (set.size() == 0) {
                throw new CommandException("No region ID specified and no region found at current location!");
            }

            id = set.iterator().next().getId();
        }
        else {
            id = args.getString(0);
        }

        final ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }

        selectRegion(player, localPlayer, region);
    }

    public void selectRegion(Player player, LocalPlayer localPlayer, ProtectedRegion region) throws CommandException, CommandPermissionsException {
        final WorldEditPlugin worldEdit = plugin.getWorldEdit();
        final String id = region.getId();

        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(player, "worldguard.region.select.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(player, "worldguard.region.select.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(player, "worldguard.region.select." + id.toLowerCase());
        }

        final World world = player.getWorld();
        if (region instanceof ProtectedCuboidRegion) {
            final ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
            final Vector pt1 = cuboid.getMinimumPoint();
            final Vector pt2 = cuboid.getMaximumPoint();
            final CuboidSelection selection = new CuboidSelection(world, pt1, pt2);
            worldEdit.setSelection(player, selection);
            player.sendMessage(ChatColor.YELLOW + "Region selected as a cuboid.");
        } else if (region instanceof ProtectedPolygonalRegion) {
            final ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion) region;
            final Polygonal2DSelection selection = new Polygonal2DSelection(
                    world, poly2d.getPoints(),
                    poly2d.getMinimumPoint().getBlockY(),
                    poly2d.getMaximumPoint().getBlockY()
            );
            worldEdit.setSelection(player, selection);
            player.sendMessage(ChatColor.YELLOW + "Region selected as a polygon.");
        } else if (region instanceof GlobalProtectedRegion) {
            throw new CommandException("Can't select global regions.");
        } else {
            throw new CommandException("Unknown region type: " + region.getClass().getCanonicalName());
        }
    }

    @Command(aliases = {"info", "i"}, usage = "[world] [id]", flags = "s",
            desc = "Get information about a region", min = 0, max = 2)
    public void info(CommandContext args, CommandSender sender) throws CommandException {

        final LocalPlayer localPlayer;
        final World world;
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            localPlayer = plugin.wrapPlayer(player);
            world = player.getWorld();
        } else if (args.argsLength() < 2) {
            throw new CommandException("A player is expected.");
        } else {
            localPlayer = null;
            world = plugin.matchWorld(sender, args.getString(0));
        }

        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);

        final String id;

        // Get different values based on provided arguments
        switch (args.argsLength()) {
        case 0:
            if (localPlayer == null) {
                throw new CommandException("A player is expected.");
            }

            final Vector pt = localPlayer.getPosition();
            final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            if (set.size() == 0) {
                throw new CommandException("No region ID specified and no region found at current location!");
            }

            id = set.iterator().next().getId();
            break;

        case 1:
            id = args.getString(0).toLowerCase();
            break;

        default:
            id = args.getString(1).toLowerCase();
        }

        final ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            if (!ProtectedRegion.isValidId(id)) {
                throw new CommandException("Invalid region ID specified!");
            }
            throw new CommandException("A region with ID '" + id + "' doesn't exist.");
        }

        displayRegionInfo(sender, localPlayer, region);

        if (args.hasFlag('s')) {
            selectRegion(plugin.checkPlayer(sender), localPlayer, region);
        }
    }

    public void displayRegionInfo(CommandSender sender, final LocalPlayer localPlayer, ProtectedRegion region) throws CommandPermissionsException {
        if (localPlayer == null) {
            plugin.checkPermission(sender, "worldguard.region.info");
        } else if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.info.own");
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.info.member");
        } else {
            plugin.checkPermission(sender, "worldguard.region.info");
        }

        final String id = region.getId();

        sender.sendMessage(ChatColor.YELLOW + "Region: " + id + ChatColor.GRAY + ", type: " + region.getTypeName() + ", " + ChatColor.BLUE + "Priority: " + region.getPriority());

        boolean hasFlags = false;
        final StringBuilder s = new StringBuilder(ChatColor.BLUE + "Flags: ");
        for (Flag<?> flag : DefaultFlag.getFlags()) {
            Object val = region.getFlag(flag), group = null;

            if (val == null) {
                continue;
            }

            if (hasFlags) {
                s.append(", ");
            }

            RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
            if (groupFlag != null) {
                group = region.getFlag(groupFlag);
            }

            if(group == null) {
                s.append(flag.getName() + ": " + String.valueOf(val));
            } else {
                s.append(flag.getName() + " -g " + String.valueOf(group) + ": " + String.valueOf(val));
            }

            hasFlags = true;
        }
        if (hasFlags) {
            sender.sendMessage(s.toString());
        }

        if (region.getParent() != null) {
            sender.sendMessage(ChatColor.BLUE + "Parent: " + region.getParent().getId());
        }

        final DefaultDomain owners = region.getOwners();
        if (owners.size() != 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Owners: " + owners.toUserFriendlyString());
        }

        final DefaultDomain members = region.getMembers();
        if (members.size() != 0) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Members: " + members.toUserFriendlyString());
        }

        final BlockVector min = region.getMinimumPoint();
        final BlockVector max = region.getMaximumPoint();
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Bounds:"
                + " (" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ")"
                + " (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")"
        );
    }

    public class RegionEntry implements Comparable<RegionEntry>{
        private final String id;
        private final int index;
        private boolean isOwner;
        private boolean isMember;

        public RegionEntry(String id, int index) {
            this.id = id;
            this.index = index;
        }

        @Override
        public int compareTo(RegionEntry o) {
            if (isOwner != o.isOwner) {
                return isOwner ? 1 : -1;
            }
            if (isMember != o.isMember) {
                return isMember ? 1 : -1;
            }
            return id.compareTo(o.id);
        }

        @Override
        public String toString() {
            if (isOwner) {
                return (index + 1) + ". +" + id;
            } else if (isMember) {
                return (index + 1) + ". -" + id;
            } else {
                return (index + 1) + ". " + id;
            }
        }
    }

    @Command(aliases = {"list"}, usage = "[.player] [page] [world]",
            desc = "Get a list of regions", max = 3)
    //@CommandPermissions({"worldguard.region.list"})
    public void list(CommandContext args, CommandSender sender) throws CommandException {

        World world;
        int page = 0;
        int argOffset = 0;
        String name = "";
        boolean own = false;
        LocalPlayer localPlayer = null;

        final String senderName = sender.getName().toLowerCase();
        if (args.argsLength() > 0 && args.getString(0).startsWith(".")) {
            name = args.getString(0).substring(1).toLowerCase();
            argOffset = 1;

            if (name.equals("me") || name.isEmpty() || name.equals(senderName)) {
                own = true;
            }
        }

        // Make /rg list default to "own" mode if the "worldguard.region.list" permission is not given
        if (!own && !plugin.hasPermission(sender, "worldguard.region.list")) {
            own = true;
        }

        if (own) {
            plugin.checkPermission(sender, "worldguard.region.list.own");
            name = senderName;
            localPlayer = plugin.wrapPlayer(plugin.checkPlayer(sender));
        }

        if (args.argsLength() > argOffset) {
            page = Math.max(0, args.getInteger(argOffset) - 1);
        }

        if (args.argsLength() > 1 + argOffset) {
            world = plugin.matchWorld(sender, args.getString(1 + argOffset));
        } else {
            world = plugin.checkPlayer(sender).getWorld();
        }

        final RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        final Map<String, ProtectedRegion> regions = mgr.getRegions();

        List<RegionEntry> regionEntries = new ArrayList<RegionEntry>();
        int index = 0;
        for (String id : regions.keySet()) {
            RegionEntry entry = new RegionEntry(id, index++);
            if (!name.isEmpty()) {
                if (own) {
                    entry.isOwner = regions.get(id).isOwner(localPlayer);
                    entry.isMember = regions.get(id).isMember(localPlayer);
                }
                else {
                    entry.isOwner = regions.get(id).isOwner(name);
                    entry.isMember = regions.get(id).isMember(name);
                }

                if (!entry.isOwner && !entry.isMember) {
                    continue;
                }
            }

            regionEntries.add(entry);
        }

        Collections.sort(regionEntries);

        final int totalSize = regionEntries.size();
        final int pageSize = 10;
        final int pages = (int) Math.ceil(totalSize / (float) pageSize);

        sender.sendMessage(ChatColor.RED
                + (name.equals("") ? "Regions (page " : "Regions for " + name + " (page ")
                + (page + 1) + " of " + pages + "):");

        if (page < pages) {
            for (int i = page * pageSize; i < page * pageSize + pageSize; i++) {
                if (i >= totalSize) {
                    break;
                }
                sender.sendMessage(ChatColor.YELLOW.toString() + regionEntries.get(i));
            }
        }
    }

    @Command(aliases = {"flag", "f"}, usage = "<id> <flag> [-g group] [value]", flags = "g:",
            desc = "Set flags", min = 2)
    public void flag(CommandContext args, CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        
        String id = args.getString(0);
        String flagName = args.getString(1);
        String value = null;
        RegionGroup groupValue = null;

        if (args.argsLength() >= 3) {
            value = args.getJoinedStrings(2);
        }

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            if (id.equalsIgnoreCase("__global__")) {
                region = new GlobalProtectedRegion(id);
                mgr.addRegion(region);
            } else {
                throw new CommandException("Could not find a region by that ID.");
            }
        }

        // @TODO deprecate "flag.[own./member./blank]"
        boolean hasPerm = false;
        if (region.isOwner(localPlayer)) {
            if (plugin.hasPermission(sender, "worldguard.region.flag.own." + id.toLowerCase())) hasPerm = true;
            else if (plugin.hasPermission(sender, "worldguard.region.flag.regions.own." + id.toLowerCase())) hasPerm = true;
        } else if (region.isMember(localPlayer)) {
            if (plugin.hasPermission(sender, "worldguard.region.flag.member." + id.toLowerCase())) hasPerm = true;
            else if (plugin.hasPermission(sender, "worldguard.region.flag.regions.member." + id.toLowerCase())) hasPerm = true;
        } else {
            if (plugin.hasPermission(sender, "worldguard.region.flag." + id.toLowerCase())) hasPerm = true;
            else if (plugin.hasPermission(sender, "worldguard.region.flag.regions." + id.toLowerCase())) hasPerm = true;
        }
        if (!hasPerm) throw new CommandPermissionsException();
        
        Flag<?> foundFlag = null;
        
        // Now time to find the flag!
        for (Flag<?> flag : DefaultFlag.getFlags()) {
            // Try to detect the flag
            if (flag.getName().replace("-", "").equalsIgnoreCase(flagName.replace("-", ""))) {
                foundFlag = flag;
                break;
            }
        }
        
        if (foundFlag == null) {
            StringBuilder list = new StringBuilder();
            
            // Need to build a list
            for (Flag<?> flag : DefaultFlag.getFlags()) {
                if (list.length() > 0) {
                    list.append(", ");
                }

                // @TODO deprecate inconsistant "owner" permission
                if (region.isOwner(localPlayer)) {
                    if (!plugin.hasPermission(sender, "worldguard.region.flag.flags."
                            + flag.getName() + ".owner." + id.toLowerCase())
                            && !plugin.hasPermission(sender, "worldguard.region.flag.flags."
                                    + flag.getName() + ".own." + id.toLowerCase())) {
                        continue;
                    }
                } else if (region.isMember(localPlayer)) {
                    if (!plugin.hasPermission(sender, "worldguard.region.flag.flags."
                            + flag.getName() + ".member." + id.toLowerCase())) {
                        continue;
                    }
                } else {
                    if (!plugin.hasPermission(sender, "worldguard.region.flag.flags."
                                + flag.getName() + "." + id.toLowerCase())) {
                        continue;
                    }
                } 
                
                list.append(flag.getName());
            }

            player.sendMessage(ChatColor.RED + "Unknown flag specified: " + flagName);
            player.sendMessage(ChatColor.RED + "Available flags: " + list);
            return;
        }

        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.flag.flags."
                    + foundFlag.getName() + ".owner." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.flag.flags."
                    + foundFlag.getName() + ".member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.flag.flags."
                    + foundFlag.getName() + "." + id.toLowerCase());
        }

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

        if (value != null) {
            // Set the flag if [value] was given even if [-g group] was given as well
            try {
                setFlag(region, foundFlag, sender, value);
            } catch (InvalidFlagFormat e) {
                throw new CommandException(e.getMessage());
            }

            sender.sendMessage(ChatColor.YELLOW
                    + "Region flag '" + foundFlag.getName() + "' set.");
        }

        if (value == null && !args.hasFlag('g')) {
            // Clear the flag only if neither [value] nor [-g group] was given
            region.setFlag(foundFlag, null);

            // Also clear the associated group flag if one exists
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
            if (groupFlag != null) {
                region.setFlag(groupFlag, null);
            }

            sender.sendMessage(ChatColor.YELLOW
                    + "Region flag '" + foundFlag.getName() + "' cleared.");
        }

        if (groupValue != null) {
            RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();

            // If group set to the default, then clear the group flag
            if (groupValue == groupFlag.getDefault()) {
                region.setFlag(groupFlag, null);
                sender.sendMessage(ChatColor.YELLOW
                        + "Region group flag for '" + foundFlag.getName() + "' reset to default.");
            } else {
                region.setFlag(groupFlag, groupValue);
                sender.sendMessage(ChatColor.YELLOW
                        + "Region group flag for '" + foundFlag.getName() + "' set.");
            }
        }

        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException("Failed to write regions: "
                    + e.getMessage());
        }
    }
    
    public <V> void setFlag(ProtectedRegion region,
            Flag<V> flag, CommandSender sender, String value)
                throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(plugin, sender, value));
    }
    
    @Command(aliases = {"setpriority", "priority", "pri"}, usage = "<id> <priority>",
            desc = "Set the priority of a region", min = 2, max = 2)
    public void setPriority(CommandContext args, CommandSender sender) throws CommandException {
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        
        String id = args.getString(0);
        int priority = args.getInteger(1);
        
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("The region cannot be named __global__");
        }
        
        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }

        id = region.getId();

        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.setpriority.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.setpriority.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.setpriority." + id.toLowerCase());
        } 

        region.setPriority(priority);

        sender.sendMessage(ChatColor.YELLOW
                + "Priority of '" + region.getId() + "' set to "
                + priority + ".");
        
        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException("Failed to write regions: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"setparent", "parent", "par"}, usage = "<id> [parent-id]",
            desc = "Set the parent of a region", min = 1, max = 2)
    public void setParent(CommandContext args, CommandSender sender) throws CommandException {
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        
        String id = args.getString(0);
        
        if (id.equalsIgnoreCase("__global__")) {
            throw new CommandException("The region cannot be named __global__");
        }
        
        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            throw new CommandException("Could not find a target region by that ID.");
        }

        id = region.getId();

        if (args.argsLength() == 1) {
            try {
                region.setParent(null);
            } catch (CircularInheritanceException ignore) {
            }
    
            sender.sendMessage(ChatColor.YELLOW
                    + "Parent of '" + region.getId() + "' cleared.");
        } else {
            String parentId = args.getString(1);
            ProtectedRegion parent = mgr.getRegion(parentId);
    
            if (parent == null) {
                throw new CommandException("Could not find the parent region by that ID.");
            }
            
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.own." + id.toLowerCase());
            } else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.member." + id.toLowerCase());
            } else {
                plugin.checkPermission(sender, "worldguard.region.setparent." + id.toLowerCase());
            } 
            
            if (parent.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.own." + parentId.toLowerCase());
            } else if (parent.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.member." + parentId.toLowerCase());
            } else {
                plugin.checkPermission(sender, "worldguard.region.setparent." + parentId.toLowerCase());
            }
            
            try {
                region.setParent(parent);
            } catch (CircularInheritanceException e) {
                throw new CommandException("Circular inheritance detected!");
            }
    
            sender.sendMessage(ChatColor.YELLOW
                    + "Parent of '" + region.getId() + "' set to '"
                    + parent.getId() + "'.");
        }
        
        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException("Failed to write regions: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"remove", "delete", "del", "rem"}, usage = "<id>",
            desc = "Remove a region", min = 1, max = 1)
    public void remove(CommandContext args, CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        
        String id = args.getString(0);

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegionExact(id);

        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.remove.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.remove.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.remove." + id.toLowerCase());
        }
        
        mgr.removeRegion(id);
        
        sender.sendMessage(ChatColor.YELLOW
                + "Region '" + id + "' removed.");
        
        try {
            mgr.save();
        } catch (ProtectionDatabaseException e) {
            throw new CommandException("Failed to write regions: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"load", "reload"}, usage = "[world]",
            desc = "Reload regions from file", max = 1)
    @CommandPermissions({"worldguard.region.load"})
    public void load(CommandContext args, CommandSender sender) throws CommandException {
        
        World world = null;
        
        if (args.argsLength() > 0) {
            world = plugin.matchWorld(sender, args.getString(0));
        }

        if (world != null) {
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            
            try {
                mgr.load();
                sender.sendMessage(ChatColor.YELLOW
                        + "Regions for '" + world.getName() + "' load.");
            } catch (ProtectionDatabaseException e) {
                throw new CommandException("Failed to read regions: "
                        + e.getMessage());
            }
        } else {
            for (World w : plugin.getServer().getWorlds()) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(w);
                
                try {
                    mgr.load();
                } catch (ProtectionDatabaseException e) {
                    throw new CommandException("Failed to read regions: "
                            + e.getMessage());
                }
            }
            
            sender.sendMessage(ChatColor.YELLOW
                    + "Region databases loaded.");
        }
    }
    
    @Command(aliases = {"save", "write"}, usage = "[world]",
            desc = "Re-save regions to file", max = 1)
    @CommandPermissions({"worldguard.region.save"})
    public void save(CommandContext args, CommandSender sender) throws CommandException {
        
        World world = null;
        
        if (args.argsLength() > 0) {
            world = plugin.matchWorld(sender, args.getString(0));
        }

        if (world != null) {
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            
            try {
                mgr.save();
                sender.sendMessage(ChatColor.YELLOW
                        + "Regions for '" + world.getName() + "' saved.");
            } catch (ProtectionDatabaseException e) {
                throw new CommandException("Failed to write regions: "
                        + e.getMessage());
            }
        } else {
            for (World w : plugin.getServer().getWorlds()) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(w);
                
                try {
                    mgr.save();
                } catch (ProtectionDatabaseException e) {
                    throw new CommandException("Failed to write regions: "
                            + e.getMessage());
                }
            }
            
            sender.sendMessage(ChatColor.YELLOW
                    + "Region databases saved.");
        }
    }

    @Command(aliases = {"migratedb"}, usage = "<from> <to>",
            desc = "Migrate from one Protection Database to another.", min = 1)
    @CommandPermissions({"worldguard.region.migratedb"})
    public void migratedb(CommandContext args, CommandSender sender) throws CommandException {
        String from = args.getString(0).toLowerCase().trim();
        String to = args.getString(1).toLowerCase().trim();

        if (from.equals(to)) {
            throw new CommandException("Will not migrate with common source and target.");
        }

        Map<MigratorKey, Class<? extends AbstractDatabaseMigrator>> migrators = AbstractDatabaseMigrator.getMigrators();
        MigratorKey key = new MigratorKey(from,to);

        if (!migrators.containsKey(key)) {
            throw new CommandException("No migrator found for that combination and direction.");
        }

        long lastRequest = 10000000;
        if (this.migrateDBRequestDate != null) { 
            lastRequest = new Date().getTime() - this.migrateDBRequestDate.getTime();
        }
        if (this.migrateDBRequest == null || lastRequest > 60000) {
            this.migrateDBRequest = key;
            this.migrateDBRequestDate = new Date();

            throw new CommandException("This command is potentially dangerous.\n" + 
                    "Please ensure you have made a backup of your data, and then re-enter the command exactly to procede.");
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
            throw new CommandException("Error migrating database: " + e.getMessage());
        }

        sender.sendMessage(ChatColor.YELLOW + "Regions have been migrated successfully.\n" +
                "If you wish to use the destination format as your new backend, please update your config and reload WorldGuard.");
    }

    @Command(aliases = {"teleport", "tp"}, usage = "<id>", flags = "s",
            desc = "Teleports you to the location associated with the region.", min = 1, max = 1)
    @CommandPermissions({"worldguard.region.teleport"})
    public void teleport(CommandContext args, CommandSender sender) throws CommandException {
        final Player player = plugin.checkPlayer(sender);

        final RegionManager mgr = plugin.getGlobalRegionManager().get(player.getWorld());
        String id = args.getString(0);

        final ProtectedRegion region = mgr.getRegion(id);
        if (region == null) {
            if (!ProtectedRegion.isValidId(id)) {
                throw new CommandException("Invalid region ID specified!");
            }
            throw new CommandException("A region with ID '" + id + "' doesn't exist.");
        }

        id = region.getId();

        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.teleport.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.teleport.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.teleport." + id.toLowerCase());
        }

        final Location teleportLocation;
        if (args.hasFlag('s')) {
            teleportLocation = region.getFlag(DefaultFlag.SPAWN_LOC);
            if (teleportLocation == null) {
                throw new CommandException("The region has no spawn point associated.");
            }
        } else {
            teleportLocation = region.getFlag(DefaultFlag.TELE_LOC);
            if (teleportLocation == null) {
                throw new CommandException("The region has no teleport point associated.");
            }
        }

        player.teleport(BukkitUtil.toLocation(teleportLocation));

        sender.sendMessage("Teleported you to the region '" + id + "'.");
    }
}
