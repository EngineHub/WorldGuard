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

import org.bukkit.event.Event.Result;

/**
 * Utility methods for dealing with delegate events.
 */
public final class DelegateEvents {

    private DelegateEvents() {
    }

    /**
     * Set an event to be silent.
     *
     * @param event the event
     * @param <T> the type of event
     * @return the same event
     */
    public static <T extends DelegateEvent> T setSilent(T event) {
        event.setSilent(true);
        return event;
    }

    /**
     * Set an event to be silent.
     *
     * @param event the event
     * @param silent true to set silent
     * @param <T> the type of event
     * @return the same event
     */
    public static <T extends DelegateEvent> T setSilent(T event, boolean silent) {
        event.setSilent(silent);
        return event;
    }

    /**
     * Set an event as handled as {@link Result#ALLOW} if {@code allowed} is
     * true, otherwise do nothing.
     *
     * @param event the event
     * @param <T> the type of event
     * @return the same event
     */
    public static <T extends Handleable> T setAllowed(T event, boolean allowed) {
        if (allowed) {
            event.setResult(Result.ALLOW);
        }
        return event;
    }

}
