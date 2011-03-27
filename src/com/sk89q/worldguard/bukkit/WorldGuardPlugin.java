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

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.TimedFlagsTimer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Plugin for Bukkit.
 * 
 * @author sk89q
 */
public class WorldGuardPlugin extends JavaPlugin {

    /**
     * Logger for messages.
     */
    protected static final Logger logger = Logger.getLogger("Minecraft.WorldGuard");
    
    /**
     * List of commands.
     */
    protected CommandsManager<CommandSender> commands;

    /**
     * Handles the region databases for all worlds.
     */
    protected GlobalRegionManager globalRegionManager;
    
    /**
     * Handles all configuration.
     */
    protected GlobalConfiguration configuration;
    
    /**
     * Processes queries for permissions information.
     */
    protected PermissionsResolverManager perms;

    /**
     * Called on plugin enable.
     */
    public void onEnable() {
        getDataFolder().mkdirs();
        
        configuration = new GlobalConfiguration(this);
        configuration.load();

        globalRegionManager = new GlobalRegionManager();
        
        for (World world : getServer().getWorlds()) {
            globalRegionManager.load(world.getName());
        }

        // Register events
        (new WorldGuardPlayerListener(this)).registerEvents();
        (new WorldGuardBlockListener(this)).registerEvents();
        (new WorldGuardEntityListener(this)).registerEvents();
        (new WorldGuardVehicleListener(this)).registerEvents();

        // 25 equals about 1s real time
        this.getServer().getScheduler().scheduleSyncRepeatingTask(
                this, new TimedFlagsTimer(this), 25 * 5, 25 * 5);

        // Register the commands that we want to use
        final WorldGuardPlugin plugin = this;
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return plugin.hasPermission(player, perm);
            }
        };

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
        globalRegionManager.unload();
        configuration.unload();

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
            return true;
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
