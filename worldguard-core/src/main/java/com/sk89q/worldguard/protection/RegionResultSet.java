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
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.NormativeOrders;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation that calculates flags using a list of regions.
 */
public class RegionResultSet extends AbstractRegionSet {

    private final List<ProtectedRegion> applicable;
    private final FlagValueCalculator flagValueCalculator;
    @Nullable
    private Set<ProtectedRegion> regionSet;

    /**
     * Create a new region result set.
     *
     * <p>The given list must not contain duplicates or the behavior of
     * this instance will be undefined.</p>
     *
     * @param applicable the regions contained in this set
     * @param globalRegion the global region, set aside for special handling.
     */
    public RegionResultSet(List<ProtectedRegion> applicable, @Nullable ProtectedRegion globalRegion) {
        this(applicable, globalRegion, false);
    }

    /**
     * Create a new region result set.
     *
     * @param applicable the regions contained in this set
     * @param globalRegion the global region, set aside for special handling.
     */
    public RegionResultSet(Set<ProtectedRegion> applicable, @Nullable ProtectedRegion globalRegion) {
        this(NormativeOrders.fromSet(applicable), globalRegion, true);
        this.regionSet = applicable;
    }

    /**
     * Create a new region result set.
     *
     * <p>The list of regions may be first sorted with
     * {@link NormativeOrders}. If that is the case, {@code sorted} should be
     * {@code true}. Otherwise, the list will be sorted in-place.</p>
     * 
     * @param applicable the regions contained in this set
     * @param globalRegion the global region, set aside for special handling.
     * @param sorted true if the list is already sorted with {@link NormativeOrders}
     */
    public RegionResultSet(List<ProtectedRegion> applicable, @Nullable ProtectedRegion globalRegion, boolean sorted) {
        checkNotNull(applicable);
        if (!sorted) {
            NormativeOrders.sort(applicable);
        }
        this.applicable = applicable;
        this.flagValueCalculator = new FlagValueCalculator(applicable, globalRegion);
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    @Nullable
    public State queryState(@Nullable RegionAssociable subject, StateFlag... flags) {
        return flagValueCalculator.queryState(subject, flags);
    }

    @Override
    @Nullable
    public <V> V queryValue(@Nullable RegionAssociable subject, Flag<V> flag) {
        return flagValueCalculator.queryValue(subject, flag);
    }

    @Override
    public <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag) {
        return flagValueCalculator.queryAllValues(subject, flag);
    }

    @Override
    public boolean isOwnerOfAll(LocalPlayer player) {
        checkNotNull(player);

        for (ProtectedRegion region : applicable) {
            if (!region.isOwner(player)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isMemberOfAll(LocalPlayer player) {
        checkNotNull(player);

        for (ProtectedRegion region : applicable) {
            if (!region.isMember(player)) {
                return false;
            }
        }

        return true;
    }
    
    @Override
    public int size() {
        return applicable.size();
    }

    @Override
    public Set<ProtectedRegion> getRegions() {
        if (regionSet != null) {
            return regionSet;
        }
        regionSet = Collections.unmodifiableSet(new HashSet<ProtectedRegion>(applicable));
        return regionSet;
    }

    @Override
    public Iterator<ProtectedRegion> iterator() {
        return applicable.iterator();
    }

    /**
     * Create a new instance using a list of regions that is known to
     * already be sorted by priority descending.
     *
     * @param regions a list of regions
     * @param globalRegion a global region
     * @return an instance
     */
    public static RegionResultSet fromSortedList(List<ProtectedRegion> regions, @Nullable ProtectedRegion globalRegion) {
        return new RegionResultSet(regions, globalRegion, true);
    }

}
