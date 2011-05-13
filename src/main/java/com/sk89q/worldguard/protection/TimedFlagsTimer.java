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

package com.sk89q.worldguard.protection;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

/**
 * Timer for handling flags that require a constant timer..
 * 
 * @author Michael
 */
public class TimedFlagsTimer implements Runnable {

    private WorldGuardPlugin plugin;

    private Map<String, TimedFlagPlayerInfo> playerData;

    /**
     * Construct the object.
     * 
     * @param plugin
     */
    public TimedFlagsTimer(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<String, TimedFlagPlayerInfo>();
    }

    private TimedFlagPlayerInfo getPlayerInfo(String name) {
        TimedFlagPlayerInfo ret = playerData.get(name);
        if (ret == null) {
            ret = new TimedFlagPlayerInfo();
            playerData.put(name, ret);
        }

        return ret;
    }

    public void run() {
        Player[] players = plugin.getServer().getOnlinePlayers();

        for (Player player : players) {
        	TimedFlagPlayerInfo playerInfo = getPlayerInfo(player.getName());
            ConfigurationManager config = plugin.getGlobalConfiguration();
            WorldConfiguration worldConfig = config.get(player.getWorld());

            if (worldConfig.useRegions) {
                Vector playerLocation = toVector(player.getLocation());
                RegionManager regionManager = plugin.getGlobalRegionManager().get(player.getWorld());
                ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(playerLocation);

                List <ProtectedRegion> protectedRegions = new ArrayList<ProtectedRegion>();
                Iterator<ProtectedRegion> protectedRegionsIterator = applicableRegions.iterator();
                Iterator<ProtectedRegion> prevProtectedRegionIterator = null;
                
                if (playerInfo.lastRegions != null) 
                	prevProtectedRegionIterator = playerInfo.lastRegions.iterator();
                
                // ----
                // ==== Healing Flags ====
                // ----
                
                long time = System.currentTimeMillis();
                
                if (playerInfo.sheduledHealTick != null && time >= playerInfo.sheduledHealTick) {
                    player.setHealth(player.getHealth() + playerInfo.sheduledHealAmount);
                    playerInfo.sheduledHealTick = null;
                    playerInfo.lastHealTick = time;
                }
                                
               while(protectedRegionsIterator.hasNext()) {
                	ProtectedRegion protectedRegion = protectedRegionsIterator.next();
                	
                	Integer healDelay = protectedRegion.getFlag(DefaultFlag.HEAL_DELAY);
                	Integer healAmount = protectedRegion.getFlag(DefaultFlag.HEAL_AMOUNT);
                	
                	if (healDelay != null && healAmount != null && healDelay > 0) {
                		healDelay *= 1000;

                		if (time - playerInfo.lastHealTick > healDelay) {
                			if (player.getHealth() < 20) {
                				if (player.getHealth() + healAmount > 20)
                					player.setHealth(20);
                				else
                					player.setHealth(player.getHealth() + healAmount);
                			}
                		}
                		else {
                			playerInfo.sheduledHealTick = time + healDelay;
                			playerInfo.sheduledHealAmount = healAmount;
                		}
                	}

                    // ----
                    // ==== Greeting Flags ====
                    // ----

                	if (playerInfo.lastRegions != null && !playerInfo.lastRegions.contains(protectedRegion)) {
                    	String greetMsg = protectedRegion.getFlag(DefaultFlag.GREET_MESSAGE);
                    	Boolean notifyGreet = protectedRegion.getFlag(DefaultFlag.NOTIFY_GREET);

                    	if (greetMsg != null)
                    		player.sendMessage(greetMsg);
                    	                   	
                    	if (notifyGreet != null && notifyGreet)
                            broadcastNotification(ChatColor.YELLOW + "Player " + player.getName() + " entered region " + protectedRegion.getId());
                	}
                	
                    // ----
                    // ==== Passthrough Flag ====
                    // ----
                	
                	State passthrough = protectedRegion.getFlag(DefaultFlag.PASSTHROUGH);
                	
                    if (passthrough != null && passthrough == State.DENY) {
                        Location newLoc = player.getLocation().clone();
                        newLoc.setX(newLoc.getBlockX() - 30);
                        newLoc.setY(newLoc.getWorld().getHighestBlockYAt(newLoc) + 1);
                        player.teleport(newLoc);
                    }
                	
                	if (!protectedRegions.contains(protectedRegion))
                		protectedRegions.add(protectedRegion);
                }
               
               // ----
               // ==== Farewell Flags ====
               // ----
                
                if (prevProtectedRegionIterator != null) {
                    while(prevProtectedRegionIterator.hasNext()) {
                    	ProtectedRegion protectedRegion = prevProtectedRegionIterator.next();
                    	
                    	if (!protectedRegions.contains(protectedRegion)) {
                        	String farewellMsg = protectedRegion.getFlag(DefaultFlag.FAREWELL_MESSAGE);
                        	Boolean notifyFarewell = protectedRegion.getFlag(DefaultFlag.NOTIFY_FAREWELL);
                        	
                        	if (farewellMsg != null)
                        		player.sendMessage(farewellMsg);
                       
                           	if (notifyFarewell != null && notifyFarewell)
                                broadcastNotification(ChatColor.YELLOW + "Player " + player.getName() + " left region " + protectedRegion.getId());
                    	}
                    }
                }
                                
                playerInfo.lastRegions = protectedRegions;
            }
        }
    }

    public void broadcastNotification(String msg) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.hasPermission(player, "notify_onenter")) {
                player.sendMessage(msg);
            }
        }
    }

}
