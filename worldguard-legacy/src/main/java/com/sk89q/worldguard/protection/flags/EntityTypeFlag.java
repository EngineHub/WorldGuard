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

import org.bukkit.entity.EntityType;

/**
 * Stores an entity type.
 */
public class EntityTypeFlag extends EnumFlag<EntityType> {

    public EntityTypeFlag(String name, RegionGroup defaultGroup) {
        super(name, EntityType.class, defaultGroup);
    }

    public EntityTypeFlag(String name) {
        super(name, EntityType.class);
    }

    @Override
    public EntityType detectValue(String input) {
        EntityType lowMatch = null;

        for (EntityType type : EntityType.values()) {
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
