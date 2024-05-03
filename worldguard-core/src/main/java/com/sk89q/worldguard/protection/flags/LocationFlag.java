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

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

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
    public Location parseInput(FlagContext context) throws InvalidFlagFormatException {
        String input = context.getUserInput();
        Player player = context.getPlayerSender();

        Location loc = null;
        if ("here".equalsIgnoreCase(input)) {
            Location playerLoc = player.getLocation();
            loc = new LazyLocation(((World) playerLoc.getExtent()).getName(),
                    playerLoc.toVector(), playerLoc.getYaw(), playerLoc.getPitch());
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

                    loc = new LazyLocation(world.getName(), Vector3.at(x, y, z), yaw, pitch);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (loc != null) {
            Object obj = context.get("region");
            if (obj instanceof ProtectedRegion) {
                ProtectedRegion rg = (ProtectedRegion) obj;
                if (WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(player.getWorld()).boundedLocationFlags) {
                    if (!rg.contains(loc.toVector().toBlockPoint())) {
                        if (new RegionPermissionModel(player).mayOverrideLocationFlagBounds(rg)) {
                            player.printDebug("WARNING: Flag location is outside of region.");
                        } else {
                            // no permission
                            throw new InvalidFlagFormatException("You can't set that flag outside of the region boundaries.");
                        }
                    }
                    // clamp height to world limits
                    loc.setPosition(loc.toVector().clampY(0, player.getWorld().getMaxY()));
                    return loc;
                }
            }
            return loc;
        }
        throw new InvalidFlagFormatException("Expected 'here' or x,y,z.");
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

            Vector3 position = Vector3.at(toNumber(rawX), toNumber(rawY), toNumber(rawZ));
            float yaw = (float) toNumber(rawYaw);
            float pitch = (float) toNumber(rawPitch);

            return new LazyLocation(String.valueOf(rawWorld), position, yaw, pitch);
        }

        return null;
    }

    @Override
    public Object marshal(Location o) {
        Vector3 position = o.toVector();
        Map<String, Object> vec = new HashMap<>();
        if (o instanceof LazyLocation) {
            vec.put("world", ((LazyLocation) o).getWorldName());
        } else {
            try {
                if (o.getExtent() instanceof World) {
                    vec.put("world", ((World) o.getExtent()).getName());
                }
            } catch (NullPointerException e) {
                return null;
            }
        }
        vec.put("x", position.x());
        vec.put("y", position.y());
        vec.put("z", position.z());
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
