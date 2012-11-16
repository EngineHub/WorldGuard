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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.region.ApplicableRegionSet;
import com.sk89q.worldguard.region.shapes.Cuboid;
import com.sk89q.worldguard.region.shapes.GlobalProtectedRegion;
import com.sk89q.worldguard.region.shapes.Region;

import java.util.TreeSet;

public class MockApplicableRegionSet {

    private TreeSet<Region> regions = new TreeSet<Region>();
    private Region global;
    private int id = 0;
    private int playerIndex = 0;

    public void add(Region region) {
        regions.add(region);
    }

    public LocalPlayer createPlayer() {
        playerIndex++;
        LocalPlayer player = new TestPlayer("#PLAYER_" + playerIndex);
        return player;
    }

    public Region global() {
        global = new GlobalProtectedRegion("__global__");
        return global;
    }

    public Region add(int priority) {
        Region region = new Cuboid(getNextId(),
                new BlockVector(0, 0, 0), new BlockVector(1, 1, 1));
        region.setPriority(priority);
        add(region);
        return region;
    }

    public Region add(int priority, Region parent)
            throws Region.CircularInheritanceException {
        Region region = new Cuboid(getNextId(),
                new BlockVector(0, 0, 0), new BlockVector(1, 1, 1));
        region.setPriority(priority);
        region.setParent(parent);
        add(region);
        return region;
    }

    public ApplicableRegionSet getApplicableSet() {
        return new ApplicableRegionSet(regions, global);
    }

    private String getNextId() {
        id++;
        return "#REGION_" + id;
    }

}
