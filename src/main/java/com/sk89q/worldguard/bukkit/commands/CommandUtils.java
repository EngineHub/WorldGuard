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
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldguard.protection.databases.util.UnresolvedNamesException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.Timer;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Command-related utility methods.
 */
final class CommandUtils {

    private static final Logger log = Logger.getLogger(CommandUtils.class.getCanonicalName());
    private static final Timer timer = new Timer();
    private static final int MESSAGE_DELAY = 1000;

    private CommandUtils() {
    }

    /**
     * Add a message that gets shown after a delay if the future has not
     * completed.
     *
     * @param future the future
     * @param sender the sender to send a message to
     * @param message the message to send (will be grey)
     */
    static void progressCallback(ListenableFuture<?> future, CommandSender sender, String message) {
        checkNotNull(future);
        checkNotNull(sender);
        checkNotNull(message);

        final MessageTimerTask task = new MessageTimerTask(sender, ChatColor.GRAY + message);
        timer.schedule(task, MESSAGE_DELAY);
        future.addListener(new Runnable() {
            @Override
            public void run() {
                task.cancel();
            }
        }, MoreExecutors.sameThreadExecutor());
    }

    /**
     * Create a callback to print a message to the user.
     *
     * @param sender the sender
     * @param successMessage a success message or {@code null} to print nothing
     * @param errorMessage an error message
     * @param <T> ignored type
     * @return a callback
     */
    static <T> FutureCallback<T> messageCallback(final CommandSender sender, @Nullable final String successMessage, final String errorMessage) {
        checkNotNull(sender);
        checkNotNull(errorMessage);

        return new FutureCallback<T>() {
            @Override
            public void onSuccess(@Nullable T o) {
                if (successMessage != null) {
                    sender.sendMessage(ChatColor.YELLOW + successMessage);
                }
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                sender.sendMessage(ChatColor.RED + errorMessage + ": " + convertThrowable(throwable));
            }
        };
    }

    /**
     * Return a function that accepts a string to send a message to the
     * given sender.
     *
     * @param sender the sender
     * @return a function
     */
    static Function<String, ?> messageFunction(final CommandSender sender) {
        return new Function<String, Object>() {
            @Override
            public Object apply(@Nullable String s) {
                sender.sendMessage(s);
                return null;
            }
        };
    }

    /**
     * Return a function to add a prefix and suffix to the input string.
     *
     * @param prefix the prefix
     * @param suffix the suffix
     * @return a function
     */
    static Function<String, String> messageAppender(final String prefix, final String suffix) {
        return new Function<String, String>() {
            @Override
            public String apply(@Nullable String s) {
                return prefix + s + suffix;
            }
        };
    }

    /**
     * Create a callback to save a region manager on success.
     *
     * @param sender the sender
     * @param manager the region manager
     * @param world the world
     * @param silent true to not print a success message
     * @param <T> an ignored type
     * @return a callback
     */
    static <T> FutureCallback<T> saveRegionsCallback(final CommandSender sender, final RegionManager manager, final World world, final boolean silent) {
        checkNotNull(sender);
        checkNotNull(manager);
        checkNotNull(world);

        return new FutureCallback<T>() {
            @Override
            public void onSuccess(@Nullable T o) {
                ListenableFuture<?> future = manager.save(true);
                String successMessage = silent ? null : "Successfully saved the region data for '" + world.getName() + "'.";
                String failureMessage = "Failed to save the region data for '" + world.getName() + "'";
                Futures.addCallback(future, messageCallback(sender, successMessage, failureMessage));
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
            }
        };
    }

    /**
     * Convert the throwable into a somewhat friendly message.
     *
     * @param throwable the throwable
     * @return a message
     */
    private static String convertThrowable(@Nullable Throwable throwable) {
        if (throwable instanceof CancellationException) {
            return "Task was cancelled";
        } else if (throwable instanceof InterruptedException) {
            return "Task was interrupted";
        } else if (throwable instanceof UnresolvedNamesException) {
            return throwable.getMessage();
        } else if (throwable instanceof Exception) {
            log.log(Level.WARNING, "WorldGuard encountered an unexpected error", throwable);
            return "Unexpected error occurred: " + ((Exception) throwable).getMessage();
        } else {
            return "Unknown error";
        }
    }

}
