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

package com.sk89q.worldguard.blacklist.event;

public enum EventType {

    BREAK(BlockBreakBlacklistEvent.class, "on-break"),
    PLACE(BlockPlaceBlacklistEvent.class, "on-place"),
    INTERACT(BlockInteractBlacklistEvent.class, "on-interact"),
    DISPENSE(BlockDispenseBlacklistEvent.class, "on-dispense"),
    DESTROY_WITH(ItemDestroyWithBlacklistEvent.class, "on-destroy-with"),
    ACQUIRE(ItemAcquireBlacklistEvent.class, "on-acquire"),
    DROP(ItemDropBlacklistEvent.class, "on-drop"),
    USE(ItemUseBlacklistEvent.class, "on-use");

    private final Class<? extends BlacklistEvent> eventClass;
    private final String ruleName;

    private EventType(Class<? extends BlacklistEvent> eventClass, String ruleName) {
        this.eventClass = eventClass;
        this.ruleName = ruleName;
    }

    public Class<? extends BlacklistEvent> getEventClass() {
        return eventClass;
    }

    public String getRuleName() {
        return ruleName;
    }

}