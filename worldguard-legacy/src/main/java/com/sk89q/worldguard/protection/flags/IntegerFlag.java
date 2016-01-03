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
 * Stores an integer.
 */
public class IntegerFlag extends Flag<Integer> {

    public IntegerFlag(String name, RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    public IntegerFlag(String name) {
        super(name);
    }

    @Override
    public Integer parseInput(WorldGuardPlugin plugin, CommandSender sender,String input) throws InvalidFlagFormat {
        input = input.trim();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Not a number: " + input);
        }
    }

    @Override
    public Integer unmarshal(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Number) {
            return ((Number) o).intValue();
        } else {
            return null;
        }
    }

    @Override
    public Object marshal(Integer o) {
        return o;
    }
}
