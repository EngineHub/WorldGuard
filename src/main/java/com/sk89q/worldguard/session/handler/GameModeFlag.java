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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GameModeFlag extends FlagValueChangeHandler<GameMode> {

    private GameMode originalGameMode;
    private GameMode setGameMode;

    public GameModeFlag(Session session) {
        super(session, DefaultFlag.GAME_MODE);
    }

    public GameMode getSetGameMode() {
        return setGameMode;
    }

    private void updateGameMode(Player player, GameMode newValue, World world) {
        if (player.getGameMode() != newValue) {
            if (originalGameMode == null) {
                originalGameMode = player.getGameMode();
            }

            if (!getSession().getManager().hasBypass(player, world)) {
                player.setGameMode(newValue);
                setGameMode = newValue;
            }
        }
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet set, GameMode value) {
        updateGameMode(player, value, player.getWorld());
    }

    @Override
    protected boolean onSetValue(Player player, Location from, Location to, ApplicableRegionSet toSet, GameMode currentValue, GameMode lastValue, MoveType moveType) {
        updateGameMode(player, currentValue, to.getWorld());
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Location from, Location to, ApplicableRegionSet toSet, GameMode lastValue, MoveType moveType) {
        if (originalGameMode != null) {
            if (player.getGameMode() == setGameMode) {
                player.setGameMode(originalGameMode);
            }
            originalGameMode = null;
        }

        return true;
    }

}
