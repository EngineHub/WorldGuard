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
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.MapFlag;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Default implementation of {@link FlagQuery}, returning flag default values.
 */
public interface DefaultFlagQuery extends FlagQuery {

    @Override
    @Nullable
    default <V, K> V queryMapValue(@Nullable RegionAssociable subject, MapFlag<K, V> flag, K key, @Nullable Flag<V> fallback) {
        Map<K, V> defaultVal = flag.getDefault();
        return defaultVal != null ? defaultVal.get(key) : fallback != null ? fallback.getDefault() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    default <V> Collection<V> queryAllValues(@Nullable RegionAssociable subject, Flag<V> flag, boolean acceptOne) {
        V fallback = flag.getDefault();
        return fallback != null ? ImmutableList.of(fallback) : (Collection<V>) ImmutableList.of();
    }

}
