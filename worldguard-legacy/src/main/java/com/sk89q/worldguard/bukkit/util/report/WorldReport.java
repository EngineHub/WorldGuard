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

import com.sk89q.worldguard.util.report.DataReport;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;

public class WorldReport extends DataReport {

    public WorldReport() {
        super("Worlds");

        List<World> worlds = Bukkit.getServer().getWorlds();

        append("World Count", worlds.size());

        for (World world : worlds) {
            DataReport report = new DataReport("World: " + world.getName());
            report.append("UUID", world.getUID());
            report.append("World Type", world.getWorldType());
            report.append("Environment", world.getEnvironment());
            ChunkGenerator generator = world.getGenerator();
            report.append("Chunk Generator", generator != null ? generator.getClass().getName() : "<Default>");

            DataReport spawning = new DataReport("Spawning");
            spawning.append("Animals?", world.getAllowAnimals());
            spawning.append("Monsters?", world.getAllowMonsters());
            spawning.append("Ambient Spawn Limit", world.getAmbientSpawnLimit());
            spawning.append("Animal Spawn Limit", world.getAnimalSpawnLimit());
            spawning.append("Monster Spawn Limit", world.getMonsterSpawnLimit());
            spawning.append("Water Creature Spawn Limit", world.getWaterAnimalSpawnLimit());
            report.append(spawning.getTitle(), spawning);

            DataReport config = new DataReport("Configuration");
            config.append("Difficulty", world.getDifficulty());
            config.append("Max Height", world.getMaxHeight());
            config.append("Sea Level", world.getSeaLevel());
            report.append(config.getTitle(), config);

            DataReport state = new DataReport("State");
            state.append("Spawn Location", world.getSpawnLocation());
            state.append("Full Time", world.getFullTime());
            state.append("Weather Duration", world.getWeatherDuration());
            state.append("Thunder Duration", world.getThunderDuration());
            report.append(state.getTitle(), state);

            DataReport protection = new DataReport("Protection");
            protection.append("PVP?", world.getPVP());
            protection.append("Game Rules", world.getGameRules());
            report.append(protection.getTitle(), protection);

            append(report.getTitle(), report);
        }
    }
}
