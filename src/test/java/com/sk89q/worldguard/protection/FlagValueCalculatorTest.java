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
    public void testGetSingleFallbackMembershipWilderness() throws Exception {
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
    public void testTestPermissionWildernessDefaults() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(player, flag1), is((State) null));
        assertThat(result.testPermission(player, flag2), is(State.ALLOW));
    }

    @Test
    public void testTestPermissionWildernessDefaultsWithGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer player = mock.createPlayer();

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(player, flag1), is((State) null));
        assertThat(result.testPermission(player, flag2), is(State.ALLOW));
    }

    @Test
    public void testTestPermissionWildernessDefaultsWithGlobalRegionOverride() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer player = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.ALLOW);
        global.setFlag(flag2, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(player, flag1), is(State.ALLOW));
        assertThat(result.testPermission(player, flag2), is((State) null));
    }

    @Test
    public void testTestPermissionWildernessWithGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.global();
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is((State) null));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember, flag1, flag2), is((State) null));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    @Test
    public void testTestPermissionWildernessWithGlobalRegionOverride() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.global();
        region.setFlag(flag2, State.ALLOW);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is((State) null));
        assertThat(result.testPermission(nonMember, flag2), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    @Test
    public void testTestPermissionWithRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is((State) null));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember, flag1, flag2), is((State) null));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    @Test
    public void testTestPermissionWithRegionAndFlagAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);
        region.setFlag(flag1, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    @Test
    public void testTestPermissionWithRegionAndFlagDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);
        region.setFlag(flag1, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.DENY));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.DENY));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is(State.DENY));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember, flag1, flag2), is(State.DENY));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    @Test
    public void testTestPermissionWithRegionAndFlagDenyAndRegionGroup() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);
        region.setFlag(flag1, State.DENY);
        region.setFlag(flag1.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is(State.DENY));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember, flag1, flag2), is(State.DENY));
    }

    @Test
    public void testTestPermissionWithRegionAndGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion global = mock.global();

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is((State) null));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember, flag1, flag2), is((State) null));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    @Test
    public void testTestPermissionWithRegionAndGlobalRegionDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.DENY); // No effect

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is((State) null));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember, flag1, flag2), is((State) null));
    }

    @Test
    public void testTestPermissionWithRegionAndGlobalRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.ALLOW); // No effect

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is((State) null));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    @Test
    public void testTestPermissionWithRegionAndGlobalRegionMembership() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);

        LocalPlayer member = mock.createPlayer();
        LocalPlayer nonMember = mock.createPlayer();

        ProtectedRegion global = mock.global();
        global.getMembers().addPlayer(nonMember);

        ProtectedRegion region = mock.add(0);
        region.getMembers().addPlayer(member);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.testPermission(member, flag1), is(State.ALLOW));
        assertThat(result.testPermission(member, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member, flag1, flag2), is(State.ALLOW));
        assertThat(result.testPermission(member), is(State.ALLOW));
        assertThat(result.testPermission(nonMember, flag1), is((State) null));
        assertThat(result.testPermission(nonMember, flag2), is((State) null));
        assertThat(result.testPermission(nonMember), is((State) null));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testGetStateWithFallbackSingle() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", false);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((State) null));
    }

    @Test
    public void testGetStateWithFallbackSeveralNoneAreDenyNoneAreTrue() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is((State) null));
    }

    @Test
    public void testGetStateWithFallbackSeveralNoneAreDenyOneIsTrue() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", true);
        StateFlag flag3 = new StateFlag("test3", false);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is(State.ALLOW));
    }

    @Test
    public void testGetStateWithFallbackSeveralNoneAreDenyNoneAreTrueWithEmptyGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is((State) null));
    }

    @Test
    public void testGetStateWithFallbackSeveralNoneAreDenyNoneAreTrueWithGlobalRegionValueSetAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is(State.ALLOW));
    }

    @Test
    public void testGetStateWithFallbackSeveralNoneAreDenySomeAreTrueWithGlobalRegionValueSetDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", true);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is((State) null));
    }

    @Test
    public void testGetStateWithFallbackWithGlobalRegionAllowAndRegionDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.ALLOW);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag1, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is(State.DENY));
    }

    @Test
    public void testGetStateWithFallbackWithGlobalRegionAllowAndRegionDenyOnDifferentFlag() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.ALLOW);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag2, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is(State.DENY));
    }

    @Test
    public void testGetStateWithFallbackWithGlobalRegionDenyAndRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion global = mock.global();
        global.setFlag(flag1, State.DENY);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag1, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is(State.ALLOW));
    }

    @Test
    public void testGetStateWithFallbackWithGlobalRegionDenyOnDifferentAndRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag1 = new StateFlag("test1", false);
        StateFlag flag2 = new StateFlag("test2", false);
        StateFlag flag3 = new StateFlag("test3", false);

        ProtectedRegion global = mock.global();
        global.setFlag(flag2, State.DENY);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag1, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getStateWithFallback(null, flag1, flag2, flag3), is(State.ALLOW));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testGetSingleValueWithFallbackWithFalseDefaultValue() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", false);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((State) null));
    }

    @Test
    public void testGetSingleValueWithFallbackWithTrueDefaultValue() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueWithFallbackWithFalseDefaultValueEmptyGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", false);

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((State) null));
    }

    @Test
    public void testGetSingleValueWithFallbackWithTrueDefaultValueEmptyGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueWithFallbackWithDefaultValueSameGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion global = mock.global();
        global.setFlag(flag, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueWithFallbackWithTrueDefaultValueDenyGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion global = mock.global();
        global.setFlag(flag, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((State) null));
    }

    @Test
    public void testGetSingleValueWithFallbackWithFalseDefaultValueAllowGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", false);

        ProtectedRegion global = mock.global();
        global.setFlag(flag, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueWithFallbackWithStringFlag() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((String) null));
    }

    @Test
    public void testGetSingleValueWithFallbackWithStringFlagEmptyGlobalRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion global = mock.global();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((String) null));
    }

    @Test
    public void testGetSingleValueWithFallbackWithStringFlagGlobalRegionValueSet() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion global = mock.global();
        global.setFlag(flag, "hello there");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is("hello there"));
    }

    @Test
    public void testGetSingleValueWithFallbackWithFalseDefaultValueAndEmptyRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", false);

        ProtectedRegion region = mock.add(0);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((State) null));
    }

    @Test
    public void testGetSingleValueWithFallbackWithTrueDefaultValueAndEmptyRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion region = mock.add(0);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueWithFallbackWithTrueDefaultValueAndRegionDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.DENY));
    }

    @Test
    public void testGetSingleValueWithFallbackWithTrueDefaultValueAndRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", true);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueWithFallbackWithFalseDefaultValueAndRegionAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", false);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueWithFallbackWithFalseDefaultValueAndRegionDeny() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StateFlag flag = new StateFlag("test", false);

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is(State.DENY));
    }

    @Test
    public void testGetSingleValueWithFallbackWithStringFlagAndEmptyRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion region = mock.add(0);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is((String) null));
    }

    @Test
    public void testGetSingleValueWithFallbackWithStringFlagAndRegionValueSet() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion region = mock.add(0);
        region.setFlag(flag, "beep beep");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is("beep beep"));
    }

    @Test
    public void testGetSingleValueWithFallbackWithStringFlagAndRegionValueSetAndPriority() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test");

        ProtectedRegion region = mock.add(10);
        region.setFlag(flag, "ello there");

        region = mock.add(0);
        region.setFlag(flag, "beep beep");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValueWithFallback(null, flag), is("ello there"));
    }

    @Test
    public void testGetSingleValueWithFallbackWithStringFlagAndRegionValueSetAndInheritanceAndRegionGroup() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        StringFlag flag = new StringFlag("test", RegionGroup.ALL);

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
        assertThat(result.getSingleValueWithFallback(null, flag), is("ello there"));
        assertThat(result.getSingleValueWithFallback(nonMember, flag), is("ello there"));
        assertThat(result.getSingleValueWithFallback(member, flag), is("beep beep"));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testGetSingleValueSingleRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PVP, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValue(null, DefaultFlag.LIGHTER), is((State) null));
        assertThat(result.getSingleValue(null, DefaultFlag.PVP), is(State.DENY));
    }

    @Test
    public void testGetSingleValueDenyOverridesAllow() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.PVP, State.DENY);

        region = mock.add(0);
        region.setFlag(DefaultFlag.PVP, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValue(null, DefaultFlag.LIGHTER), is((State) null));
        assertThat(result.getSingleValue(null, DefaultFlag.PVP), is(State.DENY));
    }

    @Test
    public void testGetSingleValueAllowOverridesNone() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);

        region = mock.add(0);
        region.setFlag(DefaultFlag.PVP, State.ALLOW);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValue(null, DefaultFlag.LIGHTER), is((State) null));
        assertThat(result.getSingleValue(null, DefaultFlag.PVP), is(State.ALLOW));
    }

    @Test
    public void testGetSingleValueMultipleFlags() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.LIGHTER, State.DENY);
        region.setFlag(DefaultFlag.PVP, State.ALLOW);

        region = mock.add(0);
        region.setFlag(DefaultFlag.PVP, State.DENY);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValue(null, DefaultFlag.LIGHTER), is(State.DENY));
        assertThat(result.getSingleValue(null, DefaultFlag.PVP), is(State.DENY));
        assertThat(result.getSingleValue(null, DefaultFlag.LAVA_FIRE), is((State) null));
    }

    @Test
    public void testGetSingleValueStringFlag() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getSingleValue(null, DefaultFlag.FAREWELL_MESSAGE), isOneOf("test1", "test2"));
        assertThat(result.getSingleValue(null, DefaultFlag.GREET_MESSAGE), is((String) null));
        assertThat(result.getSingleValue(null, DefaultFlag.LAVA_FIRE), is((State) null));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testGetValuesTwoWithSamePriority() throws Exception {
        // ====================================================================
        // Two regions with the same priority
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test1", "test2")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesTwoWithDuplicateFlagValues() throws Exception {
        // ====================================================================
        // Two regions with duplicate values
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test");

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test", "test")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesWithHigherPriority() throws Exception {
        // ====================================================================
        // One of the regions has a higher priority (should override)
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(10);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test1")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesWithTwoElevatedPriorities() throws Exception {
        // ====================================================================
        // Two regions with the same elevated priority
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(10);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        region = mock.add(10);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test1", "test3")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesParentChildWithSamePriority() throws Exception {
        // ====================================================================
        // Child region and parent region with the same priority
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(10);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setParent(parent1);

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test1")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesParentWithHigherPriority() throws Exception {
        // ====================================================================
        // Parent region with a higher priority than the child
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(20);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setParent(parent1);

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test3")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesParentWithLowerPriority() throws Exception {
        // ====================================================================
        // Parent region with a lower priority than the child
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setParent(parent1);

        region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test1")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesThirdRegionWithHigherPriorityThanParentChild() throws Exception {
        // ====================================================================
        // Third region with higher priority than parent and child
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        ProtectedRegion region = mock.add(10);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setParent(parent1);

        region = mock.add(20);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test2")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesParentsAndInheritance() throws Exception {
        // ====================================================================
        // Multiple regions with parents, one region using flag from parent
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");

        ProtectedRegion region = mock.add(20);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");
        region.setParent(parent1);

        ProtectedRegion parent2 = mock.add(6);
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        region = mock.add(20);
        region.setParent(parent2);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test2", "test3")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesParentsAndInheritanceHighPriorityAndNoFlag() throws Exception {
        // ====================================================================
        // Multiple regions with parents, one region with high priority but no flag
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(5);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");

        ProtectedRegion region = mock.add(20);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");
        region.setParent(parent1);

        ProtectedRegion parent2 = mock.add(6);
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        region = mock.add(30);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test2")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    @Test
    public void testGetValuesParentWithSamePriorityAsHighest() throws Exception {
        // ====================================================================
        // As before, except a parent region has the same priority as the previous highest
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(30);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");

        ProtectedRegion region = mock.add(20);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");
        region.setParent(parent1);

        ProtectedRegion parent2 = mock.add(6);
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");

        region = mock.add(30);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(copyOf(result.getValues(null, DefaultFlag.FAREWELL_MESSAGE)), equalTo(of("test1")));
        assertThat(result.getValues(null, DefaultFlag.GREET_MESSAGE), is(Matchers.<String>empty()));
    }

    // ========================================================================
    // ========================================================================

    @Test
    public void testGetEffectiveFlagSingleRegion() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        // ====================================================================
        // Single region
        // ====================================================================

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagWithALLGroupAndNonMember() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        // ====================================================================
        // Single region with group ALL and non-member player
        // ====================================================================

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagWithALLGroupAndNull() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        // ====================================================================
        // Single region with group ALL and null player
        // ====================================================================

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONMEMBERSGroupNonMember() throws Exception {
        // ====================================================================
        // Single region with group NON-MEMBERS and non-member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONMEMBERSGroupNull() throws Exception {
        // ====================================================================
        // Single region with group NON-MEMBERS and null player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSGroupNonMember() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and non-member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupNonMember() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and non-member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupNull() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and null player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo(null));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupMember() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagMEMBERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group MEMBERS and owner player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagOWNERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group OWNERS and owner player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagOWNERSGroupMember() throws Exception {
        // ====================================================================
        // Single region with group OWNERS and member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and owner player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONMEMBERSGroupOwner() throws Exception {
        // ====================================================================
        // Single region with group NON-MEMBERS and owner player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);

        LocalPlayer player = mock.createPlayer();
        region.getOwners().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo(null));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSGroupMember() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();
        region.getMembers().addPlayer(player);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagNONOWNERSNonMember() throws Exception {
        // ====================================================================
        // Single region with group NON-OWNERS and non-member player
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        LocalPlayer player = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagThreeInheritance() throws Exception {
        // ====================================================================
        // Three-level inheritance
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("test1"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagThreeInheritanceMiddleOverride() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on middle level
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test2");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("test2"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagThreeInheritanceLastOverride() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on last level
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test1");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "test3");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("test3"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroups() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on last level
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "everyone");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE, "members");
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent2.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnChild() throws Exception {
        // ====================================================================
        // Three-level inheritance, overridden on last level
        // ====================================================================

        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "everyone");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE, "members");
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        region.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnParent() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "everyone");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE, "members");
        parent2.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent1.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnParentFlagOnBottom() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.add(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "everyone");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "members");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent1.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }

    @Test
    public void testGetEffectiveFlagInheritanceAndDifferingGroupsMemberOnParentFlagOnBottomGroupOutside() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent1 = mock.createOutside(0);
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE, "everyone");
        parent1.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.ALL);

        ProtectedRegion parent2 = mock.add(0);
        parent2.setParent(parent1);

        ProtectedRegion region = mock.add(0);
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "members");
        region.setFlag(DefaultFlag.FAREWELL_MESSAGE.getRegionGroupFlag(), RegionGroup.MEMBERS);
        region.setParent(parent2);

        LocalPlayer player1 = mock.createPlayer();
        parent1.getMembers().addPlayer(player1);

        LocalPlayer player2 = mock.createPlayer();

        FlagValueCalculator result = mock.getFlagCalculator();
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player1), equalTo("members"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, player2), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.FAREWELL_MESSAGE, null), equalTo("everyone"));
        assertThat(result.getEffectiveFlag(region, DefaultFlag.GREET_MESSAGE, null), equalTo(null));
    }
}