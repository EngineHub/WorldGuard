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

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;

public class ProtectedPolygonalRegion extends ProtectedRegion {

    protected List<BlockVector2D> points;
    protected int minY;
    protected int maxY;

    public ProtectedPolygonalRegion(String id, List<BlockVector2D> points, int minY, int maxY) {
        super(id);
        this.points = points;
        setMinMaxPoints(points, minY, maxY);
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
        List<Vector> points = new ArrayList<Vector>();
        int y = minY;
        for (BlockVector2D point2D : points2D) {
            points.add(new Vector(point2D.getBlockX(), y, point2D.getBlockZ()));
            y = maxY;
        }
        setMinMaxPoints(points);
    }

    public List<BlockVector2D> getPoints() {
        return points;
    }

    /**
     * Checks to see if a point is inside this region.
     */
    @Override
    public boolean contains(Vector pt) {
        int targetX = pt.getBlockX(); //wide
        int targetY = pt.getBlockY(); //height
        int targetZ = pt.getBlockZ(); //depth

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
    public List<ProtectedRegion> getIntersectingRegions(List<ProtectedRegion> regions) throws UnsupportedIntersectionException {
        List<ProtectedRegion> intersectingRegions = new ArrayList<ProtectedRegion>();

        for (ProtectedRegion region : regions) {
            if (!intersectsBoundingBox(region)) continue;

            if (region instanceof ProtectedPolygonalRegion || region instanceof ProtectedCuboidRegion) {
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


    /**
     * Return the type of region as a user-friendly name.
     *
     * @return type of region
     */
    @Override
    public String getTypeName() {
        return "polygon";
    }

    @Override
    public int volume() {
        int yLength = max.getBlockY() - min.getBlockY() + 1;

        int numPoints = points.size();
        if (numPoints < 3) {
            int xLength = max.getBlockX() - min.getBlockX() + 1;
            int zLength = max.getBlockZ() - min.getBlockZ() + 1;

            return xLength * yLength * zLength;
        }

        int area = 0;
        BlockVector2D p1, p2;
        int s = numPoints - 1;

        for (int i = 0; i < numPoints; i++) {

            // Update/define p1 & p2
            p1 = points.get(i);
            p2 = points.get(s);

            // Do the math, then reassign s
            area += (p2.getBlockX() + p1.getBlockX()) * (p2.getBlockZ() - p1.getBlockZ());
            s = i;
        }
        return (int) Math.abs(Math.ceil(area / 2D) * yLength);
    }
}
