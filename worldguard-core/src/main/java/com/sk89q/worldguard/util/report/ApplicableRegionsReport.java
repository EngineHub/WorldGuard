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

package com.sk89q.worldguard.util.report;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.report.DataReport;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ApplicableRegionsReport extends DataReport {

    public ApplicableRegionsReport(LocalPlayer player) {
        super("Applicable regions");
        BlockVector3 position = player.getBlockIn().toVector().toBlockPoint();
        append("Location", player.getWorld().getName() + " @ " + position);
        RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(player.getWorld());
        if (mgr == null) {
            append("Regions", "Disabled for current world");
        } else {
            ApplicableRegionSet rgs = mgr.getApplicableRegions(position);
            if (rgs.getRegions().isEmpty()) {
                append("Regions", "None");
            } else {
                DataReport regions = new DataReport("Regions");
                for (ProtectedRegion region : rgs.getRegions()) {
                    boolean inherited = !region.contains(position);
                    regions.append(region.getId() + (inherited ? "*" : ""), new RegionReport(region));
                }
                append(regions.getTitle(), regions);
            }
        }
    }

}
