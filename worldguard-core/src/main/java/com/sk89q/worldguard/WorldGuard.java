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

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.domains.registry.DomainRegistry;
import com.sk89q.worldguard.domains.registry.SimpleDomainRegistry;
import com.sk89q.worldguard.util.profile.cache.HashMapCache;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import com.sk89q.worldguard.util.profile.cache.SQLiteCache;
import com.sk89q.worldguard.util.profile.resolver.ProfileService;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.task.SimpleSupervisor;
import com.sk89q.worldedit.util.task.Supervisor;
import com.sk89q.worldedit.util.task.Task;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.SimpleFlagRegistry;
import com.sk89q.worldguard.util.WorldGuardExceptionConverter;
import com.sk89q.worldguard.util.concurrent.EvenMoreExecutors;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorldGuard {

    public static final Logger logger = Logger.getLogger(WorldGuard.class.getCanonicalName());

    private static String version;
    private static final WorldGuard instance = new WorldGuard();

    private WorldGuardPlatform platform;
    private final SimpleFlagRegistry flagRegistry = new SimpleFlagRegistry();
    private final SimpleDomainRegistry domainRegistry = new SimpleDomainRegistry();
    private final Supervisor supervisor = new SimpleSupervisor();
    private ProfileCache profileCache;
    private ProfileService profileService;
    private ListeningExecutorService executorService;
    private WorldGuardExceptionConverter exceptionConverter = new WorldGuardExceptionConverter();

    static {
        Flags.registerAll();
    }

    public static WorldGuard getInstance() {
        return instance;
    }

    private WorldGuard() {
    }

    public void setup() {
        executorService = MoreExecutors.listeningDecorator(EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 20,
                "WorldGuard Task Executor - %s"));

        File cacheDir = new File(getPlatform().getConfigDir().toFile(), "cache");
        cacheDir.mkdirs();

        try {
            profileCache = new SQLiteCache(new File(cacheDir, "profiles.sqlite"));
        } catch (IOException | UnsatisfiedLinkError ignored) {
            logger.log(Level.WARNING, "Failed to initialize SQLite profile cache. Cache is memory-only.");
            profileCache = new HashMapCache();
        }

        profileService = getPlatform().createProfileService(profileCache);

        getPlatform().load();
    }

    /**
     * The WorldGuard Platform.
     * The Platform is only available after WorldGuard is enabled.
     *
     * @return The platform
     */
    public WorldGuardPlatform getPlatform() {
        checkNotNull(platform, "WorldGuard is not enabled, unable to access the platform.");
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
     * Get the domain registry.
     *
     * @return the domain registry
     */
    public DomainRegistry getDomainRegistry() {
        return this.domainRegistry;
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
     * Get the exception converter
     *
     * @return the exception converter
     */
    public WorldGuardExceptionConverter getExceptionConverter() {
        return exceptionConverter;
    }

    /**
     * Checks to see if the sender is a player, otherwise throw an exception.
     *
     * @param sender The sender
     * @return The player
     * @throws CommandException if it isn't a player
     */
    public LocalPlayer checkPlayer(Actor sender) throws CommandException {
        if (sender instanceof LocalPlayer) {
            return (LocalPlayer) sender;
        } else {
            throw new CommandException("A player is expected.");
        }
    }

    /**
     * Called when WorldGuard should be disabled.
     */
    public void disable() {
        executorService.shutdown();

        try {
            logger.log(Level.INFO, "Shutting down executor and cancelling any pending tasks...");

            List<Task<?>> tasks = supervisor.getTasks();
            if (!tasks.isEmpty()) {
                StringBuilder builder = new StringBuilder("Known tasks:");
                for (Task<?> task : tasks) {
                    builder.append("\n");
                    builder.append(task.getName());
                    task.cancel(true);
                }
                logger.log(Level.INFO, builder.toString());
            }

            //Futures.successfulAsList(tasks).get();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        platform.unload();
    }

    /**
     * Get the version.
     *
     * @return the version of WorldEdit
     */
    public static String getVersion() {
        if (version != null) {
            return version;
        }

        Package p = WorldGuard.class.getPackage();

        if (p == null) {
            p = Package.getPackage("com.sk89q.worldguard");
        }

        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();

            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }

}
