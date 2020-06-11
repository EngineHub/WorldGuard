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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;

/**
 * Stores an Number.
 */
public abstract class NumberFlag<T extends Number> extends Flag<T> {

    private static final Number[] EMPTY_NUMBER_ARRAY = new Number[0];
    private Number[] suggestions = EMPTY_NUMBER_ARRAY;

    protected NumberFlag(String name, RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    protected NumberFlag(String name) {
        super(name);
    }

    /**
     * Not recommended for public use. Will likely be moved when migrating to piston for commands.
     * @param values suggested values
     */
    @Beta
    public void setSuggestedValues(Number[] values) {
        this.suggestions = checkNotNull(values);
    }

    /**
     * Not recommended for public use. Will likely be moved when migrating to piston for commands.
     * @return suggested values
     */
    @Beta
    public Number[] getSuggestedValues() {
        return suggestions;
    }
}
