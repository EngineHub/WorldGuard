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


