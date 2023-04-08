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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.handler.EntryFlag;
import com.sk89q.worldguard.session.handler.ExitFlag;
import com.sk89q.worldguard.session.handler.FarewellFlag;
import com.sk89q.worldguard.session.handler.FeedFlag;
import com.sk89q.worldguard.session.handler.GameModeFlag;
import com.sk89q.worldguard.session.handler.GodMode;
import com.sk89q.worldguard.session.handler.GreetingFlag;
import com.sk89q.worldguard.session.handler.Handler;
import com.sk89q.worldguard.session.handler.HealFlag;
import com.sk89q.worldguard.session.handler.InvincibilityFlag;
import com.sk89q.worldguard.session.handler.NotifyEntryFlag;
import com.sk89q.worldguard.session.handler.NotifyExitFlag;
import com.sk89q.worldguard.session.handler.TimeLockFlag;
import com.sk89q.worldguard.session.handler.WaterBreathing;
import com.sk89q.worldguard.session.handler.WeatherLockFlag;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractSessionManager implements SessionManager {

    public static final int RUN_DELAY = 20;
    public static final long SESSION_LIFETIME = 10;

    private static final BiPredicate<World, LocalPlayer> BYPASS_PERMISSION_TEST = (world, player) -> {
        return player.hasPermission("worldguard.region.bypass." + world.getName());
    };

    private final LoadingCache<WorldPlayerTuple, Boolean> bypassCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build(CacheLoader.from(tuple -> BYPASS_PERMISSION_TEST.test(tuple.getWorld(), tuple.getPlayer())));

    private final LoadingCache<CacheKey, Session> sessions = CacheBuilder.newBuilder()
            .expireAfterAccess(SESSION_LIFETIME, TimeUnit.MINUTES)
            .build(CacheLoader.from(key ->
                    createSession(key.playerRef.get())));

    private boolean hasCustom = false;
    private List<Handler.Factory<? extends Handler>> handlers = new LinkedList<>();

    private static final List<Handler.Factory<? extends Handler>> defaultHandlers = new LinkedList<>();

    static {
        Handler.Factory<?>[] factories = {
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

    protected AbstractSessionManager() {
        handlers.addAll(defaultHandlers);
    }

    @Override
    public boolean customHandlersRegistered() {
        return hasCustom;
    }

    @Override
    public boolean registerHandler(Handler.Factory<? extends Handler> factory, @Nullable Handler.Factory<? extends Handler> after) {
        if (factory == null) return false;
        WorldGuard.logger.log(Level.INFO, "Registering session handler "
                + factory.getClass().getEnclosingClass().getName());
        hasCustom = true;
        if (after == null) {
            handlers.add(factory);
        } else {
            int index = handlers.indexOf(after);
            if (index == -1) return false;

            handlers.add(index + 1, factory); // shifts "after" right one, and everything after "after" right one
        }
        return true;
    }

    @Override
    public boolean unregisterHandler(Handler.Factory<? extends Handler> factory) {
        if (defaultHandlers.contains(factory)) {
            WorldGuard.logger.log(Level.WARNING, "Someone is unregistering a default WorldGuard handler: "
                    + factory.getClass().getEnclosingClass().getName() + ". This may cause parts of WorldGuard to stop functioning");
        } else {
            WorldGuard.logger.log(Level.INFO, "Unregistering session handler "
                    + factory.getClass().getEnclosingClass().getName());
        }
        return handlers.remove(factory);
    }

    @Override
    public boolean hasBypass(LocalPlayer player, World world) {
        Session sess = getIfPresent(player);
        if (sess == null || sess.hasBypassDisabled()) {
            return false;
        }

        if (WorldGuard.getInstance().getPlatform().getGlobalStateManager().disablePermissionCache) {
            return BYPASS_PERMISSION_TEST.test(world, player);
        }

        return bypassCache.getUnchecked(new WorldPlayerTuple(world, player));
    }

    @Override
    public void resetState(LocalPlayer player) {
        checkNotNull(player, "player");
        @Nullable Session session = sessions.getIfPresent(new CacheKey(player));
        if (session != null) {
            session.resetState(player);
        }
    }

    @Override
    @Nullable
    public Session getIfPresent(LocalPlayer player) {
        return sessions.getIfPresent(new CacheKey(player));
    }

    @Override
    public Session get(LocalPlayer player) {
        return sessions.getUnchecked(new CacheKey(player));
    }

    @Override
    public Session createSession(LocalPlayer player) {
        Session session = new Session(this);
        for (Handler.Factory<? extends Handler> factory : handlers) {
            session.register(factory.create(session));
        }
        session.initialize(player);
        return session;
    }

    protected static final class CacheKey {
        final WeakReference<LocalPlayer> playerRef;
        final UUID uuid;

        CacheKey(LocalPlayer player) {
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
