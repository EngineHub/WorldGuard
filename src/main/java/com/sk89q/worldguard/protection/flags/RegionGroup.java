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

package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.domains.Association;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A grouping of region membership types.
 */
public enum RegionGroup {

    MEMBERS(Association.MEMBER, Association.OWNER),
    OWNERS(Association.OWNER),
    NON_MEMBERS(Association.NON_MEMBER),
    NON_OWNERS(Association.MEMBER, Association.NON_MEMBER),
    ALL(Association.OWNER, Association.MEMBER, Association.NON_MEMBER),
    NONE();

    private final Set<Association> contained;

    RegionGroup(Association... association) {
        this.contained = association.length > 0 ? EnumSet.copyOf(Arrays.asList(association)) : EnumSet.noneOf(Association.class);
    }

    /**
     * Test whether this group contains the given membership status.
     *
     * @param association membership status
     * @return true if contained
     */
    public boolean contains(Association association) {
        checkNotNull(association);
        return contained.contains(association);
    }

}
