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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.squirrelid.cache.HashMapCache;
import com.sk89q.squirrelid.cache.ProfileCache;
import com.sk89q.squirrelid.cache.SQLiteCache;
import com.sk89q.squirrelid.resolver.*;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.commands.GeneralCommands;
import com.sk89q.worldguard.bukkit.commands.ProtectionCommands;
import com.sk89q.worldguard.bukkit.commands.ToggleCommands;
import com.sk89q.worldguard.bukkit.event.player.ProcessPlayerEvent;
import com.sk89q.worldguard.bukkit.listener.*;
import com.sk89q.worldguard.bukkit.util.Events;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.UnresolvedNamesException;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.util.concurrent.EvenMoreExecutors;
import com.sk89q.worldguard.util.logging.ClassSourceValidator;
import com.sk89q.worldguard.util.logging.RecordMessagePrefixer;
import com.sk89q.worldguard.util.task.SimpleSupervisor;
import com.sk89q.worldguard.util.task.Supervisor;
import com.sk89q.worldguard.util.task.Task;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * The main class for WorldGuard as a Bukkit plugin.
 */
public class WorldGuardPlugin extends JavaPlugin {

    private static final Logger log = Logger.getLogger(WorldGuardPlugin.class.getCanonicalName());

    private static WorldGuardPlugin inst;
    private final CommandsManager<CommandSender> commands;
    private final ConfigurationManager configuration = new ConfigurationManager(this);
    private final RegionContainer regionContainer = new RegionContainer(this);
    private final GlobalRegionManager globalRegionManager = new GlobalRegionManager(this, regionContainer);
    private SessionManager sessionManager;
    private final Supervisor supervisor = new SimpleSupervisor();
    private ListeningExecutorService executorService;
    private ProfileService profileService;
    private ProfileCache profileCache;
    private PlayerMoveListener playerMoveListener;

    /**
     * Construct objects. Actual loading occurs when the plugin is enabled, so
     * this merely instantiates the objects.
     */
    public WorldGuardPlugin() {
        final WorldGuardPlugin plugin = inst = this;
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return plugin.hasPermission(player, perm);
            }
        };
    }

    /**
     * Get the current instance of WorldGuard
     * @return WorldGuardPlugin instance
     */
    public static WorldGuardPlugin inst() {
        return inst;
    }

    /**
     * Called on plugin enable.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onEnable() {
        configureLogger();

        getDataFolder().mkdirs(); // Need to create the plugins/WorldGuard folder

        executorService = MoreExecutors.listeningDecorator(EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 20));

        sessionManager = new SessionManager(this);

        // Set the proper command injector
        commands.setInjector(new SimpleInjector(this));

        // Catch bad things being done by naughty plugins that include
        // WorldGuard's classes
        ClassSourceValidator verifier = new ClassSourceValidator(this);
        verifier.reportMismatches(ImmutableList.of(WGBukkit.class, ProtectedRegion.class, ProtectedCuboidRegion.class, Flag.class));

        // Register command classes
        final CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);
        reg.register(ToggleCommands.class);
        reg.register(ProtectionCommands.class);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                if (!getGlobalStateManager().hasCommandBookGodMode()) {
                    reg.register(GeneralCommands.class);
                }
            }
        }, 0L);

        File cacheDir = new File(getDataFolder(), "cache");
        cacheDir.mkdirs();
        try {
            profileCache = new SQLiteCache(new File(cacheDir, "profiles.sqlite"));
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to initialize SQLite profile cache");
            profileCache = new HashMapCache();
        }

        profileService = new CacheForwardingService(
                new CombinedProfileService(
                        BukkitPlayerService.getInstance(),
                        HttpRepositoryService.forMinecraft()),
                profileCache);

        PermissionsResolverManager.initialize(this);
        configuration.load();

        log.info("Loading region data...");
        regionContainer.initialize();

        getServer().getScheduler().scheduleSyncRepeatingTask(this, sessionManager, SessionManager.RUN_DELAY, SessionManager.RUN_DELAY);

        // Register events
        getServer().getPluginManager().registerEvents(sessionManager, this);
        (new WorldGuardPlayerListener(this)).registerEvents();
        (new WorldGuardBlockListener(this)).registerEvents();
        (new WorldGuardEntityListener(this)).registerEvents();
        (new WorldGuardWeatherListener(this)).registerEvents();
        (new WorldGuardVehicleListener(this)).registerEvents();
        (new WorldGuardServerListener(this)).registerEvents();
        (new WorldGuardHangingListener(this)).registerEvents();

        // Modules
        (playerMoveListener = new PlayerMoveListener(this)).registerEvents();
        (new BlacklistListener(this)).registerEvents();
        (new ChestProtectionListener(this)).registerEvents();
        (new RegionProtectionListener(this)).registerEvents();
        (new RegionFlagsListener(this)).registerEvents();
        (new WorldRulesListener(this)).registerEvents();
        (new BlockedPotionsListener(this)).registerEvents();
        (new EventAbstractionListener(this)).registerEvents();
        (new PlayerModesListener(this)).registerEvents();
        (new BuildPermissionListener(this)).registerEvents();
        (new InvincibilityListener(this)).registerEvents();
        if ("true".equalsIgnoreCase(System.getProperty("worldguard.debug.listener"))) {
            (new DebuggingListener(this, log)).registerEvents();
        }

        configuration.updateCommandBookGodMode();

        if (getServer().getPluginManager().isPluginEnabled("CommandBook")) {
            getServer().getPluginManager().registerEvents(new WorldGuardCommandBookListener(this), this);
        }

        // handle worlds separately to initialize already loaded worlds
        WorldGuardWorldListener worldListener = (new WorldGuardWorldListener(this));
        for (World world : getServer().getWorlds()) {
            worldListener.initWorld(world);
        }
        worldListener.registerEvents();

        for (Player player : BukkitUtil.getOnlinePlayers()) {
            ProcessPlayerEvent event = new ProcessPlayerEvent(player);
            Events.fire(event);
        }
    }

    @Override
    public void onDisable() {
        executorService.shutdown();

        try {
            log.log(Level.INFO, "Shutting down executor and waiting for any pending tasks...");

            List<Task<?>> tasks = supervisor.getTasks();
            if (!tasks.isEmpty()) {
                StringBuilder builder = new StringBuilder("Known tasks:");
                for (Task<?> task : tasks) {
                    builder.append("\n");
                    builder.append(task.getName());
                }
                log.log(Level.INFO, builder.toString());
            }

            Futures.successfulAsList(tasks).get();
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.log(Level.WARNING, "Some tasks failed while waiting for remaining tasks to finish", e);
        }

        regionContainer.unload();
        configuration.unload();
        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            sender.sendMessage(ChatColor.RED + convertThrowable(e.getCause()));
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    /**
     * Convert the throwable into a somewhat friendly message.
     *
     * @param throwable the throwable
     * @return a message
     */
    public String convertThrowable(@Nullable Throwable throwable) {
        if (throwable instanceof NumberFormatException) {
            return "Number expected, string received instead.";
        } else if (throwable instanceof StorageException) {
            log.log(Level.WARNING, "Error loading/saving regions", throwable);
            return "Region data could not be loaded/saved: " + throwable.getMessage();
        } else if (throwable instanceof RejectedExecutionException) {
            return "There are currently too many tasks queued to add yours. Use /wg running to list queued and running tasks.";
        } else if (throwable instanceof CancellationException) {
            return "WorldGuard: Task was cancelled";
        } else if (throwable instanceof InterruptedException) {
            return "WorldGuard: Task was interrupted";
        } else if (throwable instanceof UnresolvedNamesException) {
            return throwable.getMessage();
        } else if (throwable instanceof CommandException) {
            return throwable.getMessage();
        } else {
            log.log(Level.WARNING, "WorldGuard encountered an unexpected error", throwable);
            return "WorldGuard: An unexpected error occurred! Please see the server console.";
        }
    }

    /**
     * Get the GlobalRegionManager.
     *
     * @return the plugin's global region manager
     * @deprecated use {@link #getRegionContainer()}
     */
    @Deprecated
    public GlobalRegionManager getGlobalRegionManager() {
        return globalRegionManager;
    }

    /**
     * Get the object that manages region data.
     *
     * @return the region container
     */
    public RegionContainer getRegionContainer() {
        return regionContainer;
    }

    /**
     * Get the WorldGuard Configuration.
     *
     * @return ConfigurationManager
     * @deprecated Use {@link #getGlobalStateManager()} instead
     */
    @Deprecated
    public ConfigurationManager getGlobalConfiguration() {
        return getGlobalStateManager();
    }

    /**
     * Gets the flag state manager.
     *
     * @return The flag state manager
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Get the global ConfigurationManager.
     * USe this to access global configuration values and per-world configuration values.
     * @return The global ConfigurationManager
     */
    public ConfigurationManager getGlobalStateManager() {
        return configuration;
    }

    /**
     * Get the supervisor.
     *
     * @return the supervisor
     */
    public Supervisor getSupervisor() {
        return supervisor;
    }

    /**
     * Get the global executor service for internal usage (please use your
     * own executor service).
     *
     * @return the global executor service
     */
    public ListeningExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get the profile lookup service.
     *
     * @return the profile lookup service
     */
    public ProfileService getProfileService() {
        return profileService;
    }

    /**
     * Get the profile cache.
     *
     * @return the profile cache
     */
    public ProfileCache getProfileCache() {
        return profileCache;
    }

    /**
     * Check whether a player is in a group.
     * This calls the corresponding method in PermissionsResolverManager
     *
     * @param player The player to check
     * @param group The group
     * @return whether {@code player} is in {@code group}
     */
    public boolean inGroup(Player player, String group) {
        try {
            return PermissionsResolverManager.getInstance().inGroup(player, group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     * Get the groups of a player.
     * This calls the corresponding method in PermissionsResolverManager.
     * @param player The player to check
     * @return The names of each group the playe is in.
     */
    public String[] getGroups(Player player) {
        try {
            return PermissionsResolverManager.getInstance().getGroups(player);
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }

    /**
     * Gets the name of a command sender. This is a unique name and this
     * method should never return a "display name".
     *
     * @param sender The sender to get the name of
     * @return The unique name of the sender.
     */
    public String toUniqueName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return "*Console*";
        } else {
            return sender.getName();
        }
    }

    /**
     * Gets the name of a command sender. This play be a display name.
     *
     * @param sender The CommandSender to get the name of.
     * @return The name of the given sender
     */
    public String toName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            return "*Console*";
        } else if (sender instanceof Player) {
            return ((Player) sender).getDisplayName();
        } else {
            return sender.getName();
        }
    }

    /**
     * Checks permissions.
     *
     * @param sender The sender to check the permission on.
     * @param perm The permission to check the permission on.
     * @return whether {@code sender} has {@code perm}
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
            Player player = (Player) sender;
            return PermissionsResolverManager.getInstance().hasPermission(player.getWorld().getName(), player, perm);
        }

        return false;
    }

    /**
     * Checks permissions and throws an exception if permission is not met.
     *
     * @param sender The sender to check the permission on.
     * @param perm The permission to check the permission on.
     * @throws CommandPermissionsException if {@code sender} doesn't have {@code perm}
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
     * @param sender The {@link CommandSender} to check
     * @return {@code sender} casted to a player
     * @throws CommandException if {@code sender} isn't a {@link Player}
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
     * The filter string uses the following format:
     * @[name] looks up all players with the exact {@code name}
     * *[name] matches any player whose name contains {@code name}
     * [name] matches any player whose name starts with {@code name}
     *
     * @param filter The filter string to check.
     * @return A {@link List} of players who match {@code filter}
     */
    public List<Player> matchPlayerNames(String filter) {
        Collection<? extends Player> players = BukkitUtil.getOnlinePlayers();

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
     * @param players The {@link List} to check
     * @return {@code players} as an {@link Iterable}
     * @throws CommandException If {@code players} is empty
     */
    protected Iterable<? extends Player> checkPlayerMatch(List<? extends Player> players)
            throws CommandException {
        // Check to see if there were any matches
        if (players.size() == 0) {
            throw new CommandException("No players matched query.");
        }

        return players;
    }

    /**
     * Matches players based on the specified filter string
     *
     * The filter string format is as follows:
     * * returns all the players currently online
     * If {@code sender} is a {@link Player}:
     * #world returns all players in the world that {@code sender} is in
     * #near reaturns all players within 30 blocks of {@code sender}'s location
     * Otherwise, the format is as specified in {@link #matchPlayerNames(String)}
     *
     * @param source The CommandSender who is trying to find a player
     * @param filter The filter string for players
     * @return iterator for players
     * @throws CommandException if no matches are found
     */
    public Iterable<? extends Player> matchPlayers(CommandSender source, String filter)
            throws CommandException {

        if (BukkitUtil.getOnlinePlayers().isEmpty()) {
            throw new CommandException("No players matched query.");
        }

        if (filter.equals("*")) {
            return checkPlayerMatch(Lists.newArrayList(BukkitUtil.getOnlinePlayers()));
        }

        // Handle special hash tag groups
        if (filter.charAt(0) == '#') {
            // Handle #world, which matches player of the same world as the
            // calling source
            if (filter.equalsIgnoreCase("#world")) {
                List<Player> players = new ArrayList<Player>();
                Player sourcePlayer = checkPlayer(source);
                World sourceWorld = sourcePlayer.getWorld();

                for (Player player : BukkitUtil.getOnlinePlayers()) {
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

                for (Player player : BukkitUtil.getOnlinePlayers()) {
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
     * @param sender The {@link CommandSender} who is requesting a player match
     * @param filter The filter string.
     * @see #matchPlayers(org.bukkit.entity.Player) for filter string syntax
     * @return The single player
     * @throws CommandException If more than one player match was found
     */
    public Player matchSinglePlayer(CommandSender sender, String filter)
            throws CommandException {
        // This will throw an exception if there are no matches
        Iterator<? extends Player> players = matchPlayers(sender, filter).iterator();

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
     * The filter string syntax is as follows:
     * #console, *console, or ! return the server console
     * All syntax from {@link #matchSinglePlayer(org.bukkit.command.CommandSender, String)}
     * @param sender The sender trying to match a CommandSender
     * @param filter The filter string
     * @return The resulting CommandSender
     * @throws CommandException if either zero or more than one player matched.
     */
    public CommandSender matchPlayerOrConsole(CommandSender sender, String filter)
            throws CommandException {

        // Let's see if console is wanted
        if (filter.equalsIgnoreCase("#console")
                || filter.equalsIgnoreCase("*console*")
                || filter.equalsIgnoreCase("!")) {
            return getServer().getConsoleSender();
        }

        return matchSinglePlayer(sender, filter);
    }

    /**
     * Get a single player as an iterator for players.
     *
     * @param player The player to return in an Iterable
     * @return iterator for player
     */
    public Iterable<Player> matchPlayers(Player player) {
        return Arrays.asList(player);
    }

    /**
     * Match a world.
     *
     * The filter string syntax is as follows:
     * #main returns the main world
     * #normal returns the first world with a normal environment
     * #nether return the first world with a nether environment
     * #player:[name] returns the world that a player named {@code name} is located in, if the player is online.
     * [name] A world with the name {@code name}
     *
     * @param sender The sender requesting a match
     * @param filter The filter string
     * @return The resulting world
     * @throws CommandException if no world matches
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
     * @return The WorldEditPlugin instance
     * @throws CommandException If there is no WorldEditPlugin available
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
     * @param player The player to wrap
     * @return The wrapped player
     */
    public LocalPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, player);
    }

    /**
     * Wrap a player as a LocalPlayer.
     *
     * @param player The player to wrap
     * @param silenced True to silence messages
     * @return The wrapped player
     */
    public LocalPlayer wrapPlayer(Player player, boolean silenced) {
        return new BukkitPlayer(this, player, silenced);
    }

    /**
     * Wrap a player as a LocalPlayer.
     *
     * <p>This implementation is incomplete -- permissions cannot be checked.</p>
     *
     * @param player The player to wrap
     * @return The wrapped player
     */
    public LocalPlayer wrapOfflinePlayer(OfflinePlayer player) {
        return new BukkitOfflinePlayer(player);
    }

    /**
     * Return a protection query helper object that can be used by another
     * plugin to test whether WorldGuard permits an action at a particular
     * place.
     *
     * @return an instance
     */
    public ProtectionQuery createProtectionQuery() {
        return new ProtectionQuery();
    }

    /**
     * Configure WorldGuard's loggers.
     */
    private void configureLogger() {
        RecordMessagePrefixer.register(Logger.getLogger("com.sk89q.worldguard"), "[WorldGuard] ");
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param actual The destination file
     * @param defaultName The name of the file inside the jar's defaults folder
     */
    public void createDefaultConfiguration(File actual,
            String defaultName) {

        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists()) {
            return;
        }

        InputStream input =
                    null;
            try {
                JarFile file = new JarFile(getFile());
                ZipEntry copy = file.getEntry("defaults/" + defaultName);
                if (copy == null) throw new FileNotFoundException();
                input = file.getInputStream(copy);
            } catch (IOException e) {
                log.severe("Unable to read default configuration: " + defaultName);
            }

        if (input != null) {
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(actual);
                byte[] buf = new byte[8192];
                int length = 0;
                while ((length = input.read(buf)) > 0) {
                    output.write(buf, 0, length);
                }

                log.info("Default configuration file written: "
                        + actual.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ignore) {
                }

                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Notifies all with the worldguard.notify permission.
     * This will check both superperms and WEPIF,
     * but makes sure WEPIF checks don't result in duplicate notifications
     *
     * @param msg The notification to broadcast
     */
    public void broadcastNotification(String msg) {
        getServer().broadcast(msg, "worldguard.notify");
        Set<Permissible> subs = getServer().getPluginManager().getPermissionSubscriptions("worldguard.notify");
        for (Player player : BukkitUtil.getOnlinePlayers()) {
            if (!(subs.contains(player) && player.hasPermission("worldguard.notify")) &&
                    hasPermission(player, "worldguard.notify")) { // Make sure the player wasn't already broadcasted to.
                player.sendMessage(msg);
            }
        }
        log.info(msg);
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     *
     * @param player The player to check.
     * @param loc The location to check at.
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.Location)
     * @return whether {@code player} can build at {@code loc}
     */
    public boolean canBuild(Player player, Location loc) {
        return getGlobalRegionManager().canBuild(player, loc);
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     *
     * @param player The player to check
     * @param block The block to check at.
     * @see GlobalRegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.block.Block)
     * @return whether {@code player} can build at {@code block}'s location
     */
    public boolean canBuild(Player player, Block block) {
        return getGlobalRegionManager().canBuild(player, block);
    }

    /**
     * Gets the region manager for a world.
     *
     * @param world world to get the region manager for
     * @return the region manager or null if regions are not enabled
     */
    public RegionManager getRegionManager(World world) {
        if (!getGlobalStateManager().get(world).useRegions) {
            return null;
        }

        return getGlobalRegionManager().get(world);
    }

    public PlayerMoveListener getPlayerMoveListener() {
        return playerMoveListener;
    }

    /**
     * Replace macros in the text.
     *
     * The macros replaced are as follows:
     * %name%: The name of {@code sender}. See {@link #toName(org.bukkit.command.CommandSender)}
     * %id%: The unique name of the sender. See {@link #toUniqueName(org.bukkit.command.CommandSender)}
     * %online%: The number of players currently online on the server
     * If {@code sender} is a Player:
     * %world%: The name of the world {@code sender} is located in
     * %health%: The health of {@code sender}. See {@link org.bukkit.entity.Player#getHealth()}
     *
     * @param sender The sender to check
     * @param message The message to replace macros in
     * @return The message with macros replaced
     */
    public String replaceMacros(CommandSender sender, String message) {
        Collection<? extends Player> online = BukkitUtil.getOnlinePlayers();

        message = message.replace("%name%", toName(sender));
        message = message.replace("%id%", toUniqueName(sender));
        message = message.replace("%online%", String.valueOf(online.size()));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            World world = player.getWorld();

            message = message.replace("%world%", world.getName());
            message = message.replace("%health%", String.valueOf(player.getHealth()));
        }

        return message;
    }

}
