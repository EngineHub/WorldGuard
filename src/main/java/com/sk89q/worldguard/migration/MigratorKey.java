// $Id$
/*
 * MySQL WordGuard Region Database
 * Copyright (C) 2011 Nicholas Steicke <http://narthollis.net>
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

package com.sk89q.worldguard.migration;

public class MigratorKey {
    public final String from;
    public final String to;

    public MigratorKey(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public boolean equals(Object o) {
        MigratorKey other = (MigratorKey) o;

        return other.from.equals(this.from) && other.to.equals(this.to);
    }

    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + this.from.hashCode();
        hash = hash * 31 + this.to.hashCode();
        return hash;
    }
}