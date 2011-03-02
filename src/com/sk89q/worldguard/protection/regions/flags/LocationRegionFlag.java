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
import org.bukkit.Location;
import org.bukkit.Server;

/**
 *
 * @author Michael
 */
public final class LocationRegionFlag extends RegionFlag {

    private String worldName;
    private double x, y, z;
    private float yaw, pitch;

    public LocationRegionFlag(RegionFlagContainer container, RegionFlagInfo info, String value) {
        super(container, info);
        this.setValue(value);
    }

    public LocationRegionFlag() {
        super(null, null);
    }

    public void setValue(Location newValue) {
        if (newValue == null) {
            this.worldName = null;
        } else {
            this.worldName = newValue.getWorld().getName();
            this.x = newValue.getBlockX();
            this.y = newValue.getBlockY();
            this.z = newValue.getBlockZ();
            this.yaw = newValue.getYaw();
            this.pitch = newValue.getPitch();
        }

        this.updataContainer();
    }

    public Location getValue(Server server) {

        if (this.worldName == null) {
            return null;
        } else {
            try {
                Location ret = new Location(server.getWorld(this.worldName), this.x, this.y, this.z, this.yaw, this.pitch);
                return ret;
            } catch (Exception e) {
                return null;
            }
        }

    }

    public Location getValue(Server server, Location def) {
        Location ret = getValue(server);
        return ret != null ? ret : def;
    }

    public boolean setValue(String newValue) {
        if (newValue == null) {
            this.worldName = null;
        } else {
            try {
                this.worldName = null;
                String[] data = newValue.split(";");
                if (data.length == 6) {
                    this.worldName = data[0];
                    this.x = Double.valueOf(data[1]);
                    this.y = Double.valueOf(data[2]);
                    this.z = Double.valueOf(data[3]);
                    this.yaw = Float.valueOf(data[4]);
                    this.pitch = Float.valueOf(data[5]);
                }

            } catch (Exception e) {
                this.worldName = null;
                this.updataContainer();
                return false;
            }
        }

        this.updataContainer();
        return true;
    }

    private void updataContainer() {
        if (this.container != null) {
            String stringVal;
            if (this.worldName == null) {
                stringVal = null;
            } else {
                stringVal = this.worldName + ";" + this.x + ";" + this.y + ";" + this.z
                        + ";" + this.yaw + ";" + this.pitch;
            }
            this.container.internalSetValue(info.name, stringVal);
        }
    }

    @Override
    public boolean hasValue() {
        return this.worldName != null;
    }

    @Override
    public String toString() {
        if (this.worldName != null) {
            return this.worldName + "(" + Math.round(this.x) + "," + Math.round(this.y) + "," + Math.round(this.z) + ")";
        } else {
            return "-";
        }
    }
}
