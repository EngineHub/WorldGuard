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

package com.sk89q.worldguard.domains;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sk89q.worldguard.LocalPlayer;

public class GroupDomain implements Domain {
    private Set<String> groups;

    public GroupDomain() {
        this.groups = new LinkedHashSet<String>();
    }

    public GroupDomain(String[] groups) {
        this.groups = new LinkedHashSet<String>(Arrays.asList(groups));
    }

    public void addGroup(String name) {
        groups.add(name);
    }

    public boolean contains(LocalPlayer player) {
        for (String group : groups) {
            if (player.hasGroup(group)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean contains(String playerName) {
        return false; // GroupDomains can't contain player names.
    }

    public int size() {
        return groups.size();
    }
}
