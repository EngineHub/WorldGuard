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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.sk89q.worldguard.domains.CustomDomain;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimpleDomainRegistry implements DomainRegistry {
    private static final Logger log = Logger.getLogger(SimpleDomainRegistry.class.getCanonicalName());

    private final Object lock = new Object();
    private final ConcurrentMap<String, DomainFactory<?>> domains = Maps.newConcurrentMap();
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public void register(String name, DomainFactory<?> domain) throws DomainConflictException {
        synchronized (lock) {
            if (initialized) {
                throw new IllegalStateException("New domains cannot be registered at this time");
            }

            forceRegister(name, domain);
        }
    }

    @Override
    public void registerAll(Map<String, DomainFactory<?>> domains) {
        synchronized (lock) {
            for (Map.Entry<String, DomainFactory<?>> entry : domains.entrySet()) {
                try {
                    register(entry.getKey(), entry.getValue());
                } catch (DomainConflictException e) {
                    log.log(Level.WARNING, e.getMessage());
                }
            }
        }
    }

    private <T extends DomainFactory<?>> T forceRegister(String name, T domain) throws DomainConflictException {
        checkNotNull(domain, "domain");
        checkNotNull(name, "name");

        if (!CustomDomain.isValidName(name)) {
            throw new IllegalArgumentException("Invalid Domain name used.");
        }

        synchronized (lock) {
            if (domains.containsKey(name)) {
                throw new DomainConflictException("A domain already exists by the name " + name);
            }

            domains.put(name, domain);
        }

        return domain;
    }

    @Nullable
    @Override
    public DomainFactory<?> get(String name) {
        checkNotNull(name, "name");
        return domains.get(name.toLowerCase());
    }

    @Nullable
    @Override
    public CustomDomain createDomain(String name) {
        DomainFactory<?> factory = get(name);
        if (factory == null) return null;
        return factory.create(name);
    }

    @Override
    public Map<String, DomainFactory<?>> getAll() {
        return ImmutableMap.copyOf(domains);
    }

    private CustomDomain getOrCreate(String name, Object value, boolean createUnknown) {
        CustomDomain customDomain = createDomain(name);

        if (customDomain != null) {
            customDomain.unmarshal(value);
            return customDomain;
        }

        synchronized (lock) {
            customDomain = createDomain(name); // Load again because the previous load was not synchronized
            if (customDomain != null) {
                customDomain.unmarshal(value);
                return customDomain;
            }
            if (createUnknown) {
                DomainFactory<UnknownDomain> unknownFactory = forceRegister(name, UnknownDomain.FACTORY);
                if (unknownFactory != null) {
                    customDomain = unknownFactory.create(name);
                    if (customDomain != null) customDomain.unmarshal(value);
                    return customDomain;
                }
            }
        }
        return null;
    }

    public Map<String, CustomDomain> unmarshal(Map<String, Object> rawValues, boolean createUnknown) {
        checkNotNull(rawValues, "rawValues");

        Map<String, CustomDomain> domains = new HashMap<>();

        for (Map.Entry<String, Object> entry : rawValues.entrySet()) {
            try {
                CustomDomain domain = getOrCreate(entry.getKey(), entry.getValue(), createUnknown);
                domains.put(domain.getName(), domain);
            } catch (Throwable e) {
                log.log(Level.WARNING, "Failed to unmarshal domain for " + entry.getKey(), e);
            }
        }
        return domains;
    }

    @Override
    public int size() {
        return domains.size();
    }

    @Override
    public Iterator<DomainFactory<?>> iterator() {
        return Iterators.unmodifiableIterator(domains.values().iterator());
    }
}
