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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimpleCustomDomainRegistry implements CustomDomainRegistry {
    private static final Logger log = Logger.getLogger(CustomDomainRegistry.class.getCanonicalName());

    private final Object lock = new Object();
    private final ConcurrentMap<String, CustomDomain> domains = Maps.newConcurrentMap();
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public void register(CustomDomain customDomain) throws CustomDomainConflictException {
        synchronized (lock) {
            if(initialized) {
                throw new IllegalStateException("New custom domains cannot be registered at this time");
            }

            forceRegister(customDomain);
        }
    }

    @Override
    public void registerAll(Collection<CustomDomain> customDomains) {
        synchronized (lock) {
            for (CustomDomain domain : customDomains) {
                try {
                    register(domain);
                } catch (CustomDomainConflictException e) {
                    log.log(Level.WARNING, e.getMessage());
                }
            }
        }
    }

    private CustomDomain forceRegister(CustomDomain domain) throws CustomDomainConflictException {
        checkNotNull(domain, "domain");
        checkNotNull(domain.getName(), "domain.getName()");

        synchronized (lock) {
            String name = domain.getName().toLowerCase();
            if(domains.containsKey(name)) {
                throw new CustomDomainConflictException("A domain already exists by the name " + name);
            }

            domains.put(name, domain);
        }

        return domain;
    }

    @Nullable
    @Override
    public CustomDomain get(String name) {
        checkNotNull(name);
        return domains.get(name.toLowerCase());
    }

    @Override
    public List<CustomDomain> getAll() {
        return Lists.newArrayList(this.domains.values());
    }

    private CustomDomain getOrCreate(String name) {
        CustomDomain domain = get(name);

        if(domain != null) {
            return domain;
        }

        synchronized (lock) {
            domain = get(name);
            return domain != null ? domain : forceRegister(new CustomDomain(name));
        }
    }

    @Override
    public int size() {
        return domains.size();
    }

    @Override
    public Iterator<CustomDomain> iterator() {
        return Iterators.unmodifiableIterator(domains.values().iterator());
    }
}
