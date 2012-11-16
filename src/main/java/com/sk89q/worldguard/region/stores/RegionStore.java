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

package com.sk89q.worldguard.region.stores;

import java.io.IOException;
import java.util.Collection;

import com.sk89q.worldguard.region.Region;
import com.sk89q.worldguard.region.indices.RegionIndex;
import com.sk89q.worldguard.region.indices.RegionIndexFactory;

/**
 * Persists region data by writing it to some persistent storage device, such as to
 * a hard drive or a database.
 * <p>
 * Implementations must be thread-safe.
 *
 * @see RegionStoreFactory
 */
public interface RegionStore {

    /**
     * Load data from the store and return a new {@link RegionIndex}.
     *
     * @param factory a factory to create region indices with
     * @return a region index with the region data loader
     * @throws IOException on I/O error
     */
    RegionIndex load(RegionIndexFactory factory) throws IOException;

    /**
     * Save the entirety of a region collection to the store.
     * <p>
     * It must be assumed that the given list of regions is the complete list of all
     * regions. With the case of partial stores, this also means that the given list
     * is to be assumed as non-partial. It is therefore recommended that use of
     * this method is limited to only special cases, such as creation of brand new
     * region indexes.
     *
     * @param regions the collection of regions to store in its entirety
     * @throws IOException on I/O error
     */
    void save(Collection<Region> regions) throws IOException;

}
