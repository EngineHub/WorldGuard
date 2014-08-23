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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles migration from one region store driver to another.
 */
public class DriverMigration extends AbstractMigration {

    private static final Logger log = Logger.getLogger(DriverMigration.class.getCanonicalName());
    private final RegionDriver target;

    /**
     * Create a new instance.
     *
     * @param driver the source storage driver
     * @param target the target storage driver
     */
    public DriverMigration(RegionDriver driver, RegionDriver target) {
        super(driver);
        checkNotNull(target);
        this.target = target;
    }

    @Override
    protected void migrate(RegionDatabase store) throws MigrationException {
        Set<ProtectedRegion> regions;

        log.info("Loading the regions for '" + store.getName() + "' with the old driver...");

        try {
            regions = store.loadAll();
        } catch (StorageException e) {
            throw new MigrationException("Failed to load region data for the world '" + store.getName() + "'", e);
        }

        write(store.getName(), regions);
    }

    private void write(String name, Set<ProtectedRegion> regions) throws MigrationException {
        log.info("Saving the data for '" + name + "' with the new driver...");

        RegionDatabase store = target.get(name);

        try {
            store.saveAll(regions);
        } catch (StorageException e) {
            throw new MigrationException("Failed to save region data for '" + store.getName() + "' to the new driver", e);
        }
    }

    @Override
    protected void postMigration() {
    }

}
