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

package com.sk89q.worldguard.bukkit.util.report;

import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.util.report.DataReport;
import com.sk89q.worldguard.util.report.RegionReport;
import com.sk89q.worldguard.util.report.ShallowObjectReport;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;

public class ConfigReport extends DataReport {

    public ConfigReport(WorldGuardPlugin plugin) {
        super("WorldGuard Configuration");

        List<World> worlds = Bukkit.getServer().getWorlds();

        append("Configuration", new ShallowObjectReport("Configuration", plugin.getGlobalStateManager()));

        for (World world : worlds) {
            WorldConfiguration config = plugin.getGlobalStateManager().get(world);

            DataReport report = new DataReport("World: " + world.getName());
            report.append("UUID", world.getUID());
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

            RegionManager regions = plugin.getRegionContainer().get(world);
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
