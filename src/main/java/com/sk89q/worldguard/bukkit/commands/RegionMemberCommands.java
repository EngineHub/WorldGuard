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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.RegionUtil;

// @TODO: A lot of code duplication here! Need to fix.

public class RegionMemberCommands {
    
    @Command(aliases = {"addmember", "addmember"},
            usage = "<id> <members...>",
            desc = "Add a member to a region",
            flags = "", min = 2, max = -1)
    public static void addMember(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
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
            plugin.checkPermission(sender, "worldguard.region.addmember.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.addmember.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.addmember." + id.toLowerCase());
        }

        RegionUtil.addToDomain(region.getMembers(), args.getPaddedSlice(2, 0), 0);

        sender.sendMessage(ChatColor.YELLOW
                + "Region '" + id + "' updated.");
        
        try {
            mgr.save();
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"addowner", "addowner"},
            usage = "<id> <owners...>",
            desc = "Add an owner to a region",
            flags = "", min = 2, max = -1)
    public static void addOwner(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
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
            plugin.checkPermission(sender, "worldguard.region.addowner.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.addowner.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.addowner." + id.toLowerCase());
        }

        RegionUtil.addToDomain(region.getOwners(), args.getPaddedSlice(2, 0), 0);

        sender.sendMessage(ChatColor.YELLOW
                + "Region '" + id + "' updated.");
        
        try {
            mgr.save();
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"removemember", "remmember", "removemem", "remmem"},
            usage = "<id> <owners...>",
            desc = "Remove an owner to a region",
            flags = "", min = 2, max = -1)
    public static void removeMember(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
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
            plugin.checkPermission(sender, "worldguard.region.removemember.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.removemember.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.removemember." + id.toLowerCase());
        }

        RegionUtil.removeFromDomain(region.getMembers(), args.getPaddedSlice(2, 0), 0);

        sender.sendMessage(ChatColor.YELLOW
                + "Region '" + id + "' updated.");
        
        try {
            mgr.save();
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
                    + e.getMessage());
        }
    }
    
    @Command(aliases = {"removeowner", "remowner"},
            usage = "<id> <owners...>",
            desc = "Remove an owner to a region",
            flags = "", min = 2, max = -1)
    public static void removeOwner(CommandContext args, WorldGuardPlugin plugin,
            CommandSender sender) throws CommandException {
        
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
            plugin.checkPermission(sender, "worldguard.region.removeowner.own." + id.toLowerCase());
        } else if (region.isMember(localPlayer)) {
            plugin.checkPermission(sender, "worldguard.region.removeowner.member." + id.toLowerCase());
        } else {
            plugin.checkPermission(sender, "worldguard.region.removeowner." + id.toLowerCase());
        }

        RegionUtil.removeFromDomain(region.getOwners(), args.getPaddedSlice(2, 0), 0);

        sender.sendMessage(ChatColor.YELLOW
                + "Region '" + id + "' updated.");
        
        try {
            mgr.save();
        } catch (IOException e) {
            throw new CommandException("Failed to write regions file: "
                    + e.getMessage());
        }
    }
}
