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

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.GodComponent;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.Blacklist;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents the global configuration and also delegates configuration
 * for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public class ConfigurationManager {

    private static final String CONFIG_HEADER = "#\r\n" +
            "# WorldGuard's main configuration file\r\n" +
            "#\r\n" +
            "# This is the global configuration file. Anything placed into here will\r\n" +
            "# be applied to all worlds. However, each world has its own configuration\r\n" +
            "# file to allow you to replace most settings in here for that world only.\r\n" +
            "#\r\n" +
            "# About editing this file:\r\n" +
            "# - DO NOT USE TABS. You MUST use spaces or Bukkit will complain. If\r\n" +
            "#   you use an editor like Notepad++ (recommended for Windows users), you\r\n" +
            "#   must configure it to \"replace tabs with spaces.\" In Notepad++, this can\r\n" +
            "#   be changed in Settings > Preferences > Language Menu.\r\n" +
            "# - Don't get rid of the indents. They are indented so some entries are\r\n" +
            "#   in categories (like \"enforce-single-session\" is in the \"protection\"\r\n" +
            "#   category.\r\n" +
            "# - If you want to check the format of this file before putting it\r\n" +
            "#   into WorldGuard, paste it into http://yaml-online-parser.appspot.com/\r\n" +
            "#   and see if it gives \"ERROR:\".\r\n" +
            "# - Lines starting with # are comments and so they are ignored.\r\n" +
            "#\r\n";

    /**
     * Reference to the plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * Holds configurations for different worlds.
     */
    private ConcurrentMap<String, WorldConfiguration> worlds;

    /**
     * The global configuration for use when loading worlds
     */
    private YAMLProcessor config;

    /**
     * List of people with god mode.
     */
    @Deprecated
    private Set<String> hasGodMode = new HashSet<String>();

    /**
     * List of people who can breathe underwater.
     */
    private Set<String> hasAmphibious = new HashSet<String>();

    private boolean hasCommandBookGodMode = false;

    public boolean useRegionsScheduler;
    public boolean useRegionsCreatureSpawnEvent;
    public boolean activityHaltToggle = false;
    public boolean autoGodMode;
    public boolean usePlayerMove;
    public boolean usePlayerTeleports;
    public boolean deopOnJoin;
    public boolean blockInGameOp;
    public Map<String, String> hostKeys = new HashMap<String, String>();

    /**
     * Region Storage Configuration method, and config values
     */
    public boolean useSqlDatabase = false;
    public String sqlDsn;
    public String sqlUsername;
    public String sqlPassword;

    /**
     * Construct the object.
     *
     * @param plugin The plugin instance
     */
    public ConfigurationManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.worlds = new ConcurrentHashMap<String, WorldConfiguration>();
    }

    /**
     * Load the configuration.
     */
    @SuppressWarnings("unchecked")
    public void load() {
        // Create the default configuration file
        plugin.createDefaultConfiguration(
                new File(plugin.getDataFolder(), "config.yml"), "config.yml");

        config = new YAMLProcessor(new File(plugin.getDataFolder(), "config.yml"), true, YAMLFormat.EXTENDED);
        try {
            config.load();
        } catch (IOException e) {
            plugin.getLogger().severe("Error reading configuration for global config: ");
            e.printStackTrace();
        }

        config.removeProperty("suppress-tick-sync-warnings");
        useRegionsScheduler = config.getBoolean("regions.use-scheduler", true);
        useRegionsCreatureSpawnEvent = config.getBoolean("regions.use-creature-spawn-event", true);
        autoGodMode = config.getBoolean("auto-invincible", config.getBoolean("auto-invincible-permission", false));
        config.removeProperty("auto-invincible-permission");
        usePlayerMove = config.getBoolean("use-player-move-event", true);
        usePlayerTeleports = config.getBoolean("use-player-teleports", true);

        deopOnJoin = config.getBoolean("security.deop-everyone-on-join", false);
        blockInGameOp = config.getBoolean("security.block-in-game-op-command", false);

        hostKeys = new HashMap<String, String>();
        Object hostKeysRaw = config.getProperty("host-keys");
        if (hostKeysRaw == null || !(hostKeysRaw instanceof Map)) {
            config.setProperty("host-keys", new HashMap<String, String>());
        } else {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) hostKeysRaw).entrySet()) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                hostKeys.put(key.toLowerCase(), value);
            }
        }

        useSqlDatabase = config.getBoolean(
                "regions.sql.use", false);

        sqlDsn = config.getString("regions.sql.dsn", "jdbc:mysql://localhost/worldguard");
        sqlUsername = config.getString("regions.sql.username", "worldguard");
        sqlPassword = config.getString("regions.sql.password", "worldguard");

        // Load configurations for each world
        for (World world : plugin.getServer().getWorlds()) {
            get(world);
        }

        config.setHeader(CONFIG_HEADER);

        if (!config.save()) {
            plugin.getLogger().severe("Error saving configuration!");
        }
    }

    /**
     * Unload the configuration.
     */
    public void unload() {
        worlds.clear();
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
                newConfig = new WorldConfiguration(plugin, worldName, this.config);
            }
            worlds.putIfAbsent(world.getName(), newConfig);
            config = worlds.get(world.getName());
        }

        return config;
    }

    /**
     * Forget a player.
     *
     * @param player The player to forget about
     */
    public void forgetPlayer(LocalPlayer player) {
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

    public void updateCommandBookGodMode() {
        try {
            if (plugin.getServer().getPluginManager().isPluginEnabled("CommandBook")) {
                Class.forName("com.sk89q.commandbook.GodComponent");
                hasCommandBookGodMode = true;
                return;
            }
        } catch (ClassNotFoundException ignore) {}
        hasCommandBookGodMode = false;
    }

    public boolean hasCommandBookGodMode() {
        return hasCommandBookGodMode;
    }
}
