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
package com.sk89q.worldguard.bukkit;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.plugin.PluginManager;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.flags.Flags;
import org.bukkit.Location;

/**
 *
 * @author DarkLiKally
 */
public class WorldGuardVehicleListener extends VehicleListener {
    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;
    /**
     * Construct the object;
     *
     * @param plugin
     */
    public WorldGuardVehicleListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {

        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.VEHICLE_CREATE, this, Priority.High, plugin);
    }

    /**
     * Called when a vehicle is created by a player.
     *
     * @param event Relevant event details
     */
    @Override
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle vhcl = event.getVehicle();
        Location vhclLoc = vhcl.getLocation();
        Vector pt = new Vector(vhclLoc.getBlockX(), vhclLoc.getBlockY(), vhclLoc.getBlockZ());

        if (vhcl instanceof Minecart || vhcl instanceof Boat) {
            WorldGuardConfiguration cfg = plugin.getWgConfiguration();
            RegionManager mgr = cfg.getWorldGuardPlugin().getGlobalRegionManager().getRegionManager(vhcl.getWorld().getName());
            ApplicableRegionSet applicableRegions = mgr.getApplicableRegions(pt);

            if (!applicableRegions.isStateFlagAllowed(Flags.PLACE_VEHICLE)) {
                vhcl.remove();
                return;
            }
        }
    }
}
