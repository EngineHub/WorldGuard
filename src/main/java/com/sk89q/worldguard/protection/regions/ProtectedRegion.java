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

import java.lang.management.MonitorInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.flags.Flag;

/**
 * Represents a region of any shape and size that can be protected.
 *
 * @author sk89q
 */
public abstract class ProtectedRegion implements Comparable<ProtectedRegion> {

    protected BlockVector min;
    protected BlockVector max;

    private static final Pattern idPattern = Pattern.compile("^[A-Za-z0-9_,'\\-\\+/]{1,}$");

    /**
     * Holds the region's ID.
     */
    private String id;

    /**
     * Priority.
     */
    private int priority = 0;

    /**
     * Holds the curParent.
     */
    private ProtectedRegion parent;

    /**
     * List of owners.
     */
    private DefaultDomain owners = new DefaultDomain();

    /**
     * List of members.
     */
    private DefaultDomain members = new DefaultDomain();

    /**
     * List of flags.
     */
    private Map<Flag<?>, Object> flags = new HashMap<Flag<?>, Object>();

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

    /**
     * Get the lower point of the cuboid.
     *
     * @return min point
     */
    public BlockVector getMinimumPoint() {
        return min;
    }

    /**
     * Get the upper point of the cuboid.
     *
     * @return max point
     */
    public BlockVector getMaximumPoint() {
        return max;
    }

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
     * @return the curParent
     */
    public ProtectedRegion getParent() {
        return parent;
    }

    /**
     * Set the curParent. This checks to make sure that it will not result
     * in circular inheritance.
     *
     * @param parent the curParent to setFlag
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
     * @param members the members to setFlag
     */
    public void setMembers(DefaultDomain members) {
        this.members = members;
    }

    /**
     * Checks whether a region has members or owners.
     *
     * @return whether there are members or owners
     */
    public boolean hasMembersOrOwners() {
        return owners.size() > 0 || members.size() > 0;
    }

    /**
     * Checks whether a player is an owner of region or any of its parents.
     *
     * @param player player to check
     * @return whether an owner
     */
    public boolean isOwner(LocalPlayer player) {
        if (owners.contains(player)) {
            return true;
        }

        ProtectedRegion curParent = getParent();
        while (curParent != null) {
            if (curParent.getOwners().contains(player)) {
                return true;
            }

            curParent = curParent.getParent();
        }

        return false;
    }

    /**
     * Checks whether a player is a member OR OWNER of the region
     * or any of its parents.
     *
     * @param player player to check
     * @return whether an owner or member
     */
    public boolean isMember(LocalPlayer player) {
        if (owners.contains(player) || members.contains(player)) {
            return true;
        }

        ProtectedRegion curParent = getParent();
        while (curParent != null) {
            if (curParent.getOwners().contains(player)
                    || curParent.getMembers().contains(player)) {
                return true;
            }

            curParent = curParent.getParent();
        }

        return false;
    }

    /**
     * Checks whether a player is a member of the region
     * or any of its parents.
     *
     * @param player player to check
     * @return whether an member
     */
    public boolean isMemberOnly(LocalPlayer player) {
        if (members.contains(player)) {
            return true;
        }

        ProtectedRegion curParent = getParent();
        while (curParent != null) {
            if (curParent.getMembers().contains(player)) {
                return true;
            }

            curParent = curParent.getParent();
        }

        return false;
    }

    /**
     * Get a flag's value.
     *
     * @param <T>
     * @param <V>
     * @param flag
     * @return value or null if isn't defined
     */
    @SuppressWarnings("unchecked")
    public <T extends Flag<V>, V> V getFlag(T flag) {
        Object obj = flags.get(flag);
        V val;
        if (obj != null) {
            val = (V) obj;
        } else {
            return null;
        }
        return val;
    }

    /**
     * Set a flag's value.
     *
     * @param <T>
     * @param <V>
     * @param flag
     * @param val
     */
    public <T extends Flag<V>, V> void setFlag(T flag, V val) {
        if (val == null) {
            flags.remove(flag);
        } else {
            flags.put(flag, val);
        }
    }

    /**
     * Get the map of flags.
     *
     * @return
     */
    public Map<Flag<?>, Object> getFlags() {
        return flags;
    }

    /**
     * Get the map of flags.
     *
     * @param flags
     */
    public void setFlags(Map<Flag<?>, Object> flags) {
        this.flags = flags;
    }

    /**
     * Gets the 2D points for this region
     *
     * @return
     */
    public abstract List<BlockVector2D> getPoints();

    /**
     * Get the number of blocks in this region
     *
     * @return
     */
    public abstract int volume();

    /**
     * Check to see if a point is inside this region.
     *
     * @param pt
     * @return
     */
    public abstract boolean contains(Vector pt);

    /**
     * Check to see if a point is inside this region.
     *
     * @param pt
     * @return
     */
    public boolean contains(BlockVector pt) {
        return contains(new Vector(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()));
    }

    /**
     * Check to see if a point is inside this region.
     *
     * @param pt
     * @return
     */
    public boolean contains(Vector2D pt) {
        return contains(new Vector(pt.getX(), min.getBlockY(), pt.getZ()));
    }

    /**
     * Check to see if a point is inside this region.
     *
     * @param pt
     * @return
     */
    public boolean contains(BlockVector2D pt) {
        return contains(new Vector(pt.getBlockX(), min.getBlockY(), pt.getBlockZ()));
    }

    /**
     * Check to see if any of the 2D points are inside this region.
     *
     * @param pts
     * @return
     */
    public boolean containsAny(List<BlockVector2D> pts) {
        for (BlockVector2D pt : pts) {
            if (contains(pt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares to another region.
     *
     * @param other
     * @return
     */
    public int compareTo(ProtectedRegion other) {
        if (id.equals(other.id)) {
            return 0;
        } else if (priority == other.priority) {
            return 1;
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
     * Get a list of intersecting regions.
     *
     * @param regions
     * @return
     * @throws UnsupportedIntersectionException
     */
    public abstract List<ProtectedRegion> getIntersectingRegions(
            List<ProtectedRegion> regions)
            throws UnsupportedIntersectionException;

    /**
     * Checks if the bounding box of a region intersects with with the bounding
     * box of this region
     *
     * @param region
     */
    protected boolean intersectsBoundingBox(ProtectedRegion region) {
        BlockVector rMaxPoint = region.getMaximumPoint();
        BlockVector min = getMinimumPoint();

        if (rMaxPoint.getBlockX() < min.getBlockX()) return false;
        if (rMaxPoint.getBlockY() < min.getBlockY()) return false;
        if (rMaxPoint.getBlockZ() < min.getBlockZ()) return false;

        BlockVector rMinPoint = region.getMinimumPoint();
        BlockVector max = getMaximumPoint();

        if (rMinPoint.getBlockX() > max.getBlockX()) return false;
        if (rMinPoint.getBlockY() > max.getBlockY()) return false;
        if (rMinPoint.getBlockZ() > max.getBlockZ()) return false;

        return true;
    }

    /**
     * Checks to see if the given ID is accurate.
     *
     * @param id
     * @return
     */
    public static boolean isValidId(String id) {
        return idPattern.matcher(id).matches();
    }

    /**
     * Returns the hash code.
     */
    @Override
    public int hashCode(){
        return id.hashCode();
    }

    /**
     * Returns whether this region has the same ID as another region.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProtectedRegion)) {
            return false;
        }

        ProtectedRegion other = (ProtectedRegion) obj;
        return other.getId().equals(getId());
    }

    /**
     * Thrown when setting a curParent would create a circular inheritance
     * situation.
     *
     */
    public static class CircularInheritanceException extends Exception {
        private static final long serialVersionUID = 7479613488496776022L;
    }
}
