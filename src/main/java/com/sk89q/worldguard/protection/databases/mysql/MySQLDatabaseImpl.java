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

package com.sk89q.worldguard.protection.databases.mysql;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.protection.databases.AbstractAsynchronousDatabase;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.io.Closer;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * For internal use. Do not subclass.
 */
public class MySQLDatabaseImpl extends AbstractAsynchronousDatabase {

    private final ConfigurationManager config;
    private final Logger logger;

    private final BoneCP connectionPool;
    private final Yaml yaml = createYaml();
    private final int worldId;

    private Map<String, ProtectedRegion> regions = new HashMap<String, ProtectedRegion>();

    public MySQLDatabaseImpl(ConfigurationManager config, String worldName, Logger logger) throws ProtectionDatabaseException {
        checkNotNull(config);
        checkNotNull(worldName);
        checkNotNull(logger);
        
        this.config = config;
        this.logger = logger;

        BoneCPConfig poolConfig = new BoneCPConfig();
        poolConfig.setJdbcUrl(config.sqlDsn);
        poolConfig.setUsername(config.sqlUsername);
        poolConfig.setPassword(config.sqlPassword);

        try {
            connectionPool = new BoneCP(poolConfig);
        } catch (SQLException e) {
            throw new ProtectionDatabaseException("Failed to connect to the database", e);
        }

        try {
            migrate();
        } catch (FlywayException e) {
            throw new ProtectionDatabaseException("Failed to migrate tables", e);
        } catch (SQLException e) {
            throw new ProtectionDatabaseException("Failed to migrate tables", e);
        }

        try {
            worldId = chooseWorldId(worldName);
        } catch (SQLException e) {
            throw new ProtectionDatabaseException("Failed to choose the ID for this world", e);
        }
    }

    private boolean tryQuery(Connection conn, String sql) throws SQLException {
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
     * Migrate the tables to the latest version.
     *
     * @throws SQLException thrown if a connection can't be opened
     * @throws ProtectionDatabaseException thrown on other error
     */
    private void migrate() throws SQLException, ProtectionDatabaseException {
        Closer closer = Closer.create();
        Connection conn = closer.register(getConnection());

        // Check some tables
        boolean tablesExist;
        boolean isRecent;
        boolean isBeforeMigrations;
        boolean hasMigrations;

        try {
            tablesExist = tryQuery(conn, "SELECT * FROM `" + config.sqlTablePrefix + "region_cuboid` LIMIT 1");
            isRecent = tryQuery(conn, "SELECT `world_id` FROM `" + config.sqlTablePrefix + "region_cuboid` LIMIT 1");
            isBeforeMigrations = !tryQuery(conn, "SELECT `uuid` FROM `" + config.sqlTablePrefix + "user` LIMIT 1");
            hasMigrations = tryQuery(conn, "SELECT * FROM `" + config.sqlTablePrefix + "migrations` LIMIT 1");
        } finally {
            closer.closeQuietly();
        }

        // We don't bother with migrating really old tables
        if (tablesExist && !isRecent) {
            throw new ProtectionDatabaseException(
                    "Sorry, your tables are too old for the region SQL auto-migration system. " +
                    "Please run region_manual_update_20110325.sql on your database, which comes " +
                    "with WorldGuard or can be found in http://github.com/sk89q/worldguard");
        }

        // Our placeholders
        Map<String, String> placeHolders = new HashMap<String, String>();
        placeHolders.put("tablePrefix", config.sqlTablePrefix);

        BoneCPConfig boneConfig = connectionPool.getConfig();

        Flyway flyway = new Flyway();

        // The MySQL support predates the usage of Flyway, so let's do some
        // checks and issue messages appropriately
        if (!hasMigrations) {
            flyway.setInitOnMigrate(true);

            if (tablesExist) {
                // Detect if this is before migrations
                if (isBeforeMigrations) {
                    flyway.setInitVersion(MigrationVersion.fromVersion("1"));
                }

                logger.log(Level.INFO, "The MySQL region tables exist but the migrations table seems to not exist yet. Creating the migrations table...");
            } else {
                // By default, if Flyway sees any tables at all in the schema, it
                // will assume that we are up to date, so we have to manually
                // check ourselves and then ask Flyway to start from the beginning
                // if our test table doesn't exist
                flyway.setInitVersion(MigrationVersion.fromVersion("0"));

                logger.log(Level.INFO, "MySQL region tables do not exist: creating...");
            }
        }

        flyway.setClassLoader(getClass().getClassLoader());
        flyway.setLocations("migrations/region/mysql");
        flyway.setDataSource(boneConfig.getJdbcUrl(), boneConfig.getUser(), boneConfig.getPassword());
        flyway.setTable(config.sqlTablePrefix + "migrations");
        flyway.setPlaceholders(placeHolders);
        flyway.setValidateOnMigrate(false);
        flyway.migrate();
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
            PreparedStatement worldStmt = closer.register(conn.prepareStatement(
                    "SELECT `id` FROM `" + config.sqlTablePrefix + "world` WHERE `name` = ? LIMIT 0, 1"
            ));

            worldStmt.setString(1, worldName);
            ResultSet worldResult = closer.register(worldStmt.executeQuery());

            if (worldResult.first()) {
                return worldResult.getInt("id");
            } else {
                PreparedStatement insertWorldStatement = closer.register(conn.prepareStatement(
                        "INSERT INTO " +
                                "`" + config.sqlTablePrefix + "world` " +
                                "(`id`, `name`) VALUES (null, ?)",
                        Statement.RETURN_GENERATED_KEYS
                ));

                insertWorldStatement.setString(1, worldName);
                insertWorldStatement.execute();
                ResultSet generatedKeys = insertWorldStatement.getGeneratedKeys();

                if (generatedKeys.first()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Expected result, got none");
                }
            }
        } finally {
            closer.closeQuietly();
        }
    }

    private static Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(FlowStyle.FLOW);
        Representer representer = new Representer();
        representer.setDefaultFlowStyle(FlowStyle.FLOW);

        // We have to use this in order to properly save non-string values
        return new Yaml(new SafeConstructor(), new Representer(), options);
    }

    ConfigurationManager getConfiguration() {
        return this.config;
    }

    Logger getLogger() {
        return logger;
    }

    Yaml getYaml() {
        return yaml;
    }

    int getWorldId() {
        return worldId;
    }

    private Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    @Override
    public Map<String, ProtectedRegion> getRegions() {
        return regions;
    }

    @Override
    public void setRegions(Map<String, ProtectedRegion> regions) {
        this.regions = regions;
    }

    @Override
    protected void performLoad() throws ProtectionDatabaseException {
        Connection connection = null;
        try {
            connection = getConnection();
            setRegions(new RegionLoader(this, connection).load());
        } catch (SQLException e) {
            throw new ProtectionDatabaseException("Failed to load regions database", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Override
    protected void performSave() throws ProtectionDatabaseException {
        Connection connection = null;
        try {
            connection = getConnection();
            new RegionWriter(this, connection, getRegions()).save();
        } catch (SQLException e) {
            throw new ProtectionDatabaseException("Failed to save regions database", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

}
