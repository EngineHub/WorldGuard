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

package com.sk89q.worldguard.protection.managers;

import com.google.common.base.Supplier;
import com.sk89q.worldguard.protection.managers.index.ChunkHashTable;
import com.sk89q.worldguard.protection.managers.index.ConcurrentRegionIndex;
import com.sk89q.worldguard.protection.managers.index.PriorityRTreeIndex;
import com.sk89q.worldguard.protection.managers.storage.RegionStore;
import com.sk89q.worldguard.protection.managers.storage.driver.RegionStoreDriver;
import com.sk89q.worldguard.util.Normal;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manages different {@link RegionManager}s for different worlds or dimensions.
 */
public class ManagerContainer {

    private static final Logger log = Logger.getLogger(ManagerContainer.class.getCanonicalName());
    private static final int SAVE_INTERVAL = 1000 * 30;

    private final ConcurrentMap<Normal, RegionManager> mapping = new ConcurrentHashMap<Normal, RegionManager>();
    private final Object lock = new Object();
    private final RegionStoreDriver driver;
    private final Supplier<? extends ConcurrentRegionIndex> indexFactory = new ChunkHashTable.Factory(new PriorityRTreeIndex.Factory());
    private final Timer timer = new Timer();

    /**
     * Create a new instance.
     *
     * @param driver the region store driver
     */
    public ManagerContainer(RegionStoreDriver driver) {
        checkNotNull(driver);
        this.driver = driver;
        timer.schedule(new BackgroundSaver(), SAVE_INTERVAL, SAVE_INTERVAL);
    }

    /**
     * Get the region store driver.
     *
     * @return the driver
     */
    public RegionStoreDriver getDriver() {
        return driver;
    }

    /**
     * Load the {@code RegionManager} for the world with the given name,
     * creating a new instance for the world if one does not exist yet.
     *
     * @param name the name of the world
     * @return a region manager, or {@code null} if loading failed
     */
    @Nullable
    public RegionManager load(String name) {
        checkNotNull(name);

        Normal normal = Normal.normal(name);

        synchronized (lock) {
            RegionManager manager = mapping.get(normal);
            if (manager != null) {
                return manager;
            } else {
                try {
                    manager = createAndLoad(name);
                    mapping.put(normal, manager);
                    return manager;
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to load the region data for '" + name + "'", e);
                    return null;
                }
            }
        }
    }

    /**
     * Create a new region manager and load the data.
     *
     * @param name the name of the world
     * @return a region manager
     * @throws IOException thrown if loading fals
     */
    private RegionManager createAndLoad(String name) throws IOException {
        RegionStore store = driver.get(name);
        RegionManager manager = new RegionManager(store, indexFactory);
        manager.load(); // Try loading, although it may fail
        return manager;
    }

    /**
     * Unload the region manager associated with the given world name.
     *
     * <p>If no region manager has been loaded for the given name, then
     * nothing will happen.</p>
     *
     * @param name the name of the world
     */
    public void unload(String name) {
        checkNotNull(name);

        Normal normal = Normal.normal(name);

        synchronized (lock) {
            RegionManager manager = mapping.get(normal);
            if (manager != null) {
                try {
                    manager.save();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to save the region data for '" + name + "'", e);
                }

                mapping.remove(normal);
            }
        }
    }

    /**
     * Unload all region managers and save their contents before returning.
     * This message may block for an extended period of time.
     */
    public void unloadAll() {
        synchronized (lock) {
            for (Map.Entry<Normal, RegionManager> entry : mapping.entrySet()) {
                String name = entry.getKey().toString();
                RegionManager manager = entry.getValue();
                try {
                    manager.saveChanges();
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to save the region data for '" + name + "' while unloading the data for all worlds", e);
                }
            }

            mapping.clear();
        }
    }

    /**
     * Get the region manager for the given world name.
     *
     * @param name the name of the world
     * @return a region manager, or {@code null} if one was never loaded
     */
    @Nullable
    public RegionManager get(String name) {
        checkNotNull(name);
        return mapping.get(Normal.normal(name));
    }

    /**
     * Get an immutable list of loaded region managers.
     *
     * @return an immutable list
     */
    public List<RegionManager> getLoaded() {
        return Collections.unmodifiableList(new ArrayList<RegionManager>(mapping.values()));
    }

    /**
     * A task to save managers in the background.
     */
    private class BackgroundSaver extends TimerTask {
        @Override
        public void run() {
            synchronized (lock) {
                // Block loading of new region managers

                for (Map.Entry<Normal, RegionManager> entry : mapping.entrySet()) {
                    String name = entry.getKey().toString();
                    RegionManager manager = entry.getValue();
                    try {
                        if (manager.saveChanges()) {
                            log.info("Region data changes made in '" + name + "' have been background saved");
                        }
                    } catch (IOException e) {
                        log.log(Level.WARNING, "Failed to save the region data for '" + name + "' during a periodical save", e);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "An expected error occurred during a periodical save", e);
                    }
                }
            }
        }
    }

}
