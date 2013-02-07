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
 * An attribute that stores three states (none, allow, and deny).
 * 
 * @see Managed another way to store booleans (with more memory cost and null
 *      support)
 */
public class State extends Attribute {
    
    public static enum Type {
        NONE,
        ALLOW,
        DENY
    }
    
    private Type value = Type.NONE;

    /**
     * Construct an instance and assign a default name.
     * 
     * @see Attribute#Attribute() for basic mechanics
     */
    public State() {
    }

    /**
     * Construct an instance and specify an attribute name.
     * 
     * @param name name of the attribute
     */
    public State(String name) {
        super(name);
    }

    /**
     * Get the value.
     * 
     * @return the value
     */
    public Type getValue() {
        return value;
    }

    /**
     * Set the value.
     * 
     * @param value the new value
     */
    public void setValue(Type value) {
        Validate.notNull(value);
        
        this.value = value;
    }
    
    /**
     * Returns whether the value is {@link Type#NONE} or {@link Type#ALLOW}.
     * 
     * @return whether the value is for allow
     */
    public boolean allows() {
        return value == Type.NONE || value == Type.ALLOW;
    }

    @Override
    public void read(DataInputStream in, int len) throws IOException {
        int ordinal = in.readByte();
        Type[] values = Type.values();
        
        if (ordinal < 0 || ordinal >= values.length) {
            value = Type.NONE;
        } else {
            value = values[ordinal];
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(value.ordinal());
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
