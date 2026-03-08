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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Folia scheduler adapter implementation using reflection.
 * 
 * <p>This implementation uses Folia's region-based scheduling system.
 * Different tasks are scheduled on different schedulers depending on their context.</p>
 */
public class FoliaSchedulerAdapter implements SchedulerAdapter {
    
    private static final Logger LOGGER = Logger.getLogger(FoliaSchedulerAdapter.class.getName());
    
    // Cache for reflection methods
    private final Object globalRegionScheduler;
    private final Object regionScheduler;
    private final Object asyncScheduler;
    
    private final Method globalRunAtFixedRate;
    private final Method globalRunDelayed;
    private final Method globalRun;
    private final Method globalCancelTasks;
    
    private final Method regionRun;
    private final Method regionRunDelayed;
    
    private final Method entityRun;
    private final Method entityRunDelayed;
    
    private final Method asyncRun;
    
    // Task tracking for cancellation
    private final Map<Plugin, AtomicLong> taskCounters = new ConcurrentHashMap<>();
    private final Map<Long, Object> activeTasks = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public FoliaSchedulerAdapter() throws ReflectiveOperationException {
        try {
            // Get Folia schedulers
            this.globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            this.regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            this.asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            
            // Global scheduler methods - Folia uses Consumer<ScheduledTask>, not Runnable
            this.globalRunAtFixedRate = globalRegionScheduler.getClass().getMethod(
                "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            this.globalRunDelayed = globalRegionScheduler.getClass().getMethod(
                "runDelayed", Plugin.class, Consumer.class, long.class);
            this.globalRun = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
            this.globalCancelTasks = globalRegionScheduler.getClass().getMethod("cancelTasks", Plugin.class);
            
            // Region scheduler methods
            this.regionRun = regionScheduler.getClass().getMethod(
                "run", Plugin.class, Location.class, Consumer.class);
            this.regionRunDelayed = regionScheduler.getClass().getMethod(
                "runDelayed", Plugin.class, Location.class, Consumer.class, long.class);
            
            // Entity scheduler methods - Consumer for task, Runnable for retired callback
            this.entityRun = Entity.class.getMethod("getScheduler")
                .getReturnType().getMethod("run", Plugin.class, Consumer.class, Runnable.class);
            this.entityRunDelayed = Entity.class.getMethod("getScheduler")
                .getReturnType().getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
            
            // Async scheduler methods
            this.asyncRun = asyncScheduler.getClass().getMethod(
                "runNow", Plugin.class, Consumer.class);
                
        } catch (Exception e) {
            throw new ReflectiveOperationException("Failed to initialize Folia scheduler adapter", e);
        }
    }

    @Override
    public ScheduledTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object scheduledTask = globalRunAtFixedRate.invoke(globalRegionScheduler, plugin, consumer, delay, period);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule repeating task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledTask runTaskLater(Plugin plugin, Runnable task, long delay) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object scheduledTask = globalRunDelayed.invoke(globalRegionScheduler, plugin, consumer, delay);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule delayed task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledTask runTask(Plugin plugin, Runnable task) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object scheduledTask = globalRun.invoke(globalRegionScheduler, plugin, consumer);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledTask runTaskAt(Plugin plugin, Location location, Runnable task) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object scheduledTask = regionRun.invoke(regionScheduler, plugin, location, consumer);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule location-based task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledTask runTaskAtLater(Plugin plugin, Location location, Runnable task, long delay) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object scheduledTask = regionRunDelayed.invoke(regionScheduler, plugin, location, consumer, delay);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule delayed location-based task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledTask runTaskFor(Plugin plugin, Entity entity, Runnable task) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object entityScheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            Object scheduledTask = entityRun.invoke(entityScheduler, plugin, consumer, null);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule entity task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledTask runTaskForLater(Plugin plugin, Entity entity, Runnable task, long delay) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object entityScheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
            Object scheduledTask = entityRunDelayed.invoke(entityScheduler, plugin, consumer, null, delay);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule delayed entity task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ScheduledTask runTaskAsynchronously(Plugin plugin, Runnable task) {
        try {
            Consumer<Object> consumer = t -> task.run();
            Object scheduledTask = asyncRun.invoke(asyncScheduler, plugin, consumer);
            return new FoliaScheduledTask(scheduledTask, generateTaskId(plugin));
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to schedule async task", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        try {
            globalCancelTasks.invoke(globalRegionScheduler, plugin);
            
            // Also cancel tasks we're tracking
            taskCounters.remove(plugin);
            activeTasks.entrySet().removeIf(entry -> {
                try {
                    Object task = entry.getValue();
                    // Try to get the plugin from the task and compare
                    Method getPlugin = findInterfaceMethod(task, "getPlugin");
                    Object taskPlugin = getPlugin.invoke(task);
                    if (plugin.equals(taskPlugin)) {
                        Method cancel = findInterfaceMethod(task, "cancel");
                        cancel.invoke(task);
                        return true;
                    }
                } catch (Exception e) {
                    // Ignore reflection errors during cleanup
                }
                return false;
            });
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Failed to cancel tasks", e);
        }
    }

    @Override
    public boolean isFolia() {
        return true;
    }
    
    private long generateTaskId(Plugin plugin) {
        return taskCounters.computeIfAbsent(plugin, k -> new AtomicLong(1)).getAndIncrement();
    }

    /**
     * Finds a method by name on the public interfaces implemented by the given object's class.
     * This avoids {@link IllegalAccessException} when the concrete class is a non-public
     * inner class inside a module that doesn't open itself for deep reflection (e.g. Folia internals).
     */
    private static Method findInterfaceMethod(Object obj, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        for (Class<?> iface : obj.getClass().getInterfaces()) {
            try {
                return iface.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        // Fallback to the concrete class (may throw IllegalAccessException at invoke time)
        return obj.getClass().getMethod(methodName, paramTypes);
    }

    /**
     * Wrapper for Folia tasks to implement ScheduledTask interface.
     */
    private class FoliaScheduledTask implements ScheduledTask {
        private final Object task;
        private final long taskId;
        private volatile boolean cancelled = false;

        public FoliaScheduledTask(Object task, long taskId) {
            this.task = task;
            this.taskId = taskId;
            activeTasks.put(taskId, task);
        }

        @Override
        public void cancel() {
            if (cancelled) return;
            
            try {
                Method cancel = findInterfaceMethod(task, "cancel");
                cancel.invoke(task);
                cancelled = true;
                activeTasks.remove(taskId);
            } catch (ReflectiveOperationException e) {
                LOGGER.log(Level.WARNING, "Failed to cancel Folia task", e);
            }
        }

        @Override
        public boolean isCancelled() {
            if (cancelled) return true;
            
            try {
                Method isCancelled = findInterfaceMethod(task, "isCancelled");
                boolean result = (Boolean) isCancelled.invoke(task);
                if (result) {
                    cancelled = true;
                    activeTasks.remove(taskId);
                }
                return result;
            } catch (ReflectiveOperationException e) {
                LOGGER.log(Level.WARNING, "Failed to check task cancellation status", e);
                return cancelled;
            }
        }

        @Override
        public int getTaskId() {
            return (int) taskId;
        }
    }
}