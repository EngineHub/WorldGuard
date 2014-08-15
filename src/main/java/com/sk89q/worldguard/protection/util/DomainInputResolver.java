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

package com.sk89q.worldguard.protection.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.sk89q.squirrelid.util.UUIDs;
import com.sk89q.worldguard.domains.DefaultDomain;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resolves input for a domain (i.e. "player1 player2 &lt;uuid&gt; g:group").
 */
public class DomainInputResolver implements Callable<DefaultDomain> {

    private static final Pattern GROUP_PATTERN = Pattern.compile("(?i)^[G]:(.+)$");

    /**
     * The policy for locating users.
     */
    public enum UserLocatorPolicy {
        UUID_ONLY,
        NAME_ONLY,
        UUID_AND_NAME
    }

    private final ProfileService profileService;
    private final String[] input;
    private UserLocatorPolicy locatorPolicy = UserLocatorPolicy.UUID_ONLY;

    /**
     * Create a new instance.
     *
     * @param profileService the profile service
     * @param input the input to parse
     */
    public DomainInputResolver(ProfileService profileService, String[] input) {
        checkNotNull(profileService);
        checkNotNull(input);
        this.profileService = profileService;
        this.input = input;
    }

    /**
     * Get the policy used for identifying users.
     *
     * @return the policy
     */
    public UserLocatorPolicy getLocatorPolicy() {
        return locatorPolicy;
    }

    /**
     * Set the policy used for identifying users.
     *
     * @param locatorPolicy the policy
     */
    public void setLocatorPolicy(UserLocatorPolicy locatorPolicy) {
        checkNotNull(locatorPolicy);
        this.locatorPolicy = locatorPolicy;
    }

    @Override
    public DefaultDomain call() throws UnresolvedNamesException {
        DefaultDomain domain = new DefaultDomain();
        List<String> namesToQuery = new ArrayList<String>();

        for (String s : input) {
            Matcher m = GROUP_PATTERN.matcher(s);
            if (m.matches()) {
                domain.addGroup(m.group(1));
            } else {
                UUID uuid = parseUUID(s);
                if (uuid != null) {
                    // Try to add any UUIDs given
                    domain.addPlayer(UUID.fromString(UUIDs.addDashes(s.replaceAll("^uuid:", ""))));
                } else {
                    switch (locatorPolicy) {
                        case NAME_ONLY:
                            domain.addPlayer(s);
                            break;
                        case UUID_ONLY:
                            namesToQuery.add(s.toLowerCase());
                            break;
                        case UUID_AND_NAME:
                            domain.addPlayer(s);
                            namesToQuery.add(s.toLowerCase());
                    }
                }
            }
        }

        if (!namesToQuery.isEmpty()) {
            try {
                for (Profile profile : profileService.findAllByName(namesToQuery)) {
                    namesToQuery.remove(profile.getName().toLowerCase());
                    domain.addPlayer(profile.getUniqueId());
                }
            } catch (IOException e) {
                throw new UnresolvedNamesException("The UUID lookup service failed so the names entered could not be turned into UUIDs");
            } catch (InterruptedException e) {
                throw new UnresolvedNamesException("UUID lookup was interrupted");
            }
        }

        if (!namesToQuery.isEmpty()) {
            throw new UnresolvedNamesException("Unable to resolve the names " + Joiner.on(", ").join(namesToQuery));
        }

        return domain;
    }

    public Function<DefaultDomain, DefaultDomain> createAddAllFunction(final DefaultDomain target) {
        return new Function<DefaultDomain, DefaultDomain>() {
            @Override
            public DefaultDomain apply(@Nullable DefaultDomain domain) {
                target.addAll(domain);
                return domain;
            }
        };
    }

    public Function<DefaultDomain, DefaultDomain> createRemoveAllFunction(final DefaultDomain target) {
        return new Function<DefaultDomain, DefaultDomain>() {
            @Override
            public DefaultDomain apply(@Nullable DefaultDomain domain) {
                target.removeAll(domain);
                return domain;
            }
        };
    }

    /**
     * Try to parse a UUID locator from input.
     *
     * @param input the input
     * @return a UUID or {@code null} if the input is not a UUID
     */
    @Nullable
    public static UUID parseUUID(String input) {
        checkNotNull(input);

        try {
            return UUID.fromString(UUIDs.addDashes(input.replaceAll("^uuid:", "")));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
