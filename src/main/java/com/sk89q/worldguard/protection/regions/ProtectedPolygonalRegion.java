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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProtectedPolygonalRegion extends ProtectedRegion {

    private List<BlockVector2D> points;
    private int minY;
    private int maxY;

    public ProtectedPolygonalRegion(String id, List<BlockVector2D> points, int minY, int maxY) {
        super(id);
        setMinMaxPoints(points, minY, maxY);
        this.points = points;
        this.minY = min.getBlockY();
        this.maxY = max.getBlockY();
    }

    /**
     * Sets the min and max points from all the 2d points and the min/max Y values
     *
     * @param points2D A {@link List} of points that this region should contain
     * @param minY The minimum y coordinate
     * @param maxY The maximum y coordinate
     */
    private void setMinMaxPoints(List<BlockVector2D> points2D, int minY, int maxY) {
        checkNotNull(points2D);

        List<Vector> points = new ArrayList<Vector>();
        int y = minY;
        for (BlockVector2D point2D : points2D) {
            points.add(new Vector(point2D.getBlockX(), y, point2D.getBlockZ()));
            y = maxY;
        }
        setMinMaxPoints(points);
    }

    @Override
    public List<BlockVector2D> getPoints() {
        return points;
    }

    @Override
    public boolean contains(Vector position) {
        checkNotNull(position);

        int targetX = position.getBlockX(); // Width
        int targetY = position.getBlockY(); // Height
        int targetZ = position.getBlockZ(); // Depth

        if (targetY < minY || targetY > maxY) {
            return false;
        }
        //Quick and dirty check.
        if (targetX < min.getBlockX() || targetX > max.getBlockX() || targetZ < min.getBlockZ() || targetZ > max.getBlockZ()) {
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

        xOld = points.get(npoints - 1).getBlockX();
        zOld = points.get(npoints - 1).getBlockZ();

        for (i = 0; i < npoints; i++) {
            xNew = points.get(i).getBlockX();
            zNew = points.get(i).getBlockZ();
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
    public List<ProtectedRegion> getIntersectingRegions(Collection<ProtectedRegion> regions) {
        checkNotNull(regions);

        List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion region : regions) {
            if (!intersectsBoundingBox(region)) continue;

            if (region instanceof ProtectedPolygonalRegion || region instanceof ProtectedCuboidRegion) {
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
        int yLength = max.getBlockY() - min.getBlockY() + 1;

        int numPoints = points.size();
        if (numPoints < 3) {
            return -1; // Invalid polygon region
        }

        double a = 0;
        double b = 0;

        List<BlockVector2D> points = getPoints();
        for (int i = 0; i < points.size(); ++i) {
            Vector2D cur = points.get(i);
            Vector2D next;
            if (i + 1 >= points.size()) {
                next = points.get(0);
            } else {
                next = points.get(i + 1);
            }

            int cx = 0, cz = 0;
            int nx = 0, nz = 0;

            if (!contains(new Vector(cur.getX() + 1, minY, cur.getZ()))) {
                ++cx;
            }
            if (!contains(new Vector(cur.getX(), minY, cur.getZ() + 1))) {
                ++cz;
            }
            if (!contains(new Vector(next.getX() + 1, minY, next.getZ()))) {
                ++nx;
            }
            if (!contains(new Vector(next.getX(), minY, next.getZ() + 1))) {
                ++nz;
            }

            cur = cur.add(cx, cz);
            next = next.add(nx, nz);

            a += cur.getX() * next.getZ();
            b += cur.getZ() * next.getX();
        }
        return (int) (.5 * Math.abs(a - b)) * yLength;
    }

}
