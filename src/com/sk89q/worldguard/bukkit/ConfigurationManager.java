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
import java.util.Map;
import org.bukkit.World;
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
    
    public boolean suppressTickSyncWarnings;

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

        // Load configurations for each world
        for (World world : plugin.getServer().getWorlds()) {
            forWorld(world.getName());
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
     * @param worldName
     * @return
     */
    public WorldConfiguration forWorld(String worldName) {
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
    }
}
