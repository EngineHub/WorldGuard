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
public class StringRegionFlag extends RegionFlag {

    private String value;

    public StringRegionFlag(RegionFlagContainer container, RegionFlagInfo info, String value) {

        super(container, info);
        this.value = value;
    }

    public StringRegionFlag() {
        super(null, null);
    }

    public boolean setValue(String newValue) {
        this.value = newValue;
        this.updataContainer();
        return true;
    }

    public String getValue() {
        return this.value;
    }

    public String getValue(String def) {
        return this.value != null ? this.value : def;
    }

    private void updataContainer() {
        if (this.container != null) {
            this.container.internalSetValue(info.name, this.value);
        }
    }

    @Override
    public boolean hasValue() {
        return this.value != null;
    }

     @Override
    public String toString() {
        if(this.value != null)
        {
            return this.value;
        }
        else
        {
            return "-";
        }
    }
}
