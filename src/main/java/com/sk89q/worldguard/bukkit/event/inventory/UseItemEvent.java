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

package com.sk89q.worldguard.bukkit.event.inventory;

import com.sk89q.worldguard.bukkit.cause.Cause;
import com.sk89q.worldguard.bukkit.event.DelegateEvent;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is an internal event. We do not recommend handling or throwing
 * this event or its subclasses as the interface is highly subject to change.
 *
 * <p>Thrown when an item is used.</p>
 */
public class UseItemEvent extends DelegateEvent {

    private static final HandlerList handlers = new HandlerList();
    private final World world;
    private final ItemStack itemStack;

    public UseItemEvent(@Nullable Event originalEvent, Cause cause, World world, ItemStack itemStack) {
        super(originalEvent, cause);
        checkNotNull(world);
        checkNotNull(itemStack);
        this.world = world;
        this.itemStack = itemStack;
    }

    /**
     * Get the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the item stack.
     *
     * @return the item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
