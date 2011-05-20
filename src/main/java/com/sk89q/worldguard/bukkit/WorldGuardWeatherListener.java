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

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherListener;
import org.bukkit.plugin.PluginManager;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class WorldGuardWeatherListener extends WeatherListener {

    /**
     * Plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * Construct the object;
     *
     * @param plugin
     */
    public WorldGuardWeatherListener(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();

        pm.registerEvent(Event.Type.LIGHTNING_STRIKE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.THUNDER_CHANGE, this, Priority.High, plugin);
        pm.registerEvent(Event.Type.WEATHER_CHANGE, this, Priority.High, plugin);
    }

    @Override
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.isCancelled()) {
           return;
        }
        GlobalStateManager cfg = plugin.getGlobalConfiguration();
        WorldStateManager wcfg = cfg.get(event.getWorld());

        if (event.toWeatherState()) {
            if (wcfg.disableWeather) {
                event.setCancelled(true);
            }
        } else {
            if (!wcfg.disableWeather && wcfg.alwaysRaining) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.isCancelled()) {
           return;
        }

        GlobalStateManager cfg = plugin.getGlobalConfiguration();
        WorldStateManager wcfg = cfg.get(event.getWorld());

        if (event.toThunderState()) {
            if (wcfg.disableThunder) {
                event.setCancelled(true);
            }
        } else {
            if (!wcfg.disableWeather && wcfg.alwaysThundering) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onLightningStrike(LightningStrikeEvent event) {
        if (event.isCancelled()) {
           return;
        }

        GlobalStateManager cfg = plugin.getGlobalConfiguration();
        WorldStateManager wcfg = cfg.get(event.getWorld());

        if (wcfg.disallowedLightningBlocks.size() > 0) {
            int targetId = event.getLightning().getLocation().getBlock().getTypeId();
            if (wcfg.disallowedLightningBlocks.contains(targetId)) {
                event.setCancelled(true);
            }
        }

        Location loc = event.getLightning().getLocation();
        if (wcfg.useRegions) {
            Vector pt = toVector(loc);
            RegionManager mgr = plugin.getGlobalRegionManager().get(loc.getWorld());
            ApplicableRegionSet set = mgr.getApplicableRegions(pt);

            if (!set.allows(DefaultFlag.LIGHTNING)) {
                event.setCancelled(true);
            }
        }
    }

}
