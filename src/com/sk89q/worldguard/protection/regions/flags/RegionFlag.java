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

import com.sk89q.worldguard.protection.regions.flags.info.RegionFlagInfo;

/**
 *
 * @author Michael
 */
public abstract class RegionFlag {

    public enum RegionGroup {

        ALL, MEMBER, OWNER
    };

    public enum State {

        NONE, ALLOW, DENY
    };

    public enum FlagDataType {

        BOOLEAN, STATE, INTEGER, DOUBLE, STRING, LOCATION, REGIONGROUP
    };

    
    protected RegionFlagContainer container;
    protected RegionFlagInfo info;

    public RegionFlag(RegionFlagContainer container, RegionFlagInfo info) {
        this.container = container;
        this.info = info;
    }

    public RegionFlagInfo getInfo() {
        return this.info;
    }

    public String getName() {
        return this.info.name;
    }

    public abstract boolean hasValue();

    @Override
    public abstract String toString();

    public abstract boolean setValue(String newVal);
}
