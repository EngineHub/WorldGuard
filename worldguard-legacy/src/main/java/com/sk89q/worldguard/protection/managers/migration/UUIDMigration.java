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

import com.google.common.base.Predicate;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.domains.PlayerDomain;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Migrates names to UUIDs for all the worlds in a region store.
 */
public class UUIDMigration extends AbstractMigration {

    private static final Logger log = Logger.getLogger(UUIDMigration.class.getCanonicalName());
    private static final int LOG_DELAY = 5000;

    private final Timer timer = new Timer();
    private final ProfileService profileService;
    private final ConcurrentMap<String, UUID> resolvedNames = new ConcurrentHashMap<String, UUID>();
    private final Set<String> unresolvedNames = new HashSet<String>();
    private boolean keepUnresolvedNames = true;

    /**
     * Create a new instance.
     *
     * @param driver the storage driver
     * @param profileService the profile service
     */
    public UUIDMigration(RegionDriver driver, ProfileService profileService) {
        super(driver);
        checkNotNull(profileService);
        this.profileService = profileService;
    }

    @Override
    protected void migrate(RegionDatabase store) throws MigrationException {
        log.log(Level.INFO, "Migrating regions in '" + store.getName() + "' to convert names -> UUIDs...");

        Set<ProtectedRegion> regions;

        try {
            regions = store.loadAll();
        } catch (StorageException e) {
            throw new MigrationException("Failed to load region data for the world '" + store.getName() + "'", e);
        }

        migrate(regions);

        try {
            store.saveAll(regions);
        } catch (StorageException e) {
            throw new MigrationException("Failed to save region data after migration of the world '" + store.getName() + "'", e);
        }
    }

    private boolean migrate(Collection<ProtectedRegion> regions) throws MigrationException {
        // Name scan pass
        Set<String> names = getNames(regions);

        if (!names.isEmpty()) {
            // This task logs the progress of conversion (% converted...)
            // periodically
            TimerTask task = new ResolvedNamesTimerTask();

            try {
                timer.schedule(task, LOG_DELAY, LOG_DELAY);

                log.log(Level.INFO, "Resolving " + names.size() + " name(s) into UUIDs... this may take a while.");

                // Don't lookup names that we already looked up for previous
                // worlds -- note: all names are lowercase in these collections
                Set<String> lookupNames = new HashSet<String>(names);
                lookupNames.removeAll(resolvedNames.keySet());

                // Ask Mojang for names
                profileService.findAllByName(lookupNames, new Predicate<Profile>() {
                    @Override
                    public boolean apply(Profile profile) {
                        resolvedNames.put(profile.getName().toLowerCase(), profile.getUniqueId());
                        return true;
                    }
                });
            } catch (IOException e) {
                throw new MigrationException("The name -> UUID service failed", e);
            } catch (InterruptedException e) {
                throw new MigrationException("The migration was interrupted");
            } finally {
                // Stop showing the % converted messages
                task.cancel();
            }

            // Name -> UUID in all regions
            log.log(Level.INFO, "UUIDs resolved... now migrating all regions to UUIDs where possible...");
            convert(regions);

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void postMigration() {
        if (!unresolvedNames.isEmpty()) {
            if (keepUnresolvedNames) {
                log.log(Level.WARNING,
                        "Some member and owner names do not seem to exist or own Minecraft so they " +
                                "could not be converted into UUIDs. They have been left as names, but the conversion can " +
                                "be re-run with 'keep-names-that-lack-uuids' set to false in the configuration in " +
                                "order to remove these names. Leaving the names means that someone can register with one of " +
                                "these names in the future and become that player.");
            } else {
                log.log(Level.WARNING,
                        "Some member and owner names do not seem to exist or own Minecraft so they " +
                                "could not be converted into UUIDs. These names have been removed.");
            }
        }
    }

    /**
     * Grab all the player names from all the regions in the given collection.
     *
     * @param regions a collection of regions
     * @return a set of names
     */
    private static Set<String> getNames(Collection<ProtectedRegion> regions) {
        Set<String> names = new HashSet<String>();
        for (ProtectedRegion region : regions) {
            // Names are already lower case
            names.addAll(region.getOwners().getPlayers());
            names.addAll(region.getMembers().getPlayers());
        }
        return names;
    }

    /**
     * Convert all the names to UUIDs.
     *
     * @param regions a collection of regions
     */
    private void convert(Collection<ProtectedRegion> regions) {
        for (ProtectedRegion region : regions) {
            convert(region.getOwners());
            convert(region.getMembers());
        }
    }

    /**
     * Convert all the names to UUIDs.
     *
     * @param domain the domain
     */
    private void convert(DefaultDomain domain) {
        PlayerDomain playerDomain = new PlayerDomain();
        for (UUID uuid : domain.getUniqueIds()) {
            playerDomain.addPlayer(uuid);
        }

        for (String name : domain.getPlayers()) {
            UUID uuid = resolvedNames.get(name.toLowerCase());
            if (uuid != null) {
                playerDomain.addPlayer(uuid);
            } else {
                if (keepUnresolvedNames) {
                    playerDomain.addPlayer(name);
                }
                unresolvedNames.add(name);
            }
        }

        domain.setPlayerDomain(playerDomain);
    }

    /**
     * Get whether names that have no UUID equivalent (i.e. name that is not
     * owned) should be kept as names and not removed.
     *
     * @return true to keep names
     */
    public boolean getKeepUnresolvedNames() {
        return keepUnresolvedNames;
    }

    /**
     * Set whether names that have no UUID equivalent (i.e. name that is not
     * owned) should be kept as names and not removed.
     *
     * @param keepUnresolvedNames true to keep names
     */
    public void setKeepUnresolvedNames(boolean keepUnresolvedNames) {
        this.keepUnresolvedNames = keepUnresolvedNames;
    }

    /**
     * A task to periodically say how many names have been resolved.
     */
    private class ResolvedNamesTimerTask extends TimerTask {
        @Override
        public void run() {
            log.info("UUIDs have been found for " + resolvedNames.size() + " name(s)...");
        }
    }

}
