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

package com.sk89q.worldguard.internal.platform;

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.report.ReportList;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import com.sk89q.worldguard.util.profile.resolver.ProfileService;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * A platform for implementing.
 */
public interface WorldGuardPlatform {

    /**
     * Gets the name of the platform.
     *
     * @return The platform name
     */
    String getPlatformName();

    /**
     * Gets the version of the platform.
     *
     * @return The platform version
     */
    String getPlatformVersion();

    /**
     * Notifies the platform when a flag context is created.
     *
     * @param flagContextBuilder The flag context
     */
    void notifyFlagContextCreate(FlagContext.FlagContextBuilder flagContextBuilder);

    /**
     * Get the global ConfigurationManager.
     * Use this to access global configuration values and per-world configuration values.
     *
     * @return The global ConfigurationManager
     */
    ConfigurationManager getGlobalStateManager();

    /**
     * Gets an instance of the matcher, which handles matching
     * worlds, players, colours, etc from strings.
     *
     * @return The matcher
     */
    StringMatcher getMatcher();

    /**
     * Gets the session manager.
     *
     * @return The session manager
     */
    SessionManager getSessionManager();

    /**
     * Notifies all with the worldguard.notify permission.
     * This will check both superperms and WEPIF,
     * but makes sure WEPIF checks don't result in duplicate notifications
     *
     * @param message The notification to broadcast
     */
    void broadcastNotification(String message);

    /**
     * Notifies all with the worldguard.notify permission.
     * This will check both superperms and WEPIF,
     * but makes sure WEPIF checks don't result in duplicate notifications
     *
     * @param component The notification to broadcast
     */
    void broadcastNotification(TextComponent component);

    /**
     * Load the platform
     */
    void load();

    /**
     * Unload the platform
     */
    void unload();

    /**
     * Gets a RegionContainer.
     *
     * @return The region container
     */
    RegionContainer getRegionContainer();

    /**
     * Gets the handler for debug commands.
     *
     * @return The debug handler
     */
    DebugHandler getDebugHandler();

    /**
     * Gets the servers default game mode.
     *
     * @return The default game mode
     */
    GameMode getDefaultGameMode();

    /**
     * Gets the configuration directory.
     *
     * @return The config directory
     */
    Path getConfigDir();

    /**
     * Stack the inventory of the player
     *
     * @param localPlayer The player
     */
    @Deprecated(forRemoval = true)
    void stackPlayerInventory(LocalPlayer localPlayer);

    /**
     * Adds reports specific to this platform.
     *
     * @param report The reportlist
     */
    void addPlatformReports(ReportList report);

    /**
     * Internal use.
     */
    ProfileService createProfileService(ProfileCache profileCache);

    /**
     * Get a region that encompasses the Vanilla spawn protection for the given world, if applicable.
     *
     * @param world world to check spawn protection of
     * @return a region, or null if not applicable
     */
    @Nullable
    default ProtectedRegion getSpawnProtection(World world) {
        return null;
    }
}
