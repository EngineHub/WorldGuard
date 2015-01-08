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
import com.sk89q.guavabackport.cache.CacheBuilder;
import com.sk89q.guavabackport.cache.CacheLoader;
import com.sk89q.guavabackport.cache.LoadingCache;
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

import java.util.concurrent.TimeUnit;

public class PlayerMoveListener implements Listener {

    private final WorldGuardPlugin plugin;
    private LoadingCache<WorldPlayerTuple, Boolean> bypassCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .build(new CacheLoader<WorldPlayerTuple, Boolean>() {
                @Override
                public Boolean load(WorldPlayerTuple tuple) throws Exception {
                    return plugin.getGlobalRegionManager().hasBypass(tuple.player, tuple.world);
                }
            });

    public PlayerMoveListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        if (plugin.getGlobalStateManager().usePlayerMove) {
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvents(this, plugin);
        }
    }

    private boolean hasBypass(Player player, World world) {
        return bypassCache.getUnchecked(new WorldPlayerTuple(world, player));
    }

    /**
     * Handles movement related events, including changing gamemode, sending
     * greeting/farewell messages, etc.
     *
     * @param from The before location
     * @param to The to location
     * @return True if the movement should not be allowed
     */
    public boolean shouldDenyMove(Player player, Location from, Location to) {
        PlayerFlagState state = plugin.getFlagStateManager().getState(player);

        // Flush states in multi-world scenario
        if (state.lastWorld != null && !state.lastWorld.equals(to.getWorld())) {
            plugin.getFlagStateManager().forget(player);
            state = plugin.getFlagStateManager().getState(player);
        }

        // TODO: Clean up this disaster

        World world = from.getWorld();
        World toWorld = to.getWorld();

        LocalPlayer localPlayer = plugin.wrapPlayer(player);
        boolean hasBypass = hasBypass(player, world);
        boolean hasRemoteBypass;
        if (world.equals(toWorld)) {
            hasRemoteBypass = hasBypass;
        } else {
            hasRemoteBypass = hasBypass(player, toWorld);
        }

        RegionManager regions = plugin.getGlobalRegionManager().get(toWorld);
        if (regions == null) {
            return false;
        }

        Vector pt = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
        ApplicableRegionSet set = regions.getApplicableRegions(pt);

        boolean entryAllowed = set.allows(DefaultFlag.ENTRY, localPlayer);
        if (!hasRemoteBypass && !entryAllowed) {
            player.sendMessage(ChatColor.DARK_RED + "You are not permitted to enter this area.");
            return true;
        }

        // Have to set this state
        if (state.lastExitAllowed == null) {
            state.lastExitAllowed = plugin.getRegionContainer().createQuery().getApplicableRegions(from).allows(DefaultFlag.EXIT, localPlayer);
        }

        boolean exitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
        if (!hasBypass && exitAllowed && !state.lastExitAllowed) {
            player.sendMessage(ChatColor.DARK_RED + "You are not permitted to leave this area.");
            return true;
        }

        String greeting = set.getFlag(DefaultFlag.GREET_MESSAGE);
        String farewell = set.getFlag(DefaultFlag.FAREWELL_MESSAGE);
        Boolean notifyEnter = set.getFlag(DefaultFlag.NOTIFY_ENTER);
        Boolean notifyLeave = set.getFlag(DefaultFlag.NOTIFY_LEAVE);
        GameMode gameMode = set.getFlag(DefaultFlag.GAME_MODE);

        if (state.lastFarewell != null && (farewell == null || !state.lastFarewell.equals(farewell))) {
            String replacedFarewell = plugin.replaceMacros(player, BukkitUtil.replaceColorMacros(state.lastFarewell));
            player.sendMessage(replacedFarewell.replaceAll("\\\\n", "\n").split("\\n"));
        }

        if (greeting != null && (state.lastGreeting == null || !state.lastGreeting.equals(greeting))) {
            String replacedGreeting = plugin.replaceMacros(player, BukkitUtil.replaceColorMacros(greeting));
            player.sendMessage(replacedGreeting.replaceAll("\\\\n", "\n").split("\\n"));
        }

        if ((notifyLeave == null || !notifyLeave) && state.notifiedForLeave != null && state.notifiedForLeave) {
            plugin.broadcastNotification(ChatColor.GRAY + "WG: "
                    + ChatColor.LIGHT_PURPLE + player.getName()
                    + ChatColor.GOLD + " left NOTIFY region");
        }

        if (notifyEnter != null && notifyEnter && (state.notifiedForEnter == null || !state.notifiedForEnter)) {
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        World world = player.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            if (hasMoved(event.getFrom(), event.getTo())) {
                if (shouldDenyMove(player, event.getFrom(), event.getTo())) {
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

    private static boolean hasMoved(Location loc1, Location loc2) {
        return loc1.getBlockX() != loc2.getBlockX()
                || loc1.getBlockY() != loc2.getBlockY()
                || loc1.getBlockZ() != loc2.getBlockZ();
    }

    private static class WorldPlayerTuple {
        private final World world;
        private final Player player;

        private WorldPlayerTuple(World world, Player player) {
            this.world = world;
            this.player = player;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WorldPlayerTuple that = (WorldPlayerTuple) o;

            if (!player.equals(that.player)) return false;
            if (!world.equals(that.world)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + player.hashCode();
            return result;
        }
    }

}
