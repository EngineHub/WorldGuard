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

package com.sk89q.worldguard.bukkit.protection;

import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Determines that the association to a region is {@code OWNER} if the input
 * region is in a set of source regions.
 *
 * <p>This class only performs a spatial query if its
 * {@link #getAssociation(List)} method is called.</p>
 */
public class DelayedRegionOverlapAssociation implements RegionAssociable {

    private final RegionQuery query;
    private final Location location;
    @Nullable
    private Set<ProtectedRegion> source;

    /**
     * Create a new instance.
     *
     * @param query the query
     * @param location the location
     */
    public DelayedRegionOverlapAssociation(RegionQuery query, Location location) {
        checkNotNull(query);
        checkNotNull(location);
        this.query = query;
        this.location = location;
    }

    @Override
    public Association getAssociation(List<ProtectedRegion> regions) {
        if (source == null) {
            ApplicableRegionSet result = query.getApplicableRegions(location);
            source = result.getRegions();
        }

        for (ProtectedRegion region : regions) {
            if ((region.getId().equals(ProtectedRegion.GLOBAL_REGION) && source.isEmpty()) || source.contains(region)) {
                return Association.OWNER;
            }
        }

        return Association.NON_MEMBER;
    }

}
