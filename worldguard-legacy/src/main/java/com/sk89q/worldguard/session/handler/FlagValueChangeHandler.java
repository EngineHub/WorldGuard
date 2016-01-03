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
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;

public abstract class FlagValueChangeHandler<T> extends Handler {

    private final Flag<T> flag;
    private T lastValue;

    protected FlagValueChangeHandler(Session session, Flag<T> flag) {
        super(session);
        this.flag = flag;
    }

    @Override
    public final void initialize(Player player, Location current, ApplicableRegionSet set) {
        lastValue = set.queryValue(getPlugin().wrapPlayer(player), flag);
        onInitialValue(player, set, lastValue);
    }

    @Override
    public boolean onCrossBoundary(Player player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        T currentValue = toSet.queryValue(getPlugin().wrapPlayer(player), flag);
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

    protected abstract void onInitialValue(Player player, ApplicableRegionSet set, T value);

    protected abstract boolean onSetValue(Player player, Location from, Location to, ApplicableRegionSet toSet, T currentValue, T lastValue, MoveType moveType);

    protected abstract boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, T lastValue, MoveType moveType);

}
