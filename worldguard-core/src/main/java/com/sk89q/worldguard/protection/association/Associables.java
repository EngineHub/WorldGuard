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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods to deal with associables.
 */
public final class Associables {

    private static final RegionAssociable OWNER_ASSOCIABLE = new ConstantAssociation(Association.OWNER);
    private static final RegionAssociable MEMBER_ASSOCIABLE = new ConstantAssociation(Association.MEMBER);
    private static final RegionAssociable NON_MEMBER_ASSOCIABLE = new ConstantAssociation(Association.NON_MEMBER);

    private Associables() {
    }

    /**
     * Get an instance that always returns the same association.
     *
     * @param association the association
     * @return the instance
     */
    public static RegionAssociable constant(Association association) {
        checkNotNull(association);
        switch (association) {
            case OWNER:
                return OWNER_ASSOCIABLE;
            case MEMBER:
                return MEMBER_ASSOCIABLE;
            case NON_MEMBER:
                return NON_MEMBER_ASSOCIABLE;
            default:
                return new ConstantAssociation(association);
        }
    }

}
