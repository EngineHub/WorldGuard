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

package com.sk89q.worldguard.protection.regions;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionQuery.QueryOption;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps a cache of {@link RegionResultSet}s. The contents of the cache
 * must be externally invalidated occasionally (and frequently).
 *
 * <p>This class is fully concurrent.</p>
 */
public class QueryCache {

    private final ConcurrentMap<CacheKey, Map<QueryOption, ApplicableRegionSet>> cache = new ConcurrentHashMap<>(16, 0.75f, 2);

    /**
     * Get from the cache a {@code ApplicableRegionSet} if an entry exists;
     * otherwise, query the given manager for a result and cache it.
     *
     * @param manager the region manager
     * @param location the location
     * @param option the option
     * @return a result
     */
    public ApplicableRegionSet queryContains(RegionManager manager, Location location, QueryOption option) {
        checkNotNull(manager);
        checkNotNull(location);
        checkNotNull(option);

        CacheKey key = new CacheKey(location);
        return cache.compute(key, (k, v) -> option.createCache(manager, location, v)).get(option);
    }

    /**
     * Invalidate the cache and clear its contents.
     */
    public void invalidateAll() {
        cache.clear();
    }

    /**
     * Key object for the map.
     */
    private static class CacheKey {
        private final World world;
        private final int x;
        private final int y;
        private final int z;
        private final int hashCode;

        private CacheKey(Location location) {
            this.world = (World) location.getExtent();
            this.x = location.getBlockX();
            this.y = location.getBlockY();
            this.z = location.getBlockZ();

            // Pre-compute hash code
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            this.hashCode = result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (x != cacheKey.x) return false;
            if (y != cacheKey.y) return false;
            if (z != cacheKey.z) return false;
            if (!world.equals(cacheKey.world)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

}
