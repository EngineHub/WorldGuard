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
package com.sk89q.worldguard.protection.regions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Server;

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
    public static final String FLAG_WATER_FLOW = "w";

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
        } else if (flag.equals(FLAG_WATER_FLOW)) {
            return "water flow";
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
        } else if (name.equalsIgnoreCase("waterflow")) {
            return FLAG_WATER_FLOW;
        } else {
            return null;
        }
    }
    private Map<String, Map<String, String>> flags = new HashMap<String, Map<String, String>>();

    public Map<String, String> getFlagData(String name) {
        Map<String, String> ret = flags.get(name);
        if (ret == null) {
            ret = new HashMap<String, String>();
            flags.put(name, ret);
        }

        return ret;
    }

    public Set<Map.Entry<String, State>> entrySet() {

        Map<String, State> ret = new HashMap<String, State>();

        for (Map.Entry<String, String> entry : getFlagData("states").entrySet()) {
            ret.put(entry.getKey(), State.valueOf(entry.getValue()));
        }

        return ret.entrySet();
    }

    /**
     * Gets the State value of a state flag
     *
     * @param name
     * @param subname
     * @return State
     */
    public State getStateFlag(String name, String subname) {
        String value = getFlagData(name).get(subname);
        if (value == null) {
            return State.NONE;
        }
        State state = State.NONE;
        try {
            state = State.valueOf(value);
            if (state == null) {
                return State.NONE;
            }
        } catch (Exception e) {
            return State.NONE;
        }
        return state;
    }

    /**
     * Gets the State value of a state flag
     *
     * @param flag
     * @return State
     */
    public State getStateFlag(String flag) {
        return getStateFlag("states", flag);
    }

    /**
     * Sets the State value of a state flag
     *
     * @param flag
     * @param state
     */
    public void setFlag(String name, String subname, State state) {
        if (state == State.NONE) {
            getFlagData(name).remove(subname);
        } else {
            getFlagData(name).put(subname, state.toString());
        }
    }

    /**
     * Sets the State value of a state flag
     * 
     * @param flag
     * @param state
     */
    public void setFlag(String flag, State state) {
        setFlag("states", flag, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AreaFlags)) {
            return false;
        }

        AreaFlags other = (AreaFlags) obj;
        return other.flags.equals(this.flags);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.flags != null ? this.flags.hashCode() : 0);
        return hash;
    }

    public void setFlag(String name, String subname, String value) {
        if(subname == null)
        {
            return;
        }
        if (value == null) {
            getFlagData(name).remove(subname);
        } else {
            getFlagData(name).put(subname, value);
        }
    }

    public void setFlag(String name, String subname, Boolean value) {
         setFlag(name, subname, value.toString());
    }

    public void setFlag(String name, String subname, Integer value) {
         setFlag(name, subname, value.toString());
    }

    public void setFlag(String name, String subname, Float value) {
         setFlag(name, subname, value.toString());
    }

    public void setFlag(String name, String subname, Double value) {
         setFlag(name, subname, value.toString());
    }

    public String getFlag(String name, String subname) {
        return getFlagData(name).get(subname);
    }

    public String getFlag(String name, String subname, String defaultValue) {
        String data = getFlagData(name).get(subname);
        return data != null ? data : defaultValue;
    }

    public Boolean getBooleanFlag(String name, String subname) {
        String data = getFlagData(name).get(subname);
        return data != null ? Boolean.valueOf(data) : null;
    }

    public Boolean getBooleanFlag(String name, String subname, boolean defaultValue) {
        String data = getFlagData(name).get(subname);
        return data != null ? Boolean.valueOf(data) : defaultValue;
    }

    public Integer getIntFlag(String name, String subname) {
        String data = getFlagData(name).get(subname);
        try {
            return data != null ? Integer.valueOf(data) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getIntFlag(String name, String subname, int defaultValue) {
        String data = getFlagData(name).get(subname);
        try {
            return data != null ? Integer.valueOf(data) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Float getFloatFlag(String name, String subname) {
        String data = getFlagData(name).get(subname);
        try {
            return data != null ? Float.valueOf(data) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Float getFloatFlag(String name, String subname, float defaultValue) {
        String data = getFlagData(name).get(subname);
        try {
            return data != null ? Float.valueOf(data) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Double getDoubleFlag(String name, String subname) {
        String data = getFlagData(name).get(subname);
        try {
            return data != null ? Double.valueOf(data) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Double getDoubleFlag(String name, String subname, double defaultValue) {
        String data = getFlagData(name).get(subname);
        try {
            return data != null ? Double.valueOf(data) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Location getLocationFlag(Server server, String name) {
        try {
            Double x = Double.valueOf(getFlagData(name).get("x"));
            Double y = Double.valueOf(getFlagData(name).get("y"));
            Double z = Double.valueOf(getFlagData(name).get("z"));
            Float yaw = Float.valueOf(getFlagData(name).get("yaw"));
            Float pitch = Float.valueOf(getFlagData(name).get("pitch"));
            String worldName = getFlagData(name).get("world");

            Location l = new Location(server.getWorld(worldName), x, y, z, yaw, pitch);
            return l;
        } catch (Exception e) {
            return null;
        }
    }

    public void setLocationFlag(String name, Location l) {
        if (l != null) {
            getFlagData(name).put("x", new Double(l.getX()).toString());
            getFlagData(name).put("y", new Double(l.getY()).toString());
            getFlagData(name).put("z", new Double(l.getZ()).toString());
            getFlagData(name).put("yaw", new Float(l.getYaw()).toString());
            getFlagData(name).put("pitch", new Float(l.getPitch()).toString());
            getFlagData(name).put("world", l.getWorld().getName());
        } else {
            getFlagData(name).put("x", null);
            getFlagData(name).put("y", null);
            getFlagData(name).put("z", null);
            getFlagData(name).put("yaw", null);
            getFlagData(name).put("pitch", null);
            getFlagData(name).put("world", null);
        }
    }
}
