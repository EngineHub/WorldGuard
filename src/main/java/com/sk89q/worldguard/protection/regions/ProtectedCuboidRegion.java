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
import com.sk89q.worldguard.util.MathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a cuboid region that can be protected.
 *
 * @author sk89q
 */
public class ProtectedCuboidRegion extends ProtectedRegion {

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param id the region id
     * @param pt1 the first point of this region
     * @param pt2 the second point of this region
     */
    public ProtectedCuboidRegion(String id, BlockVector pt1, BlockVector pt2) {
        super(id);
        setMinMaxPoints(pt1, pt2);
    }

    /**
     * Given any two points, sets the minimum and maximum points.
     *
     * @param position1 the first point of this region
     * @param position2 the second point of this region
     */
    private void setMinMaxPoints(BlockVector position1, BlockVector position2) {
        checkNotNull(position1);
        checkNotNull(position2);

        List<Vector> points = new ArrayList<Vector>();
        points.add(position1);
        points.add(position2);
        setMinMaxPoints(points);
    }

    /**
     * Set the lower point of the cuboid.
     *
     * @param position the point to set as the minimum point
     */
    public void setMinimumPoint(BlockVector position) {
        setMinMaxPoints(position, max);
    }

    /**
     * Set the upper point of the cuboid.
     *
     * @param position the point to set as the maximum point
     */
    public void setMaximumPoint(BlockVector position) {
        setMinMaxPoints(min, position);
    }

    @Override
    public List<BlockVector2D> getPoints() {
        List<BlockVector2D> pts = new ArrayList<BlockVector2D>();
        int x1 = min.getBlockX();
        int x2 = max.getBlockX();
        int z1 = min.getBlockZ();
        int z2 = max.getBlockZ();

        pts.add(new BlockVector2D(x1, z1));
        pts.add(new BlockVector2D(x2, z1));
        pts.add(new BlockVector2D(x2, z2));
        pts.add(new BlockVector2D(x1, z2));

        return pts;
    }

    @Override
    public boolean contains(Vector pt) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();
        return x >= min.getBlockX() && x < max.getBlockX()+1
                && y >= min.getBlockY() && y < max.getBlockY()+1
                && z >= min.getBlockZ() && z < max.getBlockZ()+1;
    }

    @Override
    public RegionType getType() {
        return RegionType.CUBOID;
    }

    @Override
    public List<ProtectedRegion> getIntersectingRegions(Collection<ProtectedRegion> regions) {
        checkNotNull(regions);

        List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion region : regions) {
            if (!intersectsBoundingBox(region)) continue;

            // If both regions are Cuboids and their bounding boxes intersect, they intersect
            if (region instanceof ProtectedCuboidRegion) {
                intersectingRegions.add(region);
            } else if (region instanceof ProtectedPolygonalRegion) {
                // If either region contains the points of the other,
                // or if any edges intersect, the regions intersect
                if (containsAny(region.getPoints()) || region.containsAny(getPoints()) || intersectsEdges(region)) {
                    intersectingRegions.add(region);
                }
            } else if (region instanceof GlobalProtectedRegion) {
                // Never intersects
            } else {
                throw new IllegalArgumentException("Not supported yet.");
            }
        }
        return intersectingRegions;
    }

    @Override
    public int volume() {
        int xLength = max.getBlockX() - min.getBlockX() + 1;
        int yLength = max.getBlockY() - min.getBlockY() + 1;
        int zLength = max.getBlockZ() - min.getBlockZ() + 1;

        try {
            long v = MathUtils.checkedMultiply(xLength, yLength);
            v = MathUtils.checkedMultiply(v, zLength);
            if (v > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else {
                return (int) v;
            }
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE;
        }
    }

}