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

package com.sk89q.worldguard.protection.regionmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalFlags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegionMBRConverter;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.dbs.ProtectionDatabase;
import java.io.IOException;
import java.util.HashMap;

public class PRTreeRegionManager extends RegionManager {
    private static final int BRANCH_FACTOR = 30;

    /**
     * List of protected regions.
     */
    private Map<String,ProtectedRegion> regions;
    
    /**
     * Converter to get coordinates of the tree.
     */
    private MBRConverter<ProtectedRegion> converter
            = new ProtectedRegionMBRConverter();
    
    /**
     * Priority R-tree.
     */
    private PRTree<ProtectedRegion> tree;

    /**
     * Construct the manager.
     */
    public PRTreeRegionManager(GlobalFlags global, ProtectionDatabase regionloader) throws IOException {
        super(global, regionloader);
        regions = new TreeMap<String,ProtectedRegion>();
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        this.load();
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
     * Set a list of protected regions.
     *
     * @return
     */
    public void setRegions(Map<String,ProtectedRegion> regions) {
        this.regions = new TreeMap<String,ProtectedRegion>(regions);
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    /**
     * Adds a region.
     * 
     * @param id
     * @param region
     */
    public void addRegion(ProtectedRegion region) {
        regions.put(region.getId(), region);
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    /**
     * Return whether a region exists by an ID.
     * 
     * @param id
     * @return
     */
    public boolean hasRegion(String id) {
        return regions.containsKey(id);
    }

    /**
     * Get a region by its ID.
     * 
     * @param id
     */
    public ProtectedRegion getRegion(String id) {
        return regions.get(id);
    }
    
    /**
     * Removes a region and its children.
     * 
     * @param id
     */
    public void removeRegion(String id) {
        ProtectedRegion region = regions.get(id);
        
        regions.remove(id);
        
        if (region != null) {
            for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
                if (entry.getValue().getParent() == region) {
                    removeRegion(entry.getKey());
                }
            }
        }

        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    /**
     * Get an object for a point for rules to be applied with.
     * 
     * @param pt
     * @return
     */
    public ApplicableRegionSet getApplicableRegions(Vector pt) {

        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();

        int x = pt.getBlockX();
        int z = pt.getBlockZ();

        for (ProtectedRegion region : tree.find(x, z, x, z)) {
            if (region.contains(pt)) {
                appRegions.add(region);
            }
        }

        return new ApplicableRegionSet(pt, appRegions, global);
    }

    /**
     * Get a list of region IDs that contain a point.
     * 
     * @param pt
     * @return
     */
    public List<String> getApplicableRegionsIDs(Vector pt) {
        List<String> applicable = new ArrayList<String>();

        int x = pt.getBlockX();
        int z = pt.getBlockZ();
        
        for (ProtectedRegion region : tree.find(x, z, x, z)) {
            if (region.contains(pt)) {
                applicable.add(region.getId());
            }
        }
        
        return applicable;
    }
    
    /**
     * Returns true if the provided region overlaps with any other region that
     * is not owned by the player.
     * 
     * @param region
     * @param player
     * @return
     */
    public boolean overlapsUnownedRegion(ProtectedRegion region, LocalPlayer player) {
        for (ProtectedRegion other : regions.values()) {
            if (other.getOwners().contains(player)) {
                continue;
            }
            
            try {
                if (ProtectedRegion.intersects(region, other)) {
                    return true;
                }
            } catch (UnsupportedIntersectionException e) {
                // TODO: Maybe do something here
            }
        }
        
        return false;
    }
    
    /**
     * Get the number of regions.
     * 
     * @return
     */
    public int size() {
        return regions.size();
    }

    /**
     * Save the list of regions.
     *
     * @throws IOException
     */
    public void save() throws IOException
    {
        regionloader.save(this);
    }

}
