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

package com.sk89q.worldguard.bukkit.commands;

import com.google.common.base.Function;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * Command-related utility methods.
 */
public final class CommandUtils {

    private CommandUtils() {
    }

    /**
     * Get the name of the given owner object.
     *
     * @param owner the owner object
     * @return a name
     */
    public static String getOwnerName(@Nullable Object owner) {
        if (owner == null) {
            return "?";
        } else if (owner instanceof Player) {
            return ((Player) owner).getName();
        } else if (owner instanceof ConsoleCommandSender) {
            return "*CONSOLE*";
        } else if (owner instanceof BlockCommandSender) {
            return ((BlockCommandSender) owner).getBlock().getLocation().toString();
        } else {
            return "?";
        }
    }

    /**
     * Return a function that accepts a string to send a message to the
     * given sender.
     *
     * @param sender the sender
     * @return a function
     */
    public static Function<String, ?> messageFunction(final CommandSender sender) {
        return new Function<String, Object>() {
            @Override
            public Object apply(@Nullable String s) {
                sender.sendMessage(s);
                return null;
            }
        };
    }

}
