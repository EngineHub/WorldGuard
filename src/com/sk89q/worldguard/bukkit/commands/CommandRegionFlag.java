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
import com.sk89q.worldguard.protection.regions.AreaFlags;
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
public class CommandRegionFlag extends WgCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, CommandHandler ch, WorldGuardPlugin wg) throws CommandHandlingException {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command");
            return true;
        }
        Player player = (Player) sender;
        ch.checkRegionPermission(player, "/regiondefine");
        ch.checkArgs(args, 3, 4, "/region flag <regionid> <name> (<subname>) <value>");

        try {
            String id = args[0].toLowerCase();
            String nameStr = args[1];
            String subnameStr = null;
            String valueStr = null;
            if (args.length < 4) {
                valueStr = args[2];
            } else {
                subnameStr = args[2];
                valueStr = args[3];
            }

            RegionManager mgr = wg.getGlobalRegionManager().getRegionManager(player.getWorld().getName());
            ProtectedRegion region = mgr.getRegion(id);

            if (region == null) {
                player.sendMessage(ChatColor.RED + "Could not find a region by that ID.");
                return true;
            }

            FlagInfo nfo = FlagInfo.getFlagInfo(nameStr, subnameStr);

            if (nfo == null) {
                if(nameStr.equals("spawn"))
                {
                    if (valueStr.equals("set")) {
                        player.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag spawn set to current location");
                        AreaFlags flags = region.getFlags();
                        Location l = player.getLocation();
                        flags.setFlag("spawn", "x", l.getX());
                        flags.setFlag("spawn", "y", l.getY());
                        flags.setFlag("spawn", "z", l.getZ());
                        flags.setFlag("spawn", "yaw", l.getYaw());
                        flags.setFlag("spawn", "pitch", l.getPitch());
                        flags.setFlag("spawn", "world", l.getWorld().getName());
                    } else {
                        AreaFlags flags = region.getFlags();
                        flags.setFlag("spawn", "x", (String)null);
                        flags.setFlag("spawn", "y", (String)null);
                        flags.setFlag("spawn", "z", (String)null);
                        flags.setFlag("spawn", "yaw", (String)null);
                        flags.setFlag("spawn", "pitch", (String)null);
                        flags.setFlag("spawn", "world", (String)null);
                        player.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag spawn removed.");
                    }
                }
                else 
                {
                    player.sendMessage(ChatColor.RED + "Unknown flag specified.");
                }
                return true;
            }

            boolean validValue = false;
            switch (nfo.type) {
                case STRING: {
                    validValue = true;
                    break;
                }
                case INT: {
                    validValue = true;
                    try {
                        Integer val = Integer.valueOf(valueStr);
                    } catch (Exception e) {
                        validValue = false;
                    }
                    break;
                }
                case BOOLEAN: {
                    valueStr = valueStr.toLowerCase();
                    if (valueStr.equals("on")) {
                        valueStr = "true";
                    } else if (valueStr.equals("allow")) {
                        valueStr = "true";
                    }
                    validValue = true;
                    break;
                }
                case FLOAT: {
                    validValue = true;
                    try {
                        Float val = Float.valueOf(valueStr);
                    } catch (Exception e) {
                        validValue = false;
                    }
                    break;
                }
                case DOUBLE: {
                    validValue = true;
                    try {
                        Double val = Double.valueOf(valueStr);
                    } catch (Exception e) {
                        validValue = false;
                    }
                    break;
                }
                case STATE: {
                    validValue = true;

                    if (valueStr.equalsIgnoreCase("allow")) {
                        valueStr = AreaFlags.State.ALLOW.toString();
                    } else if (valueStr.equalsIgnoreCase("deny")) {
                        valueStr = AreaFlags.State.DENY.toString();
                    } else if (valueStr.equalsIgnoreCase("none")) {
                        valueStr = AreaFlags.State.NONE.toString();
                    } else {
                        validValue = false;
                    }
                    break;
                }
                default: {
                    validValue = false;
                    break;
                }
            }

            String fullFlagname = nameStr;
            if (subnameStr != null) {
                nameStr += " " + subnameStr;
            }

            if (!validValue) {
                player.sendMessage(ChatColor.RED + "Invalid value '" + valueStr + "' for flag " + fullFlagname);
                return true;
            }

            region.getFlags().setFlag(nfo.flagName, nfo.flagSubName, valueStr);
            mgr.save();

            player.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag " + fullFlagname + " set to " + valueStr);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
