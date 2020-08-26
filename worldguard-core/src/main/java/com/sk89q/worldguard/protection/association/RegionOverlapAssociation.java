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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Determines that the association to a region is {@code OWNER} if the input
 * region is in a set of source regions.
 */
public class RegionOverlapAssociation extends AbstractRegionOverlapAssociation {

    /**
     * Create a new instance.
     *
     * @param source set of regions that input regions must be contained within
     */
    public RegionOverlapAssociation(@Nonnull Set<ProtectedRegion> source) {
        this(source, false);
    }

    /**
     * Create a new instance.
     *
     * @param source set of regions that input regions must be contained within
     * @param useMaxPriorityAssociation whether to use the max priority from regions to determine association
     */
    public RegionOverlapAssociation(@Nonnull Set<ProtectedRegion> source, boolean useMaxPriorityAssociation) {
        super(source, useMaxPriorityAssociation);
        calcMaxPriority();
    }

}
