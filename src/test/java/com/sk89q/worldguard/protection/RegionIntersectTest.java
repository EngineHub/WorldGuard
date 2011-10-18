package com.sk89q.worldguard.protection;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class RegionIntersectTest {

    @Test
    @Ignore
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

        List<BlockVector2D> triangle_overlap = new ArrayList<BlockVector2D>();
        triangle_overlap.add(new BlockVector2D(90, -10));
        triangle_overlap.add(new BlockVector2D(120, -10));
        triangle_overlap.add(new BlockVector2D(90, 20));

        assertIntersection(region, new ProtectedPolygonalRegion("triangle_overlap",
                triangle_overlap, 0, 128),
                true);

        List<BlockVector2D> triangle_no_overlap = new ArrayList<BlockVector2D>();
        triangle_no_overlap.add(new BlockVector2D(90, -10));
        triangle_no_overlap.add(new BlockVector2D(105, -10));
        triangle_no_overlap.add(new BlockVector2D(90, 5));

        assertIntersection(region, new ProtectedPolygonalRegion("triangle_no_overlap",
                triangle_overlap, 0, 128),
                false);

        List<BlockVector2D> triangle_overlap_no_points = new ArrayList<BlockVector2D>();
        triangle_overlap_no_points.add(new BlockVector2D(100, -10));
        triangle_overlap_no_points.add(new BlockVector2D(120, 50));
        triangle_overlap_no_points.add(new BlockVector2D(140, -20));

        assertIntersection(region, new ProtectedPolygonalRegion("triangle_overlap_no_points",
                triangle_overlap_no_points, 60, 80),
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
}
