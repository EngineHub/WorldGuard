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

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.protection.databases.AbstractAsynchronousDatabase;
import com.sk89q.worldguard.protection.databases.InvalidTableFormatException;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
            worldId = chooseWorldId(worldName);
        } catch (SQLException e) {
            throw new ProtectionDatabaseException("Failed to choose the ID for this world", e);
        }
    }

    private int chooseWorldId(String worldName) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement worldStmt = null;
        ResultSet worldResult = null;
        PreparedStatement insertWorldStatement = null;

        try {
            PreparedStatement verTest = null;
            try {
                // Test if the database is up to date, if not throw a critical error
                verTest = conn.prepareStatement("SELECT `world_id` FROM `" + config.sqlTablePrefix + "region_cuboid` LIMIT 0, 1;");
                verTest.execute();
            } catch (SQLException ex) {
                throw new InvalidTableFormatException("region_storage_update_20110325.sql");
            } finally {
                AbstractJob.closeQuietly(verTest);
            }

            worldStmt = conn.prepareStatement(
                    "SELECT `id` FROM `" + config.sqlTablePrefix + "world` WHERE `name` = ? LIMIT 0, 1"
            );

            worldStmt.setString(1, worldName);
            worldResult = worldStmt.executeQuery();

            if (worldResult.first()) {
                return worldResult.getInt("id");
            } else {
                insertWorldStatement = conn.prepareStatement(
                        "INSERT INTO " +
                                "`" + config.sqlTablePrefix + "world` " +
                                "(`id`, `name`) VALUES (null, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );

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
            AbstractJob.closeQuietly(worldResult);
            AbstractJob.closeQuietly(worldStmt);
            AbstractJob.closeQuietly(insertWorldStatement);
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
            setRegions(new RegionLoader(this, connection, regions).call());
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
            new RegionCommitter(this, connection, getRegions()).call();
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
