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

import org.bukkit.event.Cancellable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents call to {@link Cancellable#setCancelled(boolean)}.
 */
public class CancelAttempt {

    private final boolean before;
    private final boolean after;
    private final StackTraceElement[] stackTrace;

    /**
     * Create a new instance.
     *
     * @param before The cancellation flag before the call
     * @param after The cancellation flag after the call
     * @param stackTrace The stack trace
     */
    public CancelAttempt(boolean before, boolean after, StackTraceElement[] stackTrace) {
        checkNotNull(stackTrace, "stackTrace");
        this.before = before;
        this.after = after;
        this.stackTrace = stackTrace;
    }

    /**
     * Get the cancellation state before the call.
     *
     * @return Whether the event was cancelled before
     */
    public boolean getBefore() {
        return before;
    }

    /**
     * Get the cancellation state after the call.
     *
     * @return The new cancellation state
     */
    public boolean getAfter() {
        return after;
    }

    /**
     * Get the stack trace.
     *
     * @return The stack trace
     */
    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

}
