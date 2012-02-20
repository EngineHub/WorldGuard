// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.blacklist;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldguard.blacklist.events.BlacklistEvent;
import com.sk89q.worldguard.blacklist.loggers.BlacklistLoggerHandler;

/**
 *
 * @author sk89q
 */
public class BlacklistLogger implements BlacklistLoggerHandler {
    /**
     * List of logger handlers.
     */
    private Set<BlacklistLoggerHandler> handlers
            = new HashSet<BlacklistLoggerHandler>();

    /**
     * Add a handler.
     *
     * @param handler The handler to add
     */
    public void addHandler(BlacklistLoggerHandler handler) {
        handlers.add(handler);
    }

    /**
     * Remove a handler.
     *
     * @param handler The handler to remove
     */
    public void removeHandler(BlacklistLoggerHandler handler) {
        handlers.remove(handler);
    }

    /**
     * Add a handler.
     */
    public void clearHandlers() {
        handlers.clear();
    }

    /**
     * Log an event.
     *
     * @param event The event to log
     */
    public void logEvent(BlacklistEvent event, String comment) {
        for (BlacklistLoggerHandler handler : handlers) {
            handler.logEvent(event, comment);
        }
    }

    /**
     * Close the connection.
     */
    public void close() {
        for (BlacklistLoggerHandler handler : handlers) {
            handler.close();
        }
    }
}
