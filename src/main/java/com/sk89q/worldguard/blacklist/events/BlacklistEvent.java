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
     * @param player
     * @param pos
     * @param type
     */
    public BlacklistEvent(LocalPlayer player, Vector pos, int type) {
        this.player = player;
        this.pos = pos;
        this.type = type;
    }
    
    /**
     * Get the player.
     * 
     * @return
     */
    public LocalPlayer getPlayer() {
        return player;
    }
    
    /**
     * Get the position.
     * 
     * @return
     */
    public Vector getPosition() {
        return pos;
    }
    
    /**
     * Get the type.
     * 
     * @return
     */
    public int getType() {
        return type;
    }
    
    /**
     * Get a short description such as "break" or "destroy with."
     * 
     * @return
     */
    public abstract String getDescription();
}
