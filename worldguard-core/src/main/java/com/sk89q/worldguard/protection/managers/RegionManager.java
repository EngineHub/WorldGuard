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

package com.sk89q.worldguard.protection.managers;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.RegionResultSet;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.index.ConcurrentRegionIndex;
import com.sk89q.worldguard.protection.managers.index.RegionIndex;
import com.sk89q.worldguard.protection.managers.storage.DifferenceSaveException;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery.QueryOption;
import com.sk89q.worldguard.util.Normal;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A region manager holds the regions for a world.
 */
public final class RegionManager {

    private final RegionDatabase store;
    private final Function<String, ? extends ConcurrentRegionIndex> indexFactory;
    private final FlagRegistry flagRegistry;
    private ConcurrentRegionIndex index;

    /**
     * Create a new index.
     *
     * @param store the region store
     * @param indexFactory the factory for creating new instances of the index
     * @param flagRegistry the flag registry
     */
    public RegionManager(RegionDatabase store, Function<String, ? extends ConcurrentRegionIndex> indexFactory, FlagRegistry flagRegistry) {
        checkNotNull(store);
        checkNotNull(indexFactory);
        checkNotNull(flagRegistry, "flagRegistry");

        this.store = store;
        this.indexFactory = indexFactory;
        this.index = indexFactory.apply(store.getName());
        this.flagRegistry = flagRegistry;
    }

    /**
     * Get a displayable name for this store.
     */
    public String getName() {
        return store.getName();
    }

    /**
     * Load regions from storage and replace the index on this manager with
     * the regions loaded from the store.
     *
     * <p>This method will block until the save completes, but it will
     * not block access to the region data from other threads, nor will it
     * prevent the creation or modification of regions in the index while
     * a new collection of regions is loaded from storage.</p>
     *
     * @throws StorageException thrown when loading fails
     */
    public void load() throws StorageException {
        Set<ProtectedRegion> regions = store.loadAll(flagRegistry);
        for (ProtectedRegion region : regions) {
            region.setDirty(false);
        }
        setRegions(regions);
    }

    /**
     * Save a snapshot of all the regions as it is right now to storage.
     *
     * @throws StorageException thrown on save error
     */
    public void save() throws StorageException {
        index.setDirty(false);
        store.saveAll(new HashSet<>(getFilteredValuesCopy()));
    }

    /**
     * Save changes to the region index to disk, preferring to only save
     * the changes (rather than the whole index), but choosing to save the
     * whole index if the underlying store does not support partial saves.
     *
     * <p>This method does nothing if there are no changes.</p>
     *
     * @return true if there were changes to be saved
     * @throws StorageException thrown on save error
     */
    public boolean saveChanges() throws StorageException {
        RegionDifference diff = index.getAndClearDifference();
        boolean successful = false;

        try {
            if (diff.containsChanges()) {
                try {
                    store.saveChanges(diff);
                } catch (DifferenceSaveException e) {
                    save(); // Partial save is not supported
                }
                successful = true;
                return true;
            } else {
                successful = true;
                return false;
            }
        } finally {
            if (!successful) {
                index.setDirty(diff);
            }
        }
    }

    /**
     * Load the regions for a chunk.
     *
     * @param position the position
     */
    public void loadChunk(BlockVector2 position) {
        index.bias(position);
    }

    /**
     * Load the regions for a chunk.
     *
     * @param positions a collection of positions
     */
    public void loadChunks(Collection<BlockVector2> positions) {
        index.biasAll(positions);
    }

    /**
     * Unload the regions for a chunk.
     *
     * @param position the position
     */
    public void unloadChunk(BlockVector2 position) {
        index.forget(position);
    }

    /**
     * Get an unmodifiable map of regions containing the state of the
     * index at the time of call.
     *
     * <p>This call is relatively heavy (and may block other threads),
     * so refrain from calling it frequently.</p>
     *
     * @return a map of regions
     */
    public Map<String, ProtectedRegion> getRegions() {
        Map<String, ProtectedRegion> map = new HashMap<>();
        for (ProtectedRegion region : index.values()) {
            map.put(Normal.normalize(region.getId()), region);
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Replace the index with the regions in the given map.
     *
     * <p>The parents of the regions will also be added to the index, even
     * if they are not in the provided map.</p>
     *
     * @param regions a map of regions
     */
    public void setRegions(Map<String, ProtectedRegion> regions) {
        checkNotNull(regions);

        setRegions(regions.values());
    }

    /**
     * Replace the index with the regions in the given collection.
     *
     * <p>The parents of the regions will also be added to the index, even
     * if they are not in the provided map.</p>
     *
     * @param regions a collection of regions
     */
    public void setRegions(Collection<ProtectedRegion> regions) {
        checkNotNull(regions);

        ConcurrentRegionIndex newIndex = indexFactory.apply(getName());
        newIndex.addAll(regions);
        newIndex.getAndClearDifference(); // Clear changes
        this.index = newIndex;
    }

    /**
     * Aad a region to the manager.
     *
     * <p>The parents of the region will also be added to the index.</p>
     *
     * @param region the region
     */
    public void addRegion(ProtectedRegion region) {
        checkNotNull(region);
        index.add(region);
    }

    /**
     * Return whether the index contains a region by the given name,
     * with equality determined by {@link Normal}.
     *
     * @param id the name of the region
     * @return true if this index contains the region
     */
    public boolean hasRegion(String id) {
        return index.contains(id);
    }

    /**
     * Get the region named by the given name (equality determined using
     * {@link Normal}).
     *
     * @param id the name of the region
     * @return a region or {@code null}
     */
    @Nullable
    public ProtectedRegion getRegion(String id) {
        checkNotNull(id);
        return index.get(id);
    }

    /**
     * @deprecated Use exact ids with {@link #getRegion}
     */
    @Nullable
    @Deprecated
    public ProtectedRegion matchRegion(String pattern) {
        return getRegion(pattern);
    }

    /**
     * Remove a region from the index with the given name, opting to remove
     * the children of the removed region.
     *
     * @param id the name of the region
     * @return a list of removed regions where the first entry is the region specified by {@code id}
     */
    @Nullable
    public Set<ProtectedRegion> removeRegion(String id) {
        return removeRegion(id, RemovalStrategy.REMOVE_CHILDREN);
    }

    /**
     * Remove a region from the index with the given name.
     *
     * @param id the name of the region
     * @param strategy what to do with children
     * @return a list of removed regions where the first entry is the region specified by {@code id}
     */
    @Nullable
    public Set<ProtectedRegion> removeRegion(String id, RemovalStrategy strategy) {
        return index.remove(id, strategy);
    }

    /**
     * Query for effective flags and members for the given position.
     *
     * <p>{@link QueryOption#COMPUTE_PARENTS} is used.</p>
     *
     * @param position the position
     * @return the query object
     */
    public ApplicableRegionSet getApplicableRegions(BlockVector3 position) {
        return getApplicableRegions(position, QueryOption.COMPUTE_PARENTS);
    }

    /**
     * Return a region set for the given position.
     *
     * @param position the position
     * @param option the option
     * @return a region set
     */
    public ApplicableRegionSet getApplicableRegions(BlockVector3 position, QueryOption option) {
        checkNotNull(position);
        checkNotNull(option);

        Set<ProtectedRegion> regions = Sets.newHashSet();
        index.applyContaining(position, option.createIndexConsumer(regions));
        return new RegionResultSet(option.constructResult(regions), index.get("__global__"), true);
    }

    /**
     * Query for effective flags and members for the area represented
     * by the given region.
     *
     * <p>{@link QueryOption#COMPUTE_PARENTS} is used.</p>
     *
     * @param region the region
     * @return the query object
     */
    public ApplicableRegionSet getApplicableRegions(ProtectedRegion region) {
        return getApplicableRegions(region, QueryOption.COMPUTE_PARENTS);
    }

    /**
     * Return a region set for the area represented by the given region.
     *
     * @param region the region
     * @param option the option
     * @return a region set
     */
    public ApplicableRegionSet getApplicableRegions(ProtectedRegion region, QueryOption option) {
        checkNotNull(region);
        checkNotNull(option);

        Set<ProtectedRegion> regions = Sets.newHashSet();
        index.applyIntersecting(region, option.createIndexConsumer(regions));
        return new RegionResultSet(option.constructResult(regions), index.get("__global__"), true);
    }

    /**
     * Get a list of region names for regions that contain the given position.
     *
     * @param position the position
     * @return a list of names
     */
    public List<String> getApplicableRegionsIDs(BlockVector3 position) {
        checkNotNull(position);

        final List<String> names = new ArrayList<>();

        index.applyContaining(position, region -> names.add(region.getId()));

        return names;
    }

    /**
     * Return whether there are any regions intersecting the given region that
     * are not owned by the given player.
     *
     * @param region the region
     * @param player the player
     * @return true if there are such intersecting regions
     */
    public boolean overlapsUnownedRegion(ProtectedRegion region, final LocalPlayer player) {
        checkNotNull(region);
        checkNotNull(player);

        RegionIndex index = this.index;

        final AtomicBoolean overlapsUnowned = new AtomicBoolean();

        index.applyIntersecting(region, test -> {
            if (!test.getOwners().contains(player)) {
                overlapsUnowned.set(true);
                return false;
            } else {
                return true;
            }
        });

        return overlapsUnowned.get();
    }

    /**
     * Get the number of regions.
     *
     * @return the number of regions
     */
    public int size() {
        return index.size();
    }

    /**
     * Get the number of regions that are owned by the given player.
     *
     * @param player the player
     * @return name number of regions that a player owns
     */
    public int getRegionCountOfPlayer(final LocalPlayer player) {
        checkNotNull(player);

        final AtomicInteger count = new AtomicInteger();

        index.apply(test -> {
            if (test.getOwners().contains(player)) {
                count.incrementAndGet();
            }
            return true;
        });

        return count.get();
    }

    /**
     * Get an {@link ArrayList} copy of regions in the index with transient regions filtered.
     *
     * @return a list
     */
    private List<ProtectedRegion> getFilteredValuesCopy() {
        List<ProtectedRegion> filteredValues = new ArrayList<>();
        for (ProtectedRegion region : index.values()) {
            if (!region.isTransient()) {
                filteredValues.add(region);
            }
        }
        return filteredValues;
    }

}
