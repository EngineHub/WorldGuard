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

package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;

/**
 * Stores a region group.
 */
public class RegionGroupFlag extends EnumFlag<RegionGroup> {

    private RegionGroup def;

    public RegionGroupFlag(String name, RegionGroup def) {
        super(name, RegionGroup.class, null);
        this.def = def;
    }

    @Override
    public RegionGroup getDefault() {
        return def;
    }

    @Override
    public RegionGroup detectValue(String input) {
        input = input.trim();

        if (input.equalsIgnoreCase("members") || input.equalsIgnoreCase("member")) {
            return RegionGroup.MEMBERS;
        } else if (input.equalsIgnoreCase("owners") || input.equalsIgnoreCase("owner")) {
            return RegionGroup.OWNERS;
        } else if (input.equalsIgnoreCase("nonowners") || input.equalsIgnoreCase("nonowner")) {
            return RegionGroup.NON_OWNERS;
        } else if (input.equalsIgnoreCase("nonmembers") || input.equalsIgnoreCase("nonmember")) {
            return RegionGroup.NON_MEMBERS;
        } else if (input.equalsIgnoreCase("everyone") || input.equalsIgnoreCase("anyone") || input.equalsIgnoreCase("all")) {
            return RegionGroup.ALL;
        } else if (input.equalsIgnoreCase("none") || input.equalsIgnoreCase("noone") || input.equalsIgnoreCase("deny")) {
            return RegionGroup.NONE;
        } else {
            return null;
        }
    }

    public static boolean isMember(ProtectedRegion region, RegionGroup group, @Nullable LocalPlayer player) {
        if (group == null || group == RegionGroup.ALL) {
            return true;
        } else if (group == RegionGroup.OWNERS) {
            if (player != null && region.isOwner(player)) {
                return true;
            }
        } else if (group == RegionGroup.MEMBERS) {
            if (player != null && region.isMember(player)) {
                return true;
            }
        } else if (group == RegionGroup.NON_OWNERS) {
            if (player == null || !region.isOwner(player)) {
                return true;
            }
        } else if (group == RegionGroup.NON_MEMBERS) {
            if (player == null || !region.isMember(player)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMember(ApplicableRegionSet set, @Nullable RegionGroup group, LocalPlayer player) {
        if (group == null || group == RegionGroup.ALL) {
            return true;
        } else if (group == RegionGroup.OWNERS) {
            if (set.isOwnerOfAll(player)) {
                return true;
            }
        } else if (group == RegionGroup.MEMBERS) {
            if (set.isMemberOfAll(player)) {
                return true;
            }
        } else if (group == RegionGroup.NON_OWNERS) {
            if (!set.isOwnerOfAll(player)) {
                return true;
            }
        } else if (group == RegionGroup.NON_MEMBERS) {
            if (!set.isMemberOfAll(player)) {
                return true;
            }
        }

        return false;
    }

}
