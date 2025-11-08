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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.Collection;

public class ServicesReport extends DataReport {

    public ServicesReport() {
        super(message("reports.services.title"));

        Collection<Class<?>> services = Bukkit.getServicesManager().getKnownServices();
        for (Class<?> service : services) {
            DataReport report = new DataReport(message("reports.services.entry.title", service.getName()));
            for (RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(service)) {
                ServicePriority priority = provider.getPriority();
                String pluginName = provider.getPlugin().getName();
                String providerName = provider.getProvider().getClass().getName();
                report.append(message("reports.services.entry.provider"),
                        message("reports.services.entry.provider.format", pluginName, priority.name(), providerName));
            }
            append(report.getTitle(), report);
        }
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }
}
