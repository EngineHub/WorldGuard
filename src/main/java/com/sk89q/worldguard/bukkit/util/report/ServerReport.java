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

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.util.report.DataReport;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class ServerReport extends DataReport {

    public ServerReport() {
        super("Server Information");

        Server server = Bukkit.getServer();

        append("Server ID", server.getServerId());
        append("Server Name", server.getServerName());
        append("Bukkit Version", server.getBukkitVersion());
        append("Implementation", server.getVersion());
        append("Player Count", "%d/%d", BukkitUtil.getOnlinePlayers().size(), server.getMaxPlayers());

        append("Server Class Source", server.getClass().getProtectionDomain().getCodeSource().getLocation());

        DataReport spawning = new DataReport("Spawning");
        spawning.append("Ambient Spawn Limit", server.getAmbientSpawnLimit());
        spawning.append("Animal Spawn Limit", server.getAnimalSpawnLimit());
        spawning.append("Monster Spawn Limit", server.getMonsterSpawnLimit());
        spawning.append("Ticks per Animal Spawn", server.getTicksPerAnimalSpawns());
        spawning.append("Ticks per Monster Spawn", server.getTicksPerMonsterSpawns());
        append(spawning.getTitle(), spawning);

        DataReport config = new DataReport("Configuration");
        config.append("Nether Enabled?", server.getAllowNether());
        config.append("The End Enabled?", server.getAllowEnd());
        config.append("Generate Structures?", server.getGenerateStructures());
        config.append("Flight Allowed?", server.getAllowFlight());
        config.append("Connection Throttle", server.getConnectionThrottle());
        config.append("Idle Timeout", server.getIdleTimeout());
        config.append("Shutdown Message", server.getShutdownMessage());
        config.append("Default Game Mode", server.getDefaultGameMode());
        config.append("Main World Type", server.getWorldType());
        config.append("View Distance", server.getViewDistance());
        append(config.getTitle(), config);

        DataReport protection = new DataReport("Protection");
        protection.append("Spawn Radius", server.getSpawnRadius());
        append(protection.getTitle(), protection);
    }

}