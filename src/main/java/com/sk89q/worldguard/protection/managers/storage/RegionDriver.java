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

package com.sk89q.worldguard.protection.managers.storage;

import java.util.List;

/**
 * A driver manages {@link RegionDatabase}s for several worlds. An instance
 * can return instances of a database for any given world.
 *
 * @see RegionDatabase
 */
public interface RegionDriver {

    /**
     * Get a region database for a world.
     *
     * <p>The given name should be a unique name for the world. Due to
     * legacy reasons, there are no stipulations on the case sensitivity
     * of the name. Historically, however, if the driver is a file-based
     * driver, case-sensitivity will vary on whether the underlying
     * filesystem is case-sensitive.</p>
     *
     * <p>This method should return quickly.</p>
     *
     * @param name the name of the world, which may be case sensitive
     * @return the world
     */
    RegionDatabase get(String name);

    /**
     * Fetch all the region databases that have been stored using this driver.
     * Essentially, return a region database for all worlds that have had
     * regions saved for it in the past.
     *
     * <p>As this may require a query to be performed, this method may block
     * for a prolonged period of time.</p>
     *
     * @return a list of databases
     * @throws StorageException thrown if the fetch operation fails
     */
    List<RegionDatabase> getAll() throws StorageException;

}
