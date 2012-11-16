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
package com.sk89q.worldguard.region.shapes;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;

public class ExtrudedPolygon extends AbstractIndexableShape {

    protected List<BlockVector2D> points;
    protected int minY;
    protected int maxY;

    public ExtrudedPolygon(String id, List<BlockVector2D> points, int minY, int maxY) {
        this.points = points;
        updateShape(points, minY, maxY);
        this.minY = getAABBMin().getBlockY();
        this.maxY = getAABBMax().getBlockY();
    }

    /**
     * Creates a list of vertices for {@link #updateShape(List)}. This is needed because
     * this object's list of points is only 2-dimension and the specified method
     * requires 3-dimensional vertices.
     *
     * @param points2D a {@link List} of points that this region should contain
     * @param minY the minimum y coordinate
     * @param maxY the maximum y coordinate
     */
    private void updateShape(List<BlockVector2D> points2D, int minY, int maxY) {
        List<Vector> points = new ArrayList<Vector>();
        int y = minY;
        for (BlockVector2D point2D : points2D) {
            points.add(new Vector(point2D.getBlockX(), y, point2D.getBlockZ()));
            y = maxY;
        }
        updateShape(points);
    }

    @Override
    public List<BlockVector2D> getProjectedVerts() {
        return points;
    }

    @Override
    public boolean contains(Vector pt) {
        BlockVector min = getAABBMin();
        BlockVector max = getAABBMax();

        int targetX = pt.getBlockX(); // Width
        int targetY = pt.getBlockY(); // Height
        int targetZ = pt.getBlockZ(); // Depth

        if (targetY < minY || targetY > maxY) {
            return false;
        }

        // Do a quick check first
        if (targetX < min.getBlockX() || targetX > max.getBlockX()
                || targetZ < min.getBlockZ() || targetZ > max.getBlockZ()) {
            return false;
        }

        boolean inside = false;
        int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossProduct;
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
                crossProduct = ((long) targetZ - (long) z1) * (x2 - x1)
                    - ((long) z2 - (long) z1) * (targetX - x1);
                if (crossProduct == 0) {
                    if ((z1 <= targetZ) == (targetZ <= z2)) return true; // On edge
                } else if (crossProduct < 0 && (x1 != targetX)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;
    }

    @Override
    public String getTypeName() {
        return "Polygon";
    }

    @Override
    public int volume() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
