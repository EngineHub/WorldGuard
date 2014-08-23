/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.protection.managers.index;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegionMBRConverter;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of an index that uses {@link HashMapIndex} for queries
 * by region name and a priority R-tree for spatial queries.
 *
 * <p>At the moment, the R-tree is only utilized for the
 * {@link #applyContaining(Vector, Predicate)} method, and the underlying
 * hash map-based index is used for the other spatial queries. In addition,
 * every modification to the index requires the entire R-tree to be rebuilt,
 * although this operation is reasonably quick.</p>
 *
 * <p>This implementation is as thread-safe as the underlying
 * {@link HashMapIndex}, although spatial queries may lag behind changes
 * for very brief periods of time as the tree is rebuilt.</p>
 */
public class PriorityRTreeIndex extends HashMapIndex {

    private static final int BRANCH_FACTOR = 30;
    private static final MBRConverter<ProtectedRegion> CONVERTER = new ProtectedRegionMBRConverter();

    private PRTree<ProtectedRegion> tree;

    public PriorityRTreeIndex() {
        tree = new PRTree<ProtectedRegion>(CONVERTER, BRANCH_FACTOR);
        tree.load(Collections.<ProtectedRegion>emptyList());
    }

    @Override
    protected void rebuildIndex() {
        PRTree<ProtectedRegion> newTree = new PRTree<ProtectedRegion>(CONVERTER, BRANCH_FACTOR);
        newTree.load(values());
        this.tree = newTree;
    }

    @Override
    public void applyContaining(Vector position, Predicate<ProtectedRegion> consumer) {
        // Floor the vector to ensure we get accurate points
        position = position.floor();

        Set<ProtectedRegion> seen = new HashSet<ProtectedRegion>();
        MBR pointMBR = new SimpleMBR(position.getX(), position.getX(), position.getY(), position.getY(), position.getZ(), position.getZ());

        for (ProtectedRegion region : tree.find(pointMBR)) {
            if (region.contains(position) && !seen.contains(region)) {
                seen.add(region);
                if (!consumer.apply(region)) {
                    break;
                }
            }
        }
    }

    @Override
    public void applyIntersecting(ProtectedRegion region, Predicate<ProtectedRegion> consumer) {
        Vector min = region.getMinimumPoint().floor();
        Vector max = region.getMaximumPoint().ceil();

        Set<ProtectedRegion> candidates = new HashSet<ProtectedRegion>();
        MBR pointMBR = new SimpleMBR(min.getX(), max.getX(), min.getY(), max.getY(), min.getZ(), max.getZ());

        for (ProtectedRegion found : tree.find(pointMBR)) {
            candidates.add(found);
        }

        for (ProtectedRegion found : region.getIntersectingRegions(candidates)) {
            if (!consumer.apply(found)) {
                break;
            }
        }
    }

    /**
     * A factory for new instances using this index.
     */
    public static final class Factory implements Supplier<PriorityRTreeIndex> {
        @Override
        public PriorityRTreeIndex get() {
            return new PriorityRTreeIndex();
        }
    }

}