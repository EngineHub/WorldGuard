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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.TestPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class RegionEntryExitTest {

    static String ENTRY_ID = "entry_rg";
    static String EXIT_ID = "exit_rg";
    static String BUILDER_GROUP = "builder";
    static String VIP_GROUP = "vip";

    Vector inEntry = new Vector(5, 64, 5);
    Vector inExit = new Vector(-5, 65, -5);

    RegionManager manager;
    ProtectedRegion globalRegion;
    ProtectedRegion entryRegion;
    ProtectedRegion exitRegion;
    TestPlayer vipPlayer;
    TestPlayer builderPlayer;

    protected abstract RegionManager createRegionManager() throws Exception;

    @Before
    public void setUp() throws Exception {
        setUpGlobalRegion();

        manager = createRegionManager();

        setUpPlayers();
        setUpEntryRegion();
        setUpExitRegion();
    }

    void setUpPlayers() {
        vipPlayer = new TestPlayer("dudu");
        vipPlayer.addGroup(VIP_GROUP);

        builderPlayer = new TestPlayer("esskay");
        builderPlayer.addGroup(BUILDER_GROUP);

        // @Test
        // assertFalse(builderPlayer.wuvs(vipPlayer)); // causes test to fail
    }

    void setUpGlobalRegion() {
        globalRegion = new GlobalProtectedRegion("__global__");
    }

    void setUpEntryRegion() {
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup(VIP_GROUP);

        ProtectedRegion region = new ProtectedCuboidRegion(ENTRY_ID, new BlockVector(1, 0, 1), new BlockVector(10, 255, 10));

        region.setMembers(domain);
        manager.addRegion(region);

        entryRegion = region;
        // this is the way it's supposed to work
        // whatever the group flag is set to is the group that the flag APPLIES to
        // in this case, non members (esskay) should be DENIED entry
        entryRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
        entryRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
    }

    void setUpExitRegion() throws Exception {
        DefaultDomain domain = new DefaultDomain();
        domain.addGroup(BUILDER_GROUP);

        ProtectedRegion region = new ProtectedCuboidRegion(EXIT_ID, new BlockVector(-1, 0, -1), new BlockVector(-10, 255, -10));

        region.setOwners(domain);
        manager.addRegion(region);

        entryRegion = region;
        // same as above
        entryRegion.setFlag(DefaultFlag.EXIT, StateFlag.State.DENY);
        entryRegion.setFlag(DefaultFlag.EXIT.getRegionGroupFlag(), RegionGroup.NON_OWNERS);
    }

    @Test
    public void testEntry() throws Exception {
        ApplicableRegionSet appl;

        appl = manager.getApplicableRegions(inEntry);
//        ProtectedRegion rg = appl.iterator().next();
//        System.out.println("rg   " + rg.getId());
//        System.out.println("mem  " + rg.getMembers().toGroupsString());
//        System.out.println("flag " + appl.getFlag(DefaultFlag.ENTRY));
//        System.out.println("grp  " + appl.getFlag(DefaultFlag.ENTRY.getRegionGroupFlag()));
//        System.out.println("===");
        assertTrue("Allowed Entry", appl.allows(DefaultFlag.ENTRY, vipPlayer));
        assertFalse("Forbidden Entry", appl.allows(DefaultFlag.ENTRY, builderPlayer));
    }

    @Test
    public void testExit() throws Exception {
        ApplicableRegionSet appl;

        appl = manager.getApplicableRegions(inExit);
//        ProtectedRegion rg = appl.iterator().next();
//        System.out.println("rg   " + rg.getId());
//        System.out.println("own  " + rg.getOwners().toGroupsString());
//        System.out.println("flag " + appl.getFlag(DefaultFlag.EXIT));
//        System.out.println("grp  " + appl.getFlag(DefaultFlag.EXIT.getRegionGroupFlag()));
//        System.out.println("===");
        assertTrue("Allowed Exit", appl.allows(DefaultFlag.EXIT, builderPlayer));
        assertFalse("Forbidden Exit", appl.allows(DefaultFlag.EXIT, vipPlayer));
    }

}
