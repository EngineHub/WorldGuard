package com.sk89q.worldguard.protection.flags;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stores a key value map of typed {@link Flag}s.
 */
public class MapFlag<K, V> extends Flag<Map<K, V>> {

    private final Flag<K> keyFlag;
    private final Flag<V> valueFlag;

    public MapFlag(final String name, final Flag<K> keyFlag, final Flag<V> valueFlag) {
        super(name);
        this.keyFlag = keyFlag;
        this.valueFlag = valueFlag;
    }

    public MapFlag(final String name, @Nullable final RegionGroup defaultGroup, final Flag<K> keyFlag, final Flag<V> valueFlag) {
        super(name, defaultGroup);
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
    public Map<K, V> parseInput(final FlagContext context) throws InvalidFlagFormat {

        final String input = context.getUserInput();
        if (input.isEmpty()) {
            return Maps.newHashMap();
        }

        final Map<K, V> items = Maps.newHashMap();
        for (final String str : input.split(",")) {

            final char split = str.indexOf('=') == -1 ? ':' : '=';
            final String[] keyVal = StringUtils.split(str, split);
            if (keyVal.length != 2) {
                throw new InvalidFlagFormat("Input must be in a 'key:value,key1=value1' format. Either ':' or '=' can be used.");
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
