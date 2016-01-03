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

package com.sk89q.worldguard.session.handler;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NotifyExitFlag extends FlagValueChangeHandler<Boolean> {

    private Boolean notifiedForLeave = false;

    public NotifyExitFlag(Session session) {
        super(session, DefaultFlag.NOTIFY_LEAVE);
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet set, Boolean value) {

    }

    @Override
    protected boolean onSetValue(Player player, Location from, Location to, ApplicableRegionSet toSet, Boolean currentValue, Boolean lastValue, MoveType moveType) {
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, Boolean lastValue, MoveType moveType) {
        getPlugin().broadcastNotification(ChatColor.GRAY + "WG: "
                + ChatColor.LIGHT_PURPLE + player.getName()
                + ChatColor.GOLD + " left NOTIFY region");
        return true;
    }
}
