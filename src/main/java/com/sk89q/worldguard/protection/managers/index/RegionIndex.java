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

package com.sk89q.worldguard.protection.managers.index;

import com.google.common.base.Predicate;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldguard.protection.managers.RegionDifference;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.ChangeTracked;
import com.sk89q.worldguard.util.Normal;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * An index of regions to allow for fast lookups of regions by their ID and
 * through spatial queries.
 *
 * <p>Indexes may be thread-unsafe.</p>
 */
public interface RegionIndex extends ChangeTracked {

    /**
     * Bias the given chunk for faster lookups (put it in a hash table, etc.).
     *
     * <p>Implementations may choose to do nothing.</p>
     *
     * @param chunkPosition the chunk position
     */
    void bias(Vector2D chunkPosition);

    /**
     * Bias the given chunk for faster lookups (put it in a hash table, etc.).
     *
     * <p>Implementations may choose to do nothing.</p>
     *
     * @param chunkPosition the chunk position
     */
    void biasAll(Collection<Vector2D> chunkPosition);

    /**
     * No longer bias the given chunk for faster lookup.
     *
     * @param chunkPosition the chunk position
     */
    void forget(Vector2D chunkPosition);

    /**
     * Clearly all extra cache data created by any calls to
     * {@link #bias(Vector2D)}.
     */
    void forgetAll();

    /**
     * Add a region to this index, replacing any existing one with the same
     * name (equality determined using {@link Normal}).
     *
     * <p>The parents of the region will also be added to the index.</p>
     *
     * @param region the region
     */
    void add(ProtectedRegion region);

    /**
     * Add a list of regions to this index, replacing any existing one
     * with the same name (equality determined using {@link Normal}).
     *
     * <p>The parents of the region will also be added to the index.</p>
     *
     * @param regions a collections of regions
     */
    void addAll(Collection<ProtectedRegion> regions);

    /**
     * Remove a region from the index with the given name.
     *
     * @param id the name of the region
     * @param strategy what to do with children
     * @return a list of removed regions where the first entry is the region specified by {@code id}
     */
    Set<ProtectedRegion> remove(String id, RemovalStrategy strategy);

    /**
     * Test whether the index contains a region named by the given name
     * (equality determined using {@link Normal}).
     *
     * @param id the name of the region
     * @return true if the index contains the region
     */
    boolean contains(String id);

    /**
     * Get the region named by the given name (equality determined using
     * {@link Normal}).
     *
     * @param id the name of the region
     * @return a region or {@code null}
     */
    @Nullable
    ProtectedRegion get(String id);

    /**
     * Apply the given predicate to all the regions in the index
     * until there are no more regions or the predicate returns false.
     *
     * @param consumer a predicate that returns true to continue iterating
     */
    void apply(Predicate<ProtectedRegion> consumer);

    /**
     * Apply the given predicate to all regions that contain the given
     * position until there are no more regions or the predicate returns false.
     *
     * @param position the position
     * @param consumer a predicate that returns true to continue iterating
     */
    void applyContaining(Vector position, Predicate<ProtectedRegion> consumer);

    /**
     * Apply the given predicate to all regions that intersect the given
     * region until there are no more regions or the predicate returns false.
     *
     * @param region the intersecting region
     * @param consumer a predicate that returns true to continue iterating
     */
    void applyIntersecting(ProtectedRegion region, Predicate<ProtectedRegion> consumer);

    /**
     * Return the number of regions in the index.
     *
     * @return the number of regions
     */
    int size();

    /**
     * Get the list of changed or removed regions since last call and
     * clear those lists.
     *
     * @return the difference
     */
    RegionDifference getAndClearDifference();

    /**
     * Set the index to be dirty using the given difference.
     *
     * @param difference the difference
     */
    void setDirty(RegionDifference difference);

    /**
     * Get an unmodifiable collection of regions stored in this index.
     *
     * @return a collection of regions
     */
    Collection<ProtectedRegion> values();

}
