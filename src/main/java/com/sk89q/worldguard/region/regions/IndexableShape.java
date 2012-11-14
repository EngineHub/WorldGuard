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

package com.sk89q.worldguard.region.regions;

import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;

/**
 * An object representing a physical area that is suitable for use in a region index.
 * It has an AABB that can be derived from it, and it may perform tests of intersection
 * or tests to see whether it contains a particular point.
 * <p>
 * For WorldGuard, it is not recommended to create new types of Shapes outside of
 * WorldGuard for use in WorldGuard, as this is not supported at all and will only
 * cause errors.
 * <p>
 * In the future, this interface will inherit from its cousin in WorldEdit, once
 * WorldEdit gets a planned API make over. This change should be backwards-compatible
 * and should have no impact on users of this class.
 */
public interface IndexableShape {

    /**
     * Get the "minimum" point of the 3-dimensional axis-aligned (AA) minimum
     * bounding box (BB) of this shape, or also known as the AABB.
     * <p>
     * The AABB is the smallest possible axis-aligned cuboid that can contain the
     * entirety of this shape. Because the AABB is axis-aligned, only two points in
     * 3-dimensional space are sufficient to fully describe the geometry. The AABB
     * has eight vertexes of such to choose from, where each vertex has one
     * complimentary, allowing for four pairs of vertexes to describe the AABB. We
     * consider the minimum point as the minimum possible X, Y, and Z coordinates of the
     * AABB's vertexes.
     *
     * @return the minimum point
     * @see #getMaximumPoint() for the maximum point
     */
    BlockVector getMinimumPoint();

    /**
     * Get the "maximum" point of the 3-dimensional axis-aligned (AA) minimum
     * bounding box (BB) of this shape, or also known as the AABB.
     * <p>
     * The AABB is the smallest possible axis-aligned cuboid that can contain the
     * entirety of this shape. Because the AABB is axis-aligned, only two points in
     * 3-dimensional space are sufficient to fully describe the geometry. The AABB
     * has eight vertexes of such to choose from, where each vertex has one
     * complimentary, allowing for four pairs of vertexes to describe the AABB. We
     * consider the maximum point as the maximum possible X, Y, and Z coordinates of the
     * AABB's vertexes.
     *
     * @return the maximum point
     * @see #getMinimumPoint() for the minimum point
     */
    BlockVector getMaximumPoint();

    /**
     * Get the vertices corresponding to minimum bounding polygon (MBP) of the projection
     * of this shape onto a 2-dimensional plane normal to the Y-axis.
     *
     * @return a list of vertices
     */
    List<BlockVector2D> get2DProjectionVertices();

    /**
     * Get the number of blocks that are contained by this region. Only blocks that
     * are considered contained by {@link #contains(Vector)} should be counted.
     *
     * @return the number of blocks
     */
    int volume();

    /**
     * Checks if the location is contained by this shape.
     *
     * @param location the location to check
     * @return true if the given location is within this shape
     */
    boolean contains(Vector location);

    /**
     * Checks if the point is contained by this shape.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return true if the given location is within this shape
     */
    boolean contains(int x, int y, int z);

    /**
     * Tests whether the given shape's AABB (axis-aligned minimum bounding box)
     * intersects with this shape's AABB.
     * <p>
     * While this test may return true for two arbitrary shapes, it does not
     * necessarily mean that the two shapes are actually intersecting (imagine two
     * complimentary triangle halves having the same AABB but not actually
     * overlapping). Use the {@link #intersectsEdges(IndexableShape)} method for a more
     * accurate, but slower, test.
     *
     * @param other the other shape
     * @return true if there is an intersection
     * @see #intersectsEdges(IndexableShape) a more accurate shape intersection test
     */
    boolean intersectsMbr(IndexableShape other);

    /**
     * Tests whether this shape intersects with the given shape, meaning that there are
     * one or more locations that are contained by both shapes.
     * <p>
     * The test is performed by comparing whether the edges of each shape's
     * 2-dimensional projection (see {@link #get2DProjectionVertices()}) overlap with
     * the other shape. Thus the test has O(n^2) performance for two shapes with
     * the same number of vertices in their 2-dimensional projection. For a faster,
     * but not necessarily accurate, intersect test, consider using
     * {@link #intersectsMbr(IndexableShape)}.
     *
     * @param other the other shape
     * @return true if there is an intersection
     * @see #intersectsMbr(IndexableShape) a faster very-approximate shape intersection test
     */
    boolean intersectsEdges(IndexableShape other);

    /**
     * Get a friendly name for this shape.
     * <p>
     * The name should be in sentence-case. For example, a cuboid may return "Cuboid".
     *
     * @return the type name
     */
    String getTypeName();

}
