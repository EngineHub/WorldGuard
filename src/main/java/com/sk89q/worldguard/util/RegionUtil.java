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

package com.sk89q.worldguard.util;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;

/**
 * Various utility functions for regions.
 * 
 * @author sk89q
 */
@Deprecated
public class RegionUtil {
    
    private RegionUtil() {
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain The domain
     * @param split The arguments
     * @param startIndex The index to start at
     * @deprecated see {@link RegionDBUtil#addToDomain(com.sk89q.worldguard.domains.DefaultDomain, String[], int)}
     */
    @Deprecated
    public static void addToDomain(DefaultDomain domain, String[] split,
            int startIndex) {
        RegionDBUtil.addToDomain(domain, split, startIndex);
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain The domain to add to
     * @param split The arguments
     * @param startIndex The index to start at
     * @deprecated see {@link RegionDBUtil#removeFromDomain(com.sk89q.worldguard.domains.DefaultDomain, String[], int)}
     */
    @Deprecated
    public static void removeFromDomain(DefaultDomain domain, String[] split,
            int startIndex) {
        RegionDBUtil.removeFromDomain(domain, split, startIndex);
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     *
     * @param split The arguments
     * @param startIndex The index to start at
     * @deprecated see {@link RegionDBUtil#parseDomainString(String[], int)}
     * @return the parsed domain
     */
    @Deprecated
    public static DefaultDomain parseDomainString(String[] split, int startIndex) {
        return RegionDBUtil.parseDomainString(split, startIndex);
    }
}
