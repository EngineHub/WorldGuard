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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegionMBRConverter;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An implementation of an index that uses {@link HashMapIndex} for queries
 * by region name and a priority R-tree for spatial queries.
 *
 * <p>At the moment, the R-tree is only utilized for the
 * {@link #applyContaining(BlockVector3, Predicate)} method, and the underlying
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
        tree = new PRTree<>(CONVERTER, BRANCH_FACTOR);
        tree.load(Collections.emptyList());
    }

    @Override
    protected void rebuildIndex() {
        PRTree<ProtectedRegion> newTree = new PRTree<>(CONVERTER, BRANCH_FACTOR);
        newTree.load(values());
        this.tree = newTree;
    }

    @Override
    public void applyContaining(BlockVector3 position, Predicate<ProtectedRegion> consumer) {
        Set<ProtectedRegion> seen = new HashSet<>();
        MBR pointMBR = new SimpleMBR(position.x(), position.x(), position.y(), position.y(), position.z(), position.z());

        for (ProtectedRegion region : tree.find(pointMBR)) {
            if (region.contains(position) && !seen.contains(region)) {
                seen.add(region);
                if (!consumer.test(region)) {
                    break;
                }
            }
        }
    }

    @Override
    public void applyIntersecting(ProtectedRegion region, Predicate<ProtectedRegion> consumer) {
        BlockVector3 min = region.getMinimumPoint().floor();
        BlockVector3 max = region.getMaximumPoint().ceil();

        Set<ProtectedRegion> candidates = new HashSet<>();
        MBR pointMBR = new SimpleMBR(min.x(), max.x(), min.y(), max.y(), min.z(), max.z());

        for (ProtectedRegion found : tree.find(pointMBR)) {
            candidates.add(found);
        }

        for (ProtectedRegion found : region.getIntersectingRegions(candidates)) {
            if (!consumer.test(found)) {
                break;
            }
        }
    }

    /**
     * A factory for new instances using this index.
     */
    public static final class Factory implements Function<String, PriorityRTreeIndex> {
        @Override
        public PriorityRTreeIndex apply(String name) {
            return new PriorityRTreeIndex();
        }
    }

}