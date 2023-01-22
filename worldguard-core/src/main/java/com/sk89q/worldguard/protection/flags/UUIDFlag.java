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

import javax.annotation.Nullable;
import java.util.UUID;

public class UUIDFlag extends Flag<UUID> {

    public UUIDFlag(String name, @Nullable RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    public UUIDFlag(String name) {
        super(name);
    }

    @Override
    public UUID parseInput(FlagContext context) throws InvalidFlagFormatException {
        String input = context.getUserInput();
        if ("self".equalsIgnoreCase(input)) {
            return context.getSender().getUniqueId();
        }
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            throw new InvalidFlagFormatException("Not a valid uuid: " + input);
        }
    }

    @Override
    public UUID unmarshal(@Nullable Object o) {
        if (!(o instanceof String)) {
            return null;
        }
        try {
            return UUID.fromString((String)o);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public Object marshal(UUID o) {
        return o.toString();
    }
}
