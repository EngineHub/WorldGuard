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

package com.sk89q.worldguard.bukkit;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;

public class BukkitPlayer extends LocalPlayer {
    private Player player;
    private WorldGuardPlugin plugin;
    
    public BukkitPlayer(WorldGuardPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName();
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
        player.kickPlayer(msg);
    }

    @Override
    public List<String> getGroups() {
        return plugin.getGroups(player);
    }

}
