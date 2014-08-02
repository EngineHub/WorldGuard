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

package com.sk89q.worldguard.protection.databases;

import com.sk89q.worldguard.domains.DefaultDomain;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utility functions for parsing region databases.
 * 
 * @author sk89q
 */
public class RegionDBUtil {

    private static Pattern groupPattern = Pattern.compile("(?i)^[G]:(.+)$");
    
    private RegionDBUtil() {
        
    }

    /**
     * Add the given names to {@code domain}
     * 
     * @param domain The domain to add to
     * @param split The {@link String[]} containing names to add to {@code domain}
     * @param startIndex The beginning index in the array
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
     * Remove the given names from {@code domain}
     *
     * @param domain The domain to remove from
     * @param split The {@link String[]} containing names to remove from {@code domain}
     * @param startIndex The beginning index in the array
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
     * @param split The array of names to add
     * @param startIndex The beginning index in the array
     * @return The resulting DefaultDomain
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
    
    /**
     * Creates a comma separated list of PreparedStatement place holders
     * 
     * @param length The number of wildcards to create
     * @return A string with {@code length} wildcards for usage in a PreparedStatement
     */
    public static String preparePlaceHolders(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length;) {
            builder.append("?");
            if (++i < length) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    /**
     * Adds all of the parsed values to the PreparedStatement
     * 
     * @param preparedStatement The preparedStanement to add to
     * @param values The values to set
     * @throws SQLException see {@link PreparedStatement#setString(int, String)}
     */
    public static void setValues(PreparedStatement preparedStatement, String... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setString(i + 1, values[i]);
        }
    }
}
