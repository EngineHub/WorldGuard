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

import com.sk89q.worldguard.protection.regions.flags.Flags.FlagType;
import com.sk89q.worldguard.protection.regions.flags.info.*;
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

    private static Map<FlagType, RegionFlagInfo> flagByFlagType = new EnumMap<FlagType, RegionFlagInfo>(FlagType.class);
    private static Map<String, RegionFlagInfo> flagByName = new HashMap<String, RegionFlagInfo>();

    public static void registerFlag(RegionFlagInfo info) {

        flagByFlagType.put(info.type, info);
        flagByName.put(info.name, info);
    }

    public static RegionFlagInfo getFlagInfoFromName(String name) {
        return flagByName.get(name);
    }

    public static List<RegionFlagInfo> getFlagInfoList() {
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
}
