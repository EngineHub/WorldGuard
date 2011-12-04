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

import java.io.IOException;
import java.util.Arrays;
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
import com.sk89q.worldedit.Vector;
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
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import com.sk89q.worldguard.util.RegionUtil;

public class RegionCommands {
    private final WorldGuardPlugin plugin;

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
            region.setOwners(RegionUtil.parseDomainString(args.getSlice(1), 1));
        }
        
        mgr.addRegion(region);
        
        try {
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
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
        ProtectedRegion existing = mgr.getRegion(id);

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
        } catch (CircularInheritanceException e) {
        }
        
        mgr.addRegion(region);
        
        sender.sendMessage(ChatColor.YELLOW + "Region updated with new area.");
        
        try {
            mgr.save();
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
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
            region.setOwners(RegionUtil.parseDomainString(args.getSlice(1), 1));
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

        ProtectedRegion existing = mgr.getRegion(id);

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
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"select", "sel", "s"}, usage = "<id>",
            desc = "Load a region as a WorldEdit selection", min = 1, max = 1)
    public void select(CommandContext args, CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        WorldEditPlugin worldEdit = plugin.getWorldEdit();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        
        String id = args.getString(0);

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            throw new CommandException("Could not find a region by that ID.");
        }
        
        if (region.isOwner(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.select.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.select.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.select." + id.toLowerCase());
        }

        if (region instanceof ProtectedCuboidRegion) {
            ProtectedCuboidRegion cuboid = (ProtectedCuboidRegion) region;
            Vector pt1 = cuboid.getMinimumPoint();
            Vector pt2 = cuboid.getMaximumPoint();
            CuboidSelection selection = new CuboidSelection(world, pt1, pt2);
            worldEdit.setSelection(player, selection);
            sender.sendMessage(ChatColor.YELLOW + "Region selected as a cuboid.");
        } else if (region instanceof ProtectedPolygonalRegion) {
            ProtectedPolygonalRegion poly2d = (ProtectedPolygonalRegion) region;
            Polygonal2DSelection selection = new Polygonal2DSelection(world, poly2d.getPoints(),
                    poly2d.getMinimumPoint().getBlockY(), poly2d.getMaximumPoint().getBlockY());
            worldEdit.setSelection(player, selection);
            sender.sendMessage(ChatColor.YELLOW + "Region selected as a polygon.");
        } else if (region instanceof GlobalProtectedRegion) {
            throw new CommandException("Can't select global regions.");
        } else {
            throw new CommandException("Unknown region type: " + region.getClass().getCanonicalName());
        }
    }
    
    @Command(aliases = {"info", "i"}, usage = "[world] <id>",
            desc = "Get information about a region", min = 1, max = 2)
    public void info(CommandContext args, CommandSender sender) throws CommandException {

        Player player = null;
        LocalPlayer localPlayer = null;
        World world;
        String id;
        
        // Get different values based on provided arguments
        if (args.argsLength() == 1) {
            player = plugin.checkPlayer(sender);
            localPlayer = plugin.wrapPlayer(player);
            world = player.getWorld();
            id = args.getString(0).toLowerCase();
        } else {
            world = plugin.matchWorld(sender, args.getString(0));
            id = args.getString(1).toLowerCase();
        }
        
        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        
        if (!mgr.hasRegion(id)) {
            if (!ProtectedRegion.isValidId(id)) {
                throw new CommandException("Invalid region ID specified!");
            }
            throw new CommandException("A region with ID '" + id + "' doesn't exist.");
        }

        ProtectedRegion region = mgr.getRegion(id);

        if (player != null) {
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.info.own");
            } else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.info.member");
            } else {
                plugin.checkPermission(sender, "worldguard.region.info");
            }
        } else {
            plugin.checkPermission(sender, "worldguard.region.info");
        }

        DefaultDomain owners = region.getOwners();
        DefaultDomain members = region.getMembers();

        sender.sendMessage(ChatColor.YELLOW + "Region: " + id
                + ChatColor.GRAY + " (type: " + region.getTypeName() + ")");
        if (!ProtectedRegion.isValidId(id)) {
            sender.sendMessage(ChatColor.RED + "This region has an invalid ID. "
                    + "Please ask the owner to delete it and recreate it.");
        }
        sender.sendMessage(ChatColor.BLUE + "Priority: " + region.getPriority());

        StringBuilder s = new StringBuilder();

        for (Flag<?> flag : DefaultFlag.getFlags()) {
            Object val = region.getFlag(flag);
            
            if (val == null) {
                continue;
            }
            
            if (s.length() > 0) {
                s.append(", ");
            }

            s.append(flag.getName() + ": " + String.valueOf(val));
        }

        sender.sendMessage(ChatColor.BLUE + "Flags: " + s.toString());
        sender.sendMessage(ChatColor.BLUE + "Parent: "
                + (region.getParent() == null ? "(none)" : region.getParent().getId()));
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Owners: "
                + owners.toUserFriendlyString());
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Members: "
                + members.toUserFriendlyString());
        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();
        String c = "(" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ")";
        c += " (" +max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")";
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Bounds: " + c);
    }
    
    @Command(aliases = {"list"}, usage = "[.player] [page] [world]",
            desc = "Get a list of regions", max = 3)
//    @CommandPermissions({"worldguard.region.list"})
    public void list(CommandContext args, CommandSender sender) throws CommandException {

        World world;
        int page = 0;
        int argl = 0;
        String name = "";
        boolean own = false;
        LocalPlayer localPlayer = null;
        
        if (args.argsLength() > 0 && args.getString(0).startsWith(".")) {
            name = args.getString(0).substring(1).toLowerCase();
            argl = 1;
            
            if (name.equals("me") || name.isEmpty() ||
                    name.equals(plugin.checkPlayer(sender).getDisplayName().toLowerCase())) {
                plugin.checkPermission(sender, "worldguard.region.list.own");
                name = plugin.checkPlayer(sender).getDisplayName().toLowerCase();
                localPlayer = plugin.wrapPlayer(plugin.checkPlayer(sender));
                own = true;
            }
        }
        if (!own)
            plugin.checkPermission(sender, "worldguard.region.list");
        
        if (args.argsLength() > 0 + argl) {
            page = Math.max(0, args.getInteger(0 + argl) - 1);
        }
        
        if (args.argsLength() > 1 + argl) {
            world = plugin.matchWorld(sender, args.getString(1 + argl));
        } else {
            world = plugin.checkPlayer(sender).getWorld();
        }
        
        int listSize = 10;

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        Map<String, ProtectedRegion> regions = mgr.getRegions();

        int size = regions.size();

        String[] regionIDList = new String[size];
        int index = 0;
        boolean show = false;
        String prefix = "";
        for (String id : regions.keySet()) {
            show = false;
            prefix = "";
            if (name.isEmpty()) {
                show = true;
            }
            else {
                if (own) {
                    if (regions.get(id).isOwner(localPlayer)) {
                        show = true;
                        prefix += "+";
                    }
                    else if (regions.get(id).isMember(localPlayer)) {
                        show = true;
                        prefix += "-";
                    }
                }
                else {
                    if (regions.get(id).getOwners().getPlayers().contains(name)) {
                        show = true;
                        prefix += "+";
                    }
                    if (regions.get(id).getMembers().getPlayers().contains(name)) {
                        show = true;
                        prefix += "-";
                    }
                }
            }
            if (show) {
                regionIDList[index] = prefix + " " + id;
                index++;
            }
        }
        if (!name.isEmpty())
            regionIDList = Arrays.copyOf(regionIDList, index);
        Arrays.sort(regionIDList);
        size = index;
        int pages = (int) Math.ceil(size / (float) listSize);

        sender.sendMessage(ChatColor.RED
                + (name.equals("") ? "Regions (page " : "Regions for " + name + " (page ")
                + (page + 1) + " of " + pages + "):");

        if (page < pages) {
            for (int i = page * listSize; i < page * listSize + listSize; i++) {
                if (i >= size) {
                    break;
                }
                sender.sendMessage(ChatColor.YELLOW.toString() + (i + 1) +
                        "." + regionIDList[i]);
            }
        }
    }
    
    @Command(aliases = {"flag", "f"}, usage = "<id> <flag> [value]",
            desc = "Set flags", min = 2)
    public void flag(CommandContext args, CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        World world = player.getWorld();
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        
        String id = args.getString(0);
        String flagName = args.getString(1);
        String value = null;

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
        
        if (value != null) {
            try {
                setFlag(region, foundFlag, sender, value);
            } catch (InvalidFlagFormat e) {
                throw new CommandException(e.getMessage());
            }

            sender.sendMessage(ChatColor.YELLOW
                    + "Region flag '" + foundFlag.getName() + "' set.");
        } else {
            // Clear the flag
            region.setFlag(foundFlag, null);

            sender.sendMessage(ChatColor.YELLOW
                    + "Region flag '" + foundFlag.getName() + "' cleared.");
        }
        
        try {
            mgr.save();
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
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
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
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
        
        if (args.argsLength() == 1) {
            try {
                region.setParent(null);
            } catch (CircularInheritanceException e) {
            }
    
            sender.sendMessage(ChatColor.YELLOW
                    + "Parent of '" + region.getId() + "' cleared.");
        } else {
            String parentId = args.getString(1);
            ProtectedRegion parent = mgr.getRegion(parentId);
    
            if (region == null) {
                throw new CommandException("Could not find a target region by that ID.");
            }
    
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
                plugin.checkPermission(sender, "worldguard.region.setparent.own." + id.toLowerCase());
            } else if (parent.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region.setparent.member." + id.toLowerCase());
            } else {
                plugin.checkPermission(sender, "worldguard.region.setparent." + id.toLowerCase());
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
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
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
        ProtectedRegion region = mgr.getRegion(id);

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
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"load", "reload"}, usage = "[world]",
            desc = "Reload regions from file", max = 1)
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
            } catch (IOException e) {
                throw new CommandException("Failed to read regions file: "
                        + e.getMessage());
            }
        } else {
            for (World w : plugin.getServer().getWorlds()) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(w);
                
                try {
                    mgr.load();
                } catch (IOException e) {
                    throw new CommandException("Failed to read regions file: "
                            + e.getMessage());
                }
            }
            
            sender.sendMessage(ChatColor.YELLOW
                    + "Region databases loaded.");
        }
    }
    
    @Command(aliases = {"save", "write"}, usage = "[world]",
            desc = "Re-save regions to file", max = 1)
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
            } catch (IOException e) {
                throw new CommandException("Failed to write regions file: "
                        + e.getMessage());
            }
        } else {
            for (World w : plugin.getServer().getWorlds()) {
                RegionManager mgr = plugin.getGlobalRegionManager().get(w);
                
                try {
                    mgr.save();
                } catch (IOException e) {
                    throw new CommandException("Failed to write regions file: "
                            + e.getMessage());
                }
            }
            
            sender.sendMessage(ChatColor.YELLOW
                    + "Region databases saved.");
        }
    }
}
