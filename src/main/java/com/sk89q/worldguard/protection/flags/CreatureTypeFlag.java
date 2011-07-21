// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldguard.protection.flags;

import org.bukkit.entity.CreatureType;

/**
 * Represents a creature type.
 *
 * @author sk89q
 */
public class CreatureTypeFlag extends EnumFlag<CreatureType> {

    public CreatureTypeFlag(String name, char legacyCode) {
        super(name, legacyCode, CreatureType.class);
    }

    public CreatureTypeFlag(String name) {
        super(name, CreatureType.class);
    }

    @Override
    public CreatureType detectValue(String input) {
        CreatureType lowMatch = null;

        for (CreatureType type : CreatureType.values()) {
            if (type.name().equalsIgnoreCase(input.trim())) {
                return type;
            }

            if (type.name().toLowerCase().startsWith(input.toLowerCase().trim())) {
                lowMatch = type;
            }
        }

        return lowMatch;
    }
}
