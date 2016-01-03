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

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

/**
 * A location that stores the name of the world in case the world is unloaded.
 */
class LazyLocation extends Location {

    private final String worldName;

    @Nullable
    private static LocalWorld findWorld(String worldName) {
        return BukkitUtil.getLocalWorld(Bukkit.getServer().getWorld(worldName));
    }

    public LazyLocation(String worldName, Vector position, float yaw, float pitch) {
        super(findWorld(worldName), position, yaw, pitch);
        this.worldName = worldName;
    }

    public LazyLocation(String worldName, Vector position) {
        super(findWorld(worldName), position);
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    public LazyLocation setAngles(float yaw, float pitch) {
        return new LazyLocation(worldName, getPosition(), yaw, pitch);
    }

    public LazyLocation setPosition(Vector position) {
        return new LazyLocation(worldName, position, getYaw(), getPitch());
    }

    public LazyLocation add(Vector other) {
        return this.setPosition(getPosition().add(other));
    }

    public LazyLocation add(double x, double y, double z) {
        return this.setPosition(getPosition().add(x, y, z));
    }

}
