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

package com.sk89q.worldguard.region;

import java.util.WeakHashMap;

/**
 * To facilitate performing regions-containing-point lookups from multiple stack frames
 * but within the same context (the same event), a cache is important in preventing the
 * need for redundant lookups made for the same point. This class serves as such a cache.
 * <p>
 * A key needs to be selected to store cache entries against. The best key is one that
 * is highly temporal, and would represent a certain "snapshot in time," such as
 * an event object. The event objects are indexed in this class using weak references,
 * and they will be removed by the garbage collector automatically. Because it
 * <em>is</em>  a weak reference, the key object needs to be held somewhere with a strong
 * reference until the "snapshot" ends.
 * <p>
 * This class is abstract because implementing class should provide methods to check
 * against a standard key object. This cache itself is thread-safe.
 */
public abstract class AbstractRegionQueryCache {

    private final WeakHashMap<Object, RegionQueryCacheEntry> cache =
            new WeakHashMap<Object, RegionQueryCacheEntry>();

    public AbstractRegionQueryCache() {
    }

    /**
     * Get the {@link RegionQueryCacheEntry} object that will cache the
     * {@link ApplicableRegionSet}s for this context.
     *
     * @param key a standard key to store by
     * @return an existing QueryCacheEntry or a new one
     */
    protected synchronized RegionQueryCacheEntry get(Object key) {
        RegionQueryCacheEntry entry = cache.get(key);
        if (entry == null) {
            entry = new RegionQueryCacheEntry();
            cache.put(key, entry);
        }
        return entry;
    }

}
