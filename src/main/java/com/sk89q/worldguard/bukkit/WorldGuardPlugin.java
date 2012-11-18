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

import static com.sk89q.worldguard.bukkit.BukkitUtil.hasHangingEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.rebar.bukkit.VirtualRebarPlugin;
import com.sk89q.rebar.config.ConfigurationException;
import com.sk89q.rebar.config.YamlConfiguration;
import com.sk89q.rebar.config.YamlConfigurationResource;
import com.sk89q.rulelists.Action;
import com.sk89q.rulelists.Criteria;
import com.sk89q.rulelists.DefaultAttachments;
import com.sk89q.rulelists.DefinitionManager;
import com.sk89q.rulelists.ResolverManager;
import com.sk89q.rulelists.RuleListsManager;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.commands.GeneralCommands;
import com.sk89q.worldguard.bukkit.commands.ProtectionCommands;
import com.sk89q.worldguard.bukkit.commands.ToggleCommands;
import com.sk89q.worldguard.bukkit.definitions.BlockCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.CancelActionLoader;
import com.sk89q.worldguard.bukkit.definitions.DamageCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.EntityCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.FlagCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.ItemCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.PermissionCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.PhenomenonCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.RegionCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.SetBlockActionLoader;
import com.sk89q.worldguard.bukkit.definitions.SetDropActionLoader;
import com.sk89q.worldguard.bukkit.definitions.SpawnCriteriaLoader;
import com.sk89q.worldguard.bukkit.definitions.TellActionLoader;
import com.sk89q.worldguard.bukkit.definitions.UpdateEntityActionLoader;
import com.sk89q.worldguard.bukkit.definitions.UpdateItemActionLoader;
import com.sk89q.worldguard.bukkit.definitions.SetMessageActionLoader;
import com.sk89q.worldguard.bukkit.definitions.UpdateWorldActionLoader;
import com.sk89q.worldguard.bukkit.definitions.WeatherCriteriaLoader;
import com.sk89q.worldguard.bukkit.resolvers.BlockResolver;
import com.sk89q.worldguard.bukkit.resolvers.EntityResolver;
import com.sk89q.worldguard.bukkit.resolvers.ItemStackSlotResolver;
import com.sk89q.worldguard.bukkit.resolvers.PlacedBlockResolver;
import com.sk89q.worldguard.bukkit.resolvers.PlayerItemStackSlotResolver;
import com.sk89q.worldguard.bukkit.resolvers.PlayerItemStackSlotResolver.Slot;
import com.sk89q.worldguard.bukkit.resolvers.SourceBlockResolver;
import com.sk89q.worldguard.bukkit.resolvers.SourceEntityResolver;
import com.sk89q.worldguard.bukkit.resolvers.TargetBlockResolver;
import com.sk89q.worldguard.bukkit.resolvers.TargetEntityResolver;
import com.sk89q.worldguard.region.RegionManager;
import com.sk89q.worldguard.region.indices.RegionIndex;

/**
 * The main class for WorldGuard as a Bukkit plugin.
 */
public class WorldGuardPlugin extends JavaPlugin {

    private final CommandsManager<CommandSender> commands;
    private final RegionManager globalRegionManager;
    private final ConfigurationManager configuration;
    private RuleListsManager ruleListsManager;
    private RegionQueryCache regionCache;
    private final LagStopMode lagStopper;

    private WorldGuardWorldListener worldListener;

    /**
     * Do initial loading of WorldGuard. Only called once. {@link #onEnable()} will be
     * called when WorldGuard is enabled.
     */
    public WorldGuardPlugin() {
        configuration = new ConfigurationManager(this);
        globalRegionManager = new RegionManager(this);
        lagStopper = new LagStopMode(this);

        final WorldGuardPlugin plugin = this;
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender player, String perm) {
                return plugin.hasPermission(player, perm);
            }
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        // We don't require Rebar yet
        VirtualRebarPlugin.setup(this);

        // Set up RuleLists
        ruleListsManager = new RuleListsManager();
        registerRuleList();

        // Setup the cache
        regionCache = new RegionQueryCache(this);

        // Set the proper command injector
        commands.setInjector(new SimpleInjector(this));

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

        // Need to create the plugins/WorldGuard folder
        getDataFolder().mkdirs();

        PermissionsResolverManager.initialize(this);

        // This must be done before configuration is loaded
        LegacyWorldGuardMigration.migrateBlacklist(this);

    	// Load the configuration
    	configuration.load();
    	globalRegionManager.preload();

        // Migrate regions after the regions were loaded because
        // the migration code reuses the loaded region managers
        LegacyWorldGuardMigration.migrateRegions(this);

        // Register events
        (new WorldGuardPlayerListener(this)).registerEvents();
        (new WorldGuardBlockListener(this)).registerEvents();
        (new WorldGuardEntityListener(this)).registerEvents();
        (new WorldGuardWeatherListener(this)).registerEvents();
        (new WorldGuardVehicleListener(this)).registerEvents();
        (new WorldGuardServerListener(this)).registerEvents();
        (worldListener = new WorldGuardWorldListener(this)).registerEvents();
        lagStopper.registerEvents();

        // Initialization
        simulateWorldLoad();

        if (hasHangingEvent()) {
            (new WorldGuardHangingListener(this)).registerEvents();
        } else {
            (new WorldGuardPaintingListener(this)).registerEvents();
        }
        configuration.updateCommandBookGodMode();

        if (getServer().getPluginManager().isPluginEnabled("CommandBook")) {
            getServer().getPluginManager().registerEvents(new WorldGuardCommandBookListener(this), this);
        }
    }

    @Override
    public void onDisable() {
        globalRegionManager.unload();
        configuration.unload();
        this.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
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
     * Dispatch {@link WorldLoadEvent}s to the world listener.
     */
    public void simulateWorldLoad() {
        for (World world : getServer().getWorlds()) {
            WorldLoadEvent event = new WorldLoadEvent(world);
            worldListener.onWorldLoad(event);
        }
    }

    /**
     * Get the GlobalRegionManager.
     *
     * @return The plugin's global region manager
     */
    public RegionManager getGlobalRegionManager() {
        return globalRegionManager;
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
     * Get the global ConfigurationManager.
     * USe this to access global configuration values and per-world configuration values.
     * @return The global ConfigurationManager
     */
    public ConfigurationManager getGlobalStateManager() {
        return configuration;
    }

    /**
     * Get the rules list manager.
     *
     * @return rule lists manager
     */
    public RuleListsManager getRulesListManager() {
        return ruleListsManager;
    }

    /**
     * Get the region query cache.
     *
     * @return region query cache
     */
    public RegionQueryCache getRegionCache() {
        return regionCache;
    }

    /**
     * Get the built-in rules.
     *
     * @return built-in rules
     * @throws ConfigurationException on config error
     * @throws IOException on I/O exception
     */
    public YamlConfiguration getBuiltInRules() throws IOException, ConfigurationException {
        YamlConfiguration rules = new YamlConfigurationResource(getClass(),
                "/defaults/builtin_rules.yml");
        rules.load();
        return rules;
    }

    /**
     * Register RuleList resolvers, criterion, and actions.
     */
    private void registerRuleList() {
        DefaultAttachments.registerWith(ruleListsManager.getAttachments());

        // Subject resolvers
        ResolverManager resolvers = ruleListsManager.getResolvers();

        resolvers.register(BlockResolver.class, "source", new SourceBlockResolver());
        resolvers.register(BlockResolver.class, "target", new TargetBlockResolver());
        resolvers.register(BlockResolver.class, "placed", new PlacedBlockResolver());

        resolvers.register(EntityResolver.class, "source", new SourceEntityResolver());
        resolvers.register(EntityResolver.class, "target", new TargetEntityResolver());

        resolvers.register(ItemStackSlotResolver.class, "held", new PlayerItemStackSlotResolver(Slot.HELD));
        resolvers.register(ItemStackSlotResolver.class, "helmet", new PlayerItemStackSlotResolver(Slot.HELMET));
        resolvers.register(ItemStackSlotResolver.class, "chestplate", new PlayerItemStackSlotResolver(Slot.CHESTPLATE));
        resolvers.register(ItemStackSlotResolver.class, "leggings", new PlayerItemStackSlotResolver(Slot.LEGGINGS));
        resolvers.register(ItemStackSlotResolver.class, "boots", new PlayerItemStackSlotResolver(Slot.BOOTS));

        // Criterion
        DefinitionManager<Criteria<?>> criterion = ruleListsManager.getCriterion();
        criterion.register("match-block", new BlockCriteriaLoader(ruleListsManager));
        criterion.register("match-item", new ItemCriteriaLoader(ruleListsManager));
        criterion.register("match-entity", new EntityCriteriaLoader(ruleListsManager));
        criterion.register("match-damage", new DamageCriteriaLoader());
        criterion.register("match-spawn", new SpawnCriteriaLoader());
        criterion.register("match-phenomenon", new PhenomenonCriteriaLoader());
        criterion.register("match-weather", new WeatherCriteriaLoader());
        criterion.register("region-of", new RegionCriteriaLoader(ruleListsManager));
        criterion.register("has-flag", new FlagCriteriaLoader(this, ruleListsManager));
        criterion.register("has-permission", new PermissionCriteriaLoader(this, ruleListsManager));

        // Actions
        DefinitionManager<Action<?>> actions = ruleListsManager.getActions();
        actions.register("cancel", new CancelActionLoader());
        actions.register("tell", new TellActionLoader(ruleListsManager));
        actions.register("update-item", new UpdateItemActionLoader(ruleListsManager));
        actions.register("update-world", new UpdateWorldActionLoader());
        actions.register("update-entity", new UpdateEntityActionLoader(this, ruleListsManager));
        actions.register("set-message", new SetMessageActionLoader(ruleListsManager));
        actions.register("set-drop", new SetDropActionLoader());
        actions.register("set-block", new SetBlockActionLoader(ruleListsManager));
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
            return PermissionsResolverManager.getInstance().hasPermission(player.getWorld().getName(), player.getName(), perm);
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
     * @param players The {@link List} to check
     * @return {@code players} as an {@link Iterable}
     * @throws CommandException If {@code players} is empty
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
     * @param sender The {@link CommandSender} who is requesting a player match
     * @param filter The filter string.
     * @see #matchPlayers(org.bukkit.entity.Player) for filter string syntax
     * @return The single player
     * @throws CommandException If more than one player match was found
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
     * Create a default configuration file from the .jar.
     *
     * @param actual The destination file
     * @param defaultName The name of the file inside the jar's defaults folder
     */
    public void createDefaultConfiguration(File actual, String defaultName) {
        // Make parent directories
        File parent = actual.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        if (actual.exists()) {
            return;
        }

        InputStream input = WorldGuardPlugin.class.getResourceAsStream("/defaults/" + defaultName);
        if (input == null) {
            getLogger().severe("Unable to read default configuration: " + defaultName);
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

                getLogger().info("Default configuration file written: "
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
        for (Player player : getServer().getOnlinePlayers()) {
            if (!(subs.contains(player) && player.hasPermission("worldguard.notify")) &&
                    hasPermission(player, "worldguard.notify")) { // Make sure the player wasn't already broadcasted to.
                player.sendMessage(msg);
            }
        }
        getLogger().info(msg);
    }

    /**
     * Forgets a player.
     *
     * @param player The player to remove state information for
     */
    public void forgetPlayer(Player player) {
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     *
     * @param player The player to check.
     * @param loc The location to check at.
     * @see RegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.Location)
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
     * @see RegionManager#canBuild(org.bukkit.entity.Player, org.bukkit.block.Block)
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
    public RegionIndex getRegionManager(World world) {
        if (!getGlobalStateManager().get(world).useRegions) {
            return null;
        }

        return getGlobalRegionManager().get(world);
    }

    /**
     * Get the "lag stop mode" controller.
     *
     * @return the lag stop mode
     */
    public LagStopMode getLagStopMode() {
        return lagStopper;
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
        Player[] online = getServer().getOnlinePlayers();

        message = message.replace("%name%", toName(sender));
        message = message.replace("%id%", toUniqueName(sender));
        message = message.replace("%online%", String.valueOf(online.length));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            World world = player.getWorld();

            message = message.replace("%world%", world.getName());
            message = message.replace("%health%", String.valueOf(player.getHealth()));
        }

        return message;
    }
}
