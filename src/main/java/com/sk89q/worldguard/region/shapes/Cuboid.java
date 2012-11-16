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

import org.apache.commons.lang.Validate;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;

/**
 * An axis-aligned cuboid.
 */
public class Cuboid extends AbstractIndexableShape {

    /**
     * Construct a new instance of this cuboid shape using two opposing points
     * of a cuboid.
     *
     * @param pt1 the first point of this region
     * @param pt2 the second point of this region
     */
    public Cuboid(BlockVector pt1, BlockVector pt2) {
        super(pt1, pt2);
    }

    /**
     * Resize this shape with two new points representing two opposing
     * corners of a cuboid.
     *
     * @param pt1 the first point of this region
     * @param pt2 the second point of this region
     */
    public void resize(BlockVector pt1, BlockVector pt2) {
        Validate.notNull(pt1, "First cuboid point cannot be null");
        Validate.notNull(pt2, "Second cuboid point cannot be null");
        updateShape(pt1, pt2);
    }

    @Override
    public List<BlockVector2D> getProjectedVerts() {
        BlockVector min = getAABBMin();
        BlockVector max = getAABBMax();

        List<BlockVector2D> pts = new ArrayList<BlockVector2D>();
        int x1 = min.getBlockX();
        int x2 = max.getBlockX();
        int z1 = min.getBlockZ();
        int z2 = max.getBlockZ();

        pts.add(new BlockVector2D(x1, z1));
        pts.add(new BlockVector2D(x2, z1));
        pts.add(new BlockVector2D(x1, z2));
        pts.add(new BlockVector2D(x2, z2));

        return pts;
    }

    @Override
    public boolean contains(Vector pt) {
        BlockVector min = getAABBMin();
        BlockVector max = getAABBMax();

        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();
        return x >= min.getBlockX() && x < max.getBlockX()+1
                && y >= min.getBlockY() && y < max.getBlockY()+1
                && z >= min.getBlockZ() && z < max.getBlockZ()+1;
    }

    @Override
    public String getTypeName() {
        return "Cuboid";
    }

    @Override
    public int volume() {
        BlockVector min = getAABBMin();
        BlockVector max = getAABBMax();

        int xLength = max.getBlockX() - min.getBlockX() + 1;
        int yLength = max.getBlockY() - min.getBlockY() + 1;
        int zLength = max.getBlockZ() - min.getBlockZ() + 1;

        return xLength * yLength * zLength;
    }

}