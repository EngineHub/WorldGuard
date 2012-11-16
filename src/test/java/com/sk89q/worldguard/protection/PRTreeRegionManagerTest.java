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

package com.sk89q.worldguard.protection;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.region.Region;
import com.sk89q.worldguard.region.flags.DefaultFlag;
import com.sk89q.worldguard.region.flags.StateFlag;
import com.sk89q.worldguard.region.indices.PriorityRTreeIndex;
import com.sk89q.worldguard.region.indices.RegionIndex;
import com.sk89q.worldguard.region.shapes.Cuboid;
import com.sk89q.worldguard.region.shapes.ExtrudedPolygon;
import com.sk89q.worldguard.region.shapes.GlobalProtectedRegion;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PRTreeRegionManagerTest extends RegionOverlapTest {
    protected RegionIndex createRegionManager() throws Exception {
        return new PriorityRTreeIndex(null);
    }
}
