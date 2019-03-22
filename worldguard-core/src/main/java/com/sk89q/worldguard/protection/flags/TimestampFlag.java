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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Stores a timestamp.
 */
public class TimestampFlag extends Flag<Date> {
    private static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");

    protected TimestampFlag(String name, @Nullable RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    protected TimestampFlag(String name) {
        super(name);
    }

    @Override
    public Date parseInput(FlagContext context) throws InvalidFlagFormat {
        String input = context.getUserInput();
        if("now".equalsIgnoreCase(input)) {
            return new Date();
        } else if("none".equalsIgnoreCase(input)) {
            return null;
        } else {
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
            try {
                return format.parse(input);
            } catch (ParseException ignored) {
                String pattern = format instanceof SimpleDateFormat ?
                        ((SimpleDateFormat) format).toLocalizedPattern() :
                        format.format(new Date());
                throw new InvalidFlagFormat("Expected input in format: " + pattern);
            }
        }
    }

    @Override
    public Date unmarshal(@Nullable Object o) {
        if (o instanceof String) {
            try {
                return DEFAULT_FORMAT.parse((String) o);
            } catch (ParseException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Object marshal(Date o) {
        return DEFAULT_FORMAT.format(o);
    }
}
