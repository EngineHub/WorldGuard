/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.protection.regions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegionIntersectTest {

    @Test
    public void testCuboidGetIntersectingRegions() {
        ProtectedRegion region = new ProtectedCuboidRegion("square",
                BlockVector3.at(100, 40, 0), BlockVector3.at(140, 128, 40));

        assertIntersection(region, new ProtectedCuboidRegion("normal",
                        BlockVector3.at(80, 40, -20), BlockVector3.at(120, 128, 20)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("small",
                        BlockVector3.at(98, 45, 20), BlockVector3.at(103, 50, 25)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("large",
                        BlockVector3.at(-500, 0, -600), BlockVector3.at(1000, 128, 1000)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("short",
                        BlockVector3.at(50, 40, -1), BlockVector3.at(150, 128, 2)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("long",
                        BlockVector3.at(0, 40, 5), BlockVector3.at(1000, 128, 8)),
                true);

        List<BlockVector2> triangleOverlap = new ArrayList<>();
        triangleOverlap.add(BlockVector2.at(90, -10));
        triangleOverlap.add(BlockVector2.at(120, -10));
        triangleOverlap.add(BlockVector2.at(90, 20));

        assertIntersection(region, new ProtectedPolygonalRegion("triangleOverlap",
                triangleOverlap, 0, 128),
                true);

        List<BlockVector2> triangleNoOverlap = new ArrayList<>();
        triangleNoOverlap.add(BlockVector2.at(90, -10));
        triangleNoOverlap.add(BlockVector2.at(105, -10));
        triangleNoOverlap.add(BlockVector2.at(90, 5));

        assertIntersection(region, new ProtectedPolygonalRegion("triangleNoOverlap",
                triangleNoOverlap, 0, 128),
                false);

        List<BlockVector2> triangleOverlapNoPoints = new ArrayList<>();
        triangleOverlapNoPoints.add(BlockVector2.at(100, -10));
        triangleOverlapNoPoints.add(BlockVector2.at(120, 50));
        triangleOverlapNoPoints.add(BlockVector2.at(140, -20));

        assertIntersection(region, new ProtectedPolygonalRegion("triangleOverlapNoPoints",
                triangleOverlapNoPoints, 60, 80),
                true);
    }

    private void assertIntersection(ProtectedRegion region1, ProtectedRegion region2, boolean expected) {
        boolean actual = false;
        List<ProtectedRegion> regions = new ArrayList<>();
        regions.add(region2);

        try {
            actual = (region1.getIntersectingRegions(regions).size() == 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals("Check for '" + region2.getId() + "' region failed.", expected, actual);
    }

    private static final BlockVector2[] polygon = {
            BlockVector2.at(1, 0),
            BlockVector2.at(4, 3),
            BlockVector2.at(4, -3),
    };

    @Test
    public void testIntersection() throws Exception {
        final ProtectedCuboidRegion cuboidRegion = new ProtectedCuboidRegion("cuboidRegion", BlockVector3.at(-3, -3, -3), BlockVector3.at(3, 3, 3));
        for (int angle = 0; angle < 360; angle += 90) {
            final BlockVector2[] rotatedPolygon = new BlockVector2[polygon.length];
            for (int i = 0; i < polygon.length; i++) {
                final BlockVector2 vertex = polygon[i];
                rotatedPolygon[i] = vertex.transform2D(angle, 0, 0, 0, 0);
            }

            final ProtectedPolygonalRegion polygonalRegion = new ProtectedPolygonalRegion("polygonalRegion", Arrays.asList(rotatedPolygon), -3, 3);

            assertTrue(String.format("%s does not intersect (cuboid.intersectsEdges(polygonal)", Arrays.asList(rotatedPolygon)), cuboidRegion.intersectsEdges(polygonalRegion));
            assertTrue(String.format("%s does not intersect (polygonal.intersectsEdges(cuboid)", Arrays.asList(rotatedPolygon)), polygonalRegion.intersectsEdges(cuboidRegion));
        }
    }
}
