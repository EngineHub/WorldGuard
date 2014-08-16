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

package com.sk89q.worldguard.internal.util.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class StatementUtils {

    private StatementUtils() {
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
