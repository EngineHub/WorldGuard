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

import com.sk89q.worldguard.region.shapes.IndexableShape;

/**
 * A region that has an ID, shape, priority, parent, and attributes assigned
 * to it. It is used for defining areas on a world.
 * <p>
 * The casing of region IDs are maintained, but they are case insensitive for the
 * purposes of indexing.
 * <p>
 * If changes are made to this region, and it is contained within a region
 * index, the index must be notified of the change.
 */
public class Region implements Comparable<Region> {

    private final String id;
    private IndexableShape shape;
    private int priority = 0;
    private Region parent;
    private Map<String, Attribute> attributes = new HashMap<String, Attribute>();

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
     * Set an attribute to this region.
     * <p>
     * While more than one region can share an attribute, this is not
     * recommended. On region index save and then load, the attribute would
     * be created individually for each region.
     * <p>
     * If an attribute of the same name already exists on this region, the
     * existing attribute will be replaced with the given one.
     * <p>
     * This method is thread-safe.
     * 
     * @param attribute the attribute
     */
    public synchronized void set(Attribute attribute) {
        attributes.put(attribute.getName(), attribute);
    }
    
    /**
     * Remove an attribute with the same name from this region.
     * <p>
     * This method is thread-safe.
     * 
     * @param attribute the attribute
     */
    public synchronized void remove(Attribute attribute) {
        attributes.remove(attribute.getName());
    }
    
    /**
     * Returns whether this region contains this attribute.
     * <p>
     * Recall that {@link Attribute#equals(Object)} is true if two attributes
     * have the same name, and so this merely checks whether an attribute
     * of the same name exists on this region.
     * <p>
     * This method is thread-safe.
     * 
     * @param attribute the attribute
     * @return true if this region contains the attribute
     */
    public synchronized boolean contains(Attribute attribute) {
        return attributes.containsKey(attribute.getName());
    }
    
    /**
     * Get the actual stored attribute on this region based on the attribute
     * type given as a parameter.
     * <p>
     * The returned object may or may not be the same as the given object,
     * but the returned attribute will definitely have the same name.
     * <p>
     * At the moment, if the attribute stored on this region is of a different
     * type than the given attribute, that is an undefined situation and
     * bad things will occur.
     * 
     * @param attribute attribute to get
     * @return the attribute stored on this region or null otherwise
     */
    @SuppressWarnings("unchecked")
    public synchronized <E extends Attribute> E get(E attribute) {
        return (E) attributes.get(attribute.getName());
    }
    
    /**
     * Rename the given attribute to a new name, and add the attribute to
     * this region if doesn't already exist on this region.
     * <p>
     * This is the only safe way to rename an attribute, assuming that the
     * given attribute is not being used across several regions
     * (not good!).
     * 
     * @param attribute attribute to rename
     * @param newName new attribute name to use
     */
    public synchronized void rename(Attribute attribute, String newName) {
        remove(attribute);
        attribute.setName(newName);
        set(attribute);
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
