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

package com.sk89q.worldguard.bukkit.event.debug;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Logs attempts at cancellation.
 */
public class CancelLogger {

    private List<CancelAttempt> entries = new ArrayList<CancelAttempt>();

    /**
     * Log a call.
     *
     * @param before The cancellation flag before the call
     * @param after The cancellation flag after the call
     * @param stackTrace The stack trace
     */
    public void log(boolean before, boolean after, StackTraceElement[] stackTrace) {
        entries.add(new CancelAttempt(before, after, stackTrace));
    }

    /**
     * Get an immutable list of cancels.
     *
     * @return An immutable list
     */
    public List<CancelAttempt> getCancels() {
        return ImmutableList.copyOf(entries);
    }

}
