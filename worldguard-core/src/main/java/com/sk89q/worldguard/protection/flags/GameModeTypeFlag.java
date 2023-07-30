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

package com.sk89q.worldguard.protection.flags;

import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;

import javax.annotation.Nullable;

/**
 * Stores an gamemode type.
 * @deprecated replaced by {@link RegistryFlag<GameMode>}, will be removed in WorldGuard 8
 */
@Deprecated
public class GameModeTypeFlag extends Flag<GameMode> {

    protected GameModeTypeFlag(String name, @Nullable RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    protected GameModeTypeFlag(String name) {
        super(name);
    }

    @Override
    public GameMode parseInput(FlagContext context) throws InvalidFlagFormatException {
        String input = context.getUserInput();
        input = input.trim();
        GameMode gamemode = unmarshal(input);
        if (gamemode == null) {
            throw new InvalidFlagFormatException("Unknown game mode: " + input);
        }
        return gamemode;
    }

    @Override
    public GameMode unmarshal(@Nullable Object o) {
        return GameModes.get(String.valueOf(o).toLowerCase());
    }

    @Override
    public Object marshal(GameMode o) {
        return o.getId();
    }
}
