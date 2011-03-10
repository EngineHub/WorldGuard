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

package com.sk89q.worldguard;

import java.util.Set;
import java.util.HashSet;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;

public class TestPlayer extends LocalPlayer {
    private String name;
    private Set<String> groups = new HashSet<String>();
    
    public TestPlayer(String name) {
        this.name = name;
    }
    
    public void addGroup(String group) {
        groups.add(group.toLowerCase());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasGroup(String group) {
        return groups.contains(group.toLowerCase());
    }

    @Override
    public Vector getPosition() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void kick(String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void ban(String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void printRaw(String msg) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] getGroups() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public boolean hasPermission(String permission) {
		// TODO Auto-generated method stub
		return false;
	}
}
