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
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class TimeLockFlag extends FlagValueChangeHandler<String> {

    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<TimeLockFlag> {
        @Override
        public TimeLockFlag create(Session session) {
            return new TimeLockFlag(session);
        }
    }

    private Long initialTime;
    private boolean initialRelative;

    private static Pattern timePattern = Pattern.compile("(\\+|-)?\\d+");

    public TimeLockFlag(Session session) {
        super(session, Flags.TIME_LOCK);
    }

    private void updatePlayerTime(LocalPlayer player, @Nullable String value) {
        // store settings, regardless of if we change anything
        initialRelative = player.isPlayerTimeRelative();
        initialTime = player.getPlayerTimeOffset();
        if (value == null || !timePattern.matcher(value).matches()) {
            // invalid input
            return;
        }
        boolean relative = value.startsWith("+") || value.startsWith("-");
        Long time = Long.valueOf(value);
//        if (!relative && (time < 0L || time > 24000L)) { // invalid time, reset to 0
//            time = 0L;
//        }
        player.setPlayerTime(time, relative);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, String value) {
        updatePlayerTime(player, value);
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String currentValue, String lastValue, MoveType moveType) {
        updatePlayerTime(player, currentValue);
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String lastValue, MoveType moveType) {
        player.setPlayerTime(initialTime, initialRelative);
        initialRelative = true;
        initialTime = 0L;
        return true;
    }

}
