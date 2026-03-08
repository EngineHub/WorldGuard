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

/**
 * Represents a scheduled task that can be cancelled.
 * 
 * <p>This provides a unified interface to work with both Bukkit and Folia
 * scheduled tasks.</p>
 */
public interface ScheduledTask {

    /**
     * Cancel this task.
     */
    void cancel();

    /**
     * Check if this task has been cancelled.
     * 
     * @return true if the task is cancelled, false otherwise
     */
    boolean isCancelled();

    /**
     * Get the task ID, if available.
     * 
     * @return the task ID, or -1 if not available
     */
    default int getTaskId() {
        return -1;
    }
}