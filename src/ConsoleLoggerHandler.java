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

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author sk89q
 */
public class ConsoleLoggerHandler implements BlacklistLoggerHandler {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    /**
     * Log a block destroy attempt.
     *
     * @param player
     * @param block
     */
    public void logDestroyAttempt(Player player, Block block) {
        logger.log(Level.INFO, "WorldGuard: " + player.getName()
                + " tried to destroy " + getFriendlyItemName(block.getType()));
    }

    /**
     * Log a right click on attempt.
     *
     * @param player
     * @param block
     */
    public void logUseAttempt(Player player, Block block) {
        logger.log(Level.INFO, "WorldGuard: " + player.getName()
                + " tried to use " + getFriendlyItemName(block.getType()));
    }
    
    /**
     * Log an attempt to destroy with an item.
     *
     * @param player
     * @param item
     */
    public void logDestroyWithAttempt(Player player, int item) {
        logger.log(Level.INFO, "WorldGuard: " + player.getName()
                + " tried to destroy with " + getFriendlyItemName(item));
    }

    /**
     * Log a block creation attempt.
     *
     * @param player
     * @param item
     */
    public void logCreateAttempt(Player player, int item) {
        logger.log(Level.INFO, "WorldGuard: " + player.getName()
                + " tried to create " + getFriendlyItemName(item));
    }

    /**
     * Log a drop attempt.
     *
     * @param player
     * @param item
     */
    public void logDropAttempt(Player player, int item) {
        logger.log(Level.INFO, "WorldGuard: " + player.getName()
                + " tried to drop " + getFriendlyItemName(item));
    }

    /**
     * Get an item's friendly name with its ID.
     * 
     * @param id
     */
    private static String getFriendlyItemName(int id) {
        return etc.getDataSource().getItem(id) + " (#" + id + ")";
    }

    /**
     * Close the connection.
     */
    public void close() {
    }
}
