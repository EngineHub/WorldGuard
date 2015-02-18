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

package com.sk89q.worldguard.session;

import com.sk89q.guavabackport.cache.CacheBuilder;
import com.sk89q.guavabackport.cache.CacheLoader;
import com.sk89q.guavabackport.cache.LoadingCache;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.session.handler.*;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps tracks of sessions and also does session-related handling
 * (flags, etc.).
 */
public class SessionManager implements Runnable, Listener {

    public static final int RUN_DELAY = 20;
    public static final long SESSION_LIFETIME = 10;

    private final WorldGuardPlugin plugin;

    private final LoadingCache<WorldPlayerTuple, Boolean> bypassCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(2, TimeUnit.SECONDS)
            .build(new CacheLoader<WorldPlayerTuple, Boolean>() {
                @Override
                public Boolean load(WorldPlayerTuple tuple) throws Exception {
                    return plugin.getGlobalRegionManager().hasBypass(tuple.player, tuple.world);
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
    public SessionManager(WorldGuardPlugin plugin) {
        checkNotNull(plugin, "plugin");
        this.plugin = plugin;
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
    public boolean hasBypass(Player player, World world) {
        return bypassCache.getUnchecked(new WorldPlayerTuple(world, player));
    }

    /**
     * Re-initialize handlers and clear "last position," "last state," etc.
     * information for all players.
     */
    public void resetAllStates() {
        Collection<? extends Player> players = BukkitUtil.getOnlinePlayers();
        for (Player player : players) {
            Session session = sessions.getIfPresent(new CacheKey(player));
            if (session != null) {
                session.resetState(player);
            }
        }
    }

    /**
     * Re-initialize handlers and clear "last position," "last state," etc.
     * information.
     *
     * @param player The player
     */
    public void resetState(Player player) {
        checkNotNull(player, "player");
        @Nullable Session session = sessions.getIfPresent(new CacheKey(player));
        if (session != null) {
            session.resetState(player);
        }
    }

    /**
     * Create a session for a player.
     *
     * @param player The player
     * @return The new session
     */
    private Session createSession(Player player) {
        Session session = new Session(this);
        session.register(new HealFlag(session));
        session.register(new FeedFlag(session));
        session.register(new NotifyEntryFlag(session));
        session.register(new NotifyExitFlag(session));
        session.register(new EntryFlag(session));
        session.register(new ExitFlag(session));
        session.register(new GreetingFlag(session));
        session.register(new FarewellFlag(session));
        session.register(new GameModeFlag(session));
        session.register(new InvincibilityFlag(session));
        session.register(new TimeLockFlag(session));
        session.register(new WeatherLockFlag(session));
        session.register(new GodMode(session));
        session.register(new WaterBreathing(session));
        session.initialize(player);
        return session;
    }

    /**
     * Get a player's session, if one exists.
     *
     * @param player The player
     * @return The session
     */
    @Nullable
    public Session getIfPresent(Player player) {
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
    public Session get(Player player) {
        return sessions.getUnchecked(new CacheKey(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Pre-load a session
        get(event.getPlayer());
    }

    @Override
    public void run() {
        for (Player player : BukkitUtil.getOnlinePlayers()) {
            get(player).tick(player);
        }
    }

    private static final class CacheKey {
        private final WeakReference<Player> playerRef;
        private final UUID uuid;

        private CacheKey(Player player) {
            playerRef = new WeakReference<Player>(player);
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
