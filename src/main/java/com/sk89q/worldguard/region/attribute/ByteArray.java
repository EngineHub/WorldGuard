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

/**
 * Stores raw byte array data.
 * <p>
 * If no more accurate {@link Attribute} class is found during deserialization,
 * this class is used because it would maintain the data. If you wish to create
 * your own {@link Attribute}s, consider subclassing that class rather than
 * using this class.
 * 
 * @see Managed another way to store byte arrays (with more memory cost and null
 *      support)
 */
public final class ByteArray extends Attribute {
    
    private byte[] value;

    /**
     * Construct the attribute with a default name.
     */
    public ByteArray() {
        super();
    }

    /**
     * Construct the attribute with a given name.
     */
    public ByteArray(String name) {
        super(name);
    }
    
    /**
     * Get the raw byte array.
     * <p>
     * Modifying the returned byte array directly modifies the byte
     * array stored within the instance of this class.
     * 
     * @return data
     */
    public byte[] getValue() {
        return value;
    }
    
    /**
     * Set the raw byte array.
     * <p>
     * The given byte array is stored as a reference within this instance.
     * 
     * @param data new data
     */
    public void setValue(byte[] value) {
        Validate.notNull(value);
        
        this.value = value;
    }
    
    /**
     * Get the size in bytes of the data.
     * 
     * @return length in bytes of data
     */
    public int size() {
        return value.length;
    }

    @Override
    public void read(DataInputStream in, int len) throws IOException {
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        this.value = buffer;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.write(value);
    }

}
