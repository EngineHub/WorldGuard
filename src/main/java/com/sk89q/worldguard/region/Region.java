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

package com.sk89q.worldguard.region;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.sk89q.worldguard.region.flags.Flag;
import com.sk89q.worldguard.region.shapes.IndexableShape;

/**
 * A region that has an ID, shape, priority, parent, and flags assigned to it. It is
 * used for defining areas on a world.
 * <p>
 * The casing of region IDs are maintained, but they are case insensitive for the
 * purposes of indexing.
 * <p>
 * If changes are made to this region, and it is contained within a region
 * index, the index must be notified of the change.
 */
public abstract class Region implements Comparable<Region> {

    private final String id;
    private IndexableShape shape;
    private int priority = 0;
    private Region parent;
    private Map<Flag<?>, Object> flags = new HashMap<Flag<?>, Object>();

    /**
     * Construct a new instance of this region.
     *
     * @param id the ID of the region
     * @param shape the shape of the region
     */
    public Region(String id, IndexableShape shape) {
        Validate.notNull(id, "Region ID cannot be null");
        Validate.notNull(shape, "Shape parameter cannot be null");

        this.id = id;
        this.shape = shape;
    }

    /**
     * Gets the ID of this region. IDs maintain casing but are case insensitive
     * during comparison.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the shape defining this region.
     *
     * @return a shape
     */
    public IndexableShape getShape() {
        return shape;
    }

    /**
     * Set a shape defining this region.
     *
     * @param shape a shape
     */
    public void setShape(IndexableShape shape) {
        Validate.notNull(shape, "Shape parameter cannot be null");
        this.shape = shape;
    }

    /**
     * Get the priority of the region. Priorities determine the resolution order of
     * flags, where greater numbers indicate higher precedence. The default priority
     * of a newly created region is 0, and negative priorities are also supported.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Set the priority of the region.
     *
     * @see #getPriority() for an explanation of priorities
     * @param priority the priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Get the parent of the region. Parents can determine how multiple overlapping
     * regions are handled in regards to some flags, but it is dependent on the flag.
     *
     * @return parent region or null
     */
    public Region getParent() {
        return parent;
    }

    /**
     * Set the parent of this region.
     *
     * @see #getParent() for an explanation of parents
     * @param parent the new parent, or null
     * @throws IllegalArgumentException when circular inheritance is detected
     */
    public synchronized void setParent(Region parent) throws IllegalArgumentException {
        if (parent == null) {
            this.parent = null;
        } else {
            if (parent == this) {
                throw new IllegalArgumentException(
                        "Circular region inheritance detected");
            }

            Region p = parent.getParent();
            while (p != null) {
                if (p == this) {
                    throw new IllegalArgumentException(
                            "Circular region inheritance detected");
                }

                p = p.getParent();
            }

            this.parent = parent;
        }
    }

    /**
     * Get a flag's value. May return null if the flag has been set.
     *
     * @param <T> the flag type
     * @param <V> the type of the flag's value
     * @param flag the flag to check
     * @return value or null if isn't defined
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Flag<V>, V> V getFlag(T flag) {
        Object obj = flags.get(flag);
        V val;
        if (obj != null) {
            val = (V) obj;
        } else {
            return null;
        }
        return val;
    }

    /**
     * Set a flag's value. If the value is null, then the flag is unset. If the flag
     * has already been set, then the existing value will be replaced with the
     * new one.
     *
     * @param <T> the flag type
     * @param <V> the type of the flag's value
     * @param flag the flag to set
     * @param value the value to set, or null to unset
     */
    public synchronized <T extends Flag<V>, V> void setFlag(T flag, V value) {
        Validate.notNull(flag, "Flag parameter cannot be null");
        if (value == null) {
            flags.remove(flag);
        } else {
            flags.put(flag, value);
        }
    }

    /**
     * Unset a flag. If the flag is not yet set, nothing will happen.
     *
     * @param flag the flag to unset
     */
    public void unsetFlag(Flag<?> flag) {
        setFlag(flag, null);
    }

    /**
     * Get the map of all flags. Please avoid using this.
     *
     * @return the map of flags currently used for this region
     */
    public Map<Flag<?>, Object> getFlags() {
        return flags;
    }

    /**
     * Get the map of flags. Please avoid using this.
     *
     * @param flags the flags to set
     */
    public void setFlags(Map<Flag<?>, Object> flags) {
        Validate.notNull(flags, "Flags parameter cannot be null");
        this.flags = flags;
    }

    /**
     * Compares to another region.
     * <ul>
     * <li>Orders primarily by the priority, descending</li>
     * Orders secondarily by the id, ascending</li>
     * </ul>
     *
     * @param other the region to compare to
     */
    @Override
    public int compareTo(Region other) {
        if (priority > other.priority) {
            return -1;
        } else if (priority < other.priority) {
            return 1;
        }

        return id.compareTo(other.id);
    }

    /**
     * Returns whether this is a high frequency region that should be placed in a
     * faster index for frequent access. Indices may or may not adhere to the
     * return value of this method.
     *
     * @return true if this is a high frequency region.
     */
    public boolean shouldCache() {
        return false;
    }

    /**
     * Returns whether this region has the same ID as another region.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Region)) {
            return false;
        }

        Region other = (Region) obj;
        return other.getId().equals(getId());
    }

    @Override
    public int hashCode(){
        return id.hashCode();
    }
}
