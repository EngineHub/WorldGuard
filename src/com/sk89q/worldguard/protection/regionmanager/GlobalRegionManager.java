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
package com.sk89q.worldguard.protection.regionmanager;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.GlobalFlags;
import com.sk89q.worldguard.protection.dbs.JSONDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.World;

/**
 *
 * @author Redecouverte
 */
public class GlobalRegionManager {

    private WorldGuardPlugin wg;
    private HashMap<String, RegionManager> managers;
    private HashMap<String, Long> managerFileDates;
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    public GlobalRegionManager(WorldGuardPlugin wg) {

        this.wg = wg;
        this.managers = new HashMap<String, RegionManager>();
        this.managerFileDates = new HashMap<String, Long>();
    }

    public void onEnable() {

        this.managers.clear();
        this.managerFileDates.clear();

        for (World w : wg.getServer().getWorlds()) {
            loadWorld(w.getName());
        }

        wg.getWgConfiguration().onEnable();
    }

    public void onDisable() {
        wg.getWgConfiguration().onDisable();
    }

    private void loadWorld(String name) {

        String filename = name + ".regions.json";
        try {
            File file = new File(wg.getDataFolder(), filename);
            managerFileDates.put(name, file.lastModified());
            managers.put(name, new FlatRegionManager(new GlobalFlags(), new JSONDatabase(file)));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions from file " + filename + " : "
                    + e.getMessage());
        }
    }

    public void reloadDataWhereRequired() {

        for (String name : managers.keySet()) {

            String filename = name + ".regions.json";
            File file = new File(wg.getDataFolder(), filename);

            Long oldDate = managerFileDates.get(name);
            if (oldDate == null) {
                oldDate = new Long(0);
            }

            try {
                if (file.lastModified() > oldDate) {
                    loadWorld(name);
                }
            } catch (Exception e) {
            }
        }
    }

    public RegionManager getRegionManager(String worldName) {

        RegionManager ret = this.managers.get(worldName);

        if (ret == null) {
            if (wg.getServer().getWorld(worldName) != null) {
                loadWorld(worldName);
            }
        }

        return ret;
    }

    public void setGlobalFlags(String worldName, GlobalFlags globalflags) {

        if (globalflags != null) {
            RegionManager ret = this.managers.get(worldName);

            if (ret == null) {
                ret.setGlobalFlags(globalflags);
            }
        }

    }
}
