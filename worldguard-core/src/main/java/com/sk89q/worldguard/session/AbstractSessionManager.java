package com.sk89q.worldguard.session;

import static com.google.common.base.Preconditions.checkNotNull;

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

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractSessionManager implements SessionManager {

    public static final int RUN_DELAY = 20;
    public static final long SESSION_LIFETIME = 10;

    private final LoadingCache<WorldPlayerTuple, Boolean> bypassCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(2, TimeUnit.SECONDS)
            .build(new CacheLoader<WorldPlayerTuple, Boolean>() {
                @Override
                public Boolean load(@Nonnull WorldPlayerTuple tuple) throws Exception {
                    return tuple.getPlayer().hasPermission("worldguard.region.bypass." + tuple.getWorld().getName());
                }
            });

    private final LoadingCache<CacheKey, Session> sessions = CacheBuilder.newBuilder()
            .expireAfterAccess(SESSION_LIFETIME, TimeUnit.MINUTES)
            .build(new CacheLoader<CacheKey, Session>() {
                @Override
                public Session load(@Nonnull CacheKey key) throws Exception {
                    return createSession(key.playerRef.get());
                }
            });

    private LinkedList<Handler.Factory<? extends Handler>> handlers = new LinkedList<>();

    private static final Set<Handler.Factory<? extends Handler>> defaultHandlers = new HashSet<>();
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

    public AbstractSessionManager() {
        handlers.addAll(defaultHandlers);
    }


    @Override
    public boolean registerHandler(Handler.Factory<? extends Handler> factory, @Nullable Handler.Factory<? extends Handler> after) {
        if (factory == null) return false;
        WorldGuard.logger.log(Level.INFO, "Registering session handler "
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
        return sessions.getIfPresent(player);
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
