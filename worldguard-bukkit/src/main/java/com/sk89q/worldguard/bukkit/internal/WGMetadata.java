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

package com.sk89q.worldguard.bukkit.internal;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Utility methods for dealing with metadata on entities.
 *
 * <p>WorldGuard is placed as the owner of all values.</p>
 */
public final class WGMetadata {

    private WGMetadata() {
    }

    /**
     * Add some metadata to a target.
     *
     * @param target the target
     * @param key the key
     * @param value the value
     */
    public static void put(Metadatable target, String key, Object value) {
        target.setMetadata(key, new FixedMetadataValue(WorldGuardPlugin.inst(), value));
    }

    /**
     * Get the (first) metadata value on the given target that has the given
     * key and is of the given class type.
     *
     * @param target the target
     * @param key the key
     * @param expected the type of the value
     * @param <T> the type of the value
     * @return a value, or {@code null} if one does not exists
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getIfPresent(Metadatable target, String key, Class<T> expected) {
        List<MetadataValue> values = target.getMetadata(key);
        WorldGuardPlugin owner = WorldGuardPlugin.inst();
        for (MetadataValue value : values) {
            if (value.getOwningPlugin() == owner) {
                Object v = value.value();
                if (expected.isInstance(v)) {
                    return (T) v;
                }
            }
        }

        return null;
    }

}
