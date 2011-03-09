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
import com.sk89q.worldguard.bukkit.GlobalConfiguration;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.RegionUtil;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Michael
 */
public class CommandRegionDefine extends WgRegionCommand {

    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg,
            WorldConfiguration wcfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        Player player = (Player) sender;
        
        Plugin wePlugin = cfg.getWorldGuardPlugin().getServer().getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null) {
            sender.sendMessage(ChatColor.RED + "WorldEdit must be installed and enabled!");
            return true;
        }
        plugin.checkPermission(sender, "region.define");
        CommandHandler.checkArgs(args, 1, -1, "/region define <id> [owner1 [owner2 [owners...]]]");

        try {
            String id = args[0].toLowerCase();

            WorldEditPlugin worldEdit = (WorldEditPlugin) wePlugin;
            World w = player.getWorld();

            LocalSession session = worldEdit.getSession(player);
            Region weRegion = session.getSelection(new BukkitWorld(w));

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

            if (args.length >= 2) {
                region.setOwners(RegionUtil.parseDomainString(args, 1));
            }
            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(w.getName());
            mgr.addRegion(region);
            mgr.save();
            sender.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
        } catch (IncompleteRegionException e) {
            sender.sendMessage(ChatColor.RED + "You must first define an area in WorldEdit.");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
