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

package com.sk89q.worldguard.bukkit.commands;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */

public class FlagInfo {

    public static enum FlagValueType { STRING, BOOLEAN, INT, FLOAT, DOUBLE, STATE };

    public String name;
    public String subName;
    public FlagValueType type;
    public String flagName;
    public String flagSubName;

    private static List<FlagInfo> flagList;
    static {
        flagList = new ArrayList<FlagInfo>();
        flagList.add(new FlagInfo("msg", "g", FlagValueType.STRING, "msg", "g"));
        flagList.add(new FlagInfo("msg", "f", FlagValueType.STRING, "msg", "f"));
        flagList.add(new FlagInfo("cs", "*", FlagValueType.STRING, "creaturespawn", "*"));
        flagList.add(new FlagInfo("heal", "delay", FlagValueType.INT, "heal", "delay"));
        flagList.add(new FlagInfo("heal", "amount", FlagValueType.INT, "heal", "amount"));
        flagList.add(new FlagInfo("passthrough", null, FlagValueType.STATE, "states", "passthrough"));
        flagList.add(new FlagInfo("build", null, FlagValueType.STATE, "states", "build"));
        flagList.add(new FlagInfo("pvp", null, FlagValueType.STATE, "states", "pvp"));
        flagList.add(new FlagInfo("mobdamage", null, FlagValueType.STATE, "states", "mobdamage"));
        flagList.add(new FlagInfo("creeper", null, FlagValueType.STATE, "states", "creeper"));
        flagList.add(new FlagInfo("tnt", null, FlagValueType.STATE, "states", "tnt"));
        flagList.add(new FlagInfo("lighter", null, FlagValueType.STATE, "states", "lighter"));
        flagList.add(new FlagInfo("firespread", null, FlagValueType.STATE, "states", "firespread"));
        flagList.add(new FlagInfo("lavafirespread", null, FlagValueType.STATE, "states", "lavafirespread"));
        flagList.add(new FlagInfo("chest", null, FlagValueType.STATE, "states", "chest"));
        flagList.add(new FlagInfo("waterflow", null, FlagValueType.STATE, "states", "waterflow"));
    }

    public static FlagInfo getFlagInfo(String name, String subName) {

        System.out.println(name + " " + subName);
        
        for (FlagInfo nfo : flagList) {
            if (name.equals(nfo.name)) {
                if (subName == null && nfo.subName == null) {
                    return nfo;
                } else if (nfo.subName != null) {
                    if (nfo.subName.equals("*")) {
                        return nfo;
                    }
                    else if(subName != null && subName.equals(nfo.subName))
                    {
                        return nfo;
                    }
                } 
            }
        }

        return null;
    }

    public static List<FlagInfo> getFlagInfoList() {
        return flagList;
    }

    public FlagInfo(String name, String subName, FlagValueType type, String flagName, String flagSubName)
    {
        this.name = name;
        this.subName = subName;
        this.type = type;
        this.flagName = flagName;
        this.flagSubName = flagSubName;
    }

}
