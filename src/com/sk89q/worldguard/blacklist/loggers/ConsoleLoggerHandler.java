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

package com.sk89q.worldguard.blacklist.loggers;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.blacklist.events.*;

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
     * Log an event.
     *
     * @param player
     * @param event
     */
    public void logEvent(BlacklistEvent event, String comment) {
        // Block break
        if (event instanceof BlockBreakBlacklistEvent) {
            BlockBreakBlacklistEvent evt = (BlockBreakBlacklistEvent)event;
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " tried to break " + getFriendlyItemName(evt.getType())
                    + (comment != null ? " (" + comment + ")" : ""));
        
        // Block place
        } else if (event instanceof BlockPlaceBlacklistEvent) {
            BlockPlaceBlacklistEvent evt = (BlockPlaceBlacklistEvent)event;
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " tried to place " + getFriendlyItemName(evt.getType())
                    + (comment != null ? " (" + comment + ")" : ""));
        
        // Block interact
        } else if (event instanceof BlockPlaceBlacklistEvent) {
            BlockPlaceBlacklistEvent evt = (BlockPlaceBlacklistEvent)event;
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " tried to interact with " + getFriendlyItemName(evt.getType())
                    + (comment != null ? " (" + comment + ")" : ""));
        
        // Destroy with
        } else if (event instanceof DestroyWithBlacklistEvent) {
            DestroyWithBlacklistEvent evt = (DestroyWithBlacklistEvent)event;
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " tried to destroy with " + getFriendlyItemName(evt.getType())
                    + (comment != null ? " (" + comment + ")" : ""));
        
        // Acquire
        } else if (event instanceof ItemAcquireBlacklistEvent) {
            ItemAcquireBlacklistEvent evt = (ItemAcquireBlacklistEvent)event;
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " tried to acquire " + getFriendlyItemName(evt.getType())
                    + (comment != null ? " (" + comment + ")" : ""));
        
        // Drop
        } else if (event instanceof ItemDropBlacklistEvent) {
            ItemDropBlacklistEvent evt = (ItemDropBlacklistEvent)event;
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " tried to drop " + getFriendlyItemName(evt.getType())
                    + (comment != null ? " (" + comment + ")" : ""));
        
        // Use
        } else if (event instanceof ItemUseBlacklistEvent) {
            ItemUseBlacklistEvent evt = (ItemUseBlacklistEvent)event;
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " tried to use " + getFriendlyItemName(evt.getType())
                    + (comment != null ? " (" + comment + ")" : ""));
        
        // Unknown
        } else {
            logger.log(Level.INFO, "WorldGuard: " + event.getPlayer().getName()
                    + " caught unknown event: " + event.getClass().getCanonicalName());
        }
    }

    /**
     * Get an item's friendly name with its ID.
     * 
     * @param id
     */
    private static String getFriendlyItemName(int id) {
        ItemType type = ItemType.fromID(id);
        if (type != null) {
            return type.getName() + " (#" + id + ")";
        } else {
            return "#" + id + "";
        }
    }

    /**
     * Close the connection.
     */
    public void close() {
    }
}
