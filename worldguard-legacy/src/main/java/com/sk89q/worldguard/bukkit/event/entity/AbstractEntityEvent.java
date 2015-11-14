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

package com.sk89q.worldguard.bukkit.event.entity;

import com.google.common.base.Predicate;
import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is an internal event. We do not recommend handling or throwing
 * this event or its subclasses as the interface is highly subject to change.
 */
abstract class AbstractEntityEvent extends DelegateEvent {

    private final Location target;
    @Nullable
    private final Entity entity;

    protected AbstractEntityEvent(@Nullable Event originalEvent, Cause cause, Entity entity) {
        super(originalEvent, cause);
        checkNotNull(entity);
        this.target = entity.getLocation();
        this.entity = entity;
    }

    protected AbstractEntityEvent(@Nullable Event originalEvent, Cause cause, Location target) {
        super(originalEvent, cause);
        checkNotNull(target);
        this.target = target;
        this.entity = null;
    }

    /**
     * Get the world.
     *
     * @return the world
     */
    public World getWorld() {
        return target.getWorld();
    }

    /**
     * Get the target location being affected.
     *
     * @return a location
     */
    public Location getTarget() {
        return target;
    }


    /**
     * Get the target entity being affected.
     *
     * @return a entity
     */
    @Nullable
    public Entity getEntity() {
        return entity;
    }

    /**
     * Filter the list of affected entities with the given predicate. If the
     * predicate returns {@code false}, then the entity is not affected.
     *
     * @param predicate the predicate
     * @param cancelEventOnFalse true to cancel the event and clear the entity
     *                           list once the predicate returns {@code false}
     * @return true if one or more entities were filtered out
     */
    public boolean filter(Predicate<Location> predicate, boolean cancelEventOnFalse) {
        if (!isCancelled()) {
            if (!predicate.apply(getTarget())) {
                setCancelled(true);
            }
        }

        return isCancelled();
    }

}
