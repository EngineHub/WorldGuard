// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region.stores;

import java.util.Map;
import java.util.concurrent.Future;

import com.sk89q.worldguard.region.indices.RegionIndex;
import com.sk89q.worldguard.region.regions.ProtectedRegion;

/**
 * Persists region data by writing it to some persistent storage device,
 * such as to a hard drive.
 */
public interface RegionStore {

    /**
     * Load the list of regions from the data store. The {@link RegionIndex}
     * should b
     *
     * @throws ProtectionDatabaseException when an error occurs
     */
    Future<RegionIndex> load(RegionIndex index) throws ProtectionDatabaseException;

    /**
     * Save the list of regions.
     *
     * @throws ProtectionDatabaseException when an error occurs
     */
    void save() throws ProtectionDatabaseException;

    /**
     * Load the list of regions into a region manager.
     *
     * @param manager The manager to load regions into
     * @throws ProtectionDatabaseException when an error occurs
     */
    public void load(RegionIndex manager) throws ProtectionDatabaseException;

    /**
     * Save the list of regions from a region manager.
     *
     * @param manager The manager to load regions into
     * @throws ProtectionDatabaseException when an error occurs
     */
    public void save(RegionIndex manager) throws ProtectionDatabaseException;

    /**
     * Get a list of regions.
     *
     * @return the regions loaded by this ProtectionDatabase
     */
    public Map<String,ProtectedRegion> getRegions();

    /**
     * Set the list of regions.
     *
     * @param regions The regions to be applied to this ProtectionDatabase
     */
    public void setRegions(Map<String,ProtectedRegion> regions);

}
