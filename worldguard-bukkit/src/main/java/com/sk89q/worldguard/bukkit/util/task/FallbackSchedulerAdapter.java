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

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Fallback scheduler adapter for servers where BukkitScheduler is disabled.
 * 
 * <p>This implementation uses Java's built-in scheduling mechanisms instead
 * of relying on the Bukkit scheduler API, making it compatible with servers
 * like Canvas that disable the traditional scheduler.</p>
 */
public class FallbackSchedulerAdapter implements SchedulerAdapter {
    
    private static final Logger LOGGER = Logger.getLogger(FallbackSchedulerAdapter.class.getName());
    
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, 
        r -> new Thread(r, "WorldGuard-Fallback-Scheduler"));
    
    private final Timer timer = new Timer("WorldGuard-Fallback-Timer", true);
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<Long, TimerTask> timerTasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(1);
    
    @Override
    public ScheduledTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        long taskId = taskIdGenerator.getAndIncrement();
        
        // Convert ticks to milliseconds (20 ticks = 1 second)
        long delayMs = delay * 50;
        long periodMs = period * 50;
        
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    task.run();
                } catch (Exception e) {
                    LOGGER.warning("Exception in scheduled task: " + e.getMessage());
                }
            }
        };
        
        timer.scheduleAtFixedRate(timerTask, delayMs, periodMs);
        timerTasks.put(taskId, timerTask);
        
        return new FallbackScheduledTask(taskId, timerTask, null);
    }

    @Override
    public ScheduledTask runTaskLater(Plugin plugin, Runnable task, long delay) {
        long taskId = taskIdGenerator.getAndIncrement();
        
        // Convert ticks to milliseconds
        long delayMs = delay * 50;
        
        ScheduledFuture<?> future = executor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warning("Exception in delayed task: " + e.getMessage());
            } finally {
                scheduledTasks.remove(taskId);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
        
        scheduledTasks.put(taskId, future);
        return new FallbackScheduledTask(taskId, null, future);
    }

    @Override
    public ScheduledTask runTask(Plugin plugin, Runnable task) {
        return runTaskLater(plugin, task, 1); // Run on next tick
    }

    @Override
    public ScheduledTask runTaskAt(Plugin plugin, Location location, Runnable task) {
        // For fallback, location doesn't matter - just run normally
        return runTask(plugin, task);
    }

    @Override
    public ScheduledTask runTaskAtLater(Plugin plugin, Location location, Runnable task, long delay) {
        // For fallback, location doesn't matter - just run normally
        return runTaskLater(plugin, task, delay);
    }

    @Override
    public ScheduledTask runTaskFor(Plugin plugin, Entity entity, Runnable task) {
        // For fallback, entity doesn't matter - just run normally
        return runTask(plugin, task);
    }

    @Override
    public ScheduledTask runTaskForLater(Plugin plugin, Entity entity, Runnable task, long delay) {
        // For fallback, entity doesn't matter - just run normally
        return runTaskLater(plugin, task, delay);
    }

    @Override
    public ScheduledTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        long taskId = taskIdGenerator.getAndIncrement();
        
        ScheduledFuture<?> future = executor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warning("Exception in async task: " + e.getMessage());
            } finally {
                scheduledTasks.remove(taskId);
            }
        }, 0, TimeUnit.MILLISECONDS);
        
        scheduledTasks.put(taskId, future);
        return new FallbackScheduledTask(taskId, null, future);
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        // Cancel all scheduled tasks
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();
        
        // Cancel all timer tasks
        timerTasks.values().forEach(TimerTask::cancel);
        timerTasks.clear();
    }

    @Override
    public boolean isFolia() {
        return false;
    }
    
    /**
     * Shutdown the fallback scheduler.
     * This should be called when the plugin is disabled.
     */
    public void shutdown() {
        timer.cancel();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Wrapper for fallback tasks to implement ScheduledTask interface.
     */
    private class FallbackScheduledTask implements ScheduledTask {
        private final long taskId;
        private final TimerTask timerTask;
        private final ScheduledFuture<?> future;
        private volatile boolean cancelled = false;

        public FallbackScheduledTask(long taskId, TimerTask timerTask, ScheduledFuture<?> future) {
            this.taskId = taskId;
            this.timerTask = timerTask;
            this.future = future;
        }

        @Override
        public void cancel() {
            if (cancelled) return;
            
            cancelled = true;
            
            // Cancel the timer task if it exists
            if (timerTask != null) {
                timerTask.cancel();
                timerTasks.remove(taskId);
            }
            
            // Cancel the scheduled future if it exists
            if (future != null) {
                future.cancel(false);
                scheduledTasks.remove(taskId);
            }
        }

        @Override
        public boolean isCancelled() {
            return cancelled || 
                   (future != null && future.isCancelled());
        }
    }
}