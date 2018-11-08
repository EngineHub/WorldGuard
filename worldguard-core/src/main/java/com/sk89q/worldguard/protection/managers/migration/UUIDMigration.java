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
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.storage.RegionDatabase;
import com.sk89q.worldguard.protection.managers.storage.RegionDriver;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.IOException;
import java.util.*;
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
    private final FlagRegistry flagRegistry;
    private final ConcurrentMap<String, UUID> resolvedNames = new ConcurrentHashMap<>();
    private final Set<String> unresolvedNames = new HashSet<>();
    private boolean keepUnresolvedNames = true;

    /**
     * Create a new instance.
     *
     * @param driver the storage driver
     * @param profileService the profile service
     * @param flagRegistry the flag registry
     */
    public UUIDMigration(RegionDriver driver, ProfileService profileService, FlagRegistry flagRegistry) {
        super(driver);
        checkNotNull(profileService);
        checkNotNull(flagRegistry, "flagRegistry");
        this.profileService = profileService;
        this.flagRegistry = flagRegistry;
    }

    @Override
    protected void migrate(RegionDatabase store) throws MigrationException {
        log.log(Level.INFO, "Миграция регионов в '" + store.getName() + "' для преобразования имен -> UUIDs...");

        Set<ProtectedRegion> regions;

        try {
            regions = store.loadAll(flagRegistry);
        } catch (StorageException e) {
            throw new MigrationException("Не удалось загрузить данные региона для мира '" + store.getName() + "'", e);
        }

        migrate(regions);

        try {
            store.saveAll(regions);
        } catch (StorageException e) {
            throw new MigrationException("Не удалось сохранить данные региона после миграции мира '" + store.getName() + "'", e);
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

                log.log(Level.INFO, "Решение " + names.size() + " имени(ей) в UUIDs... это может занять некоторое время.");

                // Don't lookup names that we already looked up for previous
                // worlds -- note: all names are lowercase in these collections
                Set<String> lookupNames = new HashSet<>(names);
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
                throw new MigrationException("Имя -> Сбой UUID", e);
            } catch (InterruptedException e) {
                throw new MigrationException("Миграция была прервана");
            } finally {
                // Stop showing the % converted messages
                task.cancel();
            }

            // Name -> UUID in all regions
            log.log(Level.INFO, "UUIDs решено... теперь мигрируются все регионы UUID, где это возможно...");
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
                        "Некоторые имена участников и владельцев не существуют, " +
                                "поэтому они не могут быть преобразованы в UUID. Они были оставлены в качестве имен, но преобразование можно повторно " +
                                "запустить с 'keep-names-that-lack-uuids' установленым false в конфигурации " +
                                "чтобы удалить эти имена. Если оставить имена, это значит, что кто-то сможет зарегистрироваться с одним из " +
                                "тех имен в будущем и стать тем игроком.");
            } else {
                log.log(Level.WARNING,
                        "Некоторые имена участников и владельцев не существуют, " +
                                "поэтому они не могут быть преобразованы в UUID. Эти имена были удалены.");
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
        Set<String> names = new HashSet<>();
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
            log.info("UUIDs был найден для имени(ей) " + resolvedNames.size() + "...");
        }
    }

}
