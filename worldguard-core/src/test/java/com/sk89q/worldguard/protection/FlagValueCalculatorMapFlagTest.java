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

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.MapFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SuppressWarnings({"UnusedDeclaration"})
public class FlagValueCalculatorMapFlagTest {
    @Test
    public void testGetEffectiveMapFlagWithFallback() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion global = mock.global();
        global.setFlag(Flags.BUILD, StateFlag.State.DENY);
        MapFlag<String, StateFlag.State> mapFlag =
                new MapFlag<>("test", new StringFlag(null), new StateFlag(null, true));
        Map<String, StateFlag.State> map = new HashMap<>();
        map.put("allow", StateFlag.State.ALLOW);
        map.put("deny", StateFlag.State.DENY);
        global.setFlag(mapFlag, map);

        ApplicableRegionSet applicableSet = mock.getApplicableSet();
        assertThat(applicableSet.queryMapValue(null, mapFlag, "allow", Flags.BUILD),
                equalTo(StateFlag.State.ALLOW));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "deny", Flags.BUILD),
                equalTo(StateFlag.State.DENY));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "undefined", Flags.BUILD),
                equalTo(StateFlag.State.DENY));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "allow", null),
                equalTo(StateFlag.State.ALLOW));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "deny", null),
                equalTo(StateFlag.State.DENY));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "undefined", null),
                equalTo(StateFlag.State.ALLOW));
    }

    @Test
    public void testGetEffectiveMapFlagWithSamePriority() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion region1 = mock.add(0);
        ProtectedRegion region2 = mock.add(0);

        MapFlag<String, StateFlag.State> mapFlag =
                new MapFlag<>("test", new StringFlag(null), new StateFlag(null, true));
        Map<String, StateFlag.State> map1 = new HashMap<>();
        Map<String, StateFlag.State> map2 = new HashMap<>();

        map1.put("should-deny", StateFlag.State.ALLOW);
        map2.put("should-deny", StateFlag.State.DENY);

        map1.put("should-allow", StateFlag.State.ALLOW);

        map1.put("should-allow2", StateFlag.State.ALLOW);
        map2.put("should-allow2", StateFlag.State.ALLOW);

        region1.setFlag(mapFlag, map1);
        region2.setFlag(mapFlag, map2);

        ApplicableRegionSet applicableSet = mock.getApplicableSet();
        assertThat(applicableSet.queryMapValue(null, mapFlag, "should-deny", null),
                equalTo(StateFlag.State.DENY));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "should-allow", null),
                equalTo(StateFlag.State.ALLOW));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "should-allow2", null),
                equalTo(StateFlag.State.ALLOW));
    }


    @Test
    public void testGetEffectiveMapFlagWithInheritance() throws Exception {
        MockApplicableRegionSet mock = new MockApplicableRegionSet();

        ProtectedRegion parent = mock.add(0);
        ProtectedRegion child = mock.add(0, parent);

        MapFlag<String, StateFlag.State> mapFlag =
                new MapFlag<>("test", new StringFlag(null), new StateFlag(null, true));
        Map<String, StateFlag.State> parentMap = new HashMap<>();
        Map<String, StateFlag.State> childMap = new HashMap<>();

        parentMap.put("useChildValue", StateFlag.State.ALLOW);
        childMap.put("useChildValue", StateFlag.State.DENY);

        parentMap.put("useParentValue", StateFlag.State.ALLOW);

        parent.setFlag(mapFlag, parentMap);
        child.setFlag(mapFlag, childMap);

        ApplicableRegionSet applicableSet = mock.getApplicableSet();
        assertThat(applicableSet.queryMapValue(null, mapFlag, "useChildValue", null),
                equalTo(StateFlag.State.DENY));
        assertThat(applicableSet.queryMapValue(null, mapFlag, "useParentValue", null),
                equalTo(StateFlag.State.ALLOW));
    }
}
