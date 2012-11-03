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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 *
 * @author sk89q
 */
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
            return BukkitUtil.toLocation(player.getLocation());
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

                    return new Location(
                            BukkitUtil.getLocalWorld(world),
                            new Vector(
                                    x,
                                    y,
                                    z
                            ),
                            yaw, pitch
                    );
                } catch (NumberFormatException ignored) {
                }
            }

            throw new InvalidFlagFormat("Expected 'here' or x,y,z.");
        }
    }

    @Override
    public Location unmarshal(Object o) {
        if (o instanceof Map<?, ?>) {
            Map<?, ?> map  = (Map<?, ?>) o;

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

            World bukkitWorld = Bukkit.getServer().getWorld((String) rawWorld);
            LocalWorld world = BukkitUtil.getLocalWorld(bukkitWorld);
            Vector position = new Vector(toNumber(rawX), toNumber(rawY), toNumber(rawZ));
            float yaw = (float) toNumber(rawYaw);
            float pitch = (float) toNumber(rawPitch);

            return new Location(world, position, yaw, pitch);
        }

        return null;
    }

    @Override
    public Object marshal(Location o) {
        Vector position = o.getPosition();
        Map<String, Object> vec = new HashMap<String, Object>();
        vec.put("world", o.getWorld().getName());
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
