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
 * An attribute that stores string data.
 */
public class StringAttr extends Attribute {
    
    private String data;

    /**
     * No-arg constructor.
     */
    public StringAttr() {
    }

    /**
     * Construct an instance and specify an attribute name.
     * 
     * @param name name of the attribute
     */
    public StringAttr(String name) {
        super(name);
    }

    /**
     * Get the text.
     * 
     * @return the text
     */
    public String getText() {
        return data;
    }

    /**
     * Set the stored text.
     * 
     * @param text the new text
     */
    public void setText(String text) {
        Validate.notNull(text);
        
        this.data = text;
    }

    @Override
    public void read(DataInputStream in, int len) throws IOException {
        data = in.readUTF();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(data);
    }
    
    @Override
    public String toString() {
        return data;
    }

}
