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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.AreaFlags.State;

/**
 * Represents a set of regions and their rules as applied to one point.
 * 
 * @author sk89q
 */
public class ApplicableRegionSet {
    private GlobalFlags global;
    private Vector pt;
    private Iterator<ProtectedRegion> applicable;
    
    /**
     * Construct the object.
     * 
     * @param pt
     * @param regions
     * @param global
     */
    public ApplicableRegionSet(Vector pt, Iterator<ProtectedRegion> applicable,
            GlobalFlags global) {
        this.pt = pt;
        this.applicable = applicable;
        this.global = global;
    }
    
    /**
     * Checks if a player can build in an area.
     * 
     * @param player
     * @return
     */
    public boolean canBuild(LocalPlayer player) {
        return isFlagAllowed(AreaFlags.FLAG_BUILD, global.canBuild, player);
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
        } else if (flag.equals(AreaFlags.FLAG_LIGHTER)) {
        	def = global.canLighter;
    	} else if (flag.equals(AreaFlags.FLAG_TNT)) {
    		def = global.canTnt;
    	} else if (flag.equals(AreaFlags.FLAG_CREEPER_EXPLOSION)) {
    		def = global.allowCreeper;
		} else if (flag.equals(AreaFlags.FLAG_MOB_DAMAGE)) {
			def = global.allowMobDamage;
		}
        
        return isFlagAllowed(flag, def, null);
    }
    
    /**
     * Checks to see if a flag is permitted.
     * 
     * @param def default state if there are no regions defined
     * @param player null to not check owners and members
     * @return
     */
    private boolean isFlagAllowed(String flag, boolean def, LocalPlayer player) {
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
            if (player != null && region.getFlags().get(AreaFlags.FLAG_PASSTHROUGH) == State.ALLOW) {
                continue;
            }
            
            // Forget about regions that are not covered
            if (!region.contains(pt)) {
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
}
