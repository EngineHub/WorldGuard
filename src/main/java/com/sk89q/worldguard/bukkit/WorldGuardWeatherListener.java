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

import java.util.logging.Logger;

public class WorldGuardWeatherListener extends WeatherListener {

    /**
     * Logger for messages.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

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

        registerEvent("LIGHTNING_STRIKE", Priority.High);
        registerEvent("THUNDER_CHANGE", Priority.High);
        registerEvent("WEATHER_CHANGE", Priority.High);
    }

    /**
     * Register an event, but not failing if the event is not implemented.
     *
     * @param typeName
     * @param priority
     */
    private void registerEvent(String typeName, Priority priority) {
        try {
            Event.Type type = Event.Type.valueOf(typeName);
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvent(type, this, priority, plugin);
        } catch (IllegalArgumentException e) {
            logger.info("WorldGuard: Unable to register missing event type " + typeName);
        }
    }

    @Override
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.isCancelled()) {
           return;
        }
        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getWorld());

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

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getWorld());

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

        ConfigurationManager cfg = plugin.getGlobalStateManager();
        WorldConfiguration wcfg = cfg.get(event.getWorld());

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
