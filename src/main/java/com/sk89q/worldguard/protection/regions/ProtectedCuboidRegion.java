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
package com.sk89q.worldguard.protection.regions;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;

/**
 * Represents a cuboid region that can be protected.
 *
 * @author sk89q
 */
public class ProtectedCuboidRegion extends ProtectedRegion {

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param id The region id
     * @param pt1 The first point of this region
     * @param pt2 The second point of this region
     */
    public ProtectedCuboidRegion(String id, BlockVector pt1, BlockVector pt2) {
        super(id);
        setMinMaxPoints(pt1, pt2);
    }

    /**
     * Given any two points, sets the minimum and maximum points
     *
     * @param pt1 The first point of this region
     * @param pt2 The second point of this region
     */
    private void setMinMaxPoints(BlockVector pt1, BlockVector pt2) {
        List<Vector> points = new ArrayList<Vector>();
        points.add(pt1);
        points.add(pt2);
        setMinMaxPoints(points);
    }

    /**
     * Set the lower point of the cuboid.
     *
     * @param pt The point to set as the minimum point
     */
    public void setMinimumPoint(BlockVector pt) {
        setMinMaxPoints(pt, max);
    }

    /**
     * Set the upper point of the cuboid.
     *
     * @param pt The point to set as the maximum point
     */
    public void setMaximumPoint(BlockVector pt) {
        setMinMaxPoints(min, pt);
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


    /*
    public boolean intersectsWith(ProtectedRegion region) throws UnsupportedIntersectionException {

        if (region instanceof ProtectedCuboidRegion) {
            ProtectedCuboidRegion r1 = (ProtectedCuboidRegion) this;
            ProtectedCuboidRegion r2 = (ProtectedCuboidRegion) region;
            BlockVector min1 = r1.getMinimumPoint();
            BlockVector max1 = r1.getMaximumPoint();
            BlockVector min2 = r2.getMinimumPoint();
            BlockVector max2 = r2.getMaximumPoint();

            return !(min1.getBlockX() > max2.getBlockX()
                    || min1.getBlockY() > max2.getBlockY()
                    || min1.getBlockZ() > max2.getBlockZ()
                    || max1.getBlockX() < min2.getBlockX()
                    || max1.getBlockY() < min2.getBlockY()
                    || max1.getBlockZ() < min2.getBlockZ());
        } else if (region instanceof ProtectedPolygonalRegion) {
            throw new UnsupportedIntersectionException();
        } else {
            throw new UnsupportedIntersectionException();
        }
    }
    */

    @Override
    public List<ProtectedRegion> getIntersectingRegions(List<ProtectedRegion> regions) throws UnsupportedIntersectionException {
        List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion region : regions) {
            if (!intersectsBoundingBox(region)) continue;

            // If both regions are Cuboids and their bounding boxes intersect, they intersect
            if (region instanceof ProtectedCuboidRegion) {
                intersectingRegions.add(region);
                continue;
            } else if (region instanceof ProtectedPolygonalRegion) {
                // If either region contains the points of the other,
                // or if any edges intersect, the regions intersect
                if (containsAny(region.getPoints())
                        || region.containsAny(getPoints())
                        || intersectsEdges(region)) {
                    intersectingRegions.add(region);
                    continue;
                }
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        return intersectingRegions;
    }

    @Override
    public String getTypeName() {
        return "cuboid";
    }

    @Override
    public int volume() {
        int xLength = max.getBlockX() - min.getBlockX() + 1;
        int yLength = max.getBlockY() - min.getBlockY() + 1;
        int zLength = max.getBlockZ() - min.getBlockZ() + 1;

        return xLength * yLength * zLength;
    }
}