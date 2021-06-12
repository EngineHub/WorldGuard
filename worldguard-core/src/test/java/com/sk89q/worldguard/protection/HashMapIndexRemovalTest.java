/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.protection;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.managers.index.HashMapIndex;
import com.sk89q.worldguard.protection.managers.storage.MemoryRegionDatabase;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HashMapIndexRemovalTest {
    private static final String ORPHAN_ID = "orphan";
    private static final String NESTED_ID_PREFIX = "nested_";
    private static final int NEST_DEPTH = 5; // minimum 3
    private RegionManager manager;

    private FlagRegistry getFlagRegistry() {
        return WorldGuard.getInstance().getFlagRegistry();
    }

    private RegionManager createRegionManager() {
        return new RegionManager(new MemoryRegionDatabase(), new HashMapIndex.Factory(), getFlagRegistry());
    }

    @BeforeEach
    public void setUp() {
        manager = createRegionManager();

        setUpOrphanRegion();
        setUpDeeplyNestedRegions();
    }

    private void setUpDeeplyNestedRegions() {
        ProtectedRegion parent = null;
        for (int i = 0; i < NEST_DEPTH; i++) {
            ProtectedRegion newRegion = new ProtectedCuboidRegion(NESTED_ID_PREFIX + i,
                    BlockVector3.ZERO, BlockVector3.ZERO); // bounds don't matter for this test
            if (parent != null) {
                try {
                    newRegion.setParent(parent);
                } catch (CircularInheritanceException ignored) {
                }
            }
            parent = newRegion;
        }
        manager.addRegion(parent);
    }

    private void setUpOrphanRegion() {
        ProtectedRegion orphan = new ProtectedCuboidRegion(ORPHAN_ID,
                BlockVector3.ZERO, BlockVector3.at(5, 5, 5));
        manager.addRegion(orphan);
    }

    @Test
    public void testRemoveWithUnset() {
        int initialSize = 1 + NEST_DEPTH; // orphan + nested
        assertEquals(initialSize, manager.size());
        manager.removeRegion(NESTED_ID_PREFIX + "0", RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
        assertEquals(initialSize - 1, manager.size());
        assertFalse(manager.hasRegion(NESTED_ID_PREFIX + "0"));

        final ProtectedRegion firstChild = manager.getRegion(NESTED_ID_PREFIX + "1");
        assertNotNull(firstChild);
        assertNull(firstChild.getParent());

        final ProtectedRegion secondChild = manager.getRegion(NESTED_ID_PREFIX + "2");
        assertNotNull(secondChild);
        assertEquals(secondChild.getParent(), firstChild);
    }

    @Test
    public void testRemoveWithChildren() {
        int initialSize = 1 + NEST_DEPTH; // orphan + nested
        assertEquals(manager.size(), initialSize);
        manager.removeRegion(NESTED_ID_PREFIX + "1", RemovalStrategy.REMOVE_CHILDREN);
        assertEquals(2, manager.size());
        assertTrue(manager.hasRegion(NESTED_ID_PREFIX + "0"));
        assertTrue(manager.hasRegion(ORPHAN_ID));
    }
}
