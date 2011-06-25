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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.bukkit.migration.*;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.TickSyncDelayLoggerFilter;
import com.sk89q.worldguard.bukkit.commands.GeneralCommands;
import com.sk89q.worldguard.bukkit.commands.ProtectionCommands;
import com.sk89q.worldguard.bukkit.commands.ToggleCommands;
import com.sk89q.worldguard.protection.*;
import java.io.*;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Logger;

/**
 * The main class for WorldGuard as a Bukkit plugin.
 * 
 * @author sk89q
 */
public class WorldGuardPlugin extends JavaPlugin {
    /**
     * Logger for messages.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    /**
     * Manager for commands. This automatically handles nested commands,
     * permissions checking, and a number of other fancy command things.
     * We just set it up and register commands against it.
     */
    
    private final CommandsManager<CommandSender> commands;
    
    /**
     * Handles the region databases for all worlds.
     */
    private final GlobalRegionManager globalRegionManager;
    
    /**
     * Handles all configuration.
     */
    private final ConfigurationManager configuration;
    
    /**
     * Processes queries for permissions information. The permissions manager
     * is from WorldEdit and it automatically handles looking up permissions
     * systems and picking the right one. WorldGuard just needs to call
     * the permission methods.
     */
    private PermissionsResolverManager perms;
    
    /**
     * Used for scheduling flags.
     */
    private FlagStateManager flagStateManager;

    /**
     * Construct objects. Actual loading occurs when the plugin is enabled, so
     * this merely instantiates the objects.
     */
    public WorldGuardPlugin() {
        configuration = new ConfigurationManager(this);
        globalRegionManager = new GlobalRegionManager(this);
        
        final WorldGuardPlugin plugin = this;
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return plugin.hasPermission(player, perm);
            }
        };
        
        // Register command classes
        commands.register(ToggleCommands.class);
        commands.register(ProtectionCommands.class);
        commands.register(GeneralCommands.class);
    }
    
    /**
     * Called on plugin enable.
     */
    public void onEnable() {
        // Need to create the plugins/WorldGuard folder
        getDataFolder().mkdirs();

        // Set up permissions
        perms = new PermissionsResolverManager(
                getConfiguration(), getServer(), "WorldGuard", logger);
        perms.load();

        // This must be done before configuration is loaded
        LegacyWorldGuardMigration.migrateBlacklist(this);
        
        // Load the configuration
        configuration.load();
        globalRegionManager.preload();
        
        // Migrate regions after the regions were loaded because
        // the migration code reuses the loaded region managers
        LegacyWorldGuardMigration.migrateRegions(this);

        // Load permissions
        (new PermissionsResolverServerListener(perms)).register(this);

        flagStateManager = new FlagStateManager(this);
        
        if (configuration.useRegionsScheduler) { 
            getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                    flagStateManager, FlagStateManager.RUN_DELAY, FlagStateManager.RUN_DELAY);
        }

        if (configuration.suppressTickSyncWarnings) {
            Logger.getLogger("Minecraft").setFilter(
                    new TickSyncDelayLoggerFilter());
        } else {
            Filter filter = Logger.getLogger("Minecraft").getFilter();
            if (filter != null && filter instanceof TickSyncDelayLoggerFilter) {
                Logger.getLogger("Minecraft").setFilter(null);
            }
        }

        // Register events
        (new WorldGuardPlayerListener(this)).registerEvents();
        (new WorldGuardBlockListener(this)).registerEvents();
        (new WorldGuardEntityListener(this)).registerEvents();
        (new WorldGuardWeatherListener(this)).registerEvents();
        (new WorldGuardWorldListener(this)).registerEvents();
        (new WorldGuardPluginListener(this)).registerEvents();
        
        logger.info("WorldGuard " + this.getDescription().getVersion() + " enabled.");
    }

    /**
     * Called on plugin disable.
     */
    public void onDisable() {
        globalRegionManager.unload();
        configuration.unload();

        logger.info("WorldGuard " + getDescription().getVersion() + " disabled.");
    }
    
    /**
     * Handle a command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        try {
            commands.execute(cmd.getName(), args, sender, this, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        
        return true;
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
     * @deprecated Use {@link #getGlobalStateManager()} instead
     */
    @Deprecated
    public ConfigurationManager getGlobalConfiguration() {
        return getGlobalStateManager();
    }
    
    /**
     * Gets the flag state manager.
     * 
     * @return
     */
    public FlagStateManager getFlagStateManager() {
        return flagStateManager;
    }

    /**
     * Get the WorldGuardConfiguraton.
     *
     * @return
     */
    public ConfigurationManager getGlobalStateManager() {
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
     * Gets the name of a command sender. This is a unique name and this
     * method should never return a "display name".
     * 
     * @param sender
     * @return
     */
    public String toUniqueName(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getName();
        } else {
            return "*Console*";
        }
    }
    
    /**
     * Gets the name of a command sender. This play be a display name.
     * 
     * @param sender
     * @return
     */
    public String toName(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getName();
        } else {
            return "*Console*";
        }
    }
    
    /**
     * Checks permissions.
     * 
     * @param sender
     * @param perm
     * @return 
     */
    public boolean hasPermission(CommandSender sender, String perm) {
        if (sender.isOp()) {
            if (sender instanceof Player) {
                if (this.getGlobalStateManager().get(((Player) sender).
                        getWorld()).opPermissions) {
                    return true;
                }
            } else {
                return true;
            }
        }
        
        // Invoke the permissions resolver
        if (sender instanceof Player) {
            return perms.hasPermission(((Player) sender).getName(), perm);
        }
        
        return false;
    }
    
    /**
     * Checks permissions and throws an exception if permission is not met.
     * 
     * @param sender
     * @param perm
     * @throws CommandPermissionsException 
     */
    public void checkPermission(CommandSender sender, String perm)
            throws CommandPermissionsException {
        if (!hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }
    
    /**
     * Checks to see if the sender is a player, otherwise throw an exception.
     * 
     * @param sender
     * @return 
     * @throws CommandException 
     */
    public Player checkPlayer(CommandSender sender)
            throws CommandException {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandException("A player is expected.");
        }
    }
    
    /**
     * Match player names.
     * 
     * @param filter
     * @return
     */
    public List<Player> matchPlayerNames(String filter) {
        Player[] players = getServer().getOnlinePlayers();

        filter = filter.toLowerCase();
        
        // Allow exact name matching
        if (filter.charAt(0) == '@' && filter.length() >= 2) {
            filter = filter.substring(1);
            
            for (Player player : players) {
                if (player.getName().equalsIgnoreCase(filter)) {
                    List<Player> list = new ArrayList<Player>();
                    list.add(player);
                    return list;
                }
            }
            
            return new ArrayList<Player>();
        // Allow partial name matching
        } else if (filter.charAt(0) == '*' && filter.length() >= 2) {
            filter = filter.substring(1);
            
            List<Player> list = new ArrayList<Player>();
            
            for (Player player : players) {
                if (player.getName().toLowerCase().contains(filter)) {
                    list.add(player);
                }
            }
            
            return list;
        
        // Start with name matching
        } else {
            List<Player> list = new ArrayList<Player>();
            
            for (Player player : players) {
                if (player.getName().toLowerCase().startsWith(filter)) {
                    list.add(player);
                }
            }
            
            return list;
        }
    }
    
    /**
     * Checks if the given list of players is greater than size 0, otherwise
     * throw an exception.
     * 
     * @param players
     * @return 
     * @throws CommandException
     */
    protected Iterable<Player> checkPlayerMatch(List<Player> players)
            throws CommandException {
        // Check to see if there were any matches
        if (players.size() == 0) {
            throw new CommandException("No players matched query.");
        }
        
        return players;
    }
    
    /**
     * Checks permissions and throws an exception if permission is not met.
     * 
     * @param source 
     * @param filter
     * @return iterator for players
     * @throws CommandException no matches found
     */
    public Iterable<Player> matchPlayers(CommandSender source, String filter)
            throws CommandException {
        
        if (getServer().getOnlinePlayers().length == 0) {
            throw new CommandException("No players matched query.");
        }
        
        if (filter.equals("*")) {
            return checkPlayerMatch(Arrays.asList(getServer().getOnlinePlayers()));
        }

        // Handle special hash tag groups
        if (filter.charAt(0) == '#') {
            // Handle #world, which matches player of the same world as the
            // calling source
            if (filter.equalsIgnoreCase("#world")) {
                List<Player> players = new ArrayList<Player>();
                Player sourcePlayer = checkPlayer(source);
                World sourceWorld = sourcePlayer.getWorld();
                
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player.getWorld().equals(sourceWorld)) {
                        players.add(player);
                    }
                }

                return checkPlayerMatch(players);
            
            // Handle #near, which is for nearby players.
            } else if (filter.equalsIgnoreCase("#near")) {
                List<Player> players = new ArrayList<Player>();
                Player sourcePlayer = checkPlayer(source);
                World sourceWorld = sourcePlayer.getWorld();
                org.bukkit.util.Vector sourceVector
                        = sourcePlayer.getLocation().toVector();
                
                for (Player player : getServer().getOnlinePlayers()) {
                    if (player.getWorld().equals(sourceWorld)
                            && player.getLocation().toVector().distanceSquared(
                                    sourceVector) < 900) {
                        players.add(player);
                    }
                }

                return checkPlayerMatch(players);
            
            } else {
                throw new CommandException("Invalid group '" + filter + "'.");
            }
        }
        
        List<Player> players = matchPlayerNames(filter);
        
        return checkPlayerMatch(players);
    }
    
    /**
     * Match only a single player.
     * 
     * @param sender
     * @param filter
     * @return
     * @throws CommandException
     */
    public Player matchSinglePlayer(CommandSender sender, String filter)
            throws CommandException {
        // This will throw an exception if there are no matches
        Iterator<Player> players = matchPlayers(sender, filter).iterator();
        
        Player match = players.next();
        
        // We don't want to match the wrong person, so fail if if multiple
        // players were found (we don't want to just pick off the first one,
        // as that may be the wrong player)
        if (players.hasNext()) {
            throw new CommandException("More than one player found! " +
                        "Use @<name> for exact matching.");
        }
        
        return match;
    }
    
    /**
     * Match only a single player or console.
     * 
     * @param sender
     * @param filter
     * @return
     * @throws CommandException
     */
    public CommandSender matchPlayerOrConsole(CommandSender sender, String filter)
            throws CommandException {
        
        // Let's see if console is wanted
        if (filter.equalsIgnoreCase("#console")
                || filter.equalsIgnoreCase("*console*")
                || filter.equalsIgnoreCase("!")) {
            return new ConsoleCommandSender(getServer());
        }
        
        return matchSinglePlayer(sender, filter);
    }
    
    /**
     * Get a single player as an iterator for players.
     * 
     * @param player
     * @return iterator for players
     */
    public Iterable<Player> matchPlayers(Player player) {
        return Arrays.asList(new Player[] {player});
    }
    
    /**
     * Match a world.
     * @param sender 
     * 
     * @param filter
     * @return
     * @throws CommandException 
     */
    public World matchWorld(CommandSender sender, String filter) throws CommandException {
        List<World> worlds = getServer().getWorlds();

        // Handle special hash tag groups
        if (filter.charAt(0) == '#') {
            // #main for the main world
            if (filter.equalsIgnoreCase("#main")) {
                return worlds.get(0);
            
            // #normal for the first normal world
            } else if (filter.equalsIgnoreCase("#normal")) {
                for (World world : worlds) {
                    if (world.getEnvironment() == Environment.NORMAL) {
                        return world;
                    }
                }

                throw new CommandException("No normal world found.");
            
            // #nether for the first nether world
            } else if (filter.equalsIgnoreCase("#nether")) {
                for (World world : worlds) {
                    if (world.getEnvironment() == Environment.NETHER) {
                        return world;
                    }
                }

                throw new CommandException("No nether world found.");

            // Handle getting a world from a player
            } else if (filter.matches("^#player$")) {
                String parts[] = filter.split(":", 2);
                
                // They didn't specify an argument for the player!
                if (parts.length == 1) {
                    throw new CommandException("Argument expected for #player.");
                }
                
                return matchPlayers(sender, parts[1]).iterator().next().getWorld();
            } else {
                throw new CommandException("Invalid identifier '" + filter + "'.");
            }
        }
        
        for (World world : worlds) {
            if (world.getName().equals(filter)) {
                return world;
            }
        }
        
        throw new CommandException("No world by that exact name found.");
    }

    /**
     * Gets a copy of the WorldEdit plugin.
     * 
     * @return
     * @throws CommandException
     */
    public WorldEditPlugin getWorldEdit() throws CommandException {
        Plugin worldEdit = getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            throw new CommandException("WorldEdit does not appear to be installed.");
        }
        
        if (worldEdit instanceof WorldEditPlugin) {
            return (WorldEditPlugin) worldEdit;
        } else {
            throw new CommandException("WorldEdit detection failed (report error).");
        }
    }
    
    /**
     * Wrap a player as a LocalPlayer.
     * 
     * @param player
     * @return
     */
    public LocalPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, player);
    }
    
    /**
     * Create a default configuration file from the .jar.
     * 
     * @param actual 
     * @param defaultName 
     */
    public static void createDefaultConfiguration(File actual,
            String defaultName) {
        
        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

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
                        + actual.getAbsolutePath());
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
    
    /**
     * Notifies all with the notify permission.
     * 
     * @param msg
     */
    public void broadcastNotification(String msg) {
        for (Player player : getServer().getOnlinePlayers()) {
            if (hasPermission(player, "worldguard.notify")) {
                player.sendMessage(msg);
            }
        }
    }
    
    /**
     * Forgets a player.
     * 
     * @param player
     */
    public void forgetPlayer(Player player) {
        flagStateManager.forget(player);
    }
    
    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     * 
     * @param player
     * @param loc
     * @return
     */
    public boolean canBuild(Player player, Location loc) {
        return getGlobalRegionManager().canBuild(player, loc);
    }
    
    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     * 
     * @param player
     * @param block
     * @return
     */
    public boolean canBuild(Player player, Block block) {
        return getGlobalRegionManager().canBuild(player, block);
    }
}
