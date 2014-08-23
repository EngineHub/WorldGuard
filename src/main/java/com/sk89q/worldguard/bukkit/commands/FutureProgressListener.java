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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Timer;

import static com.google.common.base.Preconditions.checkNotNull;

public class FutureProgressListener implements Runnable {

    private static final Timer timer = new Timer();
    private static final int MESSAGE_DELAY = 1000;

    private final MessageTimerTask task;

    public FutureProgressListener(CommandSender sender, String message) {
        checkNotNull(sender);
        checkNotNull(message);

        task = new MessageTimerTask(sender, ChatColor.GRAY + message);
        timer.schedule(task, MESSAGE_DELAY);
    }

    @Override
    public void run() {
        task.cancel();
    }

    public static void addProgressListener(ListenableFuture<?> future, CommandSender sender, String message) {
        future.addListener(new FutureProgressListener(sender, message), MoreExecutors.sameThreadExecutor());
    }

}
