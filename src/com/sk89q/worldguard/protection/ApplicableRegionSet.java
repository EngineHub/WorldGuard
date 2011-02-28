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
 * Represents a setFlag of regions and their rules as applied to one point.
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
    public ApplicableRegionSet(List<ProtectedRegion> applicable,  GlobalFlags global) {
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
        return isStateFlagAllowed(FlagType.BUILD, global.canBuild, player);
    }

    /**
     * Checks a flag.
     * 
     * @param player
     * @return
     */
    public boolean isStateFlagAllowed(FlagType type) {
        return isStateFlagAllowed(type, global.getDefaultValue(type));
    }

    public boolean isStateFlagAllowed(FlagType type, boolean def) {

        if(!this.isAnyRegionAffected())
        {
            return def;
        }

        return getStateFlag(type, true).getValue(State.DENY) == State.ALLOW;
    }

    public boolean isStateFlagAllowed(FlagType type, LocalPlayer player) {

        return isStateFlagAllowed(type, global.getDefaultValue(type), player);
    }

    public boolean isStateFlagAllowed(FlagType type, boolean def, LocalPlayer player) {

        if(!this.isAnyRegionAffected())
        {
            return def;
        }

        return getStateFlag(type, true).getValue(State.DENY) == State.ALLOW  || this.isMember(player);
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

    public boolean isAnyRegionAffected()
    {
        return this.applicable.size() > 0;
    }

  
    /**
     * Determines the region with the hightest priority that is not a parent.
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
        return affectedRegion != null ?  affectedRegion.getId() : "";
    }

    public int getAffectedRegionPriority() {
        return affectedRegion != null ?  affectedRegion.getPriority() : 0;
    }



    /**
     * Checks to see if a flag is permitted.
     *
     * @param def default state if there are no regions defined
     * @param player null to not check owners and members
     * @return
     */
    /*
    private boolean isStateFlagAllowed(String flag, boolean def, LocalPlayer player) {
    boolean found = false;
    boolean allowed = false; // Used for ALLOW override
    if (player == null) {
    allowed = def;
    }
    int lastPriority = 0;

    // The algorithm is as follows:
    // While iterating through the list of regions, if an entry disallows
    // the flag, then put it into the needsClear setFlag. If an entry allows
    // the flag and it has a parent, then its parent is put into hasCleared.
    // In the situation that the child is reached before the parent, upon
    // the parent being reached, even if the parent disallows, because the
    // parent will be in hasCleared, permission will be allowed. In the
    // other case, where the parent is reached first, if it does not allow
    // permissions, it will be placed into needsClear. If a child of
    // the parent is reached later, the parent will be removed from
    // needsClear. At the end, if needsClear is not empty, that means that
    // permission should not be given. If a parent has multiple children
    // and one child does not allow permissions, then it will be placed into
    // needsClear just like as if was a parent.

    Set<ProtectedRegion> needsClear = new HashSet<ProtectedRegion>();
    Set<ProtectedRegion> hasCleared = new HashSet<ProtectedRegion>();

    Iterator<Entry<String, ProtectedRegion>> iter = applicable.entrySet().iterator();

    while (iter.hasNext()) {
    ProtectedRegion region = iter.next().getValue();

    // Ignore non-build regions
    if (player != null && region.getFlags().getStateFlag(AreaFlags.FLAG_PASSTHROUGH) == State.ALLOW) {
    continue;
    }


    // Allow DENY to override everything
    if (region.getFlags().getStateFlag(flag) == State.DENY) {
    return false;
    }

    // Forget about regions that allow it, although make sure the
    // default state is now to allow
    if (region.getFlags().getStateFlag(flag) == State.ALLOW) {
    allowed = true;
    found = true;
    continue;
    }

    // Ignore lower priority regions
    if (found && region.getPriority() < lastPriority) {
    break;
    }

    if (player != null) {
    if (hasCleared.contains(region)) {
    // Already cleared, so do nothing
    } else {
    if (!region.isMember(player)) {
    needsClear.add(region);
    } else {
    // Need to clear all parents
    clearParents(needsClear, hasCleared, region);
    }
    }
    }

    found = true;
    lastPriority = region.getPriority();
    }

    return (found == false ? def : allowed)
    || (player != null && needsClear.size() == 0);
    }
     */
   

    /**
     * Clear a region's parents for isStateFlagAllowed().
     * 
     * @param needsClear
     * @param hasCleared
     * @param region
     */
    /*
    private void clearParents(Set<ProtectedRegion> needsClear,
    Set<ProtectedRegion> hasCleared, ProtectedRegion region) {
    ProtectedRegion parent = region.getParent();

    while (parent != null) {
    if (!needsClear.remove(parent)) {
    hasCleared.add(parent);
    }

    parent = parent.getParent();
    }
    }
     */
}
