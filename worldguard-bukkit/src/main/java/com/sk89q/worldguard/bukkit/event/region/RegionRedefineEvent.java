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
 * Called when a {@link ProtectedRegion} is about to have its boundaries
 * redefined (updated) via the {@code /rg redefine} command or equivalent API.
 *
 * <p>Cancelling this event prevents the region boundaries from being changed.
 * Flags, members, owners and priority are preserved — only the shape changes.</p>
 *
 * <p>This event is fired on the main server thread immediately before the
 * redefine operation is executed.</p>
 *
 * <p>Example usage — prevent redefinition of a locked region:</p>
 * <pre>{@code
 * @EventHandler
 * public void onRegionRedefine(RegionRedefineEvent event) {
 *     if (isLocked(event.getOldRegion().getId())) {
 *         event.setCancelled(true);
 *         event.setCancelMessage("This region's boundaries are locked.");
 *     }
 * }
 * }</pre>
 */
public class RegionRedefineEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final World world;
    private final ProtectedRegion oldRegion;
    private final ProtectedRegion newRegion;
    @Nullable
    private final CommandSender actor;

    private boolean cancelled = false;
    @Nullable
    private String cancelMessage;

    /**
     * Create a new instance.
     *
     * @param world     the world the region belongs to
     * @param oldRegion the existing region before the change
     * @param newRegion the new region that will replace it (same id, different bounds)
     * @param actor     the command sender who initiated the action, or {@code null} if triggered via API
     */
    public RegionRedefineEvent(World world, ProtectedRegion oldRegion, ProtectedRegion newRegion,
                               @Nullable CommandSender actor) {
        checkNotNull(world);
        checkNotNull(oldRegion);
        checkNotNull(newRegion);
        this.world = world;
        this.oldRegion = oldRegion;
        this.newRegion = newRegion;
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
     * Get the existing region as it currently stands (before the change).
     *
     * @return the old region
     */
    public ProtectedRegion getOldRegion() {
        return oldRegion;
    }

    /**
     * Get the new region that will replace the old one.
     *
     * <p>The new region carries the same id and the copied flags/members/owners
     * from the old region, but with updated boundaries.</p>
     *
     * @return the new region
     */
    public ProtectedRegion getNewRegion() {
        return newRegion;
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
     * Set to {@code null} to use WorldGuard's default message.
     *
     * @param cancelMessage the message, or {@code null}
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
