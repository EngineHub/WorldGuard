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

import org.bukkit.command.CommandSender;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Stores an enum value.
 */
public class EnumFlag<T extends Enum<T>> extends Flag<T> {

    private Class<T> enumClass;

    public EnumFlag(String name, Class<T> enumClass, RegionGroup defaultGroup) {
        super(name, defaultGroup);
        this.enumClass = enumClass;
    }

    public EnumFlag(String name, Class<T> enumClass) {
        super(name);
        this.enumClass = enumClass;
    }

    /**
     * Get the enum class.
     *
     * @return the enum class
     */
    public Class<T> getEnumClass() {
        return enumClass;
    }

    private T findValue(String input) throws IllegalArgumentException {
        if (input != null) {
            input = input.toUpperCase();
        }

        try {
            return Enum.valueOf(enumClass, input);
        } catch (IllegalArgumentException e) {
            T val = detectValue(input);

            if (val != null) {
                return val;
            }

            throw e;
        }
    }

    /**
     * Fuzzy detect the value if the value is not found.
     *
     * @param input string input
     * @return value or null
     */
    public T detectValue(String input) {
        return null;
    }

    @Override
    public T parseInput(WorldGuardPlugin plugin, CommandSender sender, String input) throws InvalidFlagFormat {
        try {
            return findValue(input);
        } catch (IllegalArgumentException e) {
            throw new InvalidFlagFormat("Unknown value '" + input + "' in "
                    + enumClass.getName());
        }
    }

    @Override
    public T unmarshal(Object o) {
        try {
            return Enum.valueOf(enumClass, String.valueOf(o));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public Object marshal(T o) {
        return o.name();
    }

}
