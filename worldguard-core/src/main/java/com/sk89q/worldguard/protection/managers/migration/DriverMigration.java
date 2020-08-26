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

import com.google.common.base.Preconditions;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
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
    private final FlagRegistry flagRegistry;

    /**
     * Create a new instance.
     *
     * @param driver the source storage driver
     * @param target the target storage driver
     * @param flagRegistry the flag registry
     */
    public DriverMigration(RegionDriver driver, RegionDriver target, FlagRegistry flagRegistry) {
        super(driver);
        checkNotNull(target);
        Preconditions.checkNotNull(flagRegistry, "flagRegistry");
        this.target = target;
        this.flagRegistry = flagRegistry;
    }

    @Override
    protected void migrate(RegionDatabase store) throws MigrationException {
        Set<ProtectedRegion> regions;

        log.info("Загрузка региона для '" + store.getName() + "' со старым драйвером...");

        try {
            regions = store.loadAll(flagRegistry);
        } catch (StorageException e) {
            throw new MigrationException("Не удалось загрузить данные области для мира '" + store.getName() + "'", e);
        }

        write(store.getName(), regions);
    }

    private void write(String name, Set<ProtectedRegion> regions) throws MigrationException {
        log.info("Сохранение данных для '" + name + "' с новым драйвером...");

        RegionDatabase store = target.get(name);

        try {
            store.saveAll(regions);
        } catch (StorageException e) {
            throw new MigrationException("Не удалось сохранить данные региона для '" + store.getName() + "' к новому драйверу", e);
        }
    }

    @Override
    protected void postMigration() {
    }

}
