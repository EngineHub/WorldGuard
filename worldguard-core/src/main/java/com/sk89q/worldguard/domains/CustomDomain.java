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

package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.registry.CustomDomainContext;
import com.sk89q.worldguard.domains.registry.InvalidDomainFormatException;
import com.sk89q.worldguard.util.ChangeTracked;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CustomDomain implements Domain, ChangeTracked {
    private static final Pattern VALID_NAME = Pattern.compile("^[a-z0-9\\-]{1,40}$");

    private final String name;
    private boolean dirty;

    public CustomDomain(String name) {
        if (name == null ||!isValidName(name)) {
            throw new IllegalArgumentException("Invalid Domain name used.");
        }
        this.name = name;
    }

    /**
     * Get the name of the domain resolver.
     *
     * @return The name of the domain
     */
    public String getName() {
        return name;
    }

    /**
     * Parse a given input to fill the context of the CustomDomain.
     *
     * @param context the {@link CustomDomainContext}
     * @throws InvalidDomainFormatException Raised if the input is invalid
     */
    public abstract void parseInput(CustomDomainContext context) throws InvalidDomainFormatException;

    /**
     * Convert a raw type that was loaded (from a YAML file, for example)
     * into the custom domain.
     *
     * @param o The object
     */
    public abstract void unmarshal(Object o);

    /**
     * Convert the current Domain to a storable foramt
     *
     * @return The marshalled type
     */
    public abstract Object marshal();

    /**
     * Test whether a flag name is valid.
     *
     * @param name The flag name
     * @return Whether the name is valid
     */
    public static boolean isValidName(String name) {
        checkNotNull(name, "name");
        // g is already reserved by the group domain
        return VALID_NAME.matcher(name).matches() && !name.equalsIgnoreCase("g");
    }


    @Override
    public boolean contains(LocalPlayer player) {
        return contains(player.getUniqueId());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
