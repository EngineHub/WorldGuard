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

package com.sk89q.worldguard.internal.event;

import com.sk89q.worldguard.internal.cause.Cause;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractInteractEvent extends Event implements Cancellable {

    private final Event originalEvent;
    private final List<? extends Cause<?>> causes;
    private final Action action;
    private boolean cancelled;

    /**
     * Create a new instance
     *
     * @param originalEvent the original event
     * @param causes a list of causes, where the originating causes are at the beginning
     * @param action the action that is being taken
     */
    protected AbstractInteractEvent(Event originalEvent, List<? extends Cause<?>> causes, Action action) {
        checkNotNull(originalEvent);
        checkNotNull(causes);
        checkNotNull(action);
        this.originalEvent = originalEvent;
        this.causes = causes;
        this.action = action;
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
     * Return an unmodifiable list of causes, where the originating causes are
     * at the beginning of the list.
     *
     * @return a list of causes
     */
    public List<? extends Cause<?>> getCauses() {
        return Collections.unmodifiableList(causes);
    }

    /**
     * Get the action that is being taken.
     *
     * @return the action
     */
    public Action getAction() {
        return action;
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
