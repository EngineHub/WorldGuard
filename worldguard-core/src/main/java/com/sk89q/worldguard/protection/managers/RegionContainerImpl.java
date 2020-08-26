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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.index.ChunkHashTable;
import com.sk89q.worldguard.protection.managers.index.ConcurrentRegionIndex;
import com.sk89q.worldguard.protection.managers.index.PriorityRTreeIndex;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.util.Normal;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages different {@link RegionManager}s for different worlds or dimensions.
 *
 * <p>This is an internal class. Do not use it.</p>
 */
public class RegionContainerImpl {

    private static final Logger log = Logger.getLogger(RegionContainerImpl.class.getCanonicalName());
    private static final int LOAD_ATTEMPT_INTERVAL = 1000 * 30;
    private static final int SAVE_INTERVAL = 1000 * 30;

    private final ConcurrentMap<Normal, RegionManager> mapping = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private final RegionDriver driver;
    private final Function<String, ? extends ConcurrentRegionIndex> indexFactory = new ChunkHashTable.Factory(new PriorityRTreeIndex.Factory());
    private final Timer timer = new Timer("WorldGuard Region I/O");
    private final FlagRegistry flagRegistry;

    private final Set<Normal> failingLoads = new HashSet<>();
    private final Set<RegionManager> failingSaves = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));

    /**
     * Create a new instance.
     *
     * @param driver the region store driver
     * @param flagRegistry the flag registry
     */
    public RegionContainerImpl(RegionDriver driver, FlagRegistry flagRegistry) {
        checkNotNull(driver);
        checkNotNull(flagRegistry, "flagRegistry");
        this.driver = driver;
        timer.schedule(new BackgroundLoader(), LOAD_ATTEMPT_INTERVAL, LOAD_ATTEMPT_INTERVAL);
        timer.schedule(new BackgroundSaver(), SAVE_INTERVAL, SAVE_INTERVAL);
        this.flagRegistry = flagRegistry;
    }

    /**
     * Get the region store driver.
     *
     * @return the driver
     */
    public RegionDriver getDriver() {
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
                    failingLoads.remove(normal);
                    return manager;
                } catch (StorageException e) {
                    log.log(Level.WARNING, "Не удалось загрузить данные региона для '" + name + "' (периодические попытки загрузки данных будут выполняться до тех пор, пока не увенчаются успехом)", e);
                    failingLoads.add(normal);
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
     * @throws StorageException thrown if loading fals
     */
    private RegionManager createAndLoad(String name) throws StorageException {
        RegionDatabase store = driver.get(name);
        RegionManager manager = new RegionManager(store, indexFactory, flagRegistry);
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
                } catch (StorageException e) {
                    log.log(Level.WARNING, "Не удалось сохранить данные региона для '" + name + "'", e);
                }

                mapping.remove(normal);
                failingSaves.remove(manager);
            }

            failingLoads.remove(normal);
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
                } catch (StorageException e) {
                    log.log(Level.WARNING, "Не удалось сохранить данные региона для '" + name + "' при выгрузке данных для всех миров", e);
                }
            }

            mapping.clear();
            failingLoads.clear();
            failingSaves.clear();
        }
    }

    /**
     * Disable completely.
     */
    public void shutdown() {
        timer.cancel();
        unloadAll();
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
        return Collections.unmodifiableList(new ArrayList<>(mapping.values()));
    }

    /**
     * Get the a set of region managers that are failing to save.
     *
     * @return a set of region managers
     */
    public Set<RegionManager> getSaveFailures() {
        return new HashSet<>(failingSaves);
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
                            log.info("Данные области изменения, внесенные в '" + name + "' были сохранены фоновые данные");
                        }
                        failingSaves.remove(manager);
                    } catch (StorageException e) {
                        failingSaves.add(manager);
                        log.log(Level.WARNING, "Не удалось сохранить данные региона для '" + name + "' во время периодического сохранения", e);
                    } catch (Exception e) {
                        failingSaves.add(manager);
                        log.log(Level.WARNING, "Произошла ошибка во время периодического сохранения", e);
                    }
                }
            }
        }
    }

    /**
     * A task to re-try loading region data that has not yet been
     * successfully loaded.
     */
    private class BackgroundLoader extends TimerTask {
        private String lastMsg;

        @Override
        public void run() {
            synchronized (lock) {
                if (!failingLoads.isEmpty()) {
                    log.info("Попытка загрузки данных региона, которые ранее не были загружены...");

                    Iterator<Normal> it = failingLoads.iterator();
                    while (it.hasNext()) {
                        Normal normal = it.next();
                        try {
                            RegionManager manager = createAndLoad(normal.toString());
                            mapping.put(normal, manager);
                            it.remove();
                            log.info("Данные региона для '" + normal.toString() + "' успешно загруженны");
                        } catch (StorageException e) {
                            if (e.getCause() != null && e.getCause().getMessage().equals(lastMsg)) {
                                // if it's the same error, don't print a whole stacktrace
                                log.log(Level.WARNING, "Данные по регионам по-прежнему не загружаются, по крайней мере для всего мира '" + normal.toString() + "'");
                                break;
                            }
                            log.log(Level.WARNING, "Данные по регионам по-прежнему не загружаются, по крайней мере для всего мира '" + normal.toString() + "'", e);
                            lastMsg = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
                            break;
                        }
                    }
                }
            }
        }
    }

}
