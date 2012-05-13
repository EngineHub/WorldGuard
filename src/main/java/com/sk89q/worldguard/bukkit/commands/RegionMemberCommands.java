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

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;

public class RegionMemberCommands {
    private final WorldGuardPlugin plugin;

    public RegionMemberCommands(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Command(aliases = {"addmember", "addmember"}, usage = "<id> <members...>",
            desc = "Add a member to a region", min = 2)
    public void addMember(CommandContext args, CommandSender sender) throws CommandException {
        final RegionMemberCommandContext ctxt = new RegionMemberCommandContext(args, sender, "addmember");
        ctxt.checkPermission();

        RegionDBUtil.addToDomain(ctxt.region.getMembers(), args.getPaddedSlice(2, 0), 0);

        ctxt.saveAndNotify();
    }
    
    @Command(aliases = {"addowner", "addowner"}, usage = "<id> <owners...>",
            desc = "Add an owner to a region", min = 2)
    public void addOwner(CommandContext args, CommandSender sender) throws CommandException {
        RegionMemberCommandContext ctxt = new RegionMemberCommandContext(args, sender, "addowner");

        ProtectedRegion region = ctxt.region;

        Boolean flag = region.getFlag(DefaultFlag.BUYABLE);
        boolean buyable = flag != null && flag;
        DefaultDomain owners = region.getOwners();

        if (buyable && owners != null && owners.size() == 0) {
            if (!plugin.hasPermission(ctxt.player, "worldguard.region.unlimited")) {
                int maxRegionCount = plugin.getGlobalStateManager().get(ctxt.world).getMaxRegionCount(ctxt.player);
                if (maxRegionCount >= 0 && ctxt.mgr.getRegionCountOfPlayer(ctxt.localPlayer) >= maxRegionCount) {
                    throw new CommandException("You already own the maximum allowed amount of regions.");
                }
            }
            ctxt.checkPermission("worldguard.region.addowner.unclaimed");
        } else {
            ctxt.checkPermission();
        }

        RegionDBUtil.addToDomain(region.getOwners(), args.getPaddedSlice(2, 0), 0);

        ctxt.saveAndNotify();
    }
    
    @Command(aliases = {"removemember", "remmember", "removemem", "remmem"}, usage = "<id> <owners...>",
            desc = "Remove an owner to a region", min = 2)
    public void removeMember(CommandContext args, CommandSender sender) throws CommandException {
        RegionMemberCommandContext ctxt = new RegionMemberCommandContext(args, sender, "removemember");
        ctxt.checkPermission();

        RegionDBUtil.removeFromDomain(ctxt.region.getMembers(), args.getPaddedSlice(2, 0), 0);

        ctxt.saveAndNotify();
    }
    
    @Command(aliases = {"removeowner", "remowner"}, usage = "<id> <owners...>",
            desc = "Remove an owner to a region", min = 2)
    public void removeOwner(CommandContext args, CommandSender sender) throws CommandException {
        RegionMemberCommandContext ctxt = new RegionMemberCommandContext(args, sender, "removeowner");
        ctxt.checkPermission();

        RegionDBUtil.removeFromDomain(ctxt.region.getOwners(), args.getPaddedSlice(2, 0), 0);

        ctxt.saveAndNotify();
    }

    /**
     * Helper class to reduce boilerplate.
     */
    private class RegionMemberCommandContext {
        private final String commandName;
        private final CommandSender sender;
        public final Player player;
        public final World world;
        public final LocalPlayer localPlayer;
        public final String id;
        public final RegionManager mgr;
        public final ProtectedRegion region;

        public RegionMemberCommandContext(final CommandContext args, final CommandSender sender, final String commandName) throws CommandException {
            this.commandName = commandName;
            this.sender = sender;

            player = plugin.checkPlayer(sender);
            world = player.getWorld();
            localPlayer = plugin.wrapPlayer(player);

            final String givenId = args.getString(0);

            mgr = plugin.getGlobalRegionManager().get(world);
            region = mgr.getRegion(givenId);

            if (region == null) {
                throw new CommandException("Could not find a region by that ID.");
            }

            id = region.getId();
        }
        
        /**
         * Check if the player has permission to execute this command on the specified region.
         * @throws CommandPermissionsException if the required permission is not held
         */
        public void checkPermission() throws CommandPermissionsException {
            if (region.isOwner(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region." + commandName + ".own." + id.toLowerCase());
            } else if (region.isMember(localPlayer)) {
                plugin.checkPermission(sender, "worldguard.region." + commandName + ".member." + id.toLowerCase());
            } else {
                plugin.checkPermission(sender, "worldguard.region." + commandName + "." + id.toLowerCase());
            }
        }
        
        /**
         * Check if the player has permission to execute this command on the specified region.
         * @param perm the permission node to append the region ID to and check
         * @throws CommandPermissionsException if the required permission is not held
         */
        public void checkPermission(final String perm) throws CommandPermissionsException {
            plugin.checkPermission(sender, perm + "." + id.toLowerCase());
        }
        
        /**
         * Notify the command sender of the update and save the region manager's state.
         * @throws CommandException if saving failed
         */
        public void saveAndNotify() throws CommandException {
            sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");

            try {
                mgr.save();
            } catch (final ProtectionDatabaseException e) {
                throw new CommandException("Failed to write regions: " + e.getMessage());
            }
        }
    }
}
