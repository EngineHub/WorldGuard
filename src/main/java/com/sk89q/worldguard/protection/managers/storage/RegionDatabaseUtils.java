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

package com.sk89q.worldguard.protection.managers.storage;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class provides utility methods that may be helpful in the
 * implementation of region databases.
 *
 * @see RegionDatabase
 */
public final class RegionDatabaseUtils {

    private static final Logger log = Logger.getLogger(RegionDatabaseUtils.class.getCanonicalName());

    private RegionDatabaseUtils() {
    }

    /**
     * Try setting the given map of flags onto the region.
     *
     * @param region the region
     * @param flagData the map of flag data
     */
    public static void trySetFlagMap(ProtectedRegion region, Map<String, Object> flagData) {
        checkNotNull(region);
        checkNotNull(flagData);

        for (Flag<?> flag : DefaultFlag.getFlags()) {
            if (flag == null) {
                // Some plugins that add custom flags to WorldGuard are doing
                // something very wrong -- see WORLDGUARD-3094
                continue;
            }

            Object o = flagData.get(flag.getName());
            if (o != null) {
                RegionDatabaseUtils.trySetFlag(region, flag, o);
            }

            // Set group
            if (flag.getRegionGroupFlag() != null) {
                Object o2 = flagData.get(flag.getRegionGroupFlag().getName());
                if (o2 != null) {
                    RegionDatabaseUtils.trySetFlag(region, flag.getRegionGroupFlag(), o2);
                }
            }
        }
    }

    /**
     * Try to set a flag on the region.
     *
     * @param region the region
     * @param flag the flag
     * @param value the value of the flag, which may be {@code null}
     * @param <T> the flag's type
     * @return true if the set succeeded
     */
    public static <T> boolean trySetFlag(ProtectedRegion region, Flag<T> flag, @Nullable Object value) {
        checkNotNull(region);
        checkNotNull(flag);

        T val = flag.unmarshal(value);

        if (val != null) {
            region.setFlag(flag, val);
            return true;
        } else {
            log.warning("Failed to parse flag '" + flag.getName() + "' with value '" + value + "'");
            return false;
        }
    }

    /**
     * Re-link parent regions on each provided region using the two
     * provided maps.
     *
     * @param regions the map of regions from which parent regions are found
     * @param parentSets a mapping of region to parent name
     */
    public static void relinkParents(Map<String, ProtectedRegion> regions, Map<ProtectedRegion, String> parentSets) {
        checkNotNull(regions);
        checkNotNull(parentSets);

        for (Map.Entry<ProtectedRegion, String> entry : parentSets.entrySet()) {
            ProtectedRegion target = entry.getKey();
            ProtectedRegion parent = regions.get(entry.getValue());
            if (parent != null) {
                try {
                    target.setParent(parent);
                } catch (CircularInheritanceException e) {
                    log.warning("Circular inheritance detected! Can't set the parent of '" + target + "' to parent '" + parent.getId() + "'");
                }
            } else {
                log.warning("Unknown region parent: " + entry.getValue());
            }
        }
    }

}
