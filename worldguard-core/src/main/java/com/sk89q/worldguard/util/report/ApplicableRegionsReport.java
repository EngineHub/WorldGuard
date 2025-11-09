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
        super(message("reports.applicable.title"));
        BlockVector3 position = player.getBlockIn().toVector().toBlockPoint();
        append(message("reports.applicable.location"), message("reports.applicable.location-value", player.getWorld().getName(), position));
        RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(player.getWorld());
        if (mgr == null) {
            append(message("reports.applicable.regions.title"), message("reports.applicable.regions.disabled"));
        } else {
            ApplicableRegionSet rgs = mgr.getApplicableRegions(position);
            if (rgs.getRegions().isEmpty()) {
                append(message("reports.applicable.regions.title"), message("reports.common.none"));
            } else {
                DataReport regions = new DataReport(message("reports.applicable.regions.section-title"));
                for (ProtectedRegion region : rgs.getRegions()) {
                    boolean inherited = !region.contains(position);
                    String marker = inherited ? message("reports.applicable.region-inherited-marker") : "";
                    regions.append(message("reports.applicable.region-entry", region.getId(), marker), new RegionReport(region));
                }
                append(regions.getTitle(), regions);
            }
        }
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }

}
