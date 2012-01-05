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
package com.sk89q.worldguard.protection.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabase;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegionMBRConverter;

public class PRTreeRegionManager extends RegionManager {

    private static final int BRANCH_FACTOR = 30;
    /**
     * List of protected regions.
     */
    private Map<String, ProtectedRegion> regions;
    /**
     * Converter to get coordinates of the tree.
     */
    private MBRConverter<ProtectedRegion> converter = new ProtectedRegionMBRConverter();
    /**
     * Priority R-tree.
     */
    private PRTree<ProtectedRegion> tree;

    /**
     * Construct the manager.
     *
     * @param regionloader
     * @throws IOException
     */
    public PRTreeRegionManager(ProtectionDatabase regionloader) throws IOException {
        super(regionloader);
        regions = new TreeMap<String, ProtectedRegion>();
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        this.load();
    }

    /**
     * Get a list of protected regions.
     *
     * @return
     */
    @Override
    public Map<String, ProtectedRegion> getRegions() {
        return regions;
    }

    /**
     * Set a list of protected regions.
     */
    @Override
    public void setRegions(Map<String, ProtectedRegion> regions) {
        this.regions = new TreeMap<String, ProtectedRegion>(regions);
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    /**
     * Adds a region.
     *
     * @param region
     */
    @Override
    public void addRegion(ProtectedRegion region) {
        regions.put(region.getId().toLowerCase(), region);
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    /**
     * Return whether a region exists by an ID.
     *
     * @param id
     * @return
     */
    @Override
    public boolean hasRegion(String id) {
        return regions.containsKey(id.toLowerCase());
    }

    /**
     * Get a region by its ID.
     *
     * @param id
     */
    @Override
    public ProtectedRegion getRegion(String id) {
        if (regions.containsKey(id.toLowerCase()))
            return getRegion(id.toLowerCase());
        
        for (RegionManager rm : subManagers) {
            if (rm.getRegions().containsKey(id.toLowerCase())) {
                return rm.getRegion(id.toLowerCase());
            }
        }
        return null;
    }

    /**
     * Removes a region and its children.
     *
     * @param id
     */
    @Override
    public void removeRegion(String id) {
        ProtectedRegion region = regions.get(id.toLowerCase());
        // if the region is not contained in this RegionManager then attempt to remove 
        // the region from any SubRegionManager
        if (region == null) {
            for (RegionManager rm : subManagers) {
                rm.removeRegion(id);
            }
            return;
        }
        regions.remove(id.toLowerCase());

        if (region != null) {
            List<String> removeRegions = new ArrayList<String>();
            Iterator<ProtectedRegion> iter = regions.values().iterator();
            while (iter.hasNext()) {
                ProtectedRegion curRegion = iter.next();
                if (curRegion.getParent() == region) {
                    removeRegions.add(curRegion.getId().toLowerCase());
                }
            }

            for (String remId : removeRegions) {
                removeRegion(remId);
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
    @Override
    public ApplicableRegionSet getApplicableRegions(Vector pt) {
        List<ProtectedRegion> appRegions =
                new ArrayList<ProtectedRegion>();

        int x = pt.getBlockX();
        int z = pt.getBlockZ();

        for (ProtectedRegion region : tree.find(x, z, x, z)) {
            if (region.contains(pt)) {
                appRegions.add(region);
            }
        }

        // Get all applicable regions from the sub-region managers
        for (RegionManager rm : subManagers) {
            appRegions.addAll(rm.getApplicableRegions(pt).getApplicable());
        }
        
        Collections.sort(appRegions);

        return new ApplicableRegionSet(appRegions, regions.get("__global__"));
    }

    @Override
    public ApplicableRegionSet getApplicableRegions(ProtectedRegion checkRegion) {
        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
        appRegions.addAll(regions.values());

        List<ProtectedRegion> intersectRegions;
        try {
            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
        } catch (Exception e) {
            intersectRegions = new ArrayList<ProtectedRegion>();
        }
        
        // Get all applicable regions from the sub-region managers
        for (RegionManager rm : subManagers) {
            intersectRegions.addAll(rm.getApplicableRegions(checkRegion).getApplicable());
        }
        
        return new ApplicableRegionSet(intersectRegions, regions.get("__global__"));
    }

    /**
     * Get a list of region IDs that contain a point.
     *
     * @param pt
     * @return
     */
    @Override
    public List<String> getApplicableRegionsIDs(Vector pt) {
        List<String> applicable = new ArrayList<String>();

        int x = pt.getBlockX();
        int z = pt.getBlockZ();

        for (ProtectedRegion region : tree.find(x, z, x, z)) {
            if (region.contains(pt)) {
                applicable.add(region.getId());
            }
        }
        
        // Get all IDs of applicable regions in SubManagers
        for (RegionManager rm : subManagers) {
            applicable.addAll(rm.getApplicableRegionsIDs(pt));
        }
        
        return applicable;
    }

    /**
     * Returns true if the provided region overlaps with any other region that
     * is not owned by the player.
     *
     * @param player
     * @return
     */
    @Override
    public boolean overlapsUnownedRegion(ProtectedRegion checkRegion, LocalPlayer player) {
        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion other : regions.values()) {
            if (other.getOwners().contains(player)) {
                continue;
            }

            appRegions.add(other);
        }
        
        List<ProtectedRegion> intersectRegions;
        try {
            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
        } catch (Exception e) {
            intersectRegions = new ArrayList<ProtectedRegion>();
        }
        
        if (intersectRegions.size() > 0)  return true;
        
        // Check if the Region overlaps one of the regions in a subRegionManager
        for (RegionManager rm : subManagers) {
            if (rm.overlapsUnownedRegion(checkRegion, player)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Get the number of regions.
     *
     * @return
     */
    @Override
    public int size() {
        return regions.size();
    }

    @Override
    public int getRegionCountOfPlayer(LocalPlayer player) {
        int count = 0;

        for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
            if (entry.getValue().getOwners().contains(player)) {
                count++;
            }
        }

        return count;
    }
}
