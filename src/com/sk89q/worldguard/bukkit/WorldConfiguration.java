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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/**
 * Holds the configuration for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public class WorldConfiguration {

    private static final Logger logger = Logger
            .getLogger("Minecraft.WorldGuard");

    private WorldGuardPlugin plugin;

    private String worldName;
    private File configFile;
    private File blacklistFile;

    private Blacklist blacklist;
    private SignChestProtection chestProtection = new SignChestProtection();

    /* Configuration data start */
    public boolean fireSpreadDisableToggle;
    public boolean enforceOneSession;
    public boolean itemDurability;
    public boolean classicWater;
    public boolean simulateSponge;
    public int spongeRadius;
    public boolean pumpkinScuba;
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
    public boolean disableVoidDamage;
    public boolean teleportOnVoid;
    public boolean useRegions;
    public boolean highFreqFlags;
    public int regionWand = 287;
    public Set<CreatureType> blockCreatureSpawn;
    public boolean useiConomy;
    public boolean buyOnClaim;
    public double buyOnClaimPrice;
    public int maxClaimVolume;
    public boolean claimOnlyInsideExistingRegions;
    public int maxRegionCountPerPlayer;
    public boolean antiWolfDumbness;
    public boolean signChestProtection;
    public boolean removeInfiniteStacks;

    /* Configuration data end */

    /**
     * Construct the object.
     *
     * @param plugin
     * @param worldName
     */
    public WorldConfiguration(WorldGuardPlugin plugin, String worldName) {
        File baseFolder = new File(plugin.getDataFolder(), "worlds/" + worldName);
        configFile = new File(baseFolder, "config.yml");
        blacklistFile = new File(baseFolder, "blacklist.txt");

        this.plugin = plugin;
        this.worldName = worldName;

        WorldGuardPlugin.createDefaultConfiguration(configFile, "config_world.yml");
        WorldGuardPlugin.createDefaultConfiguration(blacklistFile, "blacklist.txt");

        loadConfiguration();

        logger.info("WorldGuard: Loaded configuration for world '" + worldName + '"');
    }

    /**
     * Load the configuration.
     */
    private void loadConfiguration() {
        Configuration config = new Configuration(this.configFile);
        config.load();

        enforceOneSession = config.getBoolean("protection.enforce-single-session", true);
        itemDurability = config.getBoolean("protection.item-durability", true);
        removeInfiniteStacks = config.getBoolean("protection.remove-infinite-stacks", false);

        classicWater = config.getBoolean("simulation.classic-water", false);
        simulateSponge = config.getBoolean("simulation.sponge.enable", true);
        spongeRadius = Math.max(1, config.getInt("simulation.sponge.radius", 3)) - 1;
        redstoneSponges = config.getBoolean("simulation.sponge.redstone", false);

        pumpkinScuba = config.getBoolean("pumpkin-scuba", false);

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
        antiWolfDumbness = config.getBoolean("mobs.anti-wolf-dumbness", false);

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
        disableVoidDamage = config.getBoolean("player-damage.disable-void-damage", false);
        teleportOnVoid = config.getBoolean("player-damage.teleport-on-void-falling", false);

        signChestProtection = config.getBoolean("chest-protection.enable", false);

        useRegions = config.getBoolean("regions.enable", true);
        highFreqFlags = config.getBoolean("regions.high-frequency-flags", false);
        regionWand = config.getInt("regions.wand", 287);
        maxClaimVolume = config.getInt("regions.max-claim-volume", 30000);
        claimOnlyInsideExistingRegions = config.getBoolean("regions.claim-only-inside-existing-regions", false);
        maxRegionCountPerPlayer = config.getInt("regions.max-region-count-per-player", 7);

        useiConomy = config.getBoolean("iconomy.enable", false);
        buyOnClaim = config.getBoolean("iconomy.buy-on-claim", false);
        buyOnClaimPrice = config.getDouble("iconomy.buy-on-claim-price", 1.0);

        blockCreatureSpawn = new HashSet<CreatureType>();
        for (String creatureName : config.getStringList("mobs.block-creature-spawn", null)) {
            CreatureType creature = CreatureType.fromName(creatureName);

            if (creature == null) {
                logger.warning("WorldGuard: Unknown mob type '" + creatureName + "'");
            } else {
                blockCreatureSpawn.add(creature);
            }
        }

        boolean useBlacklistAsWhitelist = config.getBoolean("blacklist.use-as-whitelist", false);

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
            Blacklist blist = new BukkitBlacklist(useBlacklistAsWhitelist, plugin);
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
            logger.log(Level.INFO, enforceOneSession
                    ? "WorldGuard: (" + worldName + ") Single session is enforced."
                    : "WorldGuard: (" + worldName + ") Single session is NOT ENFORCED.");
            logger.log(Level.INFO, blockTNT
                    ? "WorldGuard: (" + worldName + ") TNT ignition is blocked."
                    : "WorldGuard: (" + worldName + ") TNT ignition is PERMITTED.");
            logger.log(Level.INFO, blockLighter
                    ? "WorldGuard: (" + worldName + ") Lighters are blocked."
                    : "WorldGuard: (" + worldName + ") Lighters are PERMITTED.");
            logger.log(Level.INFO, preventLavaFire
                    ? "WorldGuard: (" + worldName + ") Lava fire is blocked."
                    : "WorldGuard: (" + worldName + ") Lava fire is PERMITTED.");

            if (disableFireSpread) {
                logger.log(Level.INFO, "WorldGuard: (" + worldName + ") All fire spread is disabled.");
            } else {
                if (disableFireSpreadBlocks.size() > 0) {
                    logger.log(Level.INFO, "WorldGuard: (" + worldName
                            + ") Fire spread is limited to "
                            + disableFireSpreadBlocks.size() + " block types.");
                } else {
                    logger.log(Level.INFO, "WorldGuard: (" + worldName
                            + ") Fire spread is UNRESTRICTED.");
                }
            }
        }
    }

    public Blacklist getBlacklist() {
        return this.blacklist;
    }

    public String getWorldName() {
        return this.worldName;
    }
    
    public boolean isChestProtected(Block block, Player player) {
        if (!signChestProtection) {
            return false;
        }
        if (plugin.hasPermission(player, "worldguard.chest-protection.override")) {
            return false;
        }
        return chestProtection.isProtected(block, player);
    }
    
    public boolean isChestProtected(Block block) {
        if (!signChestProtection) {
            return false;
        }
        return chestProtection.isProtected(block, null);
    }

}
