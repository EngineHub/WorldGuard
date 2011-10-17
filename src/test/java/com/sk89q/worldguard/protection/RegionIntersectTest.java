package com.sk89q.worldguard.protection;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
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
                new BlockVector(80, 40, -20), new BlockVector(120, 128, 20)));

        assertIntersection(region, new ProtectedCuboidRegion("small",
                new BlockVector(98, 45, 20), new BlockVector(103, 50, 25)));

        assertIntersection(region, new ProtectedCuboidRegion("large",
                new BlockVector(-500, 0, -600), new BlockVector(1000, 128, 1000)));

        assertIntersection(region, new ProtectedCuboidRegion("short",
                new BlockVector(50, 40, -1), new BlockVector(150, 128, 2)));

        assertIntersection(region, new ProtectedCuboidRegion("long",
                new BlockVector(0, 40, 5), new BlockVector(1000, 128, 8)));
    }

    private void assertIntersection(ProtectedRegion region1, ProtectedRegion region2) {
        boolean isIntersectingRegion = false;
        List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
        regions.add(region2);

        try {
            isIntersectingRegion = (region1.getIntersectingRegions(regions).size() == 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue("Check for '" + region2.getId() + "' region failed.", isIntersectingRegion);
    }
}
