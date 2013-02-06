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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

public class AttributeTest {

    @Test
    public void testHashCode() {
        assertEquals(new TestAttribute("testing").hashCode(), "testing".hashCode());
        assertFalse(new TestAttribute("broken").hashCode() == "testing".hashCode());
    }

    @Test
    public void testAttribute() {
        new TestAttribute("testing");
    }

    @Test
    public void testGetName() {
        assertEquals(new TestAttribute("testing").getName(), "testing");
        assertFalse(new TestAttribute("testing").getName().equals("broken"));
    }

    @Test
    public void testSetName() {
        assertEquals(new TestAttribute("testing").getName(), "testing");
        assertFalse(new TestAttribute("testing").getName().equals("broken"));
    }

    @Test
    public void testRead() throws IOException {
        Attribute attribute = new TestAttribute("testing");
        byte[] data = new byte[] { 1, 2, 3, 4, 5, 6 };
        attribute.read(new DataInputStream(new ByteArrayInputStream(data)), data.length); // Do nothing
    }

    @Test
    public void testWrite() throws IOException {
        Attribute attribute = new TestAttribute("testing");
        ByteArrayOutputStream stream = new ByteArrayOutputStream(100);
        attribute.write(new DataOutputStream(stream)); // Do nothing
        assertEquals(stream.size(), 0);
    }

    @Test
    public void testEqualsObject() {
        assertTrue(new TestAttribute("testing").equals(new TestAttribute("testing")));
        assertFalse(new TestAttribute("testing").equals(new TestAttribute("broken")));
    }

}
