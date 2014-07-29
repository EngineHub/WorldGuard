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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.junit.Before;
import org.junit.Test;

import static com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import static org.junit.Assert.assertTrue;

public abstract class RegionCircularInheritanceTest {
    static String THRONE_ROOM_ID = "thrown-room";
    static String CASTLE_ID = "castle";
    static String KINGDOM_ID = "kingdom";

    RegionManager manager;
    ProtectedRegion globalRegion;
    ProtectedRegion throneroom;
    ProtectedRegion castle;
    ProtectedRegion kingdom;

    protected abstract RegionManager createRegionManager() throws Exception;

    @Before
    public void setUp() throws Exception {
        setUpGlobalRegion();

        manager = createRegionManager();

        setUpThroneRoomRegion();
        setUpCastleRegion();
        setUpKingdomRegion();
    }

    void setUpGlobalRegion() {
        globalRegion = new GlobalProtectedRegion("__global__");
    }

    void setUpThroneRoomRegion() {
        throneroom = new ProtectedCuboidRegion(
                THRONE_ROOM_ID,
                new BlockVector(0, 0, 0),
                new BlockVector(1, 1, 1)
        );

        manager.addRegion(throneroom);
    }

    void setUpCastleRegion() {
        castle = new ProtectedCuboidRegion(
                CASTLE_ID,
                new BlockVector(0, 0, 0),
                new BlockVector(1, 1, 1)
        );

        manager.addRegion(castle);
    }

    void setUpKingdomRegion() {
        kingdom = new ProtectedCuboidRegion(
                KINGDOM_ID,
                new BlockVector(0, 0, 0),
                new BlockVector(1, 1, 1)
        );

        manager.addRegion(kingdom);
    }

    @Test
    public void testParentIsRegion() {
        try {
            castle.setParent(castle);
            assertTrue(false);
        } catch (CircularInheritanceException ignored) { }
    }

    @Test
    public void testProperParent() {
        try {
            throneroom.setParent(castle);
            castle.setParent(kingdom);
            assertTrue(throneroom.getParent() != null && throneroom.getParent().equals(castle));
            assertTrue(castle.getParent() != null && castle.getParent().equals(kingdom));
        } catch (CircularInheritanceException ignored) { }
    }

    @Test
    public void testNullParent() {
        try {
            castle.setParent(null);
            assertTrue(castle.getParent() == null);
        } catch (CircularInheritanceException ignored) { }
    }

    @Test
    public void testParentOfParentIsRegion() {
        try {
            throneroom.setParent(castle);
            castle.setParent(kingdom);
            kingdom.setParent(throneroom);
            assertTrue(false);
        } catch (CircularInheritanceException ignored) { }
    }
}
