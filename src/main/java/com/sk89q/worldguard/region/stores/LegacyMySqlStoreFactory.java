// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region.stores;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * Creates new {@link LegacyMySqlStore}s, which are deprecated.
 */
@Deprecated
public class LegacyMySqlStoreFactory implements RegionStoreFactory {

    private final BoneCPConfig config;
    private BoneCP boneCp;

    /**
     * Create a new factory with the given connection pool configuration.
     *
     * @param config connection pool configuration
     */
    public LegacyMySqlStoreFactory(BoneCPConfig config) {
        this.config = config;
    }

    @Override
    public void initialize() throws IOException {
        try {
            boneCp = new BoneCP(config);
            updateSchema();
        } catch (SQLException e) {
            throw new IOException("Failed to setup database", e);
        }
    }

    @Override
    public void close() throws IOException {
        boneCp.close();
    }

    @Override
    public RegionStore getStore(String id) {
        return new LegacyMySqlStore(boneCp, id);
    }

    /**
     * Setup database tables as necessary.
     *
     * @throws SQLException on SQL error
     * @throws IOException
     */
    private void updateSchema() throws SQLException, IOException {
        Connection conn = boneCp.getConnection();

        try {
            conn.prepareStatement("SELECT * FROM region LIMIT 0, 1").execute();

            try {
                conn.prepareStatement("SELECT world_id FROM region_cuboid LIMIT 0, 1").execute();
            } catch (SQLException e) {
                // Update table
                update20110325(conn);
            }
        } catch (SQLException e) {
            // Install table
            install(conn);
        } finally {
            conn.close();
        }
    }

    /**
     * Install the latest schema.
     *
     * @param conn database connection
     * @throws SQLException on SQL error
     * @throws IOException on SQL read error
     */
    private void install(Connection conn) throws SQLException, IOException {
        conn.prepareStatement(getSql("regions_legacy_mysql.sql")).execute();
    }

    /**
     * Update the original table schema to the 2011-03-25 version.
     *
     * @param conn database connection
     * @throws SQLException on SQL error
     * @throws IOException on SQL read error
     */
    private void update20110325(Connection conn) throws SQLException, IOException {
        conn.prepareStatement(getSql("regions_legacy_mysql_20110325.sql")).execute();
    }

    /**
     * Utility method to get an SQL file form the .JAR.
     *
     * @param filename the filename
     * @return the SQL data
     * @throws IOException on read error
     */
    private static String getSql(String filename) throws IOException {
        String path = "/sql/" + filename;
        InputStream is = LegacyMySqlStoreFactory.class.getResourceAsStream(path);
        Scanner s = null;
        try {
            if (is == null) {
                throw new FileNotFoundException("Failed to get internal file: " + path);
            }
            s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

}
