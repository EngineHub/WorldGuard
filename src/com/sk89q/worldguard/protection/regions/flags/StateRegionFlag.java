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

/**
 *
 * @author Michael
 */
public final class StateRegionFlag extends RegionFlag {

    private State value;

    public StateRegionFlag(RegionFlagContainer container, RegionFlagInfo info, String value) {
        super(container, info);
        this.setValue(value);
    }

    public StateRegionFlag() {
        super(null, null);
    }

    public void setValue(State newValue) {
        this.value = newValue;
        this.updataContainer();
    }

    public State getValue() {
        return this.value;
    }

    public State getValue(State def) {
        return this.value != null ? this.value : def;
    }

    public boolean setValue(String newValue) {

        if (newValue == null) {
            this.value = null;
        } else {
            try {
                this.value = State.valueOf(newValue);
            } catch (Exception e) {
                this.value = null;
                this.updataContainer();
                return false;
            }
        }

        this.updataContainer();
        return true;
    }

    private void updataContainer() {
        if (this.container != null) {
            this.container.internalSetValue(info.name, this.value != null ? this.value.toString() : null);
        }
    }

    @Override
    public boolean hasValue() {
        return this.value != null;
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return this.value.toString();
        } else {
            return "-";
        }
    }
}
