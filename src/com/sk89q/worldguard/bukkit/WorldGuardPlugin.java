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


import com.sk89q.worldguard.protection.dbs.CSVDatabase;
import com.sk89q.worldguard.protection.regionmanager.RegionManager;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.nijiko.iConomy.configuration.PropertyHandler;
import com.nijikokun.bukkit.iConomy.iConomy;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.TickSyncDelayLoggerFilter;
import com.sk89q.worldguard.blacklist.*;
import com.sk89q.worldguard.blacklist.loggers.*;
import com.sk89q.worldguard.bukkit.commands.CommandHandler;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.protection.regionmanager.GlobalRegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.World;

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

    private PermissionsResolverServerListener permsListener;
    private PermissionsResolverManager perms;

    private GlobalRegionManager globalRegionManager;
    private CommandHandler commandHandler;

    Blacklist blacklist;

    public Set<String> invinciblePlayers = new HashSet<String>();
    public Set<String> amphibiousPlayers = new HashSet<String>();
    public boolean fireSpreadDisableToggle;

    public static iConomy iConomy;
    
    // Configuration follows
    public boolean suppressTickSyncWarnings;
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
    public boolean useiConomy;
    public boolean buyOnClaim;
    public int buyOnClaimPrice;
    public int regionWand = 287;
    public String blockCreatureSpawn = "";
    /**
     * Construct the plugin.
     * 
     */
    public WorldGuardPlugin() {
    	super();
        this.commandHandler = new CommandHandler(this);

        logger.info("WorldGuard loaded.");
    }

    /**
     * Called on plugin enable.
     */
    public void onEnable() {
        PluginDescriptionFile desc = this.getDescription();
        File folder = this.getDataFolder();

        folder.mkdirs();

        createDefaultConfiguration("config.yml");
        createDefaultConfiguration("blacklist.txt");

        globalRegionManager = new GlobalRegionManager(this);

        perms = new PermissionsResolverManager(getConfiguration(), getServer(),
                "WorldGuard", logger);
        permsListener = new PermissionsResolverServerListener(perms);
        
        loadConfiguration();
        postReload();
        registerEvents();

        if (suppressTickSyncWarnings) {
            Logger.getLogger("Minecraft").setFilter(new TickSyncDelayLoggerFilter());
        } else {
            Filter filter = Logger.getLogger("Minecraft").getFilter();
            if (filter != null && filter instanceof TickSyncDelayLoggerFilter) {
                Logger.getLogger("Minecraft").setFilter(null);
            }
        }

        logger.info("WorldGuard " + desc.getVersion() + " enabled.");
    }

    /**
     * Called on plugin disable.
     */
    public void onDisable() {
        logger.info("WorldGuard " + this.getDescription().getVersion() + " disabled.");
    }

    /**
     * Register used events.
     */
    private void registerEvents() {
        registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.High);
        registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.High);
        registerEvent(Event.Type.BLOCK_FLOW, blockListener, Priority.Normal);
        registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.High);
        registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Normal);
        registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.High);
        registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.High);
        registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.High);
        registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.High);
        registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.High);

        registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, Priority.High);
        registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.High);
        registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Priority.High);

        registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.High);
        registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.High);
        registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.High);
        registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal);
        
        permsListener.register(this);

        // 25 equals about 1s real time
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimedFlagsTimer(this), 25*5, 25*5);

    }

    /**
     * Check if iConomy is enabled on this server
     */
    public void onPluginEnabled(PluginEvent event) {
        if(event.getPlugin().getDescription().getName().equals("iConomy")) {
            this.iConomy = (iConomy)event.getPlugin();
            logger.info("WorldGuard: Attached to iConomy.");
        }
    }
    
    /**
     * Register an event.
     * 
     * @param type
     * @param listener
     * @param priority
     */
    private void registerEvent(Event.Type type, Listener listener, Priority priority) {
        getServer().getPluginManager().registerEvent(type, listener, priority, this);
    }
    
    /**
     * Create a default configuration file from the .jar.
     * 
     * @param name
     */
    private void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            
            InputStream input =
                    WorldGuardPlugin.class.getResourceAsStream("/defaults/" + name);
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
                            + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (IOException e) {}

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {}
                }
            }
        }
    }

    /**
     * Load the configuration.
     */
    public void loadConfiguration() {
        Configuration config = getConfiguration();
        config.load();
        perms.load();
        
        suppressTickSyncWarnings = config.getBoolean("suppress-tick-sync-warnings", false);

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

        useiConomy = config.getBoolean("iconomy.enable", false);
        buyOnClaim = config.getBoolean("iconomy.buy-on-claim", false);
        buyOnClaimPrice = config.getInt("iconomy.buy-on-claim-price", 1);

        GlobalFlags globalFlags = new GlobalFlags();
        globalFlags.canBuild = config.getBoolean("regions.default.build", true);
        globalFlags.canAccessChests = config.getBoolean("regions.default.chest-access", false);
        globalFlags.canPvP = config.getBoolean("regions.default.pvp", true);
        globalFlags.canLighter = config.getBoolean("regions.default.lighter", true);
        globalFlags.canTnt = config.getBoolean("regions.default.tnt", true);
        globalFlags.allowCreeper = config.getBoolean("regions.default.creeper", true);
        globalFlags.allowMobDamage = config.getBoolean("regions.default.mobdamage", true);
        globalFlags.allowWaterflow = config.getBoolean("regions.default.waterflow", true);
        globalRegionManager.setGlobalFlags(globalFlags);

        try {
            File CSVfile = new File(this.getDataFolder(), "regions.txt");
            if (CSVfile.exists()) {

                logger.info("WorldGuard: Converting old regions.txt to new format....");

                World w = this.getServer().getWorlds().get(0);
                RegionManager mgr = globalRegionManager.getRegionManager(w.getName());

                CSVDatabase db = new CSVDatabase(CSVfile);
                db.load();

                for (Map.Entry<String, ProtectedRegion> entry : db.getRegions().entrySet()) {
                    mgr.addRegion(entry.getValue());
                }

                mgr.save();
                CSVfile.renameTo(new File(this.getDataFolder(), "regions.txt.old"));
                
                logger.info("WorldGuard: Done.");
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.warning("WorldGuard: Failed to load regions: "
                    + e.getMessage());
        }

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
            Blacklist blist = new BukkitBlacklist(this);
            blist.load(new File(getDataFolder(), "blacklist.txt"));

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
        }

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
        
        // Temporary
        perms.load();
    }
    
    /**
     * Populates various lists.
     */
    public void postReload() {
        invinciblePlayers.clear();
        amphibiousPlayers.clear();

        try {
            for (Player player : getServer().getOnlinePlayers()) {
                if (inGroup(player, "wg-invincible")) {
                    invinciblePlayers.add(player.getName());
                }

                if (inGroup(player, "wg-amphibious")) {
                    amphibiousPlayers.add(player.getName());
                }
            }
        } catch (NullPointerException e) { // Thrown if loaded too early
        }
    }

    /**
     * Handles a command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

            return commandHandler.handleCommand(sender, cmd, commandLabel, args);
    }


    

    /**
     * Get the region manager.
     * 
     * @return
     */
    public GlobalRegionManager getGlobalRegionManager() {
        return globalRegionManager;
    }
    
    public boolean canBuild(Player player, int x, int y, int z) {
        
        if (useRegions) {
            Vector pt = new Vector(x, y, z);
            LocalPlayer localPlayer = wrapPlayer(player);

            if (!hasPermission(player, "/regionbypass")) {
                RegionManager mgr = globalRegionManager.getRegionManager(player.getWorld().getName());

                if (!mgr.getApplicableRegions(pt).canBuild(localPlayer)) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }
    
    public boolean canBuild(Player player, Vector pt) {

        if (useRegions) {
            LocalPlayer localPlayer = wrapPlayer(player);

            if (!hasPermission(player, "/regionbypass")) {
                RegionManager mgr = globalRegionManager.getRegionManager(player.getWorld().getName());

                if (!mgr.getApplicableRegions(pt).canBuild(localPlayer)) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public boolean inGroup(Player player, String group) {
        try {
            return perms.inGroup(player.getName(), group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    public boolean hasPermission(Player player, String perm) {
        try {
            return player.isOp() || perms.hasPermission(player.getName(), perm);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    public String[] getGroups(Player player) {
        try {
            return perms.getGroups(player.getName());
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    public BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, player);
    }


}
