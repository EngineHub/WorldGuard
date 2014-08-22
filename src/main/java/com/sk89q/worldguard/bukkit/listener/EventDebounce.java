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

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.bukkit.util.WGMetadata;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.metadata.Metadatable;

class EventDebounce {

    private final long time = System.currentTimeMillis();
    private final int debounceTime;
    private final boolean cancelled;

    EventDebounce(int debounceTime, boolean cancelled) {
        this.debounceTime = debounceTime;
        this.cancelled = cancelled;
    }

    public boolean isValid() {
        return System.currentTimeMillis() - time < debounceTime;
    }

    public boolean getLastCancellation() {
        return cancelled;
    }

    public static <T extends Event & Cancellable> void debounce(Metadatable target, String key, int debounceTime, Cancellable originalEvent, T firedEvent) {
        EventDebounce debounce = WGMetadata.getIfPresent(target, key, EventDebounce.class);
        if (debounce != null && debounce.isValid()) {
            if (debounce.getLastCancellation()) {
                originalEvent.setCancelled(true);
            }
        } else {
            boolean cancelled = Events.fireAndTestCancel(firedEvent);
            if (cancelled) {
                originalEvent.setCancelled(true);
            }
            WGMetadata.put(target, key, new EventDebounce(debounceTime, cancelled));
        }
    }

}
