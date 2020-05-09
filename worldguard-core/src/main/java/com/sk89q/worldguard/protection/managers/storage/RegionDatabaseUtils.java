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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

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
                    log.warning("Обнаружено циклическое наследование! Не удается установить родителя из '" + target + "' для родителя '" + parent.getId() + "'");
                }
            } else {
                log.warning("Неизвестный родитель региона: " + entry.getValue());
            }
        }
    }

}
