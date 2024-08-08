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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.config.ConfigurationManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.handler.Handler;
import com.sk89q.worldguard.util.Locations;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Keeps session information on a player.
 */
public class Session {

    private final SessionManager manager;
    private boolean disableBypass;
    private final HashMap<Class<?>, Handler> handlers = Maps.newLinkedHashMap();
    private Location lastValid;
    private Set<ProtectedRegion> lastRegionSet;
    private final AtomicBoolean needRefresh = new AtomicBoolean(false);
    private boolean initialized;

    /**
     * Create a new session.
     *
     * @param manager The session manager
     */
    public Session(SessionManager manager) {
        checkNotNull(manager, "manager");
        this.manager = manager;
    }

    /**
     * Register a new handler.
     *
     * @param handler A new handler
     */
    public void register(Handler handler) {
        handlers.put(handler.getClass(), handler);
    }

    /**
     * Get the session manager.
     *
     * @return The session manager
     */
    public SessionManager getManager() {
        return manager;
    }

    /**
     * Get a handler by class, if has been registered.
     *
     * @param type The type of handler
     * @param <T> The type of handler
     * @return A handler instance, otherwise null
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Handler> T getHandler(Class<T> type) {
        return (T) handlers.get(type);
    }

    /**
     * Initialize the session.
     *
     * @param player The player
     */
    public void initialize(LocalPlayer player) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        Location location = player.getLocation();
        ApplicableRegionSet set = query.getApplicableRegions(location);

        lastValid = location;
        lastRegionSet = set.getRegions();
        ConfigurationManager cfg = WorldGuard.getInstance().getPlatform().getGlobalStateManager();
        disableBypass = cfg.disableDefaultBypass;
        if (cfg.announceBypassStatus && player.hasPermission("worldguard.region.toggle-bypass")) {
            player.printInfo(TextComponent.of(
                    "You are " + (disableBypass ? "not " : "") + "bypassing region protection. " +
                    "You can toggle this with /rg bypass", TextColor.DARK_PURPLE));
        }


        for (Handler handler : handlers.values()) {
            handler.initialize(player, location, set);
        }
    }

    synchronized void ensureInitialized(LocalPlayer player, BiConsumer<Session, LocalPlayer> initializer) {
        if (initialized) {
            return;
        }
        initialized = true;
        initializer.accept(this, player);
    }

    /**
     * Uninitialize the session.
     *
     * @param player The player
     */
    public void uninitialize(LocalPlayer player) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        Location location = player.getLocation();
        ApplicableRegionSet set = query.getApplicableRegions(location);

        for (Handler handler : handlers.values()) {
            handler.uninitialize(player, location, set);
        }
    }

    /**
     * Tick the session.
     *
     * @param player The player
     */
    public void tick(LocalPlayer player) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        Location location = player.getLocation();
        ApplicableRegionSet set = query.getApplicableRegions(location);

        for (Handler handler : handlers.values()) {
            handler.tick(player, set);
        }
    }

    /**
     * Re-initialize the session.
     *
     * @param player The player
     */
    public void resetState(LocalPlayer player) {
        initialize(player);
        needRefresh.set(true);
    }

    /**
     * Test whether the session has invincibility enabled.
     *
     * @return Whether invincibility is enabled
     */
    public boolean isInvincible(LocalPlayer player) {
        boolean invincible = false;

        for (Handler handler : handlers.values()) {
            State state = handler.getInvincibility(player);
            if (state != null) {
                switch (state) {
                    case DENY: return false;
                    case ALLOW: invincible = true;
                }
            }
        }

        return invincible;
    }

    /**
     * Test movement to the given location.
     *
     * @param player The player
     * @param to The new location
     * @param moveType The type of move
     * @return The overridden location, if the location is being overridden
     * @see #testMoveTo(LocalPlayer, Location, MoveType, boolean) For an explanation
     */
    @Nullable
    public Location testMoveTo(LocalPlayer player, Location to, MoveType moveType) {
        return testMoveTo(player, to, moveType, false);
    }

    /**
     * Test movement to the given location.
     *
     * <p>If a non-null {@link Location} is returned, the player should be
     * at that location instead of where the player has tried to move to.</p>
     *
     * <p>If the {@code moveType} is cancellable
     * ({@link MoveType#isCancellable()}, then the last valid location will
     * be set to the given one.</p>
     *
     * @param player The player
     * @param to The new location
     * @param moveType The type of move
     * @param forced Whether to force a check
     * @return The overridden location, if the location is being overridden
     */
    @Nullable
    public Location testMoveTo(LocalPlayer player, Location to, MoveType moveType, boolean forced) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();

        if (!forced && needRefresh.getAndSet(false)) {
            forced = true;
        }

        if (forced || Locations.isDifferentBlock(lastValid, to)) {
            ApplicableRegionSet toSet = query.getApplicableRegions(to);

            for (Handler handler : handlers.values()) {
                if (!handler.testMoveTo(player, lastValid, to, toSet, moveType) && moveType.isCancellable()) {
                    return lastValid;
                }
            }

            Set<ProtectedRegion> entered = Sets.difference(toSet.getRegions(), lastRegionSet);
            Set<ProtectedRegion> exited = Sets.difference(lastRegionSet, toSet.getRegions());

            for (Handler handler : handlers.values()) {
                if (!handler.onCrossBoundary(player, lastValid, to, toSet, entered, exited, moveType) && moveType.isCancellable()) {
                    return lastValid;
                }
            }

            lastValid = to;
            lastRegionSet = toSet.getRegions();
        }

        return null;
    }

    /**
     * @return true if the owner of this session should not bypass protection, even if they have bypass permissions
     */
    public boolean hasBypassDisabled() {
        return disableBypass;
    }

    /**
     * Toggle bypass disabling for this session.
     * @param disabled true to disable region bypass
     */
    public void setBypassDisabled(boolean disabled) {
        disableBypass = disabled;
    }
}
