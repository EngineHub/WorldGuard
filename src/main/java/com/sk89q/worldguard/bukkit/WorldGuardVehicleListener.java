// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/
package com.sk89q.worldguard.bukkit;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.FlagStateManager.PlayerFlagState;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardVehicleListener implements Listener {

    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin
     */
    public WorldGuardVehicleListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Vehicle vehicle = event.getVehicle();
        Entity destroyer = event.getAttacker();

        if (!(destroyer instanceof Player)) return; // don't care
        Player player = (Player) destroyer;
        World world = vehicle.getWorld();

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        if (wcfg.useRegions) {
            Vector pt = toVector(vehicle.getLocation());
            RegionManager mgr = plugin.getGlobalRegionManager().get(world);
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (!plugin.getGlobalRegionManager().hasBypass(player, world)
                    && !set.canBuild(localPlayer)
                    && !set.allows(DefaultFlag.DESTROY_VEHICLE, localPlayer)) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission to destroy vehicles here.");
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle.getPassenger() == null
                || !(vehicle.getPassenger() instanceof Player)) return;
        Player player = (Player) vehicle.getPassenger();
        World world = vehicle.getWorld();
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(world);

        // unfortunate code duplication
        if (wcfg.useRegions) {
            // Did we move a block?
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                PlayerFlagState state = plugin.getFlagStateManager().getState(player);
                LocalPlayer localPlayer = plugin.wrapPlayer(player);
                boolean hasBypass = plugin.getGlobalRegionManager().hasBypass(player, world);

                RegionManager mgr = plugin.getGlobalRegionManager().get(world);
                Vector pt = new Vector(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
                ApplicableRegionSet set = mgr.getApplicableRegions(pt);

                boolean entryAllowed = set.allows(DefaultFlag.ENTRY, localPlayer);
                if (!hasBypass && !entryAllowed) {

                    vehicle.setVelocity(new org.bukkit.util.Vector(0,0,0));
                    vehicle.teleport(event.getFrom());
                    return;
                }

                // Have to set this state
                if (state.lastExitAllowed == null) {
                    state.lastExitAllowed = mgr.getApplicableRegions(toVector(event.getFrom()))
                            .allows(DefaultFlag.EXIT, localPlayer);
                }

                boolean exitAllowed = set.allows(DefaultFlag.EXIT, localPlayer);
                if (!hasBypass && exitAllowed && !state.lastExitAllowed) {
                    player.sendMessage(ChatColor.DARK_RED + "You are not permitted to leave this area.");

                    vehicle.setVelocity(new org.bukkit.util.Vector(0,0,0));
                    vehicle.teleport(event.getFrom());
                    return;
                }

                String greeting = set.getFlag(DefaultFlag.GREET_MESSAGE, localPlayer);
                String farewell = set.getFlag(DefaultFlag.FAREWELL_MESSAGE, localPlayer);
                String texture = set.getFlag(DefaultFlag.TEXTURE_PACK);
                Boolean notifyEnter = set.getFlag(DefaultFlag.NOTIFY_ENTER, localPlayer);
                Boolean notifyLeave = set.getFlag(DefaultFlag.NOTIFY_LEAVE, localPlayer);

                if (state.lastFarewell != null && (farewell == null
                        || !state.lastFarewell.equals(farewell))) {
                    String replacedFarewell = plugin.replaceMacros(
                            player, BukkitUtil.replaceColorMacros(state.lastFarewell));
                    player.sendMessage(ChatColor.AQUA + " ** " + replacedFarewell);
                }

                if (greeting != null && (state.lastGreeting == null
                        || !state.lastGreeting.equals(greeting))) {
                    String replacedGreeting = plugin.replaceMacros(
                            player, BukkitUtil.replaceColorMacros(greeting));
                    player.sendMessage(ChatColor.AQUA + " ** " + replacedGreeting);
                }

                if (texture != null && (state.lastTexture == null
                        || !state.lastTexture.equals(texture))) {
                        plugin.switchTexturePack(player, texture);
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

                state.lastGreeting = greeting;
                state.lastFarewell = farewell;
                state.lastTexture = texture;
                state.notifiedForEnter = notifyEnter;
                state.notifiedForLeave = notifyLeave;
                state.lastExitAllowed = exitAllowed;
                state.lastWorld = event.getTo().getWorld();
                state.lastBlockX = event.getTo().getBlockX();
                state.lastBlockY = event.getTo().getBlockY();
                state.lastBlockZ = event.getTo().getBlockZ();
            }
        }
    }
}
