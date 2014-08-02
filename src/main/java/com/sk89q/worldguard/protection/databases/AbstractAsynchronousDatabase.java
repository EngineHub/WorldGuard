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

package com.sk89q.worldguard.protection.databases;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.odeum.concurrency.EvenMoreExecutors;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract implementation of a {@code RegionManager} that supports
 * asynchronously loading and saving region data while only allowing one
 * single operation (either load or save) occurring at a given time.
 */
public abstract class AbstractAsynchronousDatabase extends AbstractProtectionDatabase {

    private static final Logger log = Logger.getLogger(AbstractAsynchronousDatabase.class.getName());

    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 4));
    private final Object lock = new Object();
    private QueuedTask lastSave;
    private QueuedTask lastLoad;

    @Override
    public final void load() throws ProtectionDatabaseException, RejectedExecutionException {
        blockOnSave(submitLoadTask(null));
    }

    @Override
    public final ListenableFuture<?> load(RegionManager manager, boolean async) throws RejectedExecutionException {
        ListenableFuture<?> future = submitLoadTask(manager);
        if (!async) {
            blockOnLoad(future);
        }
        return future;
    }

    @Override
    public final void save() throws ProtectionDatabaseException, RejectedExecutionException {
        blockOnSave(submitSaveTask(new HashMap<String, ProtectedRegion>(getRegions())));
    }

    @Override
    public final ListenableFuture<?> save(RegionManager manager, boolean async) throws RejectedExecutionException {
        ListenableFuture<?> future = submitSaveTask(new HashMap<String, ProtectedRegion>(manager.getRegions()));
        if (!async) {
            blockOnSave(future);
        }
        return future;
    }

    /**
     * Submit a load task and return a future for it.
     *
     * <p>If a load task is already queued then that load task's future will
     * be returned.</p>
     *
     * @param manager the manager
     * @return a future
     * @throws RejectedExecutionException thrown if there are too many load/save tasks queued
     */
    private ListenableFuture<?> submitLoadTask(@Nullable final RegionManager manager) throws RejectedExecutionException {
        synchronized (lock) {
            lastSave = null; // Void the pending queued save so that any future
                             // save() calls will submit a brand new save task

            QueuedTask last = lastLoad;

            // Check if there is already a queued task that has not yet started
            // that we can return, rather than queue yet another task
            if (last != null && !last.started) {
                return last.future;
            } else {
                // Submit the task
                final QueuedTask task = new QueuedTask();
                task.future = executor.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        task.started = true;

                        performLoad();
                        if (manager != null) {
                            manager.setRegions(getRegions());
                        }
                        return AbstractAsynchronousDatabase.this;
                    }
                });

                this.lastLoad = task;

                return task.future;
            }
        }
    }

    /**
     * Submit a save task and return a future for it.
     *
     * <p>If a save task is already queued then that save task's future will
     * be returned.</p>
     *
     * @param entries a map of regions
     * @return a future
     * @throws RejectedExecutionException thrown if there are too many load/save tasks queued
     */
    private ListenableFuture<?> submitSaveTask(final Map<String, ProtectedRegion> entries) throws RejectedExecutionException {
        checkNotNull(entries);

        synchronized (lock) {
            lastLoad = null; // Void the pending queued load so that any future
                             // load() calls will submit a brand new load task

            QueuedTask last = lastSave;

            // Check if there is already a queued task that has not yet started
            // that we can return, rather than queue yet another task
            if (last != null && !last.started) {
                return last.future;
            } else {
                // Submit the task
                final QueuedTask task = new QueuedTask();
                task.future = executor.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        task.started = true;

                        setRegions(entries);
                        performSave();
                        return AbstractAsynchronousDatabase.this;
                    }
                });

                this.lastSave = task;

                return task.future;
            }
        }
    }

    /**
     * Block on the given future and print error messages about failing to
     * load the database on error.
     *
     * @param future the future
     */
    private void blockOnLoad(Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.log(Level.WARNING, "Failed to load the region database", e);
        }
    }

    /**
     * Block on the given future and print error messages about failing to
     * save the database on error.
     *
     * @param future the future
     */
    private void blockOnSave(Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.log(Level.WARNING, "Failed to save the region database", e);
        }
    }

    /**
     * Call to execute a load that may occur in any thread. However, no
     * other save or load operation will be simultaneously ongoing.
     */
    protected abstract void performLoad() throws ProtectionDatabaseException;

    /**
     * Call to execute a save that may occur in any thread. However, no
     * other save or load operation will be simultaneously ongoing.
     *
     * <p>{@link #setRegions(Map)} must not be called until loading
     * has completed and the provided map is in its completed state.</p>
     */
    protected abstract void performSave() throws ProtectionDatabaseException;

    /**
     * Stores information about the a queued task.
     */
    private static class QueuedTask {
        private boolean started = false;
        private ListenableFuture<?> future;
    }

}
