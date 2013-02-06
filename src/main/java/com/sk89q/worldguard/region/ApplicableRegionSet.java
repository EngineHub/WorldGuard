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

import java.util.Collection;
import java.util.Iterator;

import com.sk89q.worldguard.region.indices.RegionIndex;

/**
 * Container for the results of region queries issued to {@link RegionIndex}es.
 */
public class ApplicableRegionSet implements Iterable<Region> {

    private Collection<Region> regions;

    /**
     * Construct the object.
     *
     * @param regions the regions contained in this set
     */
    public ApplicableRegionSet(Collection<Region> regions) {
        this.regions = regions;
    }

    /**
     * Get the number of regions that are included.
     *
     * @return the size of this set
     */
    public int size() {
        return regions.size();
    }

    /**
     * Get an iterator of affected regions.
     */
    @Override
    public Iterator<Region> iterator() {
        return regions.iterator();
    }
}
