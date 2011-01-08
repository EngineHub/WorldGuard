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

package com.sk89q.worldguard.protection;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.domains.DefaultDomain;

/**
 * Represents a region of any shape and size that can be protected.
 * 
 * @author sk89q
 */
public abstract class ProtectedRegion {
    /**
     * Area message.
     */
    private String enterMessage;
    /**
     * List of owners.
     */
    private DefaultDomain owners = new DefaultDomain();
    /**
     * Priority.
     */
    private int priority = 0;
    /**
     * Area flags.
     */
    private AreaFlags flags = new AreaFlags();

    /**
     * Construct a new instance of this cuboid region.
     *
     * @param id
     * @param priority
     * @param owners
     * @param enterMessage
     */
    public ProtectedRegion() {
        this.priority = 0;
        this.owners = new DefaultDomain();
        this.enterMessage = null;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * Se flags.
     * @param flags
     */
    public void setFlags(AreaFlags flags) {
        this.flags = flags;
    }
    
    /**
     * @return the enterMessage
     */
    public String getEnterMessage() {
        return enterMessage;
    }

    /**
     * @param enterMessage the enterMessage to set
     */
    public void setEnterMessage(String enterMessage) {
        this.enterMessage = enterMessage;
    }

    /**
     * @return the owners
     */
    public DefaultDomain getOwners() {
        return owners;
    }

    /**
     * @param owners the owners to set
     */
    public void setOwners(DefaultDomain owners) {
        this.owners = owners;
    }

    /**
     * Get flags.
     * 
     * @return
     */
    public AreaFlags getFlags() {
        return flags;
    }
    
    /**
     * Check to see if a point is inside this region.
     * 
     * @param pt
     * @return
     */
    public abstract boolean contains(Vector pt);
}
