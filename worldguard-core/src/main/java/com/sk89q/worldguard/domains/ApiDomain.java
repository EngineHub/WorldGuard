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

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ApiDomain implements Domain {
    private static final Pattern VALID_NAME = Pattern.compile("^[:A-Za-z0-9\\-]{1,40}$");

    private String name;

    public ApiDomain(String name) {
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
        return VALID_NAME.matcher(name).matches();
    }


    @Override
    public boolean contains(LocalPlayer player) {
        return contains(player.getUniqueId());
    }
}
