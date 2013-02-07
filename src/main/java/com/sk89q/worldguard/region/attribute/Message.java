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
 * An attribute that stores strings.
 * <p>
 * Compared to {@link Managed}, strings stored with this class use less
 * space and memory. Be aware that strings stored with this class
 * are limited to the size of an unsigned short.
 * 
 * @see Managed another way to store strings (with more memory cost and null
 *      support)
 */
public class Message extends Attribute {
    
    private String value = "";

    /**
     * Construct an instance and assign a default name.
     * 
     * @see Attribute#Attribute() for basic mechanics
     */
    public Message() {
    }

    /**
     * Construct an instance and specify an attribute name.
     * 
     * @param name name of the attribute
     */
    public Message(String name) {
        super(name);
    }

    /**
     * Get the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value.
     * 
     * @param value the new value (cannot be null)
     */
    public void setValue(String value) {
        Validate.notNull(value);
        
        this.value = value;
    }

    @Override
    public void read(DataInputStream in, int len) throws IOException {
        value = in.readUTF();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
