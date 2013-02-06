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
 * A simple implementation of {@link Attribute} that saves the raw binary 
 * data stream for exact recall later during runtime and during serialization.
 * <p>
 * By itself, this class will accept binary data, store it as-is, then
 * dump it back out as binary data when requested. If you have special needs,
 * and do not want to have to explicitly deserialize the contents contained
 * within this attribute, consider subclassing {@link Attribute} instead.
 * <p>
 * This is the automatic fallback attribute in case custom attribute
 * classes are unavailable during load.
 */
public final class DataValuedAttribute extends Attribute {
    
    private byte[] buffer;

    /**
     * Construct the attribute with a default name.
     */
    public DataValuedAttribute() {
        super();
    }

    /**
     * Construct the attribute with a given name.
     */
    public DataValuedAttribute(String name) {
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
    public byte[] getByteArray() {
        return buffer;
    }
    
    /**
     * Set the raw byte array.
     * <p>
     * The given byte array is not copied and so further modifications to the
     * given byte array will have the changes reflect on the byte array
     * inside this object.
     * 
     * @param data new data
     */
    public void setByteArray(byte[] data) {
        Validate.notNull(data);
        
        this.buffer = data;
    }
    
    /**
     * Get the size in bytes of the data.
     * 
     * @return length in bytes of data
     */
    public int size() {
        return buffer.length;
    }

    @Override
    public void read(DataInputStream in, int len) throws IOException {
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        this.buffer = buffer;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.write(buffer);
    }

}
