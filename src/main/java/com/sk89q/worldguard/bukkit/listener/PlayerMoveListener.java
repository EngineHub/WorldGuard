/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.listener.FlagStateManager.PlayerFlagState;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;

public class PlayerMoveListener implements Listener {

    private final WorldGuardPlugin plugin;

    public PlayerMoveListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        if (plugin.getGlobalStateManager().usePlayerMove) {
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvents(this, plugin);
        }
    }

    private static boolean hasMoved(Location loc1, Location loc2) {
        return loc1.getBlockX() != loc2.getBlockX()
                || loc1.getBlockY() != loc2.getBlockY()
                || loc1.getBlockZ() != loc2.getBlockZ();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            if (hasMoved(event.getFrom(), event.getTo())) {
                if (shouldDenyMove(plugin, player, event.getFrom(), event.getTo())) {
                    Location newLoc = event.getFrom();
                    newLoc.setX(newLoc.getBlockX() + 0.5);
                    newLoc.setY(newLoc.getBlockY());
                    newLoc.setZ(newLoc.getBlockZ() + 0.5);
                    event.setTo(newLoc);

                    Entity vehicle = player.getVehicle();
                    if (vehicle != null) {
                        vehicle.eject();
                        vehicle.teleport(newLoc);
                        player.teleport(newLoc);
                        vehicle.setPassenger(player);
                    }
                }
            }
        }
    }

    /**
     * Handles movement related events, including changing gamemode, sending
     * greeting/farewell messages, etc.
     *
     * @return true if the movement should not be allowed
     */
    public static boolean shouldDenyMove(WorldGuardPlugin plugin, Player player, Location from, Location to) {
        PlayerFlagState state = plugin.getFlagStateManager().getState(player);

        //Flush states in multiworld scenario
        if (state.lastWorld != null && !state.lastWorld.equals(to.getWorld())) {
            plugin.getFlagStateManager().forget(player);
            state = plugin.getFlagStateManager().getState(player);
        }

        World world = from.getWorld();
        World toWorld = to.getWorld();

        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        boolean hasBypass = plugin.getGlobalRegionManager().hasBypass(player, world);
        boolean hasRemoteBypass;
        if (world.equals(toWorld)) {
            hasRemoteBypass = hasBypass;
        } else {
            hasRemoteBypass = plugin.getGlobalRegionManager().hasBypass(player, toWorld);
        }

        RegionManager mgr = plugin.getGlobalRegionManager().get(toWorld);
        if (mgr == null) {
            return false;
        }
        Vector pt = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
        ApplicableRegionSet set = mgr.getApplicableRegions(pt);

        /*
        // check if region is full
        // get the lowest number of allowed members in any region
        boolean regionFull = false;
        String maxPlayerMessage = null;
        if (!hasBypass) {
            for (ProtectedRegion region : set) {
                if (region instanceof GlobalProtectedRegion) {
                    continue; // global region can't have a max
                }
                // get the max for just this region
                Integer maxPlayers = region.getFlag(DefaultFlag.MAX_PLAYERS);
                if (maxPlayers == null) {
                    continue;
                }
                int occupantCount = 0;
                for(Player occupant : world.getPlayers()) {
                    // each player in this region counts as one toward the max of just this region
                    // A person with bypass doesn't count as an occupant of the region
                    if (!occupant.equals(player) && !plugin.getGlobalRegionManager().hasBypass(occupant, world)) {
                        if (region.contains(BukkitUtil.toVector(occupant.getLocation()))) {
                            if (++occupantCount >= maxPlayers) {
                                regionFull = true;
                                maxPlayerMessage = region.getFlag(DefaultFlag.MAX_PLAYERS_MESSAGE);
                                // At least one region in the set is full, we are going to use this message because it
                                // was the first one we detected as full. In reality we should check them all and then
                                // resolve the message from full regions, but that is probably a lot laggier (and this
                                // is already pretty laggy. In practice, we can't really control which one we get first
                                // right here.
                                break;
                            }
                        }
                    }
                }
            }
        }
        */

        boolean entryAllowed = set.allows(DefaultFlag.ENTRY, localPlayer);
        if (!hasRemoteBypass && (!entryAllowed /*|| regionFull*/)) {
            String message = /*maxPlayerMessage != null ? maxPlayerMessage :*/ "You are not permitted to enter this area.";

            player.sendMessage(ChatColor.DARK_RED + message);
            return true;
        }

        // Have to set this state
        if (state.lastExitAllowed == null) {
            state.lastExitAllowed = plugin.getRegionContainer().createQuery().getApplicableRegions(from)
                        .allows(DefaultFlag.EXIT, localPlayer);
        }

        boolean exitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
        if (!hasBypass && exitAllowed && !state.lastExitAllowed) {
            player.sendMessage(ChatColor.DARK_RED + "You are not permitted to leave this area.");
            return true;
        }

//        WorldGuardRegionMoveEvent event = new WorldGuardRegionMoveEvent(plugin, player, state, set, from, to);
//        Bukkit.getPluginManager().callEvent(event);

        String greeting = set.getFlag(DefaultFlag.GREET_MESSAGE);//, localPlayer);
        String farewell = set.getFlag(DefaultFlag.FAREWELL_MESSAGE);//, localPlayer);
        Boolean notifyEnter = set.getFlag(DefaultFlag.NOTIFY_ENTER);//, localPlayer);
        Boolean notifyLeave = set.getFlag(DefaultFlag.NOTIFY_LEAVE);//, localPlayer);
        GameMode gameMode = set.getFlag(DefaultFlag.GAME_MODE);

        if (state.lastFarewell != null && (farewell == null
                || !state.lastFarewell.equals(farewell))) {
            String replacedFarewell = plugin.replaceMacros(
                    player, BukkitUtil.replaceColorMacros(state.lastFarewell));
            player.sendMessage(replacedFarewell.replaceAll("\\\\n", "\n").split("\\n"));
        }

        if (greeting != null && (state.lastGreeting == null
                || !state.lastGreeting.equals(greeting))) {
            String replacedGreeting = plugin.replaceMacros(
                    player, BukkitUtil.replaceColorMacros(greeting));
            player.sendMessage(replacedGreeting.replaceAll("\\\\n", "\n").split("\\n"));
        }

        if ((notifyLeave == null || !notifyLeave)
                && state.notifiedForLeave != null && state.notifiedForLeave) {
            plugin.broadcastNotification(ChatColor.GRAY + "WG: "
                    + ChatColor.LIGHT_PURPLE + player.getName()
                    + ChatColor.GOLD + " left NOTIFY region");
        }

        if (notifyEnter != null && notifyEnter && (state.notifiedForEnter == null
                || !state.notifiedForEnter)) {
            StringBuilder regionList = new StringBuilder();

            for (ProtectedRegion region : set) {
                if (regionList.length() != 0) {
                    regionList.append(", ");
                }
                regionList.append(region.getId());
            }

            plugin.broadcastNotification(ChatColor.GRAY + "WG: "
                    + ChatColor.LIGHT_PURPLE + player.getName()
                    + ChatColor.GOLD + " entered NOTIFY region: "
                    + ChatColor.WHITE
                    + regionList);
        }

        if (!hasBypass && gameMode != null) {
            if (player.getGameMode() != gameMode) {
                state.lastGameMode = player.getGameMode();
                player.setGameMode(gameMode);
            } else if (state.lastGameMode == null) {
                state.lastGameMode = player.getServer().getDefaultGameMode();
            }
        } else {
            if (state.lastGameMode != null) {
                GameMode mode = state.lastGameMode;
                state.lastGameMode = null;
                player.setGameMode(mode);
            }
        }

        state.lastGreeting = greeting;
        state.lastFarewell = farewell;
        state.notifiedForEnter = notifyEnter;
        state.notifiedForLeave = notifyLeave;
        state.lastExitAllowed = exitAllowed;
        state.lastWorld = to.getWorld();
        state.lastBlockX = to.getBlockX();
        state.lastBlockY = to.getBlockY();
        state.lastBlockZ = to.getBlockZ();
        return false;
    }

}
