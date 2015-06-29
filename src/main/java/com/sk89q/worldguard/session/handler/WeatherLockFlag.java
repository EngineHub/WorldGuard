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
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class WeatherLockFlag extends FlagValueChangeHandler<WeatherType> {

    private WeatherType initialWeather;

    public WeatherLockFlag(Session session) {
        super(session, DefaultFlag.WEATHER_LOCK);
    }

    private void updatePlayerWeather(Player player, @Nullable WeatherType value) {
        initialWeather = player.getPlayerWeather();
        player.setPlayerWeather(value);
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet set, WeatherType value) {
        if (value == null) {
            initialWeather = null;
            return;
        }
        updatePlayerWeather(player, value);
    }

    @Override
    protected boolean onSetValue(Player player, Location from, Location to, ApplicableRegionSet toSet, WeatherType currentValue, WeatherType lastValue, MoveType moveType) {
        updatePlayerWeather(player, currentValue);
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, WeatherType lastValue, MoveType moveType) {
        if (initialWeather != null) {
            player.setPlayerWeather(initialWeather);
        } else {
            player.resetPlayerWeather();
        }
        initialWeather = null;
        return true;
    }

}
