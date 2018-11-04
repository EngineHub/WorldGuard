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

package com.sk89q.worldguard.bukkit;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.migration.Migration;
import com.sk89q.worldguard.protection.managers.migration.MigrationException;
import com.sk89q.worldguard.protection.managers.migration.UUIDMigration;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

public class BukkitRegionContainer extends RegionContainer {

    private static final Logger log = Logger.getLogger(BukkitRegionContainer.class.getCanonicalName());

    /**
     * Invalidation frequency in ticks.
     */
    private static final int CACHE_INVALIDATION_INTERVAL = 2;

    private final WorldGuardPlugin plugin;

    /**
     * Create a new instance.
     *
     * @param plugin the plugin
     */
    public BukkitRegionContainer(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        super.initialize();

        loadWorlds();

        // Migrate to UUIDs
        autoMigrate();

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onWorldLoad(WorldLoadEvent event) {
                load(event.getWorld());
            }

            @EventHandler
            public void onWorldUnload(WorldUnloadEvent event) {
                unload(event.getWorld());
            }

            @EventHandler
            public void onChunkLoad(ChunkLoadEvent event) {
                RegionManager manager = get(BukkitAdapter.adapt(event.getWorld()));
                if (manager != null) {
                    Chunk chunk = event.getChunk();
                    manager.loadChunk(BlockVector2.at(chunk.getX(), chunk.getZ()));
                }
            }

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent event) {
                RegionManager manager = get(BukkitAdapter.adapt(event.getWorld()));
                if (manager != null) {
                    Chunk chunk = event.getChunk();
                    manager.unloadChunk(BlockVector2.at(chunk.getX(), chunk.getZ()));
                }
            }
        }, plugin);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, cache::invalidateAll, CACHE_INVALIDATION_INTERVAL, CACHE_INVALIDATION_INTERVAL);
    }

    /**
     * Save data and unload.
     */
    void unload() {
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
     * Try loading the region managers for all currently loaded worlds.
     */
    private void loadWorlds() {
        synchronized (lock) {
            for (World world : Bukkit.getServer().getWorlds()) {
                load(world);
            }
        }
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
     * Load the region data for a world if it has not been loaded already.
     *
     * @param world the world
     * @return a region manager, either returned from the cache or newly loaded
     */
    @Nullable
    private RegionManager load(World world) {
        checkNotNull(world);

        BukkitWorldConfiguration config =
                (BukkitWorldConfiguration) WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world));
        if (!config.useRegions) {
            return null;
        }

        RegionManager manager;

        synchronized (lock) {
            manager = container.load(world.getName());

            if (manager != null) {
                // Bias the region data for loaded chunks
                List<BlockVector2> positions = new ArrayList<>();
                for (Chunk chunk : world.getLoadedChunks()) {
                    positions.add(BlockVector2.at(chunk.getX(), chunk.getZ()));
                }
                manager.loadChunks(positions);
            }
        }

        return manager;
    }

    /**
     * Unload the region data for a world.
     *
     * @param world a world
     */
    void unload(World world) {
        checkNotNull(world);

        synchronized (lock) {
            container.unload(world.getName());
        }
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
                log.info("Unloading and saving region data that is currently loaded...");
                unload();
                migration.migrate();
            } finally {
                log.info("Loading region data for loaded worlds...");
                loadWorlds();
            }
        }
    }

    /**
     * Execute auto-migration.
     */
    private void autoMigrate() {
        ConfigurationManager config = WorldGuard.getInstance().getPlatform().getGlobalStateManager();

        if (config.migrateRegionsToUuid) {
            RegionDriver driver = getDriver();
            UUIDMigration migrator = new UUIDMigration(driver, WorldGuard.getInstance().getProfileService(), WorldGuard.getInstance().getFlagRegistry());
            migrator.setKeepUnresolvedNames(config.keepUnresolvedNames);
            try {
                migrate(migrator);

                log.info("Regions saved after UUID migration! This won't happen again unless " +
                        "you change the relevant configuration option in WorldGuard's config.");

                config.disableUuidMigration();
            } catch (MigrationException e) {
                log.log(Level.WARNING, "Failed to execute the migration", e);
            }
        }
    }

}
