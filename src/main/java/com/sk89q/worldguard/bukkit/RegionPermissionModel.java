// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Used for querying region-related permissions.
 */
public class RegionPermissionModel extends AbstractPermissionModel {
    
    public RegionPermissionModel(WorldGuardPlugin plugin, CommandSender sender) {
        super(plugin, sender);
    }
    
    public boolean mayForceLoadRegions() {
        return hasPluginPermission("region.load");
    }
    
    public boolean mayForceSaveRegions() {
        return hasPluginPermission("region.save");
    }
    
    public boolean mayMigrateRegionStore() {
        return hasPluginPermission("region.migratedb");
    }
    
    public boolean mayDefine() {
        return hasPluginPermission("region.define");
    }
    
    public boolean mayRedefine(ProtectedRegion region) {
        return hasPatternPermission("redefine", region);
    }
    
    public boolean mayClaim() {
        return hasPluginPermission("region.claim");
    }
    
    public boolean mayClaimRegionsUnbounded() {
        return hasPluginPermission("region.unlimited");
    }
    
    public boolean mayDelete(ProtectedRegion region) {
        return hasPatternPermission("remove", region);
    }
    
    public boolean maySetPriority(ProtectedRegion region) {
        return hasPatternPermission("setpriority", region);
    }
    
    public boolean maySetParent(ProtectedRegion child, ProtectedRegion parent) {
        return hasPatternPermission("setparent", child) &&
                (parent == null ||
                hasPatternPermission("setparent", parent));
    }
    
    public boolean maySelect(ProtectedRegion region) {
        return hasPatternPermission("select", region);
    }
    
    public boolean mayLookup(ProtectedRegion region) {
        return hasPatternPermission("info", region);
    }
    
    public boolean mayTeleportTo(ProtectedRegion region) {
        return hasPatternPermission("teleport", region);
    }
    
    public boolean mayList() {
        return hasPluginPermission("region.list");
    }
    
    public boolean mayList(String targetPlayer) {
        if (targetPlayer == null) {
            return mayList();
        }
        
        if (targetPlayer.equalsIgnoreCase(getSender().getName())) {
            return hasPluginPermission("region.list.own");
        } else {
            return mayList();
        }
    }
    
    public boolean maySetFlag(ProtectedRegion region) {
        return hasPatternPermission("flag.regions", region);
    }
    
    public boolean maySetFlag(ProtectedRegion region, Flag<?> flag) {
        // This is a WTF permission
        return hasPatternPermission(
                "flag.flags." + flag.getName().toLowerCase(), region);
    }
    
    /**
     * Checks to see if the given sender has permission to modify the given region
     * using the region permission pattern.
     * 
     * @param perm the name of the node
     * @param region the region
     */
    private boolean hasPatternPermission(String perm, ProtectedRegion region) {
        if (!(getSender() instanceof Player)) {
            return true; // Non-players (i.e. console, command blocks, etc.) have full power
        }
        
        LocalPlayer localPlayer = getPlugin().wrapPlayer((Player) getSender());
        String idLower = region.getId().toLowerCase();
        String effectivePerm;
        
        if (region.isOwner(localPlayer)) {
            return hasPluginPermission("region." + perm + ".own." + idLower) ||
                    hasPluginPermission("region." + perm + ".member." + idLower);
        } else if (region.isMember(localPlayer)) {
            return hasPluginPermission("region." + perm + ".member." + idLower);
        } else {
            effectivePerm = "region." + perm + "." + idLower;
        }

        return hasPluginPermission(effectivePerm);
    }

}
