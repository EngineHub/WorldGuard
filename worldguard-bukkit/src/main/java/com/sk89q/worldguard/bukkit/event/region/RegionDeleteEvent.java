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

import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when a {@link ProtectedRegion} is about to be removed from a
 * {@link com.sk89q.worldguard.protection.managers.RegionManager}.
 *
 * <p>Cancelling this event prevents the region (and any affected children,
 * depending on the {@link RemovalStrategy}) from being deleted. The command
 * or API call that initiated the removal will receive an error message.</p>
 *
 * <p>This event is fired on the main server thread immediately before the
 * removal is executed. It is <strong>not</strong> fired for transient regions.</p>
 *
 * <p>Example usage — protect a region from deletion:</p>
 * <pre>{@code
 * @EventHandler
 * public void onRegionDelete(RegionDeleteEvent event) {
 *     if (isProtected(event.getRegion().getId())) {
 *         event.setCancelled(true);
 *         event.setCancelMessage("This region is protected and cannot be deleted.");
 *     }
 * }
 * }</pre>
 */
public class RegionDeleteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final World world;
    private final ProtectedRegion region;
    private final RemovalStrategy removalStrategy;

    private boolean cancelled = false;
    @Nullable
    private String cancelMessage;
    @Nullable
    private final CommandSender actor;

    /**
     * Create a new instance.
     *
     * @param world           the world the region belongs to
     * @param region          the region that is about to be removed
     * @param removalStrategy the strategy that will be used for child regions
     * @param actor           the command sender who initiated the action, or {@code null} if triggered via API
     */
    public RegionDeleteEvent(World world, ProtectedRegion region, RemovalStrategy removalStrategy,
                             @Nullable CommandSender actor) {
        checkNotNull(world);
        checkNotNull(region);
        checkNotNull(removalStrategy);
        this.world = world;
        this.region = region;
        this.removalStrategy = removalStrategy;
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
     * Get the region that is about to be deleted.
     *
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    /**
     * Get the removal strategy that will be applied to child regions.
     *
     * @return the removal strategy
     */
    public RemovalStrategy getRemovalStrategy() {
        return removalStrategy;
    }

    /**
     * Get the command sender who initiated this action.
     *
     * <p>Returns {@code null} when the region is removed through a direct API call
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
