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

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.CommandUtils;

import javax.annotation.Nullable;

/**
 * Stores a component. Either a MiniMessage formatted string or a legacy format.
 * The format depends on the available implementation.
 */
public class ComponentFlag extends StringFlag {
    private final String miniMessageDefault;

    public ComponentFlag(String name) {
        super(name);
        this.miniMessageDefault = null;
    }

    public ComponentFlag(String name, String defaultValue) {
        super(name, defaultValue);
        this.miniMessageDefault = null;
    }

    public ComponentFlag(String name, String defaultValue, String miniMessageDefault) {
        super(name, defaultValue);
        this.miniMessageDefault = miniMessageDefault;
    }

    public ComponentFlag(String name, RegionGroup defaultGroup) {
        super(name, defaultGroup);
        this.miniMessageDefault = null;
    }

    public ComponentFlag(String name, RegionGroup defaultGroup, String defaultValue) {
        super(name, defaultGroup, defaultValue);
        this.miniMessageDefault = null;
    }

    public ComponentFlag(String name, RegionGroup defaultGroup, String defaultValue, String miniMessageDefault) {
        super(name, defaultGroup, defaultValue);
        this.miniMessageDefault = miniMessageDefault;
    }

    @Override
    public String parseInput(FlagContext context) throws InvalidFlagFormat {
        String lines = context.getUserInput()
                .replaceAll("(?<!\\\\)\\\\n", "\n")
                .replaceAll("\\\\\\\\n", "\\\\n");
        if (!WorldGuard.getInstance().getPlatform().hasMiniMessage()) {
            // Add color codes
            lines = CommandUtils.replaceColorMacros(lines);
        }
        return lines;
    }

    @Nullable
    @Override
    public String getDefault() {
        if (WorldGuard.getInstance().getPlatform().hasMiniMessage() && miniMessageDefault != null) {
            return miniMessageDefault;
        }
        return super.getDefault();
    }
}
