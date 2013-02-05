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
import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.sk89q.worldguard.region.shapes.IndexableShape;

public class RegionTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        thrown.expect(IllegalArgumentException.class);
        new Region(null, null);
    }

    @Test
    public void testEquals() {
        Region regionA = new Region("A", mock(IndexableShape.class));
        Region regionB = new Region("B", mock(IndexableShape.class));
        
        assertFalse(regionA.equals(regionB));
        assertFalse(regionB.equals(regionA));
        assertTrue(regionA.equals(regionA));
        assertTrue(regionB.equals(regionB));
    }

    @Test
    public void testNameCompareTo() {
        Region regionA = new Region("A", mock(IndexableShape.class));
        Region regionB = new Region("B", mock(IndexableShape.class));
        
        assertEquals(regionA.compareTo(regionB), -1);
        assertEquals(regionB.compareTo(regionA), 1);
        assertEquals(regionA.compareTo(regionA), 0);
        assertEquals(regionB.compareTo(regionB), 0);
    }

    @Test
    public void testSetParentSelf() {
        Region regionA = new Region("A", mock(IndexableShape.class));
        
        thrown.expect(IllegalArgumentException.class);
        regionA.setParent(regionA);
    }

    @Test
    public void testSetParentOther() {
        Region child = new Region("A", mock(IndexableShape.class));
        Region parent = new Region("B", mock(IndexableShape.class));

        child.setParent(parent);
        assertEquals(child.getParent(), parent);
        assertEquals(parent.getParent(), null);
    }

    @Test
    public void testSetParentCircular() {
        Region child = new Region("A", mock(IndexableShape.class));
        Region parent = new Region("B", mock(IndexableShape.class));

        child.setParent(parent);
        thrown.expect(IllegalArgumentException.class);
        parent.setParent(child);
    }

    @Test
    public void testGetPriorityDefault() {
        Region regionA = new Region("A", mock(IndexableShape.class));
        assertEquals(regionA.getPriority(), 0);
    }

    @Test
    public void testShouldCacheDefault() {
        Region regionA = new Region("A", mock(IndexableShape.class));
        assertFalse(regionA.shouldCache());
    }

}
