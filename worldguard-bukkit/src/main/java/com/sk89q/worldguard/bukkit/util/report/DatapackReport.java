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
import io.papermc.paper.datapack.Datapack;
import org.bukkit.Bukkit;

import java.util.Collection;

/**
 * A report for current datapacks with some information. Only available on Paper
 */

public class DatapackReport extends DataReport {
    public DatapackReport() {
        super(message("reports.datapack.title"));

        Collection<Datapack> packs = Bukkit.getDatapackManager().getPacks();

        append(message("reports.datapack.count"), packs.size());
        append(message("reports.datapack.enabled-count"), Bukkit.getDatapackManager().getEnabledPacks().size());

        for (Datapack pack : packs) {
            DataReport report = new DataReport(message("reports.datapack.entry.title", pack.getName()));
            report.append(message("reports.datapack.entry.enabled"), pack.isEnabled());
            report.append(message("reports.datapack.entry.name"), pack.getName());
            report.append(message("reports.datapack.entry.compatibility"), pack.getCompatibility().name());
            append(report.getTitle(), report);
        }
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }
}
