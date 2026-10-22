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

package com.sk89q.worldguard.bukkit.event.region;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when the priority of a {@link ProtectedRegion} is about to be
 * changed via the {@code /rg setpriority} command or equivalent API.
 *
 * <p>Cancelling this event prevents the priority from being changed.</p>
 *
 * <p>This event is fired on the main server thread immediately before the
 * priority is written to the region.</p>
 */
public class RegionPriorityChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final World world;
    private final ProtectedRegion region;
    private final int oldPriority;
    private final int newPriority;
    @Nullable
    private final CommandSender actor;

    private boolean cancelled = false;
    @Nullable
    private String cancelMessage;

    /**
     * Create a new instance.
     *
     * @param world       the world the region belongs to
     * @param region      the region being modified
     * @param oldPriority the current priority before the change
     * @param newPriority the new priority being set
     * @param actor       the command sender who initiated the action, or {@code null} if via API
     */
    public RegionPriorityChangeEvent(World world, ProtectedRegion region, int oldPriority,
                                     int newPriority, @Nullable CommandSender actor) {
        checkNotNull(world);
        checkNotNull(region);
        this.world = world;
        this.region = region;
        this.oldPriority = oldPriority;
        this.newPriority = newPriority;
        this.actor = actor;
    }

    /**
     * Get the world in which the region resides.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the region whose priority is being changed.
     *
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    /**
     * Get the current priority of the region (before the change).
     *
     * @return the old priority
     */
    public int getOldPriority() {
        return oldPriority;
    }

    /**
     * Get the new priority being applied.
     *
     * @return the new priority
     */
    public int getNewPriority() {
        return newPriority;
    }

    /**
     * Get the command sender who initiated this action.
     *
     * <p>Returns {@code null} when triggered through a direct API call.</p>
     *
     * @return the actor, or {@code null} if not applicable
     */
    @Nullable
    public CommandSender getActor() {
        return actor;
    }

    /**
     * Get the optional cancel message to send to the actor.
     *
     * @return the cancel message, or {@code null}
     */
    @Nullable
    public String getCancelMessage() {
        return cancelMessage;
    }

    /**
     * Set a custom message to send to the actor when this event is cancelled.
     *
     * @param cancelMessage the message, or {@code null} to use the default
     */
    public void setCancelMessage(@Nullable String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
