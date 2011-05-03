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

package com.sk89q.worldguard.protection.databases;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.IOException;
import java.util.Map;

/**
 * Represents a database to read and write lists of regions from and to.
 * 
 * @author sk89q
 */
public interface ProtectionDatabase {
    /**
     * Load the list of regions. The method should not modify the list returned
     * by getRegions() unless the load finishes successfully.
     * 
     * @throws IOException
     */
    public void load() throws IOException;
    /**
     * Save the list of regions.
     * 
     * @throws IOException
     */
    public void save() throws IOException;
    /**
     * Load the list of regions into a region manager.
     * 
     * @param manager 
     * @throws IOException
     */
    public void load(RegionManager manager) throws IOException;
    /**
     * Save the list of regions from a region manager.
     * 
     * @param manager 
     * @throws IOException
     */
    public void save(RegionManager manager) throws IOException;
    /**
     * Get a list of regions.
     * 
     * @return
     */
    public Map<String,ProtectedRegion> getRegions();
    /**
     * Set the list of regions.
     * 
     * @param regions
     */
    public void setRegions(Map<String,ProtectedRegion> regions);
}
