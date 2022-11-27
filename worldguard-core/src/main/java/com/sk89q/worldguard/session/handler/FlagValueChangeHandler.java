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
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;

import java.util.Set;

public abstract class FlagValueChangeHandler<T> extends Handler {

    private final Flag<T> flag;
    private T lastValue;

    protected FlagValueChangeHandler(Session session, Flag<T> flag) {
        super(session);
        this.flag = flag;
    }

    @Override
    public final void initialize(LocalPlayer player, Location current, ApplicableRegionSet set) {
        lastValue = set.queryValue(player, flag);
        onInitialValue(player, set, lastValue);
    }

    @Override
    public final void uninitialize(LocalPlayer player, Location current, ApplicableRegionSet set) {
        onClearValue(player, set);
        lastValue = null;
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        if (entered.isEmpty() && exited.isEmpty()
                && from.getExtent().equals(to.getExtent())) { // sets don't include global regions - check if those changed
            return true; // no changes to flags if regions didn't change
        }

        T currentValue = toSet.queryValue(player, flag);
        boolean allowed = true;

        if (currentValue == null && lastValue != null) {
            allowed = onAbsentValue(player, from, to, toSet, lastValue, moveType);
        } else if (currentValue != null && currentValue != lastValue) {
            allowed = onSetValue(player, from, to, toSet, currentValue, lastValue, moveType);
        }

        if (allowed) {
            lastValue = currentValue;
        }

        return allowed;
    }

    protected abstract void onInitialValue(LocalPlayer player, ApplicableRegionSet set, T value);

    protected abstract boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, T currentValue, T lastValue, MoveType moveType);

    protected abstract boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, T lastValue, MoveType moveType);

    protected void onClearValue(LocalPlayer player, ApplicableRegionSet set) {
        if (lastValue != null) {
            Location current = player.getLocation();
            onAbsentValue(player, current, current, set, lastValue, MoveType.OTHER_NON_CANCELLABLE);
        }
    }
}
