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
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * Represents the effective set of flags, owners, and members for a given
 * spatial query.
 *
 * <p>An instance of this can be created using the spatial query methods
 * available on {@link RegionManager}.</p>
 */
public interface ApplicableRegionSet extends Iterable<ProtectedRegion> {

    /**
     * Return whether this region set is a virtual set. A virtual set
     * does not contain real results.
     *
     * <p>A virtual result may be returned if region data failed to load or
     * there was some special exception (i.e. the region bypass permission).
     * </p>
     *
     * <p>Be sure to check the value of this flag if an instance of this
     * interface is being retrieved from {@link RegionQuery} as it may
     * return an instance of {@link PermissiveRegionSet} or
     * {@link FailedLoadRegionSet}, among other possibilities.</p>
     *
     * @return true if loaded
     * @see FailedLoadRegionSet
     */
    boolean isVirtual();

    /**
     * Tests whether the {@link DefaultFlag#BUILD} flag or membership
     * requirements permit the given player.
     *
     * @param player the player to check
     * @return true if permitted
     * @deprecated use {@link #testState(RegionAssociable, StateFlag...)}
     */
    @Deprecated
    boolean canBuild(LocalPlayer player);

    /**
     * Test whether the (effective) value for a list of state flags equals
     * {@code ALLOW}.
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link DefaultFlag#BUILD} flag is in the list of flags.</p>
     *
     * @param subject an optional subject, which would be used to determine the region groups that apply
     * @param flags a list of flags to check
     * @return true if the result was {@code ALLOW}
     * @see #queryState(RegionAssociable, StateFlag...)
     */
    boolean testState(@Nullable RegionAssociable subject, StateFlag... flags);

    /**
     * Get the (effective) value for a list of state flags. The rules of
     * states is observed here; that is, {@code DENY} overrides {@code ALLOW},
     * and {@code ALLOW} overrides {@code NONE}. One flag may override another.
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link DefaultFlag#BUILD} flag is in the list of flags.</p>
     *
     * @param subject an optional subject, which would be used to determine the region groups that apply
     * @param flags a list of flags to check
     * @return a state
     */
    @Nullable
    State queryState(@Nullable RegionAssociable subject, StateFlag... flags);

    /**
     * Get the effective value for a flag. If there are multiple values
     * (for example, multiple overlapping regions with
     * the same priority may have the same flag set), then the selected
     * (or "winning") value will depend on the flag type.
     *
     * <p>Only some flag types actually have a strategy for picking the
     * "best value." For most types, the actual value that is chosen to be
     * returned is undefined (it could be any value). As of writing, the only
     * type of flag that actually has a strategy for picking a value is the
     * {@link StateFlag}.</p>
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link DefaultFlag#BUILD} flag is the flag being queried.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a value, which could be {@code null}
     */
    @Nullable
    <V> V queryValue(@Nullable RegionAssociable subject, Flag<V> flag);

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>{@code subject} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
     * to certain users.) The subject argument is required if the
     * {@link DefaultFlag#BUILD} flag is the flag being queried.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a collection of values
     */
    <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag);

    /**
     * Test whether the construct flag evaluates true for the given player.
     *
     * @param player the player
     * @return true if true
     * @deprecated The {@code CONSTRUCT} flag is being removed and is no longer
     *             needed because flags now support groups assigned to them.
     */
    @Deprecated
    boolean canConstruct(LocalPlayer player);

    /**
     * Gets the state of a state flag. This cannot be used for the build flag.
     *
     * @param flag flag to check
     * @return whether it is allowed
     * @throws IllegalArgumentException if the build flag is given
     * @deprecated use {@link #queryState(RegionAssociable, StateFlag...)} instead
     */
    @Deprecated
    boolean allows(StateFlag flag);

    /**
     * Gets the state of a state flag. This cannot be used for the build flag.
     *
     * @param flag flag to check
     * @param player player (used by some flags)
     * @return whether the state is allows for it
     * @throws IllegalArgumentException if the build flag is given
     * @deprecated use {@link #queryState(RegionAssociable, StateFlag...)} instead
     */
    @Deprecated
    boolean allows(StateFlag flag, @Nullable LocalPlayer player);

    /**
     * Test whether a player is an owner of all regions in this set.
     *
     * @param player the player
     * @return whether the player is an owner of all regions
     */
    boolean isOwnerOfAll(LocalPlayer player);

    /**
     * Test whether a player is an owner or member of all regions in this set.
     *
     * @param player the player
     * @return whether the player is a member of all regions
     */
    boolean isMemberOfAll(LocalPlayer player);

    /**
     * Gets the value of a flag. Do not use this for state flags
     * (use {@link #allows(StateFlag, LocalPlayer)} for that).
     *
     * @param flag the flag to check
     * @return value of the flag, which may be null
     * @deprecated Use {@link #queryValue(RegionAssociable, Flag)} instead. There
     *             is no difference in functionality.
     */
    @Deprecated
    @Nullable
    <T extends Flag<V>, V> V getFlag(T flag);

    /**
     * Gets the value of a flag. Do not use this for state flags
     * (use {@link #allows(StateFlag, LocalPlayer)} for that).
     *
     * @param flag flag to check
     * @param groupPlayer player to check {@link RegionGroup}s against
     * @return value of the flag, which may be null
     * @throws IllegalArgumentException if a StateFlag is given
     * @deprecated Use {@link #queryValue(RegionAssociable, Flag)} instead. There
     *             is no difference in functionality.
     */
    @Deprecated
    @Nullable
    <T extends Flag<V>, V> V getFlag(T flag, @Nullable LocalPlayer groupPlayer);

    /**
     * Get the number of regions that are included.
     *
     * @return the number of contained regions
     */
    int size();

    /**
     * Get an immutable set of regions that are included in this set.
     *
     * @return a set of regions
     */
    Set<ProtectedRegion> getRegions();

}
