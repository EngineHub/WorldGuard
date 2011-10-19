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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;

public class GlobalProtectedRegion extends ProtectedRegion {

    public GlobalProtectedRegion(String id) {
        super(id);
        min = new BlockVector(0, 0, 0);
        max = new BlockVector(0, 0, 0);
    }

    public List<BlockVector2D> getPoints() {
        List<BlockVector2D> pts = new ArrayList<BlockVector2D>();
        pts.add(new BlockVector2D(min.getBlockX(),min.getBlockZ()));
        return pts;
    }

    @Override
    public int volume() {
        return 0;
    }

    @Override
    public boolean contains(Vector pt) {
        return false;
    }

    @Override
    public String getTypeName() {
        return "global";
    }

    @Override
    public List<ProtectedRegion> getIntersectingRegions(
            List<ProtectedRegion> regions)
            throws UnsupportedIntersectionException {
        return new ArrayList<ProtectedRegion>();
    }

}
