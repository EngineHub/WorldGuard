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

package com.sk89q.worldguard;

import com.sk89q.worldedit.Vector;

public abstract class LocalPlayer {
    /**
     * Get a player's name.
     * 
     * @return
     */
    public abstract String getName();
    
    /**
     * Returns true if the player is inside a group.
     * 
     * @param group
     * @return
     */
    public abstract boolean hasGroup(String group);
    
    /**
     * Get the player's position.
     * 
     * @return
     */
    public abstract Vector getPosition();
    
    /**
     * Kick the player.
     * 
     * @param msg
     */
    public abstract void kick(String msg);
    
    /**
     * Ban the player.
     * 
     * @param msg
     */
    public abstract void ban(String msg);
    
    /**
     * Send the player a message;
     * 
     * @param msg
     */
    public abstract void printRaw(String msg);
    
    /**
     * Get the player's list of groups.
     * 
     * @return
     */
    public abstract String[] getGroups();
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LocalPlayer)) {
            return false;
        }
        
        return ((LocalPlayer)obj).getName().equals(getName());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
