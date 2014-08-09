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

package com.sk89q.worldguard.protection;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the effective set of flags, owners, and members for a given
 * spatial query.
 *
 * <p>An instance of this can be created using the spatial query methods
 * available on {@link RegionManager}.</p>
 */
public class ApplicableRegionSet implements Iterable<ProtectedRegion> {

    private final SortedSet<ProtectedRegion> applicable;
    @Nullable
    private final ProtectedRegion globalRegion;

    /**
     * Construct the object.
     *
     * <p>A sorted set will be created to include the collection of regions.</p>
     *
     * @param applicable the regions contained in this set
     * @param globalRegion the global region, set aside for special handling.
     */
    public ApplicableRegionSet(Collection<ProtectedRegion> applicable, @Nullable ProtectedRegion globalRegion) {
        this(new TreeSet<ProtectedRegion>(checkNotNull(applicable)), globalRegion);
    }

    /**
     * Construct the object.
     * 
     * @param applicable the regions contained in this set
     * @param globalRegion the global region, set aside for special handling.
     */
    public ApplicableRegionSet(SortedSet<ProtectedRegion> applicable, @Nullable ProtectedRegion globalRegion) {
        checkNotNull(applicable);
        this.applicable = applicable;
        this.globalRegion = globalRegion;
    }
    
    /**
     * Test whether a player can build in an area.
     * 
     * @param player The player to check
     * @return build ability
     */
    public boolean canBuild(LocalPlayer player) {
        checkNotNull(player);
        return internalGetState(DefaultFlag.BUILD, player, null);
    }

    /**
     * Test whether the construct flag evaluates true for the given player.
     *
     * @param player the player
     * @return true if true
     */
    public boolean canConstruct(LocalPlayer player) {
        checkNotNull(player);
        final RegionGroup flag = getFlag(DefaultFlag.CONSTRUCT, player);
        return RegionGroupFlag.isMember(this, flag, player);
    }

    /**
     * Gets the state of a state flag. This cannot be used for the build flag.
     *
     * @param flag flag to check
     * @return whether it is allowed
     * @throws IllegalArgumentException if the build flag is given
     */
    public boolean allows(StateFlag flag) {
        checkNotNull(flag);

        if (flag == DefaultFlag.BUILD) {
            throw new IllegalArgumentException("Can't use build flag with allows()");
        }

        return internalGetState(flag, null, null);
    }
    
    /**
     * Gets the state of a state flag. This cannot be used for the build flag.
     * 
     * @param flag flag to check
     * @param player player (used by some flags)
     * @return whether the state is allows for it
     * @throws IllegalArgumentException if the build flag is given
     */
    public boolean allows(StateFlag flag, @Nullable LocalPlayer player) {
        checkNotNull(flag);

        if (flag == DefaultFlag.BUILD) {
            throw new IllegalArgumentException("Can't use build flag with allows()");
        }
        return internalGetState(flag, null, player);
    }
    
    /**
     * Test whether a player is an owner of all regions in this set.
     * 
     * @param player the player
     * @return whether the player is an owner of all regions
     */
    public boolean isOwnerOfAll(LocalPlayer player) {
        checkNotNull(player);

        for (ProtectedRegion region : applicable) {
            if (!region.isOwner(player)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Test whether a player is an owner or member of all regions in this set.
     * 
     * @param player the player
     * @return whether the player is a member of all regions
     */
    public boolean isMemberOfAll(LocalPlayer player) {
        checkNotNull(player);

        for (ProtectedRegion region : applicable) {
            if (!region.isMember(player)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Test whether a flag tests true.
     * 
     * @param flag the flag to check
     * @param player the player, or null to not check owners and members
     * @param groupPlayer a player to use for the group flag check
     * @return the allow/deny state for the flag
     */
    private boolean internalGetState(StateFlag flag, @Nullable LocalPlayer player, @Nullable LocalPlayer groupPlayer) {
        checkNotNull(flag);

        boolean found = false;
        boolean hasFlagDefined = false;
        boolean allowed = false; // Used for ALLOW override
        boolean def = flag.getDefault();
        
        // Handle defaults
        if (globalRegion != null) {
            State globalState = globalRegion.getFlag(flag);

            // The global region has this flag set
            if (globalState != null) {
                // Build flag is very special
                if (player != null && globalRegion.hasMembersOrOwners()) {
                    def = globalRegion.isMember(player) && (globalState == State.ALLOW);
                } else {
                    def = (globalState == State.ALLOW);
                }
            } else {
                // Build flag is very special
                if (player != null && globalRegion.hasMembersOrOwners()) {
                    def = globalRegion.isMember(player);
                }
            }
        }
        
        // The player argument is used if and only if the flag is the build
        // flag -- in which case, if there are any regions in this area, we
        // default to FALSE, otherwise true if there are no defined regions.
        // However, other flags are different -- if there are regions defined,
        // we default to the global region value. 
        if (player == null) {
            allowed = def; 
        }
        
        int lastPriority = Integer.MIN_VALUE;

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

        for (ProtectedRegion region : applicable) {
            // Ignore lower priority regions
            if (hasFlagDefined && region.getPriority() < lastPriority) {
                break;
            }

            lastPriority = region.getPriority();

            // Ignore non-build regions
            if (player != null
                    && region.getFlag(DefaultFlag.PASSTHROUGH) == State.ALLOW) {
                continue;
            }

            // Check group permissions
            if (groupPlayer != null && flag.getRegionGroupFlag() != null) {
                RegionGroup group = region.getFlag(flag.getRegionGroupFlag());
                if (group == null) {
                    group = flag.getRegionGroupFlag().getDefault();
                }
                if (!RegionGroupFlag.isMember(region, group, groupPlayer)) {
                    continue;
                }
            }

            State v = region.getFlag(flag);

            // Allow DENY to override everything
            if (v == State.DENY) {
                return false;
            }

            // Forget about regions that allow it, although make sure the
            // default state is now to allow
            if (v == State.ALLOW) {
                allowed = true;
                found = true;
                hasFlagDefined = true;
                continue;
            }

            // For the build flag, the flags are conditional and are based
            // on membership, so we have to check for parent-child
            // relationships
            if (player != null) {
                hasFlagDefined = true;

                //noinspection StatementWithEmptyBody
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
        }

        return !found ? def : (allowed || (player != null && needsClear.isEmpty()));
    }

    /**
     * Clear a region's parents for isFlagAllowed().
     * 
     * @param needsClear the regions that should be cleared
     * @param hasCleared the regions already cleared
     * @param region the region to start from
     */
    private void clearParents(Set<ProtectedRegion> needsClear, Set<ProtectedRegion> hasCleared, ProtectedRegion region) {
        ProtectedRegion parent = region.getParent();

        while (parent != null) {
            if (!needsClear.remove(parent)) {
                hasCleared.add(parent);
            }

            parent = parent.getParent();
        }
    }

    /**
     * Gets the value of a flag. Do not use this for state flags
     * (use {@link #allows(StateFlag, LocalPlayer)} for that).
     *
     * @param flag the flag to check
     * @return value of the flag, which may be null
     */
    @Nullable
    public <T extends Flag<V>, V> V getFlag(T flag) {
        return getFlag(flag, null);
    }

    /**
     * Gets the value of a flag. Do not use this for state flags
     * (use {@link #allows(StateFlag, LocalPlayer)} for that).
     * 
     * @param flag flag to check
     * @param groupPlayer player to check {@link RegionGroup}s against
     * @return value of the flag, which may be null
     * @throws IllegalArgumentException if a StateFlag is given
     */
    @Nullable
    public <T extends Flag<V>, V> V getFlag(T flag, @Nullable LocalPlayer groupPlayer) {
        checkNotNull(flag);

        /*
        if (flag instanceof StateFlag) {
            throw new IllegalArgumentException("Cannot use StateFlag with getFlag()");
        }
        */

        int lastPriority = 0;
        boolean found = false;

        Map<ProtectedRegion, V> needsClear = new HashMap<ProtectedRegion, V>();
        Set<ProtectedRegion> hasCleared = new HashSet<ProtectedRegion>();

        for (ProtectedRegion region : applicable) {
            // Ignore lower priority regions
            if (found && region.getPriority() < lastPriority) {
                break;
            }

            // Check group permissions
            if (groupPlayer != null && flag.getRegionGroupFlag() != null) {
                RegionGroup group = region.getFlag(flag.getRegionGroupFlag());
                if (group == null) {
                    group = flag.getRegionGroupFlag().getDefault();
                }
                if (!RegionGroupFlag.isMember(region, group, groupPlayer)) {
                    continue;
                }
            }

            //noinspection StatementWithEmptyBody
            if (hasCleared.contains(region)) {
                // Already cleared, so do nothing
            } else if (region.getFlag(flag) != null) {
                clearParents(needsClear, hasCleared, region);

                needsClear.put(region, region.getFlag(flag));

                found = true;
            }

            lastPriority = region.getPriority();
        }
        
        if (!needsClear.isEmpty()) {
            return needsClear.values().iterator().next();
        } else {
            if (globalRegion != null) {
                V gFlag = globalRegion.getFlag(flag);
                if (gFlag != null) return gFlag;
            }
            return null;
        }
    }

    /**
     * Clear a region's parents for getFlag().
     * 
     * @param needsClear The regions that should be cleared
     * @param hasCleared The regions already cleared
     * @param region The region to start from
     */
    private void clearParents(Map<ProtectedRegion, ?> needsClear,
            Set<ProtectedRegion> hasCleared, ProtectedRegion region) {
        ProtectedRegion parent = region.getParent();

        while (parent != null) {
            if (needsClear.remove(parent) == null) {
                hasCleared.add(parent);
            }

            parent = parent.getParent();
        }
    }
    
    /**
     * Get the number of regions that are included.
     * 
     * @return the number of contained regions
     */
    public int size() {
        return applicable.size();
    }

    @Override
    public Iterator<ProtectedRegion> iterator() {
        return applicable.iterator();
    }

}
