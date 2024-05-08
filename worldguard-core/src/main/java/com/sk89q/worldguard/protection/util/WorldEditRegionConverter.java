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

/**
 * A helper class to convert regions from WorldGuard to WorldEdit
 */
public final class WorldEditRegionConverter {
    private WorldEditRegionConverter() {

    }

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
                    region.getMinimumPoint().y(), region.getMaximumPoint().y());
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
                    region.getMinimumPoint().y(), region.getMaximumPoint().y());
        }
        return null;
    }
}
