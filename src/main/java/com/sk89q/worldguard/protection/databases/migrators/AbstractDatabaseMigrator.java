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

package com.sk89q.worldguard.protection.databases.migrators;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sk89q.worldguard.protection.databases.ProtectionDatabase;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class AbstractDatabaseMigrator implements DatabaseMigrator {

    private static HashMap<MigratorKey, Class<? extends AbstractDatabaseMigrator>> migrators =
            new HashMap<MigratorKey, Class<? extends AbstractDatabaseMigrator>>();

    public static Map<MigratorKey, Class<? extends AbstractDatabaseMigrator>> getMigrators() {
        if (!migrators.isEmpty()) return migrators;

        AbstractDatabaseMigrator.migrators.put(new MigratorKey("mysql", "yaml"), MySQLToYAMLMigrator.class);
        AbstractDatabaseMigrator.migrators.put(new MigratorKey("yaml", "mysql"), YAMLToMySQLMigrator.class);

        return migrators;
    }

    protected abstract Set<String> getWorldsFromOld() throws MigrationException;

    protected abstract Map<String, ProtectedRegion> getRegionsForWorldFromOld(String world) throws MigrationException;

    protected abstract ProtectionDatabase getNewWorldStorage(String world) throws MigrationException;

    public void migrate() throws MigrationException {
        for (String world : this.getWorldsFromOld()) {
            ProtectionDatabase database = this.getNewWorldStorage(world);
            database.setRegions(this.getRegionsForWorldFromOld(world));

            try {
                database.save();
            } catch (ProtectionDatabaseException e) {
                throw new MigrationException(e);
            }
        }
    }
}
