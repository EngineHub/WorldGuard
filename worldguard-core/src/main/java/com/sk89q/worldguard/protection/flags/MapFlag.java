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

package com.sk89q.worldguard.protection.flags;

import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;

/**
 * Stores a key value map of typed {@link Flag}s.
 */
public class MapFlag<K, V> extends Flag<Map<K, V>> {

    private final Flag<K> keyFlag;
    private final Flag<V> valueFlag;

    public MapFlag(final String name, final Flag<K> keyFlag, final Flag<V> valueFlag) {
        super(name);
        requireNonNull(keyFlag, "keyFlag cannot be null.");
        requireNonNull(valueFlag, "valueFlag cannot be null.");
        this.keyFlag = keyFlag;
        this.valueFlag = valueFlag;
    }

    public MapFlag(final String name, @Nullable final RegionGroup defaultGroup, final Flag<K> keyFlag, final Flag<V> valueFlag) {
        super(name, defaultGroup);
        requireNonNull(keyFlag, "keyFlag cannot be null.");
        requireNonNull(valueFlag, "valueFlag cannot be null.");
        this.keyFlag = keyFlag;
        this.valueFlag = valueFlag;
    }

    /**
     * Get the flag that is stored as the key flag type.
     *
     * @return The key flag type.
     */
    public Flag<K> getKeyFlag() {
        return this.keyFlag;
    }

    /**
     * Get the flag type that is stored as values.
     *
     * @return The value flag type.
     */
    public Flag<V> getValueFlag() {
        return this.valueFlag;
    }

    @Override
    public Map<K, V> parseInput(final FlagContext context) throws InvalidFlagFormatException {

        final String input = context.getUserInput();
        if (input.isEmpty()) {
            return Maps.newHashMap();
        }

        final Map<K, V> items = Maps.newHashMap();
        for (final String str : input.split(",")) {

            final char split = str.indexOf('=') == -1 ? ':' : '=';
            final String[] keyVal = str.split(String.valueOf(split));
            if (keyVal.length != 2) {
                throw new InvalidFlagFormatException("Input must be in a 'key:value,key1=value1' format. Either ':' or '=' can be used.");
            }

            final FlagContext key = context.copyWith(null, keyVal[0], null);
            final FlagContext value = context.copyWith(null, keyVal[1], null);
            items.put(this.keyFlag.parseInput(key), this.valueFlag.parseInput(value));
        }

        return items;
    }

    @Override
    public Map<K, V> unmarshal(@Nullable final Object o) {

        if (o instanceof Map<?, ?>) {

            final Map<?, ?> map = (Map<?, ?>) o;
            final Map<K, V> items = Maps.newHashMap();
            for (final Entry<?, ?> entry : map.entrySet()) {

                final K keyItem = this.keyFlag.unmarshal(entry.getKey());
                final V valueItem = this.valueFlag.unmarshal(entry.getValue());
                if (keyItem != null && valueItem != null) {
                    items.put(keyItem, valueItem);
                }
            }

            return items;
        }

        return null;
    }

    @Override
    public Object marshal(final Map<K, V> o) {

        final Map<Object, Object> map = Maps.newHashMap();
        for (final Entry<K, V> entry : o.entrySet()) {
            map.put(this.keyFlag.marshal(entry.getKey()), this.valueFlag.marshal(entry.getValue()));
        }

        return map;
    }
}
