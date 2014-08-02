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

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores players (only) in a domain.
 */
public class PlayerDomain implements Domain {

    private final Set<UUID> uniqueIds = new CopyOnWriteArraySet<UUID>();
    private final Set<String> names = new CopyOnWriteArraySet<String>();

    /**
     * Create a new instance.
     */
    public PlayerDomain() {
    }

    /**
     * Create a new instance with the given names.
     *
     * @param names an array of names
     * @deprecated names are deprecated in favor of UUIDs in MC 1.7+
     */
    @Deprecated
    public PlayerDomain(String[] names) {
        for (String name : names) {
            addPlayer(name);
        }
    }

    /**
     * Add the given player to the domain, identified by the player's name.
     *
     * @param name the name of the player
     * @deprecated names are deprecated in favor of UUIDs in MC 1.7+
     */
    @Deprecated
    public void addPlayer(String name) {
        checkNotNull(name);
        names.add(name.toLowerCase());
    }

    /**
     * Add the given player to the domain, identified by the player's UUID.
     *
     * @param uniqueId the UUID of the player
     */
    public void addPlayer(UUID uniqueId) {
        checkNotNull(uniqueId);
        uniqueIds.add(uniqueId);
    }

    /**
     * Add the given player to the domain, identified by the player's UUID.
     *
     * @param player the player
     */
    public void addPlayer(LocalPlayer player) {
        checkNotNull(player);
        addPlayer(player.getUniqueId());
    }

    /**
     * Remove the given player from the domain, identified by the player's name.
     *
     * @param name the name of the player
     * @deprecated names are deprecated in favor of UUIDs in MC 1.7+
     */
    @Deprecated
    public void removePlayer(String name) {
        checkNotNull(name);
        names.remove(name.toLowerCase());
    }

    /**
     * Remove the given player from the domain, identified by either the
     * player's name, the player's unique ID, or both.
     *
     * @param player the player
     */
    public void removePlayer(LocalPlayer player) {
        checkNotNull(player);
        names.remove(player.getName().toLowerCase());
        uniqueIds.remove(player.getUniqueId());
    }

    @Override
    public boolean contains(LocalPlayer player) {
        checkNotNull(player);
        return contains(player.getName()) || contains(player.getUniqueId());
    }

    /**
     * Get the set of player names.
     *
     * @return the set of player names
     * @deprecated names are deprecated in favor of UUIDs in MC 1.7+
     */
    @Deprecated
    public Set<String> getPlayers() {
        return names;
    }

    /**
     * Get the set of player UUIDs.
     *
     * @return the set of player UUIDs
     */
    public Set<UUID> getUniqueIds() {
        return uniqueIds;
    }

    @Override
    public boolean contains(UUID uniqueId) {
        checkNotNull(uniqueId);
        return uniqueIds.contains(uniqueId);
    }

    @Override
    public boolean contains(String playerName) {
        checkNotNull(playerName);
        return names.contains(playerName.toLowerCase());
    }

    @Override
    public int size() {
        return names.size() + uniqueIds.size();
    }

    @Override
    public void clear() {
        uniqueIds.clear();
        names.clear();
    }

}
