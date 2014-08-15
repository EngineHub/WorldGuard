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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldguard.protection.flags.StateFlag.*;

/**
 * Represents the effective set of flags, owners, and members for a given
 * spatial query.
 *
 * <p>An instance of this can be created using the spatial query methods
 * available on {@link RegionManager}.</p>
 */
public class ApplicableRegionSet implements Iterable<ProtectedRegion> {

    /**
     * A static instance of an empty set.
     */
    private static final ApplicableRegionSet EMPTY = new ApplicableRegionSet(Collections.<ProtectedRegion>emptyList(), null);

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
        return test(calculateState(DefaultFlag.BUILD, player, null));
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

        return test(calculateState(flag, null, null));
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
        return test(calculateState(flag, null, player));
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
     * Calculate the effective value of a flag based on the regions
     * in this set, membership, the global region (if set), and the default
     * value of a flag {@link StateFlag#getDefault()}.
     * 
     * @param flag the flag to check
     * @param player the player, or null to not check owners and members
     * @param groupPlayer a player to use for the group flag check
     * @return the allow/deny state for the flag
     */
    private State calculateState(StateFlag flag, @Nullable LocalPlayer player, @Nullable LocalPlayer groupPlayer) {
        checkNotNull(flag);

        int minimumPriority = Integer.MIN_VALUE;
        boolean regionsThatCountExistHere = false; // We can't do a application.isEmpty() because
                                                   // PASSTHROUGH regions have to be skipped
                                                   // (in some cases)
        State state = null; // Start with NONE

        // Say there are two regions in one location: CHILD and PARENT (CHILD
        // is a child of PARENT). If there are two overlapping regions in WG, a
        // player has to be a member of /both/ (or flags permit) in order to
        // build in that location. However, inheritance is supposed
        // to allow building if the player is a member of just CHILD. That
        // presents a problem.
        //
        // To rectify this, we keep two sets. When we iterate over the list of
        // regions, there are two scenarios that we may encounter:
        //
        // 1) PARENT first, CHILD later:
        //    a) When the loop reaches PARENT, PARENT is added to needsClear.
        //    b) When the loop reaches CHILD, parents of CHILD (which includes
        //       PARENT) are removed from needsClear.
        //    c) needsClear is empty again.
        //
        // 2) CHILD first, PARENT later:
        //    a) When the loop reaches CHILD, CHILD's parents (i.e. PARENT) are
        //       added to hasCleared.
        //    b) When the loop reaches PARENT, since PARENT is already in
        //       hasCleared, it does not add PARENT to needsClear.
        //    c) needsClear stays empty.
        //
        // As long as the process ends with needsClear being empty, then
        // we have satisfied all membership requirements.

        Set<ProtectedRegion> needsClear = new HashSet<ProtectedRegion>();
        Set<ProtectedRegion> hasCleared = new HashSet<ProtectedRegion>();

        for (ProtectedRegion region : applicable) {
            // Don't consider lower priorities below minimumPriority
            // (which starts at Integer.MIN_VALUE). A region that "counts"
            // (has the flag set OR has members) will raise minimumPriority
            // to its own priority.
            if (region.getPriority() < minimumPriority) {
                break;
            }

            // If PASSTHROUGH is set and we are checking to see if a player
            // is a member, then skip this region
            if (player != null && getStateFlagIncludingParents(region, DefaultFlag.PASSTHROUGH) == State.ALLOW) {
                continue;
            }

            // If the flag has a group set on to it, skip this region if
            // the group does not match our (group) player
            if (groupPlayer != null && flag.getRegionGroupFlag() != null) {
                RegionGroup group = region.getFlag(flag.getRegionGroupFlag());
                if (group == null) {
                    group = flag.getRegionGroupFlag().getDefault();
                }

                if (!RegionGroupFlag.isMember(region, group, groupPlayer)) {
                    continue;
                }
            }

            regionsThatCountExistHere = true;

            State v = getStateFlagIncludingParents(region, flag);

            // DENY overrides everything
            if (v == State.DENY) {
                state = State.DENY;
                break; // No need to process any more regions

            // ALLOW means we don't care about membership
            } else if (v == State.ALLOW) {
                state = State.ALLOW;
                minimumPriority = region.getPriority();

            } else {
                if (player != null) {
                    minimumPriority = region.getPriority();

                    if (!hasCleared.contains(region)) {
                        if (!region.isMember(player)) {
                            needsClear.add(region);
                        } else {
                            // Need to clear all parents
                            clearParents(needsClear, hasCleared, region);
                        }
                    }
                }
            }
        }

        if (player != null) {
            State fallback;

            if (regionsThatCountExistHere) {
                fallback = allowOrNone(needsClear.isEmpty());
            } else {
                fallback = getDefault(flag, player);
            }

            return combine(state, fallback);
        } else {
            return combine(state, getDefault(flag, null));
        }
    }

    @Nullable
    private State getDefault(StateFlag flag, @Nullable LocalPlayer player) {
        boolean allowed = flag.getDefault();

        // Handle defaults
        if (globalRegion != null) {
            State globalState = globalRegion.getFlag(flag);

            // The global region has this flag set
            if (globalState != null) {
                // Build flag is very special
                if (player != null && globalRegion.hasMembersOrOwners()) {
                    allowed = globalRegion.isMember(player) && (globalState == State.ALLOW);
                } else {
                    allowed = (globalState == State.ALLOW);
                }
            } else {
                // Build flag is very special
                if (player != null && globalRegion.hasMembersOrOwners()) {
                    allowed = globalRegion.isMember(player);
                }
            }
        }

        return allowed ? State.ALLOW : null;
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
     * Get a region's state flag, checking parent regions until a value for the
     * flag can be found (if one even exists).
     *
     * @param region the region
     * @param flag the flag
     * @return the value
     */
    private static State getStateFlagIncludingParents(ProtectedRegion region, StateFlag flag) {
        while (region != null) {
            State value = region.getFlag(flag);

            if (value != null) {
                return value;
            }

            region = region.getParent();
        }

        return null;
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

    /**
     * Return an instance that contains no regions and has no global region.
     */
    public static ApplicableRegionSet getEmpty() {
        return EMPTY;
    }

}
