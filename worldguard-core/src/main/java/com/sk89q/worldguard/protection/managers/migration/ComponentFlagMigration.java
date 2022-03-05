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
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComponentFlagMigration extends AbstractMigration {
    private static final Logger log = Logger.getLogger(ComponentFlagMigration.class.getCanonicalName());

    private static final Set<StringFlag> REPLACE_FLAGS = Set.of(
            Flags.TELE_MESSAGE,
            Flags.GREET_MESSAGE,
            Flags.FAREWELL_MESSAGE,
            Flags.GREET_TITLE,
            Flags.FAREWELL_TITLE,
            Flags.DENY_MESSAGE,
            Flags.ENTRY_DENY_MESSAGE,
            Flags.EXIT_DENY_MESSAGE
    );

    private static final Map<String, String> REPLACE_TEXTS = Map.of(
            "%what%", "<what>",
            "%name%", "<name>",
            "%id%", "<id>",
            "%online%", "<online>",
            "%world%", "<world>",
            "%health%", "<health>"
    );

    private final FlagRegistry flagRegistry;
    private final World world;
    private int changed = 0;

    public ComponentFlagMigration(RegionDriver driver, FlagRegistry flagRegistry, @Nullable World world) {
        super(driver);
        this.flagRegistry = flagRegistry;
        this.world = world;
    }

    @Override
    protected void migrate(RegionDatabase store) throws MigrationException {
        if (world != null && !store.getName().equals(world.getName())) return;

        log.log(Level.INFO, "Migrating regions in '" + store.getName() + "' to new component formats...");

        Set<ProtectedRegion> regions;
        try {
            regions = store.loadAll(flagRegistry);
        } catch (StorageException e) {
            throw new MigrationException("Failed to load region data for the world '" + store.getName() + "'", e);
        }
        for (ProtectedRegion region : regions) {
            for (StringFlag replaceFlag : REPLACE_FLAGS) {
                String val = region.getFlag(replaceFlag);
                if (val != null) {
                    String migration = migrateToMinimessage(val);
                    if (migration != null) {
                        region.setFlag(replaceFlag, migration);
                        changed++;
                    }
                }
            }
        }
        try {
            store.saveAll(regions);
        } catch (StorageException e) {
            throw new MigrationException("Failed to save region data after migration of the world '" + store.getName() + "'", e);
        }
    }

    private String migrateToMinimessage(String string) {
        String serialized = WorldGuard.getInstance().getPlatform().formatToMiniMessage(string);
        if (serialized == null) return null;
        for (Map.Entry<String, String> replacement : REPLACE_TEXTS.entrySet()) {
            serialized = serialized.replace(replacement.getKey(), replacement.getValue());
        }
        return serialized;
    }

    @Override
    protected void postMigration() {
        log.log(Level.INFO, "A total of " + changed + " flags were updated to a new component.");
    }
}
