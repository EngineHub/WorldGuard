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

package com.sk89q.worldguard.domains.registry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface CustomDomainRegistry extends Iterable<CustomDomain> {

    /**
     * Register a new custom Domain.
     *
     * <p>There may be an appropriate time to register this. If domains are
     * registered outside this time, then an exception may be thrown.</p>
     *
     * @param customDomain the domain
     * @throws CustomDomainConflictException Thrown when an existing domain exists with the same name
     * @throws IllegalStateException If it is not the right time to register new domains
     */
    void register(CustomDomain customDomain) throws CustomDomainConflictException;

    /**
     * Register a collection of domains.
     *
     * <p>There may be an appropriate time to register domains. If domains are
     * registered outside this time, then an exception may be thrown.</p>
     *
     * <p>If there is a domain conflict, then an error will be logged but
     * no exception will be thrown.</p>
     *
     * @param customDomains a collection of flags
     * @throws IllegalStateException If it is not the right time to register new flags
     */
    void registerAll(Collection<CustomDomain> customDomains);

    /**
     * Get a domain by its name.
     *
     * @param name The name
     * @return The domain, if it has been registered
     */
    @Nullable
    CustomDomain get(String name);

    /**
     * Get all domains
     *
     * @return All domains
     */
    List<CustomDomain> getAll();

    /*
     * Unmarshal a raw map of values into a map of flags with their
     * unmarshalled values.
     *
     * @param rawValues The raw values map
     * @param createUnknown Whether "just in time" flags should be created for unknown flags
     * @return The unmarshalled flag values map
     */
//    Map<Flag<?>, Object> unmarshal(Map<String, Object> rawValues, boolean createUnknown);

    /**
     * Get the number of registered flags.
     *
     * @return The number of registered flags
     */
    int size();
}
