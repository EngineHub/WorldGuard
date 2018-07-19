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

package com.sk89q.worldguard;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.SimpleFlagRegistry;

import java.util.logging.Logger;

public class WorldGuard {

    public static final Logger logger = Logger.getLogger(WorldGuard.class.getCanonicalName());

    private static final WorldGuard instance = new WorldGuard();
    private WorldGuardPlatform platform;
    private final SimpleFlagRegistry flagRegistry = new SimpleFlagRegistry();

    public static WorldGuard getInstance() {
        return instance;
    }

    public WorldGuard() {
        flagRegistry.setInitialized(true);
    }

    /**
     * The WorldGuard Platform.
     *
     * @return The platform
     */
    public WorldGuardPlatform getPlatform() {
        checkNotNull(platform);
        return platform;
    }

    public void setPlatform(WorldGuardPlatform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    /**
     * Get the flag registry.
     *
     * @return the flag registry
     */
    public FlagRegistry getFlagRegistry() {
        return this.flagRegistry;
    }
}
