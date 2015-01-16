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

package com.sk89q.worldguard.protection.association;

import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Determines that the association to a region is {@code OWNER} if the input
 * region is in a set of source regions.
 */
public class RegionOverlapAssociation implements RegionAssociable {

    private final Set<ProtectedRegion> source;

    /**
     * Create a new instance.
     *
     * @param source set of regions that input regions must be contained within
     */
    public RegionOverlapAssociation(Set<ProtectedRegion> source) {
        checkNotNull(source);
        this.source = source;
    }

    @Override
    public Association getAssociation(List<ProtectedRegion> regions) {
        for (ProtectedRegion region : regions) {
            if ((region.getId().equals(ProtectedRegion.GLOBAL_REGION) && source.isEmpty()) || source.contains(region)) {
                return Association.OWNER;
            }
        }

        return Association.NON_MEMBER;
    }

}
