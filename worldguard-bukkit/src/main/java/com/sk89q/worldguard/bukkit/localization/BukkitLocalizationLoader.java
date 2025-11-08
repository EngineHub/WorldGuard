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

package com.sk89q.worldguard.bukkit.localization;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.util.localization.Localization;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class BukkitLocalizationLoader {

    private final WorldGuardPlugin plugin;

    public BukkitLocalizationLoader(WorldGuardPlugin plugin) {
        this.plugin = plugin;
    }

    public Localization load(String language) {
        String fileName = "messages_" + language + ".yml";
        File file = new File(plugin.getDataFolder(), fileName);
        plugin.createDefaultConfiguration(file, fileName);
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        Map<String, String> messages = new HashMap<>();
        collect("", configuration, messages);
        return new Localization(messages);
    }

    private void collect(String root, ConfigurationSection section, Map<String, String> messages) {
        for (String key : section.getKeys(false)) {
            String qualified = root.isEmpty() ? key : root + "." + key;
            if (section.isConfigurationSection(key)) {
                ConfigurationSection child = section.getConfigurationSection(key);
                if (child != null) {
                    collect(qualified, child, messages);
                }
            } else if (section.isString(key)) {
                messages.put(qualified, section.getString(key, ""));
            }
        }
    }
}


