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

package com.sk89q.worldguard.blacklist.events;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;

/**
 * Represents a blacklist event.
 * 
 * @author sk89q
 */
public abstract class BlacklistEvent {
    private Vector pos;
    private int type;
    
    /**
     * Holds the player that triggered the event.
     */
    private LocalPlayer player;
    
    /**
     * Construct the object.
     * 
     * @param player The player associated with this event
     * @param pos The position the event occurred at
     * @param type The type of item used
     */
    public BlacklistEvent(LocalPlayer player, Vector pos, int type) {
        this.player = player;
        this.pos = pos;
        this.type = type;
    }
    
    /**
     * Get the player.
     *
     * @return The player associated with this event
     */
    public LocalPlayer getPlayer() {
        return player;
    }
    
    /**
     * Get the position.
     * 
     * @return The position of this event
     */
    public Vector getPosition() {
        return pos;
    }
    
    /**
     * Get the item type.
     * 
     * @return The type associated with this event
     */
    public int getType() {
        return type;
    }
    
    /**
     * Get a short description such as "break" or "destroy with."
     * 
     * @return The event description
     */
    public abstract String getDescription();
}
