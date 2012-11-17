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

import java.io.Closeable;

/**
 * Used to create instances of {@link RegionStore} to access collections of regions
 * grouped by a given ID.
 * <p>
 * When unloading, the {@link #close()} method needs to be called to ensure that all
 * handles and what not are taken care of.
 *
 * @see RegionStore
 */
public interface RegionStoreFactory extends Closeable {

    /**
     * Returns a store for the collection of regions identified by the given ID.
     * <p>
     * The store returned should be cached and a new store should not be created
     * until necessary.
     *
     * @param id the ID
     * @return a store
     */
    RegionStore getStore(String id);

}
