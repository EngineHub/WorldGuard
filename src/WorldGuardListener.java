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

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.io.*;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class WorldGuardListener extends PluginListener {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    /**
     * Timer for threading.
     */
    private static final Timer timer = new Timer();
    /**
     * Random number generator.
     */
    private static Random rand = new Random();

    /**
     * Plugin host.
     */
    private WorldGuard plugin;
    /**
     * Properties file for WorldGuard.
     */
    private PropertiesFile properties = new PropertiesFile("worldguard.properties");
    /**
     * List of players with god mode on.
     */
    private Set<String> invinciblePlayers = new HashSet<String>();
    /**
     * List of amphibious players.
     */
    private Set<String> amphibiousPlayers = new HashSet<String>();
    /**
     * Used to keep recent join times.
     */
    private Map<String,Long> recentLogins = new HashMap<String,Long>();
    /**
     * Used to keep recent spawn times.
     */
    private Map<String,Long> lastSpawn = new HashMap<String,Long>();

    private boolean stopFireSpread = false;
    private boolean enforceOneSession;
    private boolean blockCreepers;
    private boolean blockTNT;
    private boolean blockLighter;
    private boolean preventLavaFire;
    private boolean disableAllFire;
    private boolean simulateSponge;
    private int spongeRadius;
    private boolean itemDurability;
    private Set<Integer> fireNoSpreadBlocks;
    private Set<Integer> allowedLavaSpreadOver;
    private Set<Integer> itemDropBlacklist;
    private Set<Integer> preventWaterDamage;
    private boolean classicWater;
    private boolean noPhysicsGravel;
    private boolean noPhysicsSand;
    private boolean allowPortalAnywhere;
    private boolean disableFallDamage;
    private boolean disableLavaDamage;
    private boolean disableFireDamage;
    private boolean disableWaterDamage;
    private int loginProtection;
    private int spawnProtection;
    private boolean kickOnDeath;
    private Blacklist blacklist;

    /**
     * Construct the listener.
     * 
     * @param plugin
     */
    public WorldGuardListener(WorldGuard plugin) {
        this.plugin = plugin;
        postReload();
    }

    /**
     * Convert a comma-delimited list to a set of integers.
     * 
     * @param str
     * @return
     */
    private static Set<Integer> toBlockIDSet(String str) {
        if (str.trim().length() == 0) {
            return null;
        }
        
        String[] items = str.split(",");
        Set<Integer> result = new HashSet<Integer>();

        for (String item : items) {
            try {
                result.add(Integer.parseInt(item.trim()));
            } catch (NumberFormatException e) {
                int id = etc.getDataSource().getItem(item.trim());
                if (id != 0) {
                    result.add(id);
                } else {
                    logger.log(Level.WARNING, "WorldGuard: Unknown block name: "
                            + item);
                }
            }
        }

        return result;
    }

    /**
     * Populates various lists.
     */
    public void postReload() {
        invinciblePlayers.clear();
        amphibiousPlayers.clear();
        
        for (Player player : etc.getServer().getPlayerList()) {
            if (player.isInGroup("wg-invincible")) {
                invinciblePlayers.add(player.getName());
            }

            if (player.isInGroup("wg-amphibious")) {
                amphibiousPlayers.add(player.getName());
            }
        }
    }

    /**
     * Load the configuration
     */
    public void loadConfiguration() {
        try {
            properties.load();
        } catch (IOException e) {
            logger.log(Level.WARNING, "WorldGuard: Failed to load configuration: "
                    + e.getMessage());
        }

        recentLogins.clear();

        // Load basic options
        enforceOneSession = properties.getBoolean("enforce-single-session", true);
        blockCreepers = properties.getBoolean("block-creepers", false);
        blockTNT = properties.getBoolean("block-tnt", false);
        blockLighter = properties.getBoolean("block-lighter", false);
        preventLavaFire = properties.getBoolean("disable-lava-fire", true);
        disableAllFire = properties.getBoolean("disable-all-fire-spread", false);
        preventWaterDamage = toBlockIDSet(properties.getString("disable-water-damage-blocks", ""));
        itemDropBlacklist = toBlockIDSet(properties.getString("item-drop-blacklist", ""));
        fireNoSpreadBlocks = toBlockIDSet(properties.getString("disallowed-fire-spread-blocks", ""));
        allowedLavaSpreadOver = toBlockIDSet(properties.getString("allowed-lava-spread-blocks", ""));
        classicWater = properties.getBoolean("classic-water", false);
        simulateSponge = properties.getBoolean("simulate-sponge", true);
        spongeRadius = Math.max(1, properties.getInt("sponge-radius", 3)) - 1;
        itemDurability = properties.getBoolean("item-durability", false);
        noPhysicsGravel = properties.getBoolean("no-physics-gravel", false);
        noPhysicsSand = properties.getBoolean("no-physics-sand", false);
        allowPortalAnywhere = properties.getBoolean("allow-portal-anywhere", false);
        disableFallDamage = properties.getBoolean("disable-fall-damage", false);
        disableLavaDamage = properties.getBoolean("disable-lava-damage", false);
        disableFireDamage = properties.getBoolean("disable-fire-damage", false);
        disableWaterDamage = properties.getBoolean("disable-water-damage", false);
        loginProtection = properties.getInt("login-protection", 3);
        spawnProtection = properties.getInt("spawn-protection", 0);
        kickOnDeath = properties.getBoolean("kick-on-death", false);

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
        }

        // Print an overview of settings
        if (properties.getBoolean("summary-on-start", true)) {
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
    
    /**
     * Called during the early login process to check whether or not to kick the
     * player
     *
     * @param user
     * @return kick reason. null if you don't want to kick the player.
     */
    @Override
    public String onLoginChecks(String user) {
        if (enforceOneSession) {
            for (Player player : etc.getServer().getPlayerList()) {
                if (player.getName().equalsIgnoreCase(user)) {
                    player.kick("Logged in from another location.");
                }
            }
        }

        return null;
    }

    /**
     * Called during the later login process
     *
     * @param player
     */
    @Override
    public void onLogin(Player player) {
        if (stopFireSpread) {
            player.sendMessage(Colors.Yellow + "Fire spread is currently globally disabled.");
        }

        if (loginProtection > 0 || spawnProtection > 0 || kickOnDeath) {
            recentLogins.put(player.getName(), System.currentTimeMillis());
        }

        if (player.isInGroup("wg-invincible")) {
            invinciblePlayers.add(player.getName());
        }

        if (player.isInGroup("wg-amphibious")) {
            amphibiousPlayers.add(player.getName());
        }
    }

    /**
     * Called before the command is parsed. Return true if you don't want the
     * command to be parsed.
     *
     * @param player
     * @param split
     * @return false if you want the command to be parsed.
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        if (split[0].equalsIgnoreCase("/stopfire") &&
                player.canUseCommand("/stopfire")) {
            if (!stopFireSpread) {
                etc.getServer().messageAll(Colors.Yellow
                        + "Fire spread has been globally disabled by " + player.getName() + ".");
            } else {
                player.sendMessage(Colors.Yellow + "Fire spread was already globally disabled.");
            }
            stopFireSpread = true;
            return true;
        } else if (split[0].equalsIgnoreCase("/allowfire")
                    && player.canUseCommand("/stopfire")) {
            if (stopFireSpread) {
                etc.getServer().messageAll(Colors.Yellow
                        + "Fire spread has been globally re-enabled by " + player.getName() + ".");
            } else {
                player.sendMessage(Colors.Yellow + "Fire spread was already globally enabled.");
            }
            stopFireSpread = false;
            return true;
        } else if (split[0].equalsIgnoreCase("/god")
                    && player.canUseCommand("/god")) {
            // Allow setting other people invincible
            if (split.length > 1) {
                if (!player.canUseCommand("/godother")) {
                    player.sendMessage(Colors.Rose + "You don't have permission to make others invincible.");
                    return true;
                }

                Player other = etc.getServer().matchPlayer(split[1]);
                if (other == null) {
                    player.sendMessage(Colors.Rose + "Player not found.");
                } else {
                    if (!invinciblePlayers.contains(other.getName())) {
                        invinciblePlayers.add(other.getName());
                        player.sendMessage(Colors.Yellow + other.getName() + " is now invincible!");
                        other.sendMessage(Colors.Yellow + player.getName() + " has made you invincible!");
                    } else {
                        invinciblePlayers.remove(other.getName());
                        player.sendMessage(Colors.Yellow + other.getName() + " is no longer invincible.");
                        other.sendMessage(Colors.Yellow + player.getName() + " has taken away your invincibility.");
                    }
                }
            // Invincibility for one's self
            } else {
                if (!invinciblePlayers.contains(player.getName())) {
                    invinciblePlayers.add(player.getName());
                    player.sendMessage(Colors.Yellow + "You are now invincible!");
                } else {
                    invinciblePlayers.remove(player.getName());
                    player.sendMessage(Colors.Yellow + "You are no longer invincible.");
                }
            }
            return true;
        } else if ((split[0].equalsIgnoreCase("/stack")
                || split[0].equalsIgnoreCase("/;"))
                    && player.canUseCommand("/stack")) {
            hl[] items = player.getInventory().getArray();
            int len = items.length;

            int affected = 0;
            
            for (int i = 0; i < len; i++) {
                hl item = items[i];

                // Avoid infinite stacks and stacks with durability
                if (item == null || item.a <= 0 || item.d > 0) {
                    continue;
                }

                // Ignore buckets
                if (item.c >= 325 && item.c <= 327) {
                    continue;
                }

                if (item.a < 64) {
                    int needed = 64 - item.a; // Number of needed items until 64

                    // Find another stack of the same type
                    for (int j = i + 1; j < len; j++) {
                        hl item2 = items[j];

                        // Avoid infinite stacks and stacks with durability
                        if (item2 == null || item2.a <= 0 || item2.d > 0) {
                            continue;
                        }

                        // Same type?
                        if (item2.c == item.c) {
                            // This stack won't fit in the parent stack
                            if (item2.a > needed) {
                                item.a = 64;
                                item2.a -= needed;
                                break;
                            // This stack will
                            } else {
                                items[j] = null;
                                item.a += item2.a;
                                needed = 64 - item.a;
                            }

                            affected++;
                        }
                    }
                }
            }

            if (affected > 0) {
                player.getInventory().updateInventory();
            }

            player.sendMessage(Colors.Yellow + "Items compacted into stacks!");
            
            return true;
        } else if (split[0].equalsIgnoreCase("/reload")
                && player.canUseCommand("/reload")
                && split.length > 1) {
            if (split[1].equalsIgnoreCase("WorldGuard")) {
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
        }

        return false;
    }

    /**
     * Called before the console command is parsed. Return true if you don't
     * want the server command to be parsed by the server.
     *
     * @param split
     * @return false if you want the command to be parsed.
     */
    @Override
    public boolean onConsoleCommand(String[] split) {
        if (split[0].equalsIgnoreCase("fire-stop")) {
            if (!stopFireSpread) {
                etc.getServer().messageAll(Colors.Yellow
                        + "Fire spread has been globally disabled by server console.");
                logger.log(Level.INFO, "Fire spread is now globally disabled.");
            } else {
                logger.log(Level.INFO, "Fire spread was already globally disabled.");
            }
            stopFireSpread = true;
            return true;
        } else if (split[0].equalsIgnoreCase("fire-allow")) {
            if (stopFireSpread) {
                etc.getServer().messageAll(Colors.Yellow
                        + "Fire spread has been globally re-enabled by server console.");
                logger.log(Level.INFO, "Fire spread is now globally enabled.");
            } else {
                logger.log(Level.INFO, "Fire spread was already globally enabled.");
            }
            stopFireSpread = false;
            return true;
        }

        return false;
    }
    
    /**
     * Called when a player drops an item.
     * 
     * @param player
     *            player who dropped the item
     * @param item
     *            item that was dropped
     * @return true if you don't want the dropped item to be spawned in the
     *         world
     */
    @Override
    public boolean onItemDrop(Player player, Item item) {
        if (itemDropBlacklist != null) {
            int n = item.getItemId();
            if (itemDropBlacklist.contains(n)) {
                player.sendMessage(Colors.Rose + "Item was destroyed!");
                return true;
            }
        }
        
        if (blacklist != null) {
            if (!blacklist.onDrop(item.getItemId(), player)) {
                return true;
            }
        }

        /*if (!itemDurability) {
            item.setDamage(0);
        }*/

        return false;
    }

    /**
     * Called when a player picks up an item.
     *
     * @param player
     *            player who picked up the item
     * @param item
     *            item that was picked up
     * @return true if you want to leave the item where it was
     */
    @Override
    public boolean onItemPickUp(Player player, Item item) {
        if (blacklist != null && blacklist.hasOnAcquire()) {
            if (!blacklist.onSilentAcquire(item.getItemId(), player)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Called when a player's inventory is modified.
     *
     * @param player
     *            player who's inventory was modified
     * @return true if you want any changes to be reverted
     */
    @Override
    public boolean onInventoryChange(Player player) {
        if (blacklist != null && blacklist.hasOnAcquire()) {
            hl[] items = player.getInventory().getArray();
            boolean needUpdate = false;

            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    if (!blacklist.onAcquire(items[i].c, player)) {
                        items[i] = null;
                        needUpdate = true;
                    }
                }
            }

            if (needUpdate) {
                player.getInventory().updateInventory();
            }
        }
        
        return false;
    }

    /**
     * Called when a player uses an item (rightclick with item in hand)
     * @param player the player
     * @param item the item being used (in hand)
     * @return true to prevent using the item.
     */
    @Override
    public boolean onItemUse(Player player, Item item) {
        if (blacklist != null) {
            if (!blacklist.onCreate(item.getItemId(), player)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Called when someone presses right click. If they right clicked with a
     * block you can return true to cancel that. You can intercept this to add
     * your own right click actions to different item types (see itemInHand)
     *
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked,
            int itemInHand) {
        if (blacklist != null) {
            if (!blacklist.onCreate(itemInHand, player)) {
                return true;
            }

            if (!blacklist.onUse(blockClicked, player)) {
                return true;
            }
        }

        if (simulateSponge && blockPlaced.getType() == 19) {
            int ox = blockPlaced.getX();
            int oy = blockPlaced.getY();
            int oz = blockPlaced.getZ();

            Server server = etc.getServer();

            for (int cx = -spongeRadius; cx <= spongeRadius; cx++) {
                for (int cy = -spongeRadius; cy <= spongeRadius; cy++) {
                    for (int cz = -spongeRadius; cz <= spongeRadius; cz++) {
                        int id = server.getBlockIdAt(ox + cx, oy + cy, oz + cz);
                        if (id == 8 || id == 9) {
                            server.setBlockAt(0, ox + cx, oy + cy, oz + cz);
                        }
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Called when a person left clicks a block.
     *
     * @param player
     * @param block
     * @return
     */
    @Override
    public boolean onBlockDestroy(Player player, Block block) {
        int type = block.getType();
        
        if (blacklist != null) {
            if (!blacklist.onDestroyWith(player.getItemInHand(), player)) {
                return true;
            }

            if (!blacklist.onDestroy(block, player)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Called when a person actually breaks the block.
     *
     * @param player
     * @param block
     * @return
     */
    @Override
    public boolean onBlockBreak(Player player, Block block) {
        if (blacklist != null) {
            if (!blacklist.onBreak(block, player)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Called when either a sign, chest or furnace is changed.
     *
     * @param player
     *            player who changed it
     * @param complexBlock
     *            complex block that changed
     * @return true if you want any changes to be reverted
     */
    @Override
    public boolean onComplexBlockChange(Player player, ComplexBlock complexBlock) {
        if (blacklist != null) {
            if (complexBlock instanceof Chest) {
                Block block = new Block(54, complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());

                if (!blacklist.onSilentUse(block, player)) {
                    return true;
                }
            } else if (complexBlock instanceof Furnace) {
                int id = etc.getServer().getBlockIdAt(complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());
                Block block = new Block(id, complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());

                if (!blacklist.onSilentUse(block, player)) {
                    return true;
                }
            } else if (complexBlock instanceof Sign) {
                int id = etc.getServer().getBlockIdAt(complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());
                Block block = new Block(id, complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());

                if (!blacklist.onSilentUse(block, player)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Called when either a sign, chest or furnace is sent to a player
     *
     * @param player
     *            player who the block is being sent to
     * @param complexBlock
     *            complex block that's being sent
     * @return true if you want the chest, furnace or sign to be empty
     */
    @Override
    public boolean onSendComplexBlock(Player player, ComplexBlock complexBlock) {
        if (blacklist != null) {
            if (complexBlock instanceof Chest) {
                Block block = new Block(54, complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());

                if (!blacklist.onSilentUse(block, player)) {
                    return true;
                }
            } else if (complexBlock instanceof Furnace) {
                int id = etc.getServer().getBlockIdAt(complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());
                Block block = new Block(id, complexBlock.getX(),
                        complexBlock.getY(), complexBlock.getZ());

                if (!blacklist.onSilentUse(block, player)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /*
     * Called when either a lava block or a lighter tryes to light something on fire.
     * block status depends on the light source:
     * 1 = lava.
     * 2 = lighter (flint + steel).
     * 3 = spread (dynamic spreading of fire).
     * @param block
     *          block that the fire wants to spawn in.
     *
     * @return true if you dont want the fire to ignite.
     */
    @Override
    public boolean onIgnite(Block block, Player player) {
        if (preventLavaFire && block.getStatus() == 1) {
            return true;
        }
        
        if (blockLighter && block.getStatus() == 2) {
            return !player.canUseCommand("/uselighter")
                    && !player.canUseCommand("/lighter");
        }

        if (stopFireSpread && block.getStatus() == 3) {
            return true;
        }

        if (disableAllFire && block.getStatus() == 3) {
            return true;
        }

        if (fireNoSpreadBlocks != null && block.getStatus() == 3) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            if (fireNoSpreadBlocks.contains(etc.getServer().getBlockIdAt(x, y - 1, z))
                    || fireNoSpreadBlocks.contains(etc.getServer().getBlockIdAt(x + 1, y, z))
                    || fireNoSpreadBlocks.contains(etc.getServer().getBlockIdAt(x - 1, y, z))
                    || fireNoSpreadBlocks.contains(etc.getServer().getBlockIdAt(x, y, z - 1))
                    || fireNoSpreadBlocks.contains(etc.getServer().getBlockIdAt(x, y, z + 1))) {
                return true;
            }
        }

        return false;
    }

    /*
     * Called when a dynamite block or a creeper is triggerd.
     * block status depends on explosive compound:
     * 1 = dynamite.
     * 2 = creeper.
     * @param block
     *          dynamite block/creeper location block.
     *
     * @return true if you dont the block to explode.
     */
    @Override
    public boolean onExplode(Block block) {
        if (blockCreepers && block.getStatus() == 2) {
            return true;
        }

        if (blockTNT && block.getStatus() == 1) {
            return true;
        }

        return false;
    }
    
    /*
     * Called when fluid wants to flow to a certain block.
     * (10 & 11 for lava and 8 & 9 for water)
     *
     * @param blockFrom
     *              the block where the fluid came from.
     *              (blocktype = fluid type)
     * @param blockTo
     *              the block where fluid wants to flow to.
     *
     *
     * @return true if you dont want the substance to flow.
     */
    @Override
    public boolean onFlow(Block blockFrom, Block blockTo) {
        int x = blockFrom.getX();
        int y = blockFrom.getY();
        int z = blockFrom.getZ();

        boolean isWater = blockFrom.getType() == 8 || blockFrom.getType() == 9;
        boolean isLava = blockFrom.getType() == 10 || blockFrom.getType() == 11;

        if (simulateSponge && isWater) {
            int ox = blockTo.getX();
            int oy = blockTo.getY();
            int oz = blockTo.getZ();

            Server server = etc.getServer();

            for (int cx = -spongeRadius; cx <= spongeRadius; cx++) {
                for (int cy = -spongeRadius; cy <= spongeRadius; cy++) {
                    for (int cz = -spongeRadius; cz <= spongeRadius; cz++) {
                        if (server.getBlockIdAt(ox + cx, oy + cy, oz + cz) == 19) {
                            return true;
                        }
                    }
                }
            }
        }

        if (classicWater && isWater) {
            int blockBelow = etc.getServer().getBlockIdAt(blockFrom.getX(), blockFrom.getY() - 1, blockFrom.getZ());
            if (blockBelow != 0 && blockBelow != 8 && blockBelow != 9) {
                etc.getServer().setBlockAt(9, blockFrom.getX(), blockFrom.getY(), blockFrom.getZ());
                return false;
            }
        }

        if (allowedLavaSpreadOver != null && isLava) {
            int targetId = etc.getServer().getBlockIdAt(
                    blockTo.getX(), blockTo.getY() - 1, blockTo.getZ());
            if (!allowedLavaSpreadOver.contains(targetId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Called when the game is checking the physics for a certain block.
     * This method is called frequently whenever a nearby block is changed,
     * or if the block has just been placed.
     * Currently the only supported blocks are sand, gravel and portals.
     *
     * @param block Block which requires special physics
     * @param boolean true if this block has just been placed
     * @return true if you do want to stop the default physics for this block
     */
    public boolean onBlockPhysics(Block block, boolean placed) {
        int id = block.getType();

        if (id == 13 && noPhysicsGravel) {
            return true;
        }

        if (id == 12 && noPhysicsSand) {
            return true;
        }

        if (id == 90 && allowPortalAnywhere) {
            return true;
        }
        
    	return false;
    }

    /**
     * Called when a players health changes.
     * @param player
     *              the player which health is changed.
     * @param oldValue
     *              old lives value
     * @param newValue
     *              new lives value
     * @return
     *      return true to stop the change.
     */
    @Override
    public boolean onHealthChange(Player player, int oldValue, int newValue) {
        String playerName = player.getName();

        if (invinciblePlayers.contains(playerName)) {
            return oldValue > newValue;
        }

        if (loginProtection > 0 || spawnProtection > 0 || kickOnDeath) {
            long now = System.currentTimeMillis();
            boolean recentLogin = false;

            if (recentLogins.containsKey(playerName)) {
                long time = recentLogins.get(playerName);
                long elapsed = now - time;

                if (loginProtection > 0 && elapsed <= loginProtection * 1000
                        && newValue < oldValue) {
                    return true;
                }

                recentLogin = elapsed <= 2000;

                if (elapsed > 2000 && elapsed > loginProtection * 1000) {
                    recentLogins.remove(playerName);
                }
            }

            if (kickOnDeath && oldValue == -1 && newValue == 20 && !recentLogin) {
                player.kick("You died! Rejoin please.");
                return false;
            }

            if (spawnProtection > 0) {
                if (oldValue == -1 && newValue == 20 && !recentLogin) { // Player was just respawned
                    lastSpawn.put(player.getName(), now);
                } else if (lastSpawn.containsKey(playerName)) {
                    long time = lastSpawn.get(playerName);
                    long elapsed = now - time;

                    if (elapsed < spawnProtection * 1000) {
                        return newValue < oldValue;
                    } else {
                        lastSpawn.remove(playerName);
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Called when a living object is attacked.
     * tip:
     * Use isMob() and isPlayer() and getPlayer().
     *
     * @param type
     *          type of damage dealt.
     * @param attacker
     *          object that is attacking.
     * @param defender
     *          object that is defending.
     * @param amount
     *          amount of damage dealt.
     *
     * @return
     */
    @Override
    public boolean onDamage(PluginLoader.DamageType type, BaseEntity attacker,
            BaseEntity defender, int amount) {
        
        if (defender.isPlayer()) {
            Player player = defender.getPlayer();

            if (invinciblePlayers.contains(player.getName())) {
                return true;
            }
            
            if (disableFallDamage && type == PluginLoader.DamageType.FALL) {
                return true;
            }

            if (disableLavaDamage && type == PluginLoader.DamageType.LAVA) {
                return true;
            }

            if (disableFireDamage && (type == PluginLoader.DamageType.FIRE
                    || type == PluginLoader.DamageType.FIRE_TICK)) {
                return true;
            }

            if (disableWaterDamage && type == PluginLoader.DamageType.WATER) {
                return true;
            }

            if (type == PluginLoader.DamageType.WATER
                    && amphibiousPlayers.contains(player.getName())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Called when water or lava tries to populate a block, you can prevent
     * crushing of torches, railways, flowers etc. You can alternatively allow
     * to let normally solid blocks be crushed.
     *
     * @param currentState the current tristate, once it's set to a non DEFAULT_ACTION it is final.
     * @param liquidBlock the type of the attacking block
     * @param targetBlock the block to be destroyed
     * @return final after a non DEFAULT_ACTION
     */
    @Override
    public PluginLoader.HookResult onLiquidDestroy(PluginLoader.HookResult currentState,
            int liquidBlockId, Block targetBlock) {
        if (preventWaterDamage != null && liquidBlockId <= 9) {
            if (preventWaterDamage.contains(targetBlock.getType())) {
                return PluginLoader.HookResult.PREVENT_ACTION;
            }
        }

        return PluginLoader.HookResult.DEFAULT_ACTION;
    }

    /**
     * Called on player disconnect
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        BlacklistEntry.forgetPlayer(player);
        invinciblePlayers.remove(player.getName());
        amphibiousPlayers.remove(player.getName());
        recentLogins.remove(player.getName());
    }

    /**
     * Call to disable the plugin.
     */
    public void disable() {
        if (blacklist != null) {
            blacklist.getLogger().close();
        }
    }
}