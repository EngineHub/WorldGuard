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

import com.sk89q.worldedit.math.Vector3;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores a vector.
 */
public class VectorFlag extends Flag<Vector3> {
    
    public VectorFlag(String name, RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    public VectorFlag(String name) {
        super(name);
    }

    @Override
    public Vector3 parseInput(FlagContext context) throws InvalidFlagFormatException {
        String input = context.getUserInput();

        if ("here".equalsIgnoreCase(input)) {
            return context.getPlayerSender().getLocation().toVector();
        } else {
            String[] split = input.split(",");
            if (split.length == 3) {
                try {
                    return Vector3.at(
                            Double.parseDouble(split[0]),
                            Double.parseDouble(split[1]),
                            Double.parseDouble(split[2])
                    );
                } catch (NumberFormatException ignored) {
                }
            }

            throw new InvalidFlagFormatException("Expected 'here' or x,y,z.");
        }
    }

    @Override
    public Vector3 unmarshal(Object o) {
        if (o instanceof Map<?, ?>) {
            Map<?, ?> map  = (Map<?, ?>) o;

            Object rawX = map.get("x");
            Object rawY = map.get("y");
            Object rawZ = map.get("z");

            if (rawX == null || rawY == null || rawZ == null) {
                return null;
            }

            return Vector3.at(toNumber(rawX), toNumber(rawY), toNumber(rawZ));
        }

        return null;
    }

    @Override
    public Object marshal(Vector3 o) {
        Map<String, Object> vec = new HashMap<>();
        vec.put("x", o.getX());
        vec.put("y", o.getY());
        vec.put("z", o.getZ());
        return vec;
    }

    private double toNumber(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Long) {
            return (Long) o;
        } else if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Double) {
            return (Double) o;
        } else {
            return 0;
        }
    }
}
