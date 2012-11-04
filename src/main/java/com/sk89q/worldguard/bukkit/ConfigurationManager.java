// $Id$
/*
 * WorldGuard
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldguard.bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.GodComponent;
import com.sk89q.rebar.config.ConfigurationException;
import com.sk89q.rebar.config.PairedKeyValueLoaderBuilder;
import com.sk89q.rebar.config.YamlConfigurationFile;
import com.sk89q.rebar.config.YamlStyle;
import com.sk89q.rebar.config.types.LowercaseStringLoaderBuilder;
import com.sk89q.rebar.config.types.StringLoaderBuilder;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rulelists.RuleEntryLoader;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.Blacklist;

/**
 * Represents the global configuration and also delegates configuration
 * for individual worlds.
 */
public class ConfigurationManager {

    private static Logger logger = LoggerUtils.getLogger(RuleEntryLoader.class, "[WorldGuard] ");

    private static final String DEFAULT_RULELIST_PATH = "rulelist.yml";
    private static final String DEFAULT_BLACKLIST_PATH = "worlds/%world%/blacklist.txt";
    private static final String DEFAULT_WORLD_CONFIG_PATH = "worlds/%world%/config.yml";

    static final YamlStyle YAML_STYLE = new YamlStyle(FlowStyle.BLOCK);

    private WorldGuardPlugin plugin;
    private ConcurrentMap<String, WorldConfiguration> worlds;
    private YamlConfigurationFile config;

    private Map<String, String> ruleLists = new HashMap<String, String>();
    private Map<String, String> blacklists = new HashMap<String, String>();
    private Map<String, String> worldConfigs = new HashMap<String, String>();

    @Deprecated
    private Set<String> hasGodMode = new HashSet<String>();
    private Set<String> hasAmphibious = new HashSet<String>();

    private boolean hasCommandBookGodMode = false;

    /* Configuration data start */
    public boolean useRegionsScheduler;
    public boolean useRegionsCreatureSpawnEvent;
    public boolean usePlayerMove;
    public Map<String, String> hostKeys = new HashMap<String, String>();

    public boolean useSqlDatabase = false;
    public String sqlDsn;
    public String sqlUsername;
    public String sqlPassword;
    /* Configuration data end */

    /**
     * Construct the object.
     *
     * @param plugin the plugin instance
     */
    public ConfigurationManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.worlds = new ConcurrentHashMap<String, WorldConfiguration>();
    }

    /**
     * Load the configuration.
     */
    public void load() {
        loadConfig();

        // Regions settings
        useRegionsScheduler = config.getBoolean("regions.use-scheduler", true);
        useRegionsCreatureSpawnEvent = config.getBoolean("regions.use-creature-spawn-event", true);

        // Regions SQL settings
        useSqlDatabase = config.getBoolean("regions.sql.use", false);
        sqlDsn = config.getString("regions.sql.dsn", "jdbc:mysql://localhost/worldguard");
        sqlUsername = config.getString("regions.sql.username", "worldguard");
        sqlPassword = config.getString("regions.sql.password", "worldguard");

        // Other settings
        usePlayerMove = config.getBoolean("use-player-move-event", true);

        // Load host keys
        hostKeys = config.mapOf("host-keys",
                PairedKeyValueLoaderBuilder.build(
                        new LowercaseStringLoaderBuilder(), new StringLoaderBuilder()));

        // Load rule list filenames
        if (config.contains("rulelists")) {
            ruleLists = config.mapOf("rulelists",
                    PairedKeyValueLoaderBuilder.build(
                            new LowercaseStringLoaderBuilder(), new StringLoaderBuilder()));
        } else {
            config.setNode("rulelists").set("__default__", DEFAULT_RULELIST_PATH);
            ruleLists = new HashMap<String, String>();
            ruleLists.put("__default__", DEFAULT_RULELIST_PATH);
        }

        // Load blacklist filenames
        if (config.contains("blacklists")) {
            blacklists = config.mapOf("blacklists",
                    PairedKeyValueLoaderBuilder.build(
                            new LowercaseStringLoaderBuilder(), new StringLoaderBuilder()));
        } else {
            config.setNode("blacklists").set("__default__", DEFAULT_BLACKLIST_PATH);
            blacklists = new HashMap<String, String>();
            blacklists.put("__default__", DEFAULT_BLACKLIST_PATH);
        }

        // Load world config filenames
        if (config.contains("world-configs")) {
            worldConfigs = config.mapOf("world-configs",
                    PairedKeyValueLoaderBuilder.build(
                            new LowercaseStringLoaderBuilder(), new StringLoaderBuilder()));
        } else {
            config.setNode("world-configs").set("__default__", DEFAULT_WORLD_CONFIG_PATH);
            worldConfigs = new HashMap<String, String>();
            worldConfigs.put("__default__", DEFAULT_WORLD_CONFIG_PATH);
        }

        // Load configurations for each world
        for (World world : plugin.getServer().getWorlds()) {
            get(world);
        }

        saveConfig();
    }

    /**
     * Unload the configuration.
     */
    public void unload() {
        worlds.clear();
    }

    /**
     * Load the configuration from file.
     */
    private void loadConfig() {
        config = new YamlConfigurationFile(new File(plugin.getDataFolder(), "config.yml"), YAML_STYLE);

        try {
            config.load();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading config.yml for global config", e);
        } catch (ConfigurationException e) {
            logger.log(Level.SEVERE, "Error reading config.yml for global config", e);
        }
    }

    /**
     * Save the configuration to file.
     */
    private void saveConfig() {
        config.setHeader(
                "# GLOBAL CONFIGURATION FILE\r\n" +
                "#\r\n" +
                "# Everything in here applies to all worlds. For world-specific settings, edit \r\n" +
                "# the files in plugins/WorldGuard/worlds/WORLD_NAME_HERE/config.yml\r\n" +
                "#\r\n" +
                "# The official format of this file is called YAML. If you've never had to\r\n" +
                "# edit one before, you should REALLY read http://wiki.sk89q.com/wiki/Editing_YAML\r\n");

        try {
            config.save();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save configuration", e);
        }
    }

    /**
     * Get the configuration for a world.
     *
     * @param world The world to get the configuration for
     * @return {@code world}'s configuration
     */
    public WorldConfiguration get(World world) {
        String worldName = world.getName();
        WorldConfiguration config = worlds.get(worldName);
        WorldConfiguration newConfig = null;

        while (config == null) {
            if (newConfig == null) {
                newConfig = new WorldConfiguration(plugin, worldName, this, this.config);
            }
            worlds.putIfAbsent(world.getName(), newConfig);
            config = worlds.get(world.getName());
        }

        return config;
    }

    /**
     * Get the RuleList file for the given world.
     *
     * @param worldName world
     * @return file (which may or may not exist)
     */
    File getRuleListFile(String worldName) {
        String path = ruleLists.get(worldName.toLowerCase());

        if (path == null) {
            path = ruleLists.get("__default__");
        }

        if (path == null) {
            path = DEFAULT_RULELIST_PATH;
        }

        path = path.replace("%world%", worldName);
        File file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            plugin.createDefaultConfiguration(file, "rulelist.yml");
        }

        return file;
    }

    /**
     * Get the blacklist file for the given world.
     *
     * @param worldName world
     * @return file (which may or may not exist)
     */
    File getBlacklistFile(String worldName) {
        String path = blacklists.get(worldName.toLowerCase());
        File file;

        if (path == null) {
            path = blacklists.get("__default__");
        }

        if (path == null) {
            path = DEFAULT_BLACKLIST_PATH;
        }

        path = path.replace("%world%", worldName);
        file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            plugin.createDefaultConfiguration(file, "blacklist.txt");
        }

        return file;
    }

    /**
     * Get the world configuration file for the given world.
     *
     * @param worldName world
     * @return file (which may or may not exist)
     */
    File getWorldConfigFile(String worldName) {
        String path = worldConfigs.get(worldName.toLowerCase());
        File file;

        if (path == null) {
            path = worldConfigs.get("__default__");
        }

        if (path == null) {
            path = DEFAULT_WORLD_CONFIG_PATH;
        }

        path = path.replace("%world%", worldName);
        file = new File(plugin.getDataFolder(), path);

        return file;
    }

    /**
     * Forget a player.
     *
     * @param player The player to forget about
     */
    void forgetPlayer(LocalPlayer player) {
        for (Map.Entry<String, WorldConfiguration> entry
                : worlds.entrySet()) {

            // The blacklist needs to forget players
            Blacklist bl = entry.getValue().getBlacklist();
            if (bl != null) {
                bl.forgetPlayer(player);
            }
        }

        hasGodMode.remove(player.getName());
        hasAmphibious.remove(player.getName());
    }

    /**
     * Enable god mode for a player.
     *
     * @param player The player to enable god mode for.
     */
    @Deprecated
    public void enableGodMode(Player player) {
        hasGodMode.add(player.getName());
    }

    /**
     * Disable god mode for a player.
     *
     * @param player The player to disable godmode for
     */
    @Deprecated
    public void disableGodMode(Player player) {
        hasGodMode.remove(player.getName());
    }

    /**
     * Check to see if god mode is enabled for a player.
     *
     * @param player The player to check
     * @return Whether the player has godmode through WorldGuard or CommandBook
     */
    public boolean hasGodMode(Player player) {
        if (hasCommandBookGodMode) {
            GodComponent god = CommandBook.inst().getComponentManager().getComponent(GodComponent.class);
            if (god != null) {
                return god.hasGodMode(player);
            }
        }
        return hasGodMode.contains(player.getName());
    }

    /**
     * Enable amphibious mode for a player.
     *
     * @param player The player to enable amphibious mode for
     */
    public void enableAmphibiousMode(Player player) {
        hasAmphibious.add(player.getName());
    }

    /**
     * Disable amphibious mode  for a player.
     *
     * @param player The player to disable amphibious mode for
     */
    public void disableAmphibiousMode(Player player) {
        hasAmphibious.remove(player.getName());
    }

    /**
     * Check to see if amphibious mode is enabled for a player.
     *
     * @param player The player to check
     * @return Whether {@code player} has amphibious mode
     */
    public boolean hasAmphibiousMode(Player player) {
        return hasAmphibious.contains(player.getName());
    }

    /**
     * Check and store whether CommandBook is installed.
     */
    void updateCommandBookGodMode() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("CommandBook")) {
                Class.forName("com.sk89q.commandbook.GodComponent");
                hasCommandBookGodMode = true;
                return;
            }
        } catch (ClassNotFoundException ignore) {}
        hasCommandBookGodMode = false;
    }

    /**
     * Return whether CommandBook is available for God mode usage.
     *
     * @return true if it available
     */
    boolean hasCommandBookGodMode() {
        return hasCommandBookGodMode;
    }
}
