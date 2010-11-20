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

import java.util.Set;
import java.util.HashSet;

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
     * @param handler
     */
    public void addHandler(BlacklistLoggerHandler handler) {
        handlers.add(handler);
    }

    /**
     * Add a handler.
     *
     * @param handler
     */
    public void removeHandler(BlacklistLoggerHandler handler) {
        handlers.remove(handler);
    }

    /**
     * Add a handler.
     *
     * @param handler
     */
    public void clearHandlers() {
        handlers.clear();
    }

    /**
     * Log a block destroy attempt.
     *
     * @param player
     * @param block
     */
    public void logDestroyAttempt(Player player, Block block) {
        for (BlacklistLoggerHandler handler : handlers) {
            handler.logDestroyAttempt(player, block);
        }
    }

    /**
     * Log a right click on attempt.
     *
     * @param player
     * @param block
     */
    public void logUseAttempt(Player player, Block block) {
        for (BlacklistLoggerHandler handler : handlers) {
            handler.logUseAttempt(player, block);
        }
    }

    /**
     * Right a left click attempt.
     *
     * @param player
     * @param item
     */
    public void logDestroyWithAttempt(Player player, int item) {
        for (BlacklistLoggerHandler handler : handlers) {
            handler.logDestroyWithAttempt(player, item);
        }
    }

    /**
     * Log a right click attempt.
     *
     * @param player
     * @param item
     */
    public void logCreateAttempt(Player player, int item) {
        for (BlacklistLoggerHandler handler : handlers) {
            handler.logCreateAttempt(player, item);
        }
    }

    /**
     * Log a right click attempt.
     *
     * @param player
     * @param item
     */
    public void logDropAttempt(Player player, int item) {
        for (BlacklistLoggerHandler handler : handlers) {
            handler.logDropAttempt(player, item);
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
