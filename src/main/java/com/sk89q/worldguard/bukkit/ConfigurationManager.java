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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.blacklist.Blacklist;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/**
 * Represents the global configuration and also delegates configuration
 * for individual worlds.
 * 
 * @author sk89q
 * @author Michael
 */
public class ConfigurationManager {

    /**
     * Reference to the plugin.
     */
    private WorldGuardPlugin plugin;
    
    /**
     * Holds configurations for different worlds.
     */
    private Map<String, WorldConfiguration> worlds;
    
    /**
     * List of people with god mode.
     */
    private Set<String> hasGodMode = new HashSet<String>();
    
    /**
     * List of people who can breathe underwater.
     */
    private Set<String> hasAmphibious = new HashSet<String>();
    
    public boolean suppressTickSyncWarnings;
    public boolean useRegionsScheduler;
    public boolean activityHaltToggle = false;

    /**
     * Construct the object.
     * 
     * @param plugin
     */
    public ConfigurationManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.worlds = new HashMap<String, WorldConfiguration>();
    }

    /**
     * Load the configuration.
     */
    public void load() {
        // Create the default configuration file
        WorldGuardPlugin.createDefaultConfiguration(
                new File(plugin.getDataFolder(), "config.yml"), "config.yml");
        
        Configuration config = plugin.getConfiguration();
        config.load();

        suppressTickSyncWarnings = config.getBoolean(
                "suppress-tick-sync-warnings", false);
        useRegionsScheduler = config.getBoolean(
                "regions.use-scheduler", true);

        // Load configurations for each world
        for (World world : plugin.getServer().getWorlds()) {
            get(world);
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
     * @param world
     * @return
     */
    public WorldConfiguration get(World world) {
        String worldName = world.getName();
        WorldConfiguration config = worlds.get(worldName);
        
        if (config == null) {
            config = new WorldConfiguration(plugin, worldName);
            worlds.put(worldName, config);
        }

        return config;
    }

    /**
     * Forget a player.
     * 
     * @param player
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
     * @param player
     */
    public void enableGodMode(Player player) {
        hasGodMode.add(player.getName());
    }
    
    /**
     * Disable god mode for a player.
     * 
     * @param player
     */
    public void disableGodMode(Player player) {
        hasGodMode.remove(player.getName());
    }
    
    /**
     * Check to see if god mode is enabled for a player.
     * 
     * @param player
     * @return 
     */
    public boolean hasGodMode(Player player) {
        return hasGodMode.contains(player.getName());
    }
    
    /**
     * Enable amphibious mode for a player.
     * 
     * @param player
     */
    public void enableAmphibiousMode(Player player) {
        hasAmphibious.add(player.getName());
    }
    
    /**
     * Disable amphibious mode  for a player.
     * 
     * @param player
     */
    public void disableAmphibiousMode(Player player) {
        hasAmphibious.remove(player.getName());
    }
    
    /**
     * Check to see if amphibious mode  is enabled for a player.
     * 
     * @param player
     * @return 
     */
    public boolean hasAmphibiousMode(Player player) {
        return hasAmphibious.contains(player.getName());
    }
}
