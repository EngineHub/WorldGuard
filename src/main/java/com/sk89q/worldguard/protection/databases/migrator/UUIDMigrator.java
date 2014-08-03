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

package com.sk89q.worldguard.protection.databases.migrator;

import com.google.common.base.Predicate;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.sk89q.worldguard.bukkit.ConfigurationManager;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.domains.PlayerDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
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

public class UUIDMigrator {

    private static final int LOG_DELAY = 5000;

    private final Timer timer = new Timer();
    private final ProfileService profileService;
    private final Logger logger;
    private final ConcurrentMap<String, UUID> resolvedNames = new ConcurrentHashMap<String, UUID>();
    private final Set<String> unresolvedNames = new HashSet<String>();
    private boolean keepUnresolvedNames = true;

    public UUIDMigrator(ProfileService profileService, Logger logger) {
        checkNotNull(profileService);
        checkNotNull(logger);

        this.profileService = profileService;
        this.logger = logger;
    }

    public boolean migrate(Collection<RegionManager> managers) throws MigrationException {
        Set<String> names = new HashSet<String>();

        // Name scan pass
        logger.log(Level.INFO, "UUID migrator: Gathering names to convert...");
        for (RegionManager manager : managers) {
            scanForNames(manager, names);
        }

        if (names.isEmpty()) {
            logger.log(Level.INFO, "UUID migrator: No names to convert!");
            return false;
        }

        TimerTask resolvedTask = new ResolvedNamesTimerTask();
        try {
            timer.schedule(resolvedTask, LOG_DELAY, LOG_DELAY);
            logger.log(Level.INFO, "UUID migrator: Resolving " + names.size() + " name(s) into UUIDs... this may take a while.");
            profileService.findAllByName(names, new Predicate<Profile>() {
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
            resolvedTask.cancel();
        }

        logger.log(Level.INFO, "UUID migrator: UUIDs resolved... now migrating all regions to UUIDs where possible...");
        for (RegionManager manager : managers) {
            convertToUniqueIds(manager);
        }

        if (!unresolvedNames.isEmpty()) {
            if (keepUnresolvedNames) {
                logger.log(Level.WARNING,
                        "UUID migrator: Some member and owner names do not seem to exist or own Minecraft so they " +
                        "could not be converted into UUIDs. They have been left as names, but the conversion can " +
                        "be re-run with 'keep-names-that-lack-uuids' set to false in the configuration in " +
                        "order to remove these names. Leaving the names means that someone can register with one of " +
                        "these names in the future and become that player.");
            } else {
                logger.log(Level.WARNING,
                        "UUID migrator: Some member and owner names do not seem to exist or own Minecraft so they " +
                        "could not be converted into UUIDs. These names have been removed.");
            }
        }

        logger.log(Level.INFO, "UUID migrator: Migration finished!");

        return true;
    }

    private void scanForNames(RegionManager manager, Set<String> target) {
        for (ProtectedRegion region : manager.getRegions().values()) {
            target.addAll(region.getOwners().getPlayers());
            target.addAll(region.getMembers().getPlayers());
        }
    }

    private void convertToUniqueIds(RegionManager manager) {
        for (ProtectedRegion region : manager.getRegions().values()) {
            convertToUniqueIds(region.getOwners());
            convertToUniqueIds(region.getMembers());
        }
    }

    private void convertToUniqueIds(DefaultDomain domain) {
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

    public boolean isKeepUnresolvedNames() {
        return keepUnresolvedNames;
    }

    public void setKeepUnresolvedNames(boolean keepUnresolvedNames) {
        this.keepUnresolvedNames = keepUnresolvedNames;
    }

    public void readConfiguration(ConfigurationManager config) {
        setKeepUnresolvedNames(config.keepUnresolvedNames);
    }

    private class ResolvedNamesTimerTask extends TimerTask {
        @Override
        public void run() {
            logger.info("UUID migrator: UUIDs have been found for " + resolvedNames.size() + " name(s)...");
        }
    }

}
