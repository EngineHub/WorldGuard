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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegionIntersectTest {

    @Test
    public void testCuboidGetIntersectingRegions() {
        ProtectedRegion region = new ProtectedCuboidRegion("square",
                new BlockVector(100, 40, 0), new BlockVector(140, 128, 40));

        assertIntersection(region, new ProtectedCuboidRegion("normal",
                new BlockVector(80, 40, -20), new BlockVector(120, 128, 20)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("small",
                new BlockVector(98, 45, 20), new BlockVector(103, 50, 25)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("large",
                new BlockVector(-500, 0, -600), new BlockVector(1000, 128, 1000)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("short",
                new BlockVector(50, 40, -1), new BlockVector(150, 128, 2)),
                true);

        assertIntersection(region, new ProtectedCuboidRegion("long",
                new BlockVector(0, 40, 5), new BlockVector(1000, 128, 8)),
                true);

        List<BlockVector2D> triangleOverlap = new ArrayList<BlockVector2D>();
        triangleOverlap.add(new BlockVector2D(90, -10));
        triangleOverlap.add(new BlockVector2D(120, -10));
        triangleOverlap.add(new BlockVector2D(90, 20));

        assertIntersection(region, new ProtectedPolygonalRegion("triangleOverlap",
                triangleOverlap, 0, 128),
                true);

        List<BlockVector2D> triangleNoOverlap = new ArrayList<BlockVector2D>();
        triangleNoOverlap.add(new BlockVector2D(90, -10));
        triangleNoOverlap.add(new BlockVector2D(105, -10));
        triangleNoOverlap.add(new BlockVector2D(90, 5));

        assertIntersection(region, new ProtectedPolygonalRegion("triangleNoOverlap",
                triangleNoOverlap, 0, 128),
                false);

        List<BlockVector2D> triangleOverlapNoPoints = new ArrayList<BlockVector2D>();
        triangleOverlapNoPoints.add(new BlockVector2D(100, -10));
        triangleOverlapNoPoints.add(new BlockVector2D(120, 50));
        triangleOverlapNoPoints.add(new BlockVector2D(140, -20));

        assertIntersection(region, new ProtectedPolygonalRegion("triangleOverlapNoPoints",
                triangleOverlapNoPoints, 60, 80),
                true);
    }

    private void assertIntersection(ProtectedRegion region1, ProtectedRegion region2, boolean expected) {
        boolean actual = false;
        List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
        regions.add(region2);

        try {
            actual = (region1.getIntersectingRegions(regions).size() == 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals("Check for '" + region2.getId() + "' region failed.", expected, actual);
    }

    private static final BlockVector2D[] polygon = {
            new BlockVector2D(1, 0),
            new BlockVector2D(4, 3),
            new BlockVector2D(4, -3),
    };

    @Test
    public void testIntersection() throws Exception {
        final ProtectedCuboidRegion cuboidRegion = new ProtectedCuboidRegion("cuboidRegion", new BlockVector(-3, -3, -3), new BlockVector(3, 3, 3));
        for (int angle = 0; angle < 360; angle += 90) {
            final BlockVector2D[] rotatedPolygon = new BlockVector2D[polygon.length];
            for (int i = 0; i < polygon.length; i++) {
                final BlockVector2D vertex = polygon[i];
                rotatedPolygon[i] = vertex.transform2D(angle, 0, 0, 0, 0).toBlockVector2D();
            }

            final ProtectedPolygonalRegion polygonalRegion = new ProtectedPolygonalRegion("polygonalRegion", Arrays.asList(rotatedPolygon), -3, 3);

            assertTrue(String.format("%s does not intersect (cuboid.intersectsEdges(polygonal)", Arrays.asList(rotatedPolygon)), cuboidRegion.intersectsEdges(polygonalRegion));
            assertTrue(String.format("%s does not intersect (polygonal.intersectsEdges(cuboid)", Arrays.asList(rotatedPolygon)), polygonalRegion.intersectsEdges(cuboidRegion));
        }
    }
}
