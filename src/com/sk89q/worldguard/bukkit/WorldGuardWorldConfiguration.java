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

import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.blacklist.BlacklistLogger;
import com.sk89q.worldguard.blacklist.loggers.ConsoleLoggerHandler;
import com.sk89q.worldguard.blacklist.loggers.DatabaseLoggerHandler;
import com.sk89q.worldguard.blacklist.loggers.FileLoggerHandler;
import com.sk89q.worldguard.protection.GlobalFlags;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Michael
 */
public class WorldGuardWorldConfiguration {

    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");

    private WorldGuardPlugin wp;
    
    private String worldName;
    private File configFile;
    private File blacklistFile;

    private Blacklist blacklist;


    /* Configuration data start */
    public boolean fireSpreadDisableToggle;
    public boolean enforceOneSession;
    public boolean itemDurability;
    public boolean classicWater;
    public boolean simulateSponge;
    public int spongeRadius;
    public boolean redstoneSponges;
    public boolean noPhysicsGravel;
    public boolean noPhysicsSand;
    public boolean allowPortalAnywhere;
    public Set<Integer> preventWaterDamage;
    public boolean blockTNT;
    public boolean blockLighter;
    public boolean disableFireSpread;
    public Set<Integer> disableFireSpreadBlocks;
    public boolean preventLavaFire;
    public Set<Integer> allowedLavaSpreadOver;
    public boolean blockCreeperExplosions;
    public boolean blockCreeperBlockDamage;
    public int loginProtection;
    public int spawnProtection;
    public boolean kickOnDeath;
    public boolean exactRespawn;
    public boolean teleportToHome;
    public boolean disableContactDamage;
    public boolean disableFallDamage;
    public boolean disableLavaDamage;
    public boolean disableFireDamage;
    public boolean disableDrowningDamage;
    public boolean disableSuffocationDamage;
    public boolean teleportOnSuffocation;
    public boolean useRegions;
    public int regionWand = 287;
    public String blockCreatureSpawn;
    /* Configuration data end */


    public WorldGuardWorldConfiguration(WorldGuardPlugin wp, String worldName, File configFile, File blacklistFile)
    {
        this.wp = wp;
        this.worldName = worldName;
        this.configFile = configFile;
        this.blacklistFile = blacklistFile;

        createDefaultConfiguration(configFile, "config.yml");
        createDefaultConfiguration(blacklistFile, "blacklist.txt");
        
        loadConfiguration();
    }


    /**
     * Create a default configuration file from the .jar.
     *
     * @param name
     */
    public static void createDefaultConfiguration(File actual, String defaultName) {

        if (!actual.exists()) {

            InputStream input =
                    WorldGuardPlugin.class.getResourceAsStream("/defaults/" + defaultName);
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    logger.info("WorldGuard: Default configuration file written: " + defaultName);
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

   /**
     * Load the configuration.
     */
    private void loadConfiguration() {
        Configuration config = new Configuration(this.configFile);
        config.load();
 
        enforceOneSession = config.getBoolean("protection.enforce-single-session", true);
        itemDurability = config.getBoolean("protection.item-durability", true);

        classicWater = config.getBoolean("simulation.classic-water", false);
        simulateSponge = config.getBoolean("simulation.sponge.enable", true);
        spongeRadius = Math.max(1, config.getInt("simulation.sponge.radius", 3)) - 1;
        redstoneSponges = config.getBoolean("simulation.sponge.redstone", false);

        noPhysicsGravel = config.getBoolean("physics.no-physics-gravel", false);
        noPhysicsSand = config.getBoolean("physics.no-physics-sand", false);
        allowPortalAnywhere = config.getBoolean("physics.allow-portal-anywhere", false);
        preventWaterDamage = new HashSet<Integer>(config.getIntList("physics.disable-water-damage-blocks", null));

        blockTNT = config.getBoolean("ignition.block-tnt", false);
        blockLighter = config.getBoolean("ignition.block-lighter", false);

        preventLavaFire = config.getBoolean("fire.disable-lava-fire-spread", true);
        disableFireSpread = config.getBoolean("fire.disable-all-fire-spread", false);
        disableFireSpreadBlocks = new HashSet<Integer>(config.getIntList("fire.disable-fire-spread-blocks", null));
        allowedLavaSpreadOver = new HashSet<Integer>(config.getIntList("fire.lava-spread-blocks", null));

        blockCreeperExplosions = config.getBoolean("mobs.block-creeper-explosions", false);
        blockCreeperBlockDamage = config.getBoolean("mobs.block-creeper-block-damage", false);

        loginProtection = config.getInt("spawn.login-protection", 3);
        spawnProtection = config.getInt("spawn.spawn-protection", 0);
        kickOnDeath = config.getBoolean("spawn.kick-on-death", false);
        exactRespawn = config.getBoolean("spawn.exact-respawn", false);
        teleportToHome = config.getBoolean("spawn.teleport-to-home-on-death", false);

        disableFallDamage = config.getBoolean("player-damage.disable-fall-damage", false);
        disableLavaDamage = config.getBoolean("player-damage.disable-lava-damage", false);
        disableFireDamage = config.getBoolean("player-damage.disable-fire-damage", false);
        disableDrowningDamage = config.getBoolean("player-damage.disable-drowning-damage", false);
        disableSuffocationDamage = config.getBoolean("player-damage.disable-suffocation-damage", false);
        disableContactDamage = config.getBoolean("player-damage.disable-contact-damage", false);
        teleportOnSuffocation = config.getBoolean("player-damage.teleport-on-suffocation", false);

        useRegions = config.getBoolean("regions.enable", true);
        regionWand = config.getInt("regions.wand", 287);

        for (String creature : config.getStringList("mobs.block-creature-spawn", null)) {
            blockCreatureSpawn += creature.toLowerCase() + " ";
        }

        GlobalFlags globalFlags = new GlobalFlags();
        globalFlags.canBuild = config.getBoolean("regions.default.build", true);
        globalFlags.canAccessChests = config.getBoolean("regions.default.chest-access", false);
        globalFlags.canPvP = config.getBoolean("regions.default.pvp", true);
        globalFlags.canLighter = config.getBoolean("regions.default.lighter", true);
        globalFlags.canTnt = config.getBoolean("regions.default.tnt", true);
        globalFlags.allowCreeper = config.getBoolean("regions.default.creeper", true);
        globalFlags.allowMobDamage = config.getBoolean("regions.default.mobdamage", true);
        wp.getGlobalRegionManager().setGlobalFlags(worldName, globalFlags);


        // Console log configuration
        boolean logConsole = config.getBoolean("blacklist.logging.console.enable", true);

        // Database log configuration
        boolean logDatabase = config.getBoolean("blacklist.logging.database.enable", false);
        String dsn = config.getString("blacklist.logging.database.dsn", "jdbc:mysql://localhost:3306/minecraft");
        String user = config.getString("blacklist.logging.database.user", "root");
        String pass = config.getString("blacklist.logging.database.pass", "");
        String table = config.getString("blacklist.logging.database.table", "blacklist_events");

        // File log configuration
        boolean logFile = config.getBoolean("blacklist.logging.file.enable", false);
        String logFilePattern = config.getString("blacklist.logging.file.path", "worldguard/logs/%Y-%m-%d.log");
        int logFileCacheSize = Math.max(1, config.getInt("blacklist.logging.file.open-files", 10));

        // Load the blacklist
        try {
            // If there was an existing blacklist, close loggers
            if (blacklist != null) {
                blacklist.getLogger().close();
            }

            // First load the blacklist data from worldguard-blacklist.txt
            Blacklist blist = new BukkitBlacklist(wp);
            blist.load(blacklistFile);

            // If the blacklist is empty, then set the field to null
            // and save some resources
            if (blist.isEmpty()) {
                this.blacklist = null;
            } else {
                this.blacklist = blist;
                logger.log(Level.INFO, "WorldGuard: Blacklist loaded.");

                BlacklistLogger blacklistLogger = blist.getLogger();

                if (logDatabase) {
                    blacklistLogger.addHandler(new DatabaseLoggerHandler(dsn, user, pass, table, worldName));
                }

                if (logConsole) {
                    blacklistLogger.addHandler(new ConsoleLoggerHandler(worldName));
                }

                if (logFile) {
                    FileLoggerHandler handler =
                            new FileLoggerHandler(logFilePattern, logFileCacheSize, worldName);
                    blacklistLogger.addHandler(handler);
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "WorldGuard blacklist does not exist.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load WorldGuard blacklist: "
                    + e.getMessage());
        }

        // Print an overview of settings
        if (config.getBoolean("summary-on-start", true)) {
            logger.log(Level.INFO, "=============== WorldGuard configuration for world " + worldName + " ===============");
            logger.log(Level.INFO, enforceOneSession ? "WorldGuard: Single session is enforced."
                    : "WorldGuard: Single session is NOT ENFORCED.");
            logger.log(Level.INFO, blockTNT ? "WorldGuard: TNT ignition is blocked."
                    : "WorldGuard: TNT ignition is PERMITTED.");
            logger.log(Level.INFO, blockLighter ? "WorldGuard: Lighters are blocked."
                    : "WorldGuard: Lighters are PERMITTED.");
            logger.log(Level.INFO, preventLavaFire ? "WorldGuard: Lava fire is blocked."
                    : "WorldGuard: Lava fire is PERMITTED.");
            if (disableFireSpread) {
                logger.log(Level.INFO, "WorldGuard: All fire spread is disabled.");
            } else {
                if (disableFireSpreadBlocks.size() > 0) {
                    logger.log(Level.INFO, "WorldGuard: Fire spread is limited to "
                            + disableFireSpreadBlocks.size() + " block types.");
                } else {
                    logger.log(Level.INFO, "WorldGuard: Fire spread is UNRESTRICTED.");
                }
            }
        }
    }
    
    public Blacklist getBlacklist()
    {
        return this.blacklist;
    }

    public String getWorldName()
    {
        return this.worldName;
    }

}
