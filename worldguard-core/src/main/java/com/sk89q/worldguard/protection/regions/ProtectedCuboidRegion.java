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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.util.MathUtils;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cuboid region that can be protected.
 *
 * @author sk89q
 */
public class ProtectedCuboidRegion extends ProtectedRegion {

    /**
     * Construct a new instance of this cuboid region.<br>
     * Equivalent to {@link #ProtectedCuboidRegion(String, boolean, BlockVector3, BlockVector3)
     * ProtectedCuboidRegion(id, false, pt1, pt2)}<br>
     * <code>transientRegion</code> will be set to false, and this region can be saved.
     *
     * @param id the region id
     * @param pt1 the first point of this region
     * @param pt2 the second point of this region
     */
    public ProtectedCuboidRegion(String id, BlockVector3 pt1, BlockVector3 pt2) {
        this(id, false, pt1, pt2);
    }

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param id the region id
     * @param transientRegion whether this region should only be kept in memory and not be saved
     * @param pt1 the first point of this region
     * @param pt2 the second point of this region
     */
    public ProtectedCuboidRegion(String id, boolean transientRegion, BlockVector3 pt1, BlockVector3 pt2) {
        super(id, transientRegion);
        setMinMaxPoints(pt1, pt2);
    }

    /**
     * Given any two points, sets the minimum and maximum points.
     *
     * @param position1 the first point of this region
     * @param position2 the second point of this region
     */
    private void setMinMaxPoints(BlockVector3 position1, BlockVector3 position2) {
        checkNotNull(position1);
        checkNotNull(position2);

        List<BlockVector3> points = new ArrayList<>();
        points.add(position1);
        points.add(position2);
        setMinMaxPoints(points);
    }

    /**
     * Set the lower point of the cuboid.
     *
     * @param position the point to set as the minimum point
     * @deprecated ProtectedRegion bounds should never be mutated. Regions must be redefined to move them.
     *              This method will be removed in a future release.
     */
    @Deprecated
    public void setMinimumPoint(BlockVector3 position) {
        WorldGuard.logger.warning("ProtectedCuboidRegion#setMinimumPoint call ignored. Mutating regions leads to undefined behavior.");
    }

    /**
     * Set the upper point of the cuboid.
     *
     * @param position the point to set as the maximum point
     * @deprecated ProtectedRegion bounds should never be mutated. Regions must be redefined to move them.
     *              This method will be removed in a future release.
     */
    @Deprecated
    public void setMaximumPoint(BlockVector3 position) {
        WorldGuard.logger.warning("ProtectedCuboidRegion#setMaximumPoint call ignored. Mutating regions leads to undefined behavior.");
    }

    @Override
    public boolean isPhysicalArea() {
        return true;
    }

    @Override
    public List<BlockVector2> getPoints() {
        List<BlockVector2> pts = new ArrayList<>();
        int x1 = min.x();
        int x2 = max.x();
        int z1 = min.z();
        int z2 = max.z();

        pts.add(BlockVector2.at(x1, z1));
        pts.add(BlockVector2.at(x2, z1));
        pts.add(BlockVector2.at(x2, z2));
        pts.add(BlockVector2.at(x1, z2));

        return pts;
    }

    @Override
    public boolean contains(BlockVector3 pt) {
        final double x = pt.x();
        final double y = pt.y();
        final double z = pt.z();
        return x >= min.x() && x < max.x() + 1
                && y >= min.y() && y < max.y() + 1
                && z >= min.z() && z < max.z() + 1;
    }

    @Override
    public RegionType getType() {
        return RegionType.CUBOID;
    }

    @Override
    Area toArea() {
        int x = getMinimumPoint().x();
        int z = getMinimumPoint().z();
        int width = getMaximumPoint().x() - x + 1;
        int height = getMaximumPoint().z() - z + 1;
        return new Area(new Rectangle(x, z, width, height));
    }

    @Override
    protected boolean intersects(ProtectedRegion region, Area thisArea) {
        if (region instanceof ProtectedCuboidRegion) {
            return intersectsBoundingBox(region);
        } else {
            return super.intersects(region, thisArea);
        }
    }

    @Override
    public int volume() {
        int xLength = max.x() - min.x() + 1;
        int yLength = max.y() - min.y() + 1;
        int zLength = max.z() - min.z() + 1;

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
