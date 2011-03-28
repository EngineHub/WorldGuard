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
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.*;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.*;
import com.sk89q.worldguard.util.RegionUtil;

public class RegionCommands {
    
    @Command(aliases = {"define", "def"},
            usage = "<id> [<owner1> [<owner2> [<owners...>]]]",
            desc = "Defines a region",
            flags = "", min = 1, max = -1)
    @CommandPermissions({"worldguard.region.define"})
    public static void define(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        WorldEditPlugin worldEdit = plugin.getWorldEdit();
        String id = args.getString(0);
        
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

        // Get the list of region owners
        if (args.argsLength() > 1) {
            region.setOwners(RegionUtil.parseDomainString(args.getSlice(2), 1));
        }
        
        RegionManager mgr = plugin.getGlobalRegionManager().get(
                sel.getWorld().getName());
        mgr.addRegion(region);
        
        try {
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"claim"},
            usage = "<id> [<owner1> [<owner2> [<owners...>]]]",
            desc = "Claim a region",
            flags = "", min = 1, max = -1)
    @CommandPermissions({"worldguard.region.claim"})
    public static void claim(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
        Player player = plugin.checkPlayer(sender);
        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        WorldEditPlugin worldEdit = plugin.getWorldEdit();
        String id = args.getString(0);
        
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

        // Get the list of region owners
        if (args.argsLength() > 1) {
            region.setOwners(RegionUtil.parseDomainString(args.getSlice(2), 1));
        }

        WorldConfiguration wcfg = plugin.getGlobalConfiguration()
                .forWorld(player.getWorld().getName());
        RegionManager mgr = plugin.getGlobalRegionManager().get(
                sel.getWorld().getName());
        
        // Check whether the player has created too many regions 
        if (wcfg.maxRegionCountPerPlayer >= 0
                && mgr.getRegionCountOfPlayer(localPlayer) >= wcfg.maxRegionCountPerPlayer) {
            throw new CommandException("You own too many regions, delete one first to claim a new one.");
        }

        ProtectedRegion existing = mgr.getRegion(id);

        // Check for an existing region
        if (existing != null) {
            if (!existing.getOwners().contains(localPlayer)) {
                throw new CommandException("This region already exists and you don't own it.");
            }
        }
/*
        ApplicableRegionSet regions = mgr.getApplicableRegions(region);
        
        // Check if this region overlaps any other region
        if (regions.isAnyRegionAffected()) {
            if (!regions.isOwner(localPlayer)) {
                throw new CommandException("This region overlaps with someone else's region.");
            }
            
            region.setPriority(regions.getAffectedRegionPriority() + 1);
        } else {
            if (wcfg.claimOnlyInsideExistingRegions) {
                throw new CommandException("You may only claim regions inside " +
                		"existing regions that you or your group own.");
            }
        }*/

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

        if (region.countBlocks() > wcfg.maxClaimVolume) {
            player.sendMessage(ChatColor.RED + "This region is to large to claim.");
            player.sendMessage(ChatColor.RED +
                    "Max. volume: " + wcfg.maxClaimVolume + ", your volume: " + region.countBlocks());
            return;
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
    
    @Command(aliases = {"info"},
            usage = "[world] <id>",
            desc = "Get information about a region",
            flags = "", min = 1, max = 2)
    public static void info(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {

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
        
        RegionManager mgr = plugin.getGlobalRegionManager().get(world.getName());
        
        if (!mgr.hasRegion(id)) {
            throw new CommandException("A region with ID '" + id + "' doesn't exist.");
        }

        ProtectedRegion region = mgr.getRegion(id);

        if (player != null) {
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "region.info.own");
            } else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "region.info.member");
            } else {
                plugin.checkPermission(sender, "region.info");
            }
        } else {
            plugin.checkPermission(sender, "region.info");
        }

        DefaultDomain owners = region.getOwners();
        DefaultDomain members = region.getMembers();

        sender.sendMessage(ChatColor.YELLOW + "Region: " + id
                + ChatColor.GRAY + " (type: " + region.getTypeName() + ")");
        sender.sendMessage(ChatColor.BLUE + "Priority: " + region.getPriority());

        StringBuilder s = new StringBuilder();

        for (Flag<?> flag : DefaultFlag.getFlags()) {
            if (s.length() > 0) {
                s.append(", ");
            }

            s.append(flag.getName() + ": " + String.valueOf(region.getFlag(flag)));
        }

        sender.sendMessage(ChatColor.BLUE + "Flags: " + s.toString());
        sender.sendMessage(ChatColor.BLUE + "Parent: "
                + (region.getParent() == null ? "(none)" : region.getParent().getId()));
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Owners: "
                + owners.toUserFriendlyString());
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Members: "
                + members.toUserFriendlyString());
    }
}
