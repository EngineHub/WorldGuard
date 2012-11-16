// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region.shapes;

import java.awt.geom.Line2D;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;

/**
 * A simple base class for easy implementation of {@link IndexableShape}.
 */
public abstract class AbstractIndexableShape implements IndexableShape {

    private BlockVector min;
    private BlockVector max;

    /**
     * Construct this object and build an AABB to store in memory from the provided
     * list of vertices.
     *
     * @param vertices list of vertices
     */
    public AbstractIndexableShape(List<Vector> vertices) {
        updateShape(vertices);
    }

    /**
     * Construct this object and build an AABB to store in memory from the provided
     * list of vertices.
     *
     * @param vertices list of vertices
     */
    public AbstractIndexableShape(Vector... vertices) {
        updateShape(vertices);
    }

    /**
     * Update this shape with a new bounding box. This method should be called if
     * an implementing shape gets resized during its lifetime.
     *
     * @param vertices list of vertices
     */
    protected void updateShape(List<Vector> vertices) {
        Validate.notNull(vertices, "List of vertices cannot be null");
        Validate.noNullElements(vertices, "No null entries are allowed in the list of vertices");
        Validate.isTrue(vertices.size() >= 3, "At least 3 vertices are required to make a valid shape");

        int minX = vertices.get(0).getBlockX();
        int minY = vertices.get(0).getBlockY();
        int minZ = vertices.get(0).getBlockZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;

        for (Vector v : vertices) {
            int x = v.getBlockX();
            int y = v.getBlockY();
            int z = v.getBlockZ();

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;

            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        min = new BlockVector(minX, minY, minZ);
        max = new BlockVector(maxX, maxY, maxZ);
    }

    /**
     * Update this shape with a new bounding box. This method should be called if
     * an implementing shape gets resized during its lifetime.
     *
     * @param vertices list of vertices
     */
    protected void updateShape(Vector... vertices) {
        Validate.notNull(vertices, "List of vertices cannot be null");
        Validate.noNullElements(vertices, "No null entries are allowed in the list of vertices");
        Validate.isTrue(vertices.length >= 3, "At least 3 vertices are required to make a valid shape");

        int minX = vertices[0].getBlockX();
        int minY = vertices[0].getBlockY();
        int minZ = vertices[0].getBlockZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;

        for (Vector v : vertices) {
            int x = v.getBlockX();
            int y = v.getBlockY();
            int z = v.getBlockZ();

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;

            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;
        }

        min = new BlockVector(minX, minY, minZ);
        max = new BlockVector(maxX, maxY, maxZ);
    }

    @Override
    public BlockVector getAABBMin() {
        return min;
    }

    @Override
    public BlockVector getAABBMax() {
        return max;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return contains(new Vector(x, y, z));
    }

    @Override
    public boolean intersectsMbr(IndexableShape other) {
        BlockVector rMaxPoint = other.getAABBMax();
        BlockVector min = getAABBMin();

        if (rMaxPoint.getBlockX() < min.getBlockX()) return false;
        if (rMaxPoint.getBlockY() < min.getBlockY()) return false;
        if (rMaxPoint.getBlockZ() < min.getBlockZ()) return false;

        BlockVector rMinPoint = other.getAABBMin();
        BlockVector max = getAABBMax();

        if (rMinPoint.getBlockX() > max.getBlockX()) return false;
        if (rMinPoint.getBlockY() > max.getBlockY()) return false;
        if (rMinPoint.getBlockZ() > max.getBlockZ()) return false;

        return true;
    }

    @Override
    public boolean intersectsEdges(IndexableShape other) {
        List<BlockVector2D> pts1 = other.getProjectedVerts();
        List<BlockVector2D> pts2 = other.getProjectedVerts();
        BlockVector2D lastPt1 = pts1.get(pts1.size() - 1);
        BlockVector2D lastPt2 = pts2.get(pts2.size() - 1);

        for (BlockVector2D aPts1 : pts1) {
            for (BlockVector2D aPts2 : pts2) {
                Line2D line1 = new Line2D.Double(
                        lastPt1.getBlockX(),
                        lastPt1.getBlockZ(),
                        aPts1.getBlockX(),
                        aPts1.getBlockZ());

                if (line1.intersectsLine(
                        lastPt2.getBlockX(),
                        lastPt2.getBlockZ(),
                        aPts2.getBlockX(),
                        aPts2.getBlockZ())) {
                    return true;
                }

                lastPt2 = aPts2;
            }

            lastPt1 = aPts1;
        }

        return false;
    }

}
