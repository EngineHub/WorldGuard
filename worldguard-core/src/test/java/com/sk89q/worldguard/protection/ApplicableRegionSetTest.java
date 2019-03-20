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
import com.sk89q.worldguard.protection.flags.Flags;
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
        assertThat(set.testState(player, Flags.BUILD), is(true));
    }

    @Test
    public void testWildernessBuildWithGlobalRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ProtectedRegion global = mock.global();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertThat(set.testState(player, Flags.BUILD), is(true));
    }

    @Test
    public void testWildernessBuildWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertThat(set.testState(member, Flags.BUILD), is(true));
        assertThat(set.testState(nonMember, Flags.BUILD), is(false));
    }

    @Test
    public void testWildernessFlags() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();

        assertThat(set.testState(player, Flags.MOB_DAMAGE), is(true));
        assertThat(set.testState(player, Flags.ENTRY), is(true));
        assertThat(set.testState(player, Flags.EXIT), is(true));
        assertThat(set.testState(player, Flags.LEAF_DECAY), is(true));
        assertThat(set.testState(player, Flags.RECEIVE_CHAT), is(true));
        assertThat(set.testState(player, Flags.SEND_CHAT), is(true));
        assertThat(set.testState(player, Flags.INVINCIBILITY), is(false));

        assertThat(set.testState(player, Flags.BUILD), is(true));
    }

    @Test
    public void testWildernessFlagsWithGlobalRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ProtectedRegion global = mock.global();

        ApplicableRegionSet set = mock.getApplicableSet();

        assertThat(set.testState(player, Flags.MOB_DAMAGE), is(true));
        assertThat(set.testState(player, Flags.ENTRY), is(true));
        assertThat(set.testState(player, Flags.EXIT), is(true));
        assertThat(set.testState(player, Flags.LEAF_DECAY), is(true));
        assertThat(set.testState(player, Flags.RECEIVE_CHAT), is(true));
        assertThat(set.testState(player, Flags.SEND_CHAT), is(true));
        assertThat(set.testState(player, Flags.INVINCIBILITY), is(false));

        assertThat(set.testState(player, Flags.BUILD), is(true));
    }

    @Test
    public void testFlagsWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();

        assertThat(set.testState(member, Flags.MOB_DAMAGE), is(true));
        assertThat(set.testState(member, Flags.ENTRY), is(true));
        assertThat(set.testState(member, Flags.EXIT), is(true));
        assertThat(set.testState(member, Flags.LEAF_DECAY), is(true));
        assertThat(set.testState(member, Flags.RECEIVE_CHAT), is(true));
        assertThat(set.testState(member, Flags.SEND_CHAT), is(true));
        assertThat(set.testState(member, Flags.INVINCIBILITY), is(false));

        assertThat(set.testState(member, Flags.BUILD), is(true));

        assertThat(set.testState(nonMember, Flags.MOB_DAMAGE), is(true));
        assertThat(set.testState(nonMember, Flags.ENTRY), is(true));
        assertThat(set.testState(nonMember, Flags.EXIT), is(true));
        assertThat(set.testState(nonMember, Flags.LEAF_DECAY), is(true));
        assertThat(set.testState(nonMember, Flags.RECEIVE_CHAT), is(true));
        assertThat(set.testState(nonMember, Flags.SEND_CHAT), is(true));
        assertThat(set.testState(nonMember, Flags.INVINCIBILITY), is(false));

        assertThat(set.testState(nonMember, Flags.BUILD), is(false));
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
        assertFalse(set.testState(null, state1));
        assertFalse(set.testState(null, state2));
        assertTrue(set.testState(null, state3));
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
        assertEquals(set.queryValue(null, string1), "Cats");
        assertEquals(set.queryValue(null, string2), "Apples");
        assertEquals(set.queryValue(null, string3), "Bananas");
        assertNull(set.queryValue(null, string4));
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
        assertEquals(set.queryValue(null, string1), "Cats");
        assertEquals(set.queryValue(null, string2), "Apples");
        assertEquals(set.queryValue(null, string3), "Strings");
        assertNull(set.queryValue(null, string4));
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
        assertTrue(set.testState(null, state1));
        assertFalse(set.testState(null, state2));
        assertFalse(set.testState(null, state3));
        assertTrue(set.testState(null, state4));
        assertFalse(set.testState(null, state5));
        assertTrue(set.testState(null, state6));
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
        assertFalse(set.testState(null, state1));
        assertFalse(set.testState(null, state2));
        assertFalse(set.testState(null, state3));
        assertFalse(set.testState(null, state4));
        assertTrue(set.testState(null, state5));
        assertTrue(set.testState(null, state6));
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
        assertTrue(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
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
        assertTrue(set.testState(upperMember, Flags.BUILD));
        assertFalse(set.testState(lowerMember, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testBuildDenyFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testBuildAllowFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertTrue(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testHigherPriorityOverrideBuildDenyFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        region = mock.add(1);
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertTrue(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testHigherPriorityUnsetBuildDenyFlag() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.getOwners().addPlayer(member);
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        region = mock.add(1);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testPriorityDisjointBuildDenyFlagAndMembership() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        region = mock.add(1);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testPriorityDisjointBuildDenyFlagAndRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.add(0);
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        region = mock.add(1);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
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
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testNoGlobalRegionDefaultBuild() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertTrue(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionDefaultBuild() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        @SuppressWarnings("unused")
        ProtectedRegion region = mock.global();

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertTrue(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionBuildFlagAllow() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        region = mock.global();
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertTrue(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionBuildFlagDeny() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        region = mock.global();
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionBuildFlagAllowWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);

        region = mock.add(0);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionBuildFlagDenyWithRegion() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);

        region = mock.add(0);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
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
        assertTrue(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionHavingOwnershipBuildFlagAllow() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertTrue(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionHavingOwnershipBuildFlagDeny() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);
        region.getOwners().addPlayer(member);

        ApplicableRegionSet set = mock.getApplicableSet();
        assertFalse(set.testState(member, Flags.BUILD));
        assertFalse(set.testState(nonMember, Flags.BUILD));
    }

    @Test
    public void testGlobalRegionCommandBlacklistWithRegionWhitelist() {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();
        ProtectedRegion region;

        LocalPlayer nonMember = mock.createPlayer();

        region = mock.global();
        Set<String> blocked = new HashSet<>();
        blocked.add("/deny");
        blocked.add("/strange");
        region.setFlag(Flags.BLOCKED_CMDS, blocked);

        region = mock.add(0);
        Set<String> allowed = new HashSet<>();
        allowed.add("/permit");
        allowed.add("/strange");
        region.setFlag(Flags.ALLOWED_CMDS, allowed);

        ApplicableRegionSet set;
        CommandFilter test;

        set = mock.getApplicableSet();
        test = new CommandFilter(
                set.queryValue(nonMember, Flags.ALLOWED_CMDS),
                set.queryValue(nonMember, Flags.BLOCKED_CMDS));
        assertThat(test.apply("/permit"), is(true));
        assertThat(test.apply("/strange"), is(true));
        assertThat(test.apply("/other"), is(false));
        assertThat(test.apply("/deny"), is(false));

        set = mock.getApplicableSetInWilderness();
        test = new CommandFilter(
                set.queryValue(nonMember, Flags.ALLOWED_CMDS),
                set.queryValue(nonMember, Flags.BLOCKED_CMDS));
        assertThat(test.apply("/permit"), is(true));
        assertThat(test.apply("/strange"), is(false));
        assertThat(test.apply("/other"), is(true));
        assertThat(test.apply("/deny"), is(false));
    }

}
