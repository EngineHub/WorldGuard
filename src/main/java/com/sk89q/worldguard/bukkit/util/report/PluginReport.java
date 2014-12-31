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
import org.bukkit.plugin.Plugin;

public class PluginReport extends DataReport {

    public PluginReport() {
        super("Plugins");

        Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();

        append("Plugin Count", plugins.length);

        for (Plugin plugin : plugins) {
            DataReport report = new DataReport("Plugin: " + plugin.getName());
            report.append("Enabled?", plugin.isEnabled());
            report.append("Full Name", plugin.getDescription().getFullName());
            report.append("Version", plugin.getDescription().getVersion());
            report.append("Website", plugin.getDescription().getWebsite());
            report.append("Description", plugin.getDescription().getDescription());
            report.append("Authors", plugin.getDescription().getAuthors());
            report.append("Load Before", plugin.getDescription().getLoadBefore());
            report.append("Dependencies", plugin.getDescription().getDepend());
            report.append("Soft Dependencies", plugin.getDescription().getSoftDepend());
            report.append("Folder", plugin.getDataFolder().getAbsoluteFile());
            report.append("Entry Point", plugin.getDescription().getMain());
            report.append("Class", plugin.getClass().getName());
            report.append("Class Source", plugin.getClass().getProtectionDomain().getCodeSource().getLocation());
            append(report.getTitle(), report);
        }
    }

}
