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

package com.sk89q.worldguard.domains;

import com.sk89q.worldguard.domains.registry.CustomDomainContext;
import com.sk89q.worldguard.domains.registry.InvalidDomainFormatException;

import java.util.Objects;
import java.util.UUID;

public class CustomUUIDDomain extends CustomDomain {
    private UUID test;

    public CustomUUIDDomain(String name, UUID test) {
        super(name);
        this.test = test;
    }

    @Override
    public void parseInput(CustomDomainContext context) throws InvalidDomainFormatException {
        throw new InvalidDomainFormatException("not supported");
    }

    @Override
    public void unmarshal(Object o) {
    }

    @Override
    public Object marshal() {
        return null;
    }

    @Override
    public boolean contains(UUID uniqueId) {
        return Objects.equals(test, uniqueId);
    }

    @Override
    public boolean contains(String playerName) {
        return false;
    }

    @Override
    public void clear() {
        test = null;
    }
}
