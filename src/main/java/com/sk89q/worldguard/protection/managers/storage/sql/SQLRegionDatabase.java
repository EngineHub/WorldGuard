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

package com.sk89q.worldguard.protection.managers.storage.sql;

import com.sk89q.worldguard.protection.managers.RegionDifference;
import com.sk89q.worldguard.protection.managers.storage.DifferenceSaveException;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.io.Closer;
import com.sk89q.worldguard.util.sql.DataSourceConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores region data into a SQL database in a highly normalized fashion.
 */
class SQLRegionDatabase implements RegionDatabase {

    private final String worldName;
    private final DataSourceConfig config;
    private final SQLDriver driver;
    private int worldId;
    private boolean initialized = false;

    /**
     * Create a new instance.
     *
     * @param driver the driver instance
     * @param worldName the name of the world to store regions by
     */
    SQLRegionDatabase(SQLDriver driver, String worldName) {
        checkNotNull(driver);
        checkNotNull(worldName);

        this.config = driver.getConfig();
        this.worldName = worldName;
        this.driver = driver;
    }

    @Override
    public String getName() {
        return worldName;
    }

    /**
     * Initialize the database if it hasn't been yet initialized.
     *
     * @throws StorageException thrown if initialization fails
     */
    private synchronized void initialize() throws StorageException {
        if (!initialized) {
            driver.initialize();

            try {
                worldId = chooseWorldId(worldName);
            } catch (SQLException e) {
                throw new StorageException("Failed to choose the ID for this world", e);
            }

            initialized = true;
        }
    }

    /**
     * Get the ID for this world from the database or pick a new one if
     * an entry does not exist yet.
     *
     * @param worldName the world name
     * @return a world ID
     * @throws SQLException on a database access error
     */
    private int chooseWorldId(String worldName) throws SQLException {
        Closer closer = Closer.create();
        try {
            Connection conn = closer.register(getConnection());

            PreparedStatement stmt = closer.register(conn.prepareStatement(
                    "SELECT id FROM " + config.getTablePrefix() + "world WHERE name = ? LIMIT 0, 1"));

            stmt.setString(1, worldName);
            ResultSet worldResult = closer.register(stmt.executeQuery());

            if (worldResult.next()) {
                return worldResult.getInt("id");
            } else {
                PreparedStatement stmt2 = closer.register(conn.prepareStatement(
                        "INSERT INTO " + config.getTablePrefix() + "world  (id, name) VALUES (null, ?)",
                        Statement.RETURN_GENERATED_KEYS));

                stmt2.setString(1, worldName);
                stmt2.execute();
                ResultSet generatedKeys = stmt2.getGeneratedKeys();

                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Expected result, got none");
                }
            }
        } finally {
            closer.closeQuietly();
        }
    }

    /**
     * Return a new database connection.
     *
     * @return a connection
     * @throws SQLException thrown if the connection could not be created
     */
    private Connection getConnection() throws SQLException {
        return driver.getConnection();
    }

    /**
     * Get the data source config.
     *
     * @return the data source config
     */
    public DataSourceConfig getDataSourceConfig() {
        return config;
    }

    /**
     * Get the world ID.
     *
     * @return the world ID
     */
    public int getWorldId() {
        return worldId;
    }

    /**
     * Get the identifier string for a region's type.
     *
     * @param region the region
     * @return the ID of the region type
     */
    static String getRegionTypeName(ProtectedRegion region) {
        if (region instanceof ProtectedCuboidRegion) {
            return "cuboid";
        } else if (region instanceof ProtectedPolygonalRegion) {
            return "poly2d"; // Differs from getTypeName() on ProtectedRegion
        } else if (region instanceof GlobalProtectedRegion) {
            return "global";
        } else {
            throw new IllegalArgumentException("Unexpected region type: " + region.getClass().getName());
        }
    }

    /**
     * Create a YAML dumper / parser.
     *
     * @return a YAML dumper / parser
     */
    static Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(FlowStyle.FLOW);
        Representer representer = new Representer();
        representer.setDefaultFlowStyle(FlowStyle.FLOW);

        // We have to use this in order to properly save non-string values
        return new Yaml(new SafeConstructor(), new Representer(), options);
    }

    @Override
    public Set<ProtectedRegion> loadAll() throws StorageException {
        initialize();

        Closer closer = Closer.create();
        DataLoader loader;

        try {
            try {
                loader = new DataLoader(this, closer.register(getConnection()));
            } catch (SQLException e) {
                throw new StorageException("Failed to get a connection to the database", e);
            }

            try {
                return loader.load();
            } catch (SQLException e) {
                throw new StorageException("Failed to save the region data to the database", e);
            }
        } finally {
            closer.closeQuietly();
        }
    }

    @Override
    public void saveAll(Set<ProtectedRegion> regions) throws StorageException {
        checkNotNull(regions);

        initialize();

        Closer closer = Closer.create();
        DataUpdater updater;

        try {
            try {
                updater = new DataUpdater(this, closer.register(getConnection()));
            } catch (SQLException e) {
                throw new StorageException("Failed to get a connection to the database", e);
            }

            try {
                updater.saveAll(regions);
            } catch (SQLException e) {
                throw new StorageException("Failed to save the region data to the database", e);
            }
        } finally {
            closer.closeQuietly();
        }
    }

    @Override
    public void saveChanges(RegionDifference difference) throws DifferenceSaveException, StorageException {
        checkNotNull(difference);

        initialize();

        Closer closer = Closer.create();
        DataUpdater updater;

        try {
            try {
                updater = new DataUpdater(this, closer.register(getConnection()));
            } catch (SQLException e) {
                throw new StorageException("Failed to get a connection to the database", e);
            }

            try {
                updater.saveChanges(difference.getChanged(), difference.getRemoved());
            } catch (SQLException e) {
                throw new StorageException("Failed to save the region data to the database", e);
            }
        } finally {
            closer.closeQuietly();
        }
    }

}
