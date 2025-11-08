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

import com.sk89q.worldedit.util.report.DataReport;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class WorldReport extends DataReport {

    public WorldReport() {
        super(message("reports.world.title"));

        for (World world : Bukkit.getServer().getWorlds()) {
            DataReport report = new DataReport(message("reports.world.entry.title", world.getName()));
            report.append(message("reports.world.entry.seed"), world.getSeed());
            report.append(message("reports.world.entry.type"), world.getWorldType().getName());
            report.append(message("reports.world.entry.entities"), world.getEntities().size());

            int tileEntityCount = 0;
            for (Chunk chunk : world.getLoadedChunks()) {
                tileEntityCount += chunk.getTileEntities().length;
            }
            report.append(message("reports.world.entry.tiles"), tileEntityCount);

            report.append(message("reports.world.entry.environment"), world.getEnvironment().name());
            report.append(message("reports.world.entry.difficulty"), world.getDifficulty().name());
            report.append(message("reports.world.entry.time"), world.getTime());
            report.append(message("reports.world.entry.full-time"), world.getFullTime());
            report.append(message("reports.world.entry.thunder"), world.isThundering());
            report.append(message("reports.world.entry.storm"), world.hasStorm());
            report.append(message("reports.world.entry.autosave"), world.isAutoSave());
            append(report.getTitle(), report);
        }
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }
}
