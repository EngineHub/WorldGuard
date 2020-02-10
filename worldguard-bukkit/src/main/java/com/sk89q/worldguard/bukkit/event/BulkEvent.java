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

package com.sk89q.worldguard.bukkit.event;

import org.bukkit.event.Event.Result;

/**
 * A bulk event contains several affected objects in a list.
 */
public interface BulkEvent {

    /**
     * Get the actual result.
     *
     * <p>By default, bulk events will set the result to DENY if the number of
     * affected objects drops to zero. This method returns the true result.</p>
     *
     * @return the explicit result
     */
    Result getExplicitResult();

}
