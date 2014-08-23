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

package com.sk89q.worldguard.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Describes a data source.
 */
public class DataSourceConfig {

    private final String dsn;
    private final String username;
    private final String password;
    private final String tablePrefix;

    /**
     * Create a new instance.
     *
     * @param dsn the DSN
     * @param username the username
     * @param password the password
     * @param tablePrefix the table prefix
     */
    public DataSourceConfig(String dsn, String username, String password, String tablePrefix) {
        checkNotNull(dsn);
        checkNotNull(username);
        checkNotNull(password);
        checkNotNull(tablePrefix);

        this.dsn = dsn;
        this.username = username;
        this.password = password;
        this.tablePrefix = tablePrefix;
    }

    /**
     * Get the DSN.
     *
     * @return the DSN
     */
    public String getDsn() {
        return dsn;
    }

    /**
     * Get the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the table prefix.
     *
     * @return the table prefix
     */
    public String getTablePrefix() {
        return tablePrefix;
    }

    /**
     * Create a new instance with a new DSN.
     *
     * @param dsn a new DSN string
     * @return a new instance
     */
    public DataSourceConfig setDsn(String dsn) {
        return new DataSourceConfig(dsn, username, password, tablePrefix);
    }

    /**
     * Create a new instance with a new username.
     *
     * @param username a new username
     * @return a new instance
     */
    public DataSourceConfig setUsername(String username) {
        return new DataSourceConfig(dsn, username, password, tablePrefix);
    }

    /**
     * Create a new instance with a new password.
     *
     * @param password a new password
     * @return a new instance
     */
    public DataSourceConfig setPassword(String password) {
        return new DataSourceConfig(dsn, username, password, tablePrefix);
    }

    /**
     * Create a new instance with a new table prefix.
     *
     * @param tablePrefix the new table prefix
     * @return a new instance
     */
    public DataSourceConfig setTablePrefix(String tablePrefix) {
        return new DataSourceConfig(dsn, username, password, tablePrefix);
    }

    /**
     * Create a new connection.
     *
     * @return the new connection
     * @throws SQLException raised if the connection cannot be instantiated
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dsn, username, password);
    }

}
