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
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandRegionAddMember extends WgRegionCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg, WorldGuardWorldConfiguration wcfg) throws CommandHandlingException {


        boolean cmdIsOwner = command.equalsIgnoreCase("addowner");

        String permOwn;
        String permAll;

        if (cmdIsOwner) {
            CommandHandler.checkArgs(args, 2, -1, "/region addowner <id> [player1 [group1 [players/groups...]]]");
            permOwn = "region.addowner.own";
            permAll = "region.addowner.all";
        } else {
            CommandHandler.checkArgs(args, 2, -1, "/region addmember <id> [player1 [group1 [players/groups...]]]");
            permOwn = "region.addmember.own";
            permAll = "region.addmember.all";
        }

        RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(wcfg.getWorldName());

        String id = args[0].toLowerCase();
        if (!mgr.hasRegion(id)) {
            sender.sendMessage(ChatColor.RED + "A region with ID '"
                    + id + "' doesn't exist.");
            return true;
        }

        ProtectedRegion existing = mgr.getRegion(id);

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (existing.isOwner(BukkitPlayer.wrapPlayer(cfg, player))) {
                cfg.checkRegionPermission(sender, permOwn);
            }
            else {
                cfg.checkRegionPermission(sender, permAll);
            }
        }
        else
        {
           cfg.checkRegionPermission(sender, permAll);
        }

        if (cmdIsOwner) {
            WorldGuardConfiguration.addToDomain(existing.getOwners(), args, 1);
        } else {
            WorldGuardConfiguration.addToDomain(existing.getMembers(), args, 1);
        }

        try {
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region updated!");
            sender.sendMessage(ChatColor.GRAY + "Current owners: "
                    + existing.getOwners().toUserFriendlyString());
            sender.sendMessage(ChatColor.GRAY + "Current members: "
                    + existing.getMembers().toUserFriendlyString());
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
