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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PRTreeRegionManager extends RegionManager {

    private static final int BRANCH_FACTOR = 30;

    private MBRConverter<ProtectedRegion> converter = new ProtectedRegionMBRConverter();
    private RegionsContainer data = new RegionsContainer();

    /**
     * Construct the manager.
     *
     * @param regionLoader The region loader to use
     */
    public PRTreeRegionManager(ProtectionDatabase regionLoader) {
        super(regionLoader);
    }

    @Override
    public Map<String, ProtectedRegion> getRegions() {
        return data.regions;
    }

    @Override
    public void setRegions(Map<String, ProtectedRegion> regions) {
        ConcurrentMap<String, ProtectedRegion> newRegions = new ConcurrentHashMap<String, ProtectedRegion>(regions);
        PRTree<ProtectedRegion> tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(newRegions.values());
        this.data = new RegionsContainer(newRegions, tree);
    }

    @Override
    public void addRegion(ProtectedRegion region) {
        RegionsContainer data = this.data;
        data.regions.put(region.getId().toLowerCase(), region);
        PRTree<ProtectedRegion> tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(data.regions.values());
        this.data = new RegionsContainer(data.regions, tree);
    }

    @Override
    public boolean hasRegion(String id) {
        return data.regions.containsKey(id.toLowerCase());
    }

    @Override
    public void removeRegion(String id) {
        RegionsContainer data = this.data;

        ProtectedRegion region = data.regions.get(id.toLowerCase());

        data.regions.remove(id.toLowerCase());

        if (region != null) {
            List<String> removeRegions = new ArrayList<String>();
            for (ProtectedRegion curRegion : data.regions.values()) {
                if (curRegion.getParent() == region) {
                    removeRegions.add(curRegion.getId().toLowerCase());
                }
            }

            for (String remId : removeRegions) {
                removeRegion(remId);
            }
        }

        PRTree<ProtectedRegion> tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        tree.load(data.regions.values());
        this.data = new RegionsContainer(data.regions, tree);
    }

    @Override
    public ApplicableRegionSet getApplicableRegions(Vector pt) {
        RegionsContainer data = this.data;

        // Floor the vector to ensure we get accurate points
        pt = pt.floor();

        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
        MBR pointMBR = new SimpleMBR(pt.getX(), pt.getX(), pt.getY(), pt.getY(), pt.getZ(), pt.getZ());

        for (ProtectedRegion region : data.tree.find(pointMBR)) {
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

        return new ApplicableRegionSet(appRegions, data.regions.get("__global__"));
    }

    @Override
    public ApplicableRegionSet getApplicableRegions(ProtectedRegion checkRegion) {
        RegionsContainer data = this.data;

        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();
        appRegions.addAll(data.regions.values());

        List<ProtectedRegion> intersectRegions;
        try {
            intersectRegions = checkRegion.getIntersectingRegions(appRegions);
        } catch (Exception e) {
            intersectRegions = new ArrayList<ProtectedRegion>();
        }

        return new ApplicableRegionSet(intersectRegions, data.regions.get("__global__"));
    }

    @Override
    public List<String> getApplicableRegionsIDs(Vector pt) {
        RegionsContainer data = this.data;

        // Floor the vector to ensure we get accurate points
        pt = pt.floor();

        List<String> applicable = new ArrayList<String>();
        MBR pointMBR = new SimpleMBR(pt.getX(), pt.getX(), pt.getY(), pt.getY(), pt.getZ(), pt.getZ());

        for (ProtectedRegion region : data.tree.find(pointMBR)) {
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
        RegionsContainer data = this.data;

        List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion other : data.regions.values()) {
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

        return !intersectRegions.isEmpty();
    }

    @Override
    public int size() {
        return data.regions.size();
    }

    @Override
    public int getRegionCountOfPlayer(LocalPlayer player) {
        int count = 0;

        for (Map.Entry<String, ProtectedRegion> entry : data.regions.entrySet()) {
            if (entry.getValue().getOwners().contains(player)) {
                count++;
            }
        }

        return count;
    }

    private class RegionsContainer {
        private final ConcurrentMap<String, ProtectedRegion> regions;
        private final PRTree<ProtectedRegion> tree;

        private RegionsContainer() {
            regions = new ConcurrentHashMap<String, ProtectedRegion>();
            tree = new PRTree<ProtectedRegion>(converter, BRANCH_FACTOR);
        }

        private RegionsContainer(ConcurrentMap<String, ProtectedRegion> regions, PRTree<ProtectedRegion> tree) {
            this.regions = regions;
            this.tree = tree;
        }
    }

}