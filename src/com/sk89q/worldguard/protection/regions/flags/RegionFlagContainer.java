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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Holds the sFlags for a region.
 *
 * @author sk89q
 */
public class RegionFlagContainer {

    private Map<String, String> sFlags = new HashMap<String, String>();
    private transient Map<FlagType, RegionFlag> flagData = new EnumMap<FlagType, RegionFlag>(FlagType.class);
    private transient boolean hasInit = false;

    private RegionFlag getFlag(FlagType type) {

        if (!this.hasInit) {
            this.initFlagData();
            this.hasInit = true;
        }

        RegionFlag ret = this.flagData.get(type);

        if (ret == null) {
            ret = FlagDatabase.getNewInstanceOf(type, null, this);

            if (ret != null) {
                this.flagData.put(type, ret);
            }
        }

        return ret;
    }

    private void initFlagData() {

        Iterator<Entry<String, String>> iter = this.sFlags.entrySet().iterator();

        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();

            RegionFlag rflag = FlagDatabase.getNewInstanceOf(entry.getKey(), entry.getValue(), this);
            if (rflag == null) {
                iter.remove();
            } else {
                this.flagData.put(rflag.info.type, rflag);
            }
        }
    }

    protected void internalSetValue(String name, String value) {
        if (name == null) {
            return;
        }

        if (value == null) {
            this.sFlags.remove(name);
        } else {
            this.sFlags.put(name, value);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RegionFlagContainer)) {
            return false;
        }

        RegionFlagContainer other = (RegionFlagContainer) obj;
        return other.sFlags.equals(this.sFlags);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.sFlags != null ? this.sFlags.hashCode() : 0);
        return hash;
    }

    public RegionFlag getFlag(RegionFlagInfo info) {
        return this.getFlag(info.type);
    }

    public BooleanRegionFlag getBooleanFlag(BooleanRegionFlagInfo info) {

        RegionFlag flag = this.getFlag(info.type);

        if (flag instanceof BooleanRegionFlag) {
            return (BooleanRegionFlag) flag;
        } else {
            return null;
        }
    }

    public StateRegionFlag getStateFlag(StateRegionFlagInfo info) {

        RegionFlag flag = this.getFlag(info.type);

        if (flag instanceof StateRegionFlag) {
            return (StateRegionFlag) flag;
        } else {
            return null;
        }
    }

    public IntegerRegionFlag getIntegerFlag(IntegerRegionFlagInfo info) {

        RegionFlag flag = this.getFlag(info.type);

        if (flag instanceof IntegerRegionFlag) {
            return (IntegerRegionFlag) flag;
        } else {
            return null;
        }
    }

    public DoubleRegionFlag getDoubleFlag(DoubleRegionFlagInfo info) {

        RegionFlag flag = this.getFlag(info.type);

        if (flag instanceof DoubleRegionFlag) {
            return (DoubleRegionFlag) flag;
        } else {
            return null;
        }
    }

    public StringRegionFlag getStringFlag(StringRegionFlagInfo info) {

        RegionFlag flag = this.getFlag(info.type);

        if (flag instanceof StringRegionFlag) {
            return (StringRegionFlag) flag;
        } else {
            return null;
        }
    }

    public RegionGroupRegionFlag getRegionGroupFlag(RegionGroupRegionFlagInfo info) {

        RegionFlag flag = this.getFlag(info.type);

        if (flag instanceof RegionGroupRegionFlag) {
            return (RegionGroupRegionFlag) flag;
        } else {
            return null;
        }
    }

    public LocationRegionFlag getLocationFlag(LocationRegionFlagInfo info) {

        RegionFlag flag = this.getFlag(info.type);

        if (flag instanceof LocationRegionFlag) {
            return (LocationRegionFlag) flag;
        } else {
            return null;
        }
    }
}
