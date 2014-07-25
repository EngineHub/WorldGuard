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

package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultDomain implements Domain {
    private final Set<String> groups;
    private final Set<String> players;
    
    public DefaultDomain() {
        this.groups = new CopyOnWriteArraySet<String>();
        this.players = new CopyOnWriteArraySet<String>();
    }
    
    public void addPlayer(String name) {
        players.add(name.toLowerCase());
    }
    
    public void addPlayer(LocalPlayer player) {
        players.add(player.getName().toLowerCase());
    }
    
    public void removePlayer(String name) {
        players.remove(name.toLowerCase());
    }
    
    public void removePlayer(LocalPlayer player) {
        players.remove(player.getName().toLowerCase());
    }
    
    public void addGroup(String name) {
        groups.add(name.toLowerCase());
    }
    
    public void removeGroup(String name) {
        groups.remove(name.toLowerCase());
    }
    
    public Set<String> getGroups() {
        return groups;
    }
    
    public Set<String> getPlayers() {
        return players;
    }

    @Override
    public boolean contains(LocalPlayer player) {
        if (contains(player.getName())) {
            return true;
        }

        for (String group : groups) {
            if (player.hasGroup(group)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean contains(String playerName) {
        return players.contains(playerName.toLowerCase());
    }

    public int size() {
        return groups.size() + players.size();
    }
    
    public String toPlayersString() {
        StringBuilder str = new StringBuilder();
        List<String> output = new ArrayList<String>(players);
        Collections.sort(output, String.CASE_INSENSITIVE_ORDER);
        for (Iterator<String> it = output.iterator(); it.hasNext();) {
            str.append(it.next());
            if (it.hasNext()) {
                str.append(", ");
            }
        }
        return str.toString();
    }
    
    public String toGroupsString() {
        StringBuilder str = new StringBuilder();
        for (Iterator<String> it = groups.iterator(); it.hasNext(); ) {
            str.append("*");
            str.append(it.next());
            if (it.hasNext()) {
                str.append(", ");
            }
        }
        return str.toString();
    }
    
    public String toUserFriendlyString() {
        StringBuilder str = new StringBuilder();
        if (players.size() > 0) {
            str.append(toPlayersString());
        }
        
        if (groups.size() > 0) {
            if (str.length() > 0) {
                str.append("; ");
            }
            
            str.append(toGroupsString());
        }
        
        return str.toString();
    }

    public void removeAll() {
        groups.clear();
        players.clear();
    }
}
