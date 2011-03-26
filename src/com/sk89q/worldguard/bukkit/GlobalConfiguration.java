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

import com.nijiko.coelho.iConomy.iConomy;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.TickSyncDelayLoggerFilter;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.protection.databases.CSVDatabase;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/**
 * Represents the global configuration and also delegates configuration
 * for individual worlds.
 * 
 * @author Michael
 * @author sk89q
 */
public class GlobalConfiguration {

    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    private WorldGuardPlugin plugin;
    private Map<String, WorldConfiguration> worldConfig;

    protected Set<String> invinciblePlayers = new HashSet<String>();
    protected Set<String> amphibiousPlayers = new HashSet<String>();

    private iConomy iConomy;

    /**
     * Construct the object.
     * 
     * @param plugin
     */
    public GlobalConfiguration(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        this.worldConfig = new HashMap<String, WorldConfiguration>();
        this.iConomy = null;
    }

    /**
     * Get the configuration for a world.
     * 
     * @param worldName
     * @return
     */
    public WorldConfiguration getWorldConfig(String worldName) {
        WorldConfiguration ret = worldConfig.get(worldName);
        if (ret == null) {
            ret = createWorldConfig(worldName);
            worldConfig.put(worldName, ret);
        }

        return ret;
    }

    /**
     * Create the configuration for a world.
     * 
     * @param worldName
     * @return
     */
    private WorldConfiguration createWorldConfig(String worldName) {
        File baseFolder = new File(plugin.getDataFolder(), worldName);
        File configFile = new File(baseFolder, "config.yml");
        File blacklistFile = new File(baseFolder, "blacklist.txt");

        return new WorldConfiguration(plugin, worldName, configFile, blacklistFile);
    }

    /**
     * Load the configuration files.
     */
    public void load() {
        checkOldConfigFiles();
        checkOldCSVDB();
        
        Configuration config = plugin.getConfiguration();

        boolean suppressTickSyncWarnings = config.getBoolean(
                "suppress-tick-sync-warnings", false);

        invinciblePlayers.clear();
        amphibiousPlayers.clear();

        // Build initial lists of users matching the criteria
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (plugin.inGroup(player, "wg-invincible"))
                invinciblePlayers.add(player.getName());

            if (plugin.inGroup(player, "wg-amphibious"))
                amphibiousPlayers.add(player.getName());
        }
        
        if (suppressTickSyncWarnings) {
            Logger.getLogger("Minecraft").setFilter(
                    new TickSyncDelayLoggerFilter());
        } else {
            Filter filter = Logger.getLogger("Minecraft").getFilter();
            if (filter != null && filter instanceof TickSyncDelayLoggerFilter) {
                Logger.getLogger("Minecraft").setFilter(null);
            }
        }
        
        loadWorldConfiguration();
    }

    public void unload() {
    }
    
    /**
     * Load the configurations for the different worlds.
     */
    private void loadWorldConfiguration() {
        worldConfig.clear();
        
        for (World w : plugin.getServer().getWorlds()) {
            String worldName = w.getName();
            worldConfig.put(worldName, createWorldConfig(worldName));
        }
    }

    /**
     * Check for old pre-4.x configuration files.
     */
    private void checkOldConfigFiles() {
        File oldFile = new File(plugin.getDataFolder(), "blacklist.txt");
        
        if (oldFile.exists()) {
            logger.warning("WorldGuard: blacklist.txt is outdated, please "
                    + "reapply your configuration in <world>/blacklist.txt");
            logger.warning("WorldGuard: blacklist.txt renamed to blacklist.txt.old");
            
            oldFile.renameTo(new File(plugin.getDataFolder(),
                    "blacklist.txt.old"));
        }
    }

    /**
     * Check for old world databases from pre-4.x versions. Also convert them
     * over to the new format.
     */
    private void checkOldCSVDB() {
        try {
            File oldDatabase = new File(plugin.getDataFolder(), "regions.txt");
            if (!oldDatabase.exists()) return;
            
            logger.info("WorldGuard: The regions database has changed in 4.x. "
                    + "Your old regions database will be converted to the new format "
                    + "and set as your primarily world's database.");
            logger.info("WorldGuard: Converting...");

            // We're assuming that the regions 
            World w = plugin.getServer().getWorlds().get(0);
            
            RegionManager mgr = plugin.getGlobalRegionManager()
                    .get(w.getName());

            // First load up the old database using the CSV loader
            CSVDatabase db = new CSVDatabase(oldDatabase);
            db.load();
            
            // Then save the new database
            mgr.setRegions(db.getRegions());
            mgr.save();

            logger.info("WorldGuard: regions.txt has been renamed to regions.txt.old.");
            
            oldDatabase.renameTo(new File(plugin.getDataFolder(), "regions.txt.old"));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions: "
                    + e.getMessage());
        }
    }

    /**
     * Check if a player has permission to build at a location.
     * 
     * @param player
     * @param pt
     * @return
     */
    public boolean canBuild(Player player, Vector pt) {
        if (getWorldConfig(player.getWorld().getName()).useRegions) {
            LocalPlayer localPlayer = plugin.wrapPlayer(player);

            if (!plugin.hasPermission(player, "region.bypass")) {
                RegionManager mgr = plugin.getGlobalRegionManager()
                        .get(player.getWorld().getName());

                if (!mgr.getApplicableRegions(pt).canBuild(localPlayer)) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    /**
     * Check if a player has permission to build at a location.
     * 
     * @param player
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean canBuild(Player player, int x, int y, int z) {
        return canBuild(player, new Vector(x, y, z));
    }

    /**
     * Check whether a player can breathe underwater.
     * 
     * @param playerName
     * @return
     */
    public boolean isAmphibiousPlayer(String playerName) {
        if (amphibiousPlayers.contains(playerName)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether a player is invincible.
     * 
     * @param playerName
     * @return
     */
    public boolean isInvinciblePlayer(String playerName) {
        if (invinciblePlayers.contains(playerName)) {
            return true;
        }

        return false;
    }

    public void addAmphibiousPlayer(String playerName) {
        amphibiousPlayers.add(playerName);
    }

    public void addInvinciblePlayer(String playerName) {
        invinciblePlayers.add(playerName);
    }

    public void removeAmphibiousPlayer(String playerName) {
        amphibiousPlayers.remove(playerName);
    }

    public void removeInvinciblePlayer(String playerName) {
        invinciblePlayers.remove(playerName);
    }

    /**
     * Forget all players.
     * @param player
     */
    public void forgetPlayer(LocalPlayer player) {

        for (Map.Entry<String, WorldConfiguration> entry : worldConfig
                .entrySet()) {
            Blacklist bl = entry.getValue().getBlacklist();
            if (bl != null) {
                bl.forgetPlayer(player);
            }
        }

    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return this.plugin;
    }

    public iConomy getiConomy() {
        return this.iConomy;
    }

    public void setiConomy(iConomy newVal) {
        this.iConomy = newVal;
    }
}
