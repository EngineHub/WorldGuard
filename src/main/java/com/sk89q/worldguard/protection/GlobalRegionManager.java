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
package com.sk89q.worldguard.protection;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import java.io.File;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.bukkit.WorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.ProtectionDatabase;
import com.sk89q.worldguard.protection.databases.YAMLDatabase;
import com.sk89q.worldguard.protection.databases.MySQLDatabase;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.FlatRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * This class keeps track of region information for every world. It loads
 * world region information as needed.
 *
 * @author sk89q
 * @author Redecouverte
 */
public class GlobalRegionManager {

    /**
     * Reference to the plugin.
     */
    private WorldGuardPlugin plugin;

    /**
     * Reference to the global configuration.
     */
    private ConfigurationManager config;

    /**
     * Map of managers per-world.
     */
    private HashMap<String, RegionManager> managers;

    /**
     * Stores the list of modification dates for the world files. This allows
     * WorldGuard to reload files as needed.
     */
    private HashMap<String, Long> lastModified;

    /**
     * Construct the object.
     *
     * @param plugin
     */
    public GlobalRegionManager(WorldGuardPlugin plugin) {
        this.plugin = plugin;
        config = plugin.getGlobalStateManager();
        managers = new HashMap<String, RegionManager>();
        lastModified = new HashMap<String, Long>();
    }

    /**
     * Unload region information.
     */
    public void unload() {
        managers.clear();
        lastModified.clear();
    }

    /**
     * Get the path for a world's regions file.
     *
     * @param name
     * @return
     */
    protected File getPath(String name) {
        return new File(plugin.getDataFolder(),
                "worlds" + File.separator + name + File.separator + "regions.yml");
    }

    /**
     * Unload region information for a world.
     *
     * @param name
     */
    public void unload(String name) {
        RegionManager manager = managers.get(name);

        if (manager != null) {
            managers.remove(name);
            lastModified.remove(name);
        }
    }

    /**
     * Unload all region information.
     */
    public void unloadAll() {
        managers.clear();
        lastModified.clear();
    }

    /**
     * Load region information for a world.
     *
     * @param world
     * @return
     */
    public RegionManager load(World world) {
        String name = world.getName();
        ProtectionDatabase database = null;
        File file = null;
        
        try {
            if (!config.useSqlDatabase) {
                file = getPath(name);
                database = new YAMLDatabase(file, plugin.getLogger());

                // Store the last modification date so we can track changes
                lastModified.put(name, file.lastModified());
            } else {
                database = new MySQLDatabase(config, name, plugin.getLogger());
            }

            // Create a manager
            RegionManager manager = new FlatRegionManager(database);

            managers.put(name, manager);
            manager.load();

            plugin.getLogger().info(manager.getRegions().size()
                    + " regions loaded for '" + name + "'");

            return manager;
        } catch (ProtectionDatabaseException e) {
            String logStr = "Failed to load regions from ";
            if (config.useSqlDatabase) {
                logStr += "SQL Database <" + config.sqlDsn + "> ";
            } else {
                logStr += "file \"" + file + "\" ";
            }

            plugin.getLogger().info(logStr + " : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            plugin.getLogger().info("Error loading regions for world \""
                    + name + "\": " + e.toString() + "\n\t" + e.getMessage());
            e.printStackTrace();
        }

        // @TODO: THIS CREATES PROBLEMS!!one!!1!!eleven!!1!!!
        return null;
    }

    /**
     * Preloads region managers for all worlds.
     */
    public void preload() {
        // Load regions
        for (World world : plugin.getServer().getWorlds()) {
            load(world);
        }
    }

    /**
     * Reloads the region information from file when region databases
     * have changed.
     */
    public void reloadChanged() {
        if (config.useSqlDatabase) return;

        for (String name : managers.keySet()) {
            File file = getPath(name);

            Long oldDate = lastModified.get(name);

            if (oldDate == null) {
                oldDate = 0L;
            }

            try {
                if (file.lastModified() > oldDate) {
                    World world = plugin.getServer().getWorld(name);

                    if (world != null) {
                        load(world);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Get the region manager for a particular world.
     *
     * @param world
     * @return
     */
    public RegionManager get(World world) {
        RegionManager manager = managers.get(world.getName());

        if (manager == null) {
            manager = load(world);
        }

        return manager;
    }

    /**
     * Returns whether the player can bypass.
     *
     * @param player
     * @param world
     * @return
     */
    public boolean hasBypass(LocalPlayer player, World world) {
        return player.hasPermission("worldguard.region.bypass."
                        + world.getName());
    }

    /**
     * Returns whether the player can bypass.
     *
     * @param player
     * @param world
     * @return
     */
    public boolean hasBypass(Player player, World world) {
        return plugin.hasPermission(player, "worldguard.region.bypass."
                        + world.getName());
    }

    /**
     * Check if a player has permission to build at a block.
     *
     * @param player
     * @param block
     * @return
     */
    public boolean canBuild(Player player, Block block) {
        return canBuild(player, block.getLocation());
    }

    /**
     * Check if a player has permission to build at a location.
     *
     * @param player
     * @param loc
     * @return
     */
    public boolean canBuild(Player player, Location loc) {
        World world = loc.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        LocalPlayer localPlayer = plugin.wrapPlayer(player);

        if (!hasBypass(player, world)) {
            RegionManager mgr = get(world);

            if (!mgr.getApplicableRegions(BukkitUtil.toVector(loc))
                    .canBuild(localPlayer)) {
                return false;
            }
        }

        return true;
    }

    public boolean allows(StateFlag flag, Location loc) {
        return allows(flag, loc, null);
    }

    /**
     * Checks to see whether a flag is allowed.
     *
     * @param flag
     * @param loc
     * @return
     */
    public boolean allows(StateFlag flag, Location loc, LocalPlayer player) {
        World world = loc.getWorld();
        WorldConfiguration worldConfig = config.get(world);

        if (!worldConfig.useRegions) {
            return true;
        }

        RegionManager mgr = plugin.getGlobalRegionManager().get(world);
        return mgr.getApplicableRegions(toVector(loc)).allows(flag, player);
    }
}
