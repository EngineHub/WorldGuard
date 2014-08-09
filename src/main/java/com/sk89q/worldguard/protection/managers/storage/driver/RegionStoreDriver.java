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

package com.sk89q.worldguard.protection.managers.storage.driver;

import com.sk89q.worldguard.protection.managers.storage.RegionStore;

import java.io.IOException;
import java.util.List;

/**
 * A driver is able to create new {@code RegionStore}s for named worlds.
 */
public interface RegionStoreDriver {

    /**
     * Get a region store for the named world.
     *
     * @param name the name
     * @return the world
     * @throws IOException thrown if the region store can't be created due to an I/O error
     */
    RegionStore get(String name) throws IOException;

    /**
     * Fetch the names of all worlds that are stored with this driver.
     *
     * @return a list of names
     * @throws IOException thrown if the fetch operation fails
     */
    List<String> fetchAllExisting() throws IOException;

}
