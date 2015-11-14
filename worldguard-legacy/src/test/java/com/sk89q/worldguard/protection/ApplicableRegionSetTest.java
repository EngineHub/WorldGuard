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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.command.CommandFilter;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ApplicableRegionSetTest {

    @Test
    public void testWildernessBuild() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertThat(set.testState(player, DefaultFlag.BUILD), is(true));
    }

    @Test
    public void testWildernessBuildWithGlobalRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ProtectedRegion global = mock.global();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertThat(set.testState(player, DefaultFlag.BUILD), is(true));
    }

    @Test
    public void testWildernessBuildWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertThat(set.testState(member, DefaultFlag.BUILD), is(true));
        assertThat(set.testState(nonMember, DefaultFlag.BUILD), is(false));
    }

    @Test
    public void testWildernessFlags() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();

        assertThat(set.testState(player, DefaultFlag.MOB_DAMAGE), is(true));
        assertThat(set.testState(player, DefaultFlag.ENTRY), is(true));
        assertThat(set.testState(player, DefaultFlag.EXIT), is(true));
        assertThat(set.testState(player, DefaultFlag.LEAF_DECAY), is(true));
        assertThat(set.testState(player, DefaultFlag.RECEIVE_CHAT), is(true));
        assertThat(set.testState(player, DefaultFlag.SEND_CHAT), is(true));
        assertThat(set.testState(player, DefaultFlag.INVINCIBILITY), is(false));

        assertThat(set.testState(player, DefaultFlag.BUILD), is(true));
    }

    @Test
    public void testWildernessFlagsWithGlobalRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ProtectedRegion global = mock.global();

        ApplicableRegionSet set = mock.getApplicableSet();

        assertThat(set.testState(player, DefaultFlag.MOB_DAMAGE), is(true));
        assertThat(set.testState(player, DefaultFlag.ENTRY), is(true));
        assertThat(set.testState(player, DefaultFlag.EXIT), is(true));
        assertThat(set.testState(player, DefaultFlag.LEAF_DECAY), is(true));
        assertThat(set.testState(player, DefaultFlag.RECEIVE_CHAT), is(true));
        assertThat(set.testState(player, DefaultFlag.SEND_CHAT), is(true));
        assertThat(set.testState(player, DefaultFlag.INVINCIBILITY), is(false));

        assertThat(set.testState(player, DefaultFlag.BUILD), is(true));
    }

    @Test
    public void testFlagsWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();

        assertThat(set.testState(member, DefaultFlag.MOB_DAMAGE), is(true));
        assertThat(set.testState(member, DefaultFlag.ENTRY), is(true));
        assertThat(set.testState(member, DefaultFlag.EXIT), is(true));
        assertThat(set.testState(member, DefaultFlag.LEAF_DECAY), is(true));
        assertThat(set.testState(member, DefaultFlag.RECEIVE_CHAT), is(true));
        assertThat(set.testState(member, DefaultFlag.SEND_CHAT), is(true));
        assertThat(set.testState(member, DefaultFlag.INVINCIBILITY), is(false));

        assertThat(set.testState(member, DefaultFlag.BUILD), is(true));

        assertThat(set.testState(nonMember, DefaultFlag.MOB_DAMAGE), is(true));
        assertThat(set.testState(nonMember, DefaultFlag.ENTRY), is(true));
        assertThat(set.testState(nonMember, DefaultFlag.EXIT), is(true));
        assertThat(set.testState(nonMember, DefaultFlag.LEAF_DECAY), is(true));
        assertThat(set.testState(nonMember, DefaultFlag.RECEIVE_CHAT), is(true));
        assertThat(set.testState(nonMember, DefaultFlag.SEND_CHAT), is(true));
        assertThat(set.testState(nonMember, DefaultFlag.INVINCIBILITY), is(false));

        assertThat(set.testState(nonMember, DefaultFlag.BUILD), is(false));
    }

    @Test
    public void testStateFlagPriorityFallThrough() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StateFlag state1 = new StateFlag(null, false);
        StateFlag state2 = new StateFlag(null, false);
        StateFlag state3 = new StateFlag(null, false);

        region = mock.add(0);
        region.setFlag(state1, StateFlag.State.ALLOW);
        region.setFlag(state2, StateFlag.State.DENY);
        
        region = mock.add(1);
        region.setFlag(state1, StateFlag.State.DENY);
        region.setFlag(state3, StateFlag.State.ALLOW);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.allows(state1));
        assertFalse(set.allows(state2));
        assertTrue(set.allows(state3));
    }

    @Test
    public void testNonStateFlagPriorityFallThrough() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StringFlag string1 = new StringFlag(null);
        StringFlag string2 = new StringFlag(null);
        StringFlag string3 = new StringFlag(null);
        StringFlag string4 = new StringFlag(null);

        region = mock.add(0);
        region.setFlag(string1, "Beans");
        region.setFlag(string2, "Apples");

        region = mock.add(1);
        region.setFlag(string1, "Cats");
        region.setFlag(string3, "Bananas");

        ApplicableRegionSet set = mock.getApplicableSet();
        assertEquals(set.getFlag(string1), "Cats");
        assertEquals(set.getFlag(string2), "Apples");
        assertEquals(set.getFlag(string3), "Bananas");
        assertEquals(set.getFlag(string4), null);
    }

    @Test
    public void testStateFlagMultiplePriorityFallThrough() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StringFlag string1 = new StringFlag(null);
        StringFlag string2 = new StringFlag(null);
        StringFlag string3 = new StringFlag(null);
        StringFlag string4 = new StringFlag(null);

        region = mock.add(0);
        region.setFlag(string1, "Beans");
        region.setFlag(string2, "Apples");
        region.setFlag(string3, "Dogs");

        region = mock.add(1);
        region.setFlag(string1, "Cats");
        region.setFlag(string3, "Bananas");

        region = mock.add(10);
        region.setFlag(string3, "Strings");

        ApplicableRegionSet set = mock.getApplicableSet();
        assertEquals(set.getFlag(string1), "Cats");
        assertEquals(set.getFlag(string2), "Apples");
        assertEquals(set.getFlag(string3), "Strings");
        assertEquals(set.getFlag(string4), null);
    }

    @Test
    public void testStateGlobalDefault() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StateFlag state1 = new StateFlag(null, false);
        StateFlag state2 = new StateFlag(null, false);
        StateFlag state3 = new StateFlag(null, false);
        StateFlag state4 = new StateFlag(null, true);
        StateFlag state5 = new StateFlag(null, true);
        StateFlag state6 = new StateFlag(null, true);

        region = mock.global();
        region.setFlag(state1, StateFlag.State.ALLOW);
        region.setFlag(state2, StateFlag.State.DENY);
        region.setFlag(state4, StateFlag.State.ALLOW);
        region.setFlag(state5, StateFlag.State.DENY);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.allows(state1));
        assertFalse(set.allows(state2));
        assertFalse(set.allows(state3));
        assertTrue(set.allows(state4));
        assertFalse(set.allows(state5));
        assertTrue(set.allows(state6));
    }

    @Test
    public void testStateGlobalWithRegionsDefault() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        StateFlag state1 = new StateFlag(null, false);
        StateFlag state2 = new StateFlag(null, false);
        StateFlag state3 = new StateFlag(null, false);
        StateFlag state4 = new StateFlag(null, true);
        StateFlag state5 = new StateFlag(null, true);
        StateFlag state6 = new StateFlag(null, true);

        region = mock.global();
        region.setFlag(state1, StateFlag.State.ALLOW);
        region.setFlag(state2, StateFlag.State.DENY);
        region.setFlag(state4, StateFlag.State.ALLOW);
        region.setFlag(state5, StateFlag.State.DENY);

        region = mock.add(0);
        region.setFlag(state1, StateFlag.State.DENY);
        region.setFlag(state2, StateFlag.State.DENY);
        region.setFlag(state4, StateFlag.State.DENY);
        region.setFlag(state5, StateFlag.State.DENY);

        region = mock.add(1);
        region.setFlag(state5, StateFlag.State.ALLOW);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.allows(state1));
        assertFalse(set.allows(state2));
        assertFalse(set.allows(state3));
        assertFalse(set.allows(state4));
        assertTrue(set.allows(state5));
        assertTrue(set.allows(state6));
    }

    @Test
    public void testBuildAccess() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testBuildRegionPriorities() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer upperMember = mock.createPlayer();
        LocalPlayer lowerMember = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(lowerMember);

        region = mock.add(1);
        region.getOwners().addPlayer(upperMember);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(upperMember));
        assertFalse(set.canBuild(lowerMember));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testBuildDenyFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testBuildAllowFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.ALLOW);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertTrue(set.canBuild(nonMember));
    }

    @Test
    public void testHigherPriorityOverrideBuildDenyFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        region = mock.add(1);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.ALLOW);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertTrue(set.canBuild(nonMember));
    }

    @Test
    public void testHigherPriorityUnsetBuildDenyFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        region = mock.add(1);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testPriorityDisjointBuildDenyFlagAndMembership() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        region = mock.add(1);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testPriorityDisjointBuildDenyFlagAndRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        region = mock.add(1);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testPriorityDisjointMembershipAndBuildDenyFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);

        region = mock.add(1);
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testNoGlobalRegionDefaultBuild() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertTrue(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionDefaultBuild() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        @SuppressWarnings("unused")
        ProtectedRegion region = mock.global();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertTrue(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionBuildFlagAllow() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        region = mock.global();
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.ALLOW);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertTrue(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionBuildFlagDeny() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        region = mock.global();
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionBuildFlagAllowWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.ALLOW);

        region = mock.add(0);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionBuildFlagDenyWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);

        region = mock.add(0);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionHavingOwnershipBuildFlagUnset() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionHavingOwnershipBuildFlagAllow() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.ALLOW);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionHavingOwnershipBuildFlagDeny() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(DefaultFlag.BUILD, StateFlag.State.DENY);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.canBuild(member));
        assertFalse(set.canBuild(nonMember));
    }

    @Test
    public void testGlobalRegionCommandBlacklistWithRegionWhitelist() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        Set<String> blocked = new HashSet<String>();
        blocked.add("/deny");
        blocked.add("/strange");
        region.setFlag(DefaultFlag.BLOCKED_CMDS, blocked);

        region = mock.add(0);
        Set<String> allowed = new HashSet<String>();
        allowed.add("/permit");
        allowed.add("/strange");
        region.setFlag(DefaultFlag.ALLOWED_CMDS, allowed);

        ApplicableRegionSet set;
        CommandFilter test;

        set = mock.getApplicableSet();
        test = new CommandFilter(
                set.getFlag(DefaultFlag.ALLOWED_CMDS, nonMember),
                set.getFlag(DefaultFlag.BLOCKED_CMDS, nonMember));
        assertThat(test.apply("/permit"), is(true));
        assertThat(test.apply("/strange"), is(true));
        assertThat(test.apply("/other"), is(false));
        assertThat(test.apply("/deny"), is(false));

        set = mock.getApplicableSetInWilderness();
        test = new CommandFilter(
                set.getFlag(DefaultFlag.ALLOWED_CMDS, nonMember),
                set.getFlag(DefaultFlag.BLOCKED_CMDS, nonMember));
        assertThat(test.apply("/permit"), is(true));
        assertThat(test.apply("/strange"), is(false));
        assertThat(test.apply("/other"), is(true));
        assertThat(test.apply("/deny"), is(false));
    }

}
