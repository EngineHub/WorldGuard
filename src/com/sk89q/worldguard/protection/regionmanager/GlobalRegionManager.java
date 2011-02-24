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
    private GlobalFlags globalFlags;
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    public GlobalRegionManager(WorldGuardPlugin wg) {

        this.wg = wg;
        this.managers = new HashMap<String, RegionManager>();
        this.globalFlags = new GlobalFlags();

        for (World w : wg.getServer().getWorlds()) {
            loadWorld(w.getName());
        }
    }

    private void loadWorld(String name) {

        String filename = name + ".regions.json";
        try {
            managers.put(name, new FlatRegionManager(globalFlags, new JSONDatabase(new File(wg.getDataFolder(), filename))));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions from file " + filename + " : "
                    + e.getMessage());
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

    public void setGlobalFlags(GlobalFlags globalflags) {

        if (globalflags != null) {
            this.globalFlags = globalflags;
            for (Map.Entry<String, RegionManager> entry : managers.entrySet()) {
                entry.getValue().setGlobalFlags(globalflags);
            }
        }

    }
}
