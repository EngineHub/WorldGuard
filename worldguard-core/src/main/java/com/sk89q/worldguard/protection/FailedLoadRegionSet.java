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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.MapFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A region set that is to be used when region data has failed. Operations
 * are blocked.
 */
public class FailedLoadRegionSet extends AbstractRegionSet {

    private static final FailedLoadRegionSet INSTANCE = new FailedLoadRegionSet();

    private final String denyMessage = "Данные региона для WorldGuard не удалось загрузить для этого мира, так как " +
            "все было защищено в качестве меры предосторожности. Пожалуйста, сообщите администратору.";
    private final Collection<String> denyMessageCollection = ImmutableList.of(denyMessage);

    private FailedLoadRegionSet() {
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <V> V queryValue(@Nullable RegionAssociable subject, Flag<V> flag) {
        if (flag == Flags.BUILD) {
            return (V) State.DENY;
        } else if (flag == Flags.DENY_MESSAGE) {
            return (V) denyMessage;
        }
        return flag.getDefault();
    }

    @Nullable
    @Override
    public <V, K> V queryMapValue(@Nullable RegionAssociable subject, MapFlag<K, V> flag, K key) {
        return queryMapValue(subject, flag, key, null);
    }

    @Nullable
    @Override
    public <V, K> V queryMapValue(@Nullable RegionAssociable subject, MapFlag<K, V> flag, K key, @Nullable Flag<V> fallback) {
        Map<K, V> defaultVal = flag.getDefault();
        return defaultVal != null ? defaultVal.get(key) : fallback != null ? fallback.getDefault() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag) {
        if (flag == Flags.BUILD) {
            return (Collection<V>) ImmutableList.of(State.DENY);
        } else if (flag == Flags.DENY_MESSAGE) {
            return (Collection<V>) denyMessageCollection;
        }
        V fallback = flag.getDefault();
        return fallback != null ? ImmutableList.of(fallback) : (Collection<V>) ImmutableList.of();
    }

    @Override
    public boolean isOwnerOfAll(LocalPlayer player) {
        return false;
    }

    @Override
    public boolean isMemberOfAll(LocalPlayer player) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<ProtectedRegion> getRegions() {
        return Collections.emptySet();
    }

    @Override
    public Iterator<ProtectedRegion> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Get an instance.
     *
     * @return an instance
     */
    public static FailedLoadRegionSet getInstance() {
        return INSTANCE;
    }
    
}
