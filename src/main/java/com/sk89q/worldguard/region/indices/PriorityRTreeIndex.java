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
import com.sk89q.worldguard.region.shapes.ShapeMBRConverter;

import org.apache.commons.lang.Validate;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.*;

/**
 * Indexes regions into a priority R-tree.
 */
public class PriorityRTreeIndex extends FlatIndex {

    private int branchFactor;
    private MBRConverter<Region> converter;
    private PRTree<Region> tree;

    /**
     * Construct a new priority R-tree index with a given branch factor.
     *
     * @param branchFactor branch factor
     */
    public PriorityRTreeIndex(int branchFactor) {
        this.branchFactor = branchFactor;
        converter = new ShapeMBRConverter();
        tree = new PRTree<Region>(converter, branchFactor);
    }

    @Override
    public synchronized Collection<Region> queryContains(
            Vector location, boolean preferOnlyCached) {
        Validate.notNull(location, "The location parameter cannot be null");

        // Floor the vector to ensure we get accurate points
        location = location.floor();

        List<Region> result = new ArrayList<Region>();
        MBR pointMBR = new SimpleMBR(location.getX(), location.getX(), location.getY(),
                location.getY(), location.getZ(), location.getZ());

        for (Region region : tree.find(pointMBR)) {
            if (region.getShape().contains(location) && !result.contains(region)) {
                result.add(region);
            }
        }

        return result;
    }

    @Override
    public void reindex() {
        tree = new PRTree<Region>(converter, branchFactor);
        tree.load(getRegions().values());
    }

    @Override
    public void clear() {
        super.clear();
        tree = new PRTree<Region>(converter, branchFactor);
    }

}