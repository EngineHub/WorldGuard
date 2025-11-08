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

package com.sk89q.worldguard.bukkit.util.report;

import com.google.common.reflect.TypeToken;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sk89q.worldedit.util.report.DataReport;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SchedulerReport extends DataReport {

    private LoadingCache<Class<?>, Optional<Field>> taskFieldCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, Optional<Field>>() {
                @Override
                public Optional<Field> load(Class<?> clazz) throws Exception {
                    try {
                        Field field = clazz.getDeclaredField("task");
                        field.setAccessible(true);
                        return Optional.ofNullable(field);
                    } catch (NoSuchFieldException ignored) {
                        return Optional.empty();
                    }
                }
            });

    public SchedulerReport() {
        super(message("reports.scheduler.title"));

        List<BukkitTask> tasks = Bukkit.getServer().getScheduler().getPendingTasks();

        append(message("reports.scheduler.pending-count"), tasks.size());

        for (BukkitTask task : tasks) {
            Class<?> taskClass = getTaskClass(task);

            DataReport report = new DataReport(message("reports.scheduler.entry.title", task.getTaskId()));
            report.append(message("reports.scheduler.entry.owner"), task.getOwner().getName());
            report.append(message("reports.scheduler.entry.runnable"),
                    taskClass != null ? taskClass.getName() : message("reports.scheduler.entry.runnable-unknown"));
            report.append(message("reports.scheduler.entry.sync"), task.isSync());
            append(report.getTitle(), report);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Class<?> getTaskClass(BukkitTask task) {
        try {
            Class<?> clazz = task.getClass();
            Set<Class<?>> classes = (Set) TypeToken.of(clazz).getTypes().rawTypes();

            for (Class<?> type : classes) {
                Optional<Field> field = taskFieldCache.getUnchecked(type);
                if (field.isPresent()) {
                    Object res = field.get().get(task);
                    return res == null ? null : res.getClass();
                }
            }
        } catch (IllegalAccessException | NoClassDefFoundError ignored) {
        }

        return null;
    }

    private static String message(String key, Object... arguments) {
        return WorldGuard.getInstance().getLocalization().format(key, arguments);
    }
}
