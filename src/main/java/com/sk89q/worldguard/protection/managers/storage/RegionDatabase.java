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

package com.sk89q.worldguard.protection.managers.storage;

import com.sk89q.worldguard.protection.managers.RegionDifference;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Set;

/**
 * A region database stores a set of regions for a single world.
 *
 * <p>If there are multiple worlds, then there should be one region database
 * per world. To manage multiple region databases, consider using an
 * implementation of a {@link RegionDriver}.</p>
 *
 * @see RegionDriver
 */
public interface RegionDatabase {

    /**
     * Get a displayable name for this store.
     */
    String getName();

    /**
     * Load all regions from storage and place them into the passed map.
     *
     * <p>The map will only be modified from one thread. The keys of
     * each map entry will be in respect to the ID of the region but
     * transformed to be lowercase. Until this method returns, the map may not
     * be modified by any other thread simultaneously. If an exception is
     * thrown, then the state in which the map is left is undefined.</p>
     *
     * <p>The provided map should have reasonably efficient
     * {@code get()} and {@code put()} calls in order to maximize performance.
     * </p>
     *
     * @return a set of loaded regions
     * @throws StorageException thrown on read error
     */
    Set<ProtectedRegion> loadAll() throws StorageException;

    /**
     * Replace all the data in the store with the given collection of regions.
     *
     * @param regions a set of regions
     * @throws StorageException thrown on write error
     */
    void saveAll(Set<ProtectedRegion> regions) throws StorageException;

    /**
     * Perform a partial save that only commits changes, rather than the
     * entire region index.
     *
     * @param difference the difference
     * @throws DifferenceSaveException thrown if partial saves are not supported
     * @throws StorageException thrown on write error
     */
    void saveChanges(RegionDifference difference) throws DifferenceSaveException, StorageException;

}
