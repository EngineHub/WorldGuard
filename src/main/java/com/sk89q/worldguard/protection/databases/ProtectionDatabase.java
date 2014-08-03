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

package com.sk89q.worldguard.protection.databases;

import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * Represents a database to read and write lists of regions from and to.
 */
public interface ProtectionDatabase {

    /**
     * Load the list of regions. The method should not modify the list returned
     * by getRegions() unless the load finishes successfully.
     *
     * @throws ProtectionDatabaseException when an error occurs
     */
    public void load() throws ProtectionDatabaseException, RejectedExecutionException;

    /**
     * Load the list of regions into a region manager.
     *
     * <p>This call will block.</p>
     *
     * @param manager The manager to load regions into
     * @throws ProtectionDatabaseException when an error occurs
     */
    public void load(RegionManager manager) throws ProtectionDatabaseException, RejectedExecutionException;

    /**
     * Load the list of regions into a region manager, optionally to suggest
     * that the data be load in another thread without blocking the thread
     * from which the call is made.
     *
     * <p>{@code async} is merely a suggestion and it may be ignored by
     * implementations if it is not supported.</p>
     *
     * @param manager The manager to load regions into
     * @param async true to attempt to save the data asynchronously if it is supported
     */
    public ListenableFuture<?> load(RegionManager manager, boolean async) throws RejectedExecutionException;

    /**
     * Save the list of regions.
     *
     * @throws ProtectionDatabaseException when an error occurs
     */
    public void save() throws ProtectionDatabaseException, RejectedExecutionException;

    /**
     * Save the list of regions from a region manager.
     *
     * <p>This call will block.</p>
     *
     * @param manager The manager to load regions into
     * @throws ProtectionDatabaseException when an error occurs
     */
    public void save(RegionManager manager) throws ProtectionDatabaseException, RejectedExecutionException;

    /**
     * Save the list of regions from a region manager, optionally to suggest
     * that the data be saved in another thread without blocking the thread
     * from which the call is made.
     *
     * <p>{@code async} is merely a suggestion and it may be ignored by
     * implementations if it is not supported.</p>
     *
     * @param manager The manager to load regions into
     * @param async true to attempt to save the data asynchronously if it is supported
     * @throws RejectedExecutionException on rejection
     */
    public ListenableFuture<?> save(RegionManager manager, boolean async) throws RejectedExecutionException;

    /**
     * Get a list of regions.
     *
     * @return the regions loaded by this ProtectionDatabase
     */
    public Map<String,ProtectedRegion> getRegions();

    /**
     * Set the list of regions.
     *
     * @param regions The regions to be applied to this ProtectionDatabase
     */
    public void setRegions(Map<String,ProtectedRegion> regions);

}
