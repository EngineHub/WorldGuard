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

import org.bukkit.World;

import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * Helper class to get a reference to WorldGuard and its components.
 */
public class WGBukkit {

    private WGBukkit() {
    }

    /**
     * Get the WorldGuard plugin. If WorldGuard isn't loaded yet, then this will
     * return null.
     * <p>
     * If you are depending on WorldGuard in your plugin, you should place
     * <code>softdepend: [WorldGuard]</code> or <code>depend: [WorldGuard]</code>
     * in your plugin.yml so that this won't return null for you.
     *
     * @return the WorldGuard plugin or null
     */
    public static WorldGuardPlugin getPlugin() {
        return WorldGuardPlugin.inst();
    }

    /**
     * Set cache to null for reload WorldGuardPlugin
     * @deprecated instance is now stored directly in {@link WorldGuardPlugin}
     */
    @Deprecated
    public static void cleanCache() {
        // do nothing - plugin instance is stored in plugin class now
    }

    /**
     * Returns the region manager for a given world. May return null if WorldGuard
     * is not loaded or region protection is not enabled for the given world.
     *
     * @param world world
     * @return a region manager or null
     */
    public static RegionManager getRegionManager(World world) {
        if (getPlugin() == null) {
            return null;
        }
        return WorldGuardPlugin.inst().getRegionManager(world);
    }

}
