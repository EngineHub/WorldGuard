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

import java.util.HashMap;
import java.util.Map;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.region.indices.RegionIndex;
import com.sk89q.worldguard.region.regions.ProtectedRegion;

/**
 * An object to cache a list of {@link ApplicableRegionSet}s. This class is
 * thread-safe.
 *
 * @see AbstractRegionQueryCache
 */
public final class RegionQueryCacheEntry {

    private final Map<BlockVector, ApplicableRegionSet> cachedPoints =
            new HashMap<BlockVector, ApplicableRegionSet>();

    RegionQueryCacheEntry() {
    }

    /**
     * Get an {@link ApplicableRegionSet} for a given point. If a lookup already has
     * been performed, a cached result will be returned.
     *
     * @param manager the manager
     * @param location the location to lookup
     * @return the applicable region set
     */
    public synchronized ApplicableRegionSet lookup(RegionIndex manager, Vector location) {
        BlockVector hashableLocation = location.toBlockVector();
        ApplicableRegionSet set = cachedPoints.get(hashableLocation);
        if (set == null) {
            set = manager.getApplicableRegions(location);
            cachedPoints.put(hashableLocation, set);
        }
        return set;
    }

    /**
     * Get an {@link ApplicableRegionSet} for a given region. This method does not yet
     * cache results.
     *
     * @param manager
     *            the manager
     * @param region
     *            the area to lookup
     * @return the applicable region set
     */
    public synchronized ApplicableRegionSet lookup(RegionIndex manager,
            ProtectedRegion region) {
        return manager.getApplicableRegions(region);
    }

}
