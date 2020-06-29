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

package com.sk89q.worldguard.protection.regions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.protection.managers.RegionContainerImpl;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.migration.Migration;
import com.sk89q.worldguard.protection.managers.migration.MigrationException;
import com.sk89q.worldguard.protection.managers.migration.UUIDMigration;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Nullable;

/**
 * A region container creates {@link RegionManager}s for loaded worlds, which
 * allows access to the region data of a world. Generally, only data is
 * loaded for worlds that are loaded in the server.
 *
 * <p>This class is thread safe and its contents can be accessed from
 * multiple concurrent threads.</p>
 */
public abstract class RegionContainer {

    protected final Object lock = new Object();
    protected final QueryCache cache = new QueryCache();
    protected RegionContainerImpl container;

    /**
     * Initialize the region container.
     */
    public void initialize() {
        ConfigurationManager config = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        container = new RegionContainerImpl(config.selectedRegionStoreDriver, WorldGuard.getInstance().getFlagRegistry());

        loadWorlds();

        // Migrate to UUIDs
        autoMigrate();
    }

    /**
     * Save data and unload.
     */
    public void unload() {
        synchronized (lock) {
            container.unloadAll();
        }
    }

    /**
     * Get the region store driver.
     *
     * @return the driver
     */
    public RegionDriver getDriver() {
        return container.getDriver();
    }

    /**
     * Reload the region container.
     *
     * <p>This method may block until the data for all loaded worlds has been
     * unloaded and new data has been loaded.</p>
     */
    public void reload() {
        synchronized (lock) {
            unload();
            loadWorlds();
        }
    }

    /**
     * Get the region manager for a world if one exists.
     *
     * <p>If you wish to make queries and performance is more important
     * than accuracy, use {@link #createQuery()} instead.</p>
     *
     * <p>This method may return {@code null} if region data for the given
     * world has not been loaded, has failed to load, or support for regions
     * has been disabled.</p>
     *
     * @param world the world
     * @return a region manager, or {@code null} if one is not available
     */
    @Nullable
    public RegionManager get(World world) {
        return container.get(world.getName());
    }

    /**
     * Get an immutable list of loaded {@link RegionManager}s.
     *
     * @return a list of managers
     */
    public List<RegionManager> getLoaded() {
        return Collections.unmodifiableList(container.getLoaded());
    }

    /**
     * Get the a set of region managers that are failing to save.
     *
     * @return a set of region managers
     */
    public Set<RegionManager> getSaveFailures() {
        return container.getSaveFailures();
    }

    /**
     * Create a new region query.
     *
     * @return a new query
     */
    public RegionQuery createQuery() {
        return new RegionQuery(cache);
    }

    /**
     * Execute a migration and block any loading of region data during
     * the migration.
     *
     * @param migration the migration
     * @throws MigrationException thrown by the migration on error
     */
    public void migrate(Migration migration) throws MigrationException {
        checkNotNull(migration);

        synchronized (lock) {
            try {
                WorldGuard.logger.info("Выгрузка и сохранение данных региона, который в настоящее время загружен...");
                unload();
                migration.migrate();
            } finally {
                WorldGuard.logger.info("Загрузка данных региона для загруженных миров...");
                loadWorlds();
            }
        }
    }

    /**
     * Try loading the region managers for all currently loaded worlds.
     */
    protected void loadWorlds() {
        WorldGuard.logger.info("Загрузка данных региона...");
        synchronized (lock) {
            for (World world : WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds()) {
                load(world);
            }
        }
    }

    /**
     * Unload the region data for a world.
     *
     * @param world a world
     */
    public void unload(World world) {
        checkNotNull(world);

        synchronized (lock) {
            container.unload(world.getName());
        }
    }

    /**
     * Execute auto-migration.
     */
    protected void autoMigrate() {
        ConfigurationManager config = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        if (config.migrateRegionsToUuid) {
            RegionDriver driver = getDriver();
            UUIDMigration migrator = new UUIDMigration(driver, WorldGuard.getInstance().getProfileService(), WorldGuard.getInstance().getFlagRegistry());
            migrator.setKeepUnresolvedNames(config.keepUnresolvedNames);
            try {
                migrate(migrator);

                WorldGuard.logger.info("Регионы сохранены после миграции UUID! Это не повторится, если " +
                        "изменить соответствующий параметр конфигурации WorldGuard.");

                config.disableUuidMigration();
            } catch (MigrationException e) {
                WorldGuard.logger.log(Level.WARNING, "Не удалось выполнить миграцию", e);
            }
        }
    }

    /**
     * Load the region data for a world if it has not been loaded already.
     *
     * @param world the world
     * @return a region manager, either returned from the cache or newly loaded
     */
    @Nullable protected abstract RegionManager load(World world);
}
