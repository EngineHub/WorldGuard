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

package com.sk89q.worldguard.internal.event.entity;

import com.sk89q.worldguard.internal.cause.Cause;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpawnEntityEvent extends AbstractEntityEvent {

    private static final HandlerList handlers = new HandlerList();
    private final EntityType effectiveType;

    public SpawnEntityEvent(Event originalEvent, List<? extends Cause<?>> causes, Entity target) {
        super(originalEvent, causes, checkNotNull(target));
        this.effectiveType = target.getType();
    }

    public SpawnEntityEvent(Event originalEvent, List<? extends Cause<?>> causes, Location location, EntityType type) {
        super(originalEvent, causes, location);
        checkNotNull(type);
        this.effectiveType = type;
    }

    /**
     * Get the effective entity type of the spawned entity.
     *
     * @return the effective type
     */
    public EntityType getEffectiveType() {
        return effectiveType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
