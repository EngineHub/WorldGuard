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

package com.sk89q.worldguard.bukkit.util.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating the appropriate scheduler adapter based on the server platform.
 * 
 * <p>This class detects whether the server is running on Folia (which requires
 * region-aware scheduling) or traditional Bukkit/Paper (which uses the classic
 * BukkitScheduler API).</p>
 */
public class SchedulerAdapterFactory {
    
    private static final Logger LOGGER = Logger.getLogger(SchedulerAdapterFactory.class.getName());
    private static SchedulerAdapter cachedAdapter = null;
    
    /**
     * Get the scheduler adapter appropriate for the current server platform.
     * Must be called with a Plugin instance at least once (during plugin init)
     * before the no-arg overload can be used.
     *
     * @param plugin the plugin instance used to test scheduling
     * @return the scheduler adapter instance
     */
    public static synchronized SchedulerAdapter getAdapter(Plugin plugin) {
        if (cachedAdapter == null) {
            cachedAdapter = createAdapter(plugin);
        }
        return cachedAdapter;
    }

    /**
     * Get the cached scheduler adapter. Requires that {@link #getAdapter(Plugin)}
     * has already been called during plugin initialization.
     *
     * @return the cached scheduler adapter instance
     * @throws IllegalStateException if the adapter has not been initialized yet
     */
    public static synchronized SchedulerAdapter getAdapter() {
        if (cachedAdapter == null) {
            throw new IllegalStateException("SchedulerAdapterFactory has not been initialized. "
                + "Call getAdapter(Plugin) first during plugin enable.");
        }
        return cachedAdapter;
    }
    
    private static SchedulerAdapter createAdapter(Plugin plugin) {
        if (isFolia()) {
            try {
                LOGGER.info("Detected Folia server - using region-aware scheduler");
                return new FoliaSchedulerAdapter();
            } catch (ReflectiveOperationException e) {
                LOGGER.log(Level.WARNING, "Failed to initialize Folia scheduler adapter, falling back to Bukkit scheduler", e);
                return createBukkitOrFallbackAdapter(plugin);
            }
        } else {
            return createBukkitOrFallbackAdapter(plugin);
        }
    }
    
    private static SchedulerAdapter createBukkitOrFallbackAdapter(Plugin plugin) {
        if (isBukkitSchedulerAvailable(plugin)) {
            LOGGER.info("Detected Bukkit/Paper server - using traditional scheduler");
            return new BukkitSchedulerAdapter();
        } else {
            LOGGER.warning("BukkitScheduler is disabled (Canvas server?) - using fallback scheduler");
            return new FallbackSchedulerAdapter();
        }
    }
    
    /**
     * Check if we're running on Folia.
     * Checks for a Folia-specific class that does not exist on Canvas or Paper.
     *
     * @return true if running on Folia, false otherwise
     */
    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Check if the Bukkit scheduler is available and functional by actually
     * attempting to schedule a task. Canvas servers have the scheduler object
     * but throw UnsupportedOperationException when any task is scheduled.
     *
     * @param plugin the plugin instance to use for the test
     * @return true if Bukkit scheduler works, false otherwise
     */
    public static boolean isBukkitSchedulerAvailable(Plugin plugin) {
        try {
            org.bukkit.scheduler.BukkitTask task = Bukkit.getScheduler().runTask(plugin, () -> {});
            task.cancel();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get information about the detected platform.
     * 
     * @return platform information string
     */
    public static String getPlatformInfo() {
        if (isFolia()) {
            return "Folia (region-aware scheduling)";
        }
        return "Bukkit/Paper or Canvas";
    }
}