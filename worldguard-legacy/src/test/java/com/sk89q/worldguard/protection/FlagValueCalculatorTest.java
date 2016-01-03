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
import com.sk89q.worldguard.protection.FlagValueCalculator.Result;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SuppressWarnings({"UnusedAssignment", "UnusedDeclaration"})
public class FlagValueCalculatorTest {

    @Test
    public void testGetMembershipWilderness() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.NO_REGIONS));
    }

    @Test
    public void testGetMembershipWildernessWithGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer player = mock.createPlayer();

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.NO_REGIONS));
    }

    @Test
    public void testGetMembershipGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion global = mock.global();

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.NO_REGIONS));
    }

    @Test
    public void testGetMembershipGlobalRegionAndRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion global = mock.global();

        ProtectedRegion region = mock.add(0);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.FAIL));
    }

    @Test
    public void testGetMembershipPassthroughRegions() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        region = mock.add(0);
        region.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.NO_REGIONS));
    }

    @Test
    public void testGetMembershipPassthroughAndRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        region = mock.add(0);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.FAIL));
    }

    @Test
    public void testGetMembershipPassthroughAndRegionMemberOf() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        region = mock.add(0);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.SUCCESS));
    }

    @Test
    public void testGetMembershipPassthroughAndRegionMemberOfAndAnotherNot() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        region = mock.add(0);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        region = mock.add(0);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.FAIL));

        // Add another player (should still fail)
        region.getMembers().addPlayer(mock.createPlayer());

        result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.FAIL));
    }

    @Test
    public void testGetMembershipPassthroughAndRegionMemberOfAndAnotherNotWithHigherPriority() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        region = mock.add(0);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        region = mock.add(10);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.FAIL));
    }

    @Test
    public void testGetMembershipPassthroughAndRegionMemberOfWithHigherPriorityAndAnotherNot() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        region = mock.add(10);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        region = mock.add(0);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.SUCCESS));
    }

    @Test
    public void testGetMembershipPassthroughAndRegionMemberOfWithAnotherParent() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion passthrough = mock.add(0);
        passthrough.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        ProtectedRegion parent = mock.add(0);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.SUCCESS));
    }

    @Test
    public void testGetMembershipPassthroughAndRegionMemberOfWithAnotherChild() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion passthrough = mock.add(0);
        passthrough.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        ProtectedRegion parent = mock.add(0);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent);

        LocalPlayer player = mock.createPlayer();
        parent.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.SUCCESS));
    }

    @Test
    public void testGetMembershipPassthroughAndRegionMemberOfWithAnotherChildAndAnother() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion passthrough = mock.add(0);
        passthrough.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        ProtectedRegion parent = mock.add(0);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent);

        region = mock.add(0);

        LocalPlayer player = mock.createPlayer();
        parent.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.FAIL));
    }

    @Test
    public void testGetMembershipThirdPriorityLower() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion passthrough = mock.add(0);
        passthrough.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        ProtectedRegion parent = mock.add(0);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent);

        region = mock.add(0);
        region.setPriority(-5);

        LocalPlayer player = mock.createPlayer();
        parent.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getMembership(player), is(Result.SUCCESS));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testQueryStateWilderness() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryState(null, flag1), is((State) null));
        assertThat(result.queryState(null, flag2), is(State.ALLOW));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testQueryValueSingleRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag2, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag1), is((State) null));
        assertThat(result.queryValue(null, flag2), is(State.DENY));
    }

    @Test
    public void testQueryValueDenyOverridesAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag2, State.DENY);

        region = mock.add(0);
        region.setFlag(flag2, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag1), is((State) null));
        assertThat(result.queryValue(null, flag2), is(State.DENY));
    }

    @Test
    public void testQueryValueAllowOverridesNone() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);

        region = mock.add(0);
        region.setFlag(flag2, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag1), is((State) null));
        assertThat(result.queryValue(null, flag2), is(State.ALLOW));
    }

    @Test
    public void testQueryValueMultipleFlags() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag1, State.DENY);
        region.setFlag(flag2, State.ALLOW);

        region = mock.add(0);
        region.setFlag(flag2, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag1), is(State.DENY));
        assertThat(result.queryValue(null, flag2), is(State.DENY));
        assertThat(result.queryValue(null, flag3), is((State) null));
    }

    @Test
    public void testQueryValueFlagsWithRegionGroupsAndInheritance() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer member = mock.createPlayer();

        ProtectedRegion parent = mock.add(0);
        parent.setFlag(flag1, State.DENY);
        parent.setFlag(flag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);
        region.setParent(parent);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, flag1), is(State.DENY));
        assertThat(result.queryValue(member, flag1), is((State) null));
    }

    @Test
    public void testQueryValueFlagsWithRegionGroupsAndInheritanceAndParentMember() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberTwo = mock.createPlayer();

        ProtectedRegion parent = mock.add(0);
        parent.getMembers().addPlayer(memberOne);
        parent.setFlag(flag1, State.DENY);
        parent.setFlag(flag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.setParent(parent);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, flag1), is(State.DENY));
        assertThat(result.queryValue(memberOne, flag1), is((State) null));
        assertThat(result.queryValue(memberTwo, flag1), is(State.DENY));
    }

    @Test
    public void testQueryValueFlagsWithRegionGroupsAndPriority() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer member = mock.createPlayer();

        ProtectedRegion lower = mock.add(-1);
        lower.setFlag(flag1, State.DENY);
        lower.setFlag(flag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, flag1), is(State.DENY));
        assertThat(result.queryValue(member, flag1), is(State.DENY));
    }

    @Test
    public void testQueryValueFlagsWithRegionGroupsAndPriorityAndOveride() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer member = mock.createPlayer();

        ProtectedRegion lower = mock.add(-1);
        lower.setFlag(flag1, State.DENY);
        lower.setFlag(flag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag1, State.ALLOW);
        region.setFlag(flag1.getRegionGroupFlag(), RegionGroup.MEMBERS);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, flag1), is(State.DENY));
        assertThat(result.queryValue(member, flag1), is(State.ALLOW));
    }

    @Test
    public void testQueryValueStringFlag() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");
        StateFlag flag1 = new StateFlag("test1", false);

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");

        region = mock.add(0);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, stringFlag1), isOneOf("test1", "test2"));
        assertThat(result.queryValue(null, stringFlag2), is((String) null));
        assertThat(result.queryValue(null, flag1), is((State) null));
    }

    @Test
    public void testQueryValueEmptyGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is(State.ALLOW));
    }

    @Test
    public void testQueryValueGlobalRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion global = mock.global();
        global.setFlag(flag, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is(State.ALLOW));
    }

    @Test
    public void testQueryValueGlobalRegionDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion global = mock.global();
        global.setFlag(flag, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is(State.DENY));
    }

    @Test
    public void testQueryValueStringFlagWithGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is((String) null));
    }

    @Test
    public void testQueryValueStringFlagWithGlobalRegionValueSet() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion global = mock.global();
        global.setFlag(flag, "hello");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is("hello"));
    }

    @Test
    public void testQueryValueStringFlagWithGlobalRegionAndRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion global = mock.global();
        global.setFlag(flag, "hello");

        ProtectedRegion region = mock.add(0);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is("hello"));
    }

    @Test
    public void testQueryValueStringFlagWithGlobalRegionAndRegionOverride() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion global = mock.global();
        global.setFlag(flag, "hello");

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag, "beep");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is("beep"));
    }

    @Test
    public void testQueryValueStringFlagWithEverything() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test", RegionGroup.ALL);

        ProtectedRegion global = mock.global();
        global.setFlag(flag, "hello");

        ProtectedRegion parent = mock.add(0);
        parent.setFlag(flag, "ello there");

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag, "beep beep");
        region.setFlag(flag.getRegionGroupFlag(), RegionGroup.MEMBERS);
        region.setParent(parent);

        LocalPlayer nonMember = mock.createPlayer();

        LocalPlayer member = mock.createPlayer();
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(null, flag), is("ello there"));
        assertThat(result.queryValue(nonMember, flag), is("ello there"));
        assertThat(result.queryValue(member, flag), is("beep beep"));
    }

    @Test
    public void testQueryValueBuildFlagWilderness() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagWildernessAndGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagWildernessAndGlobalRegionDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(DefaultFlag.BUILD, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is(State.DENY));
    }

    @Test
    public void testQueryValueBuildFlagWildernessAndGlobalRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(DefaultFlag.BUILD, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagWildernessAndGlobalRegionMembership() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer member = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(member, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
    }

    @Test
    public void testQueryValueBuildFlagWildernessAndGlobalRegionMembershipAndDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer member = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.getMembers().addPlayer(member);
        global.setFlag(DefaultFlag.BUILD, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(member, DefaultFlag.BUILD), is(State.DENY));
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is(State.DENY));
    }

    @Test
    public void testQueryValueBuildFlagWildernessAndGlobalRegionMembershipAndAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer member = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.getMembers().addPlayer(member);
        global.setFlag(DefaultFlag.BUILD, State.ALLOW);

        // Cannot set ALLOW on BUILD

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(member, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
    }

    @Test
    public void testQueryValueBuildFlagRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer member = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(member, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlapping() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingDifferingPriority() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);
        region.setPriority(10);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingInheritanceFromParent() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion parent = mock.add(0);
        parent.getMembers().addPlayer(memberOne);
        parent.getMembers().addPlayer(memberBoth);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);
        region.setParent(parent);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingInheritanceFromChild() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion parent = mock.add(0);
        parent.getMembers().addPlayer(memberBoth);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);
        region.getMembers().addPlayer(memberOne);
        region.setParent(parent);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingInheritanceFromChildAndPriority() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion parent = mock.add(0);
        parent.getMembers().addPlayer(memberBoth);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);
        region.getMembers().addPlayer(memberOne);
        region.setParent(parent);

        ProtectedRegion priority = mock.add(10);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is((State) null));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingInheritanceFromChildAndPriorityPassthrough() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion parent = mock.add(0);
        parent.getMembers().addPlayer(memberBoth);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);
        region.getMembers().addPlayer(memberOne);
        region.setParent(parent);

        ProtectedRegion priority = mock.add(10);
        priority.setFlag(DefaultFlag.PASSTHROUGH, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingAndGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion global = mock.global();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingAndGlobalRegionDenyRegionOverride() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(DefaultFlag.BUILD, State.DENY);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);
        region.setFlag(DefaultFlag.BUILD, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is(State.ALLOW));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingAndGlobalRegionDenyRegionOverrideDenyAndAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(DefaultFlag.BUILD, State.DENY);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);
        region.setFlag(DefaultFlag.BUILD, State.DENY);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);
        region.setFlag(DefaultFlag.BUILD, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is(State.DENY));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is(State.DENY));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.DENY));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingAndGlobalRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(DefaultFlag.BUILD, State.ALLOW);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);

        // Disable setting ALLOW for safety reasons

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingAndGlobalRegionMembership() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer globalMember = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.getMembers().addPlayer(globalMember);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(globalMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingAndGlobalRegionMembershipAndGlobalDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer globalMember = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.getMembers().addPlayer(globalMember);
        global.setFlag(DefaultFlag.BUILD, State.DENY);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);

        FlagValueCalculator result = mock.getFlagCalculator();
        // Inconsistent due to legacy reasons
        assertThat(result.queryValue(globalMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    @Test
    public void testQueryValueBuildFlagRegionsOverlappingAndGlobalRegionMembershipAndGlobalAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        LocalPlayer globalMember = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();
        LocalPlayer memberOne = mock.createPlayer();
        LocalPlayer memberBoth = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.getMembers().addPlayer(globalMember);
        global.setFlag(DefaultFlag.BUILD, State.ALLOW);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(memberOne);
        region.getMembers().addPlayer(memberBoth);

        region = mock.add(0);
        region.getMembers().addPlayer(memberBoth);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.queryValue(globalMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(nonMember, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberOne, DefaultFlag.BUILD), is((State) null));
        assertThat(result.queryValue(memberBoth, DefaultFlag.BUILD), is(State.ALLOW));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testQueryAllValuesTwoWithSamePriority() throws Exception {
        // ====================================================================
        // Two regions with the same priority
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");

        region = mock.add(0);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test1", "test2")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesTwoWithDuplicateFlagValues() throws Exception {
        // ====================================================================
        // Two regions with duplicate values
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test");

        region = mock.add(0);
        region.setFlag(stringFlag1, "test");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test", "test")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesWithHigherPriority() throws Exception {
        // ====================================================================
        // One of the regions has a higher priority (should override)
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(10);
        region.setFlag(stringFlag1, "test1");

        region = mock.add(0);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test1")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesWithTwoElevatedPriorities() throws Exception {
        // ====================================================================
        // Two regions with the same elevated priority
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(10);
        region.setFlag(stringFlag1, "test3");

        region = mock.add(10);
        region.setFlag(stringFlag1, "test1");

        region = mock.add(0);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test1", "test3")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesParentChildWithSamePriority() throws Exception {
        // ====================================================================
        // Child region and parent region with the same priority
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.add(10);
        parent1.setFlag(stringFlag1, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(stringFlag1, "test1");
        region.setParent(parent1);

        region = mock.add(0);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test1")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesParentWithHigherPriority() throws Exception {
        // ====================================================================
        // Parent region with a higher priority than the child
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.add(20);
        parent1.setFlag(stringFlag1, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(stringFlag1, "test1");
        region.setParent(parent1);

        region = mock.add(0);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test3")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesParentWithLowerPriority() throws Exception {
        // ====================================================================
        // Parent region with a lower priority than the child
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(stringFlag1, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(stringFlag1, "test1");
        region.setParent(parent1);

        region = mock.add(0);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test1")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesThirdRegionWithHigherPriorityThanParentChild() throws Exception {
        // ====================================================================
        // Third region with higher priority than parent and child
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(stringFlag1, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(stringFlag1, "test1");
        region.setParent(parent1);

        region = mock.add(20);
        region.setFlag(stringFlag1, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test2")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesParentsAndInheritance() throws Exception {
        // ====================================================================
        // Multiple regions with parents, one region using flag from parent
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(stringFlag1, "test1");

        ProtectedRegion region = mock.add(20);
        region.setFlag(stringFlag1, "test2");
        region.setParent(parent1);

        ProtectedRegion parent2 = mock.add(6);
        parent2.setFlag(stringFlag1, "test3");

        region = mock.add(20);
        region.setParent(parent2);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test2", "test3")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesParentsAndInheritanceHighPriorityAndNoFlag() throws Exception {
        // ====================================================================
        // Multiple regions with parents, one region with high priority but no flag
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");
        StateFlag flag1 = new StateFlag("test1", false);

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(stringFlag1, "test1");

        ProtectedRegion region = mock.add(20);
        region.setFlag(stringFlag1, "test2");
        region.setParent(parent1);

        ProtectedRegion parent2 = mock.add(6);
        parent2.setFlag(stringFlag1, "test3");

        region = mock.add(30);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test2")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    @Test
    public void testQueryAllValuesParentWithSamePriorityAsHighest() throws Exception {
        // ====================================================================
        // As before, except a parent region has the same priority as the previous highest
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");
        StateFlag flag1 = new StateFlag("test1", false);

        ProtectedRegion parent1 = mock.add(30);
        parent1.setFlag(stringFlag1, "test1");

        ProtectedRegion region = mock.add(20);
        region.setFlag(stringFlag1, "test2");
        region.setParent(parent1);

        ProtectedRegion parent2 = mock.add(6);
        parent2.setFlag(stringFlag1, "test3");

        region = mock.add(30);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.queryAllValues(null, stringFlag1)), equalTo(of("test1")));
        assertThat(result.queryAllValues(null, stringFlag2), is(Matchers.<String>empty()));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testGetEffectivePriority() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(30);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getPriority(region), is(30));
    }

    @Test
    public void testGetEffectivePriorityGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getPriority(region), is(Integer.MIN_VALUE));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testGetEffectiveFlagSingleRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        // ====================================================================
        // Single region
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagWithALLGroupAndNonMember() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        // ====================================================================
        // Single region with group ALL and non-member player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagWithALLGroupAndNull() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        // ====================================================================
        // Single region with group ALL and null player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONMEMBERSGroupNonMember() throws Exception {
        // ====================================================================
        // Single region with group NON-MEMBERS and non-member player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONMEMBERSGroupNull() throws Exception {
        // ====================================================================
        // Single region with group NON-MEMBERS and null player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSGroupNonMember() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and non-member player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupNonMember() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and non-member player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupNull() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and null player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo(null));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupMember() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and member player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and owner player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagOWNERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group OWNERS and owner player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagOWNERSGroupMember() throws Exception {
        // ====================================================================
        // Single region with group OWNERS and member player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and owner player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONMEMBERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group NON-MEMBERS and owner player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSGroupMember() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSNonMember() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and non-member player
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "test1");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagThreeInheritance() throws Exception {
        // ====================================================================
        // Three-level inheritance
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(stringFlag1, "test1");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagThreeInheritanceMiddleOverride() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on middle level
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(stringFlag1, "test1");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);
        parent2.setFlag(stringFlag1, "test2");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("test2"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagThreeInheritanceLastOverride() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on last level
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(stringFlag1, "test1");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);
        region.setFlag(stringFlag1, "test3");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("test3"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroups() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on last level
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(stringFlag1, "everyone");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setFlag(stringFlag1, "members");
        parent2.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent2.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnChild() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on last level
        // ====================================================================

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(stringFlag1, "everyone");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setFlag(stringFlag1, "members");
        parent2.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        region.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnParent() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(stringFlag1, "everyone");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setFlag(stringFlag1, "members");
        parent2.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent1.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnParentFlagOnBottom() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(stringFlag1, "everyone");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "members");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent1.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnParentFlagOnBottomGroupOutside() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag stringFlag1 = new StringFlag("string1");
        StringFlag stringFlag2 = new StringFlag("string2");

        ProtectedRegion parent1 = mock.createOutside(0);
        parent1.setFlag(stringFlag1, "everyone");
        parent1.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setFlag(stringFlag1, "members");
        region.setFlag(stringFlag1.getRegionGroupFlag(), RegionGroup.MEMBERS);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent1.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, stringFlag1, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag1, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, stringFlag2, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagGlobalRegionBuild() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(global, DefaultFlag.BUILD, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagGlobalRegionBuildDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion global = mock.global();
        global.setFlag(DefaultFlag.BUILD, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        // Cannot let users override BUILD on GLOBAL
        assertThat(result.getEffectiveFlag(global, DefaultFlag.BUILD, null), equalTo(State.DENY));
    }

    @Test
    public void testGetEffectiveFlagGlobalRegionBuildAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion global = mock.global();
        global.setFlag(DefaultFlag.BUILD, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        // Cannot let users override BUILD on GLOBAL
        assertThat(result.getEffectiveFlag(global, DefaultFlag.BUILD, null), equalTo(null));
    }
}