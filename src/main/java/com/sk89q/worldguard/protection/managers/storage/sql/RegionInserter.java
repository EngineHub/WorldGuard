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

import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.io.Closer;
import com.sk89q.worldguard.util.sql.DataSourceConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Insert regions that don't exist in the database yet.
 */
class RegionInserter {

    private final DataSourceConfig config;
    private final Connection conn;
    private final int worldId;
    private final List<ProtectedRegion> all = new ArrayList<ProtectedRegion>();
    private final List<ProtectedCuboidRegion> cuboids = new ArrayList<ProtectedCuboidRegion>();
    private final List<ProtectedPolygonalRegion> polygons = new ArrayList<ProtectedPolygonalRegion>();

    RegionInserter(DataUpdater updater) {
        this.config = updater.config;
        this.conn = updater.conn;
        this.worldId = updater.worldId;
    }

    public void insertRegionType(ProtectedRegion region) throws SQLException {
        all.add(region);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void insertGeometry(ProtectedRegion region) throws SQLException {
        if (region instanceof ProtectedCuboidRegion) {
            cuboids.add((ProtectedCuboidRegion) region);

        } else if (region instanceof ProtectedPolygonalRegion) {
            polygons.add((ProtectedPolygonalRegion) region);

        } else if (region instanceof GlobalProtectedRegion) {
            // Nothing special to do about them

        } else {
            throw new IllegalArgumentException("Unknown type of region: " + region.getClass().getName());
        }
    }

    private void insertRegionTypes() throws SQLException {
        Closer closer = Closer.create();
        try {
            PreparedStatement stmt = closer.register(conn.prepareStatement(
                    "INSERT INTO " + config.getTablePrefix() + "region " +
                    "(id, world_id, type, priority, parent) " +
                    "VALUES " +
                    "(?, ?, ?, ?, NULL)"));

            for (List<ProtectedRegion> partition : Lists.partition(all, StatementBatch.MAX_BATCH_SIZE)) {
                for (ProtectedRegion region : partition) {
                    stmt.setString(1, region.getId());
                    stmt.setInt(2, worldId);
                    stmt.setString(3, SQLRegionDatabase.getRegionTypeName(region));
                    stmt.setInt(4, region.getPriority());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }
        } finally {
            closer.closeQuietly();
        }
    }

    private void insertCuboids() throws SQLException {
        Closer closer = Closer.create();
        try {
            PreparedStatement stmt = closer.register(conn.prepareStatement(
                    "INSERT INTO " + config.getTablePrefix() + "region_cuboid " +
                    "(region_id, world_id, min_z, min_y, min_x, max_z, max_y, max_x ) " +
                    "VALUES " +
                    "(?, " + worldId + ", ?, ?, ?, ?, ?, ?)"));

            for (List<ProtectedCuboidRegion> partition : Lists.partition(cuboids, StatementBatch.MAX_BATCH_SIZE)) {
                for (ProtectedCuboidRegion region : partition) {
                    BlockVector min = region.getMinimumPoint();
                    BlockVector max = region.getMaximumPoint();

                    stmt.setString(1, region.getId());
                    stmt.setInt(2, min.getBlockZ());
                    stmt.setInt(3, min.getBlockY());
                    stmt.setInt(4, min.getBlockX());
                    stmt.setInt(5, max.getBlockZ());
                    stmt.setInt(6, max.getBlockY());
                    stmt.setInt(7, max.getBlockX());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }
        } finally {
            closer.closeQuietly();
        }
    }

    private void insertPolygons() throws SQLException {
        Closer closer = Closer.create();
        try {
            PreparedStatement stmt = closer.register(conn.prepareStatement(
                    "INSERT INTO " + config.getTablePrefix() + "region_poly2d " +
                    "(region_id, world_id, max_y, min_y) " +
                    "VALUES " +
                    "(?, " + worldId + ", ?, ?)"));

            for (List<ProtectedPolygonalRegion> partition : Lists.partition(polygons, StatementBatch.MAX_BATCH_SIZE)) {
                for (ProtectedPolygonalRegion region : partition) {
                    stmt.setString(1, region.getId());
                    stmt.setInt(2, region.getMaximumPoint().getBlockY());
                    stmt.setInt(3, region.getMinimumPoint().getBlockY());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }
        } finally {
            closer.closeQuietly();
        }
    }

    private void insertPolygonVertices() throws SQLException {
        Closer closer = Closer.create();
        try {
            PreparedStatement stmt = closer.register(conn.prepareStatement(
                    "INSERT INTO " + config.getTablePrefix() + "region_poly2d_point" +
                    "(region_id, world_id, z, x) " +
                    "VALUES " +
                    "(?, " + worldId + ", ?, ?)"));

            StatementBatch batch = new StatementBatch(stmt, StatementBatch.MAX_BATCH_SIZE);

            for (ProtectedPolygonalRegion region : polygons) {
                for (BlockVector2D point : region.getPoints()) {
                    stmt.setString(1, region.getId());
                    stmt.setInt(2, point.getBlockZ());
                    stmt.setInt(3, point.getBlockX());
                    batch.addBatch();
                }
            }

            batch.executeRemaining();
        } finally {
            closer.closeQuietly();
        }
    }

    public void apply() throws SQLException {
        insertRegionTypes();
        insertCuboids();
        insertPolygons();
        insertPolygonVertices();
    }

}
