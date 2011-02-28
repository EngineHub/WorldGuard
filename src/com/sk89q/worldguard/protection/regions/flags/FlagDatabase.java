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
package com.sk89q.worldguard.protection.regions.flags;

import com.sk89q.worldguard.protection.regions.flags.RegionFlag.FlagDataType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael
 */
public class FlagDatabase {

    public enum FlagType {

        PASSTHROUGH, BUILD, PVP, MOB_DAMAGE, CREEPER_EXPLOSION,
        TNT, LIGHTER, FIRE_SPREAD, LAVA_FIRE, CHEST_ACCESS, WATER_FLOW,
        LEVER_AND_BUTTON, GREET_MESSAGE, FAREWELL_MESSAGE, DENY_SPAWN,
        HEAL_DELAY, HEAL_AMOUNT, TELE_LOC, TELE_PERM, SPAWN_LOC, SPAWN_PERM,
        BUYABLE, PRICE

    };

    static {
        registerFlag("passthrough", FlagType.PASSTHROUGH, FlagDataType.STATE);
        registerFlag("build", FlagType.BUILD, FlagDataType.STATE);
        registerFlag("pvp", FlagType.PVP, FlagDataType.STATE);
        registerFlag("mobdamage", FlagType.MOB_DAMAGE, FlagDataType.STATE);
        registerFlag("creeperexp", FlagType.CREEPER_EXPLOSION, FlagDataType.STATE);
        registerFlag("tnt", FlagType.TNT, FlagDataType.STATE);
        registerFlag("lighter", FlagType.LIGHTER, FlagDataType.STATE);
        registerFlag("firespread", FlagType.FIRE_SPREAD, FlagDataType.STATE);
        registerFlag("lavafire", FlagType.LAVA_FIRE, FlagDataType.STATE);
        registerFlag("chest", FlagType.CHEST_ACCESS, FlagDataType.STATE);
        registerFlag("waterflow", FlagType.WATER_FLOW, FlagDataType.STATE);
        registerFlag("leverandbutton", FlagType.LEVER_AND_BUTTON, FlagDataType.STATE);

        registerFlag("buyable", FlagType.BUYABLE, FlagDataType.BOOLEAN);

        registerFlag("healdelay", FlagType.HEAL_DELAY, FlagDataType.INTEGER);
        registerFlag("healamount", FlagType.HEAL_AMOUNT, FlagDataType.INTEGER);
        
        registerFlag("price", FlagType.PRICE, FlagDataType.DOUBLE);

        registerFlag("gmsg", FlagType.GREET_MESSAGE, FlagDataType.STRING);
        registerFlag("fmsg", FlagType.FAREWELL_MESSAGE, FlagDataType.STRING);
        registerFlag("denyspawn", FlagType.DENY_SPAWN, FlagDataType.STRING);

        registerFlag("teleloc", FlagType.TELE_LOC, FlagDataType.LOCATION);
        registerFlag("spawnloc", FlagType.SPAWN_LOC, FlagDataType.LOCATION);

        registerFlag("teleperm", FlagType.TELE_PERM, FlagDataType.REGIONGROUP);
        registerFlag("spawnperm", FlagType.SPAWN_PERM, FlagDataType.REGIONGROUP);
    }


    
    private static void registerFlag(String name, FlagType type, FlagDataType dataType) {

        RegionFlagInfo info = new RegionFlagInfo(name, type, dataType);
        flagByFlagType.put(info.type, info);
        flagByName.put(info.name, info);
    }


    public static RegionFlagInfo getFlagInfoFromName(String name)
    {
        return flagByName.get(name);
    }

    public static List<RegionFlagInfo> getFlagInfoList()
    {
        List<RegionFlagInfo> list = new ArrayList<RegionFlagInfo>();
        list.addAll(flagByFlagType.values());
        return list;
    }


    public static RegionFlag getNewInstanceOf(FlagType type, String value, RegionFlagContainer container) {
        RegionFlagInfo info = flagByFlagType.get(type);

        if (info == null) {
            return null;
        }

        switch (info.dataType) {
            case BOOLEAN:
                return new BooleanRegionFlag(container, info, value);
            case STATE:
                return new StateRegionFlag(container, info, value);
            case INTEGER:
                return new IntegerRegionFlag(container, info, value);
            case DOUBLE:
                return new DoubleRegionFlag(container, info, value);
            case STRING:
                return new StringRegionFlag(container, info, value);
            case LOCATION:
                return new LocationRegionFlag(container, info, value);
            case REGIONGROUP:
                return new RegionGroupRegionFlag(container, info, value);
            default:
                return null;
        }

    }

    public static RegionFlag getNewInstanceOf(String name, String value, RegionFlagContainer container) {
        RegionFlagInfo info = flagByName.get(name);

        if (info == null) {
            return null;
        }

        switch (info.dataType) {
            case BOOLEAN:
                return new BooleanRegionFlag(container, info, value);
            case STATE:
                return new StateRegionFlag(container, info, value);
            case INTEGER:
                return new IntegerRegionFlag(container, info, value);
            case DOUBLE:
                return new DoubleRegionFlag(container, info, value);
            case STRING:
                return new StringRegionFlag(container, info, value);
            case LOCATION:
                return new LocationRegionFlag(container, info, value);
            case REGIONGROUP:
                return new RegionGroupRegionFlag(container, info, value);
            default:
                return null;
        }

    }
    
    private static Map<FlagType, RegionFlagInfo> flagByFlagType = new EnumMap<FlagType, RegionFlagInfo>(FlagType.class);
    private static Map<String, RegionFlagInfo> flagByName = new HashMap<String, RegionFlagInfo>();
}
