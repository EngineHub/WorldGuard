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

package com.sk89q.worldguard.protection.managers.migration;

import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.StorageException;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract implementation of a migrator that gets all the worlds in
 * a driver and calls a override-able {@code migrate()} method for
 * each store.
 */
abstract class AbstractMigration implements Migration {

    private static final Logger log = Logger.getLogger(AbstractMigration.class.getCanonicalName());
    private final RegionDriver driver;

    /**
     * Create a new instance.
     *
     * @param driver the storage driver
     */
    public AbstractMigration(RegionDriver driver) {
        checkNotNull(driver);

        this.driver = driver;
    }

    @Override
    public final void migrate() throws MigrationException {
        try {
            for (RegionDatabase store : driver.getAll()) {
                try {
                    migrate(store);
                } catch (MigrationException e) {
                    log.log(Level.WARNING, "Migration of one world (" + store.getName() + ") failed with an error", e);
                }
            }

            postMigration();
        } catch (StorageException e) {
            throw new MigrationException("Migration failed because the process of getting a list of all the worlds to migrate failed", e);
        }
    }

    /**
     * Called for all the worlds in the driver.
     *
     * @param store the region store
     * @throws MigrationException on migration error
     */
    protected abstract void migrate(RegionDatabase store)throws MigrationException;

    /**
     * Called after migration has successfully completed.
     */
    protected abstract void postMigration();

}
