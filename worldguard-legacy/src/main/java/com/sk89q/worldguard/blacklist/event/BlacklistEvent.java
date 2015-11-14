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

package com.sk89q.worldguard.blacklist.event;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.target.Target;

import javax.annotation.Nullable;

public interface BlacklistEvent {

    /**
     * Get the player.
     *
     * @return The player associated with this event
     */
    @Nullable
    LocalPlayer getPlayer();

    /**
     * Get the cause name, which is usually the player name.
     *
     * @return the cause name
     */
    String getCauseName();

    /**
     * Get the position.
     *
     * @return The position of this event
     */
    Vector getPosition();

    /**
     * Get the position that should be logged.
     *
     * @return The position that be logged.
     */
    Vector getLoggedPosition();

    /**
     * Get the item type.
     *
     * @return The type associated with this event
     */
    Target getTarget();

    /**
     * Get a short description such as "break" or "destroy with."
     *
     * @return The event description
     */
    String getDescription();

    /**
     * Get a message for logger outputs.
     *
     * @return A logging message
     */
    String getLoggerMessage();

    /**
     * Get the event type.
     *
     * @return the type
     */
    EventType getEventType();

}
