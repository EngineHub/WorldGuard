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

package com.sk89q.worldguard.protection.regions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.FailedLoadRegionSet;
import com.sk89q.worldguard.protection.PermissiveRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * This object allows easy spatial queries involving region data for the
 * purpose of implementing protection / region flag checks.
 *
 * <p>Results may be cached for brief amounts of time. If you want to get
 * data for the purposes of changing it, use of this class is not recommended.
 * Some of the return values of the methods may be simulated to reduce
 * boilerplate code related to implementing protection, meaning that false
 * data is returned.</p>
 */
public class RegionQuery {

    private final ConfigurationManager config;
    private final QueryCache cache;

    /**
     * Create a new instance.
     *
     * @param cache the query cache
     */
    public RegionQuery(QueryCache cache) {
        checkNotNull(cache);

        this.config = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        this.cache = cache;
        //noinspection deprecation
    }

    /**
     * Query for regions containing the given location.
     *
     * <p>An instance of {@link RegionResultSet} will always be returned,
     * even if regions are disabled or region data failed to load. An
     * appropriate "virtual" set will be returned in such a case
     * (for example, if regions are disabled, the returned set
     * would permit all activities).</p>
     *
     * @param location the location
     * @return a region set
     */
    public ApplicableRegionSet getApplicableRegions(Location location) {
        checkNotNull(location);

        World world = (World) location.getExtent();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return PermissiveRegionSet.getInstance();
        }

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get((World) location.getExtent());
        if (manager != null) {
            return cache.queryContains(manager, location);
        } else {
            return FailedLoadRegionSet.getInstance();
        }
    }

    /**
     * Returns true if the BUILD flag allows the action in the location, but it
     * can be overridden by a list of other flags. The BUILD flag will not
     * override the other flags, but the other flags can override BUILD. If
     * neither BUILD or any of the flags permit the action, then false will
     * be returned.
     *
     * <p>Use this method when checking flags that are related to build
     * protection. For example, lighting fire in a region should not be
     * permitted unless the player is a member of the region or the
     * LIGHTER flag allows it. However, the LIGHTER flag should be able
     * to allow lighting fires even if BUILD is set to DENY.</p>
     *
     * <p>How this method works (BUILD can be overridden by other flags but
     * not the other way around) is inconsistent, but it's required for
     * legacy reasons.</p>
     *
     * <p>This method does not check the region bypass permission. That must
     * be done by the calling code.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region group to apply
     * @param flag the flag
     * @return true if the result was {@code ALLOW}
     * @see RegionResultSet#queryValue(RegionAssociable, Flag)
     */
    public boolean testBuild(Location location, LocalPlayer player, StateFlag... flag) {
        if (flag.length == 0) {
            return testState(location, player, Flags.BUILD);
        }

        return StateFlag.test(StateFlag.combine(
                StateFlag.denyToNone(queryState(location, player, Flags.BUILD)),
                queryState(location, player, flag)));
    }

    /**
     * Returns true if the BUILD flag allows the action in the location, but it
     * can be overridden by a list of other flags. The BUILD flag will not
     * override the other flags, but the other flags can override BUILD. If
     * neither BUILD or any of the flags permit the action, then false will
     * be returned.
     *
     * <p>Use this method when checking flags that are related to build
     * protection. For example, lighting fire in a region should not be
     * permitted unless the player is a member of the region or the
     * LIGHTER flag allows it. However, the LIGHTER flag should be able
     * to allow lighting fires even if BUILD is set to DENY.</p>
     *
     * <p>How this method works (BUILD can be overridden by other flags but
     * not the other way around) is inconsistent, but it's required for
     * legacy reasons.</p>
     *
     * <p>This method does not check the region bypass permission. That must
     * be done by the calling code.</p>
     *
     * @param location the location
     * @param associable an optional associable
     * @param flag the flag
     * @return true if the result was {@code ALLOW}
     * @see RegionResultSet#queryValue(RegionAssociable, Flag)
     */
    public boolean testBuild(Location location, RegionAssociable associable, StateFlag... flag) {
        if (flag.length == 0) {
            return testState(location, associable, Flags.BUILD);
        }

        return StateFlag.test(StateFlag.combine(
                StateFlag.denyToNone(queryState(location, associable, Flags.BUILD)),
                queryState(location, associable, flag)));
    }

    /**
     * Test whether the (effective) value for a list of state flags equals
     * {@code ALLOW}.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is in the list of flags.</p>
     *
     * <p>This method does not check the region bypass permission. That must
     * be done by the calling code.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region group to apply
     * @param flag the flag
     * @return true if the result was {@code ALLOW}
     * @see RegionResultSet#queryValue(RegionAssociable, Flag)
     */
    public boolean testState(Location location, @Nullable LocalPlayer player, StateFlag... flag) {
        return StateFlag.test(queryState(location, player, flag));
    }

    /**
     * Test whether the (effective) value for a list of state flags equals
     * {@code ALLOW}.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is in the list of flags.</p>
     *
     * <p>This method does not check the region bypass permission. That must
     * be done by the calling code.</p>
     *
     * @param location the location
     * @param associable an optional associable
     * @param flag the flag
     * @return true if the result was {@code ALLOW}
     * @see RegionResultSet#queryValue(RegionAssociable, Flag)
     */
    public boolean testState(Location location, @Nullable RegionAssociable associable, StateFlag... flag) {
        return StateFlag.test(queryState(location, associable, flag));
    }

    /**
     * Get the (effective) value for a list of state flags. The rules of
     * states is observed here; that is, {@code DENY} overrides {@code ALLOW},
     * and {@code ALLOW} overrides {@code NONE}. One flag may override another.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is in the list of flags.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region groups that apply
     * @param flags a list of flags to check
     * @return a state
     * @see RegionResultSet#queryState(RegionAssociable, StateFlag...)
     */
    @Nullable
    public State queryState(Location location, @Nullable LocalPlayer player, StateFlag... flags) {
        return getApplicableRegions(location).queryState(player, flags);
    }

    /**
     * Get the (effective) value for a list of state flags. The rules of
     * states is observed here; that is, {@code DENY} overrides {@code ALLOW},
     * and {@code ALLOW} overrides {@code NONE}. One flag may override another.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is in the list of flags.</p>
     *
     * @param location the location
     * @param associable an optional associable
     * @param flags a list of flags to check
     * @return a state
     * @see RegionResultSet#queryState(RegionAssociable, StateFlag...)
     */
    @Nullable
    public State queryState(Location location, @Nullable RegionAssociable associable, StateFlag... flags) {
        return getApplicableRegions(location).queryState(associable, flags);
    }

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
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is the flag being queried.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a value, which could be {@code null}
     * @see RegionResultSet#queryValue(RegionAssociable, Flag)
     */
    @Nullable
    public <V> V queryValue(Location location, @Nullable LocalPlayer player, Flag<V> flag) {
        return getApplicableRegions(location).queryValue(player, flag);
    }

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
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is the flag being queried.</p>
     *
     * @param location the location
     * @param associable an optional associable
     * @param flag the flag
     * @return a value, which could be {@code null}
     * @see RegionResultSet#queryValue(RegionAssociable, Flag)
     */
    @Nullable
    public <V> V queryValue(Location location, @Nullable RegionAssociable associable, Flag<V> flag) {
        return getApplicableRegions(location).queryValue(associable, flag);
    }

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is the flag being queried.</p>
     *
     * @param location the location
     * @param player an optional player, which would be used to determine the region group to apply
     * @param flag the flag
     * @return a collection of values
     * @see RegionResultSet#queryAllValues(RegionAssociable, Flag)
     */
    public <V> Collection<V> queryAllValues(Location location, @Nullable LocalPlayer player, Flag<V> flag) {
        return getApplicableRegions(location).queryAllValues(player, flag);
    }

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (FlagUtil on a region can be changed so that they only apply
     * to certain users.) The player argument is required if the
     * {@link Flags#BUILD} flag is the flag being queried.</p>
     *
     * @param location the location
     * @param associable an optional associable
     * @param flag the flag
     * @return a collection of values
     * @see RegionResultSet#queryAllValues(RegionAssociable, Flag)
     */
    public <V> Collection<V> queryAllValues(Location location, @Nullable RegionAssociable associable, Flag<V> flag) {
        return getApplicableRegions(location).queryAllValues(associable, flag);
    }

}
