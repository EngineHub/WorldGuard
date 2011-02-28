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

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Iterator;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.flags.*;
import com.sk89q.worldguard.protection.regions.flags.FlagDatabase.FlagType;
import com.sk89q.worldguard.protection.regions.flags.RegionFlag;
import com.sk89q.worldguard.protection.regions.flags.RegionFlag.State;
import java.util.List;

/**
 * Represents a setFlag of regions and their rules as applied to one point or region.
 * 
 * @author sk89q
 */
public class ApplicableRegionSet {

    private GlobalFlags global;
    private List<ProtectedRegion> applicable;
    private ProtectedRegion affectedRegion;

    /**
     * Construct the object.
     * 
     * @param pt
     * @param regions
     * @param global
     */
    public ApplicableRegionSet(List<ProtectedRegion> applicable, GlobalFlags global) {
        this.applicable = applicable;
        this.global = global;

        determineAffectedRegion();
    }

    /**
     * Checks if a player can build in an area.
     * 
     * @param player
     * @return
     */
    public boolean canBuild(LocalPlayer player) {

        if (!this.isAnyRegionAffected()) {
            return global.canBuild;
        }

        return getStateFlag(FlagType.BUILD, true).getValue(State.DENY) == State.ALLOW || isMember(player);
    }


    public boolean isStateFlagAllowed(FlagType type) {

        return isStateFlagAllowed(type, global.getDefaultValue(type));
    }

    public boolean isStateFlagAllowed(FlagType type, boolean def) {

        if (!this.isAnyRegionAffected()) {
            return def;
        }
        State defState = def ? State.ALLOW : State.DENY;
        return getStateFlag(type, true).getValue(defState) == State.ALLOW;
    }

    public boolean isStateFlagAllowed(FlagType type, LocalPlayer player) {

        if (type == FlagType.BUILD) {
            return canBuild(player);
        }
        return isStateFlagAllowed(type, global.getDefaultValue(type), player);
    }

    public boolean isStateFlagAllowed(FlagType type, boolean def, LocalPlayer player) {

        if (type == FlagType.BUILD) {
            return canBuild(player);
        }

        if (!this.isAnyRegionAffected()) {
            return def;
        }

        State defState = def ? State.ALLOW : State.DENY;
        return getStateFlag(type, true).getValue(defState) == State.ALLOW || this.isMember(player);
    }

    private RegionFlag getFlag(FlagType type, Boolean inherit) {

        ProtectedRegion region = affectedRegion;

        if (region == null) {
            return null;
        }

        if (!inherit) {
            return region.getFlags().getFlag(type);
        } else {
            RegionFlag value;
            do {
                value = region.getFlags().getFlag(type);
                region = region.getParent();

            } while (!value.hasValue() && region != null);

            return value;
        }

    }

    public BooleanRegionFlag getBooleanFlag(FlagType type, boolean inherit) {

        RegionFlag flag = this.getFlag(type, inherit);

        if (flag instanceof BooleanRegionFlag) {
            return (BooleanRegionFlag) flag;
        } else {
            return new BooleanRegionFlag();
        }
    }

    public StateRegionFlag getStateFlag(FlagType type, boolean inherit) {

        RegionFlag flag = this.getFlag(type, inherit);

        if (flag instanceof StateRegionFlag) {
            return (StateRegionFlag) flag;
        } else {
            return new StateRegionFlag();
        }
    }

    public IntegerRegionFlag getIntegerFlag(FlagType type, boolean inherit) {

        RegionFlag flag = this.getFlag(type, inherit);

        if (flag instanceof IntegerRegionFlag) {
            return (IntegerRegionFlag) flag;
        } else {
            return new IntegerRegionFlag();
        }
    }

    public DoubleRegionFlag getDoubleFlag(FlagType type, boolean inherit) {

        RegionFlag flag = this.getFlag(type, inherit);

        if (flag instanceof DoubleRegionFlag) {
            return (DoubleRegionFlag) flag;
        } else {
            return new DoubleRegionFlag();
        }
    }

    public StringRegionFlag getStringFlag(FlagType type, boolean inherit) {

        RegionFlag flag = this.getFlag(type, inherit);

        if (flag instanceof StringRegionFlag) {
            return (StringRegionFlag) flag;
        } else {
            return new StringRegionFlag();
        }
    }

    public RegionGroupRegionFlag getRegionGroupFlag(FlagType type, boolean inherit) {

        RegionFlag flag = this.getFlag(type, inherit);

        if (flag instanceof RegionGroupRegionFlag) {
            return (RegionGroupRegionFlag) flag;
        } else {
            return new RegionGroupRegionFlag();
        }
    }

    public LocationRegionFlag getLocationFlag(FlagType type, boolean inherit) {

        RegionFlag flag = this.getFlag(type, inherit);

        if (flag instanceof LocationRegionFlag) {
            return (LocationRegionFlag) flag;
        } else {
            return new LocationRegionFlag();
        }
    }

    public boolean isAnyRegionAffected() {
        return this.applicable.size() > 0;
    }

    /**
     * Determines the region with the hightest priority.
     *
     */
    private void determineAffectedRegion() {

        affectedRegion = null;
        Iterator<ProtectedRegion> iter = applicable.iterator();

        while (iter.hasNext()) {
            ProtectedRegion region = iter.next();

            if (affectedRegion == null || affectedRegion.getPriority() < region.getPriority()) {
                affectedRegion = region;
            }
        }
    }

    public boolean isOwner(LocalPlayer player) {
        return affectedRegion != null ? affectedRegion.isOwner(player) : false;
    }

    /**
     * Checks whether a player is a member of the region or any of its parents.
     *
     * @param player
     * @return
     */
    public boolean isMember(LocalPlayer player) {
        return affectedRegion != null ? affectedRegion.isMember(player) : false;
    }

    public String getAffectedRegionId() {
        return affectedRegion != null ? affectedRegion.getId() : "";
    }

    public int getAffectedRegionPriority() {
        return affectedRegion != null ? affectedRegion.getPriority() : 0;
    }
 
}
