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

import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler;
import com.sk89q.worldguard.protection.TimedFlagsTimer;
import com.sk89q.worldguard.protection.regionmanager.GlobalRegionManager;
import java.util.logging.Logger;

/**
 * Plugin for Bukkit.
 * 
 * @author sk89qs
 */
public class WorldGuardPlugin extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    private final WorldGuardPlayerListener playerListener =
            new WorldGuardPlayerListener(this);
    private final WorldGuardBlockListener blockListener =
            new WorldGuardBlockListener(this);
    private final WorldGuardEntityListener entityListener =
            new WorldGuardEntityListener(this);
    private final WorldGuardVehicleListener vehicleListener =
            new WorldGuardVehicleListener(this);
    
    private final CommandHandler commandHandler = new CommandHandler(this);
    private final GlobalRegionManager globalRegionManager = new GlobalRegionManager(this);
    private final WorldGuardConfiguration configuration = new WorldGuardConfiguration(this);


    /**
     * Called on plugin enable.
     */
    public void onEnable() {

        getDataFolder().mkdirs();
        globalRegionManager.onEnable();

        playerListener.registerEvents();
        blockListener.registerEvents();
        entityListener.registerEvents();
        vehicleListener.registerEvents();

        // 25 equals about 1s real time
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimedFlagsTimer(this), 25 * 5, 25 * 5);

        commandHandler.registerCommands();

        logger.info("WorldGuard " + this.getDescription().getVersion() + " enabled.");
    }

    /**
     * Called on plugin disable.
     */
    public void onDisable() {

        globalRegionManager.onDisable();

        logger.info("WorldGuard " + this.getDescription().getVersion() + " disabled.");
    }


    /**
     * Get the GlobalRegionManager.
     * 
     * @return
     */
    public GlobalRegionManager getGlobalRegionManager() {
        return globalRegionManager;
    }

    /**
     * Get the WorldGuardConfiguraton.
     *
     * @return
     */
    public WorldGuardConfiguration getWgConfiguration() {
        return configuration;
    }
}
