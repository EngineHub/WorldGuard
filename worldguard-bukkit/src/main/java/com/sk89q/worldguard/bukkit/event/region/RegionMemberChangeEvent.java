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

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Called when the members or owners of a {@link ProtectedRegion} are about
 * to change — either added or removed via command or API.
 *
 * <p>Cancelling this event prevents the domain change from being applied.</p>
 *
 * <p>This event is fired on an async worker thread (because UUID resolution
 * is async), so listeners must be thread-safe. Use
 * {@code @EventHandler(ignoreCancelled = true)} and avoid calling
 * Bukkit API methods that require the main thread.</p>
 *
 * <p>Example usage — prevent adding owners to a frozen region:</p>
 * <pre>{@code
 * @EventHandler
 * public void onMemberChange(RegionMemberChangeEvent event) {
 *     if (event.getChangeType() == RegionMemberChangeEvent.ChangeType.ADD_OWNER
 *             && isFrozen(event.getRegion().getId())) {
 *         event.setCancelled(true);
 *     }
 * }
 * }</pre>
 */
public class RegionMemberChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The type of domain change being made.
     */
    public enum ChangeType {
        /** A player or group is being added as a member. */
        ADD_MEMBER,
        /** A player or group is being removed from members. */
        REMOVE_MEMBER,
        /** A player or group is being added as an owner. */
        ADD_OWNER,
        /** A player or group is being removed from owners. */
        REMOVE_OWNER
    }

    private final World world;
    private final ProtectedRegion region;
    private final ChangeType changeType;
    private final DefaultDomain domain;
    @Nullable
    private final CommandSender actor;

    private boolean cancelled = false;

    /**
     * Create a new instance.
     *
     * @param world      the world the region belongs to
     * @param region     the region being modified
     * @param changeType the type of change being made
     * @param domain     the domain entries being added or removed
     * @param actor      the command sender who initiated the action, or {@code null} if via API
     */
    public RegionMemberChangeEvent(World world, ProtectedRegion region, ChangeType changeType,
                                   DefaultDomain domain, @Nullable CommandSender actor) {
        super(true); // async = true — UUID resolution happens off-thread
        checkNotNull(world);
        checkNotNull(region);
        checkNotNull(changeType);
        checkNotNull(domain);
        this.world = world;
        this.region = region;
        this.changeType = changeType;
        this.domain = domain;
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
     * Get the region whose domain is being modified.
     *
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    /**
     * Get the type of change being made (add/remove, member/owner).
     *
     * @return the change type
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
     * Get the domain entries that are being added or removed.
     *
     * @return the affected domain
     */
    public DefaultDomain getDomain() {
        return domain;
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
