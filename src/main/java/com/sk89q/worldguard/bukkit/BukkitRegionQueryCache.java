// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.RegionQueryCache;

/**
 * An implementation of {@link RegionQueryCache} for Bukkit. This implementation is
 * thread-safe.
 */
public class BukkitRegionQueryCache extends RegionQueryCache {

    private final WorldGuardPlugin plugin;

    /**
     * Construct the cache with the given plugin instance.
     *
     * @param plugin plugin instance
     */
    public BukkitRegionQueryCache(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get an {@link ApplicableRegionSet} for a given location. The result may have been
     * temporarily cached for the given event, or null if region protection has
     * been disabled for the the world of the given location.
     *
     * @param event the event to cache against
     * @param location the location to check
     * @return the set, or null if region protection is disabled for the given location
     */
    public ApplicableRegionSet lookup(Event event, Location location) {
        World world = location.getWorld();
        Vector vector = BukkitUtil.toVector(location);

        ConfigurationManager config = plugin.getGlobalStateManager();
        WorldConfiguration worldConfig = config.get(world);
        GlobalRegionManager regionManager = plugin.getGlobalRegionManager();

        if (!worldConfig.useRegions) {
            return null;
        }

        return get(event).lookup(regionManager.get(world), vector);
    }

    /**
     * Get an {@link ApplicableRegionSet} for a given block. The result may have been
     * temporarily cached for the given event, or null if region protection has
     * been disabled for the the world of the given block.
     *
     * @param event the event to cache against
     * @param block the block to check
     * @return the set, or null if region protection is disabled for the given block
     */
    public ApplicableRegionSet lookup(Event event, Block block) {
        return lookup(event, block.getLocation());
    }

    /**
     * Get an {@link ApplicableRegionSet} for a given entity. The result may have been
     * temporarily cached for the given event, or null if region protection has
     * been disabled for the the world of the given entity.
     *
     * @param event the event to cache against
     * @param entity the entity to check
     * @return the set, or null if region protection is disabled for the given entity
     */
    public ApplicableRegionSet lookup(Event event, Entity entity) {
        return lookup(event, entity.getLocation());
    }

}
