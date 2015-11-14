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

import com.sk89q.worldguard.protection.managers.RegionDifference;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A region database that saves the memory to an in-memory {@link HashSet}.
 *
 * <p>This implementation is thread-safe. Difference saves
 * are not supported.</p>
 */
public class MemoryRegionDatabase implements RegionDatabase {

    private Set<ProtectedRegion> regions = Collections.emptySet();

    @Override
    public String getName() {
        return "MEMORY";
    }

    @Override
    public Set<ProtectedRegion> loadAll() {
        return regions;
    }

    @Override
    public void saveAll(Set<ProtectedRegion> regions) {
        this.regions = Collections.unmodifiableSet(new HashSet<ProtectedRegion>(regions));
    }

    @Override
    public void saveChanges(RegionDifference difference) throws DifferenceSaveException {
        throw new DifferenceSaveException();
    }

}
