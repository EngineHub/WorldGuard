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

package com.sk89q.worldguard.internal.event;

import com.sk89q.worldguard.internal.cause.Cause;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Fired when a block is interacted with.
 */
public class BlockInteractEvent extends AbstractInteractEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Block target;

    /**
     * Create a new instance.
     *
     * @param originalEvent the original event
     * @param causes a list of causes, where the originating causes are at the beginning
     * @param action the action that is being taken
     * @param target the target block being affected
     */
    public BlockInteractEvent(Event originalEvent, List<? extends Cause<?>> causes, Action action, Block target) {
        super(originalEvent, causes, action);
        checkNotNull(target);
        this.target = target;
    }

    /**
     * Get the target block being affected.
     *
     * @return a block
     */
    public Block getTarget() {
        return target;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
