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
import com.sk89q.worldguard.util.report.DataReport;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;

public class PerformanceReport extends DataReport {

    public PerformanceReport() {
        super("Performance");

        List<World> worlds = Bukkit.getServer().getWorlds();

        append("World Count", worlds.size());

        for (World world : worlds) {
            int loadedChunkCount = world.getLoadedChunks().length;

            DataReport report = new DataReport("World: " + world.getName());
            report.append("Keep in Memory?", world.getKeepSpawnInMemory());
            report.append("Entity Count", world.getEntities().size());
            report.append("Chunk Count", loadedChunkCount);

            Map<Class<? extends Entity>, Integer> entityCounts = Maps.newHashMap();

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
            DataReport entities = new DataReport("Entity Distribution");
            for (Map.Entry<Class<? extends Entity>, Integer> entry : entityCounts.entrySet()) {
                entities.append(entry.getKey().getSimpleName(), "%d [%f/chunk]",
                        entry.getValue(),
                        (float) (entry.getValue() / (double) loadedChunkCount));
            }
            report.append(entities.getTitle(), entities);

            append(report.getTitle(), report);
        }

    }

}
