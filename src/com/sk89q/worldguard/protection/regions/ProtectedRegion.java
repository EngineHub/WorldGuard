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

package com.sk89q.worldguard.protection.regions;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;

/**
 * Represents a region of any shape and size that can be protected.
 * 
 * @author sk89q
 */
public abstract class ProtectedRegion implements Comparable<ProtectedRegion> {
    /**
     * Holds the region's ID.
     */
    private String id;
    /**
     * Priority.
     */
    private int priority = 0;
    /**
     * Holds the parent.
     */
    private transient ProtectedRegion parent;
    /**
     * Holds the parent's Id. Used for serialization, don't touch it.
     */
    private String parentId;
    /**
     * List of owners.
     */
    private DefaultDomain owners = new DefaultDomain();
    /**
     * List of members.
     */
    private DefaultDomain members = new DefaultDomain();
    /**
     * Area flags.
     */
    private AreaFlags flags = new AreaFlags();


    /**
     * Construct a new instance of this region.
     *
     * @param id
     */
    public ProtectedRegion(String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }


    /*
     *  Important for serialization
     */
    public String getParentId() {

        if (this.parent != null) {
            this.parentId = parent.getId();
        }

        return this.parentId;
    }

    /**
     * @setFlag the parentId. Used for serialization, don't touch it.
     */
    public void setParentId() {
        
        if (this.parent != null) {
            this.parentId = parent.getId();
        } else {
            this.parentId = null;
        }
    }

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    public abstract BlockVector getMinimumPoint();

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    public abstract BlockVector getMaximumPoint();

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to setFlag
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * @return the parent
     */
    public ProtectedRegion getParent() {
        return parent;
    }

    /**
     * Set the parent. This checks to make sure that it will not result
     * in circular inheritance.
     * 
     * @param parent the parent to setFlag
     * @throws CircularInheritanceException 
     */
    public void setParent(ProtectedRegion parent) throws CircularInheritanceException {
        if (parent == null) {
            this.parent = null;
            return;
        }
        
        if (parent == this) {
            throw new CircularInheritanceException();
        }
        
        ProtectedRegion p = parent.getParent();
        while (p != null) {
            if (p == this) {
                throw new CircularInheritanceException();
            }
            p = p.getParent();
        }
        
        this.parent = parent;
    }

    /**
     * Se flags.
     * @param flags
     */
    public void setFlags(AreaFlags flags) {
        this.flags = flags;
    }
    
    /**
     * @return the owners
     */
    public DefaultDomain getOwners() {

        return owners;
    }

    /**
     * @param owners the owners to setFlag
     */
    public void setOwners(DefaultDomain owners) {
        this.owners = owners;
    }

    /**
     * @return the members
     */
    public DefaultDomain getMembers() {

        return members;
    }

    /**
     * @param owners the owners to setFlag
     */
    public void setMembers(DefaultDomain members) {
        this.members = members;
    }
    
    /**
     * Checks whether a player is an owner of region or any of its parents.
     * 
     * @param player
     * @return
     */
    public boolean isOwner(LocalPlayer player) {
        if (owners.contains(player)) {
            return true;
        }
        
        ProtectedRegion parent = getParent();
        while (parent != null) {
            if (parent.getOwners().contains(player)) {
                return true;
            }
            
            parent = parent.getParent();
        }
        
        return false;
    }


    /**
     * Checks whether a player is a member of the region or any of its parents.
     * 
     * @param player
     * @return
     */
    public boolean isMember(LocalPlayer player) {
        if (owners.contains(player) || members.contains(player)) {
            return true;
        }
        
        ProtectedRegion parent = getParent();
        while (parent != null) {
            if (parent.getOwners().contains(player)
                    || parent.getMembers().contains(player)) {
                return true;
            }
            
            parent = parent.getParent();
        }
        
        return false;
    }

    /**
     * Get flags.
     * 
     * @return
     */
    public AreaFlags getFlags() {
        return flags;
    }

    /**
     * Get the number of Blocks in this region
     * 
     * @return
     */
    public abstract int countBlocks();
    
    /**
     * Check to see if a point is inside this region.
     * 
     * @param pt
     * @return
     */
    public abstract boolean contains(Vector pt);
    
    /**
     * Compares to another region.
     * 
     * @param other
     * @return
     */
    public int compareTo(ProtectedRegion other) {
        if (priority == other.priority) {
            return 0;
        } else if (priority > other.priority) {
            return -1;
        } else {
            return 1;
        }
    }
    
    /**
     * Return the type of region as a user-friendly, lowercase name.
     * 
     * @return type of region
     */
    public abstract String getTypeName();
    
    /**
     * Checks if two region intersects.
     * 
     * @param region
     * @throws UnsupportedIntersectionException
     * @return
     */
    public abstract boolean intersectsWith(ProtectedRegion region) throws UnsupportedIntersectionException;
    
    
    /**
     * Thrown when setting a parent would create a circular inheritance
     * situation.
     * 
     */
    public static class CircularInheritanceException extends Exception {
        private static final long serialVersionUID = 7479613488496776022L;
    }
}
