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

public class ApplicableRegionSetTest {
    static String COURTYARD_ID = "courtyard";
    static String FOUNTAIN_ID = "fountain";
    static String MEMBER_GROUP = "member";
    static String COURTYARD_GROUP = "courtyard";

    Vector inFountain = new Vector(2, 2, 2);
    Vector inCourtyard = new Vector(7, 7, 7);
    Vector outside = new Vector(15, 15, 15);
    RegionManager manager;
    TestPlayer player1;
    TestPlayer player2;

    @Before
    public void setUp() throws Exception {
        manager = new FlatRegionManager();

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
    
    void setUpCourtyardRegion() {
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup(COURTYARD_GROUP);
        
        ProtectedRegion region = new ProtectedCuboidRegion(
                new BlockVector(0, 0, 0), new BlockVector(10, 10, 10));
        AreaFlags flags = new AreaFlags();
        flags.allowBuild = AreaFlags.State.NONE;
        flags.allowFireSpread = AreaFlags.State.ALLOW;
        region.setFlags(flags);
        region.setOwners(domain);
        manager.addRegion(COURTYARD_ID, region);
    }
    
    void setUpFountainRegion() {
        ProtectedRegion region = new ProtectedCuboidRegion(
                new BlockVector(0, 0, 0), new BlockVector(5, 5, 5));
        AreaFlags flags = new AreaFlags();
        flags.allowBuild = AreaFlags.State.ALLOW;
        flags.allowFireSpread = AreaFlags.State.DENY;
        region.setFlags(flags);
        manager.addRegion(FOUNTAIN_ID, region);
    }
    
    @Test
    public void testNonBuildFlag() {        
        ApplicableRegionSet appl;
        
        // Outside
        appl = manager.getApplicableRegions(outside);
        assertTrue(appl.allowsFireSpread());
        // Inside courtyard
        appl = manager.getApplicableRegions(inCourtyard);
        assertTrue(appl.allowsFireSpread());
        // Inside fountain
        appl = manager.getApplicableRegions(inFountain);
        assertFalse(appl.allowsFireSpread());
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
