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

import com.google.common.base.Predicate;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldguard.protection.flags.StateFlag.test;

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
    private final FlagValueCalculator flagValueCalculator;

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
        this.flagValueCalculator = new FlagValueCalculator(applicable, globalRegion);
    }
    
    /**
     * Test whether a player can build in an area.
     * 
     * @param player The player to check
     * @return build ability
     */
    public boolean canBuild(LocalPlayer player) {
        checkNotNull(player);
        return test(flagValueCalculator.testPermission(player, DefaultFlag.BUILD));
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

        return test(flagValueCalculator.queryState(null, flag));
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

        return test(flagValueCalculator.queryState(player, flag));
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
        return flagValueCalculator.queryValue(groupPlayer, flag);
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

    /**
     * Returns true if a player is a member (or owner) of a region.
     */
    private static class RegionMemberTest implements Predicate<ProtectedRegion> {
        private final LocalPlayer player;

        private RegionMemberTest(LocalPlayer player) {
            this.player = checkNotNull(player);
        }

        @Override
        public boolean apply(ProtectedRegion region) {
            return region.isMember(player);
        }
    }

}
