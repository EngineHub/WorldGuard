// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.protection;

import java.util.Comparator;

class DescendingPriorityComparator implements Comparator<ProtectedRegion> {
    /**
     * Compares two regions.
     * 
     * @param other
     * @return
     */
    public int compare(ProtectedRegion r1, ProtectedRegion r2) {
        if (r1.getPriority() == r2.getPriority()) {
            return 0;
        } else if (r1.getPriority() > r2.getPriority()) {
            return -1;
        } else {
            return 1;
        }
    }
}
