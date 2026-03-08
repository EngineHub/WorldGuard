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

package com.sk89q.worldguard.bukkit.util.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Traditional Bukkit/Paper scheduler adapter implementation.
 * 
 * <p>This implementation uses the classic BukkitScheduler API which works
 * on single-threaded Paper and CraftBukkit servers.</p>
 */
public class BukkitSchedulerAdapter implements SchedulerAdapter {

    @Override
    public ScheduledTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        try {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
            return new BukkitScheduledTask(bukkitTask);
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("BukkitScheduler is disabled on this server. Use FallbackSchedulerAdapter instead.", e);
        }
    }

    @Override
    public ScheduledTask runTaskLater(Plugin plugin, Runnable task, long delay) {
        try {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            return new BukkitScheduledTask(bukkitTask);
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("BukkitScheduler is disabled on this server. Use FallbackSchedulerAdapter instead.", e);
        }
    }

    @Override
    public ScheduledTask runTask(Plugin plugin, Runnable task) {
        try {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
            return new BukkitScheduledTask(bukkitTask);
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("BukkitScheduler is disabled on this server. Use FallbackSchedulerAdapter instead.", e);
        }
    }

    @Override
    public ScheduledTask runTaskAt(Plugin plugin, Location location, Runnable task) {
        // For Bukkit/Paper, location doesn't matter - just run normally
        return runTask(plugin, task);
    }

    @Override
    public ScheduledTask runTaskAtLater(Plugin plugin, Location location, Runnable task, long delay) {
        // For Bukkit/Paper, location doesn't matter - just run normally
        return runTaskLater(plugin, task, delay);
    }

    @Override
    public ScheduledTask runTaskFor(Plugin plugin, Entity entity, Runnable task) {
        // For Bukkit/Paper, entity doesn't matter - just run normally
        return runTask(plugin, task);
    }

    @Override
    public ScheduledTask runTaskForLater(Plugin plugin, Entity entity, Runnable task, long delay) {
        // For Bukkit/Paper, entity doesn't matter - just run normally
        return runTaskLater(plugin, task, delay);
    }

    @Override
    public ScheduledTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        try {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            return new BukkitScheduledTask(bukkitTask);
        } catch (UnsupportedOperationException e) {
            throw new RuntimeException("BukkitScheduler is disabled on this server. Use FallbackSchedulerAdapter instead.", e);
        }
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        try {
            Bukkit.getScheduler().cancelTasks(plugin);
        } catch (UnsupportedOperationException e) {
            // Ignore - scheduler is disabled, nothing to cancel
        }
    }

    @Override
    public boolean isFolia() {
        return false;
    }

    /**
     * Wrapper for BukkitTask to implement ScheduledTask interface.
     */
    private static class BukkitScheduledTask implements ScheduledTask {
        private final BukkitTask task;

        public BukkitScheduledTask(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public int getTaskId() {
            return task.getTaskId();
        }
    }
}