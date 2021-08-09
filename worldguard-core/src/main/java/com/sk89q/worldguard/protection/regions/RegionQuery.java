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

import com.google.common.collect.ImmutableList;
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
import com.sk89q.worldguard.protection.flags.MapFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.index.RegionIndex;
import com.sk89q.worldguard.protection.util.NormativeOrders;
import com.sk89q.worldguard.protection.util.RegionCollectionConsumer;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    }

    /**
     * Query for regions containing the given location.
     *
     * <p>{@link QueryOption#COMPUTE_PARENTS} is used.</p>
     *
     * <p>An instance of {@link ApplicableRegionSet} will always be returned,
     * even if regions are disabled or region data failed to load. An
     * appropriate "virtual" set will be returned in such a case (for example,
     * if regions are disabled, the returned set would permit all
     * activities).</p>
     *
     * @param location the location
     * @return a region set
     */
    public ApplicableRegionSet getApplicableRegions(Location location) {
        return getApplicableRegions(location, QueryOption.COMPUTE_PARENTS);
    }

    /**
     * Query for regions containing the given location.
     *
     * <p>An instance of {@link ApplicableRegionSet} will always be returned,
     * even if regions are disabled or region data failed to load. An
     * appropriate "virtual" set will be returned in such a case (for example,
     * if regions are disabled, the returned set would permit all
     * activities).</p>
     *
     * @param location the location
     * @param option the option
     * @return a region set
     */
    public ApplicableRegionSet getApplicableRegions(Location location, QueryOption option) {
        checkNotNull(location);
        checkNotNull(option);

        World world = (World) location.getExtent();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return PermissiveRegionSet.getInstance();
        }

        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get((World) location.getExtent());
        if (manager != null) {
            return cache.queryContains(manager, location, option);
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
     * <p>This method does include parameters for a {@link MapFlag}.</p>
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
     * @param mapFlag the MapFlag
     * @param key the key for the MapFlag
     * @param fallback the fallback flag for MapFlag
     * @param flag the flags
     * @return true if the result was {@code ALLOW}
     * @see RegionResultSet#queryValue(RegionAssociable, Flag)
     */
    public <K> boolean testBuild(Location location, RegionAssociable associable, MapFlag<K, State> mapFlag, K key,
                                 @Nullable StateFlag fallback, StateFlag... flag) {
        if (mapFlag == null)
            return testBuild(location, associable, flag);

        if (flag.length == 0) {
            return StateFlag.test(StateFlag.combine(
                    StateFlag.denyToNone(queryState(location, associable, Flags.BUILD)),
                    queryMapValue(location, associable, mapFlag, key, fallback)
            ));
        }

        return StateFlag.test(StateFlag.combine(
                StateFlag.denyToNone(queryState(location, associable, Flags.BUILD)),
                queryMapValue(location, associable, mapFlag, key, fallback),
                queryState(location, associable, flag)
        ));
    }

    /**
     * Test whether the (effective) value for a list of state flags equals
     * {@code ALLOW}.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
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
     * regions. (Flags on a region can be changed so that they only apply
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
     * regions. (Flags on a region can be changed so that they only apply
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
     * regions. (Flags on a region can be changed so that they only apply
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
     * regions. (Flags on a region can be changed so that they only apply
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
     * regions. (Flags on a region can be changed so that they only apply
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
     * Get the effective value for a key in a {@link MapFlag}. If there are multiple values
     * (for example, if there are multiple regions with the same priority
     * but with different farewell messages set, there would be multiple
     * completing values), then the selected (or "winning") value will be undefined.
     *
     * <p>A subject can be provided that is used to determine whether the value
     * of a flag on a particular region should be used. For example, if a
     * flag's region group is set to {@link RegionGroup#MEMBERS} and the given
     * subject is not a member, then the region would be skipped when
     * querying that flag. If {@code null} is provided for the subject, then
     * only flags that use {@link RegionGroup#ALL},
     * {@link RegionGroup#NON_MEMBERS}, etc. will apply.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag of type {@link MapFlag}
     * @param key the key for the map flag
     * @return a value, which could be {@code null}
     */
    @Nullable
    public <V, K> V queryMapValue(Location location, @Nullable RegionAssociable subject, MapFlag<K, V> flag, K key) {
        return getApplicableRegions(location).queryMapValue(subject, flag, key);
    }

    /**
     * Get the effective value for a key in a {@link MapFlag}. If there are multiple values
     * (for example, if there are multiple regions with the same priority
     * but with different farewell messages set, there would be multiple
     * completing values), then the selected (or "winning") value will be undefined.
     *
     * <p>A subject can be provided that is used to determine whether the value
     * of a flag on a particular region should be used. For example, if a
     * flag's region group is set to {@link RegionGroup#MEMBERS} and the given
     * subject is not a member, then the region would be skipped when
     * querying that flag. If {@code null} is provided for the subject, then
     * only flags that use {@link RegionGroup#ALL},
     * {@link RegionGroup#NON_MEMBERS}, etc. will apply.</p>
     *
     * <p>It's possible to provide a fallback flag for the case when the key doesn't
     * exist in the {@link MapFlag}.</p>
     *
     * @param subject an optional subject, which would be used to determine the region group to apply
     * @param flag the flag of type {@link MapFlag}
     * @param key the key for the map flag
     * @param fallback the fallback flag
     * @return a value, which could be {@code null}
     */
    @Nullable
    public <V, K> V queryMapValue(Location location, @Nullable RegionAssociable subject, MapFlag<K, V> flag, K key, Flag<V> fallback) {
        return getApplicableRegions(location).queryMapValue(subject, flag, key, fallback);
    }

    /**
     * Get the effective values for a flag, returning a collection of all
     * values. It is up to the caller to determine which value, if any,
     * from the collection will be used.
     *
     * <p>{@code player} can be non-null to satisfy region group requirements,
     * otherwise it will be assumed that the caller that is not a member of any
     * regions. (Flags on a region can be changed so that they only apply
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
     * regions. (Flags on a region can be changed so that they only apply
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

    /**
     * Options for constructing a region set via
     * {@link #getApplicableRegions(Location, QueryOption)} for example.
     */
    public enum QueryOption {
        /**
         * Constructs a region set that does not include parent regions and
         * may be left unsorted (but a cached, sorted set of the same regions
         * may be returned).
         */
        NONE(false) {
            @Override
            public List<ProtectedRegion> constructResult(Set<ProtectedRegion> applicable) {
                return ImmutableList.copyOf(applicable);
            }

            @Override
            Map<QueryOption, ApplicableRegionSet> createCache(RegionManager manager, Location location, Map<QueryOption, ApplicableRegionSet> cache) {
                if (cache == null) {
                    cache = new EnumMap<>(QueryOption.class);
                    cache.put(QueryOption.NONE, manager.getApplicableRegions(location.toVector().toBlockPoint(), QueryOption.NONE));
                }

                // If c != null, we can assume that Option.NONE is present.
                return cache;
            }
        },

        /**
         * Constructs a region set that does not include parent regions and is
         * sorted by {@link NormativeOrders}.
         */
        SORT(false) {
            @Override
            Map<QueryOption, ApplicableRegionSet> createCache(RegionManager manager, Location location, Map<QueryOption, ApplicableRegionSet> cache) {
                if (cache == null) {
                    Map<QueryOption, ApplicableRegionSet> newCache = new EnumMap<>(QueryOption.class);
                    ApplicableRegionSet result = manager.getApplicableRegions(location.toVector().toBlockPoint(), QueryOption.SORT);
                    newCache.put(QueryOption.NONE, result);
                    newCache.put(QueryOption.SORT, result);
                    return newCache;
                } else {
                    // If c != null, we can assume that Option.NONE is present.
                    cache.computeIfAbsent(QueryOption.SORT, k -> new RegionResultSet(cache.get(QueryOption.NONE).getRegions(), manager.getRegion("__global__")));
                    return cache;
                }
            }
        },

        /**
         * Constructs a region set that includes parent regions and is sorted by
         * {@link NormativeOrders}.
         */
        COMPUTE_PARENTS(true) {
            @Override
            Map<QueryOption, ApplicableRegionSet> createCache(RegionManager manager, Location location, Map<QueryOption, ApplicableRegionSet> cache) {
                if (cache == null) {
                    Map<QueryOption, ApplicableRegionSet> newCache = new EnumMap<>(QueryOption.class);
                    ApplicableRegionSet noParResult = manager.getApplicableRegions(location.toVector().toBlockPoint(), QueryOption.NONE);
                    Set<ProtectedRegion> noParRegions = noParResult.getRegions();
                    Set<ProtectedRegion> regions = new HashSet<>();
                    noParRegions.forEach(new RegionCollectionConsumer(regions, true)::apply);
                    ApplicableRegionSet result = new RegionResultSet(regions, manager.getRegion("__global__"));

                    if (regions.size() == noParRegions.size()) {
                        newCache.put(QueryOption.NONE, result);
                        newCache.put(QueryOption.SORT, result);
                    } else {
                        newCache.put(QueryOption.NONE, noParResult);
                    }

                    newCache.put(QueryOption.COMPUTE_PARENTS, result);
                    return newCache;
                }

                cache.computeIfAbsent(QueryOption.COMPUTE_PARENTS, k -> {
                    Set<ProtectedRegion> regions = new HashSet<>();
                    ApplicableRegionSet result = cache.get(QueryOption.SORT);
                    boolean sorted = true;

                    if (result == null) {
                        // If c != null, we can assume that Option.NONE is present.
                        result = cache.get(QueryOption.NONE);
                        sorted = false;
                    }

                    Set<ProtectedRegion> noParRegions = result.getRegions();
                    noParRegions.forEach(new RegionCollectionConsumer(regions, true)::apply);

                    if (sorted && regions.size() == noParRegions.size()) {
                        return result;
                    }

                    result = new RegionResultSet(regions, manager.getRegion("__global__"));

                    if (regions.size() == noParRegions.size()) {
                        cache.put(QueryOption.SORT, result);
                    }

                    return result;
                });
                return cache;
            }
        };

        private final boolean collectParents;

        QueryOption(boolean collectParents) {
            this.collectParents = collectParents;
        }

        /**
         * Create a {@link RegionCollectionConsumer} with the given collection
         * used for the {@link RegionIndex}. Internal API.
         *
         * @param collection the collection
         * @return a region collection consumer
         */
        public RegionCollectionConsumer createIndexConsumer(Collection<? super ProtectedRegion> collection) {
            return new RegionCollectionConsumer(collection, collectParents);
        }

        /**
         * Convert the set of regions to a list. Sort and add parents if
         * necessary. Internal API.
         *
         * @param applicable the set of regions
         * @return a list of regions
         */
        public List<ProtectedRegion> constructResult(Set<ProtectedRegion> applicable) {
            return NormativeOrders.fromSet(applicable);
        }

        /**
         * Create (if null) or update the given cache map with at least an entry
         * for this option if necessary and return it.
         *
         * @param manager the manager
         * @param location the location
         * @param cache the cache map
         * @return a cache map
         */
        abstract Map<QueryOption, ApplicableRegionSet> createCache(RegionManager manager, Location location, Map<QueryOption, ApplicableRegionSet> cache);

    }

}
