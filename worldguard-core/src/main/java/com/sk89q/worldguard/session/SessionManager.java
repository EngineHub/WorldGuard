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

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.session.handler.Handler;

import java.util.Collection;

import javax.annotation.Nullable;

public interface SessionManager {

    /**
     * Check whether a player has the region bypass permission.
     *
     * <p>The return value may be cached for a few seconds.</p>
     *
     * @param player The player
     * @param world The world
     * @return A value
     */
    boolean hasBypass(LocalPlayer player, World world);

    /**
     * Re-initialize handlers and clear "last position," "last state," etc.
     * information for all players.
     */
    void resetAllStates();

    /**
     * Re-initialize handlers and clear "last position," "last state," etc.
     * information.
     *
     * @param player The player
     */
    void resetState(LocalPlayer player);

    /**
     * @return true if custom handlers are or were at some point registered, false otherwise
     */
    boolean customHandlersRegistered();

    /**
     * Register a handler with the BukkitSessionManager.
     *
     * You may specify another handler class to ensure your handler is always registered after that class.
     * If that class is not already registered, this method will return false.
     *
     * For example, flags that always act on a player in a region (like HealFlag and FeedFlag)
     * should be registered earlier, whereas flags that only take effect when a player leaves the region (like
     * FarewellFlag and GreetingFlag) should be registered after the ExitFlag.Factory.class handler factory.
     *
     * @param factory a factory which takes a session and returns an instance of your handler
     * @param after the handler factory to insert the first handler after, to ensure a specific order when creating new sessions
     *
     * @return {@code true} (as specified by {@link Collection#add})
     *          {@code false} if after is not registered, or factory is null
     */
    boolean registerHandler(Handler.Factory<? extends Handler> factory, @Nullable Handler.Factory<? extends Handler> after);

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
    boolean unregisterHandler(Handler.Factory<? extends Handler> factory);

    /**
     * Create a session for a player.
     *
     * @param player The player
     * @return The new session
     * @deprecated Use {@link SessionManager#get} instead
     */
    @Deprecated
    Session createSession(LocalPlayer player);

    /**
     * Get a player's session, if one exists.
     *
     * @param player The player
     * @return The session
     */
    @Nullable Session getIfPresent(LocalPlayer player);

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
    Session get(LocalPlayer player);
}
