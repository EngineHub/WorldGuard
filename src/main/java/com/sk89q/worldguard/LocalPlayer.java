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

package com.sk89q.worldguard;

import com.sk89q.worldedit.Vector;

import java.util.UUID;

public abstract class LocalPlayer {

    /**
     * Get this player's name.
     * 
     * @return The player's name
     */
    public abstract String getName();

    /**
     * Get this player's unique ID.
     *
     * @return a UUID
     */
    public abstract UUID getUniqueId();
    
    /**
     * Returns true if this player is inside a group.
     * 
     * @param group The group to check
     * @return Whether this player is in {@code group}
     */
    public abstract boolean hasGroup(String group);
    
    /**
     * Get this player's position.
     * 
     * @return The player's position
     */
    public abstract Vector getPosition();
    
    /**
     * Kick this player.
     * 
     * @param msg The message to kick the player with
     */
    public abstract void kick(String msg);
    
    /**
     * Ban this player.
     * 
     * @param msg The message to ban the player with
     */
    public abstract void ban(String msg);
    
    /**
     * Send this player a message.
     * 
     * @param msg The message to send to the player
     */
    public abstract void printRaw(String msg);
    
    /**
     * Get this player's list of groups.
     * 
     * @return The groups this player is in
     */
    public abstract String[] getGroups();
    
    /**
     * Returns whether this player has permission.
     * 
     * @param perm The permission to check
     * @return Whether this player has {@code perm}
     */
    public abstract boolean hasPermission(String perm);
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocalPlayer && ((LocalPlayer) obj).getName().equals(getName());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

}
