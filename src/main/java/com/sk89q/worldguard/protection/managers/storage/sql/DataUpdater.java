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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.io.Closer;
import com.sk89q.worldguard.util.sql.DataSourceConfig;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

class DataUpdater {

    final Connection conn;
    final DataSourceConfig config;
    final int worldId;
    final DomainTableCache domainTableCache;

    DataUpdater(SQLRegionDatabase regionStore, Connection conn) throws SQLException {
        checkNotNull(regionStore);

        this.conn = conn;
        this.config = regionStore.getDataSourceConfig();
        this.worldId = regionStore.getWorldId();
        this.domainTableCache = new DomainTableCache(config, conn);
    }

    /**
     * Save the given set of regions to the database.
     *
     * @param regions a set of regions to save
     * @throws SQLException thrown on a fatal SQL error
     */
    public void saveAll(Set<ProtectedRegion> regions) throws SQLException {
        executeSave(regions, null);
    }

    /**
     * Save the given set of regions to the database.
     *
     * @param changed a set of changed regions
     * @param removed a set of removed regions
     * @throws SQLException thrown on a fatal SQL error
     */
    public void saveChanges(Set<ProtectedRegion> changed, Set<ProtectedRegion> removed) throws SQLException {
        executeSave(changed, removed);
    }

    /**
     * Execute the save operation.
     *
     * @param toUpdate a list of regions to update
     * @param toRemove a list of regions to remove, or {@code null} to remove
     *                 regions in the database that were not in {@code toUpdate}
     * @throws SQLException thrown on a fatal SQL error
     */
    private void executeSave(Set<ProtectedRegion> toUpdate, @Nullable Set<ProtectedRegion> toRemove) throws SQLException {
        Map<String, String> existing = getExistingRegions(); // Map of regions that already exist in the database

        // WARNING: The database uses utf8_bin for its collation, so
        // we have to remove the exact same ID (it is case-sensitive!)

        try {
            conn.setAutoCommit(false);

            RegionUpdater updater = new RegionUpdater(this);
            RegionInserter inserter = new RegionInserter(this);
            RegionRemover remover = new RegionRemover(this);

            for (ProtectedRegion region : toUpdate) {
                if (toRemove != null && toRemove.contains(region)) {
                    continue;
                }

                String currentType = existing.get(region.getId());

                // Check if the region
                if (currentType != null) { // Region exists in the database
                    existing.remove(region.getId());

                    updater.updateRegionType(region);
                    remover.removeGeometry(region, currentType);
                } else {
                    inserter.insertRegionType(region);
                }

                inserter.insertGeometry(region);
                updater.updateRegionProperties(region);
            }

            if (toRemove != null) {
                List<String> removeNames = new ArrayList<String>();
                for (ProtectedRegion region : toRemove) {
                    removeNames.add(region.getId());
                }
                remover.removeRegionsEntirely(removeNames);
            } else {
                remover.removeRegionsEntirely(existing.keySet());
            }

            remover.apply();
            inserter.apply();
            updater.apply();

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } catch (RuntimeException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }

        // Connection to be closed by caller
    }

    private Map<String, String> getExistingRegions() throws SQLException {
        Map<String, String> existing = new HashMap<String, String>();

        Closer closer = Closer.create();
        try {
            PreparedStatement stmt = closer.register(conn.prepareStatement(
                    "SELECT id, type " +
                    "FROM " + config.getTablePrefix() + "region " +
                    "WHERE world_id = " + worldId));

            ResultSet resultSet = closer.register(stmt.executeQuery());

            while (resultSet.next()) {
                existing.put(resultSet.getString("id"), resultSet.getString("type"));
            }

            return existing;
        } finally {
            closer.closeQuietly();
        }
    }

}
