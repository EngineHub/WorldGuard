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
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

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

        if (wcfg.useRegions) {
            // Did we move a block?
            if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                    || event.getFrom().getBlockY() != event.getTo().getBlockY()
                    || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                boolean result = WorldGuardPlayerListener.checkMove(plugin, player, event.getFrom(), event.getTo());
                if (result) {
                    vehicle.setVelocity(new org.bukkit.util.Vector(0,0,0));
                    vehicle.teleport(event.getFrom());
                }
            }
        }
    }
}
