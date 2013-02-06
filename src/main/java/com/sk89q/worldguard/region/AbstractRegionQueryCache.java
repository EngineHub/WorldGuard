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
 * A simple cache to reduce hits to the region manager when region information
 * is needed for the same location in the same event context.
 * <p>
 * If two or more pieces of code (such as from different programs or plugins)
 * need to ask the question of "which regions contain this point" within the
 * same event (such as when a player breaks a block), it is ideal that a query
 * is only performed once and a cached answer is returned for the subsequent
 * requests for this information (but only within the same context or event!).
 * This is because queries to the region manager are often demanding and it
 * would be taxing to perform the same search more than once. The purpose of
 * this class is to facilitate exactly that.
 * <p>
 * In order for this cache to work, a key must be generated for each query to
 * this cache. If two pieces of code need to execute the same region query
 * during the same event (consider this at time A), both pieces of code must
 * give the instance of this class the exact same 'key' object in order for the
 * second calling piece of code to receive the cached result. However, once that
 * event has been processed and time has moved on (proceeding to time X+1), that
 * previously chosen key object must not ever be used again, otherwise it may
 * return the results that were found at time X, which may be out of date.
 * <p>
 * Weak references are used to refer to that key object, and so whatever key
 * object is chosen can be properly garbage collected once it is no longer
 * needed outside the context of this cache.
 * <p>
 * This cache itself is thread-safe.
 */
public abstract class AbstractRegionQueryCache {

    private final WeakHashMap<Object, RegionQueryCacheEntry> cache =
            new WeakHashMap<Object, RegionQueryCacheEntry>();

    /**
     * Construct a copy of this cache.
     */
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
