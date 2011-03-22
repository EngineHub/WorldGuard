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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.worldguard.bukkit.commands.CommandHandler;
import com.sk89q.worldguard.bukkit.commands.CommandHandler.InsufficientPermissionsException;
import com.sk89q.worldguard.protection.TimedFlagsTimer;
import com.sk89q.worldguard.protection.regionmanager.GlobalRegionManager;
import com.sk89q.worldguard.protection.regions.flags.Flags;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Plugin for Bukkit.
 * 
 * @author sk89q
 */
public class WorldGuardPlugin extends JavaPlugin {

    protected static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    protected final CommandHandler commandHandler = new CommandHandler(this);
    protected final GlobalRegionManager globalRegionManager = new GlobalRegionManager(this);
    protected final GlobalConfiguration configuration = new GlobalConfiguration(this);
    protected PermissionsResolverManager perms;

    /**
     * Called on plugin enable.
     */
    public void onEnable() {
        getDataFolder().mkdirs();
        Flags.Init();
        globalRegionManager.onEnable();

        // Register events
        (new WorldGuardPlayerListener(this)).registerEvents();
        (new WorldGuardBlockListener(this)).registerEvents();
        (new WorldGuardEntityListener(this)).registerEvents();
        (new WorldGuardVehicleListener(this)).registerEvents();

        // 25 equals about 1s real time
        this.getServer().getScheduler().scheduleSyncRepeatingTask(
                this, new TimedFlagsTimer(this), 25 * 5, 25 * 5);

        commandHandler.registerCommands();

        // Set up permissions
        perms = new PermissionsResolverManager(
                getConfiguration(), getServer(), "WorldGuard", logger);
        (new PermissionsResolverServerListener(perms)).register(this);
        perms.load();
        
        logger.info("WorldGuard " + this.getDescription().getVersion() + " enabled.");
    }

    /**
     * Called on plugin disable.
     */
    public void onDisable() {
        globalRegionManager.onDisable();

        logger.info("WorldGuard " + getDescription().getVersion() + " disabled.");
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
    public GlobalConfiguration getGlobalConfiguration() {
        return configuration;
    }

    /**
     * Check whether a player is in a group.
     * 
     * @param player
     * @param group
     * @return
     */
    public boolean inGroup(Player player, String group) {
        try {
            return perms.inGroup(player.getName(), group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Get the groups of a player.
     * 
     * @param player
     * @return
     */
    public String[] getGroups(Player player) {
        try {
            return perms.getGroups(player.getName());
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Checks whether a player has a permission.
     * 
     * @param player
     * @param perm
     * @return
     */
    public boolean hasPermission(Player player, String perm) {
        try {
            return player.isOp()
                    || perms.hasPermission(player.getName(),
                            "worldguard." + perm);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    /**
    * Checks to see if there are sufficient permissions, otherwise an exception
    * is raised in that case.
    *
    * @param sender
    * @param permission
    * @throws InsufficientPermissionsException
    */
    public void checkPermission(CommandSender sender, String permission)
            throws InsufficientPermissionsException {
        if (!(sender instanceof Player)) {
            return;
        }
        if (!hasPermission((Player)sender, permission)) {
            throw new InsufficientPermissionsException();
        }
    }

    /**
     * Create a default configuration file from the .jar.
     * 
     * @param actual 
     * @param defaultName 
     */
    public static void createDefaultConfiguration(File actual,
            String defaultName) {

        if (actual.exists()) {
            return;
        }

        InputStream input = WorldGuardPlugin.class
                .getResourceAsStream("/defaults/" + defaultName);
        
        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length = 0;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                logger.info("WorldGuard: Default configuration file written: "
                        + defaultName);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }
}
