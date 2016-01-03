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

package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.LocalPlayer;

import java.util.UUID;

/**
 * A domain contains a list of memberships.
 */
public interface Domain {

    /**
     * Returns true if a domain contains a player.
     *
     * @param player the player to check
     * @return whether this domain contains {@code player}
     */
    boolean contains(LocalPlayer player);

    /**
     * Returns true if a domain contains a player.
     *
     * <p>This method doesn't check for groups!</p>
     *
     * @param uniqueId the UUID of the user
     * @return whether this domain contains a player by that name
     */
    boolean contains(UUID uniqueId);

    /**
     * Returns true if a domain contains a player.
     *
     * <p>This method doesn't check for groups!</p>
     *
     * @param playerName The name of the player to check
     * @return whether this domain contains a player by that name
     * @deprecated names are deprecated in MC 1.7+ in favor of UUIDs
     */
    @Deprecated
    boolean contains(String playerName);

    /**
     * Get the number of entries.
     *
     * @return the number of entries
     */
    int size();

    /**
     * Remove all entries.
     */
    void clear();

}
