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

package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Set;

/**
 * Represents the effective set of flags, owners, and members for a given
 * spatial query.
 *
 * <p>An instance of this can be created using the spatial query methods
 * available on {@link RegionManager}.</p>
 */
public interface ApplicableRegionSet extends FlagQuery, Iterable<ProtectedRegion> {

    /**
     * Return whether this region set is a virtual set. A virtual set
     * does not contain real results.
     *
     * <p>A virtual result may be returned if region data failed to load or
     * there was some special exception (i.e. the region bypass permission).
     * </p>
     *
     * <p>Be sure to check the value of this flag if an instance of this
     * interface is being retrieved from RegionQuery as it may
     * return an instance of {@link PermissiveRegionSet} or
     * {@link FailedLoadRegionSet}, among other possibilities.</p>
     *
     * @return true if loaded
     * @see FailedLoadRegionSet
     */
    boolean isVirtual();

    /**
     * Test whether a player is an owner of all regions in this set.
     *
     * @param player the player
     * @return whether the player is an owner of all regions
     */
    boolean isOwnerOfAll(LocalPlayer player);

    /**
     * Test whether a player is an owner or member of all regions in this set.
     *
     * @param player the player
     * @return whether the player is a member of all regions
     */
    boolean isMemberOfAll(LocalPlayer player);

    /**
     * Get the number of regions that are included.
     *
     * @return the number of contained regions
     */
    int size();

    /**
     * Get an immutable set of regions that are included in this set.
     *
     * @return a set of regions
     */
    Set<ProtectedRegion> getRegions();

}
