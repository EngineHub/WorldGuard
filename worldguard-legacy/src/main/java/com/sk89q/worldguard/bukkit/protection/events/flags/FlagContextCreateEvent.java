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

package com.sk89q.worldguard.bukkit.protection.events.flags;

import com.sk89q.worldguard.protection.flags.FlagContext.FlagContextBuilder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FlagContextCreateEvent extends Event {

    private FlagContextBuilder builder;

    public FlagContextCreateEvent(FlagContextBuilder builder) {
        this.builder = builder;
    }

    /**
     * Add an object to the flag context with the given key. Keys must be unique.
     *
     * @param key a unique string to identify the object
     * @param value the object to store in the context
     * @return true if added successfully, false if the key was already used
     */
    public boolean addObject(String key, Object value) {
        return builder.tryAddToMap(key, value);
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
