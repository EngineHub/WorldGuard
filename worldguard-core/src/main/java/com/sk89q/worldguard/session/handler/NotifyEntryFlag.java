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

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.formatting.StyledFragment;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;

public class NotifyEntryFlag extends FlagValueChangeHandler<Boolean> {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<NotifyEntryFlag> {
        @Override
        public NotifyEntryFlag create(Session session) {
            return new NotifyEntryFlag(session);
        }
    }

    public NotifyEntryFlag(Session session) {
        super(session, Flags.NOTIFY_ENTER);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, Boolean value) {

    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean currentValue, Boolean lastValue, MoveType moveType) {
        StringBuilder regionList = new StringBuilder();

        for (ProtectedRegion region : toSet) {
            if (regionList.length() != 0) {
                regionList.append(", ");
            }

            regionList.append(region.getId());
        }

        WorldGuard.getInstance().getPlatform().broadcastNotification(
                ColorCodeBuilder.asColorCodes(new StyledFragment().append(new StyledFragment(Style.GRAY).append("WG: "))
                        .append(new StyledFragment(Style.PURPLE).append(player.getName()))
                        .append(new StyledFragment(Style.YELLOW_DARK).append(" entered NOTIFY region: " + regionList)))
        );

        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Boolean lastValue, MoveType moveType) {
        return true;
    }

}
