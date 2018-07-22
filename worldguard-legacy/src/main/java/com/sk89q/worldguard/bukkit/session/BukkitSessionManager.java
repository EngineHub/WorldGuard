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

package com.sk89q.worldguard.bukkit.session;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.WorldPlayerTuple;
import com.sk89q.worldguard.session.handler.*;
import com.sk89q.worldguard.session.handler.Handler.Factory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps tracks of sessions and also does session-related handling
 * (flags, etc.).
 */
public class BukkitSessionManager implements SessionManager, Runnable, Listener {

    public static final int RUN_DELAY = 20;
    public static final long SESSION_LIFETIME = 10;

    private final WorldGuardPlugin plugin;

    private final LoadingCache<WorldPlayerTuple, Boolean> bypassCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(2, TimeUnit.SECONDS)
            .build(new CacheLoader<WorldPlayerTuple, Boolean>() {
                @Override
                public Boolean load(WorldPlayerTuple tuple) throws Exception {
                    return tuple.getPlayer().hasPermission("worldguard.region.bypass." + tuple.getWorld().getName());
                }
            });

    private final LoadingCache<CacheKey, Session> sessions = CacheBuilder.newBuilder()
            .expireAfterAccess(SESSION_LIFETIME, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheKey, Session>() {
                @Override
                public Session load(CacheKey key) throws Exception {
                    return createSession(key.playerRef.get());
                }
            });

    /**
     * Create a new instance.
     *
     * @param plugin The plugin
     */
    public BukkitSessionManager(WorldGuardPlugin plugin) {
        checkNotNull(plugin, "plugin");
        this.plugin = plugin;
        handlers.addAll(defaultHandlers);
    }

    /**
     * Get the plugin.
     *
     * @return The plugin
     */
    public WorldGuardPlugin getPlugin() {
        return plugin;
    }

    /**
     * Check whether a player has the region bypass permission.
     *
     * <p>The return value may be cached for a few seconds.</p>
     *
     * @param player The player
     * @param world The world
     * @return A value
     */
    @Override
    public boolean hasBypass(LocalPlayer player, World world) {
        return bypassCache.getUnchecked(new WorldPlayerTuple(world, player));
    }

    /**
     * Re-initialize handlers and clear "last position," "last state," etc.
     * information for all players.
     */
    @Override
    public void resetAllStates() {
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
        for (Player player : players) {
            BukkitPlayer bukkitPlayer = new BukkitPlayer(WorldGuardPlugin.inst(), player);
            Session session = sessions.getIfPresent(new CacheKey(bukkitPlayer));
            if (session != null) {
                session.resetState(bukkitPlayer);
            }
        }
    }

    /**
     * Re-initialize handlers and clear "last position," "last state," etc.
     * information.
     *
     * @param player The player
     */
    @Override
    public void resetState(LocalPlayer player) {
        checkNotNull(player, "player");
        @Nullable Session session = sessions.getIfPresent(new CacheKey(player));
        if (session != null) {
            session.resetState(player);
        }
    }

    private LinkedList<Factory<? extends Handler>> handlers = new LinkedList<>();

    private static final Set<Factory<? extends Handler>> defaultHandlers = new HashSet<>();
    static {
        Factory<?>[] factories = {
            HealFlag.FACTORY,
            FeedFlag.FACTORY,
            NotifyEntryFlag.FACTORY,
            NotifyExitFlag.FACTORY,
            EntryFlag.FACTORY,
            ExitFlag.FACTORY,
            FarewellFlag.FACTORY,
            GreetingFlag.FACTORY,
            GameModeFlag.FACTORY,
            InvincibilityFlag.FACTORY,
            TimeLockFlag.FACTORY,
            WeatherLockFlag.FACTORY,
            GodMode.FACTORY,
            WaterBreathing.FACTORY
        };
        defaultHandlers.addAll(Arrays.asList(factories));
    }

    /**
     * Register a handler with the BukkitSessionManager.
     *
     * You may specify another handler class to ensure your handler is always registered after that class.
     * If that class is not already registered, this method will return false.
     *
     * For example, flags that always act on a player in a region (like {@link HealFlag} and {@link FeedFlag})
     * should be registered earlier, whereas flags that only take effect when a player leaves the region (like
     * {@link FarewellFlag} and {@link GreetingFlag}) should be registered after the {@link ExitFlag.Factory}.class handler factory.
     *
     * @param factory a factory which takes a session and returns an instance of your handler
     * @param after the handler factory to insert the first handler after, to ensure a specific order when creating new sessions
     *
     * @return {@code true} (as specified by {@link Collection#add})
     *          {@code false} if {@param after} is not registered, or {@param factory} is null
     */
    @Override
    public boolean registerHandler(Factory<? extends Handler> factory, @Nullable Factory<? extends Handler> after) {
        if (factory == null) return false;
        WorldGuardPlugin.inst().getLogger().log(Level.INFO, "Registering session handler "
                + factory.getClass().getEnclosingClass().getName());
        if (after == null) {
            handlers.add(factory);
        } else {
            int index = handlers.indexOf(after);
            if (index == -1) return false;

            handlers.add(index, factory); // shifts "after" right one, and everything after "after" right one
        }
        return true;
    }

    /**
     * Unregister a handler.
     *
     * This will prevent it from being added to newly created sessions only. Existing
     * sessions with the handler will continue to use it.
     *
     * Will return false if the handler was not registered to begin with.
     *
     * @param factory the handler factory to unregister
     * @return true if the handler was registered and is now unregistered, false otherwise
     */
    @Override
    public boolean unregisterHandler(Factory<? extends Handler> factory) {
        if (defaultHandlers.contains(factory)) {
            WorldGuardPlugin.inst().getLogger().log(Level.WARNING, "Someone is unregistering a default WorldGuard handler: "
                    + factory.getClass().getEnclosingClass().getName() + ". This may cause parts of WorldGuard to stop functioning");
        } else {
            WorldGuardPlugin.inst().getLogger().log(Level.INFO, "Unregistering session handler "
                    + factory.getClass().getEnclosingClass().getName());
        }
        return handlers.remove(factory);
    }

    /**
     * Create a session for a player.
     *
     * @param player The player
     * @return The new session
     */
    @Override
    public Session createSession(LocalPlayer player) {
        Session session = new Session(this);
        for (Factory<? extends Handler> factory : handlers) {
            session.register(factory.create(session));
        }
        session.initialize(player);
        return session;
    }

    /**
     * Get a player's session, if one exists.
     *
     * @param player The player
     * @return The session
     */
    @Override
    @Nullable
    public Session getIfPresent(LocalPlayer player) {
        return sessions.getIfPresent(player);
    }

    /**
     * Get a player's session. A session will be created if there is no
     * existing session for the player.
     *
     * <p>This method can only be called from the main thread. While the
     * session manager itself is thread-safe, some of the handlers may
     * require initialization that requires the server main thread.</p>
     *
     * @param player The player to get a session for
     * @return The {@code player}'s session
     */
    @Override
    public Session get(LocalPlayer player) {
        return sessions.getUnchecked(new CacheKey(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Pre-load a session
        get(new BukkitPlayer(WorldGuardPlugin.inst(), event.getPlayer()));
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            get(new BukkitPlayer(WorldGuardPlugin.inst(), player)).tick(new BukkitPlayer(WorldGuardPlugin.inst(), player));
        }
    }

    private static final class CacheKey {
        private final WeakReference<LocalPlayer> playerRef;
        private final UUID uuid;

        private CacheKey(LocalPlayer player) {
            playerRef = new WeakReference<>(player);
            uuid = player.getUniqueId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return uuid.equals(cacheKey.uuid);

        }

        @Override
        public int hashCode() {
            return uuid.hashCode();
        }
    }

}
