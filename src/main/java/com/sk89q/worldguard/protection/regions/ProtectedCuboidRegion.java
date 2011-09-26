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
     * Store the first point.
     */
    private BlockVector min;
    /**
     * Store the second point.
     */
    private BlockVector max;

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param id
     * @param min 
     * @param max 
     */
    public ProtectedCuboidRegion(String id, BlockVector min, BlockVector max) {
        super(id);
        this.min = min;
        this.max = max;
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    @Override
    public BlockVector getMinimumPoint() {
        return min;
    }

    /**
     * Set the lower point of the cuboid.
     *
     * @param pt
     */
    public void setMinimumPoint(BlockVector pt) {
        min = pt;
    }

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    @Override
    public BlockVector getMaximumPoint() {
        return max;
    }

    /**
     * Set the upper point of the cuboid.
     *
     * @param pt
     */
    public void setMaximumPoint(BlockVector pt) {
        max = pt;
    }

    /**
     * Checks to see if a point is inside this region.
     */
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
        int numRegions = regions.size();
        List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();
        int i;

        for (i = 0; i < numRegions; i++) {
            ProtectedRegion region = regions.get(i);
            BlockVector rMinPoint = region.getMinimumPoint();
            BlockVector rMaxPoint = region.getMaximumPoint();

            // Check whether the region is outside the min and max vector
            if ((rMinPoint.getBlockX() < min.getBlockX() && rMaxPoint.getBlockX() < min.getBlockX()) 
                    || (rMinPoint.getBlockX() > max.getBlockX() && rMaxPoint.getBlockX() > max.getBlockX())
                    || (rMinPoint.getBlockY() < min.getBlockY() && rMaxPoint.getBlockY() < min.getBlockY())
                    || (rMinPoint.getBlockY() > max.getBlockY() && rMaxPoint.getBlockY() > max.getBlockY())
                    || (rMinPoint.getBlockZ() < min.getBlockZ() && rMaxPoint.getBlockZ() < min.getBlockZ())
                    || (rMinPoint.getBlockZ() > max.getBlockZ() && rMaxPoint.getBlockZ() > max.getBlockZ())) {

                    // One or more dimensions wholly outside. Regions aren't overlapping.
                    continue;
            }
            // ^ is false, therefore regions must be overlapping, if cuboid.
            if (region instanceof ProtectedCuboidRegion) {
                intersectingRegions.add(regions.get(i));
                continue;
            }
            // No more checks needed for cuboid region against cuboid region!

            // Poly region. Check whether corners of intersecting bounding box are inside region
            int x1 = Math.max(rMinPoint.getBlockX(), min.getBlockX());
            int y1 = Math.max(rMinPoint.getBlockY(), min.getBlockY());
            int z1 = Math.max(rMinPoint.getBlockZ(), min.getBlockZ());
            int x2 = Math.min(rMaxPoint.getBlockX(), max.getBlockX());
            int y2 = Math.min(rMaxPoint.getBlockY(), max.getBlockY());
            int z2 = Math.min(rMaxPoint.getBlockZ(), max.getBlockZ());
            if (region.contains(new Vector(x1, y1, z1))
                    || region.contains(new Vector(x1, y1, z2))
                    || region.contains(new Vector(x1, y2, z2))
                    || region.contains(new Vector(x1, y2, z1))
                    || region.contains(new Vector(x2, y2, z2))
                    || region.contains(new Vector(x2, y2, z1))
                    || region.contains(new Vector(x2, y1, z1))
                    || region.contains(new Vector(x2, y1, z2)) ) {
                intersectingRegions.add(regions.get(i));
                continue;
            }
        }

        return intersectingRegions;
    }


    /**
     * Return the type of region as a user-friendly name.
     * 
     * @return type of region
     */
    @Override
    public String getTypeName() {
        return "cuboid";
    }

    /**
     * Get the number of Blocks in this region
     * 
     * @return
     */
    @Override
    public int volume() {
        int xLength = max.getBlockX() - min.getBlockX() + 1;
        int yLength = max.getBlockY() - min.getBlockY() + 1;
        int zLength = max.getBlockZ() - min.getBlockZ() + 1;

        int volume = xLength * yLength * zLength;
        return volume;
    }


}
