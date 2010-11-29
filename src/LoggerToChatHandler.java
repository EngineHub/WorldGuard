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
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Sends all logger messages to a player.
 *
 * @author sk89q
 */
public class LoggerToChatHandler extends Handler {
    /**
     * Player.
     */
    private Player player;

    /**
     * Construct the object.
     * 
     * @param player
     */
    public LoggerToChatHandler(Player player) {
        this.player = player;
    }

    /**
     * Close the handler.
     */
    public void close() {
    }

    /**
     * Flush the output.
     */
    public void flush() {
    }

    /**
     * Publish a log record.
     */
    public void publish(LogRecord record) {
        player.sendMessage(Colors.LightGray + record.getLevel().getName() + ": "
                + Colors.White + record.getMessage());
    }
}
