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
import com.sk89q.worldguard.region.UnsupportedIntersectionException;
import com.sk89q.worldguard.region.regions.ProtectedRegion;
import com.sk89q.worldguard.region.regions.ProtectedRegionMBRConverter;
import org.apache.commons.lang.Validate;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.*;

/**
 * Indexes regions into a priority R-tree.
 */
public class PriorityRTreeIndex extends AbstractRegionIndex {

    private int branchFactor;
    private Map<String, ProtectedRegion> regions;
    private MBRConverter<ProtectedRegion> converter;
    private PRTree<ProtectedRegion> tree;

    /**
     * Construct a new priority R-tree index with a given branch factor.
     *
     * @param branchFactor branch factor
     */
    public PriorityRTreeIndex(int branchFactor) {
        this.branchFactor = branchFactor;
        regions = new TreeMap<String, ProtectedRegion>();
        converter = new ProtectedRegionMBRConverter();
        tree = new PRTree<ProtectedRegion>(converter, branchFactor);
    }

    @Override
    public synchronized void add(ProtectedRegion... region) {
        Validate.notNull(region, "The region parameter cannot be null");
        for (ProtectedRegion r : region) {
            regions.put(normalizeId(r.getId()), r);
        }
        tree = new PRTree<ProtectedRegion>(converter, branchFactor);
        tree.load(regions.values());
    }

    @Override
    public synchronized void remove(String... id) {
        Validate.notNull(id, "The id parameter cannot be null");
        for (String i : id) {
            regions.remove(normalizeId(i));
        }
        tree = new PRTree<ProtectedRegion>(converter, branchFactor);
        tree.load(regions.values());
    }

    @Override
    public synchronized boolean contains(String id) {
        Validate.notNull(id, "The id parameter cannot be null");
        return regions.containsKey(normalizeId(id));
    }

    @Override
    public synchronized ProtectedRegion get(String id) {
        Validate.notNull(id, "The id parameter cannot be null");
        return regions.get(id);
    }

    @Override
    public synchronized Collection<ProtectedRegion> queryContains(
            Vector location, boolean preferOnlyCached) {
        Validate.notNull(location, "The location parameter cannot be null");

        // Floor the vector to ensure we get accurate points
        location = location.floor();

        List<ProtectedRegion> result = new ArrayList<ProtectedRegion>();
        MBR pointMBR = new SimpleMBR(location.getX(), location.getX(), location.getY(),
                location.getY(), location.getZ(), location.getZ());

        for (ProtectedRegion region : tree.find(pointMBR)) {
            if (region.contains(location) && !result.contains(region)) {
                result.add(region);
            }
        }

        return result;
    }

    @Override
    public synchronized Collection<ProtectedRegion> queryOverlapping(
            ProtectedRegion region, boolean preferOnlyCached) {
        Validate.notNull(region, "The location parameter cannot be null");

        List<ProtectedRegion> testRegions = new ArrayList<ProtectedRegion>();
        testRegions.addAll(regions.values());
        List<ProtectedRegion> result;

        try {
            result = region.getIntersectingRegions(testRegions);
        } catch (UnsupportedIntersectionException e) { // This is bad!
            result = new ArrayList<ProtectedRegion>();
        }

        return result;
    }

    @Override
    public synchronized int size() {
        return regions.size();
    }

}