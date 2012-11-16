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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.region.Region;
import com.sk89q.worldguard.region.UnsupportedIntersectionException;

import java.util.*;

import org.apache.commons.lang.Validate;

/**
 * A very simple implementation of the region manager that uses a flat list and iterates
 * through the list to identify applicable regions. This method is not very efficient.
 * <p>
 * The overlapping queries are especially slow.
 */
public class FlatIndex extends AbstractRegionIndex {

    private Map<String, Region> regions = new TreeMap<String, Region>();

    @Override
    public synchronized void add(Region... region) {
        Validate.notNull(region, "The region parameter cannot be null");
        for (Region r : region) {
            regions.put(normalizeId(r.getId()), r);
        }
    }

    @Override
    public synchronized void remove(String... id) {
        Validate.notNull(id, "The id parameter cannot be null");
        for (String i : id) {
            regions.remove(normalizeId(i));
        }
    }

    @Override
    public synchronized Region get(String id) {
        Validate.notNull(id, "The id parameter cannot be null");
        return regions.get(id);
    }

    @Override
    public synchronized boolean contains(String id) {
        Validate.notNull(id, "The id parameter cannot be null");
        return regions.containsKey(normalizeId(id));
    }

    @Override
    public synchronized Collection<Region> queryContains(
            Vector location, boolean preferOnlyCached) {
        Validate.notNull(location, "The location parameter cannot be null");

        List<Region> result = new ArrayList<Region>();

        for (Region region : regions.values()) {
            if (region.contains(location)) {
                result.add(region);
            }
        }

        return result;
    }

    @Override
    public synchronized Collection<Region> queryOverlapping(
            Region region, boolean preferOnlyCached) {
        Validate.notNull(region, "The location parameter cannot be null");

        List<Region> testRegions = new ArrayList<Region>();
        testRegions.addAll(regions.values());
        List<Region> result;

        try {
            result = region.getIntersectingRegions(testRegions);
        } catch (UnsupportedIntersectionException e) { // This is bad!
            result = new ArrayList<Region>();
        }

        return result;
    }

    @Override
    public synchronized int size() {
        return regions.size();
    }

    @Override
    public void reindex() {
        // Whoo, nothing to do, because this index is so simple
    }

    @Override
    public Iterator<Region> iterator() {
        return regions.values().iterator();
    }
}
