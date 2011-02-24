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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandRegionSetParent extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, CommandHandler ch, WorldGuardPlugin wg) throws CommandHandlingException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        Player player = (Player) sender;
        if (!wg.hasPermission(player, "/regionclaim")) {
            ch.checkRegionPermission(player, "/regiondefine");
        }
        ch.checkArgs(args, 1, 2, "/region setparent <id> <parent-id>");

        String id = args[0].toLowerCase();
        String parentId = args.length > 1 ? args[1].toLowerCase() : null;
        RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

        ProtectedRegion region = mgr.getRegion(id);

        if (region == null) {
            player.sendMessage(ChatColor.RED + "Could not find a region with ID: " + id);
            return true;
        }

        if (!ch.canUseRegionCommand(player, "/regiondefine")
                && !region.isOwner(wg.wrapPlayer(player))) {
            player.sendMessage(ChatColor.RED + "You need to own the target regions");
            return true;
        }

        ProtectedRegion parent = null;

        // Set a parent
        if (parentId != null) {
            parent = mgr.getRegion(parentId);

            if (parent == null) {
                player.sendMessage(ChatColor.RED + "Could not find a region with ID: " + parentId);
                return true;
            }

            if (!ch.canUseRegionCommand(player, "/regiondefine")
                    && !parent.isOwner(wg.wrapPlayer(player))) {
                player.sendMessage(ChatColor.RED + "You need to own the parent region.");
                return true;
            }
        }

        try {
            region.setParent(parent);

            mgr.save();
            player.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
        } catch (CircularInheritanceException e) {
            player.sendMessage(ChatColor.RED + "Circular inheritance detected. The operation failed.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
