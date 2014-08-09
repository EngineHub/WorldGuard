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

package com.sk89q.worldguard.protection.managers.storage.driver;

import com.jolbox.bonecp.BoneCP;
import com.sk89q.worldguard.protection.managers.storage.RegionStore;
import com.sk89q.worldguard.protection.managers.storage.sql.SQLRegionStore;
import com.sk89q.worldguard.util.io.Closer;
import com.sk89q.worldguard.util.sql.DataSourceConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores regions using {@link SQLRegionStore}.
 */
public class SQLDriver implements RegionStoreDriver {

    private final DataSourceConfig config;
    private final Object lock = new Object();
    private BoneCP connectionPool;

    /**
     * Create a new instance.
     *
     * @param config a configuration
     */
    public SQLDriver(DataSourceConfig config) {
        checkNotNull(config);
        this.config = config;
    }

    /**
     * Get an instance of the connection pool.
     *
     * @return the connection pool
     * @throws SQLException occurs when the connection pool can't be created
     */
    protected BoneCP getConnectionPool() throws SQLException {
        synchronized (lock) {
            if (connectionPool == null) {
                connectionPool = new BoneCP(config.createBoneCPConfig());
            }

            return connectionPool;
        }
    }

    @Override
    public RegionStore get(String name) throws IOException {
        try {
            return new SQLRegionStore(config, getConnectionPool(), name);
        } catch (SQLException e) {
            throw new IOException("Failed to get a connection pool for storing regions (are the SQL details correct?)");
        }
    }

    @Override
    public List<String> fetchAllExisting() throws IOException {
        Closer closer = Closer.create();
        try {
            List<String> names = new ArrayList<String>();
            Connection connection = closer.register(getConnectionPool().getConnection());
            Statement stmt = connection.createStatement();
            ResultSet rs = closer.register(stmt.executeQuery("SELECT name FROM world"));
            while (rs.next()) {
                names.add(rs.getString(1));
            }
            return names;
        } catch (SQLException e) {
            throw new IOException("Failed to fetch list of worlds", e);
        } finally {
            closer.close();
        }
    }

}
