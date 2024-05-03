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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class ProtectedPolygonalRegion extends ProtectedRegion {

    private final ImmutableList<BlockVector2> points;
    private final int minY;
    private final int maxY;

    /**
     * Construct a new instance of this polygonal region.<br>
     * Equivalent to {@link #ProtectedPolygonalRegion(String, boolean, List, int, int)
     * ProtectedPolygonalRegion(id, false, points, minY, maxY)}<br>
     * <code>transientRegion</code> will be set to false, and this region can be saved.
     *
     * @param id the region id
     * @param points a {@link List} of points that this region should contain
     * @param minY the minimum y coordinate
     * @param maxY the maximum y coordinate
     */
    public ProtectedPolygonalRegion(String id, List<BlockVector2> points, int minY, int maxY) {
        this(id, false, points, minY, maxY);
    }

    /**
     * Construct a new instance of this polygonal region.
     *
     * @param id the region id
     * @param transientRegion whether this region should only be kept in memory and not be saved
     * @param points a {@link List} of points that this region should contain
     * @param minY the minimum y coordinate
     * @param maxY the maximum y coordinate
     */
    public ProtectedPolygonalRegion(String id, boolean transientRegion, List<BlockVector2> points, int minY, int maxY) {
        super(id, transientRegion);
        ImmutableList<BlockVector2> immutablePoints = ImmutableList.copyOf(points);
        setMinMaxPoints(immutablePoints, minY, maxY);
        this.points = immutablePoints;
        this.minY = min.y();
        this.maxY = max.y();
    }

    /**
     * Sets the min and max points from all the 2d points and the min/max Y values
     *
     * @param points2D A {@link List} of points that this region should contain
     * @param minY The minimum y coordinate
     * @param maxY The maximum y coordinate
     */
    private void setMinMaxPoints(List<BlockVector2> points2D, int minY, int maxY) {
        checkNotNull(points2D);

        List<BlockVector3> points = new ArrayList<>();
        int y = minY;
        for (BlockVector2 point2D : points2D) {
            points.add(BlockVector3.at(point2D.x(), y, point2D.z()));
            y = maxY;
        }
        setMinMaxPoints(points);
    }

    @Override
    public boolean isPhysicalArea() {
        return true;
    }

    @Override
    public List<BlockVector2> getPoints() {
        return points;
    }

    @Override
    public boolean contains(BlockVector3 position) {
        checkNotNull(position);

        int targetX = position.x(); // Width
        int targetY = position.y(); // Height
        int targetZ = position.z(); // Depth

        if (targetY < minY || targetY > maxY) {
            return false;
        }
        //Quick and dirty check.
        if (targetX < min.x() || targetX > max.x() || targetZ < min.z() || targetZ > max.z()) {
            return false;
        }
        boolean inside = false;
        int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;
        int i;

        xOld = points.get(npoints - 1).x();
        zOld = points.get(npoints - 1).z();

        for (i = 0; i < npoints; i++) {
            xNew = points.get(i).x();
            zNew = points.get(i).z();
            //Check for corner
            if (xNew == targetX && zNew == targetZ) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
            if (x1 <= targetX && targetX <= x2) {
                crossproduct = ((long) targetZ - (long) z1) * (long) (x2 - x1)
                    - ((long) z2 - (long) z1) * (long) (targetX - x1);
                if (crossproduct == 0) {
                    if ((z1 <= targetZ) == (targetZ <= z2)) return true; // on edge
                } else if (crossproduct < 0 && (x1 != targetX)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;
    }

    @Override
    public RegionType getType() {
        return RegionType.POLYGON;
    }

    @Override
    Area toArea() {
        List<BlockVector2> points = getPoints();
        int numPoints = points.size();
        int[] xCoords = new int[numPoints];
        int[] yCoords = new int[numPoints];

        int i = 0;
        for (BlockVector2 point : points) {
            xCoords[i] = point.x();
            yCoords[i] = point.z();
            i++;
        }

        Polygon polygon = new Polygon(xCoords, yCoords, numPoints);
        return new Area(polygon);
    }

    @Override
    public int volume() {
        // TODO: Fix this -- the previous algorithm returned incorrect results, but the current state of this method is even worse
        return 0;
    }

}
