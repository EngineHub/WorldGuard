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

package com.sk89q.worldguard.config;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.storage.DriverType;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.session.handler.WaterBreathing;
import com.sk89q.worldedit.util.report.Unreported;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents the global configuration and also delegates configuration
 * for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public abstract class ConfigurationManager {

    protected static final Logger log = Logger.getLogger(ConfigurationManager.class.getCanonicalName());

    static final String CONFIG_HEADER = "#\r\n" +
            "# Конфигурационный файл WorldGuard.\r\n" +
            "#\r\n" +
            "# Это файл конфигурации мира. Все, что находится здесь будет влиять\r\n" +
            "# только на этот мир. Тем не менее, каждый мир имеет свою собственную конфигурацию,\r\n" +
            "# чтобы позволить вам заменить большинство настроек здесь только для этого мира.\r\n" +
            "#\r\n" +
            "# О редактировании этого файла:\r\n" +
            "# - НЕ ИСПОЛЬЗОВАТЬ TAB. Вы должны использовать пробелы иначе Bukkit будет жаловаться. Если\r\n" +
            "#   вы используете редактор, такой как Notepad++ (рекомендуется для пользователей Windows), вы\r\n" +
            "#   должны настроить его на 'заменить TAB на пробелы.' В Notepad++, это можно\r\n" +
            "#   изменить в Опции > Настройки > Настройки табуляци.\r\n" +
            "# - Не избавиляйтесь от отступов. Они смещены, поэтому некоторые записи\r\n" +
            "#   в категориях (как 'enforce-single-session' находится в категории 'protection')\r\n" +
            "# - Если вы хотите проверить формат этого файла, прежде чем положить его\r\n" +
            "#   в WorldGuard, вставьте его в http://yaml-online-parser.appspot.com/\r\n" +
            "#   и посмотрите полученные 'ОШИБКИ:'.\r\n" +
            "# - Строки, начинающие с # называются комментариями, поэтому они игнорируются.\r\n" +
            "#\r\n";

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
    public boolean particleEffects;
    public boolean disablePermissionCache;
    public boolean disableDefaultBypass;
    public boolean announceBypassStatus;

    @Unreported public Map<String, String> hostKeys = new HashMap<>();
    public boolean hostKeysAllowFMLClients;

    /**
     * Region Storage Configuration method, and config values
     */
    @Unreported public RegionDriver selectedRegionStoreDriver;
    @Unreported public Map<DriverType, RegionDriver> regionStoreDriverMap;

    /**
     * Get the folder for storing data files and configuration.
     *
     * @return the data folder
     */
    public abstract File getDataFolder();

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
    public abstract void load();

    /**
     * Unload the configuration.
     */
    public abstract void unload();

    /**
     * Get the configuration for a world.
     *
     * @param world The world to get the configuration for
     * @return {@code world}'s configuration
     */
    public abstract WorldConfiguration get(World world);

    public abstract void disableUuidMigration();

    /**
     * Check to see if god mode is enabled for a player.
     *
     * @param player The player to check
     * @return Whether the player has godmode through WorldGuard or CommandBook
     */
    public boolean hasGodMode(LocalPlayer player) {
        return WorldGuard.getInstance().getPlatform().getSessionManager().get(player).isInvincible(player);
    }

    /**
     * Enable amphibious mode for a player.
     *
     * @param player The player to enable amphibious mode for
     */
    public void enableAmphibiousMode(LocalPlayer player) {
        WaterBreathing handler = WorldGuard.getInstance().getPlatform().getSessionManager().get(player).getHandler(WaterBreathing.class);
        if (handler != null) {
            handler.setWaterBreathing(true);
        }
    }

    /**
     * Disable amphibious mode  for a player.
     *
     * @param player The player to disable amphibious mode for
     */
    public void disableAmphibiousMode(LocalPlayer player) {
        WaterBreathing handler = WorldGuard.getInstance().getPlatform().getSessionManager().get(player).getHandler(WaterBreathing.class);
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
    public boolean hasAmphibiousMode(LocalPlayer player) {
        WaterBreathing handler = WorldGuard.getInstance().getPlatform().getSessionManager().get(player).getHandler(WaterBreathing.class);
        return handler != null && handler.hasWaterBreathing();
    }
}
