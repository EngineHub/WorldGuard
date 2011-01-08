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

package com.sk89q.worldguard.protection;

import java.util.Map;
import java.util.LinkedHashMap;
import com.sk89q.worldedit.Vector;

/**
 * A very simple implementation of the region manager that uses a flat list
 * and iterates through the list to identify applicable regions. This method
 * is not very efficient.
 * 
 * @author sk89q
 */
public class FlatRegionManager implements RegionManager {
    /**
     * List of protected regions.
     */
    private Map<String,ProtectedRegion> regions;
    
    /**
     * Construct the manager.
     */
    public FlatRegionManager() {
        regions = new LinkedHashMap<String,ProtectedRegion>();
    }
    
    /**
     * Get a list of protected regions.
     *
     * @return
     */
    public Map<String,ProtectedRegion> getRegions() {
        return regions;
    }
    
    /**
     * Adds a region.
     * 
     * @param id
     * @param region
     */
    public void addRegion(String id, ProtectedRegion region) {
        regions.put(id, region);
    }
    
    /**
     * Removes a region.
     * 
     * @param id
     */
    public void removeRegion(String id) {
        regions.remove(id);
    }
    
    /**
     * Set a list of protected regions.
     *
     * @return
     */
    public void setRegions(Map<String,ProtectedRegion> regions) {
        this.regions = regions;
    }
    
    /**
     * Get a LinkedList of regions that contain a point.
     * 
     * @param pt
     * @return
     */
    public ApplicableRegionSet getApplicableRegions(Vector pt) {
        return new ApplicableRegionSet(pt, regions);
    }
    
    /**
     * Get the number of regions.
     * 
     * @return
     */
    public int size() {
        return regions.size();
    }
}
