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

import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.Iterator;
import com.sk89q.worldguard.LocalPlayer;
import java.util.List;

/**
 * Represents a setFlag of regions and their rules as applied to one point or
 * region.
 * 
 * @author sk89q
 */
public class ApplicableRegionSet {

    private Iterator<ProtectedRegion> applicable;
    private ProtectedRegion globalRegion;
    private ProtectedRegion affectedRegion;

    /**
     * Construct the object.
     * 
     * @param applicable
     * @param globalRegion 
     */
    public ApplicableRegionSet(Iterator<ProtectedRegion> applicable,
            ProtectedRegion globalRegion) {
        this.applicable = applicable;
        this.globalRegion = globalRegion;
    }
    
    /**
     * Checks if a player can build in an area.
     * 
     * @param player
     * @return
     */
    public boolean canBuild(LocalPlayer player) {
        return isFlagAllowed(DefaultFlag.BUILD, player);
    }

    /**
     * Checks a flag.
     * 
     * @param player
     * @return
     */
    public boolean allowsFlag(String flag) {
        boolean def = true;

        if (flag.equals(AreaFlags.FLAG_CHEST_ACCESS)) {
            def = global.canAccessChests;
        } else if (flag.equals(AreaFlags.FLAG_PVP)) {
            def = global.canPvP;
        }

        return isFlagAllowed(flag, def, null);
    }

    /**
     * Checks to see if a flag is permitted.
     * 
     * @param def
     *            default state if there are no regions defined
     * @param player
     *            null to not check owners and members
     * @return
     */
    private boolean getState(StateFlag flag, LocalPlayer player) {
        boolean found = false;
        boolean allowed = false; // Used for ALLOW override
        if (player == null) {
            allowed = def;
        }
        int lastPriority = 0;

        // The algorithm is as follows:
        // While iterating through the list of regions, if an entry disallows
        // the flag, then put it into the needsClear set. If an entry allows
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

        while (applicable.hasNext()) {
            ProtectedRegion region = applicable.next();

            // Ignore non-build regions
            if (player != null
                    && region.getFlags().get(AreaFlags.FLAG_PASSTHROUGH) == State.ALLOW) {
                continue;
            }

            // Allow DENY to override everything
            if (region.getFlags().get(flag) == State.DENY) {
                return false;
            }

            // Forget about regions that allow it, although make sure the
            // default state is now to allow
            if (region.getFlags().get(flag) == State.ALLOW) {
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

    /**
     * Clear a region's parents for isFlagAllowed().
     * 
     * @param needsClear
     * @param hasCleared
     * @param region
     */
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

    /**
     * Returns whether this set has any regions affected (discounting
     * a global region).
     * 
     * @return
     */
    public boolean isAnyRegionAffected() {
        return applicable.size() > 0;
    }

    /**
     * Get the affected region.
     * 
     * @return 
     */
    public ProtectedRegion _getAffectedRegion() {
        if (affectedRegion != null) {
            return affectedRegion;
        }

        affectedRegion = null;
        Iterator<ProtectedRegion> iter = applicable.iterator();

        while (iter.hasNext()) {
            ProtectedRegion region = iter.next();

            if (affectedRegion == null
                    || affectedRegion.getPriority() < region.getPriority()) {
                affectedRegion = region;
            }
        }
        
        return affectedRegion;
    }

    /**
     * Checks whether a player is an owner of any region in this set.
     * 
     * @param player
     * @return
     */
    public boolean _isOwner(LocalPlayer player) {
        
        return affectedRegion != null ? affectedRegion.isOwner(player) : false;
    }

    /**
     * Checks whether a player is a member of the region or any of its parents.
     * 
     * @param player
     * @return
     */
    public boolean _isMember(LocalPlayer player) {
        return affectedRegion != null ? affectedRegion.isMember(player) : false;
    }

}
