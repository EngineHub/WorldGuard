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
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SchedulerReport extends DataReport {

    private FoliaLib foliaLib = WorldGuardPlugin.inst().getFoliaLib();

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

    private LoadingCache<Class<?>, Optional<Field>> foliaTaskFieldCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, Optional<Field>>() {
                @Override
                public Optional<Field> load(Class<?> clazz) throws Exception {
                    try {
                        Field field = clazz.getDeclaredField("run");
                        field.setAccessible(true);
                        return Optional.ofNullable(field);
                    } catch (NoSuchFieldException ignored) {
                        return Optional.empty();
                    }
                }
            });

    public SchedulerReport() {
        super("Scheduler");

        List<WrappedTask> tasks = foliaLib.getImpl().getAllTasks();

        append("Pending Task Count", tasks.size());

        for (WrappedTask task : tasks) {
            Object handle = getTaskHandle(task);
            Class<?> taskClass;
            if (foliaLib.isFolia()) {
                taskClass = getFoliaTaskClass(handle);
            } else {
                taskClass = getBukkitTaskClass(handle);
            }

            DataReport report = new DataReport("Task: #" + handle.hashCode());
            report.append("Owner", task.getOwningPlugin().getName());
            report.append("Runnable", taskClass != null ? taskClass.getName() : "<Unknown>");
            report.append("Synchronous?", !task.isAsync());
            append(report.getTitle(), report);
        }
    }

    private Object getTaskHandle(WrappedTask task) {
        try {
            Field field = task.getClass().getDeclaredField("task");
            field.setAccessible(true);
            return field.get(task);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Class<?> getFoliaTaskClass(Object task) {
        try {
            Class<?> clazz = task.getClass();
            Set<Class<?>> classes = (Set) TypeToken.of(clazz).getTypes().rawTypes();

            for (Class<?> type : classes) {
                Optional<Field> field = foliaTaskFieldCache.getUnchecked(type);
                if (field.isPresent()) {
                    Object res = field.get().get(task);
                    return res == null ? null : res.getClass();
                }
            }
        } catch (IllegalAccessException | NoClassDefFoundError ignored) {
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Class<?> getBukkitTaskClass(Object task) {
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
}
