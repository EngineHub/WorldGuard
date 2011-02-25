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
import com.sk89q.worldguard.bukkit.WorldGuardConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardWorldConfiguration;
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
public class CommandRegionFlag extends WgRegionCommand {

    public boolean handle(CommandSender sender, String senderName, String command, String[] args, WorldGuardConfiguration cfg, WorldGuardWorldConfiguration wcfg) throws CommandHandlingException {

        CommandHandler.checkArgs(args, 3, 4, "/region flag <regionid> <name> (<subname>) <value>");

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

            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(wcfg.getWorldName());
            ProtectedRegion region = mgr.getRegion(id);

            if (region == null) {
                sender.sendMessage(ChatColor.RED + "Could not find a region by that ID.");
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (region.isOwner(BukkitPlayer.wrapPlayer(cfg, player))) {
                    cfg.checkRegionPermission(sender, "region.flag.ownregions");
                } else if (region.isMember(BukkitPlayer.wrapPlayer(cfg, player))) {
                    cfg.checkRegionPermission(sender, "region.flag.memberregions");
                } else {
                    cfg.checkRegionPermission(sender, "region.flag.foreignregions");
                }
            } else {
                cfg.checkRegionPermission(sender, "region.flag.foreignregions");
            }

            FlagInfo nfo = FlagInfo.getFlagInfo(nameStr, subnameStr);

            if (nfo == null) {
                sender.sendMessage(ChatColor.RED + "Unknown flag specified.");
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
                case LOCATION: {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Flag not supported in console mode.");
                        return true;
                    }
                    Player player = (Player) sender;

                    Location l = player.getLocation();

                    if (valueStr.equals("set")) {

                        if (region.contains(BukkitUtil.toVector(l))) {
                            region.getFlags().setLocationFlag(nfo.flagName, l);
                            validValue = true;
                            sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag " + nameStr + " set to current location");

                        } else {
                            player.sendMessage(ChatColor.RED + "You must set the " + nameStr + " location inside the region it belongs to.");
                            return true;
                        }

                    } else if (valueStr.equals("delete")) {
                        region.getFlags().setLocationFlag(nfo.flagName, null);
                        validValue = true;
                        sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag " + nameStr + " removed.");
                    }


                    if (validValue) {
                        mgr.save();
                        return true;
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
                fullFlagname += " " + subnameStr;
            }

            if (!validValue) {
                sender.sendMessage(ChatColor.RED + "Invalid value '" + valueStr + "' for flag " + fullFlagname);
                return true;
            }

            region.getFlags().setFlag(nfo.flagName, nfo.flagSubName, valueStr);
            mgr.save();

            sender.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated. Flag " + fullFlagname + " set to " + valueStr);
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Region database failed to save: "
                    + e.getMessage());
        }

        return true;
    }
}
