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
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormatException;

import javax.annotation.Nullable;

public class UnknownFlag extends Flag<Object> {

    public UnknownFlag(String name) {
        super(name);
    }

    @Override
    public Object parseInput(FlagContext context) throws InvalidFlagFormatException {
        throw new InvalidFlagFormatException("The plugin that registered this flag is not currently installed");
    }

    @Override
    public Object unmarshal(@Nullable Object o) {
        return o;
    }

    @Override
    public Object marshal(Object o) {
        return o;
    }

}
