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

package com.sk89q.worldguard.commands;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;

import javax.annotation.Nullable;
import java.util.Map;

public class CommandInputContext {
    protected final Actor sender;
    protected final String input;

    protected Map<String, Object> context;

    protected CommandInputContext(Actor sender, String input, Map<String, Object> values) {
        this.sender = sender;
        this.input = input;
        this.context = values;
    }

    public void put(String name, Object value) {
        context.put(name, value);
    }

    public Actor getSender() {
        return sender;
    }

    public String getUserInput() {
        return input;
    }

    /**
     * Gets the CommandSender as a player.
     *
     * @return Player
     * @throws InvalidFlagFormat if the sender is not a player
     */
    public LocalPlayer getPlayerSender() throws InvalidFlagFormat {
        if (sender.isPlayer() && sender instanceof LocalPlayer) {
            return (LocalPlayer) sender;
        } else {
            throw new InvalidFlagFormat("Not a player");
        }
    }

    public Integer getUserInputAsInt() throws InvalidFlagFormat {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Not a number: " + input);
        }
    }

    public Double getUserInputAsDouble() throws InvalidFlagFormat {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Not a number: " + input);
        }
    }

    /**
     * Get an object from the context by key name.
     * May return null if the object does not exist in the context.
     *
     * @param name key name of the object
     * @return the object matching the key, or null
     */
    @Nullable
    public Object get(String name) {
        return get(name, null);
    }

    /**
     * Get an object from the context by key name.
     * Will only return null if
     *  a) you provide null as the default
     *  b) the key has explicity been set to null
     *
     * @param name key name of the object
     * @return the object matching the key
     */
    @Nullable
    public Object get(String name, Object defaultValue) {
        Object obj;
        return (((obj = context.get(name)) != null) || context.containsKey(name)
                ? obj : defaultValue);
    }
}
