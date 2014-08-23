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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldguard.util.task.FutureForwardingTask;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class AsyncCommandHelper {

    private final ListenableFuture<?> future;
    private final WorldGuardPlugin plugin;
    private final CommandSender sender;
    @Nullable
    private Object[] formatArgs;

    private AsyncCommandHelper(ListenableFuture<?> future, WorldGuardPlugin plugin, CommandSender sender) {
        checkNotNull(future);
        checkNotNull(plugin);
        checkNotNull(sender);

        this.future = future;
        this.plugin = plugin;
        this.sender = sender;
    }

    public AsyncCommandHelper formatUsing(Object... args) {
        this.formatArgs = args;
        return this;
    }

    private String format(String message) {
        if (formatArgs != null) {
            return String.format(message, formatArgs);
        } else {
            return message;
        }
    }

    public AsyncCommandHelper registerWithSupervisor(String description) {
        plugin.getSupervisor().monitor(
                FutureForwardingTask.create(
                        future, format(description), sender));
        return this;
    }

    public AsyncCommandHelper sendMessageAfterDelay(String message) {
        FutureProgressListener.addProgressListener(future, sender, format(message));
        return this;
    }

    public AsyncCommandHelper thenRespondWith(String success, String failure) {
        // Send a response message
        Futures.addCallback(
                future,
                new MessageFutureCallback.Builder(plugin, sender)
                        .onSuccess(format(success))
                        .onFailure(format(failure))
                        .build());
        return this;
    }

    public AsyncCommandHelper thenTellErrorsOnly(String failure) {
        // Send a response message
        Futures.addCallback(
                future,
                new MessageFutureCallback.Builder(plugin, sender)
                        .onFailure(format(failure))
                        .build());
        return this;
    }

    public AsyncCommandHelper forRegionDataLoad(World world, boolean silent) {
        checkNotNull(world);

        formatUsing(world.getName());
        registerWithSupervisor("Loading region data for '%s'");
        if (silent) {
            thenTellErrorsOnly("Failed to load regions '%s'");
        } else {
            sendMessageAfterDelay("(Please wait... loading the region data for '%s')");
            thenRespondWith(
                    "Loaded region data for '%s'",
                    "Failed to load regions '%s'");
        }

        return this;
    }

    public AsyncCommandHelper forRegionDataSave(World world, boolean silent) {
        checkNotNull(world);

        formatUsing(world.getName());
        registerWithSupervisor("Saving region data for '%s'");
        if (silent) {
            thenTellErrorsOnly("Failed to save regions '%s'");
        } else {
            sendMessageAfterDelay("(Please wait... saving the region data for '%s')");
            thenRespondWith(
                    "Saved region data for '%s'",
                    "Failed to load regions '%s'");
        }

        return this;
    }

    public static AsyncCommandHelper wrap(ListenableFuture<?> future, WorldGuardPlugin plugin, CommandSender sender) {
        return new AsyncCommandHelper(future, plugin, sender);
    }

}
