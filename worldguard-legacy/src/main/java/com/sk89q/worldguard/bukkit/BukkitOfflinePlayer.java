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

package com.sk89q.worldguard.bukkit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

class BukkitOfflinePlayer extends LocalPlayer {

    private final OfflinePlayer player;

    BukkitOfflinePlayer(OfflinePlayer offlinePlayer) {
        this.player = offlinePlayer;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public boolean hasGroup(String group) {
        return false;
    }

    @Override
    public Vector getPosition() {
        return Vector.ZERO;
    }

    @Override
    public void kick(String msg) {
    }

    @Override
    public void ban(String msg) {
    }

    @Override
    public void printRaw(String msg) {
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public boolean hasPermission(String perm) {
        return false;
    }

}
