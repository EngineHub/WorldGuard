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
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class ServerReport extends DataReport {

    public ServerReport() {
        super(message("reports.server.title"));

        Server server = Bukkit.getServer();

        append(message("reports.server.bukkit-version"), server.getBukkitVersion());
        append(message("reports.server.implementation"), server.getName() + " " + server.getVersion());
        append(message("reports.server.player-count"), message("reports.server.player-count.format",
                Bukkit.getOnlinePlayers().size(), server.getMaxPlayers()));
        append(message("reports.server.class-source"), server.getClass().getProtectionDomain().getCodeSource().getLocation());

        DataReport onlineMode = new DataReport(message("reports.server.online-mode.title"));
        onlineMode.append(message("reports.server.online-mode.enabled"), server.getOnlineMode());
        if (PaperLib.isSpigot()) {
            onlineMode.append(message("reports.server.online-mode.bungeecord"),
                    Bukkit.spigot().getConfig().getBoolean("settings.bungeecord", false));
        }
        if (PaperLib.isPaper()) {
            onlineMode.append(message("reports.server.online-mode.velocity"),
                    Bukkit.spigot().getPaperConfig().getBoolean("proxies.velocity.enabled", false));
        }
        append(onlineMode.getTitle(), onlineMode);

        DataReport spawning = new DataReport(message("reports.server.spawning.title"));
        spawning.append(message("reports.server.spawning.ambient-limit"), server.getAmbientSpawnLimit());
        spawning.append(message("reports.server.spawning.animal-limit"), server.getAnimalSpawnLimit());
        spawning.append(message("reports.server.spawning.monster-limit"), server.getMonsterSpawnLimit());
        spawning.append(message("reports.server.spawning.ticks-animal"), server.getTicksPerAnimalSpawns());
        spawning.append(message("reports.server.spawning.ticks-monster"), server.getTicksPerMonsterSpawns());
        append(spawning.getTitle(), spawning);

        DataReport config = new DataReport(message("reports.server.configuration.title"));
        config.append(message("reports.server.configuration.nether"), server.getAllowNether());
        config.append(message("reports.server.configuration.end"), server.getAllowEnd());
        config.append(message("reports.server.configuration.structures"), server.getGenerateStructures());
        config.append(message("reports.server.configuration.flight"), server.getAllowFlight());
        config.append(message("reports.server.configuration.connection-throttle"), server.getConnectionThrottle());
        config.append(message("reports.server.configuration.idle-timeout"), server.getIdleTimeout());
        config.append(message("reports.server.configuration.shutdown-message"), server.getShutdownMessage());
        config.append(message("reports.server.configuration.gamemode"), server.getDefaultGameMode());
        config.append(message("reports.server.configuration.world-type"), server.getWorldType());
        config.append(message("reports.server.configuration.view-distance"), server.getViewDistance());
        append(config.getTitle(), config);

        DataReport protection = new DataReport(message("reports.server.protection.title"));
        protection.append(message("reports.server.protection.spawn-radius"), server.getSpawnRadius());
        append(protection.getTitle(), protection);
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }
}