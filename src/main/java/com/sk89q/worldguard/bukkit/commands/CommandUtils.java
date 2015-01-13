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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.util.paste.EngineHubPaste;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command-related utility methods.
 */
public final class CommandUtils {

    private static final Logger log = Logger.getLogger(CommandUtils.class.getCanonicalName());

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

        str = str.replace("`r", ChatColor.RED.toString());
        str = str.replace("`R", ChatColor.DARK_RED.toString());

        str = str.replace("`y", ChatColor.YELLOW.toString());
        str = str.replace("`Y", ChatColor.GOLD.toString());

        str = str.replace("`g", ChatColor.GREEN.toString());
        str = str.replace("`G", ChatColor.DARK_GREEN.toString());

        str = str.replace("`c", ChatColor.AQUA.toString());
        str = str.replace("`C", ChatColor.DARK_AQUA.toString());

        str = str.replace("`b", ChatColor.BLUE.toString());
        str = str.replace("`B", ChatColor.DARK_BLUE.toString());

        str = str.replace("`p", ChatColor.LIGHT_PURPLE.toString());
        str = str.replace("`P", ChatColor.DARK_PURPLE.toString());

        str = str.replace("`0", ChatColor.BLACK.toString());
        str = str.replace("`1", ChatColor.DARK_GRAY.toString());
        str = str.replace("`2", ChatColor.GRAY.toString());
        str = str.replace("`w", ChatColor.WHITE.toString());

        str = str.replace("`k", ChatColor.MAGIC.toString());

        str = str.replace("`l", ChatColor.BOLD.toString());
        str = str.replace("`m", ChatColor.STRIKETHROUGH.toString());
        str = str.replace("`n", ChatColor.UNDERLINE.toString());
        str = str.replace("`o", ChatColor.ITALIC.toString());

        str = str.replace("`x", ChatColor.RESET.toString());

        // MC classic

        str = str.replace("&c", ChatColor.RED.toString());
        str = str.replace("&4", ChatColor.DARK_RED.toString());

        str = str.replace("&e", ChatColor.YELLOW.toString());
        str = str.replace("&6", ChatColor.GOLD.toString());

        str = str.replace("&a", ChatColor.GREEN.toString());
        str = str.replace("&2", ChatColor.DARK_GREEN.toString());

        str = str.replace("&b", ChatColor.AQUA.toString());
        str = str.replace("&3", ChatColor.DARK_AQUA.toString());

        str = str.replace("&9", ChatColor.BLUE.toString());
        str = str.replace("&1", ChatColor.DARK_BLUE.toString());

        str = str.replace("&d", ChatColor.LIGHT_PURPLE.toString());
        str = str.replace("&5", ChatColor.DARK_PURPLE.toString());

        str = str.replace("&0", ChatColor.BLACK.toString());
        str = str.replace("&8", ChatColor.DARK_GRAY.toString());
        str = str.replace("&7", ChatColor.GRAY.toString());
        str = str.replace("&f", ChatColor.WHITE.toString());

        str = str.replace("&k", ChatColor.MAGIC.toString());

        str = str.replace("&l", ChatColor.BOLD.toString());
        str = str.replace("&m", ChatColor.STRIKETHROUGH.toString());
        str = str.replace("&n", ChatColor.UNDERLINE.toString());
        str = str.replace("&o", ChatColor.ITALIC.toString());

        str = str.replace("&x", ChatColor.RESET.toString());
        str = str.replace("&r", ChatColor.RESET.toString());

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
    public static Function<String, ?> messageFunction(final CommandSender sender) {
        return new Function<String, Object>() {
            @Override
            public Object apply(@Nullable String s) {
                sender.sendMessage(s);
                return null;
            }
        };
    }

    /**
     * Submit data to a pastebin service and inform the sender of
     * success or failure.
     *
     * @param plugin The plugin
     * @param sender The sender
     * @param content The content
     * @param successMessage The message, formatted with {@link String#format(String, Object...)} on success
     */
    public static void pastebin(WorldGuardPlugin plugin, final CommandSender sender, String content, final String successMessage) {
        ListenableFuture<URL> future = new EngineHubPaste().paste(content);

        AsyncCommandHelper.wrap(future, plugin, sender)
                .registerWithSupervisor("Submitting content to a pastebin service...")
                .sendMessageAfterDelay("(Please wait... sending output to pastebin...)");

        Futures.addCallback(future, new FutureCallback<URL>() {
            @Override
            public void onSuccess(URL url) {
                sender.sendMessage(ChatColor.YELLOW + String.format(successMessage, url));
            }

            @Override
            public void onFailure(Throwable throwable) {
                log.log(Level.WARNING, "Failed to submit pastebin", throwable);
                sender.sendMessage(ChatColor.RED + "Failed to submit to a pastebin. Please see console for the error.");
            }
        });
    }

}
