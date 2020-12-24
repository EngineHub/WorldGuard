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

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FlagUtil {

    private static final Logger log = Logger.getLogger(FlagUtil.class.getCanonicalName());

    private FlagUtil() {
    }

    /**
     * Marshal a value of flag values into a map of raw values.
     *
     * @param values The unmarshalled flag values map
     * @return The raw values map
     */
    public static Map<String, Object> marshal(Map<Flag<?>, Object> values) {
        checkNotNull(values, "values");

        Map<String, Object> rawValues = Maps.newHashMap();
        for (Entry<Flag<?>, Object> entry : values.entrySet()) {
            try {
                rawValues.put(entry.getKey().getName(), marshal(entry.getKey(), entry.getValue()));
            } catch (Throwable e) {
                log.log(Level.WARNING, "Не удалось упаковать значение флага для " + entry.getKey() + "; значение " + entry.getValue(), e);
            }
        }

        return rawValues;
    }

    @SuppressWarnings("unchecked")
    private static <T> Object marshal(Flag<T> flag, Object value) {
        return flag.marshal((T) value);
    }

}
