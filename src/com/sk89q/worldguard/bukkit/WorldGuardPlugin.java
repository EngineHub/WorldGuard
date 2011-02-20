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

import static com.sk89q.worldguard.bukkit.BukkitUtil.matchSinglePlayer;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.TickSyncDelayLoggerFilter;
import com.sk89q.worldguard.blacklist.*;
import com.sk89q.worldguard.blacklist.loggers.*;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.*;
import com.sk89q.worldguard.protection.AreaFlags.State;
import com.sk89q.worldguard.protection.ProtectedRegion.CircularInheritanceException;

/**
 * Plugin for Bukkit.
 * 
 * @author sk89qs
 */
public final class WorldGuardPlugin extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    private static Pattern groupPattern = Pattern.compile("^[gG]:(.+)$");
    private static int CMD_LIST_SIZE = 9;
    
    private final WorldGuardPlayerListener playerListener =
        new WorldGuardPlayerListener(this);
    private final WorldGuardBlockListener blockListener =
        new WorldGuardBlockListener(this);
    private final WorldGuardEntityListener entityListener =
        new WorldGuardEntityListener(this);
    private final PermissionsResolverServerListener permsListener;
    
    private final PermissionsResolverManager perms;
    
    Blacklist blacklist;

    GlobalFlags globalFlags = new GlobalFlags();
    RegionManager regionManager = new FlatRegionManager(globalFlags);
    ProtectionDatabase regionLoader;
    
    Set<String> invinciblePlayers = new HashSet<String>();
    Set<String> amphibiousPlayers = new HashSet<String>();
    boolean fireSpreadDisableToggle;
    
    // Configuration follows
    
    boolean suppressTickSyncWarnings;
    
    boolean enforceOneSession;
    boolean itemDurability;

    boolean classicWater;
    boolean simulateSponge;
    int spongeRadius;
    boolean redstoneSponges;

    boolean noPhysicsGravel;
    boolean noPhysicsSand;
    boolean allowPortalAnywhere;
    Set<Integer> preventWaterDamage;

    boolean blockTNT;
    boolean blockLighter;

    boolean disableFireSpread;
    Set<Integer> disableFireSpreadBlocks;
    boolean preventLavaFire;
    Set<Integer> allowedLavaSpreadOver;
    
    boolean blockCreeperExplosions;
    boolean blockCreeperBlockDamage;

    int loginProtection;
    int spawnProtection;
    boolean kickOnDeath;
    boolean exactRespawn;
    boolean teleportToHome;

    boolean disableContactDamage;
    boolean disableFallDamage;
    boolean disableLavaDamage;
    boolean disableFireDamage;
    boolean disableDrowningDamage;
    boolean disableSuffocationDamage;
    boolean teleportOnSuffocation;

    boolean useRegions;
    int regionWand = 287; 
    
    /**
     * Construct the plugin.
     * 
     * @param pluginLoader
     * @param instance
     * @param desc
     * @param folder
     * @param plugin
     * @param cLoader
     */
    public WorldGuardPlugin() {
        super();

        logger.info("WorldGuard " + getDescription().getVersion() + " loaded.");
        
        getDataFolder().mkdirs();

        createDefaultConfiguration("config.yml");
        createDefaultConfiguration("blacklist.txt");
        
        regionLoader = new CSVDatabase(new File(getDataFolder(), "regions.txt"));
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
    }

    /**
     * Called on plugin enable.
     */
    public void onEnable() {
    }

    /**
     * Called on plugin disable.
     */
    public void onDisable() {
    }

    /**
     * Register used events.
     */
    private void registerEvents() {
        registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.High);
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

        registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.High);
        registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal);
        registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal);
        
        permsListener.register(this);
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
        globalFlags.canBuild = config.getBoolean("regions.default.build", true);
        globalFlags.canAccessChests = config.getBoolean("regions.default.chest-access", false);
        globalFlags.canPvP = config.getBoolean("regions.default.pvp", true);

        try {
            regionLoader.load();
            regionManager.setRegions(regionLoader.getRegions());
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
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        try {
            return handleCommand(sender, cmd.getName(), args);
        } catch (InsufficientArgumentsException e) {
            if (e.getHelp() != null) {
                sender.sendMessage(ChatColor.RED + e.getHelp());
                return true;
            } else {
                return false;
            }
        } catch (InsufficientPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have sufficient permission.");
            return true;
        } catch (CommandHandlingException e) {
            return true;
        } catch (Throwable t) {
            sender.sendMessage(ChatColor.RED + "ERROR: " + t.getMessage());
            t.printStackTrace();
            return true;
        }
    }
    
    /**
     * Internal method to handle a command.
     * 
     * @param player
     * @param cmd
     * @param args
     * @return
     * @throws CommandHandlingException
     */
    private boolean handleCommand(CommandSender sender, String cmd, String[] args)
            throws CommandHandlingException {
        
        String senderName = sender instanceof Player ? ((Player)sender).getName() : "Console";
        
        if (cmd.equalsIgnoreCase("stopfire")) {
            checkPermission(sender, "/stopfire");
            checkArgs(args, 0, 0);
            
            if (!fireSpreadDisableToggle) {
                getServer().broadcastMessage(ChatColor.YELLOW
                        + "Fire spread has been globally disabled by " + senderName + ".");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Fire spread was already globally disabled.");
            }
            
            fireSpreadDisableToggle = true;
            
            return true;
        }
        
        if (cmd.equalsIgnoreCase("allowfire")) {
            checkPermission(sender, "/stopfire");
            checkArgs(args, 0, 0);
            
            if (fireSpreadDisableToggle) {
                getServer().broadcastMessage(ChatColor.YELLOW
                        + "Fire spread has been globally re-enabled by " + senderName + ".");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Fire spread was already globally enabled.");
            }
            
            fireSpreadDisableToggle = false;
            
            return true;
        }
        
        if (!(sender instanceof Player)) {
            return false;
        }
        
        Player player = (Player)sender;
        
        if (cmd.equalsIgnoreCase("god")) {
            checkPermission(player, "/god");
            checkArgs(args, 0, 1);
            
            // Allow setting other people invincible
            if (args.length > 0) {
                if (!hasPermission(player, "/godother")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to make others invincible.");
                    return true;
                }

                Player other = matchSinglePlayer(getServer(), args[0]);
                if (other == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                } else {
                    if (!invinciblePlayers.contains(other.getName())) {
                        invinciblePlayers.add(other.getName());
                        player.sendMessage(ChatColor.YELLOW + other.getName() + " is now invincible!");
                        other.sendMessage(ChatColor.YELLOW + player.getName() + " has made you invincible!");
                    } else {
                        invinciblePlayers.remove(other.getName());
                        player.sendMessage(ChatColor.YELLOW + other.getName() + " is no longer invincible.");
                        other.sendMessage(ChatColor.YELLOW + player.getName() + " has taken away your invincibility.");
                    }
                }
            // Invincibility for one's self
            } else {
                if (!invinciblePlayers.contains(player.getName())) {
                    invinciblePlayers.add(player.getName());
                    player.sendMessage(ChatColor.YELLOW + "You are now invincible!");
                } else {
                    invinciblePlayers.remove(player.getName());
                    player.sendMessage(ChatColor.YELLOW + "You are no longer invincible.");
                }
            }
            
            return true;
        }
        
        if (cmd.equalsIgnoreCase("heal")) {
            checkPermission(player, "/heal");
            checkArgs(args, 0, 1);
            
            // Allow healing other people
            if (args.length > 0) {
                if (!hasPermission(player, "/healother")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to heal others.");
                    return true;
                }

                Player other = matchSinglePlayer(getServer(), args[0]);
                if (other == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                } else {
                    other.setHealth(20);
                    player.sendMessage(ChatColor.YELLOW + other.getName() + " has been healed!");
                    other.sendMessage(ChatColor.YELLOW + player.getName() + " has healed you!");
                }
            } else {
                player.setHealth(20);
                player.sendMessage(ChatColor.YELLOW + "You have been healed!");
            }
            
            return true;
        }
        
        if (cmd.equalsIgnoreCase("slay")) {
            checkPermission(player, "/slay");
            checkArgs(args, 0, 1);
            
            // Allow killing other people
            if (args.length > 0) {
                if (!hasPermission(player, "/slayother")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to kill others.");
                    return true;
                }

                Player other = matchSinglePlayer(getServer(), args[0]);
                if (other == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                } else {
                    other.setHealth(0);
                    player.sendMessage(ChatColor.YELLOW + other.getName() + " has been killed!");
                    other.sendMessage(ChatColor.YELLOW + player.getName() + " has killed you!");
                }
            } else {
                player.setHealth(0);
                player.sendMessage(ChatColor.YELLOW + "You have committed suicide!");
            }
            
            return true;
        }
        
        if (cmd.equalsIgnoreCase("stack")) {
            checkPermission(player, "/stack");
            checkArgs(args, 0, 0);
            
            ItemStack[] items = player.getInventory().getContents();
            int len = items.length;

            int affected = 0;
            
            for (int i = 0; i < len; i++) {
                ItemStack item = items[i];

                // Avoid infinite stacks and stacks with durability
                if (item == null || item.getAmount() <= 0
                        || ItemType.shouldNotStack(item.getTypeId())) {
                    continue;
                }

                // Ignore buckets
                if (item.getTypeId() >= 325 && item.getTypeId() <= 327) {
                    continue;
                }

                if (item.getAmount() < 64) {
                    int needed = 64 - item.getAmount(); // Number of needed items until 64

                    // Find another stack of the same type
                    for (int j = i + 1; j < len; j++) {
                        ItemStack item2 = items[j];

                        // Avoid infinite stacks and stacks with durability
                        if (item2 == null || item2.getAmount() <= 0
                                || ItemType.shouldNotStack(item.getTypeId())) {
                            continue;
                        }

                        // Same type?
                        // Blocks store their color in the damage value
                        if (item2.getTypeId() == item.getTypeId() &&
                                (!ItemType.usesDamageValue(item.getTypeId())
                                        || item.getDurability() == item2.getDurability())) {
                            // This stack won't fit in the parent stack
                            if (item2.getAmount() > needed) {
                                item.setAmount(64);
                                item2.setAmount(item2.getAmount() - needed);
                                break;
                            // This stack will
                            } else {
                                items[j] = null;
                                item.setAmount(item.getAmount() + item2.getAmount());
                                needed = 64 - item.getAmount();
                            }

                            affected++;
                        }
                    }
                }
            }

            if (affected > 0) {
                player.getInventory().setContents(items);
            }

            player.sendMessage(ChatColor.YELLOW + "Items compacted into stacks!");
            
            return true;
        }
        
        if (cmd.equalsIgnoreCase("locate")) {
            checkPermission(player, "/locate");
            checkArgs(args, 0, 3);

            if (args.length == 1) {
                String name = args[0];
                Player target = BukkitUtil.matchSinglePlayer(getServer(), name);
                if (target != null) {
                    player.setCompassTarget(target.getLocation());
                    player.sendMessage(ChatColor.YELLOW + "Compass target set to " + target.getName() + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find player.");
                }
            } else if (args.length == 3) {
                try {
                    Location loc = new Location(
                            player.getWorld(),
                            Integer.parseInt(args[0]),
                            Integer.parseInt(args[1]),
                            Integer.parseInt(args[2])
                            );
                    player.setCompassTarget(loc);
                    player.sendMessage(ChatColor.YELLOW + "Compass target set to "
                            +  loc.getBlockX() + ","
                            + loc.getBlockY() + ","
                            + loc.getBlockZ() + ".");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number specified");
                }
            } else if (args.length == 0) {
                player.setCompassTarget(player.getWorld().getSpawnLocation());
                player.sendMessage(ChatColor.YELLOW + "Compass reset to the spawn location.");
            } else {
                return false;
            }

            return true;
        }
        
        if (cmd.equalsIgnoreCase("region")) {
            checkArgs(args, 1, -1);
            
            String action = args[0];
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            return handleRegionCommand(player, action, subArgs);
        }
        
        if (cmd.equalsIgnoreCase("reloadwg")) {
            checkPermission(player, "/reloadwg");
            checkArgs(args, 0, 0);
            
            LoggerToChatHandler handler = new LoggerToChatHandler(player);
            handler.setLevel(Level.ALL);
            Logger minecraftLogger = Logger.getLogger("Minecraft");
            minecraftLogger.addHandler(handler);

            try {
                loadConfiguration();
                postReload();
                player.sendMessage("WorldGuard configuration reloaded.");
            } catch (Throwable t) {
                player.sendMessage("Error while reloading: "
                        + t.getMessage());
            } finally {
                minecraftLogger.removeHandler(handler);
            }

            return true;
        }
        
        return false;
    }
    
    /**
     * Handles a region command.
     * 
     * @param player
     * @param action
     * @param args
     * @throws CommandHandlingException
     */
    private boolean handleRegionCommand(Player player, String action, String[] args)
            throws CommandHandlingException {
        if (!useRegions) {
            player.sendMessage(ChatColor.RED + "Regions are disabled.");
            return true;
        }

        Plugin wePlugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if (wePlugin == null) {
            player.sendMessage(ChatColor.RED + "WorldEdit must be installed and enabled!");
            return true;
        }
        
        if (action.equalsIgnoreCase("define")) {
            checkRegionPermission(player, "/regiondefine");
            checkArgs(args, 1, -1, "/region define <id> [owner1 [owner2 [owners...]]]");
            
            try {
                String id = args[0].toLowerCase();
                
                WorldEditPlugin worldEdit = (WorldEditPlugin)wePlugin;
                WorldEditAPI api = worldEdit.getAPI();
                
                LocalSession session = api.getSession(player);
                Region weRegion = session.getRegion();
                
                BlockVector min = weRegion.getMinimumPoint().toBlockVector();
                BlockVector max = weRegion.getMaximumPoint().toBlockVector();
                
                ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
                if (args.length >= 2) {
                    region.setOwners(parseDomainString(args, 1));
                }
                regionManager.addRegion(region);
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.RED + "You must first define an area in WorldEdit.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("claim")) {
            checkRegionPermission(player, "/regionclaim");
            checkArgs(args, 1, 1, "/region claim <id>");
            
            try {
                String id = args[0].toLowerCase();

                ProtectedRegion existing = regionManager.getRegion(id);
                
                if (existing != null) {
                    if (!existing.getOwners().contains(wrapPlayer(player))) {
                        player.sendMessage(ChatColor.RED + "You don't own this region.");
                        return true;
                    }
                }
                
                WorldEditPlugin worldEdit = (WorldEditPlugin)wePlugin;
                WorldEditAPI api = worldEdit.getAPI();
                
                LocalSession session = api.getSession(player);
                Region weRegion = session.getRegion();
                
                BlockVector min = weRegion.getMinimumPoint().toBlockVector();
                BlockVector max = weRegion.getMaximumPoint().toBlockVector();
                
                ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
                
                if (regionManager.overlapsUnownedRegion(region, wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "This region overlaps with someone else's region.");
                    return true;
                }
                
                region.getOwners().addPlayer(player.getName());
                
                regionManager.addRegion(region);
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region saved as " + id + ".");
            } catch (IncompleteRegionException e) {
                player.sendMessage(ChatColor.RED + "You must first define an area in WorldEdit.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("flag")) {
            checkRegionPermission(player, "/regiondefine");
            checkArgs(args, 3, 3, "/region flag <id> <flag> <none|allow|deny>");
            
            try {
                String id = args[0].toLowerCase();
                String flagStr = args[1];
                String stateStr = args[2];
                ProtectedRegion region = regionManager.getRegion(id);
                
                if (region == null) {
                    player.sendMessage(ChatColor.RED + "Could not find a region by that ID.");
                    return true;
                }
                
                AreaFlags.State state = null;
    
                if (stateStr.equalsIgnoreCase("allow")) {
                    state = AreaFlags.State.ALLOW;
                } else if (stateStr.equalsIgnoreCase("deny")) {
                    state = AreaFlags.State.DENY;
                } else if (stateStr.equalsIgnoreCase("none")) {
                    state = AreaFlags.State.NONE;
                } else {
                    player.sendMessage(ChatColor.RED + "Acceptable states: allow, deny, none");
                    return true;
                }
                
                if (flagStr.length() == 0) {
                    player.sendMessage(ChatColor.RED + "A flag must be specified.");
                    return true;
                    // Custom flag
                } else if (flagStr.length() == 2 && flagStr.matches("^_[A-Za-z0-0]$")) {
                } else {
                    flagStr = AreaFlags.fromAlias(flagStr);
                    if (flagStr == null) {
                        player.sendMessage(ChatColor.RED + "Unknown flag specified.");
                        return true;
                    }
                }

                AreaFlags flags = region.getFlags();
                flags.set(flagStr, state);
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("setparent")) {
            if (!hasPermission(player, "/regionclaim")) {
                checkRegionPermission(player, "/regiondefine");
            }
            checkArgs(args, 1, 2, "/region setparent <id> <parent-id>");
            
            String id = args[0].toLowerCase();
            String parentId = args.length > 1 ? args[1].toLowerCase() : null;
            
            ProtectedRegion region = regionManager.getRegion(id);
            
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Could not find a region with ID: " + id);
                return true;
            }
            
            if (!canUseRegionCommand(player, "/regiondefine")
                    && !region.isOwner(wrapPlayer(player))) {
                player.sendMessage(ChatColor.RED + "You need to own the target regions");
                return true;
            }
            
            ProtectedRegion parent = null;
            
            // Set a parent
            if (parentId != null) {
                parent = regionManager.getRegion(parentId);
                
                if (parent == null) {
                    player.sendMessage(ChatColor.RED + "Could not find a region with ID: " + parentId);
                    return true;
                }
                
                if (!canUseRegionCommand(player, "/regiondefine")
                        && !parent.isOwner(wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "You need to own the parent region.");
                    return true;
                }
            }
            
            try {
                region.setParent(parent);
                
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region '" + id + "' updated.");
            } catch (CircularInheritanceException e) {
                player.sendMessage(ChatColor.RED + "Circular inheritance detected. The operation failed.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("info")) {
            checkRegionPermission(player, "/regioninfo");
            checkArgs(args, 1, 1, "/region info <id>");
    
            String id = args[0].toLowerCase();
            if (!regionManager.hasRegion(id)) {
                player.sendMessage(ChatColor.RED + "A region with ID '"
                        + id + "' doesn't exist.");
                return true;
            }
    
            ProtectedRegion region = regionManager.getRegion(id);
            AreaFlags flags = region.getFlags();
            DefaultDomain owners = region.getOwners();
            DefaultDomain members = region.getMembers();
            
            player.sendMessage(ChatColor.YELLOW + "Region: " + id
                    + ChatColor.GRAY + " (type: " + region.getTypeName() + ")");
            player.sendMessage(ChatColor.BLUE + "Priority: " + region.getPriority());
            
            StringBuilder s = new StringBuilder();
            for (Map.Entry<String, State> entry : flags.entrySet()) {
                if (s.length() > 0) {
                    s.append(", ");
                }
                
                if (entry.getValue() == State.ALLOW) {
                    s.append("+");
                    s.append(AreaFlags.getFlagName(entry.getKey()));
                } else if (entry.getValue() == State.DENY) {
                    s.append("-");
                    s.append(AreaFlags.getFlagName(entry.getKey()));
                }
            }
            
            player.sendMessage(ChatColor.BLUE + "Flags: " + s.toString());
            player.sendMessage(ChatColor.BLUE + "Parent: "
                    + (region.getParent() == null ? "(none)" : region.getParent().getId()));
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Owners: "
                    + owners.toUserFriendlyString());
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Members: "
                    + members.toUserFriendlyString());
            return true;
        }

        if (action.equalsIgnoreCase("addowner") || action.equalsIgnoreCase("addmember")) {
            if (!hasPermission(player, "/regionclaim") && !hasPermission(player, "/regionmembership")) {
                checkRegionPermission(player, "/regiondefine");
            }
            checkArgs(args, 2, -1, "/region add[member|owner] <id> [player1 [group1 [players/groups...]]]");
            
            boolean isOwner = action.equalsIgnoreCase("addowner");

            String id = args[0].toLowerCase();
            if (!regionManager.hasRegion(id)) {
                player.sendMessage(ChatColor.RED + "A region with ID '"
                        + id + "' doesn't exist.");
                return true;
            }
            
            ProtectedRegion existing = regionManager.getRegion(id);
            
            if (!canUseRegionCommand(player, "/regiondefine")
                    && !existing.isOwner(wrapPlayer(player))) {
                player.sendMessage(ChatColor.RED + "You don't own this region.");
                return true;
            }
            
            if (isOwner) {
                addToDomain(existing.getOwners(), args, 1);
            } else {
                addToDomain(existing.getMembers(), args, 1);
            }

            try {
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region updated!");
                player.sendMessage(ChatColor.GRAY + "Current owners: "
                        + existing.getOwners().toUserFriendlyString());
                player.sendMessage(ChatColor.GRAY + "Current members: "
                        + existing.getMembers().toUserFriendlyString());
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("removeowner") || action.equalsIgnoreCase("removemember")) {
            if (!hasPermission(player, "/regionclaim") && !hasPermission(player, "/regionmembership")) {
                checkRegionPermission(player, "/regiondefine");
            }
            checkArgs(args, 2, -1, "/region removeowner <id> [owner1 [owner2 [owners...]]]");
            
            boolean isOwner = action.equalsIgnoreCase("removeowner");
            
            String id = args[0].toLowerCase();
            if (!regionManager.hasRegion(id)) {
                player.sendMessage(ChatColor.RED + "A region with ID '"
                        + id + "' doesn't exist.");
                return true;
            }
            
            ProtectedRegion existing = regionManager.getRegion(id);
            
            if (!canUseRegionCommand(player, "/regiondefine")
                    && !existing.isOwner(wrapPlayer(player))) {
                player.sendMessage(ChatColor.RED + "You don't own this region.");
                return true;
            }

            if (isOwner) {
                removeFromDomain(existing.getOwners(), args, 1);
            } else {
                removeFromDomain(existing.getMembers(), args, 1);
            }
                
            try {
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region updated!");
                player.sendMessage(ChatColor.GRAY + "Current owners: "
                        + existing.getOwners().toUserFriendlyString());
                player.sendMessage(ChatColor.GRAY + "Current members: "
                        + existing.getMembers().toUserFriendlyString());
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("list")) {
            checkRegionPermission(player, "/regionlist");
            checkArgs(args, 0, 1, "/region list [page]");
            
            int page = 0;
            
            if (args.length >= 1) {
                try {
                    page = Math.max(0, Integer.parseInt(args[0]) - 1);
                } catch (NumberFormatException e) {
                    page = 0;
                }
            }
    
            Map<String,ProtectedRegion> regions = regionManager.getRegions();
            int size = regions.size();
            int pages = (int)Math.ceil(size / (float)CMD_LIST_SIZE);
            
            String[] regionIDList = new String[size];
            int index = 0;
            for (String id : regions.keySet()) {
                regionIDList[index] = id;
                index++;
            }
            Arrays.sort(regionIDList);
            
            
            player.sendMessage(ChatColor.RED + "Regions (page "
                    + (page + 1) + " of " + pages + "):");
            
            if (page < pages) {
                for (int i = page * CMD_LIST_SIZE; i < page * CMD_LIST_SIZE + CMD_LIST_SIZE; i++) {
                    if (i >= size) break;
                    player.sendMessage(ChatColor.YELLOW.toString() + (i + 1) + ". " + regionIDList[i]);
                }
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("delete")) {
            if (!hasPermission(player, "/regionclaim")) {
                checkRegionPermission(player, "/regiondelete");
            }
            checkArgs(args, 0, 1, "/region delete <id>");
    
            try {
                String id = args[0].toLowerCase();
                if (!regionManager.hasRegion(id)) {
                    player.sendMessage(ChatColor.RED + "A region with ID '"
                            + id + "' doesn't exist.");
                    return true;
                }

                ProtectedRegion existing = regionManager.getRegion(id);
                
                if (!canUseRegionCommand(player, "/regiondelete")
                        && !existing.isOwner(wrapPlayer(player))) {
                    player.sendMessage(ChatColor.RED + "You don't own this region.");
                    return true;
                }
                
                regionManager.removeRegion(id);
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region removed!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }

        if (action.equalsIgnoreCase("save")) {
            checkRegionPermission(player, "/regionsave");
            checkArgs(args, 0, 0, "/region save");
            
            try {
                regionLoader.save(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region database saved to file!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to save: "
                        + e.getMessage());
            }
            
            return true;
        }
        
        if (action.equalsIgnoreCase("load")) {
            checkRegionPermission(player, "/regionload");
            checkArgs(args, 0, 0, "/region load");
            
            try {
                regionLoader.load(regionManager);
                player.sendMessage(ChatColor.YELLOW + "Region database loaded from file!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Region database failed to load: "
                        + e.getMessage());
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain
     * @param split
     * @param startIndex
     */
    private static void addToDomain(DefaultDomain domain,
            String[] split, int startIndex) {        
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }
    }
    
    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param domain
     * @param split
     * @param startIndex
     */
    private static void removeFromDomain(DefaultDomain domain,
            String[] split, int startIndex) {        
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.removeGroup(m.group(1));
            } else {
                domain.removePlayer(s);
            }
        }
    }
    
    /**
     * Parse a group/player DefaultDomain specification for areas.
     * 
     * @param split
     * @param startIndex
     * @return
     */
    private static DefaultDomain parseDomainString(String[] split, int startIndex) {
        DefaultDomain domain = new DefaultDomain();
        
        for (int i = startIndex; i < split.length; i++) {
            String s = split[i];
            Matcher m = groupPattern.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                domain.addPlayer(s);
            }
        }
        
        return domain;
    }
    
    /**
     * Checks for the command or /region.
     * 
     * @param player
     * @param cmd
     * @return
     */
    private boolean canUseRegionCommand(Player player, String cmd) {
        return hasPermission(player, "/region")
                || hasPermission(player, cmd);
    }
    
    /**
     * Checks to see if there are sufficient permissions, otherwise an exception
     * is raised in that case.
     * 
     * @param player
     * @param permission
     * @throws InsufficientPermissionsException
     */
    private void checkRegionPermission(Player player, String permission)
            throws InsufficientPermissionsException {
        if (!hasPermission(player, "/region") && !hasPermission(player, permission)) {
            throw new InsufficientPermissionsException();
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
    private void checkPermission(CommandSender sender, String permission)
            throws InsufficientPermissionsException {
        if (!(sender instanceof Player)) {
            return;
        }
        if (!hasPermission((Player)sender, permission)) {
            throw new InsufficientPermissionsException();
        }
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min, int max)
            throws InsufficientArgumentsException {
        if (args.length < min || (max != -1 && args.length > max)) {
            throw new InsufficientArgumentsException();
        }
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param help
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min, int max, String help)
            throws InsufficientArgumentsException {
        if (args.length < min || (max != -1 && args.length > max)) {
            throw new InsufficientArgumentsException(help);
        }
    }
    
    /**
     * Get the region manager.
     * 
     * @return
     */
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    /**
     * Get the region loader.
     * 
     * @return
     */
    public ProtectionDatabase getRegionLoader() {
        return regionLoader;
    }
    
    public boolean canBuild(Player player, int x, int y, int z) {
        if (useRegions) {
            Vector pt = new Vector(x, y, z);
            LocalPlayer localPlayer = wrapPlayer(player);

            if (!hasPermission(player, "/regionbypass")
                    && !regionManager.getApplicableRegions(pt).canBuild(localPlayer)) {
                return false;
            }
            
            return true;
        } else {
            return true;
        }
    }
    
    boolean inGroup(Player player, String group) {
        try {
            return perms.inGroup(player.getName(), group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    boolean hasPermission(Player player, String perm) {
        try {
            return player.isOp() || perms.hasPermission(player.getName(), perm);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    String[] getGroups(Player player) {
        try {
            return perms.getGroups(player.getName());
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, player);
    }

    /**
     * Thrown when command handling has raised an exception.
     * 
     * @author sk89q
     */
    private static class CommandHandlingException extends Exception {
        private static final long serialVersionUID = 7912130636812036780L;
    }

    /**
     * Thrown when a player has insufficient permissions.
     * 
     * @author sk89q
     */
    private static class InsufficientPermissionsException extends CommandHandlingException {
        private static final long serialVersionUID = 9087662707619954750L;
    }
    
    /**
     * Thrown when a command wasn't given sufficient arguments. 
     * 
     * @author sk89q
     */
    private static class InsufficientArgumentsException extends CommandHandlingException {
        private static final long serialVersionUID = 4153597953889773788L;
        private final String help;
        
        public InsufficientArgumentsException() {
            help = null;
        }
        
        public InsufficientArgumentsException(String msg) {
            this.help = msg;
        }
        
        public String getHelp() {
            return help;
        }
    }
}
