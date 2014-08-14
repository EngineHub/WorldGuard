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

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitPlayer extends LocalPlayer {

    private final WorldGuardPlugin plugin;
    private final Player player;
    
    public BukkitPlayer(WorldGuardPlugin plugin, Player player) {
        checkNotNull(plugin);
        checkNotNull(player);

        this.plugin = plugin;
        this.player = player;
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
        return plugin.inGroup(player, group);
    }

    @Override
    public Vector getPosition() {
        Location loc = player.getLocation();
        return new Vector(loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public void kick(String msg) {
        player.kickPlayer(msg);
    }

    @Override
    public void ban(String msg) {
        player.setBanned(true);
        player.kickPlayer(msg);
    }

    @Override
    public String[] getGroups() {
        return plugin.getGroups(player);
    }

    @Override
    public void printRaw(String msg) {
        player.sendMessage(msg);
    }

    @Override
    public boolean hasPermission(String perm) {
        return plugin.hasPermission(player, perm);
    }

    public Player getPlayer() {
        return player;
    }

}
