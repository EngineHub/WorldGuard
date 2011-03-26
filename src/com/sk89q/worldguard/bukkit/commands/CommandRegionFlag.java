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
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.GlobalConfiguration;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.CommandHandlingException;
import com.sk89q.worldguard.protection.flags.FlagDatabase;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import com.sk89q.worldguard.protection.flags.RegionFlag.FlagDataType;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Michael
 */
public class CommandRegionFlag extends WgRegionCommand {
    @Override
    public boolean handle(CommandSender sender, String senderName,
            String command, String[] args, GlobalConfiguration cfg,
            WorldConfiguration wcfg, WorldGuardPlugin plugin)
            throws CommandHandlingException {
        
        CommandHandler.checkArgs(args, 2, -1, "/region flag <regionid> <name> (<value>) [no value to unset flag]");

        try {
            String id = args[0].toLowerCase();
            String nameStr = args[1];
            String valueStr = null;

            if (args.length == 3) {
                valueStr = args[2];
            } else if (args.length > 3) {
                StringBuilder tmp = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    tmp.append(args[i]);
                }
                valueStr = tmp.toString();
            }

            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().get(wcfg.getWorldName());
            ProtectedRegion region = mgr.getRegion(id);

            if (region == null) {
                sender.sendMessage(ChatColor.RED + "Could not find a region by that ID.");
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (region.isOwner(BukkitPlayer.wrapPlayer(plugin, player))) {
                    plugin.checkPermission(sender, "region.flag.own");
                } else if (region.isMember(BukkitPlayer.wrapPlayer(plugin, player))) {
                    plugin.checkPermission(sender, "region.flag.member");
                } else {
                    plugin.checkPermission(sender, "region.flag");
                }
            } else {
                plugin.checkPermission(sender, "region.flag");
            }

            Flag nfo = FlagDatabase.getFlagInfoFromName(nameStr);

            if (nfo == null) {
                sender.sendMessage(ChatColor.RED + "Unknown flag specified.");
                return true;
            }

            if (nfo instanceof LocationFlag) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Flag not supported in console mode.");
                    return true;
                }
                Player player = (Player) sender;
                LocationFlag lInfo = (LocationFlag)nfo;

                Location l = player.getLocation();

                if (valueStr != null && valueStr.equals("set")) {

                    if (region.contains(BukkitUtil.toVector(l))) {
                        region.getFlags().getLocationFlag(lInfo).setValue(l);
                        sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag " + nameStr + " set to current location");
                        return true;

                    } else {
                        player.sendMessage(ChatColor.RED + "You must set the " + nameStr + " location inside the region it belongs to.");
                        return true;
                    }

                } else if (valueStr == null || valueStr.equals("delete")) {
                    region.getFlags().getLocationFlag(lInfo).setValue((Location) null);
                    sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag " + nameStr + " removed.");
                    return true;
                }
            }

            if (!region.getFlags().getFlag(nfo).setValue(valueStr)) {
                sender.sendMessage(ChatColor.RED + "Invalid value '" + valueStr + "' for flag " + nameStr);
                return true;
            } else {
                mgr.save();
                if (valueStr == null) {
                    valueStr = "null";
                }
                sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag " + nameStr + " set to " + valueStr);
                return true;
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
