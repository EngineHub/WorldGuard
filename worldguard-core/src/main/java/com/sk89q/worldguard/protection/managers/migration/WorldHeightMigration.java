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

package com.sk89q.worldguard.protection.managers.migration;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldHeightMigration extends AbstractMigration {
    private static final Logger log = Logger.getLogger(WorldHeightMigration.class.getCanonicalName());

    private static final Field minField;
    private static final Field maxField;
    static {
        try {
            minField = ProtectedRegion.class.getDeclaredField("min");
            minField.setAccessible(true);
            maxField = ProtectedRegion.class.getDeclaredField("max");
            maxField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(new MigrationException("Migrator broke.", e));
        }
    }

    private final FlagRegistry flagRegistry;
    private final World world;
    private int changed = 0;

    public WorldHeightMigration(RegionDriver driver, FlagRegistry flagRegistry, @Nullable World world) {
        super(driver);
        this.flagRegistry = flagRegistry;
        this.world = world;
    }

    @Override
    protected void migrate(RegionDatabase store) throws MigrationException {
        if (world != null && !store.getName().equals(world.getName())) return;

        log.log(Level.INFO, "Migrating regions in '" + store.getName() + "' to new height limits...");

        Set<ProtectedRegion> regions;

        try {
            regions = store.loadAll(flagRegistry);
        } catch (StorageException e) {
            throw new MigrationException("Failed to load region data for the world '" + store.getName() + "'", e);
        }

        int min = -64;
        int max = 319;
        World world = WorldGuard.getInstance().getPlatform().getMatcher().getWorldByName(store.getName());
        if (world != null) {
            min = world.getMinY();
            max = world.getMaxY();
            // in theory someone could run a data pack that keeps their world height
            // at the old defaults...? either way if this is the case there are no changes to make.
            if (min == 0 && max == 255) return;
        }
        for (ProtectedRegion region : regions) {
            if (region.getMinimumPoint().y() <= 0
                    && region.getMaximumPoint().y() >= 255) {
                expand(region, min, max);
                changed++;
            }
        }
        try {
            store.saveAll(regions);
        } catch (StorageException e) {
            throw new MigrationException("Failed to save region data after migration of the world '" + store.getName() + "'", e);
        }
    }

    private static void expand(ProtectedRegion region, int min, int max) throws MigrationException {
        try {
            minField.set(region, region.getMinimumPoint().withY(min));
            maxField.set(region, region.getMaximumPoint().withY(max));
            region.setDirty(true);
        } catch (IllegalAccessException e) {
            throw new MigrationException("Migrator broke.", e);
        }
    }

    @Override
    protected void postMigration() {
        log.log(Level.INFO, "A total of " + changed + " top-to-bottom regions were vertically expanded.");
    }
}
