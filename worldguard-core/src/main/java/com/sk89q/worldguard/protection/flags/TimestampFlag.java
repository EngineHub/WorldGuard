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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Stores a timestamp.
 */
public class TimestampFlag extends Flag<Instant> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC);
    public TimestampFlag(String name, @Nullable RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    public TimestampFlag(String name) {
        super(name);
    }

    @Override
    public Instant parseInput(FlagContext context) throws InvalidFlagFormat {
        String input = context.getUserInput();
        if("now".equalsIgnoreCase(input)) {
            return Instant.now();
        } else {
            try {
                return Instant.from(FORMATTER.parse(input));
            } catch(DateTimeParseException ignored) {
                throw new InvalidFlagFormat("Expected 'now' or ISO 8601 formatted input.");
            }
        }
    }

    @Override
    public Instant unmarshal(@Nullable Object o) {
        if (o instanceof String) {
            try {
                return Instant.from(FORMATTER.parse((String) o));
            } catch(DateTimeParseException ignored) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Object marshal(Instant o) {
        return FORMATTER.format(o);
    }
}
