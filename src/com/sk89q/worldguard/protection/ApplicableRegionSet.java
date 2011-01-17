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

import java.util.SortedMap;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.AreaFlags.State;

/**
 * Represents a set of regions and their rules as applied to one point.
 * 
 * @author sk89q
 */
public class ApplicableRegionSet {
    private Vector pt;
    private SortedMap<String,ProtectedRegion> regions;
    
    public ApplicableRegionSet(Vector pt, SortedMap<String,ProtectedRegion> regions) {
        this.pt = pt;
        this.regions = regions;
    }
    
    public boolean canBuild(LocalPlayer player) {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (region.getFlags().allowBuild == State.ALLOW) continue;
            if (!region.contains(pt)) continue;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            if (!region.getOwners().contains(player)) {
                return false;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
    
    public boolean allowsPvP() {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowPvP == State.DENY) return false;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
    
    public boolean allowsMobDamage() {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowMobDamage == State.DENY) return false;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
    
    public boolean allowsCreeperExplosions() {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowCreeperExplosions == State.DENY) return false;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
    
    public boolean allowsTNT() {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowTNT == State.DENY) return false;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
    
    public boolean allowsLighter() {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowLighter == State.DENY) return false;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
    
    public boolean allowsFireSpread() {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowFireSpread == State.DENY) return false;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
    
    public boolean allowsLavaFire() {
        boolean found = false;
        int lastPriority = 0;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowLavaFire == State.DENY) return false;
            
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }
            
            found = true;
            lastPriority = region.getPriority();
        }
        
        return true;
    }
}
