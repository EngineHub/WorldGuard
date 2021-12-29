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


import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RegionPriorityTest {
    static String COURTYARD_ID = "courtyard";
    static String FOUNTAIN_ID = "fountain";
    static String NO_FIRE_ID = "nofire";
    static String MEMBER_GROUP = "member";
    static String COURTYARD_GROUP = "courtyard";

    BlockVector3 inFountain = BlockVector3.at(2, 2, 2);
    BlockVector3 inCourtyard = BlockVector3.at(7, 7, 7);
    BlockVector3 outside = BlockVector3.at(15, 15, 15);
    RegionManager manager;
    ProtectedRegion globalRegion;
    ProtectedRegion courtyard;
    ProtectedRegion fountain;
    TestPlayer player1;
    TestPlayer player2;

    protected FlagRegistry getFlagRegistry() {
        return WorldGuard.getInstance().getFlagRegistry();
    }
    
    protected abstract RegionManager createRegionManager() throws Exception;

    @BeforeAll
    public void setUp() throws Exception {
        setUpGlobalRegion();
        
        manager = createRegionManager();

        setUpPlayers();
        setUpCourtyardRegion();
        setUpFountainRegion();
    }
    
    void setUpPlayers() {
        player1 = new TestPlayer("tetsu");
        player1.addGroup(MEMBER_GROUP);
        player1.addGroup(COURTYARD_GROUP);

        player2 = new TestPlayer("alex");
        player2.addGroup(MEMBER_GROUP);
    }
    
    void setUpGlobalRegion() {
        globalRegion = new GlobalProtectedRegion("__global__");
    }
    
    void setUpCourtyardRegion() {
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup(COURTYARD_GROUP);
        
        ArrayList<BlockVector2> points = new ArrayList<>();
        points.add(BlockVector2.ZERO);
        points.add(BlockVector2.at(10, 0));
        points.add(BlockVector2.at(10, 10));
        points.add(BlockVector2.at(0, 10));
        
        //ProtectedRegion region = new ProtectedCuboidRegion(COURTYARD_ID, new BlockVector(0, 0, 0), new BlockVector(10, 10, 10));
        ProtectedRegion region = new ProtectedPolygonalRegion(COURTYARD_ID, points, 0, 10);

        region.setOwners(domain);
        manager.addRegion(region);
        
        courtyard = region;
        courtyard.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
    }
    
    void setUpFountainRegion() throws Exception {
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup(MEMBER_GROUP);
        
        ProtectedRegion region = new ProtectedCuboidRegion(FOUNTAIN_ID,
                BlockVector3.ZERO, BlockVector3.at(5, 5, 5));
        region.setMembers(domain);
        manager.addRegion(region);

        fountain = region;
        fountain.setParent(courtyard);
        fountain.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        fountain.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
    }
    
    @Test
    public void testNoPriorities() throws Exception {
        ApplicableRegionSet appl;

        courtyard.setPriority(0);
        fountain.setPriority(0);
        
        appl = manager.getApplicableRegions(inCourtyard);
        assertTrue(appl.testState(null, Flags.FIRE_SPREAD));
        assertFalse(appl.testState(null, Flags.MOB_SPAWNING));
        appl = manager.getApplicableRegions(inFountain);
        assertFalse(appl.testState(null, Flags.FIRE_SPREAD));
        assertTrue(appl.testState(null, Flags.MOB_SPAWNING));
    }
    
    @Test
    public void testPriorities() throws Exception {
        ApplicableRegionSet appl;

        courtyard.setPriority(5);
        fountain.setPriority(0);
        
        appl = manager.getApplicableRegions(inCourtyard);
        assertTrue(appl.testState(null, Flags.FIRE_SPREAD));
        appl = manager.getApplicableRegions(inFountain);
        assertFalse(appl.testState(null, Flags.FIRE_SPREAD));
    }
    
    @Test
    public void testPriorities2() throws Exception {
        ApplicableRegionSet appl;

        courtyard.setPriority(0);
        fountain.setPriority(5);
        
        appl = manager.getApplicableRegions(inCourtyard);
        assertFalse(appl.testState(null, Flags.MOB_SPAWNING));
        appl = manager.getApplicableRegions(inFountain);
        assertTrue(appl.testState(null, Flags.MOB_SPAWNING));
    }
}
