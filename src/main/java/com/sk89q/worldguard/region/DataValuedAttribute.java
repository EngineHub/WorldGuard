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

/**
 * A simple implementation of {@link Attribute} that actually can keep
 * its data between reads and writes.
 * <p>
 * By itself, this class will accept binary data, store it as-is, then
 * dump it back out as binary data when requested. If you wish to use
 * attributes, then one option is to manually decode this binary data somewhere
 * in your code and then re-encode the data and put it back. However, the
 * better idea would be to extend this class with your own version and
 * override the {@link #read(DataInputStream, int)} and
 * {@link #write(DataOutputStream)} methods so they can serialize
 * and unserialize the data.
 * <p>
 * The way attributes are loaded is that the region
 * loader will first search to see if the corresponding overriding
 * {@link Attribute} class exists somewhere in its class path
 * (the class's full canonical name is written during serialization of the
 * region data). If it does, the class will be instantiated with the
 * <strong>no-argument constructor</strong>, the name will be set
 * with {@link Attribute#setName(String)}, and then the raw data stream
 * will be given to {@link Attribute#read(DataInputStream, int)}. If all
 * goes well, the corresponding class found will be what is returned when
 * a copy of the attribute is requested for a region.
 * <p>
 * However, if the class does not appear visible to the class loader, or
 * during instantiation and setup, the process fails, this class
 * ({@link DataValuedAttribute}) will be utilized instead. This way, even if
 * the class for the attribute does not exist at runtime, the data will be
 * still be maintained on the region.
 * <p>
 * The regular {@link Attribute} class is never actually used because
 * it discards data on read.
 */
public final class DataValuedAttribute extends Attribute {
    
    private byte[] buffer;

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
