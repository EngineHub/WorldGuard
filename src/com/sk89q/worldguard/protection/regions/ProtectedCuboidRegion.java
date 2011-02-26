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

import com.sk89q.worldedit.*;

/**
 * Represents a cuboid region that can be protected.
 *
 * @author sk89q
 */
public class ProtectedCuboidRegion extends ProtectedRegion {
    /**
     * Store the first point.
     */
    private BlockVector min;
    /**
     * Store the second point.
     */
    private BlockVector max;

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param id
     * @param pos1
     * @param pos2
     * @param priority
     */
    public ProtectedCuboidRegion(String id, BlockVector min, BlockVector max) {
        super(id);
        this.min = min;
        this.max = max;
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    public BlockVector getMinimumPoint() {
        return min;
    }

    /**
     * Set the lower point of the cuboid.
     *
     * @param pt
     */
    public void setMinimumPoint(BlockVector pt) {
        min = pt;
    }

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    public BlockVector getMaximumPoint() {
        return max;
    }

    /**
     * Set the upper point of the cuboid.
     *
     * @param pt
     */
    public void setMaximumPoint(BlockVector pt) {
        max = pt;
    }

    /**
     * Checks to see if a point is inside this region.
     */
    @Override
    public boolean contains(Vector pt) {
        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();
        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }
    
    /**
     * Return the type of region as a user-friendly name.
     * 
     * @return type of region
     */
    public String getTypeName() {
        return "cuboid";
    }

    /**
     * Get the number of Blocks in this region
     * 
     * @return
     */
    public int countBlocks() {
        int xLength = max.getBlockX() - min.getBlockX() + 1;
        int yLength = max.getBlockY() - min.getBlockY() + 1;
        int zLength = max.getBlockZ() - min.getBlockZ() + 1;

        int volume = xLength * yLength * zLength;
        return volume;
    }
}
