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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.lang.Validate;

/**
 * Attributes can be attached to regions to store all sorts of data.
 * <p>
 * Example attributes include:</p>
 * <ul>
 * <li>PvP allow/denial flag</li>
 * <li>Owner of the region</li>
 * <li>Faction of the region</li>
 * <li>A picture of your crush</li>
 * </ul>
 * <p>
 * Normally, asking for attributes from a region index will either return
 * a {@link DataValuedAttribute} or a custom overriding attribute class.
 * This class is never used for that purpose because it loses the
 * data it reads in.
 * <p>
 * To create your own attributes, see the class documentation for
 * {@link DataValuedAttribute}.
 */
public abstract class Attribute {
    
    private String name;

    /**
     * A no-arg constructor required for automatic instantiation of
     * attributes. If you are overriding this class, you must provide
     * this constructor.
     */
    public Attribute() {
    }

    /**
     * Get the name of this attribute. Attribute names allow for distinguishing
     * different attributes (such as between a PvP flag and an image storage
     * attribute). A region cannot have two attributes with the same name.
     * 
     * @return the name of the attribute
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name of an attribute. Remember that the corresponding
     * {@link Region} must be made aware of this change or very bad
     * things may happen.
     * <p>
     * All valid Unicode strings are supported for names.
     * 
     * @param name new name to use
     * @see Region#rename(Attribute, String)
     */
    public void setName(String name) {
        Validate.notNull(name);
        
        this.name = name;
    }

    /**
     * Read the input data stream and do something with the incoming data.
     * <p>
     * This method is called whenever the attribute is being loaded. By
     * default, this method does nothing and therefore causes data
     * loss if not overridden.
     * 
     * @param in input data stream
     * @param len length of the data
     * @throw IOException thrown on read error, which would cause the instance
     *                    of this class to fail, causing the region index to
     *                    fall back to {@link DataValuedAttribute}
     */
    public void read(DataInputStream in, int len) throws IOException {
        
    }
    
    /**
     * Write out this attribute's data.
     * <p>
     * This method is called whenever the attribute is being saved. By
     * default, this method does nothing and therefore causes data
     * loss if not overridden.
     * 
     * @param out output data stream
     * @throw IOException thrown on write error, which is really bad and would
     *                    cause data loss
     */
    public void write(DataOutputStream out) throws IOException {
        
    }

    /**
     * Hash codes are based on attribute name.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Two attributes are equal if they have the same name.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Attribute)) return false;
        return getName().equals(((Attribute) obj).getName());
    }

    @Override
    public String toString() {
        return "[Attribute: " + getName() + "]";
    }
    
}
