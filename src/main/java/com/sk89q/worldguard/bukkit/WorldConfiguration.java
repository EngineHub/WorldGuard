/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit;

import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldguard.blacklist.Blacklist;
import com.sk89q.worldguard.blacklist.BlacklistLoggerHandler;
import com.sk89q.worldguard.blacklist.logger.ConsoleHandler;
import com.sk89q.worldguard.blacklist.logger.DatabaseHandler;
import com.sk89q.worldguard.blacklist.logger.FileHandler;
import com.sk89q.worldguard.blacklist.target.TargetMatcherParseException;
import com.sk89q.worldguard.blacklist.target.TargetMatcherParser;
import com.sk89q.worldguard.bukkit.commands.CommandUtils;
import com.sk89q.worldguard.bukkit.internal.BukkitBlacklist;
import com.sk89q.worldguard.bukkit.internal.TargetMatcherSet;
import com.sk89q.worldguard.chest.ChestProtection;
import com.sk89q.worldguard.chest.SignChestProtection;
import com.sk89q.worldguard.util.report.Unreported;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the configuration for individual worlds.
 *
 * @author sk89q
 * @author Michael
 */
public class WorldConfiguration {

    private static final Logger log = Logger.getLogger(WorldConfiguration.class.getCanonicalName());
    private static final TargetMatcherParser matcherParser = new TargetMatcherParser();

    public static final String CONFIG_HEADER = "#\r\n" +
            "# WorldGuard's world configuration file\r\n" +
            "#\r\n" +
            "# This is a world configuration file. Anything placed into here will only\r\n" +
            "# affect this world. If you don't put anything in this file, then the\r\n" +
            "# settings will be inherited from the main configuration file.\r\n" +
            "#\r\n" +
            "# If you see {} below, that means that there are NO entries in this file.\r\n" +
            "# Remove the {} and add your own entries.\r\n" +
            "#\r\n";

    @Unreported private WorldGuardPlugin plugin;

    @Unreported private String worldName;
    @Unreported private YAMLProcessor parentConfig;
    @Unreported private YAMLProcessor config;
    private File blacklistFile;

    @Unreported private Blacklist blacklist;
    @Unreported private ChestProtection chestProtection = new SignChestProtection();

    /* Configuration data start */
    public boolean summaryOnStart;
    public boolean opPermissions;
    public boolean buildPermissions;
    public String buildPermissionDenyMessage = "";
    public boolean fireSpreadDisableToggle;
    public boolean itemDurability;
    public boolean simulateSponge;
    public int spongeRadius;
    public boolean disableExpDrops;
    public Set<PotionEffectType> blockPotions;
    public boolean blockPotionsAlways;
    public boolean pumpkinScuba;
    public boolean redstoneSponges;
    public boolean noPhysicsGravel;
    public boolean noPhysicsSand;
    public boolean ropeLadders;
    public boolean allowPortalAnywhere;
    public Set<Integer> preventWaterDamage;
    public boolean blockLighter;
    public boolean disableFireSpread;
    public Set<Integer> disableFireSpreadBlocks;
    public boolean preventLavaFire;
    public Set<Integer> allowedLavaSpreadOver;
    public boolean blockTNTExplosions;
    public boolean blockTNTBlockDamage;
    public boolean blockCreeperExplosions;
    public boolean blockCreeperBlockDamage;
    public boolean blockWitherExplosions;
    public boolean blockWitherBlockDamage;
    public boolean blockWitherSkullExplosions;
    public boolean blockWitherSkullBlockDamage;
    public boolean blockEnderDragonBlockDamage;
    public boolean blockEnderDragonPortalCreation;
    public boolean blockFireballExplosions;
    public boolean blockFireballBlockDamage;
    public boolean blockOtherExplosions;
    public boolean blockEntityPaintingDestroy;
    public boolean blockEntityItemFrameDestroy;
    public boolean blockPluginSpawning;
    public boolean blockGroundSlimes;
    public boolean blockZombieDoorDestruction;
    public boolean disableContactDamage;
    public boolean disableFallDamage;
    public boolean disableLavaDamage;
    public boolean disableFireDamage;
    public boolean disableLightningDamage;
    public boolean disableDrowningDamage;
    public boolean disableSuffocationDamage;
    public boolean teleportOnSuffocation;
    public boolean disableVoidDamage;
    public boolean teleportOnVoid;
    public boolean disableExplosionDamage;
    public boolean disableMobDamage;
    public boolean useRegions;
    public boolean highFreqFlags;
    public boolean checkLiquidFlow;
    public int regionWand;
    public Set<EntityType> blockCreatureSpawn;
    public boolean allowTamedSpawns;
    // public boolean useiConomy;
    // public boolean buyOnClaim;
    // public double buyOnClaimPrice;
    public int maxClaimVolume;
    public boolean claimOnlyInsideExistingRegions;
    public int maxRegionCountPerPlayer;
    public boolean antiWolfDumbness;
    public boolean signChestProtection;
    public boolean disableSignChestProtectionCheck;
    public boolean removeInfiniteStacks;
    public boolean disableCreatureCropTrampling;
    public boolean disablePlayerCropTrampling;
    public boolean preventLightningFire;
    public Set<Integer> disallowedLightningBlocks;
    public boolean disableThunder;
    public boolean disableWeather;
    public boolean alwaysRaining;
    public boolean alwaysThundering;
    public boolean disablePigZap;
    public boolean disableCreeperPower;
    public boolean disableHealthRegain;
    public boolean disableMushroomSpread;
    public boolean disableIceMelting;
    public boolean disableSnowMelting;
    public boolean disableSnowFormation;
    public boolean disableIceFormation;
    public boolean disableLeafDecay;
    public boolean disableGrassGrowth;
    public boolean disableMyceliumSpread;
    public boolean disableVineGrowth;
    public boolean disableEndermanGriefing;
    public boolean disableSnowmanTrails;
    public boolean disableSoilDehydration;
    public Set<Integer> allowedSnowFallOver;
    public boolean regionInvinciblityRemovesMobs;
    public boolean fakePlayerBuildOverride;
    public boolean explosionFlagCancellation;
    public boolean disableDeathMessages;
    public boolean disableObsidianGenerators;
    public boolean strictEntitySpawn;
    public TargetMatcherSet allowAllInteract;
    public TargetMatcherSet blockUseAtFeet;

    private Map<String, Integer> maxRegionCounts;

    /* Configuration data end */

    /**
     * Construct the object.
     *
     * @param plugin The WorldGuardPlugin instance
     * @param worldName The world name that this WorldConfiguration is for.
     * @param parentConfig The parent configuration to read defaults from
     */
    public WorldConfiguration(WorldGuardPlugin plugin, String worldName, YAMLProcessor parentConfig) {
        File baseFolder = new File(plugin.getDataFolder(), "worlds/" + worldName);
        File configFile = new File(baseFolder, "config.yml");
        blacklistFile = new File(baseFolder, "blacklist.txt");

        this.plugin = plugin;
        this.worldName = worldName;
        this.parentConfig = parentConfig;

        plugin.createDefaultConfiguration(configFile, "config_world.yml");
        plugin.createDefaultConfiguration(blacklistFile, "blacklist.txt");

        config = new YAMLProcessor(configFile, true, YAMLFormat.EXTENDED);
        loadConfiguration();

        if (summaryOnStart) {
            log.info("Loaded configuration for world '" + worldName + "'");
        }
    }

    private boolean getBoolean(String node, boolean def) {
        boolean val = parentConfig.getBoolean(node, def);

        if (config.getProperty(node) != null) {
            return config.getBoolean(node, def);
        } else {
            return val;
        }
    }

    private String getString(String node, String def) {
        String val = parentConfig.getString(node, def);

        if (config.getProperty(node) != null) {
            return config.getString(node, def);
        } else {
            return val;
        }
    }

    private int getInt(String node, int def) {
        int val = parentConfig.getInt(node, def);

        if (config.getProperty(node) != null) {
            return config.getInt(node, def);
        } else {
            return val;
        }
    }

    @SuppressWarnings("unused")
    private double getDouble(String node, double def) {
        double val = parentConfig.getDouble(node, def);

        if (config.getProperty(node) != null) {
            return config.getDouble(node, def);
        } else {
            return val;
        }
    }

    private List<Integer> getIntList(String node, List<Integer> def) {
        List<Integer> res = parentConfig.getIntList(node, def);

        if (res == null || res.size() == 0) {
            parentConfig.setProperty(node, new ArrayList<Integer>());
        }

        if (config.getProperty(node) != null) {
            res = config.getIntList(node, def);
        }

        return res;
    }

    private TargetMatcherSet getTargetMatchers(String node) {
        TargetMatcherSet set = new TargetMatcherSet();
        List<String> inputs = parentConfig.getStringList(node, null);

        if (inputs == null || inputs.size() == 0) {
            parentConfig.setProperty(node, new ArrayList<String>());
            return set;
        }

        for (String input : inputs) {
            try {
                set.add(matcherParser.fromInput(input));
            } catch (TargetMatcherParseException e) {
                log.warning("Failed to parse the block / item type specified as '" + input + "'");
            }
        }

        return set;
    }

    private List<String> getStringList(String node, List<String> def) {
        List<String> res = parentConfig.getStringList(node, def);

        if (res == null || res.size() == 0) {
            parentConfig.setProperty(node, new ArrayList<String>());
        }

        if (config.getProperty(node) != null) {
            res = config.getStringList(node, def);
        }

        return res;
    }

    private List<String> getKeys(String node) {
        List<String> res = parentConfig.getKeys(node);

        if (res == null || res.size() == 0) {
            res = config.getKeys(node);
        }
        if (res == null) {
            res = new ArrayList<String>();
        }

        return res;
    }

    private Object getProperty(String node) {
        Object res = parentConfig.getProperty(node);

        if (config.getProperty(node) != null) {
            res = config.getProperty(node);
        }

        return res;
    }

    /**
     * Load the configuration.
     */
    private void loadConfiguration() {
        try {
            config.load();
        } catch (IOException e) {
            log.severe("Error reading configuration for world " + worldName + ": ");
            e.printStackTrace();
        } catch (ParserException e) {
            log.severe("Error parsing configuration for world " + worldName + ". ");
            throw e;
        }

        summaryOnStart = getBoolean("summary-on-start", true);
        opPermissions = getBoolean("op-permissions", true);

        buildPermissions = getBoolean("build-permission-nodes.enable", false);
        buildPermissionDenyMessage = CommandUtils.replaceColorMacros(
                getString("build-permission-nodes.deny-message", "&eSorry, but you are not permitted to do that here."));

        strictEntitySpawn = getBoolean("event-handling.block-entity-spawns-with-untraceable-cause", false);
        allowAllInteract = getTargetMatchers("event-handling.interaction-whitelist");
        blockUseAtFeet = getTargetMatchers("event-handling.emit-block-use-at-feet");

        itemDurability = getBoolean("protection.item-durability", true);
        removeInfiniteStacks = getBoolean("protection.remove-infinite-stacks", false);
        disableExpDrops = getBoolean("protection.disable-xp-orb-drops", false);
        disableObsidianGenerators = getBoolean("protection.disable-obsidian-generators", false);

        blockPotions = new HashSet<PotionEffectType>();
        for (String potionName : getStringList("gameplay.block-potions", null)) {
            PotionEffectType effect = PotionEffectType.getByName(potionName);

            if (effect == null) {
                log.warning("Unknown potion effect type '" + potionName + "'");
            } else {
                blockPotions.add(effect);
            }
        }
        blockPotionsAlways = getBoolean("gameplay.block-potions-overly-reliably", false);

        simulateSponge = getBoolean("simulation.sponge.enable", false);
        spongeRadius = Math.max(1, getInt("simulation.sponge.radius", 3)) - 1;
        redstoneSponges = getBoolean("simulation.sponge.redstone", false);

        pumpkinScuba = getBoolean("default.pumpkin-scuba", false);
        disableHealthRegain = getBoolean("default.disable-health-regain", false);

        noPhysicsGravel = getBoolean("physics.no-physics-gravel", false);
        noPhysicsSand = getBoolean("physics.no-physics-sand", false);
        ropeLadders = getBoolean("physics.vine-like-rope-ladders", false);
        allowPortalAnywhere = getBoolean("physics.allow-portal-anywhere", false);
        preventWaterDamage = new HashSet<Integer>(getIntList("physics.disable-water-damage-blocks", null));

        blockTNTExplosions = getBoolean("ignition.block-tnt", false);
        blockTNTBlockDamage = getBoolean("ignition.block-tnt-block-damage", false);
        blockLighter = getBoolean("ignition.block-lighter", false);

        preventLavaFire = getBoolean("fire.disable-lava-fire-spread", true);
        disableFireSpread = getBoolean("fire.disable-all-fire-spread", false);
        disableFireSpreadBlocks = new HashSet<Integer>(getIntList("fire.disable-fire-spread-blocks", null));
        allowedLavaSpreadOver = new HashSet<Integer>(getIntList("fire.lava-spread-blocks", null));

        blockCreeperExplosions = getBoolean("mobs.block-creeper-explosions", false);
        blockCreeperBlockDamage = getBoolean("mobs.block-creeper-block-damage", false);
        blockWitherExplosions = getBoolean("mobs.block-wither-explosions", false);
        blockWitherBlockDamage = getBoolean("mobs.block-wither-block-damage", false);
        blockWitherSkullExplosions = getBoolean("mobs.block-wither-skull-explosions", false);
        blockWitherSkullBlockDamage = getBoolean("mobs.block-wither-skull-block-damage", false);
        blockEnderDragonBlockDamage = getBoolean("mobs.block-enderdragon-block-damage", false);
        blockEnderDragonPortalCreation = getBoolean("mobs.block-enderdragon-portal-creation", false);
        blockFireballExplosions = getBoolean("mobs.block-fireball-explosions", false);
        blockFireballBlockDamage = getBoolean("mobs.block-fireball-block-damage", false);
        antiWolfDumbness = getBoolean("mobs.anti-wolf-dumbness", false);
        allowTamedSpawns = getBoolean("mobs.allow-tamed-spawns", true);
        disableEndermanGriefing = getBoolean("mobs.disable-enderman-griefing", false);
        disableSnowmanTrails = getBoolean("mobs.disable-snowman-trails", false);
        blockEntityPaintingDestroy = getBoolean("mobs.block-painting-destroy", false);
        blockEntityItemFrameDestroy = getBoolean("mobs.block-item-frame-destroy", false);
        blockPluginSpawning = getBoolean("mobs.block-plugin-spawning", true);
        blockGroundSlimes = getBoolean("mobs.block-above-ground-slimes", false);
        blockOtherExplosions = getBoolean("mobs.block-other-explosions", false);
        blockZombieDoorDestruction = getBoolean("mobs.block-zombie-door-destruction", false);

        disableFallDamage = getBoolean("player-damage.disable-fall-damage", false);
        disableLavaDamage = getBoolean("player-damage.disable-lava-damage", false);
        disableFireDamage = getBoolean("player-damage.disable-fire-damage", false);
        disableLightningDamage = getBoolean("player-damage.disable-lightning-damage", false);
        disableDrowningDamage = getBoolean("player-damage.disable-drowning-damage", false);
        disableSuffocationDamage = getBoolean("player-damage.disable-suffocation-damage", false);
        disableContactDamage = getBoolean("player-damage.disable-contact-damage", false);
        teleportOnSuffocation = getBoolean("player-damage.teleport-on-suffocation", false);
        disableVoidDamage = getBoolean("player-damage.disable-void-damage", false);
        teleportOnVoid = getBoolean("player-damage.teleport-on-void-falling", false);
        disableExplosionDamage = getBoolean("player-damage.disable-explosion-damage", false);
        disableMobDamage = getBoolean("player-damage.disable-mob-damage", false);
        disableDeathMessages = getBoolean("player-damage.disable-death-messages", false);

        signChestProtection = getBoolean("chest-protection.enable", false);
        disableSignChestProtectionCheck = getBoolean("chest-protection.disable-off-check", false);

        disableCreatureCropTrampling = getBoolean("crops.disable-creature-trampling", false);
        disablePlayerCropTrampling = getBoolean("crops.disable-player-trampling", false);

        disallowedLightningBlocks = new HashSet<Integer>(getIntList("weather.prevent-lightning-strike-blocks", null));
        preventLightningFire = getBoolean("weather.disable-lightning-strike-fire", false);
        disableThunder = getBoolean("weather.disable-thunderstorm", false);
        disableWeather = getBoolean("weather.disable-weather", false);
        disablePigZap = getBoolean("weather.disable-pig-zombification", false);
        disableCreeperPower = getBoolean("weather.disable-powered-creepers", false);
        alwaysRaining = getBoolean("weather.always-raining", false);
        alwaysThundering = getBoolean("weather.always-thundering", false);

        disableMushroomSpread = getBoolean("dynamics.disable-mushroom-spread", false);
        disableIceMelting = getBoolean("dynamics.disable-ice-melting", false);
        disableSnowMelting = getBoolean("dynamics.disable-snow-melting", false);
        disableSnowFormation = getBoolean("dynamics.disable-snow-formation", false);
        disableIceFormation = getBoolean("dynamics.disable-ice-formation", false);
        disableLeafDecay = getBoolean("dynamics.disable-leaf-decay", false);
        disableGrassGrowth = getBoolean("dynamics.disable-grass-growth", false);
        disableMyceliumSpread = getBoolean("dynamics.disable-mycelium-spread", false);
        disableVineGrowth = getBoolean("dynamics.disable-vine-growth", false);
        disableSoilDehydration = getBoolean("dynamics.disable-soil-dehydration", false);
        allowedSnowFallOver = new HashSet<Integer>(getIntList("dynamics.snow-fall-blocks", null));

        useRegions = getBoolean("regions.enable", true);
        regionInvinciblityRemovesMobs = getBoolean("regions.invincibility-removes-mobs", false);
        fakePlayerBuildOverride = getBoolean("regions.fake-player-build-override", true);
        explosionFlagCancellation = getBoolean("regions.explosion-flags-block-entity-damage", true);
        highFreqFlags = getBoolean("regions.high-frequency-flags", false);
        checkLiquidFlow = getBoolean("regions.protect-against-liquid-flow", false);
        regionWand = getInt("regions.wand", 334);
        maxClaimVolume = getInt("regions.max-claim-volume", 30000);
        claimOnlyInsideExistingRegions = getBoolean("regions.claim-only-inside-existing-regions", false);

        maxRegionCountPerPlayer = getInt("regions.max-region-count-per-player.default", 7);
        maxRegionCounts = new HashMap<String, Integer>();
        maxRegionCounts.put(null, maxRegionCountPerPlayer);

        for (String key : getKeys("regions.max-region-count-per-player")) {
            if (!key.equalsIgnoreCase("default")) {
                Object val = getProperty("regions.max-region-count-per-player." + key);
                if (val != null && val instanceof Number) {
                    maxRegionCounts.put(key, ((Number) val).intValue());
                }
            }
        }

        // useiConomy = getBoolean("iconomy.enable", false);
        // buyOnClaim = getBoolean("iconomy.buy-on-claim", false);
        // buyOnClaimPrice = getDouble("iconomy.buy-on-claim-price", 1.0);

        blockCreatureSpawn = new HashSet<EntityType>();
        for (String creatureName : getStringList("mobs.block-creature-spawn", null)) {
            EntityType creature = EntityType.fromName(creatureName);

            if (creature == null) {
                log.warning("Unknown mob type '" + creatureName + "'");
            } else if (!creature.isAlive()) {
                log.warning("Entity type '" + creatureName + "' is not a creature");
            } else {
                blockCreatureSpawn.add(creature);
            }
        }

        boolean useBlacklistAsWhitelist = getBoolean("blacklist.use-as-whitelist", false);

        // Console log configuration
        boolean logConsole = getBoolean("blacklist.logging.console.enable", true);

        // Database log configuration
        boolean logDatabase = getBoolean("blacklist.logging.database.enable", false);
        String dsn = getString("blacklist.logging.database.dsn", "jdbc:mysql://localhost:3306/minecraft");
        String user = getString("blacklist.logging.database.user", "root");
        String pass = getString("blacklist.logging.database.pass", "");
        String table = getString("blacklist.logging.database.table", "blacklist_events");

        // File log configuration
        boolean logFile = getBoolean("blacklist.logging.file.enable", false);
        String logFilePattern = getString("blacklist.logging.file.path", "worldguard/logs/%Y-%m-%d.log");
        int logFileCacheSize = Math.max(1, getInt("blacklist.logging.file.open-files", 10));

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
                if (summaryOnStart) {
                    log.log(Level.INFO, "Blacklist loaded.");
                }

                BlacklistLoggerHandler blacklistLogger = blist.getLogger();

                if (logDatabase) {
                    blacklistLogger.addHandler(new DatabaseHandler(dsn, user, pass, table, worldName, log));
                }

                if (logConsole) {
                    blacklistLogger.addHandler(new ConsoleHandler(worldName, log));
                }

                if (logFile) {
                    FileHandler handler =
                            new FileHandler(logFilePattern, logFileCacheSize, worldName, log);
                    blacklistLogger.addHandler(handler);
                }
            }
        } catch (FileNotFoundException e) {
            log.log(Level.WARNING, "WorldGuard blacklist does not exist.");
        } catch (IOException e) {
            log.log(Level.WARNING, "Could not load WorldGuard blacklist: "
                    + e.getMessage());
        }

        // Print an overview of settings
        if (summaryOnStart) {
            log.log(Level.INFO, blockTNTExplosions
                    ? "(" + worldName + ") TNT ignition is blocked."
                    : "(" + worldName + ") TNT ignition is PERMITTED.");
            log.log(Level.INFO, blockLighter
                    ? "(" + worldName + ") Lighters are blocked."
                    : "(" + worldName + ") Lighters are PERMITTED.");
            log.log(Level.INFO, preventLavaFire
                    ? "(" + worldName + ") Lava fire is blocked."
                    : "(" + worldName + ") Lava fire is PERMITTED.");

            if (disableFireSpread) {
                log.log(Level.INFO, "(" + worldName + ") All fire spread is disabled.");
            } else {
                if (disableFireSpreadBlocks.size() > 0) {
                    log.log(Level.INFO, "(" + worldName
                            + ") Fire spread is limited to "
                            + disableFireSpreadBlocks.size() + " block types.");
                } else {
                    log.log(Level.INFO, "(" + worldName
                            + ") Fire spread is UNRESTRICTED.");
                }
            }
        }

        config.setHeader(CONFIG_HEADER);

        config.save();
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
        if (plugin.hasPermission(player, "worldguard.chest-protection.override")
                || plugin.hasPermission(player, "worldguard.override.chest-protection")) {
            return false;
        }
        return chestProtection.isProtected(block, player);
    }

    public boolean isChestProtected(Block block) {

        return signChestProtection && chestProtection.isProtected(block, null);
    }

    public boolean isChestProtectedPlacement(Block block, Player player) {
        if (!signChestProtection) {
            return false;
        }
        if (plugin.hasPermission(player, "worldguard.chest-protection.override")
                || plugin.hasPermission(player, "worldguard.override.chest-protection")) {
            return false;
        }
        return chestProtection.isProtectedPlacement(block, player);
    }

    public boolean isAdjacentChestProtected(Block block, Player player) {
        if (!signChestProtection) {
            return false;
        }
        if (plugin.hasPermission(player, "worldguard.chest-protection.override")
                || plugin.hasPermission(player, "worldguard.override.chest-protection")) {
            return false;
        }
        return chestProtection.isAdjacentChestProtected(block, player);
    }

    public ChestProtection getChestProtection() {
        return chestProtection;
    }

    public int getMaxRegionCount(Player player) {
        int max = -1;
        for (String group : plugin.getGroups(player)) {
            if (maxRegionCounts.containsKey(group)) {
                int groupMax = maxRegionCounts.get(group);
                if (max < groupMax) {
                    max = groupMax;
                }
            }
        }
        if (max <= -1) {
            max = maxRegionCountPerPlayer;
        }
        return max;
    }
}
