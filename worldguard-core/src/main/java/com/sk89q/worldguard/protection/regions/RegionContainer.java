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

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.protection.managers.RegionContainerImpl;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    public abstract void reload();

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

}
