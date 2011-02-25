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

import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardWorldConfiguration;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.AreaFlags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandRegionInfo extends WgRegionCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg, WorldGuardWorldConfiguration wcfg) throws CommandHandlingException {

        CommandHandler.checkArgs(args, 1, 1, "/region info <id>");

        RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(wcfg.getWorldName());
        String id = args[0].toLowerCase();
        if (!mgr.hasRegion(id)) {
            sender.sendMessage(ChatColor.RED + "A region with ID '"
                    + id + "' doesn't exist.");
            return true;
        }

        ProtectedRegion region = mgr.getRegion(id);

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (region.isOwner(BukkitPlayer.wrapPlayer(cfg, player))) {
                cfg.checkRegionPermission(sender, "region.info.ownregions");
            }
            else if(region.isMember(BukkitPlayer.wrapPlayer(cfg, player))) {
                cfg.checkRegionPermission(sender, "region.info.memberregions");
            }
            else {
                cfg.checkRegionPermission(sender, "region.info.foreignregions");
            }
        }
        else
        {
           cfg.checkRegionPermission(sender, "region.info.foreignregions");
        }

        AreaFlags flags = region.getFlags();
        DefaultDomain owners = region.getOwners();
        DefaultDomain members = region.getMembers();

        sender.sendMessage(ChatColor.YELLOW + "Region: " + id
                + ChatColor.GRAY + " (type: " + region.getTypeName() + ")");
        sender.sendMessage(ChatColor.BLUE + "Priority: " + region.getPriority());

        StringBuilder s = new StringBuilder();
        for (FlagInfo nfo : FlagInfo.getFlagInfoList()) {
            String fullName = nfo.name;
            if (nfo.subName != null) {
                fullName += " " + nfo.subName;
            }

            String value = flags.getFlag(nfo.flagName, nfo.flagSubName);
            if (value != null) {
                s.append(fullName + ": " + value + ", ");
            }
        }

        String spawnTest = flags.getFlag("spawn", "x");
        if(spawnTest != null)
        {
            s.append("spawn: set");
        }
        else
        {
            s.append("spawn: not set");
        }

        sender.sendMessage(ChatColor.BLUE + "Flags: " + s.toString());
        sender.sendMessage(ChatColor.BLUE + "Parent: "
                + (region.getParent() == null ? "(none)" : region.getParent().getId()));
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Owners: "
                + owners.toUserFriendlyString());
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Members: "
                + members.toUserFriendlyString());
        return true;
    }
}
