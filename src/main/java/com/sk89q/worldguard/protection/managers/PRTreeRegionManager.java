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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabase;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegionMBRConverter;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import java.util.*;

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
     * @param regionLoader The region loader to use
     */
    public PRTreeRegionManager(ProtectionDatabase regionLoader) {
        super(regionLoader);
        regions = new TreeMap<String, ProtectedRegion>();
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
    }

    @Override
    public Map<String, ProtectedRegion> getRegions() {
        return regions;
    }

    @Override
    public void setRegions(Map<String, ProtectedRegion> regions) {
        this.regions = new TreeMap<String, ProtectedRegion>(regions);
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    @Override
    public void addRegion(ProtectedRegion region) {
        regions.put(region.getId().toLowerCase(), region);
        tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(regions.values());
    }

    @Override
    public boolean hasRegion(String id) {
        return regions.containsKey(id.toLowerCase());
    }

    @Override
    public void removeRegion(String id) {
        ProtectedRegion region = regions.get(id.toLowerCase());

        regions.remove(id.toLowerCase());

        if (region != null) {
            List<String> removeRegions = new ArrayList<String>();
            for (ProtectedRegion curRegion : regions.values()) {
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

    @Override
    public ApplicableRegionSet getApplicableRegions(Vector pt) {

        // Floor the vector to ensure we get accurate points
        pt = pt.floor();

        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
        MBR pointMBR = new SimpleMBR(pt.getX(), pt.getX(), pt.getY(), pt.getY(), pt.getZ(), pt.getZ());

        for (ProtectedRegion region : tree.find(pointMBR)) {
            if (region.contains(pt) && !appRegions.contains(region)) {
                appRegions.add(region);

                ProtectedRegion parent = region.getParent();

                while (parent != null) {
                    if (!appRegions.contains(parent)) {
                        appRegions.add(parent);
                    }

                    parent = parent.getParent();
                }
            }
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

        return new ApplicableRegionSet(intersectRegions, regions.get("__global__"));
    }

    @Override
    public List<String> getApplicableRegionsIDs(Vector pt) {

        // Floor the vector to ensure we get accurate points
        pt = pt.floor();

        List<String> applicable = new ArrayList<String>();
        MBR pointMBR = new SimpleMBR(pt.getX(), pt.getX(), pt.getY(), pt.getY(), pt.getZ(), pt.getZ());

        for (ProtectedRegion region : tree.find(pointMBR)) {
            if (region.contains(pt) && !applicable.contains(region.getId())) {
                applicable.add(region.getId());

                ProtectedRegion parent = region.getParent();

                while (parent != null) {
                    if (!applicable.contains(parent.getId())) {
                        applicable.add(parent.getId());
                    }

                    parent = parent.getParent();
                }
            }
        }

        return applicable;
    }

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

        return intersectRegions.size() > 0;
    }

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