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

package com.sk89q.worldguard.protection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds the flags for a region.
 * 
 * @author sk89q
 */
public class AreaFlags {
    public enum State {
        NONE,
        ALLOW,
        DENY,
    };

    public static final String FLAG_PASSTHROUGH = "z";
    public static final String FLAG_BUILD = "b";
    public static final String FLAG_PVP = "p";
    public static final String FLAG_MOB_DAMAGE = "m";
    public static final String FLAG_CREEPER_EXPLOSION = "c";
    public static final String FLAG_TNT = "t";
    public static final String FLAG_LIGHTER = "l";
    public static final String FLAG_FIRE_SPREAD = "f";
    public static final String FLAG_LAVA_FIRE = "F";
    public static final String FLAG_CHEST_ACCESS = "C";
    
    /**
     * Get the user-friendly name of a flag. If a name isn't known, then
     * the flag is returned unchanged.
     * 
     * @param flag
     * @return
     */
    public static String getFlagName(String flag) {
        if (flag.equals(FLAG_PASSTHROUGH)) {
            return "passthrough";
        } else if (flag.equals(FLAG_BUILD)) {
            return "build";
        } else if (flag.equals(FLAG_PVP)) {
            return "PvP";
        } else if (flag.equals(FLAG_MOB_DAMAGE)) {
            return "mob damage";
        } else if (flag.equals(FLAG_CREEPER_EXPLOSION)) {
            return "creeper explosion";
        } else if (flag.equals(FLAG_TNT)) {
            return "TNT";
        } else if (flag.equals(FLAG_LIGHTER)) {
            return "lighter";
        } else if (flag.equals(FLAG_FIRE_SPREAD)) {
            return "fire spread";
        } else if (flag.equals(FLAG_LAVA_FIRE)) {
            return "lava fire spread";
        } else if (flag.equals(FLAG_CHEST_ACCESS)) {
            return "chest access";
        } else {
            return flag;
        }
    }
    
    /**
     * Gets a flag from an alias. May return null.
     * 
     * @param name
     * @return
     */
    public static String fromAlias(String name) {
        if (name.equalsIgnoreCase("passthrough")) {
            return FLAG_PASSTHROUGH;
        } else if (name.equalsIgnoreCase("build")) {
            return FLAG_BUILD;
        } else if (name.equalsIgnoreCase("pvp")) {
            return FLAG_PVP;
        } else if (name.equalsIgnoreCase("mobdamage")) {
            return FLAG_MOB_DAMAGE;
        } else if (name.equalsIgnoreCase("creeper")) {
            return FLAG_CREEPER_EXPLOSION;
        } else if (name.equalsIgnoreCase("tnt")) {
            return FLAG_TNT;
        } else if (name.equalsIgnoreCase("lighter")) {
            return FLAG_LIGHTER;
        } else if (name.equalsIgnoreCase("firespread")) {
            return FLAG_FIRE_SPREAD;
        } else if (name.equalsIgnoreCase("lavafirespread")) {
            return FLAG_LAVA_FIRE;
        } else if (name.equalsIgnoreCase("chest")) {
            return FLAG_CHEST_ACCESS;
        } else {
            return null;
        }
    }
    
    private Map<String, State> states = new HashMap<String, State>();
    
    public State get(String flag) {
        State state = states.get(flag);
        if (state == null) {
            return State.NONE;
        }
        return state;
    }
    
    public void set(String flag, State state) {
        if (state == State.NONE) {
            states.remove(flag);
        } else {
            states.put(flag, state);
        }
    }
    
    public Set<Map.Entry<String, State>> entrySet() {
        return states.entrySet();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AreaFlags)) {
            return false;
        }
        
        AreaFlags other = (AreaFlags)obj;
        return other.states.equals(this);
    }
}
