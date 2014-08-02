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

package com.sk89q.worldguard.protection.databases.util;

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
 *
 * <p>Unless {@link #getUseNames()} is true, names will be resolved into
 * UUIDs.</p>
 */
public class DomainInputResolver implements Callable<DefaultDomain> {

    private static final Pattern GROUP_PATTERN = Pattern.compile("(?i)^[G]:(.+)$");

    private final ProfileService profileService;
    private final String[] input;
    private boolean useNames = false;

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
     * Get whether names should be used rather than UUIDs.
     *
     * @return true to use names
     */
    public boolean getUseNames() {
        return useNames;
    }

    /**
     * Set whether names should be used rather than UUIDs.
     *
     * @param useNames true to use names
     */
    public void setUseNames(boolean useNames) {
        this.useNames = useNames;
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
                try {
                    domain.addPlayer(UUID.fromString(UUIDs.addDashes(s)));
                } catch (IllegalArgumentException e) {
                    if (useNames) {
                        domain.addPlayer(s);
                    } else {
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
}
