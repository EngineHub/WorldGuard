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

/**
 * Scheduler adapter for cross-compatibility between Paper/Bukkit and Folia.
 * 
 * <p>Folia introduces region-based threading which requires different scheduling
 * approaches compared to the single-threaded Bukkit scheduler. This adapter 
 * provides a unified interface for both platforms.</p>
 */
public interface SchedulerAdapter {

    /**
     * Schedule a repeating task to run synchronously.
     * 
     * @param plugin the plugin scheduling the task
     * @param task the task to run
     * @param delay initial delay in ticks
     * @param period repeat period in ticks
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTaskTimer(Plugin plugin, Runnable task, long delay, long period);

    /**
     * Schedule a task to run synchronously after a delay.
     * 
     * @param plugin the plugin scheduling the task
     * @param task the task to run
     * @param delay delay in ticks
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTaskLater(Plugin plugin, Runnable task, long delay);

    /**
     * Schedule a task to run synchronously on the next tick.
     * 
     * @param plugin the plugin scheduling the task
     * @param task the task to run
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTask(Plugin plugin, Runnable task);

    /**
     * Schedule a task to run synchronously at a specific location.
     * For non-Folia implementations, this behaves the same as runTask.
     * 
     * @param plugin the plugin scheduling the task
     * @param location the location where the task should run
     * @param task the task to run
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTaskAt(Plugin plugin, Location location, Runnable task);

    /**
     * Schedule a task to run synchronously at a specific location after a delay.
     * For non-Folia implementations, this behaves the same as runTaskLater.
     * 
     * @param plugin the plugin scheduling the task
     * @param location the location where the task should run
     * @param task the task to run
     * @param delay delay in ticks
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTaskAtLater(Plugin plugin, Location location, Runnable task, long delay);

    /**
     * Schedule a task to run synchronously for a specific entity.
     * For non-Folia implementations, this behaves the same as runTask.
     * 
     * @param plugin the plugin scheduling the task
     * @param entity the entity for which the task should run
     * @param task the task to run
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTaskFor(Plugin plugin, Entity entity, Runnable task);

    /**
     * Schedule a task to run synchronously for a specific entity after a delay.
     * For non-Folia implementations, this behaves the same as runTaskLater.
     * 
     * @param plugin the plugin scheduling the task
     * @param entity the entity for which the task should run
     * @param task the task to run
     * @param delay delay in ticks
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTaskForLater(Plugin plugin, Entity entity, Runnable task, long delay);

    /**
     * Schedule a task to run asynchronously.
     * 
     * @param plugin the plugin scheduling the task
     * @param task the task to run
     * @return task identifier that can be used to cancel the task
     */
    ScheduledTask runTaskAsynchronously(Plugin plugin, Runnable task);

    /**
     * Cancel all tasks scheduled by the given plugin.
     * 
     * @param plugin the plugin whose tasks should be cancelled
     */
    void cancelTasks(Plugin plugin);

    /**
     * Check if this adapter is running on Folia.
     * 
     * @return true if running on Folia, false otherwise
     */
    boolean isFolia();
}