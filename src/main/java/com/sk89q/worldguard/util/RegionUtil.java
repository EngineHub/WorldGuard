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

package com.sk89q.worldguard.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sk89q.worldguard.domains.DefaultDomain;

/**
 * Various utility functions for regions.
 * 
 * @author sk89q
 */
public class RegionUtil {
    private static Pattern groupPattern = Pattern.compile("^[gG]:(.+)$");
    
    private RegionUtil() {
        
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain
     * @param split
     * @param startIndex
     */
    public static void addToDomain(DefaultDomain domain, String[] split,
            int startIndex) {
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain
     * @param split
     * @param startIndex
     */
    public static void removeFromDomain(DefaultDomain domain, String[] split,
            int startIndex) {
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.removeGroup(m.group(1));
            } else {
                domain.removePlayer(s);
            }
        }
    }

    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param split
     * @param startIndex
     * @return
     */
    public static DefaultDomain parseDomainString(String[] split, int startIndex) {
        DefaultDomain domain = new DefaultDomain();

        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }

        return domain;
    }
}
