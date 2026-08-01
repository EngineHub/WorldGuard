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
import com.sk89q.worldguard.protection.flags.MapFlag;
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
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#testBuild(RegionAssociable, StateFlag...)
     * .testBuild(RegionAssociable, StateFlag...)}
     *
     * @deprecated use {@link #testBuild(Location, RegionAssociable, StateFlag...)} instead, will be removed in WorldGuard 8
     */
    @Deprecated(forRemoval = true)
    public boolean testBuild(Location location, LocalPlayer subject, StateFlag... flags) {
        return getApplicableRegions(location).testBuild(subject, flags);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#testBuild(RegionAssociable, StateFlag...)
     * .testBuild(RegionAssociable, StateFlag...)}
     */
    public boolean testBuild(Location location, RegionAssociable subject, StateFlag... flags) {
        return getApplicableRegions(location).testBuild(subject, flags);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#testBuild(RegionAssociable, MapFlag, Object, StateFlag, StateFlag...)
     * .testBuild(RegionAssociable, MapFlag, Object, StateFlag, StateFlag...)}
     */
    public <K> boolean testBuild(Location location, RegionAssociable subject, MapFlag<K, State> flag, K key,
                                 @Nullable StateFlag fallback, StateFlag... flags) {
        return getApplicableRegions(location).testBuild(subject, flag, key, fallback, flags);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#testState(RegionAssociable, StateFlag...)
     * .testState(RegionAssociable, StateFlag...)}
     *
     * @deprecated use {@link #testState(Location, RegionAssociable, StateFlag...)} instead, will be removed in WorldGuard 8
     */
    @Deprecated(forRemoval = true)
    public boolean testState(Location location, @Nullable LocalPlayer subject, StateFlag... flags) {
        return getApplicableRegions(location).testState(subject, flags);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#testState(RegionAssociable, StateFlag...)
     * .testState(RegionAssociable, StateFlag...)}
     */
    public boolean testState(Location location, @Nullable RegionAssociable subject, StateFlag... flags) {
        return getApplicableRegions(location).testState(subject, flags);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryState(RegionAssociable, StateFlag...)
     * .queryState(RegionAssociable, StateFlag...)}
     *
     * @deprecated use {@link #queryState(Location, RegionAssociable, StateFlag...)} instead, will be removed in WorldGuard 8
     */
    @Deprecated(forRemoval = true)
    @Nullable
    public State queryState(Location location, @Nullable LocalPlayer subject, StateFlag... flags) {
        return getApplicableRegions(location).queryState(subject, flags);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryState(RegionAssociable, StateFlag...)
     * .queryState(RegionAssociable, StateFlag...)}
     */
    @Nullable
    public State queryState(Location location, @Nullable RegionAssociable subject, StateFlag... flags) {
        return getApplicableRegions(location).queryState(subject, flags);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryValue(RegionAssociable, Flag)
     * .queryValue(RegionAssociable, Flag)}
     *
     * @deprecated use {@link #queryValue(Location, RegionAssociable, Flag)} instead, will be removed in WorldGuard 8
     */
    @Deprecated(forRemoval = true)
    @Nullable
    public <V> V queryValue(Location location, @Nullable LocalPlayer subject, Flag<V> flag) {
        return getApplicableRegions(location).queryValue(subject, flag);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryValue(RegionAssociable, Flag)
     * .queryValue(RegionAssociable, Flag)}
     */
    @Nullable
    public <V> V queryValue(Location location, @Nullable RegionAssociable subject, Flag<V> flag) {
        return getApplicableRegions(location).queryValue(subject, flag);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryMapValue(RegionAssociable, MapFlag, Object)
     * .queryMapValue(RegionAssociable, MapFlag, Object)}
     */
    @Nullable
    public <V, K> V queryMapValue(Location location, @Nullable RegionAssociable subject, MapFlag<K, V> flag, K key) {
        return getApplicableRegions(location).queryMapValue(subject, flag, key);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryMapValue(RegionAssociable, MapFlag, Object, Flag)
     * .queryMapValue(RegionAssociable, MapFlag, Object, Flag)}
     */
    @Nullable
    public <V, K> V queryMapValue(Location location, @Nullable RegionAssociable subject, MapFlag<K, V> flag, K key, @Nullable Flag<V> fallback) {
        return getApplicableRegions(location).queryMapValue(subject, flag, key, fallback);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryAllValues(RegionAssociable, Flag)
     * .queryAllValues(RegionAssociable, Flag)}
     *
     * @deprecated use {@link #queryAllValues(Location, RegionAssociable, Flag)} instead, will be removed in WorldGuard 8
     */
    @Deprecated(forRemoval = true)
    public <V> Collection<V> queryAllValues(Location location, @Nullable LocalPlayer subject, Flag<V> flag) {
        return getApplicableRegions(location).queryAllValues(subject, flag);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryAllValues(RegionAssociable, Flag)
     * .queryAllValues(RegionAssociable, Flag)}
     */
    public <V> Collection<V> queryAllValues(Location location, @Nullable RegionAssociable subject, Flag<V> flag) {
        return getApplicableRegions(location).queryAllValues(subject, flag);
    }

    /**
     * Convenience method for
     * {@link #getApplicableRegions(Location)}{@link ApplicableRegionSet#queryAllValues(RegionAssociable, Flag, boolean)
     * .queryAllValues(RegionAssociable, Flag, boolean)}
     */
    public <V> Collection<V> queryAllValues(Location location, @Nullable RegionAssociable subject, Flag<V> flag, boolean acceptOne) {
        return getApplicableRegions(location).queryAllValues(subject, flag, acceptOne);
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
