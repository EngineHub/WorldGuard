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

package com.sk89q.worldguard.bukkit;

import com.google.common.collect.ImmutableMap;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldguard.protection.managers.storage.DriverType;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.file.DirectoryYamlDriver;
import com.sk89q.worldguard.protection.managers.storage.sql.SQLDriver;
import com.sk89q.worldguard.session.handler.WaterBreathing;
import com.sk89q.worldguard.util.report.Unreported;
import com.sk89q.worldguard.util.sql.DataSourceConfig;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Represents the global configuration and also delegates configuration
 * for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public class ConfigurationManager {

    private static final Logger log = Logger.getLogger(ConfigurationManager.class.getCanonicalName());

    private static final String CONFIG_HEADER = "#\r\n" +
            "# Конфигурационный файл WorldGuard.\r\n" +
            "#\r\n" +
            "# Это файл конфигурации мира. Все, что помещается здесь будет влиять\r\n" +
            "# только на этот мир. Если вы ничего не помещали в этот файл, то\r\n" +
            "# настройки будут унаследованы от основного файла конфигурации.\r\n" +
            "#\r\n" +
            "# О редактировании этого файла:\r\n" +
            "# - НЕ ИСПОЛЬЗОВАТЬ ТАБУЛЯЦИЮ. Вы должны использовать пробелы, иначе Bukkit будет выдавать ошибку. Если\r\n" +
            "#   вы используете редактор, такой как Notepad++ (рекомендуется для пользователей Windows), вы\r\n" +
            "#   должны настроить его на \"заменить TAB на пробелы.\" В Notepad++, это можно\r\n" +
            "#   изменить в Опции > Настройки > Настройки табуляци.\r\n" +
            "# - Не избавиляйтесь от отступов. Они смещены, поэтому некоторые записи\r\n" +
            "#   в категориях (как \"enforce-single-session\" находится в категории \"protection\")\r\n" +
            "# - Если вы хотите проверить формат этого файла, прежде чем положить его\r\n" +
            "#   в WorldGuard, вставьте его в http://yaml-online-parser.appspot.com/\r\n" +
            "#   и посмотрите полученные \"ОШИБКА:\".\r\n" +
            "# - Строки, начинающие с # называются комментариями, поэтому они игнорируются.\r\n" +
            "#\r\n";

    @Unreported private WorldGuardPlugin plugin;
    @Unreported private ConcurrentMap<String, WorldConfiguration> worlds;
    @Unreported private YAMLProcessor config;

    private boolean hasCommandBookGodMode = false;

    public boolean useRegionsCreatureSpawnEvent;
    public boolean activityHaltToggle = false;
    public boolean useGodPermission;
    public boolean useGodGroup;
    public boolean useAmphibiousGroup;
    public boolean usePlayerMove;
    public boolean usePlayerTeleports;
    public boolean deopOnJoin;
    public boolean blockInGameOp;
    public boolean migrateRegionsToUuid;
    public boolean keepUnresolvedNames;

    @Unreported public Map<String, String> hostKeys = new HashMap<String, String>();
    public boolean hostKeysAllowFMLClients;

    /**
     * Region Storage Configuration method, and config values
     */
    @Unreported public RegionDriver selectedRegionStoreDriver;
    @Unreported public Map<DriverType, RegionDriver> regionStoreDriverMap;

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
     * Get the folder for storing data files and configuration.
     *
     * @return the data folder
     */
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    /**
     * Get the folder for storing data files and configuration for each
     * world.
     *
     * @return the data folder
     */
    public File getWorldsDataFolder() {
        return new File(getDataFolder(), "worlds");
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
            log.severe("Ошибка чтения глобальной конфигурации: ");
            e.printStackTrace();
        }

        config.removeProperty("suppress-tick-sync-warnings");
        migrateRegionsToUuid = config.getBoolean("regions.uuid-migration.perform-on-next-start", true);
        keepUnresolvedNames = config.getBoolean("regions.uuid-migration.keep-names-that-lack-uuids", true);
        useRegionsCreatureSpawnEvent = config.getBoolean("regions.use-creature-spawn-event", true);
        useGodPermission = config.getBoolean("auto-invincible", config.getBoolean("auto-invincible-permission", false));
        useGodGroup = config.getBoolean("auto-invincible-group", false);
        useAmphibiousGroup = config.getBoolean("auto-no-drowning-group", false);
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
        hostKeysAllowFMLClients = config.getBoolean("security.host-keys-allow-forge-clients", false);

        // ====================================================================
        // Region store drivers
        // ====================================================================

        boolean useSqlDatabase = config.getBoolean("regions.sql.use", false);
        String sqlDsn = config.getString("regions.sql.dsn", "jdbc:mysql://localhost/worldguard");
        String sqlUsername = config.getString("regions.sql.username", "worldguard");
        String sqlPassword = config.getString("regions.sql.password", "worldguard");
        String sqlTablePrefix = config.getString("regions.sql.table-prefix", "");

        DataSourceConfig dataSourceConfig = new DataSourceConfig(sqlDsn, sqlUsername, sqlPassword, sqlTablePrefix);
        SQLDriver sqlDriver = new SQLDriver(dataSourceConfig);
        DirectoryYamlDriver yamlDriver = new DirectoryYamlDriver(getWorldsDataFolder(), "regions.yml");

        this.regionStoreDriverMap = ImmutableMap.<DriverType, RegionDriver>builder()
                .put(DriverType.MYSQL, sqlDriver)
                .put(DriverType.YAML, yamlDriver)
                .build();
        this.selectedRegionStoreDriver = useSqlDatabase ? sqlDriver : yamlDriver;

        // Load configurations for each world
        for (World world : plugin.getServer().getWorlds()) {
            get(world);
        }

        config.setHeader(CONFIG_HEADER);
    }

    /**
     * Unload the configuration.
     */
    public void unload() {
        worlds.clear();
    }

    public void disableUuidMigration() {
        config.setProperty("regions.uuid-migration.perform-on-next-start", false);
        if (!config.save()) {
            log.severe("Ошибка сохранения конфигурации!");
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
                newConfig = new WorldConfiguration(plugin, worldName, this.config);
            }
            worlds.putIfAbsent(world.getName(), newConfig);
            config = worlds.get(world.getName());
        }

        return config;
    }

    /**
     * Check to see if god mode is enabled for a player.
     *
     * @param player The player to check
     * @return Whether the player has godmode through WorldGuard or CommandBook
     */
    public boolean hasGodMode(Player player) {
        return plugin.getSessionManager().get(player).isInvincible(player);
    }

    /**
     * Enable amphibious mode for a player.
     *
     * @param player The player to enable amphibious mode for
     */
    public void enableAmphibiousMode(Player player) {
        WaterBreathing handler = plugin.getSessionManager().get(player).getHandler(WaterBreathing.class);
        if (handler != null) {
            handler.setWaterBreathing(true);
        }
    }

    /**
     * Disable amphibious mode  for a player.
     *
     * @param player The player to disable amphibious mode for
     */
    public void disableAmphibiousMode(Player player) {
        WaterBreathing handler = plugin.getSessionManager().get(player).getHandler(WaterBreathing.class);
        if (handler != null) {
            handler.setWaterBreathing(false);
        }
    }

    /**
     * Check to see if amphibious mode is enabled for a player.
     *
     * @param player The player to check
     * @return Whether {@code player} has amphibious mode
     */
    public boolean hasAmphibiousMode(Player player) {
        WaterBreathing handler = plugin.getSessionManager().get(player).getHandler(WaterBreathing.class);
        return handler != null && handler.hasWaterBreathing();
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
