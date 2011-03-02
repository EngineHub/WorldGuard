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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.*;
import com.sk89q.worldguard.bukkit.WorldGuardConfiguration;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.flags.Flags;
import com.sk89q.worldguard.protection.regions.flags.RegionFlagContainer;

/**
 *
 * @author DarkLiKally
 */
public class CommandBuyRegion extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg) throws CommandHandlingException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        Player player = (Player) sender;

        if (cfg.getiConomy() == null) {
            sender.sendMessage("iConomy is not installed on this Server.");
            return true;
        }

        CommandHandler.checkArgs(args, 1, 2);

        cfg.checkRegionPermission(player, "buyregion");

        String id = args[0];

        RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(player.getWorld().getName());
        ProtectedRegion region = mgr.getRegion(id);
        if (region != null) {
            RegionFlagContainer flags = region.getFlags();

            if (flags.getBooleanFlag(Flags.BUYABLE).getValue(false)) {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("info")) {
                        player.sendMessage(ChatColor.YELLOW + "Region " + id + " costs " + 
                                iConomy.getBank().format(flags.getDoubleFlag(Flags.PRICE).getValue()));
                        if (iConomy.getBank().hasAccount(player.getName())) {
                            player.sendMessage(ChatColor.YELLOW + "You have " +
                                    iConomy.getBank().format(
                                            iConomy.getBank().getAccount(player.getName()).getBalance()));
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Usage: /buyregion <region id> (info)");
                    }
                } else {
                    if (iConomy.getBank().hasAccount(player.getName())) {
                        Account account = iConomy.getBank().getAccount(player.getName());
                        double balance = account.getBalance();
                        double regionPrice = flags.getDoubleFlag(Flags.PRICE).getValue();

                        if (balance >= regionPrice) {
                            account.subtract(regionPrice);
                            player.sendMessage(ChatColor.YELLOW + "You have bought the region " + id + " for " +
                                    iConomy.getBank().format(regionPrice));
                            DefaultDomain owners = region.getOwners();
                            owners.addPlayer(player.getName());
                            region.setOwners(owners);
                            flags.getBooleanFlag(Flags.BUYABLE).setValue(false);
                            account.save();
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "You have not enough money.");
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
