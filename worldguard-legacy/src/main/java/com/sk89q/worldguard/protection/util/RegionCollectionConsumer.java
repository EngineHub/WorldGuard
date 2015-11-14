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

package com.sk89q.worldguard.protection.util;

import com.google.common.base.Predicate;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A consumer predicate that adds regions to a collection.
 *
 * <p>This class can also add the parents of regions that are visited
 * to the collection, although it may result in duplicates in the collection
 * if the collection is not a set.</p>
 */
public class RegionCollectionConsumer implements Predicate<ProtectedRegion> {

    private final Collection<ProtectedRegion> collection;
    private final boolean addParents;

    /**
     * Create a new instance.
     *
     * @param collection the collection to add regions to
     * @param addParents true to also add the parents to the collection
     */
    public RegionCollectionConsumer(Collection<ProtectedRegion> collection, boolean addParents) {
        checkNotNull(collection);

        this.collection = collection;
        this.addParents = addParents;
    }

    @Override
    public boolean apply(ProtectedRegion region) {
        collection.add(region);

        if (addParents) {
            ProtectedRegion parent = region.getParent();

            while (parent != null) {
                collection.add(parent);
                parent = parent.getParent();
            }
        }

        return true;
    }

}
