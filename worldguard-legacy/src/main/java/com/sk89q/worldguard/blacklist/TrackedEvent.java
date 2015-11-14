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

package com.sk89q.worldguard.blacklist;

import com.sk89q.worldguard.blacklist.event.BlacklistEvent;

import javax.annotation.Nullable;

class TrackedEvent {

    @Nullable
    private BlacklistEvent lastEvent;
    private long time = 0;

    public boolean matches(BlacklistEvent other) {
        if (lastEvent == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        return other.getEventType() == lastEvent.getEventType() && time > now - 3000;
    }

    public void resetTimer() {
        time = System.currentTimeMillis();
    }

    public void setLastEvent(@Nullable BlacklistEvent lastEvent) {
        this.lastEvent = lastEvent;
    }
}
