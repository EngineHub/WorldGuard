/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.protection.regions;

import org.khelekore.prtree.MBRConverter;

public class ProtectedRegionMBRConverter implements MBRConverter<ProtectedRegion> {

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public double getMax(int dimension, ProtectedRegion region) {
        switch (dimension) {
            case 0:
                return region.getMaximumPoint().getBlockX();
            case 1:
                return region.getMaximumPoint().getBlockY();
            case 2:
                return region.getMaximumPoint().getBlockZ();
        }
        return 0;
    }

    @Override
    public double getMin(int dimension, ProtectedRegion region) {
        switch (dimension) {
            case 0:
                return region.getMinimumPoint().getBlockX();
            case 1:
                return region.getMinimumPoint().getBlockY();
            case 2:
                return region.getMinimumPoint().getBlockZ();
        }
        return 0;
    }
}
