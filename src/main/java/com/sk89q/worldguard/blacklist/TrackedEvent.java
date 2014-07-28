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

class TrackedEvent {

    private BlacklistEvent event;
    private long time;

    /**
     * Construct the object.
     *
     * @param event The event tracked
     * @param time The time at which the event occurred
     */
    TrackedEvent(BlacklistEvent event, long time) {
        this.event = event;
        this.time = time;
    }

    public boolean matches(BlacklistEvent other, long now) {
        return other.getType() == event.getType()
                && time > now - 3000
                && other.getClass() == event.getClass();
    }

    public void resetTimer() {
        time = System.currentTimeMillis();
    }

}
