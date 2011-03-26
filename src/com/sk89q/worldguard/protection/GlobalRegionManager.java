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

import com.sk89q.worldguard.protection.databases.JSONDatabase;
import com.sk89q.worldguard.protection.managers.FlatRegionManager;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class keeps track of region information for every world. It loads
 * world region information as needed.
 * 
 * @author sk89q
 * @author Redecouverte
 */
public class GlobalRegionManager {

    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    /**
     * Path to the folder that region data is stored within.
     */
    protected File dataFolder;
    
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
     */
    public GlobalRegionManager() {
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
        return new File(dataFolder,
                "name" + File.separator + "regions.json");
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
     * @param name
     */
    public void load(String name) {
        File file = getPath(name);
        
        try {
            // Create a manager
            RegionManager manager = new FlatRegionManager(new JSONDatabase(file));
            managers.put(name, manager);
            manager.load();

            // Store the last modification date so we can track changes
            lastModified.put(name, file.lastModified());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions from file "
                    + file.getAbsolutePath() + " : " + e.getMessage());
        }
    }

    /**
     * Reloads the region information from file when region databases
     * have changed.
     */
    public void reloadChanged() {
        for (String name : managers.keySet()) {

            File file = getPath(name);

            Long oldDate = lastModified.get(name);
            
            if (oldDate == null) {
                oldDate = 0L;
            }

            try {
                if (file.lastModified() > oldDate) {
                    load(name);
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Get the region manager for a particular world.
     * 
     * @param name
     * @return
     */
    public RegionManager get(String name) {
        RegionManager manager = managers.get(name);

        if (manager == null) {
            load(name);
        }

        return manager;
    }
}
