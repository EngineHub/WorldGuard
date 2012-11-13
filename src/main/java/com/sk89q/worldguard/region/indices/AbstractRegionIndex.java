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
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region.indices;

import java.util.Collection;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.region.regions.ProtectedRegion;

/**
 * An abstract implementation of {@link RegionIndex} to make it easier to implement
 * region indices.
 */
public abstract class AbstractRegionIndex implements RegionIndex {

    /**
     * Change the casing of IDs to lowercase so that they act as case
     * insensitive in indexes.
     *
     * @param id the original ID
     * @return the new lowercase ID
     */
    protected final String normalizeId(String id) {
        return id.toLowerCase();
    }

    @Override
    public Collection<ProtectedRegion> queryContains(Vector location) {
        return queryContains(location, false);
    }

    @Override
    public Collection<ProtectedRegion> queryOverlapping(ProtectedRegion region) {
        return queryOverlapping(region, false);
    }

    @Override
    public void removeMatching(ProtectedRegion... region) {
        String[] ids = new String[region.length];
        for (int i = 0; i < region.length; i++) {
            ids[i] = region[i].getId();
        }
        remove(ids);
    }

    @Override
    public ProtectedRegion getMatching(ProtectedRegion region) {
        return get(region.getId());
    }

    @Override
    public boolean containsMatching(ProtectedRegion region) {
        return contains(region.getId());
    }

    @Override
    public boolean containsExact(ProtectedRegion region) {
        return get(region.getId()) == region;
    }

}
