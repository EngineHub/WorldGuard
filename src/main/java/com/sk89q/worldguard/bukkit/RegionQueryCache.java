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

import org.bukkit.event.Event;

import com.sk89q.worldguard.protection.AbstractRegionQueryCache;

/**
 * An implementation of {@link AbstractRegionQueryCache} for Bukkit. This implementation
 * is thread-safe.
 */
public final class RegionQueryCache extends AbstractRegionQueryCache {

    private final WorldGuardPlugin plugin;

    /**
     * Construct the cache with the given plugin instance.
     *
     * @param plugin plugin instance
     */
    public RegionQueryCache(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get a {@link RegionQuery} object that can cache region queries. The object
     * returned by this method does not need to be cached. It is merely a proxy class.
     *
     * @param event the event to cache against
     * @return an object to perform lookups against
     */
    public RegionQuery against(Event event) {
        return new RegionQuery(plugin, get(event));
    }

}
