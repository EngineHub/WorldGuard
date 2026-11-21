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
import org.bukkit.command.CommandSender;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when a {@link ProtectedRegion} is about to be added (defined) in a
 * {@link com.sk89q.worldguard.protection.managers.RegionManager}.
 *
 * <p>Cancelling this event prevents the region from being created. The command
 * or API call that initiated the addition will receive an error message.</p>
 *
 * <p>This event is fired on the main server thread immediately before the
 * region is added to the index. It is <strong>not</strong> fired when regions
 * are loaded from storage on world load.</p>
 *
 * <p>Example usage — prevent regions with a specific prefix from being created:</p>
 * <pre>{@code
 * @EventHandler
 * public void onRegionAdd(RegionAddEvent event) {
 *     if (event.getRegion().getId().startsWith("reserved_")) {
 *         event.setCancelled(true);
 *         event.setCancelMessage("Region names starting with 'reserved_' are not allowed.");
 *     }
 * }
 * }</pre>
 */
public class RegionAddEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final World world;
    private final ProtectedRegion region;
    @Nullable
    private final CommandSender actor;

    private boolean cancelled = false;
    @Nullable
    private String cancelMessage;

    /**
     * Create a new instance.
     *
     * @param world  the world the region is being added to
     * @param region the region that is about to be added
     * @param actor  the command sender who initiated the action, or {@code null} if triggered via API
     */
    public RegionAddEvent(World world, ProtectedRegion region, @Nullable CommandSender actor) {
        checkNotNull(world);
        checkNotNull(region);
        this.world = world;
        this.region = region;
        this.actor = actor;
    }

    /**
     * Get the world in which the region is being defined.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the region that is about to be added.
     *
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    /**
     * Get the command sender who initiated this action.
     *
     * <p>Returns {@code null} when the region is created through a direct API call
     * rather than through a player or console command.</p>
     *
     * @return the actor, or {@code null} if not applicable
     */
    @Nullable
    public CommandSender getActor() {
        return actor;
    }

    /**
     * Get the optional message to send back to the command sender when the
     * event is cancelled.
     *
     * <p>If {@code null}, WorldGuard will use a generic cancellation message.</p>
     *
     * @return the cancel message, or {@code null} if none is set
     */
    @Nullable
    public String getCancelMessage() {
        return cancelMessage;
    }

    /**
     * Set a custom message to send to the command sender when this event is
     * cancelled. Set to {@code null} to use WorldGuard's default message.
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
