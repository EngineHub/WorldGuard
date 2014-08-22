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
import com.google.common.base.Supplier;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldguard.protection.managers.RegionDifference;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldguard.util.Normal.normalize;

/**
 * An index that stores regions in a hash map, which allows for fast lookup
 * by ID but O(n) performance for spatial queries.
 *
 * <p>This implementation supports concurrency to the extent that
 * a {@link ConcurrentMap} does.</p>
 */
public class HashMapIndex extends AbstractRegionIndex implements ConcurrentRegionIndex {

    private final ConcurrentMap<String, ProtectedRegion> regions = new ConcurrentHashMap<String, ProtectedRegion>();
    private Set<ProtectedRegion> removed = new HashSet<ProtectedRegion>();
    private final Object lock = new Object();

    /**
     * Called to rebuild the index after changes.
     */
    protected void rebuildIndex() {
        // Can be implemented by subclasses
    }

    /**
     * Perform the add operation.
     *
     * @param region the region
     */
    private void performAdd(ProtectedRegion region) {
        checkNotNull(region);
        
        region.setDirty(true);

        synchronized (lock) {
            String normalId = normalize(region.getId());

            ProtectedRegion existing = regions.get(normalId);

            // Casing / form of ID has changed
            if (existing != null && !existing.getId().equals(region.getId())) {
                removed.add(existing);
            }

            regions.put(normalId, region);

            removed.remove(region);

            ProtectedRegion parent = region.getParent();
            if (parent != null) {
                performAdd(parent);
            }
        }
    }

    @Override
    public void addAll(Collection<ProtectedRegion> regions) {
        checkNotNull(regions);
        
        synchronized (lock) {
            for (ProtectedRegion region : regions) {
                performAdd(region);
            }

            rebuildIndex();
        }
    }

    @Override
    public void bias(Vector2D chunkPosition) {
        // Nothing to do
    }

    @Override
    public void biasAll(Collection<Vector2D> chunkPositions) {
        // Nothing to do
    }

    @Override
    public void forget(Vector2D chunkPosition) {
        // Nothing to do
    }

    @Override
    public void forgetAll() {
        // Nothing to do
    }

    @Override
    public void add(ProtectedRegion region) {
        synchronized (lock) {
            performAdd(region);

            rebuildIndex();
        }
    }

    @Override
    public Set<ProtectedRegion> remove(String id, RemovalStrategy strategy) {
        checkNotNull(id);
        checkNotNull(strategy);

        Set<ProtectedRegion> removedSet = new HashSet<ProtectedRegion>();

        synchronized (lock) {
            ProtectedRegion removed = regions.remove(normalize(id));

            if (removed != null) {
                removedSet.add(removed);

                Iterator<ProtectedRegion> it = regions.values().iterator();

                // Handle children
                while (it.hasNext()) {
                    ProtectedRegion current = it.next();
                    ProtectedRegion parent = current.getParent();

                    if (parent != null && parent == removed) {
                        switch (strategy) {
                            case REMOVE_CHILDREN:
                                removedSet.add(current);
                                it.remove();
                                break;
                            case UNSET_PARENT_IN_CHILDREN:
                                current.clearParent();
                        }
                    }
                }
            }

            this.removed.addAll(removedSet);

            rebuildIndex();
        }

        return removedSet;
    }

    @Override
    public boolean contains(String id) {
        return regions.containsKey(normalize(id));
    }

    @Nullable
    @Override
    public ProtectedRegion get(String id) {
        return regions.get(normalize(id));
    }

    @Override
    public void apply(Predicate<ProtectedRegion> consumer) {
        for (ProtectedRegion region : regions.values()) {
            if (!consumer.apply(region)) {
                break;
            }
        }
    }

    @Override
    public void applyContaining(final Vector position, final Predicate<ProtectedRegion> consumer) {
        apply(new Predicate<ProtectedRegion>() {
            @Override
            public boolean apply(ProtectedRegion region) {
                return !region.contains(position) || consumer.apply(region);
            }
        });
    }

    @Override
    public void applyIntersecting(ProtectedRegion region, Predicate<ProtectedRegion> consumer) {
        for (ProtectedRegion found : region.getIntersectingRegions(regions.values())) {
            if (!consumer.apply(found)) {
                break;
            }
        }
    }

    @Override
    public int size() {
        return regions.size();
    }

    @Override
    public RegionDifference getAndClearDifference() {
        synchronized (lock) {
            Set<ProtectedRegion> changed = new HashSet<ProtectedRegion>();
            Set<ProtectedRegion> removed = this.removed;

            for (ProtectedRegion region : regions.values()) {
                if (region.isDirty()) {
                    changed.add(region);
                    region.setDirty(false);
                }
            }

            this.removed = new HashSet<ProtectedRegion>();

            return new RegionDifference(changed, removed);
        }
    }

    @Override
    public void setDirty(RegionDifference difference) {
        synchronized (lock) {
            for (ProtectedRegion changed : difference.getChanged()) {
                changed.setDirty(true);
            }
            removed.addAll(difference.getRemoved());
        }
    }

    @Override
    public Collection<ProtectedRegion> values() {
        return Collections.unmodifiableCollection(regions.values());
    }

    @Override
    public boolean isDirty() {
        synchronized (lock) {
            if (!removed.isEmpty()) {
                return true;
            }

            for (ProtectedRegion region : regions.values()) {
                if (region.isDirty()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void setDirty(boolean dirty) {
        synchronized (lock) {
            if (!dirty) {
                removed.clear();
            }

            for (ProtectedRegion region : regions.values()) {
                region.setDirty(dirty);
            }
        }
    }

    /**
     * A factory for new instances using this index.
     */
    public static final class Factory implements Supplier<HashMapIndex> {
        @Override
        public HashMapIndex get() {
            return new HashMapIndex();
        }
    }

}
