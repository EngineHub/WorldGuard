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
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardWorldConfiguration;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.nijiko.coelho.iConomy.*;
import com.nijiko.coelho.iConomy.system.*;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Michael
 */
public class CommandRegionClaim extends WgRegionCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg, WorldGuardWorldConfiguration wcfg) throws CommandHandlingException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        Player player = (Player) sender;

        Plugin wePlugin = cfg.getWorldGuardPlugin().getServer().getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null) {
            player.sendMessage(ChatColor.RED + "WorldEdit must be installed and enabled!");
            return true;
        }

        cfg.checkRegionPermission(player, "region.claim");
        CommandHandler.checkArgs(args, 1, 1, "/region claim <id>");

        try {
            String id = args[0].toLowerCase();
            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(player.getWorld().getName());

            LocalPlayer lPlayer = BukkitPlayer.wrapPlayer(cfg, player);

            if (mgr.getRegionCountOfPlayer(lPlayer) >= wcfg.maxRegionCountPerPlayer) {
                player.sendMessage(ChatColor.RED + "You own too much regions, delete one first to claim a new one.");
                return true;
            }

            ProtectedRegion existing = mgr.getRegion(id);

            if (existing != null) {
                if (!existing.getOwners().contains(lPlayer)) {
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

            ApplicableRegionSet regions = mgr.getApplicableRegions(region);
            if (regions.isAnyRegionAffected()) {
                if (!regions.isOwner(lPlayer)) {
                    player.sendMessage(ChatColor.RED + "This region overlaps with someone else's region.");
                    return true;
                }
                region.setPriority(regions.getAffectedRegionPriority() + 1);
            }
            else
            {
                if(wcfg.claimOnlyInsideExistingRegions)
                {
                    player.sendMessage(ChatColor.RED + "You may only claim regions inside existing regions that you or your group own.");
                    return true;
                }
            }

            if (region.countBlocks() > wcfg.maxClaimVolume) {
                player.sendMessage(ChatColor.RED + "This region is to large to claim.");
                player.sendMessage(ChatColor.RED + "Max. volume: " + wcfg.maxClaimVolume + " Your volume: " + region.countBlocks());
                return true;
            }

            region.getOwners().addPlayer(player.getName());

            if (cfg.getiConomy() != null && wcfg.useiConomy && wcfg.buyOnClaim) {
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
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
                    return true;
                }
            }

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
