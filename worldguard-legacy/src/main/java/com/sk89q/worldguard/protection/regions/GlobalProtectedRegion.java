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

package com.sk89q.worldguard.protection.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A special region that is not quite "anywhere" (its volume is 0, it
 * contains no positions, and it does not intersect with any other region).
 *
 * <p>Global regions, however, are used to specify a region with flags that
 * are applied with the lowest priority.</p>
 */
public class GlobalProtectedRegion extends ProtectedRegion {

    /**
     * Create a new instance.
     *
     * @param id the ID
     */
    public GlobalProtectedRegion(String id) {
        super(id);
        min = new BlockVector(0, 0, 0);
        max = new BlockVector(0, 0, 0);
    }

    @Override
    public boolean isPhysicalArea() {
        return false;
    }

    @Override
    public List<BlockVector2D> getPoints() {
        // This doesn't make sense
        List<BlockVector2D> pts = new ArrayList<BlockVector2D>();
        pts.add(new BlockVector2D(min.getBlockX(), min.getBlockZ()));
        return pts;
    }

    @Override
    public int volume() {
        return 0;
    }

    @Override
    public boolean contains(Vector pt) {
        // Global regions are handled separately so it must not contain any positions
        return false;
    }

    @Override
    public RegionType getType() {
        return RegionType.GLOBAL;
    }

    @Override
    public List<ProtectedRegion> getIntersectingRegions(Collection<ProtectedRegion> regions) {
        // Global regions are handled separately so it must not contain any positions
        return Collections.emptyList();
    }

    @Override
    Area toArea() {
        return null;
    }

}
