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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An attribute that serializes and de-serializes objects automatically
 * using Java's serialization methods.
 * <p>
 * This class allows for null values.
 */
public class Managed<T> extends Attribute {
    
    private T value;

    /**
     * Construct an instance and assign a default name.
     * 
     * @see Attribute#Attribute() for basic mechanics
     */
    public Managed() {
    }

    /**
     * Construct an instance and specify an attribute name.
     * 
     * @param name name of the attribute
     */
    public Managed(String name) {
        super(name);
    }

    /**
     * Get the value.
     * 
     * @return the value or null
     */
    public T getValue() {
        return value;
    }

    /**
     * Set the value.
     * 
     * @param value the new value or null
     */
    public void setValue(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(DataInputStream in, int len) throws IOException {
        ObjectInputStream objIn = new ObjectInputStream(in);
        try {
            value = (T) objIn.readObject();
        } catch (ClassCastException e) {
            throw new IOException("Deserialized object was something else", e);
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialized object uses class not found", e);
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        objOut.writeObject(out);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
