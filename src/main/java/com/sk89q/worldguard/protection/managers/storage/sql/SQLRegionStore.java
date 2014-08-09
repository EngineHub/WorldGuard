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

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.sk89q.worldguard.protection.managers.RegionDifference;
import com.sk89q.worldguard.protection.managers.storage.DifferenceSaveException;
import com.sk89q.worldguard.protection.managers.storage.RegionStore;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.io.Closer;
import com.sk89q.worldguard.util.sql.DataSourceConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores region data into a SQL database in a highly normalized fashion.
 */
public class SQLRegionStore implements RegionStore {

    private static final Logger log = Logger.getLogger(SQLRegionStore.class.getCanonicalName());

    private final BoneCP connectionPool;
    private final DataSourceConfig config;
    private final int worldId;

    /**
     * Create a new instance.
     *
     * @param config a configuration object that configures a {@link Connection}
     * @param connectionPool a connection pool
     * @param worldName the name of the world to store regions by
     * @throws IOException thrown on error
     */
    public SQLRegionStore(DataSourceConfig config, BoneCP connectionPool, String worldName) throws IOException {
        checkNotNull(config);
        checkNotNull(connectionPool);
        checkNotNull(worldName);

        this.config = config;
        this.connectionPool = connectionPool;

        try {
            migrate();
        } catch (FlywayException e) {
            throw new IOException("Failed to migrate tables", e);
        } catch (SQLException e) {
            throw new IOException("Failed to migrate tables", e);
        }

        try {
            worldId = chooseWorldId(worldName);
        } catch (SQLException e) {
            throw new IOException("Failed to choose the ID for this world", e);
        }
    }

    /**
     * Attempt to migrate the tables to the latest version.
     *
     * @throws SQLException thrown on SQL errors
     */
    private void migrate() throws SQLException {
        Closer closer = Closer.create();
        Connection conn = closer.register(getConnection());

        try {
            // Check some tables
            boolean tablesExist;
            boolean isRecent;
            boolean isBeforeMigrations;
            boolean hasMigrations;

            try {
                tablesExist = tryQuery(conn, "SELECT * FROM " + config.getTablePrefix() + "region_cuboid LIMIT 1");
                isRecent = tryQuery(conn, "SELECT world_id FROM " + config.getTablePrefix() + "region_cuboid LIMIT 1");
                isBeforeMigrations = !tryQuery(conn, "SELECT uuid FROM " + config.getTablePrefix() + "user LIMIT 1");
                hasMigrations = tryQuery(conn, "SELECT * FROM " + config.getTablePrefix() + "migrations LIMIT 1");
            } finally {
                closer.closeQuietly();
            }

            // We don't bother with migrating really old tables
            if (tablesExist && !isRecent) {
                throw new SQLException(
                        "Sorry, your tables are too old for the region SQL auto-migration system. " +
                                "Please run region_manual_update_20110325.sql on your database, which comes " +
                                "with WorldGuard or can be found in http://github.com/sk89q/worldguard");
            }

            // Our placeholders
            Map<String, String> placeHolders = new HashMap<String, String>();
            placeHolders.put("tablePrefix", config.getTablePrefix());

            BoneCPConfig boneConfig = connectionPool.getConfig();

            Flyway flyway = new Flyway();

            // The SQL support predates the usage of Flyway, so let's do some
            // checks and issue messages appropriately
            if (!hasMigrations) {
                flyway.setInitOnMigrate(true);

                if (tablesExist) {
                    // Detect if this is before migrations
                    if (isBeforeMigrations) {
                        flyway.setInitVersion(MigrationVersion.fromVersion("1"));
                    }

                    log.log(Level.INFO, "The SQL region tables exist but the migrations table seems to not exist yet. Creating the migrations table...");
                } else {
                    // By default, if Flyway sees any tables at all in the schema, it
                    // will assume that we are up to date, so we have to manually
                    // check ourselves and then ask Flyway to start from the beginning
                    // if our test table doesn't exist
                    flyway.setInitVersion(MigrationVersion.fromVersion("0"));

                    log.log(Level.INFO, "SQL region tables do not exist: creating...");
                }
            }

            flyway.setClassLoader(getClass().getClassLoader());
            flyway.setLocations("migrations/region/" + getMigrationFolderName());
            flyway.setDataSource(boneConfig.getJdbcUrl(), boneConfig.getUser(), boneConfig.getPassword());
            flyway.setTable(config.getTablePrefix() + "migrations");
            flyway.setPlaceholders(placeHolders);
            flyway.setValidateOnMigrate(false);
            flyway.migrate();
        } finally {
            closer.closeQuietly();
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
        return connectionPool.getConnection();
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
     * Try to execute a query and return true if it did not fail.
     *
     * @param conn the connection to run the query on
     * @param sql the SQL query
     * @return true if the query did not end in error
     */
    private boolean tryQuery(Connection conn, String sql) {
        Closer closer = Closer.create();
        try {
            Statement statement = closer.register(conn.createStatement());
            statement.executeQuery(sql);
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            closer.closeQuietly();
        }
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

    /**
     * Get the name of the folder in migrations/region containing the migration files.
     *
     * @return the migration folder name
     */
    public String getMigrationFolderName() {
        return "mysql";
    }

    @Override
    public Set<ProtectedRegion> loadAll() throws IOException {
        Closer closer = Closer.create();
        DataLoader loader;

        try {
            try {
                loader = new DataLoader(this, closer.register(getConnection()));
            } catch (SQLException e) {
                throw new IOException("Failed to get a connection to the database", e);
            }

            try {
                return loader.load();
            } catch (SQLException e) {
                throw new IOException("Failed to save the region data to the database", e);
            }
        } finally {
            closer.closeQuietly();
        }
    }

    @Override
    public void saveAll(Set<ProtectedRegion> regions) throws IOException {
        checkNotNull(regions);

        Closer closer = Closer.create();
        DataUpdater updater;

        try {
            try {
                updater = new DataUpdater(this, closer.register(getConnection()));
            } catch (SQLException e) {
                throw new IOException("Failed to get a connection to the database", e);
            }

            try {
                updater.saveAll(regions);
            } catch (SQLException e) {
                throw new IOException("Failed to save the region data to the database", e);
            }
        } finally {
            closer.closeQuietly();
        }
    }

    @Override
    public void saveChanges(RegionDifference difference) throws DifferenceSaveException, IOException {
        checkNotNull(difference);

        Closer closer = Closer.create();
        DataUpdater updater;

        try {
            try {
                updater = new DataUpdater(this, closer.register(getConnection()));
            } catch (SQLException e) {
                throw new IOException("Failed to get a connection to the database", e);
            }

            try {
                updater.saveChanges(difference.getChanged(), difference.getRemoved());
            } catch (SQLException e) {
                throw new IOException("Failed to save the region data to the database", e);
            }
        } finally {
            closer.closeQuietly();
        }
    }
}
