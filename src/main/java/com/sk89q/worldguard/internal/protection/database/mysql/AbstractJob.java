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

package com.sk89q.worldguard.internal.protection.database.mysql;

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import org.yaml.snakeyaml.error.YAMLException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("ProtectedField")
abstract class AbstractJob {

    protected final MySQLDatabaseImpl database;
    protected final ConfigurationManager config;
    protected final Connection conn;
    protected final Logger logger;

    protected AbstractJob(MySQLDatabaseImpl database, Connection conn) {
        checkNotNull(database);
        checkNotNull(conn);
        this.database = database;
        this.config = database.getConfiguration();
        this.conn = conn;
        this.logger = database.getLogger();
    }

    static void closeQuietly(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignored) {}
        }
    }

    static void closeQuietly(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException ignored) {}
        }
    }

    protected Object sqlUnmarshal(String rawValue) {
        try {
            return database.getYaml().load(rawValue);
        } catch (YAMLException e) {
            return String.valueOf(rawValue);
        }
    }

    protected String sqlMarshal(Object rawObject) {
        return database.getYaml().dump(rawObject);
    }

}
