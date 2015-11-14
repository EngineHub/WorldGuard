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

package com.sk89q.worldguard.protection;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.NormativeOrders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockApplicableRegionSet {

    private List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
    private ProtectedRegion global;
    private int id = 0;
    private int playerIndex = 0;

    public void add(ProtectedRegion region) {
        regions.add(region);
    }

    public LocalPlayer createPlayer() {
        playerIndex++;
        LocalPlayer player = new TestPlayer("#PLAYER_" + playerIndex);
        return player;
    }

    public ProtectedRegion global() {
        global = new GlobalProtectedRegion("__global__");
        return global;
    }

    public ProtectedRegion createOutside(int priority) {
        ProtectedRegion region = new ProtectedCuboidRegion(getNextId(),
                new BlockVector(0, 0, 0), new BlockVector(1, 1, 1));
        region.setPriority(priority);
        return region;
    }

    public ProtectedRegion add(int priority) {
        ProtectedRegion region = new ProtectedCuboidRegion(getNextId(),
                new BlockVector(0, 0, 0), new BlockVector(1, 1, 1));
        region.setPriority(priority);
        add(region);
        return region;
    }

    public ProtectedRegion add(int priority, ProtectedRegion parent)
            throws ProtectedRegion.CircularInheritanceException {
        ProtectedRegion region = new ProtectedCuboidRegion(getNextId(),
                new BlockVector(0, 0, 0), new BlockVector(1, 1, 1));
        region.setPriority(priority);
        region.setParent(parent);
        add(region);
        return region;
    }

    public ApplicableRegionSet getApplicableSetInWilderness() {
        return new RegionResultSet(Collections.<ProtectedRegion>emptyList(), global);
    }

    public ApplicableRegionSet getApplicableSet() {
        return new RegionResultSet(regions, global);
    }

    public FlagValueCalculator getFlagCalculator() {
        NormativeOrders.sort(regions);
        return new FlagValueCalculator(regions, global);
    }

    private String getNextId() {
        id++;
        return "REGION_" + id;
    }

}
