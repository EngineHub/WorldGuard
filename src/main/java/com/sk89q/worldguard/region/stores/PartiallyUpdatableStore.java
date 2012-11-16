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

/**
 * A store that can accept a subset of regions to update.
 * <p>
 * This store is suitable for stores that don't need to have the entire copy of the
 * region database at a time to perform a load or save operation.
 */
public interface PartiallyUpdatableStore extends RegionStore {

    /**
     * Save only selected regions to the region store.
     *
     * @param added a list of regions that were added
     * @param updated a list of regions that were updated
     * @param removed a list of regions that were removed
     * @throws IOException on I/O error
     */
    void save(Collection<Region> added, Collection<Region> updated,
            Collection<Region> removed) throws IOException;

}
