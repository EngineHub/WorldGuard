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
import com.sk89q.worldedit.util.report.ShallowObjectReport;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.config.WorldConfiguration;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.List;

public class ConfigReport extends DataReport {

    public ConfigReport() {
        super("WorldGuard Configuration");

        List<? extends World> worlds = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWorlds();

        append("Configuration", new ShallowObjectReport("Configuration", WorldGuard.getInstance().getPlatform().getGlobalStateManager()));

        for (World world : worlds) {
            WorldConfiguration config = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world);

            DataReport report = new DataReport("World: " + world.getName());
            report.append("Configuration", new ShallowObjectReport("Configuration", config));

            Blacklist blacklist = config.getBlacklist();
            if (blacklist != null) {
                DataReport section = new DataReport("Blacklist");
                section.append("Rule Count", blacklist.getItemCount());
                section.append("Whitelist Mode?", blacklist.isWhitelist());
                report.append(section.getTitle(), section);
            } else {
                report.append("Blacklist", "<Disabled>");
            }

            RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
            if (regions != null) {
                DataReport section = new DataReport("Regions");
                section.append("Region Count", regions.size());

                ProtectedRegion global = regions.getRegion("__global__");
                if (global != null) {
                    section.append("__global__", new RegionReport(global));
                } else {
                    section.append("__global__", "<Undefined>");
                }

                report.append(section.getTitle(), section);
            } else {
                report.append("Regions", "<Disabled>");
            }

            append(report.getTitle(), report);
        }
    }

}
