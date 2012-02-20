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

package com.sk89q.worldguard.util;

public class ArrayReader<T> {
    private T[] arr;
    
    public ArrayReader(T[] arr) {
        this.arr = arr;
    }

    /**
     * Gets an element from the array
     * @param index The index to get an element at
     * @return The element at {@code index} if the index is within bounds, otherwise null
     */
    public T get(int index) {
        return get(index, null);
    }

    /**
     * Gets an element from the array
     * @param index The index to get an element at
     * @param def the default value, used when the given index is out of bounds
     * @return The element at {@code index} if the index is within bounds, otherwise the default value
     */
    public T get(int index, T def) {
        if (index >= 0 && arr.length > index) {
            return arr[index];
        } else {
            return def;
        }
    }
}
