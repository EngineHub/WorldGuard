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
import org.bukkit.plugin.Plugin;

public class PluginReport extends DataReport {

    public PluginReport() {
        super(message("reports.plugin.title"));

        Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();

        append(message("reports.plugin.count"), plugins.length);

        for (Plugin plugin : plugins) {
            DataReport report = new DataReport(message("reports.plugin.entry.title", plugin.getName()));
            report.append(message("reports.plugin.entry.enabled"), plugin.isEnabled());
            report.append(message("reports.plugin.entry.full-name"), plugin.getDescription().getFullName());
            report.append(message("reports.plugin.entry.version"), plugin.getDescription().getVersion());
            report.append(message("reports.plugin.entry.website"), plugin.getDescription().getWebsite());
            report.append(message("reports.plugin.entry.description"), plugin.getDescription().getDescription());
            report.append(message("reports.plugin.entry.authors"), plugin.getDescription().getAuthors());
            report.append(message("reports.plugin.entry.load-before"), plugin.getDescription().getLoadBefore());
            report.append(message("reports.plugin.entry.dependencies"), plugin.getDescription().getDepend());
            report.append(message("reports.plugin.entry.soft-dependencies"), plugin.getDescription().getSoftDepend());
            report.append(message("reports.plugin.entry.folder"), plugin.getDataFolder().getAbsoluteFile());
            report.append(message("reports.plugin.entry.entry-point"), plugin.getDescription().getMain());
            report.append(message("reports.plugin.entry.class"), plugin.getClass().getName());
            report.append(message("reports.plugin.entry.class-source"),
                    plugin.getClass().getProtectionDomain().getCodeSource().getLocation());
            append(report.getTitle(), report);
        }
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }
}

