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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;

/**
 * Indicates a region that encompasses the world.
 */
public class Everywhere implements IndexableShape {

    private static final BlockVector min = new BlockVector(
            Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private static final BlockVector max = new BlockVector(
            Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private static final List<BlockVector2D> projVertices;

    static {
        List<BlockVector2D> vertices = new LinkedList<BlockVector2D>();
        vertices.add(new BlockVector2D(Integer.MIN_VALUE, Integer.MIN_VALUE));
        vertices.add(new BlockVector2D(Integer.MAX_VALUE, Integer.MIN_VALUE));
        vertices.add(new BlockVector2D(Integer.MAX_VALUE, Integer.MAX_VALUE));
        vertices.add(new BlockVector2D(Integer.MIN_VALUE, Integer.MAX_VALUE));
        projVertices = Collections.unmodifiableList(vertices);
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
    public List<BlockVector2D> getProjectedVerts() {
        return projVertices;
    }

    @Override
    public int volume() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean contains(Vector location) {
        return true;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return true;
    }

    @Override
    public boolean intersectsMbr(IndexableShape other) {
        return true;
    }

    @Override
    public boolean intersectsEdges(IndexableShape other) {
        return true;
    }

    @Override
    public String getTypeName() {
        return "Everywhere";
    }

}
