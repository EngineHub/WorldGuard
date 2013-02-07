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

package com.sk89q.worldguard.region.attribute;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.lang.Validate;

import com.sk89q.worldguard.region.Region;

/**
 * Attributes can be attached to regions to store all sorts of data.
 * <p>
 * Example attributes include:</p>
 * <ul>
 *   <li>PvP allow/denial flag</li>
 *   <li>Owner of the region</li>
 *   <li>Faction of the region</li>
 *   <li>A picture of your crush</li>
 * </ul>
 * <p>
 * Asking for attributes from a region will return a subclass of
 * this class, which can be either an instance of {@link ByteArray}
 * or a custom overriding attribute class available in WorldGuard or
 * from another project.
 * <p>
 * Creating your attributes to store any type of data is easy. The most
 * important decision to make is whether to use {@link ByteArray}
 * or your own subclass. The latter lets you store the attribute data
 * in a structure more native to your software (using primitive data types and
 * your own objects), but it may be not be necessary if your payload does
 * consist of raw binary data, which is what {@link ByteArray} does.
 * <p>
 * When you make a subclass, you must define the
 * {@link #read(DataInputStream, int)} and
 * {@link #write(DataOutputStream)} methods so that the incoming data
 * can be deserialized and the outgoing data can be serialized. The reason is
 * because whatever is storing the region data has to ultimately store the
 * data as a series of ones and zeros, and rather than using Java's native
 * serialization routines (which may be subject to change), we instead
 * delegate that task explicitly to each attribute. Remember that is important
 * to choose a format that can be changed in the future and still maintain
 * backwards compatibility.
 * <p>
 * During loading, subclasses will automatically be found by searching
 * the classpath, as the canonical name of each attribute's class is saved
 * during saving. The process involved here is outlined more closely on
 * the WorldGuard wiki. If worse comes to worse, and the subclass cannot
 * be found, then the data will be loaded as an {@link ByteArray},
 * which prevents loss of the raw binary data, but it will render the
 * data unusuable during runtime unless explicitly deserialization is
 * conducted when recalling the attribute.
 */
public abstract class Attribute {
    
    private String name = "unnamed";

    /**
     * A constructor required for automatic instantiation of attributes.
     * <p>
     * If you are overriding this class, you must provide this constructor.
     * Be aware that the default name of attributes is 'unnamed'.
     */
    public Attribute() {
    }
    
    /**
     * Construct an instance and set the name.
     * <p>
     * Subclasses do not have to provide this constructor but it is helpful
     * to do so.
     * 
     * @param name the name to set
     * @see #setName(String)
     */
    public Attribute(String name) {
        setName(name);
    }

    /**
     * Get the name of this attribute.
     * <p>
     * Attribute names allow for distinguishing different attributes (such as
     * between a PvP flag and an image storage attribute). A region cannot have
     * two attributes with the same name.
     * 
     * @return the name of the attribute
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name of an attribute.
     * <p>
     * Remember that the corresponding {@link Region} must be made aware of this
     * change or very bad things may happen.
     * <p>
     * All valid non-empty Unicode strings are supported for names.
     * 
     * @param name
     *            new name to use
     * @see Region#rename(Attribute, String)
     */
    public void setName(String name) {
        Validate.notNull(name);
        Validate.notEmpty(name);
        
        this.name = name;
    }

    /**
     * Read the input data stream and do something with the incoming data.
     * <p>
     * This method is called whenever the attribute is being unserialized
     * from persistent storage.
     * 
     * @param in input data stream
     * @param len length of the data
     * @throw IOException thrown on read error, which would cause the instance
     *                    of this class to fail, causing the region index to
     *                    fall back to {@link ByteArray}
     */
    public abstract void read(DataInputStream in, int len) throws IOException;
    
    /**
     * Write out this attribute's data.
     * <p>
     * This method is called whenever the attribute is being serialized.
     * 
     * @param out output data stream
     * @throw IOException thrown on write error, which is really bad and would
     *                    cause data loss
     */
    public abstract void write(DataOutputStream out) throws IOException;

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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Attribute)) {
            return false;
        }
        return getName().equals(((Attribute) obj).getName());
    }

    @Override
    public String toString() {
        return "Attribute(" + getName() + ")";
    }
    
}
