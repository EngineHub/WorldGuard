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

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class LocationFlag extends Flag<Location> {

    public LocationFlag(String name, RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    public LocationFlag(String name) {
        super(name);
    }

    @Override
    public Location parseInput(WorldGuardPlugin plugin, CommandSender sender, String input) throws InvalidFlagFormat {
        input = input.trim();

        final Player player;
        try {
            player = plugin.checkPlayer(sender);
        } catch (CommandException e) {
            throw new InvalidFlagFormat(e.getMessage());
        }

        if ("here".equalsIgnoreCase(input)) {
            return toLazyLocation(player.getLocation());
        } else if ("none".equalsIgnoreCase(input)) {
            return null;
        } else {
            String[] split = input.split(",");
            if (split.length >= 3) {
                try {
                    final World world = player.getWorld();
                    final double x = Double.parseDouble(split[0]);
                    final double y = Double.parseDouble(split[1]);
                    final double z = Double.parseDouble(split[2]);
                    final float yaw = split.length < 4 ? 0 : Float.parseFloat(split[3]);
                    final float pitch = split.length < 5 ? 0 : Float.parseFloat(split[4]);

                    return new LazyLocation(world.getName(), new Vector(x, y, z), yaw, pitch);
                } catch (NumberFormatException ignored) {
                }
            }

            throw new InvalidFlagFormat("Expected 'here' or x,y,z.");
        }
    }

    private Location toLazyLocation(org.bukkit.Location location) {
        return new LazyLocation(location.getWorld().getName(), BukkitUtil.toVector(location), location.getYaw(), location.getPitch());
    }

    @Override
    public Location unmarshal(Object o) {
        if (o instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) o;

            Object rawWorld = map.get("world");
            if (rawWorld == null) return null;

            Object rawX = map.get("x");
            if (rawX == null) return null;

            Object rawY = map.get("y");
            if (rawY == null) return null;

            Object rawZ = map.get("z");
            if (rawZ == null) return null;

            Object rawYaw = map.get("yaw");
            if (rawYaw == null) return null;

            Object rawPitch = map.get("pitch");
            if (rawPitch == null) return null;

            Vector position = new Vector(toNumber(rawX), toNumber(rawY), toNumber(rawZ));
            float yaw = (float) toNumber(rawYaw);
            float pitch = (float) toNumber(rawPitch);

            return new LazyLocation(String.valueOf(rawWorld), position, yaw, pitch);
        }

        return null;
    }

    @Override
    public Object marshal(Location o) {
        Vector position = o.getPosition();
        Map<String, Object> vec = new HashMap<String, Object>();
        if (o instanceof LazyLocation) {
            vec.put("world", ((LazyLocation) o).getWorldName());
        } else {
            try {
                vec.put("world", o.getWorld().getName());
            } catch (NullPointerException e) {
                return null;
            }
        }
        vec.put("x", position.getX());
        vec.put("y", position.getY());
        vec.put("z", position.getZ());
        vec.put("yaw", o.getYaw());
        vec.put("pitch", o.getPitch());
        return vec;
    }

    private double toNumber(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            return 0;
        }

    }
}
