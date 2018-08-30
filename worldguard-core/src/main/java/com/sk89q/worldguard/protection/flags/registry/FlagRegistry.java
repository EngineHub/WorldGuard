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

package com.sk89q.worldguard.protection.flags.registry;

import com.sk89q.worldguard.protection.flags.Flag;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of registered flags.
 */
public interface FlagRegistry extends Iterable<Flag<?>> {

    /**
     * Register a new flag.
     *
     * <p>There may be an appropriate time to register flags. If flags are
     * registered outside this time, then an exception may be thrown.</p>
     *
     * @param flag The flag
     * @throws FlagConflictException Thrown when an existing flag exists with the same name
     * @throws IllegalStateException If it is not the right time to register new flags
     */
    void register(Flag<?> flag) throws FlagConflictException;

    /**
     * Register a collection of flags.
     *
     * <p>There may be an appropriate time to register flags. If flags are
     * registered outside this time, then an exception may be thrown.</p>
     *
     * <p>If there is a flag conflict, then an error will be logged but
     * no exception will be thrown.</p>
     *
     * @param flags a collection of flags
     * @throws IllegalStateException If it is not the right time to register new flags
     */
    void registerAll(Collection<Flag<?>> flags);

    /**
     * Get af flag by its name.
     *
     * @param name The name
     * @return The flag, if it has been registered
     */
    @Nullable
    Flag<?> get(String name);

    /**
     * Get all flags
     *
     * @return All flags
     */
    List<Flag<?>> getAll();

    /**
     * Unmarshal a raw map of values into a map of flags with their
     * unmarshalled values.
     *
     * @param rawValues The raw values map
     * @param createUnknown Whether "just in time" flags should be created for unknown flags
     * @return The unmarshalled flag values map
     */
    Map<Flag<?>, Object> unmarshal(Map<String, Object> rawValues, boolean createUnknown);

    /**
     * Get the number of registered flags.
     *
     * @return The number of registered flags
     */
    int size();

}
