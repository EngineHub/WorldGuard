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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.sk89q.worldguard.domains.ApiDomain;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimpleDomainRegistry implements DomainRegistry {
    private static final Logger log = Logger.getLogger(SimpleDomainRegistry.class.getCanonicalName());

    private final Object lock = new Object();
    private final ConcurrentMap<String, Factory<?>> domains = Maps.newConcurrentMap();
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public void register(String name, Factory<?> domain) throws DomainConflictException {
        synchronized (lock) {
            if (initialized) {
                throw new IllegalStateException("New flags cannot be registered at this time");
            }

            forceRegister(name, domain);
        }
    }

    @Override
    public void registerAll(Map<String, Factory<?>> domains) {
        synchronized (lock) {
            for (Map.Entry<String, Factory<?>> entry : domains.entrySet()) {
                try {
                    register(entry.getKey(), entry.getValue());
                } catch (DomainConflictException e) {
                    log.log(Level.WARNING, e.getMessage());
                }
            }
        }
    }

    private <T extends Factory<?>> T forceRegister(String name, T domain) throws DomainConflictException {
        checkNotNull(domain, "domain");
        checkNotNull(name, "name");

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
    public Factory<?> get(String name) {
        checkNotNull(name, "name");
        return domains.get(name.toLowerCase());
    }

    @Override
    public List<Factory<?>> getAll() {
        return ImmutableList.copyOf(domains.values());
    }

    private ApiDomain getOrCreate(String name, Object value, boolean createUnknown) {
        Factory<? extends ApiDomain> domain = get(name);

        if (domain != null) {
            return domain.create(name, value);
        }

        synchronized (lock) {
            domain = get(name); // Load again because the previous load was not synchronized
            if (domain != null) {
                return domain.create(name, value);
            }
            if (createUnknown) {
                Factory<UnknownDomain> unknownFactory = forceRegister(name, UnknownDomain.FACTORY);
                return unknownFactory.create(name, value);
            }
        }
        return null;
    }

    public List<ApiDomain> unmarshal(Map<String, Object> rawValues, boolean createUnknown) {
        checkNotNull(rawValues, "rawValues");

        List<ApiDomain> domainList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : rawValues.entrySet()) {
            try {
                ApiDomain domain = getOrCreate(entry.getKey(), entry.getValue(), createUnknown);
                domainList.add(domain);
            } catch (Throwable e) {
                log.log(Level.WARNING, "Failed to unmarshal domain for " + entry.getKey(), e);
            }
        }
        return domainList;
    }

    @Override
    public int size() {
        return domains.size();
    }

    @Override
    public Iterator<DomainRegistry.Factory<?>> iterator() {
        return Iterators.unmodifiableIterator(domains.values().iterator());
    }
}
