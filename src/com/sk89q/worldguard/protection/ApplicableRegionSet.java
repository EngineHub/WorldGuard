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

import java.util.Map;
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
    private Map<String,ProtectedRegion> regions;
    
    public ApplicableRegionSet(Vector pt, Map<String,ProtectedRegion> regions) {
        this.pt = pt;
        this.regions = regions;
    }
    
    public boolean canBuild(LocalPlayer player) {
        boolean allowed = false;
        boolean found = false;
        
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowBuild == State.DENY) return false;
            
            found = true;

            if (!allowed && region.getFlags().allowBuild == State.ALLOW) {
                allowed = true;
            }
            
            if (!allowed && region.getOwners().contains(player)) {
                allowed = true;
            }
        }
        
        return found ? allowed : true;
    }
    
    public boolean canPvP(LocalPlayer player) {
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowBuild == State.DENY) return false;
        }
        
        return true;
    }
    
    public boolean allowsMobDamage() {
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowMobDamage == State.DENY) return false;
        }
        
        return true;
    }
    
    public boolean allowsCreeperExplosions() {
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowCreeperExplosions == State.DENY) return false;
        }
        
        return true;
    }
    
    public boolean allowsTNT() {
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowTNT == State.DENY) return false;
        }
        
        return true;
    }
    
    public boolean allowsLighter() {
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowLighter == State.DENY) return false;
        }
        
        return true;
    }
    
    public boolean allowsFireSpread() {
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowFireSpread == State.DENY) return false;
        }
        
        return true;
    }
    
    public boolean allowsLavaFire() {
        for (ProtectedRegion region : regions.values()) {
            if (!region.contains(pt)) continue;
            if (region.getFlags().allowLavaFire == State.DENY) return false;
        }
        
        return true;
    }
}
