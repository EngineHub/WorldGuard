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

import static com.sk89q.worldguard.bukkit.BukkitUtil.matchSinglePlayer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijikokun.bukkit.iConomy.iConomy;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.AreaFlags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 *
 * @author DarkLiKally
 */
public class CommandBuyRegion extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, CommandHandler ch, WorldGuardPlugin wg) throws CommandHandlingException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        if (wg.iConomy == null) {
            sender.sendMessage("iConomy is not installed on this Server.");
            return true;
        }
        String id = args[0];
        Player player = (Player) sender;
        RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());
        ProtectedRegion region = mgr.getRegion(id);
        if (region != null) {
            AreaFlags flags = region.getFlags();

            if (flags.getBooleanFlag("iconomy", "buyable", false)) {
                if (args.length == 2) {
                    if (args[1] == "info") {
                        player.sendMessage(ChatColor.YELLOW + "Region " + id + " costs " + 
                                iConomy.Misc.formatCurrency(flags.getIntFlag("iconomy", "price"), iConomy.currency));
                        if (iConomy.database.hasBalance(player.getName())) {
                            player.sendMessage(ChatColor.YELLOW + "You have " +
                                    iConomy.Misc.formatCurrency((int)Math.round(iConomy.database.getBalance(player.getName())), iConomy.currency));
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Usage: /buyregion <region id> (info)");
                    }
                } else {
                    if (iConomy.database.hasBalance(player.getName())) {
                        double balance = iConomy.database.getBalance(player.getName());
                        int regionPrice = flags.getIntFlag("iconomy", "price");

                        if (balance >= regionPrice) {
                            iConomy.database.setBalance(player.getName(), balance - regionPrice);
                            player.sendMessage(ChatColor.YELLOW + "You have bought the region " + id + " for " +
                                    iConomy.Misc.formatCurrency(regionPrice, iConomy.currency));
                        }
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "Region: " + id + " is not buyable");
            } 
        } else {
            player.sendMessage(ChatColor.RED + "Region: " + id + " not defined");
        }
        return true;
    }
}
