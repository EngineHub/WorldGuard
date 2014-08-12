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

package com.sk89q.worldguard.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Utility methods to deal with events.
 */
public final class Events {

    private Events() {
    }

    /**
     * Fire the {@code eventToFire} and return whether the event was cancelled.
     *
     * @param eventToFire the event to fire
     * @param <T> an event that can be fired and is cancellable
     * @return true if the event was cancelled
     */
    public static <T extends Event & Cancellable> boolean fireAndTestCancel( T eventToFire) {
        Bukkit.getServer().getPluginManager().callEvent(eventToFire);
        return eventToFire.isCancelled();
    }

    /**
     * Fire the {@code eventToFire} and cancel the original if the fired event
     * is cancelled.
     *
     * @param original the original event to potentially cancel
     * @param eventToFire the event to fire to consider cancelling the original event
     * @param <T> an event that can be fired and is cancellable
     * @return true if the event was fired and it caused the original event to be cancelled
     */
    public static <T extends Event & Cancellable> boolean fireToCancel(Cancellable original, T eventToFire) {
        Bukkit.getServer().getPluginManager().callEvent(eventToFire);
        if (eventToFire.isCancelled()) {
            original.setCancelled(true);
            return true;
        }

        return false;
    }

    /**
     * Fire the {@code eventToFire} and cancel the original if the fired event
     * is cancelled.
     *
     * @param original the original event to potentially cancel
     * @param eventToFire the event to fire to consider cancelling the original event
     * @param <T> an event that can be fired and is cancellable
     * @return true if the event was fired and it caused the original event to be cancelled
     */
    public static <T extends Event & Cancellable> boolean fireItemEventToCancel(PlayerInteractEvent original, T eventToFire) {
        Bukkit.getServer().getPluginManager().callEvent(eventToFire);
        if (eventToFire.isCancelled()) {
            original.setUseItemInHand(Result.DENY);
            return true;
        }

        return false;
    }

}
