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

package com.sk89q.worldguard.internal.platform;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;

import java.util.Iterator;
import java.util.List;

public interface StringMatcher {

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
    World matchWorld(Actor sender, String filter) throws CommandException;

    /**
     * Match player names.
     *
     * The filter string uses the following format:
     * \@[name] looks up all players with the exact {@code name}
     * *[name] matches any player whose name contains {@code name}
     * [name] matches any player whose name starts with {@code name}
     *
     * @param filter The filter string to check.
     * @return A {@link List} of players who match {@code filter}
     */
    List<LocalPlayer> matchPlayerNames(String filter);

    /**
     * Checks if the given list of players is greater than size 0, otherwise
     * throw an exception.
     *
     * @param players The {@link List} to check
     * @return {@code players} as an {@link Iterable}
     * @throws CommandException If {@code players} is empty
     */
    default Iterable<? extends LocalPlayer> checkPlayerMatch(List<? extends LocalPlayer> players) throws CommandException {
        // Check to see if there were any matches
        if (players.isEmpty()) {
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
    Iterable<? extends LocalPlayer> matchPlayers(Actor source, String filter) throws CommandException;

    /**
     * Match only a single player.
     *
     * @param sender The {@link Actor} who is requesting a player match
     * @param filter The filter string.
     * @see #matchPlayers(LocalPlayer) for filter string syntax
     * @return The single player
     * @throws CommandException If more than one player match was found
     */
    default LocalPlayer matchSinglePlayer(Actor sender, String filter) throws CommandException {
        // This will throw an exception if there are no matches
        Iterator<? extends LocalPlayer> players = matchPlayers(sender, filter).iterator();

        LocalPlayer match = players.next();

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
     * All syntax from {@link #matchSinglePlayer(Actor, String)}
     * @param sender The sender trying to match a CommandSender
     * @param filter The filter string
     * @return The resulting CommandSender
     * @throws CommandException if either zero or more than one player matched.
     */
    Actor matchPlayerOrConsole(Actor sender, String filter) throws CommandException;

    /**
     * Get a single player as an iterator for players.
     *
     * @param player The player to return in an Iterable
     * @return iterator for player
     */
    default Iterable<LocalPlayer> matchPlayers(LocalPlayer player) {
        return Lists.newArrayList(player);
    }

    /**
     * Gets a world by name, if possible.
     *
     * @param worldName The name
     * @return The world
     */
    World getWorldByName(String worldName);

    /**
     * Replace macros in the text.
     *
     * The macros replaced are as follows:
     * %name%: The name of {@code sender}.
     * %id%: The unique name of the sender.
     * %online%: The number of players currently online on the server
     * If {@code sender} is a Player:
     * %world%: The name of the world {@code sender} is located in
     * %health%: The health of {@code sender}.
     *
     * @param sender The sender to check
     * @param message The message to replace macros in
     * @return The message with macros replaced
     */
    String replaceMacros(Actor sender, String message);

}
