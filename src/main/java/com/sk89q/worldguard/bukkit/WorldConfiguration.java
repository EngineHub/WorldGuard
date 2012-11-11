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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.sk89q.rebar.config.ConfigurationException;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.YamlConfiguration;
import com.sk89q.rebar.config.YamlConfigurationFile;
import com.sk89q.rebar.config.YamlConfigurationResource;
import com.sk89q.rebar.config.YamlStyle;
import com.sk89q.rebar.util.LoggerUtils;
import com.sk89q.rulelists.RuleList;
import com.sk89q.rulelists.RuleEntry;
import com.sk89q.rulelists.RuleEntryLoader;
import com.sk89q.rulelists.RuleTemplateEntryLoader;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.blacklist.BlacklistLogger;
import com.sk89q.worldguard.blacklist.loggers.ConsoleLoggerHandler;
import com.sk89q.worldguard.blacklist.loggers.DatabaseLoggerHandler;
import com.sk89q.worldguard.blacklist.loggers.FileLoggerHandler;

/**
 * Holds the configuration for individual worlds.
 */
public class WorldConfiguration {

    private static Logger logger = LoggerUtils.getLogger(WorldConfiguration.class, "[WorldGuard] ");

    private final WorldGuardPlugin plugin;
    private final ConfigurationManager configMan;
    private final YamlConfigurationFile config;
    private final String worldName;

    private Blacklist blacklist;
    private RuleList ruleList = new RuleList();
    private SpongeApplicator spongeApplicator = null;

    /* Configuration data start */
    public boolean opPermissions;
    public boolean useRegions;
    public boolean highFreqFlags;
    public int regionWand;
    public boolean regionInvinciblityRemovesMobs;
    // public boolean useiConomy;
    // public boolean buyOnClaim;
    // public double buyOnClaimPrice;
    public int maxClaimVolume;
    public boolean claimOnlyInsideExistingRegions;
    public int maxRegionCountPerPlayer;
    public boolean signChestProtection;
    public boolean disableSignChestProtectionCheck;
    private Map<String, Integer> maxRegionCounts;
    public boolean fireSpreadDisableToggle;
    /* Configuration data end */

    /**
     * Construct the object.
     *
     * @param plugin the plugin instance
     * @param worldName name of the world that this object is for
     * @param configMan configuration manager
     * @param parentConfig parent configuration
     */
    public WorldConfiguration(WorldGuardPlugin plugin, String worldName,
            ConfigurationManager configMan, ConfigurationNode parentConfig) {
        this.plugin = plugin;
        this.worldName = worldName;
        this.configMan = configMan;

        File file = configMan.getWorldConfigFile(worldName);
        config = new YamlConfigurationFile(file, ConfigurationManager.YAML_STYLE);
        config.setParent(parentConfig);

        load();
    }

    /**
     * Load the configuration.
     */
    private void load() {
        logger.info("Loading configuration for '" + worldName + "'...");

        loadConfig();

        // Load rule lists
        ruleList.clear();
        loadBuiltInRules();
        loadUserRules();
        loadBlacklist();
        loadSettings();

        saveConfig();
    }

    /**
     * Load the configuration from file.
     */
    private void loadConfig() {
        try {
            config.load();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading configuration for world " + worldName, e);
        } catch (ConfigurationException e) {
            logger.log(Level.SEVERE, "Error reading configuration for world " + worldName, e);
        }
    }

    /**
     * Save the configuration to file.
     */
    private void saveConfig() {
        config.setHeader(
                "# CONFIGURATION FILE FOR ONLY THIS WORLD\r\n" +
                "#\r\n" +
                "# Everything in here applies to ONLY this world. For global settings, edit \r\n" +
                "# the file in plugins/WorldGuard/config.yml\r\n" +
                "#\r\n" +
                "# How do you use this file? Copy potions of the global configuration file into this file.\r\n" +
                "#\r\n" +
                "# EXAMPLE (remove the # in front):\r\n" +
                "#physics:\r\n" +
                "#    block-tnt-damage: false");

        try {
            config.save();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save configuration", e);
        }
    }

    /**
     * Load the built-in rules.
     */
    private void loadBuiltInRules() {
        try {
            RuleTemplateEntryLoader loader = new RuleTemplateEntryLoader(
                    plugin.getRulesListManager(), config);

            for (List<RuleEntry> entries : plugin.getBuiltInRules().listOf("", loader)) {
                for (RuleEntry entry : entries) {
                    ruleList.learn(entry);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load the built-in rules", e);
        } catch (ConfigurationException e) {
            logger.log(Level.SEVERE, "Failed to load the built-in rules", e);
        }
    }

    /**
     * Load the user rules.
     */
    private void loadUserRules() {
        File rulesFile = configMan.getRuleListFile(worldName);
        RuleEntryLoader entryLoader = new RuleEntryLoader(plugin.getRulesListManager());
        YamlConfigurationFile rulesConfig = new YamlConfigurationFile(rulesFile,
                ConfigurationManager.YAML_STYLE, false);

        try {
            rulesConfig.load();

            for (RuleEntry entry : rulesConfig.listOf(ConfigurationNode.ROOT, entryLoader)) {
                ruleList.learn(entry);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load " + rulesFile.getAbsolutePath(), e);
        } catch (ConfigurationException e) {
            logger.log(Level.SEVERE, "Failed to load " + rulesFile.getAbsolutePath(), e);
        }
    }

    /**
     * Load the blacklist.
     */
    private void loadBlacklist() {
        try {
            if (blacklist != null) {
                blacklist.getLogger().close();
            }

            File blacklistFile = configMan.getBlacklistFile(worldName);

            // Load blacklist settings
            boolean useBlacklistAsWhitelist = config.getBoolean("blacklist.use-as-whitelist", false);
            boolean logConsole = config.getBoolean("blacklist.logging.console.enable", true);
            boolean logDatabase = config.getBoolean("blacklist.logging.database.enable", false);
            String dsn = config.getString("blacklist.logging.database.dsn", "jdbc:mysql://localhost:3306/minecraft");
            String user = config.getString("blacklist.logging.database.user", "root");
            String pass = config.getString("blacklist.logging.database.pass", "");
            String table = config.getString("blacklist.logging.database.table", "blacklist_events");
            boolean logFile = config.getBoolean("blacklist.logging.file.enable", false);
            String logFilePattern = config.getString("blacklist.logging.file.path", "worldguard/logs/%Y-%m-%d.log");
            int logFileCacheSize = Math.max(1, config.getInt("blacklist.logging.file.open-files", 10));

            Blacklist newBlacklist = new BukkitBlacklist(useBlacklistAsWhitelist, plugin);
            newBlacklist.load(blacklistFile);

            // If the blacklist is empty, then set the field to null and save some resources
            if (newBlacklist.isEmpty()) {
                this.blacklist = null;
            } else {
                this.blacklist = newBlacklist;
                logger.log(Level.INFO, "Blacklist loaded.");

                BlacklistLogger blacklistLogger = newBlacklist.getLogger();

                if (logDatabase)
                    blacklistLogger.addHandler(new DatabaseLoggerHandler(dsn, user,
                            pass, table, worldName, logger));

                if (logConsole)
                    blacklistLogger.addHandler(new ConsoleLoggerHandler(worldName,
                            logger));

                if (logFile) {
                    FileLoggerHandler handler = new FileLoggerHandler(logFilePattern,
                            logFileCacheSize, worldName, logger);
                    blacklistLogger.addHandler(handler);
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "WorldGuard blacklist does not exist", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load blacklist", e);
        }
    }

    /**
     * Load the settings.
     */
    private void loadSettings() {
        opPermissions = config.getBoolean("op-permissions", true);

        boolean simulateSponge = config.getBoolean("simulation.sponge.enable", true);
        int spongeRadius = Math.max(1, config.getInt("simulation.sponge.radius", 3)) - 1;
        boolean redstoneSponges = config.getBoolean("simulation.sponge.redstone", false);
        boolean refillArea = config.getBoolean("simulation.sponge.refill-area", false);
        if (simulateSponge) {
            spongeApplicator = new SpongeApplicator(spongeRadius, redstoneSponges, refillArea);
        }

        signChestProtection = config.getBoolean("chest-protection.enable", false);
        disableSignChestProtectionCheck = config.getBoolean("chest-protection.disable-off-check", false);

        useRegions = config.getBoolean("regions.enable", true);
        regionInvinciblityRemovesMobs = config.getBoolean("regions.invincibility-removes-mobs", false);
        highFreqFlags = config.getBoolean("regions.high-frequency-flags", false);
        regionWand = config.getInt("regions.wand", 287);
        maxClaimVolume = config.getInt("regions.max-claim-volume", 30000);
        claimOnlyInsideExistingRegions = config.getBoolean("regions.claim-only-inside-existing-regions", false);

        maxRegionCountPerPlayer = config.getInt("regions.max-region-count-per-player.default", 7);
        maxRegionCounts = new HashMap<String, Integer>();
        maxRegionCounts.put(null, maxRegionCountPerPlayer);

        for (String key : config.getKeys("regions.max-region-count-per-player")) {
            if (!key.equalsIgnoreCase("default")) {
                Object val = config.get("regions.max-region-count-per-player." + key);
                if (val != null && val instanceof Number) {
                    maxRegionCounts.put(key, ((Number) val).intValue());
                }
            }
        }

        // Very important part for protection!
        if (useRegions) {
            loadRegionModels();
        }

        // useiConomy = getBoolean("iconomy.enable", false);
        // buyOnClaim = getBoolean("iconomy.buy-on-claim", false);
        // buyOnClaimPrice = getDouble("iconomy.buy-on-claim-price", 1.0);
    }

    /**
     * Load region protection model.
     */
    private void loadRegionModels() {
        List<String> filenames;
        RuleEntryLoader entryLoader = new RuleEntryLoader(plugin.getRulesListManager());

        // Make a default protection model list
        List<String> defaultProtectionModels = new ArrayList<String>();
        defaultProtectionModels.add("minimal");

        // Load protection model
        filenames = config.getStringList("regions.protection-models", defaultProtectionModels);
        for (YamlConfiguration config : readFiles(filenames, "/models/protection/",
                new File(plugin.getDataFolder(), "models/protection"))) {
            for (RuleEntry entry : config.listOf(ConfigurationNode.ROOT, entryLoader)) {
                ruleList.learn(entry);
            }
        }
    }

    /**
     * Get the blacklist.
     *
     * @return the blacklist
     */
    Blacklist getBlacklist() {
        return blacklist;
    }

    /**
     * Get the RuleList.
     *
     * @return the rule list
     */
    RuleList getRuleList() {
        return ruleList;
    }

    /**
     * Get the sponge applicator.
     *
     * @return sponge applicator
     */
    SpongeApplicator getSpongeApplicator() {
        return spongeApplicator;
    }

    /**
     * Get the world name of this configuration.
     *
     * @return the world name
     */
    public String getWorldName() {
        return this.worldName;
    }

    /**
     * Get the maximum region count for a given player.
     *
     * @param player player
     * @return number of regions
     */
    public int getMaxRegionCount(Player player) {
        int max = -1;
        for (String group : plugin.getGroups(player)) {
            if (maxRegionCounts.containsKey(group)) {
                int groupMax = maxRegionCounts.get(group);
                if (max < groupMax) max = groupMax;
            }
        }
        if (max <= -1) {
            max = maxRegionCountPerPlayer;
        }
        return max;
    }

    /**
     * Read a list of files, either from the .jar (preferred) or from disk.
     *
     * @param filenames list of filenames
     * @param internalDir the base path for the JAR files
     * @param externalDir the base dir for the disk files
     * @return a list of {@link YamlConfiguration}s
     */
    private static List<YamlConfiguration> readFiles(Collection<String> filenames,
            String internalDir, File externalDir) {
        List<YamlConfiguration> configs = new ArrayList<YamlConfiguration>();

        for (String filename : filenames) {
            YamlConfiguration config;

            // External file path
            File file = new File(externalDir, filename);
            file.getParentFile().mkdirs();

            // Try loading from the .jar first
            String path = internalDir + filename + ".yml";
            config = new YamlConfigurationResource(WorldGuardPlugin.class, path);
            try {
                config.load();
                configs.add(config);
                continue; // Found!
            } catch (FileNotFoundException e) {
                // Do nothing
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to internal path " + path, e);
            } catch (ConfigurationException e) {
                logger.log(Level.SEVERE, "Failed to internal path " + path, e);
            }

            // Try loading from disk
            config = new YamlConfigurationFile(file, new YamlStyle(), false);
            try {
                config.load();
                configs.add(config);
                continue; // Found!
            } catch (FileNotFoundException e) {
                logger.log(Level.SEVERE, "Failed to load " + file.getAbsolutePath());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load " + file.getAbsolutePath(), e);
            } catch (ConfigurationException e) {
                logger.log(Level.SEVERE, "Failed to load " + file.getAbsolutePath(), e);
            }
        }

        return configs;
    }
}
