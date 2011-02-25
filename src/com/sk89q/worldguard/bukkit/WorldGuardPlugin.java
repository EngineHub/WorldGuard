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

import java.util.logging.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.commands.CommandHandler;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.protection.regionmanager.GlobalRegionManager;
import org.bukkit.plugin.PluginManager;

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
    private final WorldGuardServerListener serverListener =
            new WorldGuardServerListener(this);
    
    private final CommandHandler commandHandler = new CommandHandler(this);
    private final GlobalRegionManager globalRegionManager = new GlobalRegionManager(this);
    private final WorldGuardConfiguration configuration = new WorldGuardConfiguration(this);


    /**
     * Called on plugin enable.
     */
    public void onEnable() {

        getDataFolder().mkdirs();
        globalRegionManager.onEnable();
        registerEvents();

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
     * Register used events.
     */
    private void registerEvents() {

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.High, this);
        pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.High, this);

        pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.High, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.High, this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.High, this);

        pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.High, this);
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.High, this);
        pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.High, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);

        pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);

        // 25 equals about 1s real time
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimedFlagsTimer(this), 25 * 5, 25 * 5);
    }

    /**
     * Handles a command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return commandHandler.handleCommand(sender, cmd, commandLabel, args);
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
