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
import io.papermc.paper.datapack.Datapack;
import org.bukkit.Bukkit;

import java.util.Collection;

/**
 * A report for current datapacks with some information. Only available on Paper
 */
public class DatapackReport extends DataReport {
    public DatapackReport() {
        super("DataPacks");

        Collection<Datapack> packs = Bukkit.getDatapackManager().getPacks();

        append("Datapack Count", packs.size());
        append("Datapack Enabled Count", Bukkit.getDatapackManager().getEnabledPacks().size());

        for (Datapack pack : packs) {
            DataReport report = new DataReport("DataPack: " + pack.getName());
            report.append("Enabled?", pack.isEnabled());
            report.append("Name", pack.getName());
            report.append("Compatibility", pack.getCompatibility().name());
            append(report.getTitle(), report);
        }
    }
}
