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

import org.khelekore.prtree.MBRConverter;

/**
 * Used to convert a shape to its minimum bounding rectangle for the priority
 * R-tree library.
 */
public class ShapeMBRConverter implements MBRConverter<IndexableShape> {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMax(int dimension, IndexableShape region) {
        switch (dimension) {
        case 0:
            return region.getAABBMax().getBlockX();
        case 1:
            return region.getAABBMax().getBlockY();
        case 2:
            return region.getAABBMax().getBlockZ();
        }
        return 0;
    }

    @Override
    public double getMin(int dimension, IndexableShape region) {
        switch (dimension) {
        case 0:
            return region.getAABBMin().getBlockX();
        case 1:
            return region.getAABBMin().getBlockY();
        case 2:
            return region.getAABBMin().getBlockZ();
        }
        return 0;
    }
}
