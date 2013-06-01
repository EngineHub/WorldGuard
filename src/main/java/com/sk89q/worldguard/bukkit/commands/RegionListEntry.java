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

package com.sk89q.worldguard.bukkit.commands;

/**
 * Used for /rg list.
 */
class RegionListEntry implements Comparable<RegionListEntry> {
    
    private final String id;
    private final int index;
    boolean isOwner;
    boolean isMember;

    public RegionListEntry(String id, int index) {
        this.id = id;
        this.index = index;
    }

    @Override
    public int compareTo(RegionListEntry o) {
        if (isOwner != o.isOwner) {
            return isOwner ? 1 : -1;
        }
        if (isMember != o.isMember) {
            return isMember ? 1 : -1;
        }
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        if (isOwner) {
            return (index + 1) + ". +" + id;
        } else if (isMember) {
            return (index + 1) + ". -" + id;
        } else {
            return (index + 1) + ". " + id;
        }
    }
    
}