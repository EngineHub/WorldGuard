package com.sk89q.worldguard.protection.util;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldEditRegionConverter {
    /**
     * Converts a ProtectedRegion to a WorldEdit Region, otherwise null if
     * the ProtectedRegion can't be converted to a RegionSelector.
     *
     * @param region the WorldGuard region
     * @return the WorldEdit Region
     */
    public static Region convertToRegion(ProtectedRegion region) {
        if (region instanceof ProtectedCuboidRegion) {
            return new CuboidRegion(null, region.getMinimumPoint(), region.getMaximumPoint());
        }
        if (region instanceof ProtectedPolygonalRegion) {
            return new Polygonal2DRegion(null, region.getPoints(),
                    region.getMinimumPoint().getY(), region.getMaximumPoint().getY());
        }
        return null;
    }

    /**
     * Converts a ProtectedRegion to a WorldEdit RegionSelector, otherwise null if
     * the ProtectedRegion can't be converted to a RegionSelector.
     *
     * @param region the WorldGuard region
     * @return the WorldEdit Region
     */
    public static RegionSelector convertToSelector(ProtectedRegion region) {
        if (region instanceof ProtectedCuboidRegion) {
            return new CuboidRegionSelector(null, region.getMinimumPoint(), region.getMaximumPoint());
        }
        if (region instanceof ProtectedPolygonalRegion) {
            return new Polygonal2DRegionSelector(null, region.getPoints(),
                    region.getMinimumPoint().getY(), region.getMaximumPoint().getY());
        }
        return null;
    }
}
