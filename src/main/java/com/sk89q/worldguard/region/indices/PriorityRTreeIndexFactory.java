// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region.indices;

/**
 * Creates new {@link PriorityRTreeIndex} instances.
 */
public class PriorityRTreeIndexFactory implements RegionIndexFactory {

    private int branchFactor = 30;

    /**
     * Construct a new {@link PriorityRTreeIndex} factory with a branch factor of 30.
     */
    public PriorityRTreeIndexFactory() {
    }

    /**
     * Construct a new {@link PriorityRTreeIndex} factory with a given branch factor.
     *
     * @param branchFactor branch factor
     */
    public PriorityRTreeIndexFactory(int branchFactor) {
        this.branchFactor = branchFactor;
    }

    @Override
    public RegionIndex newIndex() {
        return new PriorityRTreeIndex(branchFactor);
    }

}
