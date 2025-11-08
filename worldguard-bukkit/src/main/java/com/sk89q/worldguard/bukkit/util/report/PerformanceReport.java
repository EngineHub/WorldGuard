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

import com.google.common.collect.Maps;
import com.sk89q.worldedit.util.report.DataReport;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;

public class PerformanceReport extends DataReport {

    public PerformanceReport() {
        super(message("reports.performance.title"));

        List<World> worlds = Bukkit.getServer().getWorlds();

        append(message("reports.performance.world-count"), worlds.size());

        for (World world : worlds) {
            int loadedChunkCount = world.getLoadedChunks().length;

            DataReport report = new DataReport(message("reports.performance.world.title", world.getName()));
            report.append(message("reports.performance.world.keep-spawn"), world.getKeepSpawnInMemory());
            report.append(message("reports.performance.world.entity-count"), world.getEntities().size());
            report.append(message("reports.performance.world.chunk-count"), loadedChunkCount);

            Map<Class<? extends Entity>, Integer> entityCounts = Maps.newHashMap();
            Map<Class<? extends BlockState>, Integer> tileEntityCounts = Maps.newHashMap();

            // Collect tile entities
            int teCount = 0;
            for (Chunk chunk : world.getLoadedChunks()) {
                teCount += chunk.getTileEntities().length;
                for (BlockState state : chunk.getTileEntities()) {
                    Class<? extends BlockState> cls = state.getClass();

                    if (tileEntityCounts.containsKey(cls)) {
                        tileEntityCounts.put(cls, tileEntityCounts.get(cls) + 1);
                    } else {
                        tileEntityCounts.put(cls, 1);
                    }
                }
            }
            report.append(message("reports.performance.world.tile-entity-count"), teCount);

            // Collect entities
            for (Entity entity : world.getEntities()) {
                Class<? extends Entity> cls = entity.getClass();

                if (entityCounts.containsKey(cls)) {
                    entityCounts.put(cls, entityCounts.get(cls) + 1);
                } else {
                    entityCounts.put(cls, 1);
                }
            }

            // Print entities
            DataReport entities = new DataReport(message("reports.performance.entities.title"));
            for (Map.Entry<Class<? extends Entity>, Integer> entry : entityCounts.entrySet()) {
                entities.append(entry.getKey().getSimpleName(), message("reports.performance.entities.format"),
                        entry.getValue(),
                        (float) (entry.getValue() / (double) loadedChunkCount));
            }
            report.append(entities.getTitle(), entities);

            // Print tile entities
            DataReport tileEntities = new DataReport(message("reports.performance.tile-entities.title"));
            for (Map.Entry<Class<? extends BlockState>, Integer> entry : tileEntityCounts.entrySet()) {
                tileEntities.append(entry.getKey().getSimpleName(), message("reports.performance.entities.format"),
                        entry.getValue(),
                        (float) (entry.getValue() / (double) loadedChunkCount));
            }
            report.append(tileEntities.getTitle(), tileEntities);

            append(report.getTitle(), report);
        }

    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }

}
