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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.target.Target;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractBlacklistEvent implements BlacklistEvent {

    @Nullable
    private final LocalPlayer player;
    private final Vector position;
    private final Target target;
    
    /**
     * Construct the object.
     * 
     * @param player The player associated with this event
     * @param position The position the event occurred at
     * @param target The target of the event
     */
    AbstractBlacklistEvent(@Nullable LocalPlayer player, Vector position, Target target) {
        checkNotNull(position);
        checkNotNull(target);
        this.player = player;
        this.position = position;
        this.target = target;
    }

    @Nullable
    @Override
    public LocalPlayer getPlayer() {
        return player;
    }

    @Override
    public String getCauseName() {
        return player != null ? player.getName() : position.toString();
    }

    @Override
    public Vector getPosition() {
        return position;
    }
    
    @Override
    public Target getTarget() {
        return target;
    }

    protected String getPlayerName() {
        return player == null ? "(unknown)" : player.getName();
    }

}
