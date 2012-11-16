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

import org.khelekore.prtree.MBRConverter;

import com.sk89q.worldguard.region.Region;

/**
 * Used to convert a shape to its minimum bounding rectangle for the priority
 * R-tree library.
 */
public class ShapeMBRConverter implements MBRConverter<Region> {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMax(int dimension, Region region) {
        switch (dimension) {
        case 0:
            return region.getShape().getAABBMax().getBlockX();
        case 1:
            return region.getShape().getAABBMax().getBlockY();
        case 2:
            return region.getShape().getAABBMax().getBlockZ();
        }
        return 0;
    }

    @Override
    public double getMin(int dimension, Region region) {
        switch (dimension) {
        case 0:
            return region.getShape().getAABBMin().getBlockX();
        case 1:
            return region.getShape().getAABBMin().getBlockY();
        case 2:
            return region.getShape().getAABBMin().getBlockZ();
        }
        return 0;
    }
}
