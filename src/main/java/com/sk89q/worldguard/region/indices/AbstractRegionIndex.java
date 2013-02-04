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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.region.Region;
import com.sk89q.worldguard.region.shapes.IndexableShape;

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
    public Collection<Region> queryContains(Vector location) {
        return queryContains(location, false);
    }

    @Override
    public Collection<Region> queryIntersects(Region region) {
        return queryIntersects(region, false);
    }

    @Override
    public synchronized Collection<Region> queryContains(
            Vector location, boolean preferOnlyCached) {
        Validate.notNull(location, "The location parameter cannot be null");

        List<Region> result = new ArrayList<Region>();

        for (Region region : this) {
            if (region.getShape().contains(location)) {
                result.add(region);
            }
        }

        return result;
    }

    @Override
    public synchronized Collection<Region> queryIntersects(
            Region region, boolean preferOnlyCached) {
        Validate.notNull(region, "The region parameter cannot be null");

        IndexableShape shape = region.getShape();
        List<Region> result = new ArrayList<Region>();

        for (Region other : this) {
            if (other.getShape().intersectsEdges(shape)) {
                result.add(region);
            }
        }

        return result;
    }

    @Override
    public boolean removeMatching(Region region) {
        return remove(region.getId());
    }

    @Override
    public Region getMatching(Region region) {
        return get(region.getId());
    }

    @Override
    public boolean containsMatching(Region region) {
        return contains(region.getId());
    }

    @Override
    public boolean containsExact(Region region) {
        return get(region.getId()) == region;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Region) {
            return containsMatching((Region) o);
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Region) {
            return removeMatching((Region) o);
        } else {
            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean found = false;

        for (Object o : c) {
            if (contains(o)) {
                found = true;
            } else {
                return false;
            }
        }

        return found;
    }

    @Override
    public boolean addAll(Collection<? extends Region> c) {
        boolean changed = false;

        for (Region region : c) {
            if (add(region)) {
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;

        for (Object region : c) {
            if (remove(region)) {
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size()];
        int i = 0;
        for (Region region : this) {
            arr[i++] = region;
        }
        return arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] arr) {
        if (arr.length < size()) {
            arr = (T[]) new Object[size()];
        }
        int i = 0;
        for (Region region : this) {
            arr[i++] = (T) region;
        }
        return arr;
    }

}
