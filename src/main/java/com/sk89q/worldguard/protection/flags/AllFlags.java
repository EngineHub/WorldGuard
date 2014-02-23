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

package com.sk89q.worldguard.protection.flags;


public final class AllFlags {

    public enum FlagType {
        BOOLEAN,
        COMMANDSTRING,
        DOUBLE,
        ENTITYTYPE,
        ENUM,
        INTEGER,
        LOCATION,
        REGIONGROUP,
        SET,
        STATE,
        STRING,
        VECTOR
    }
    // TODO add support for custom EnumFlag and SetFlag

    private AllFlags() {
    }

    /**
     * Get a list of all flags including default and custom flags
     * 
     * @return list of flags
     */
    public static FlagsList getFlags() {
        FlagsList flags = new FlagsList();
        flags.addAll(DefaultFlag.getFlags());
        flags.addAll(CustomFlags.getAllCustomFlags());
        return flags;
    }

    /**
     * Try to match the flag with the given ID using a fuzzy name match.
     *
     * @param id the flag ID
     * @return a flag, or null
     */
    public static Flag<?> fuzzyMatchFlag(String id) {
        for (Flag<?> flag : AllFlags.getFlags()) {
            if (flag.getName().replace("-", "").equalsIgnoreCase(id.replace("-", ""))) {
                return flag;
            }
        }

        return null;
    }
}
