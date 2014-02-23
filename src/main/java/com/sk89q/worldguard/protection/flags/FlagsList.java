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

package com.sk89q.worldguard.protection.flags;

import java.util.ArrayList;

/**
 * extends {@literal ArrayList<Flag<?>>}
 *
 */
@SuppressWarnings("serial")
public class FlagsList extends ArrayList<Flag<?>> {

    /**
     * Constructs an empty flags list
     */
    public FlagsList() {
        super();
    }

    /**
     * Constructs a list of flags from {@code flags} array
     * 
     * @param flags array of type {@literal Flag<?>}
     */
    public FlagsList(Flag<?>[] flags) {
        super(flags.length);
        for (Flag<?> flag : flags) {
            this.add(flag);
        }
    }

    /**
     * Returns <tt>true</tt> if this list contains a flag with a name matching the specified {@code name}.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one flag named: {@code name} <tt>e</tt> such that
     * <tt>(name==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;name.equals(e.getName()))</tt>.
     *
     * @param name name of a flag element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains a flag by the name of {@code name}
     */
    public boolean contains(String name) {
        if (name == null) {
            return false;
        }
        for (Flag<?> e : this) {
            return name.equals(e.getName());
        }
        return false;
    }

    /**
     * Removes the first occurrence of a flag named: {@code name} from this list,
     * if it is present.  If the list does not contain a flag named: {@code name}, it is
     * unchanged.  More formally, removes the flag named: {@code name} with the lowest index
     * <tt>i</tt> such that
     * <tt>(name==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;name.equals(get(i).getName()))</tt>
     * (if such flag exists).  Returns <tt>true</tt> if this list
     * contained the specified flag named: {@code name} (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param name name of flag element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained a flag named: {@code name}
     */
    public boolean remove(String name) {
        for (Flag<?> e : this) {
            if (name.equals(e.getName())) {
                return this.remove(e);
            }
        }
        return false;
    }
}
