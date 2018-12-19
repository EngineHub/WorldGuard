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
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.Style;
import org.bukkit.command.BlockCommandSender;
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
     * Replace color macros in a string.
     *
     * @param str the string
     * @return the new string
     */
    public static String replaceColorMacros(String str) {
        // TODO: Make this more efficient

        str = str.replace("`r", Style.RED.toString());
        str = str.replace("`R", Style.RED_DARK.toString());

        str = str.replace("`y", Style.YELLOW.toString());
        str = str.replace("`Y", Style.YELLOW_DARK.toString());

        str = str.replace("`g", Style.GREEN.toString());
        str = str.replace("`G", Style.GREEN_DARK.toString());

        str = str.replace("`c", Style.CYAN.toString());
        str = str.replace("`C", Style.CYAN_DARK.toString());

        str = str.replace("`b", Style.BLUE.toString());
        str = str.replace("`B", Style.BLUE_DARK.toString());

        str = str.replace("`p", Style.PURPLE.toString());
        str = str.replace("`P", Style.PURPLE_DARK.toString());

        str = str.replace("`0", Style.BLACK.toString());
        str = str.replace("`1", Style.GRAY_DARK.toString());
        str = str.replace("`2", Style.GRAY.toString());
        str = str.replace("`w", Style.WHITE.toString());

        str = str.replace("`k", Style.RANDOMIZE.toString());

        str = str.replace("`l", Style.BOLD.toString());
        str = str.replace("`m", Style.STRIKETHROUGH.toString());
        str = str.replace("`n", Style.UNDERLINE.toString());
        str = str.replace("`o", Style.ITALIC.toString());

        str = str.replace("`x", Style.RESET.toString());

        // MC classic
        str = Style.translateAlternateColorCodes('&', str);

        return str;
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
    public static java.util.function.Function<String, ?> messageFunction(final Actor sender) {
        return (Function<String, Object>) s -> {
            sender.printRaw(s);
            return null;
        };
    }

}
