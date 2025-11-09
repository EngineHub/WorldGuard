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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.report.DataReport;
import com.sk89q.worldedit.util.report.HierarchyObjectReport;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.List;

public class ConfigReport extends DataReport {

    public ConfigReport() {
        super(message("reports.config.title"));

        List<? extends World> worlds = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds();

        append(message("reports.config.configuration"), new HierarchyObjectReport(message("reports.config.configuration"), WorldGuard.getInstance().getPlatform().getGlobalStateManager()));

        for (World world : worlds) {
            WorldConfiguration config = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

            DataReport report = new DataReport(message("reports.config.world.title", world.getName()));
            report.append(message("reports.config.configuration"), new HierarchyObjectReport(message("reports.config.configuration"), config));

            Blacklist blacklist = config.getBlacklist();
            if (blacklist != null) {
                DataReport section = new DataReport(message("reports.config.blacklist.title"));
                section.append(message("reports.config.blacklist.rule-count"), blacklist.getItemCount());
                section.append(message("reports.config.blacklist.whitelist"), blacklist.isWhitelist());
                report.append(section.getTitle(), section);
            } else {
                report.append(message("reports.config.blacklist.title"), message("reports.common.disabled"));
            }

            RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
            if (regions != null) {
                DataReport section = new DataReport(message("reports.config.regions.title"));
                section.append(message("reports.config.regions.count"), regions.size());

                ProtectedRegion global = regions.getRegion("__global__");
                if (global != null) {
                    section.append(message("reports.config.regions.global"), new RegionReport(global));
                } else {
                    section.append(message("reports.config.regions.global"), message("reports.common.undefined"));
                }

                report.append(section.getTitle(), section);
            } else {
                report.append(message("reports.config.regions.title"), message("reports.common.disabled"));
            }

            append(report.getTitle(), report);
        }
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }

}
