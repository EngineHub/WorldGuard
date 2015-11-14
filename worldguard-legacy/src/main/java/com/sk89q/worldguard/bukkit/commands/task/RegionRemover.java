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

package com.sk89q.worldguard.bukkit.commands.task;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Removes a region.
 */
public class RegionRemover implements Callable<Set<ProtectedRegion>> {

    private final RegionManager manager;
    private final ProtectedRegion region;
    @Nullable
    private RemovalStrategy removalStrategy;

    /**
     * Create a new instance.
     *
     * @param manager a region manager
     * @param region the region to remove
     */
    public RegionRemover(RegionManager manager, ProtectedRegion region) {
        checkNotNull(manager);
        checkNotNull(region);
        this.manager = manager;
        this.region = region;
    }

    /**
     * GSet a parent removal strategy.
     *
     * @return a removal strategy or null (see{@link #setRemovalStrategy(RemovalStrategy)}
     */
    @Nullable
    public RemovalStrategy getRemovalStrategy() {
        return removalStrategy;
    }

    /**
     * Set a parent removal strategy. Set it to {@code null} to have the code
     * check for children and throw an error if any are found.
     *
     * @param removalStrategy a removal strategy, or {@code null} to error if children exist
     */
    public void setRemovalStrategy(@Nullable RemovalStrategy removalStrategy) {
        this.removalStrategy = removalStrategy;
    }

    @Override
    public Set<ProtectedRegion> call() throws Exception {
        if (removalStrategy == null) {
            for (ProtectedRegion test : manager.getRegions().values()) {
                ProtectedRegion parent = test.getParent();
                if (parent != null && parent.equals(region)) {
                    throw new CommandException(
                            "The region '" + region.getId() + "' has child regions. Use -f to force removal of children " +
                                    "or -u to unset the parent value of these children.");
                }
            }

            return manager.removeRegion(region.getId(), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
        } else {
            return manager.removeRegion(region.getId(), removalStrategy);
        }
    }
}
