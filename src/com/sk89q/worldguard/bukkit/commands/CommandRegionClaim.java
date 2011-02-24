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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Michael
 */
public class CommandRegionClaim extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, CommandHandler ch, WorldGuardPlugin wg) throws CommandHandlingException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        Player player = (Player) sender;

        Plugin wePlugin = wg.getServer().getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null) {
            player.sendMessage(ChatColor.RED + "WorldEdit must be installed and enabled!");
            return true;
        }

        ch.checkRegionPermission(player, "/regionclaim");
        ch.checkArgs(args, 1, 1, "/region claim <id>");

        try {
            String id = args[0].toLowerCase();
            RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());

            ProtectedRegion existing = mgr.getRegion(id);

            if (existing != null) {
                if (!existing.getOwners().contains(wg.wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "You don't own this region.");
                    return true;
                }
            }

            WorldEditPlugin worldEdit = (WorldEditPlugin) wePlugin;

            LocalSession session = worldEdit.getSession(player);
            Region weRegion;

            weRegion = session.getSelection(new BukkitWorld(player.getWorld()));


            ProtectedRegion region;

            if (weRegion instanceof Polygonal2DRegion) {
                Polygonal2DRegion pweRegion = (Polygonal2DRegion) weRegion;
                int minY = pweRegion.getMinimumPoint().getBlockY();
                int maxY = pweRegion.getMaximumPoint().getBlockY();
                region = new ProtectedPolygonalRegion(id, pweRegion.getPoints(), minY, maxY);
            } else {
                BlockVector min = weRegion.getMinimumPoint().toBlockVector();
                BlockVector max = weRegion.getMaximumPoint().toBlockVector();
                region = new ProtectedCuboidRegion(id, min, max);
            }

            if (mgr.overlapsUnownedRegion(region, wg.wrapPlayer(player))) {
                player.sendMessage(ChatColor.RED + "This region overlaps with someone else's region.");
                return true;
            }

            region.getOwners().addPlayer(player.getName());

            mgr.addRegion(region);
            mgr.save();
            player.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (IncompleteRegionException e) {
            player.sendMessage(ChatColor.RED + "You must first define an area in WorldEdit.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
