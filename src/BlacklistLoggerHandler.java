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

/**
 * Interface for loggers for the blacklist.
 *
 * @author sk89q
 */
public interface BlacklistLoggerHandler {
    /**
     * Log a block destroy attempt.
     *
     * @param player
     * @param block
     */
    public void logDestroyAttempt(Player player, Block block, String comment);
    /**
     * Log a block break attempt.
     *
     * @param player
     * @param block
     */
    public void logBreakAttempt(Player player, Block block, String comment);
    /**
     * Log a right click on attempt.
     * 
     * @param player
     * @param block
     */
    public void logUseAttempt(Player player, Block block, String comment);
    /**
     * Right a left click attempt.
     * 
     * @param player
     * @param item
     */
    public void logDestroyWithAttempt(Player player, int item, String comment);
    /**
     * Log a right click attempt.
     * 
     * @param player
     * @param item
     */
    public void logCreateAttempt(Player player, int item, String comment);
    /**
     * Log a drop attempt.
     *
     * @param player
     * @param item
     */
    public void logDropAttempt(Player player, int item, String comment);
    /**
     * Close the logger.
     */
    public void close();
}
