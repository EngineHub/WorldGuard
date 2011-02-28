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

import com.sk89q.worldguard.protection.regions.flags.FlagDatabase.FlagType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Holds the flags for a region.
 *
 * @author sk89q
 */
public class RegionFlagContainer {

    private Map<String, String> flags = new HashMap<String, String>();
    private transient Map<FlagType, RegionFlag> flagData = new EnumMap<FlagType, RegionFlag>(FlagType.class);

    public RegionFlag getFlag(FlagType type) {

        if (this.flagData == null) {
            this.initFlagData();
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

        Iterator<Entry<String, String>> iter = this.flags.entrySet().iterator();

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
            this.flags.remove(name);
        } else {
            this.flags.put(name, value);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RegionFlagContainer)) {
            return false;
        }

        RegionFlagContainer other = (RegionFlagContainer) obj;
        return other.flags.equals(this.flags);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.flags != null ? this.flags.hashCode() : 0);
        return hash;
    }

    public BooleanRegionFlag getBooleanFlag(FlagType type) {

        RegionFlag flag = this.getFlag(type);
                
        if (flag instanceof BooleanRegionFlag) {
            return (BooleanRegionFlag) flag;
        } else {
            return null;
        }
    }

    public StateRegionFlag getStateFlag(FlagType type) {

        RegionFlag flag = this.getFlag(type);

        if (flag instanceof StateRegionFlag) {
            return (StateRegionFlag) flag;
        } else {
            return null;
        }
    }

     public IntegerRegionFlag getIntegerFlag(FlagType type) {

        RegionFlag flag = this.getFlag(type);
                
        if (flag instanceof IntegerRegionFlag) {
            return (IntegerRegionFlag) flag;
        } else {
            return null;
        }
    }
     
    public DoubleRegionFlag getDoubleFlag(FlagType type) {

        RegionFlag flag = this.getFlag(type);
                
        if (flag instanceof DoubleRegionFlag) {
            return (DoubleRegionFlag) flag;
        } else {
            return null;
        }
    }
    
    public StringRegionFlag getStringFlag(FlagType type) {

        RegionFlag flag = this.getFlag(type);
                
        if (flag instanceof StringRegionFlag) {
            return (StringRegionFlag) flag;
        } else {
            return null;
        }
    }
    
    public RegionGroupRegionFlag getRegionGroupFlag(FlagType type) {

        RegionFlag flag = this.getFlag(type);
                
        if (flag instanceof RegionGroupRegionFlag) {
            return (RegionGroupRegionFlag) flag;
        } else {
            return null;
        }
    }
    
    public LocationRegionFlag getLocationFlag(FlagType type) {

        RegionFlag flag = this.getFlag(type);
                
        if (flag instanceof LocationRegionFlag) {
            return (LocationRegionFlag) flag;
        } else {
            return null;
        }
    }


}
