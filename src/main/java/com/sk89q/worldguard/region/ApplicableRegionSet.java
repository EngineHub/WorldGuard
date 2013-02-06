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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.sk89q.worldguard.region.indices.RegionIndex;

/**
 * Container for the results of region queries issued to {@link RegionIndex}es.
 * <p>
 * The list of regions in this object are sorted according to
 * {@link Region#compareTo(Region)}, so the highest priority regions will
 * be emitted by the iterator first.
 */
public class ApplicableRegionSet implements Iterable<Region>, Collection<Region> {

    private List<Region> regions;

    /**
     * Construct the object.
     * <p>
     * The given list does not have to be sorted as this set will do the
     * sorting in-place. Once the region set is constructed, the list given
     * to it must not be modified outside the instance of this class.
     *
     * @param regions the regions to be contained by this set
     */
    public ApplicableRegionSet(List<Region> regions) {
        Validate.notNull(regions);
        
        Collections.sort(regions);
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

    @Override
    public Iterator<Region> iterator() {
        return regions.iterator();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return regions.contains(o);
    }

    @Override
    public Object[] toArray() {
        return regions.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return regions.toArray(a);
    }

    @Override
    public boolean add(Region e) {
        throw new UnsupportedOperationException("Cannot modify ApplicableRegionSets");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Cannot modify ApplicableRegionSets");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return regions.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Region> c) {
        throw new UnsupportedOperationException("Cannot modify ApplicableRegionSets");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot modify ApplicableRegionSets");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot modify ApplicableRegionSets");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot modify ApplicableRegionSets");
    }
}
