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

import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.Registry;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RegistryFlag<T extends Keyed> extends Flag<T> {
    private final Registry<T> registry;

    public RegistryFlag(String name, Registry<T> registry) {
        super(name);
        requireNonNull(registry, "registry cannot be null.");
        this.registry = registry;
    }

    public RegistryFlag(String name, @Nullable RegionGroup defaultGroup, Registry<T> registry) {
        super(name, defaultGroup);
        requireNonNull(registry, "registry cannot be null.");
        this.registry = registry;
    }

    @Override
    public T parseInput(FlagContext context) throws InvalidFlagFormatException {
        final String key = context.getUserInput().trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(registry.get(key))
                .orElseThrow(() -> new InvalidFlagFormatException("Unknown " + registry.getName() + ": " + key));
    }

    public Registry<T> getRegistry() {
        return registry;
    }

    @Override
    public T unmarshal(@Nullable Object o) {
        return registry.get(String.valueOf(o).toLowerCase(Locale.ROOT));
    }

    @Override
    public Object marshal(T o) {
        return o.id();
    }
}
