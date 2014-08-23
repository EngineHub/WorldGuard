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

package com.sk89q.worldguard.bukkit.listener;

import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract listener to ease creation of listeners.
 */
class AbstractListener implements Listener {

    private final WorldGuardPlugin plugin;

    /**
     * Construct the listener.
     *
     * @param plugin an instance of WorldGuardPlugin
     */
    public AbstractListener(WorldGuardPlugin plugin) {
        checkNotNull(plugin);
        this.plugin = plugin;
    }

    /**
     * Register events.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Get the plugin.
     *
     * @return the plugin
     */
    protected WorldGuardPlugin getPlugin() {
        return plugin;
    }

    /**
     * Get the global configuration.
     *
     * @return the configuration
     */
    protected ConfigurationManager getConfig() {
        return plugin.getGlobalStateManager();
    }

    /**
     * Get the world configuration given a world.
     *
     * @param world The world to get the configuration for.
     * @return The configuration for {@code world}
     */
    protected WorldConfiguration getWorldConfig(World world) {
        return plugin.getGlobalStateManager().get(world);
    }

    /**
     * Get the world configuration given a player.
     *
     * @param player The player to get the wold from
     * @return The {@link WorldConfiguration} for the player's world
     */
    protected WorldConfiguration getWorldConfig(Player player) {
        return getWorldConfig(player.getWorld());
    }

    /**
     * Return whether region support is enabled.
     *
     * @param world the world
     * @return true if region support is enabled
     */
    protected boolean isRegionSupportEnabled(World world) {
        return getWorldConfig(world).useRegions;
    }

}
