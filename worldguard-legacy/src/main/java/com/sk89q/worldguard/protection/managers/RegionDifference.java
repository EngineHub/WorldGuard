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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Collections;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Describes the difference in region data.
 */
public final class RegionDifference {

    private final Set<ProtectedRegion> changed;
    private final Set<ProtectedRegion> removed;

    /**
     * Create a new instance.
     *
     * @param changed a set of regions that were changed or added
     * @param removed a set of regions that were removed
     */
    public RegionDifference(Set<ProtectedRegion> changed, Set<ProtectedRegion> removed) {
        checkNotNull(changed);
        checkNotNull(removed);

        this.changed = changed;
        this.removed = removed;
    }

    /**
     * Get the regions that were changed or added.
     *
     * @return regions
     */
    public Set<ProtectedRegion> getChanged() {
        return Collections.unmodifiableSet(changed);
    }

    /**
     * Get the regions that were removed.
     *
     * @return regions
     */
    public Set<ProtectedRegion> getRemoved() {
        return Collections.unmodifiableSet(removed);
    }

    /**
     * Test whether there are changes or removals.
     *
     * @return true if there are changes
     */
    public boolean containsChanges() {
        return !changed.isEmpty() || !removed.isEmpty();
    }

    public void addAll(RegionDifference diff) {
        changed.addAll(diff.getChanged());
        removed.addAll(diff.getRemoved());
    }

}
