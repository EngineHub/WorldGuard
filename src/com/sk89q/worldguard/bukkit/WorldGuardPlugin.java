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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.sk89q.worldguard.protection.CSVDatabase;
import com.sk89q.worldguard.protection.FlatRegionManager;
import com.sk89q.worldguard.protection.ProtectionDatabase;
import com.sk89q.worldguard.protection.RegionManager;

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

    RegionManager regionManager = new FlatRegionManager();
    ProtectionDatabase regionLoader =
            new CSVDatabase(new File("worldguard-regions.txt"));
    
    Set<String> invinciblePlayers = new HashSet<String>();
    Set<String> amphibiousPlayers = new HashSet<String>();
    Map<String,Long> recentLogins = new HashMap<String,Long>();
    Map<String,Long> lastSpawn = new HashMap<String,Long>();

    boolean stopFireSpread = false;

    boolean useRegions = false;
    boolean enforceOneSession;
    boolean blockCreepers;
    boolean blockTNT;
    boolean blockLighter;
    boolean preventLavaFire;
    boolean disableAllFire;
    boolean simulateSponge;
    int spongeRadius;
    Set<Integer> fireNoSpreadBlocks;
    Set<Integer> allowedLavaSpreadOver;
    Set<Integer> itemDropBlacklist;
    Set<Integer> preventWaterDamage;
    boolean classicWater;
    boolean noPhysicsGravel;
    boolean noPhysicsSand;
    boolean allowPortalAnywhere;
    boolean disableFallDamage;
    boolean disableLavaDamage;
    boolean disableFireDamage;
    boolean disableWaterDamage;
    boolean disableSuffocationDamage;
    boolean teleportOnSuffocation;
    int loginProtection;
    int spawnProtection;
    boolean teleportToHome;
    boolean exactRespawn;
    boolean kickOnDeath;
    int regionWand = 287; 
    
    public WorldGuardPlugin(PluginLoader pluginLoader, Server instance,
            PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        logger.info("WorldGuard " + desc.getVersion() + " loaded.");
        loadConfiguration();
        registerEvents();
    }

    public void onEnable() {
        //loadConfiguration();
    }

    public void onDisable() {
    }

    private void registerEvents() {        
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT,
                playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND,
                playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGED,
                blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICKED,
                blockListener, Priority.Normal, this);
    }

    /**
     * Load the configuration
     */
    public void loadConfiguration() {
        Configuration config = getConfiguration();

        try {
            regionLoader.load();
            regionManager.setRegions(regionLoader.getRegions());
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions: "
                    + e.getMessage());
        }
        
        recentLogins.clear();

        // Load basic options
        enforceOneSession = config.getBoolean("protection.enforce-single-session", true);
        
        blockCreepers = config.getBoolean("mobs.block-creeper-explosions", false);
        
        blockTNT = config.getBoolean("ignition.block-tnt", false);
        blockLighter = config.getBoolean("ignition.block-lighter", false);
        
        preventLavaFire = config.getBoolean("fire.disable-lava-fire-spread", true);
        disableAllFire = config.getBoolean("fire.disable-fire-spread", false);
        preventWaterDamage = new HashSet<Integer>(config.getIntList("physics.disable-water-damage-blocks", null));
        itemDropBlacklist = new HashSet<Integer>(config.getIntList("protection.item-drop-blacklist", null));
        fireNoSpreadBlocks = new HashSet<Integer>(config.getIntList("fire.disable-fire-spread", null));
        allowedLavaSpreadOver = new HashSet<Integer>(config.getIntList("fire.lava-spread-blocks", null));
        
        classicWater = config.getBoolean("simulation.classic-water", false);
        simulateSponge = config.getBoolean("simulation.sponge.enable", true);
        spongeRadius = Math.max(1, config.getInt("simulation.sponge.radius", 3)) - 1;
        
        noPhysicsGravel = config.getBoolean("physics.no-physics-gravel", false);
        noPhysicsSand = config.getBoolean("physics.no-physics-sand", false);
        allowPortalAnywhere = config.getBoolean("physics.allow-portal-anywhere", false);
        
        disableFallDamage = config.getBoolean("player-damage.disable-fall-damage", false);
        disableLavaDamage = config.getBoolean("player-damage.disable-lava-damage", false);
        disableFireDamage = config.getBoolean("player-damage.disable-fire-damage", false);
        disableWaterDamage = config.getBoolean("player-damage.disable-water-damage", false);
        disableSuffocationDamage = config.getBoolean("player-damage.disable-suffocation-damage", false);
        teleportOnSuffocation = config.getBoolean("player-damage.teleport-on-suffocation", false);
        
        loginProtection = config.getInt("spawn.login-protection", 3);
        spawnProtection = config.getInt("spawn.spawn-protection", 0);
        kickOnDeath = config.getBoolean("spawn.kick-on-death", false);
        teleportToHome = config.getBoolean("spawn.teleport-to-home-on-death", false);
        exactRespawn = config.getBoolean("spawn.exact-respawn", false);
        
        useRegions = config.getBoolean("regions.enable", true);
        regionWand = config.getInt("regions.wand", 287);

        /*
        // Console log configuration
        boolean logConsole = properties.getBoolean("log-console", true);

        // Database log configuration
        boolean logDatabase = properties.getBoolean("log-database", false);
        String dsn = properties.getString("log-database-dsn", "jdbc:mysql://localhost:3306/minecraft");
        String user = properties.getString("log-database-user", "root");
        String pass = properties.getString("log-database-pass", "");
        String table = properties.getString("log-database-table", "blacklist_events");

        // File log configuration
        boolean logFile = properties.getBoolean("log-file", false);
        String logFilePattern = properties.getString("log-file-path", "worldguard/logs/%Y-%m-%d.log");
        int logFileCacheSize = Math.max(1, properties.getInt("log-file-open-files", 10));

        // Load the blacklist
        try {
            // If there was an existing blacklist, close loggers
            if (blacklist != null) {
                blacklist.getLogger().close();
            }

            // First load the blacklist data from worldguard-blacklist.txt
            Blacklist blist = new Blacklist();
            blist.load(new File("worldguard-blacklist.txt"));

            // If the blacklist is empty, then set the field to null
            // and save some resources
            if (blist.isEmpty()) {
                this.blacklist = null;
            } else {
                this.blacklist = blist;
                logger.log(Level.INFO, "WorldGuard: Blacklist loaded.");

                BlacklistLogger blacklistLogger = blist.getLogger();

                if (logDatabase) {
                    blacklistLogger.addHandler(new DatabaseLoggerHandler(dsn, user, pass, table));
                }

                if (logConsole) {
                    blacklistLogger.addHandler(new ConsoleLoggerHandler());
                }

                if (logFile) {
                    FileLoggerHandler handler =
                            new FileLoggerHandler(logFilePattern, logFileCacheSize);
                    blacklistLogger.addHandler(handler);
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "WorldGuard blacklist does not exist.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load WorldGuard blacklist: "
                    + e.getMessage());
        }*/

        // Print an overview of settings
        if (config.getBoolean("summary-on-start", true)) {
            logger.log(Level.INFO, enforceOneSession ? "WorldGuard: Single session is enforced."
                    : "WorldGuard: Single session is NOT ENFORCED.");
            logger.log(Level.INFO, blockTNT ? "WorldGuard: TNT ignition is blocked."
                    : "WorldGuard: TNT ignition is PERMITTED.");
            logger.log(Level.INFO, blockLighter ? "WorldGuard: Lighters are blocked."
                    : "WorldGuard: Lighters are PERMITTED.");
            logger.log(Level.INFO, preventLavaFire ? "WorldGuard: Lava fire is blocked."
                    : "WorldGuard: Lava fire is PERMITTED.");
            if (disableAllFire) {
                logger.log(Level.INFO, "WorldGuard: All fire spread is disabled.");
            } else {
                if (fireNoSpreadBlocks != null) {
                    logger.log(Level.INFO, "WorldGuard: Fire spread is limited to "
                            + fireNoSpreadBlocks.size() + " block types.");
                } else {
                    logger.log(Level.INFO, "WorldGuard: Fire spread is UNRESTRICTED.");
                }
            }
        }
    }
    
    boolean inGroup(Player player, String group) {
        return true;
    }
    
    boolean hasPermission(Player player, String hasPermission) {
        return true;
    }
    
    List<String> getGroups(Player player) {
        return new ArrayList<String>();
    }

    BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, player);
    }
}
