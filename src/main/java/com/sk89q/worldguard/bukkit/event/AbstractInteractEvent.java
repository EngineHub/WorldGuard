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

package com.sk89q.worldguard.bukkit.event;

import com.sk89q.worldguard.bukkit.cause.Cause;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractInteractEvent extends Event implements Cancellable {

    private final Event originalEvent;
    private final Cause cause;
    private boolean cancelled;

    /**
     * Create a new instance
     *
     * @param originalEvent the original event
     * @param cause the cause
     */
    protected AbstractInteractEvent(Event originalEvent, Cause cause) {
        checkNotNull(originalEvent);
        checkNotNull(cause);
        this.originalEvent = originalEvent;
        this.cause = cause;
    }

    /**
     * Get the original event.
     *
     * @return the original event
     */
    public Event getOriginalEvent() {
        return originalEvent;
    }

    /**
     * Return the cause.
     *
     * @return the cause
     */
    public Cause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
