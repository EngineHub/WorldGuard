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

package com.sk89q.worldguard;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.squirrelid.cache.HashMapCache;
import com.sk89q.squirrelid.cache.ProfileCache;
import com.sk89q.squirrelid.cache.SQLiteCache;
import com.sk89q.squirrelid.resolver.BukkitPlayerService;
import com.sk89q.squirrelid.resolver.CacheForwardingService;
import com.sk89q.squirrelid.resolver.CombinedProfileService;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.SimpleFlagRegistry;
import com.sk89q.worldguard.util.concurrent.EvenMoreExecutors;
import com.sk89q.worldguard.util.task.SimpleSupervisor;
import com.sk89q.worldguard.util.task.Supervisor;
import com.sk89q.worldguard.util.task.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldGuard {

    public static final Logger logger = Logger.getLogger(WorldGuard.class.getCanonicalName());

    private static final WorldGuard instance = new WorldGuard();
    private WorldGuardPlatform platform;
    private final SimpleFlagRegistry flagRegistry = new SimpleFlagRegistry();
    private final Supervisor supervisor = new SimpleSupervisor();
    private ProfileCache profileCache;
    private ProfileService profileService;
    private ListeningExecutorService executorService;

    public static WorldGuard getInstance() {
        return instance;
    }

    private WorldGuard() {
    }

    public void setup() {
        executorService = MoreExecutors.listeningDecorator(EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 20));

        File cacheDir = new File(getPlatform().getConfigDir().toFile(), "cache");
        cacheDir.mkdirs();

        try {
            profileCache = new SQLiteCache(new File(getPlatform().getConfigDir().toFile(), "profiles.sqlite"));
        } catch (IOException e) {
            WorldGuard.logger.log(Level.WARNING, "Failed to initialize SQLite profile cache");
            profileCache = new HashMapCache();
        }

        profileService = new CacheForwardingService(
                new CombinedProfileService(
                        BukkitPlayerService.getInstance(),
                        HttpRepositoryService.forMinecraft()),
                profileCache);

        getPlatform().load();
        Flags.registerAll();
    }

    /**
     * The WorldGuard Platform.
     *
     * @return The platform
     */
    public WorldGuardPlatform getPlatform() {
        checkNotNull(platform);
        return platform;
    }

    public void setPlatform(WorldGuardPlatform platform) {
        checkNotNull(platform);
        this.platform = platform;
    }

    /**
     * Get the flag registry.
     *
     * @return the flag registry
     */
    public FlagRegistry getFlagRegistry() {
        return this.flagRegistry;
    }

    /**
     * Get the supervisor.
     *
     * @return the supervisor
     */
    public Supervisor getSupervisor() {
        return supervisor;
    }

    /**
     * Get the global executor service for internal usage (please use your
     * own executor service).
     *
     * @return the global executor service
     */
    public ListeningExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get the profile lookup service.
     *
     * @return the profile lookup service
     */
    public ProfileService getProfileService() {
        return profileService;
    }

    /**
     * Get the profile cache.
     *
     * @return the profile cache
     */
    public ProfileCache getProfileCache() {
        return profileCache;
    }

    /**
     * Called when WorldGuard should be disabled.
     */
    public void disable() {
        executorService.shutdown();

        try {
            WorldGuard.logger.log(Level.INFO, "Shutting down executor and waiting for any pending tasks...");

            List<Task<?>> tasks = supervisor.getTasks();
            if (!tasks.isEmpty()) {
                StringBuilder builder = new StringBuilder("Known tasks:");
                for (Task<?> task : tasks) {
                    builder.append("\n");
                    builder.append(task.getName());
                }
                WorldGuard.logger.log(Level.INFO, builder.toString());
            }

            Futures.successfulAsList(tasks).get();
            executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            WorldGuard.logger.log(Level.WARNING, "Some tasks failed while waiting for remaining tasks to finish", e);
        }

        platform.unload();
    }
}
