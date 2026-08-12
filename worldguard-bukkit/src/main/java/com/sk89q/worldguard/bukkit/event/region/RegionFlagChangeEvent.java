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

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when a {@link Flag} on a {@link ProtectedRegion} is about to be
 * set or cleared via the {@code /rg flag} command or equivalent API.
 *
 * <p>Cancelling this event prevents the flag change from being applied.</p>
 *
 * <p>This event is fired on the main server thread immediately before the
 * flag is written to the region.</p>
 *
 * <p>Example usage — log all flag changes:</p>
 * <pre>{@code
 * @EventHandler
 * public void onFlagChange(RegionFlagChangeEvent event) {
 *     String val = event.getNewValue() != null ? event.getNewValue().toString() : "(cleared)";
 *     Bukkit.getLogger().info("Flag " + event.getFlag().getName()
 *             + " on " + event.getRegion().getId() + " changed to " + val);
 * }
 * }</pre>
 */
public class RegionFlagChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final World world;
    private final ProtectedRegion region;
    private final Flag<?> flag;
    @Nullable
    private final Object newValue;
    @Nullable
    private final CommandSender actor;

    private boolean cancelled = false;
    @Nullable
    private String cancelMessage;

    /**
     * Create a new instance.
     *
     * @param world    the world the region belongs to
     * @param region   the region being modified
     * @param flag     the flag being changed
     * @param newValue the new value being set, or {@code null} if the flag is being cleared
     * @param actor    the command sender who initiated the action, or {@code null} if via API
     */
    public RegionFlagChangeEvent(World world, ProtectedRegion region, Flag<?> flag,
                                 @Nullable Object newValue, @Nullable CommandSender actor) {
        checkNotNull(world);
        checkNotNull(region);
        checkNotNull(flag);
        this.world = world;
        this.region = region;
        this.flag = flag;
        this.newValue = newValue;
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
     * Get the region whose flag is being changed.
     *
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    /**
     * Get the flag being changed.
     *
     * @return the flag
     */
    public Flag<?> getFlag() {
        return flag;
    }

    /**
     * Get the new value the flag is being set to.
     *
     * <p>Returns {@code null} if the flag is being <em>cleared</em> (removed
     * from the region so it falls back to the default or parent value).</p>
     *
     * @return the new value, or {@code null} if the flag is being cleared
     */
    @Nullable
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Returns {@code true} if the flag is being cleared rather than set to
     * a new value.
     *
     * @return true if the flag is being cleared
     */
    public boolean isClearing() {
        return newValue == null;
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
