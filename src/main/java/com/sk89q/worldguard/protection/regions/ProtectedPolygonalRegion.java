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
        int volume = 0;
        // TODO: Fix this
        /*int numPoints = points.size();
        if (numPoints < 3) {
            return 0;
        }

        double area = 0;
        int xa, z1, z2;

        for (int i = 0; i < numPoints; i++) {
            xa = points.get(i).getBlockX();
            //za = points.get(i).getBlockZ();

            if (points.get(i + 1) == null) {
                z1 = points.get(0).getBlockZ();
            } else {
                z1 = points.get(i + 1).getBlockZ();
            }
            if (points.get(i - 1) == null) {
                z2 = points.get(numPoints - 1).getBlockZ();
            } else {
                z2 = points.get(i - 1).getBlockZ();
            }

            area = area + (xa * (z1 - z2));
        }

        xa = points.get(0).getBlockX();
        //za = points.get(0).getBlockZ();

        area = area + (xa * (points.get(1).getBlockZ() - points.get(numPoints - 1).getBlockZ()));

        volume = (Math.abs(maxY - minY) + 1) * (int) Math.ceil((Math.abs(area) / 2));*/

        return volume;
    }
}
