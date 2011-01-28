// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.protection;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.AreaFlags.State;

public class ApplicableRegionSetTest {
    static String COURTYARD_ID = "courtyard";
    static String FOUNTAIN_ID = "fountain";
    static String NO_FIRE_ID = "nofire";
    static String MEMBER_GROUP = "member";
    static String COURTYARD_GROUP = "courtyard";

    Vector inFountain = new Vector(2, 2, 2);
    Vector inCourtyard = new Vector(7, 7, 7);
    Vector outside = new Vector(15, 15, 15);
    Vector inNoFire = new Vector(150, 150, 150);
    RegionManager manager;
    ProtectedRegion courtyard;
    ProtectedRegion fountain;
    TestPlayer player1;
    TestPlayer player2;

    @Before
    public void setUp() throws Exception {
        manager = new FlatRegionManager(new GlobalFlags());

        setUpPlayers();
        setUpCourtyardRegion();
        setUpFountainRegion();
        setUpNoFireRegion();
    }
    
    void setUpPlayers() {
        player1 = new TestPlayer("tetsu");
        player1.addGroup(MEMBER_GROUP);
        player1.addGroup(COURTYARD_GROUP);

        player2 = new TestPlayer("alex");
        player2.addGroup(MEMBER_GROUP);
    }
    
    void setUpCourtyardRegion() {
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup(COURTYARD_GROUP);
        
        ProtectedRegion region = new ProtectedCuboidRegion(COURTYARD_ID,
                new BlockVector(0, 0, 0), new BlockVector(10, 10, 10));
        AreaFlags flags = new AreaFlags();
        flags.set(AreaFlags.FLAG_BUILD, State.NONE);
        flags.set(AreaFlags.FLAG_FIRE_SPREAD, State.ALLOW);
        region.setFlags(flags);
        region.setOwners(domain);
        manager.addRegion(region);
        
        courtyard = region;
    }
    
    void setUpFountainRegion() throws Exception {
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup(MEMBER_GROUP);
        
        ProtectedRegion region = new ProtectedCuboidRegion(FOUNTAIN_ID,
                new BlockVector(0, 0, 0), new BlockVector(5, 5, 5));
        AreaFlags flags = new AreaFlags();
        flags.set(AreaFlags.FLAG_FIRE_SPREAD, State.DENY);
        region.setFlags(flags);
        region.setMembers(domain);
        manager.addRegion(region);

        fountain = region;
        fountain.setParent(courtyard);
    }
    
    void setUpNoFireRegion() throws Exception {
        ProtectedRegion region = new ProtectedCuboidRegion(NO_FIRE_ID,
                new BlockVector(100, 100, 100), new BlockVector(200, 200, 200));
        AreaFlags flags = new AreaFlags();
        flags.set(AreaFlags.FLAG_FIRE_SPREAD, State.DENY);
        region.setFlags(flags);
        manager.addRegion(region);
    }
    
    @Test
    public void testNonBuildFlag() {        
        ApplicableRegionSet appl;
        
        // Outside
        appl = manager.getApplicableRegions(outside);
        assertTrue(appl.allowsFlag(AreaFlags.FLAG_FIRE_SPREAD));
        // Inside courtyard
        appl = manager.getApplicableRegions(inCourtyard);
        assertTrue(appl.allowsFlag(AreaFlags.FLAG_FIRE_SPREAD));
        // Inside fountain
        appl = manager.getApplicableRegions(inFountain);
        assertFalse(appl.allowsFlag(AreaFlags.FLAG_FIRE_SPREAD));

        // Inside no fire zone
        appl = manager.getApplicableRegions(inNoFire);
        assertFalse(appl.allowsFlag(AreaFlags.FLAG_FIRE_SPREAD));
    }
    
    @Test
    public void testPlayer1BuildAccess() {        
        ApplicableRegionSet appl;
        
        // Outside
        appl = manager.getApplicableRegions(outside);
        assertTrue(appl.canBuild(player1));
        // Inside courtyard
        appl = manager.getApplicableRegions(inCourtyard);
        assertTrue(appl.canBuild(player1));
        // Inside fountain
        appl = manager.getApplicableRegions(inFountain);
        assertTrue(appl.canBuild(player1));
    }
    
    @Test
    public void testPlayer2BuildAccess() {        
        ApplicableRegionSet appl;
        
        // Outside
        appl = manager.getApplicableRegions(outside);
        assertTrue(appl.canBuild(player2));
        // Inside courtyard
        appl = manager.getApplicableRegions(inCourtyard);
        assertFalse(appl.canBuild(player2));
        // Inside fountain
        appl = manager.getApplicableRegions(inFountain);
        assertTrue(appl.canBuild(player2));
    }

}
